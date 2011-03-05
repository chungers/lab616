#include <iostream>
#include <map>
#include <stdio.h>
#include <string.h>
#include <vector>

#include <boost/algorithm/string.hpp>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/thread.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <zmq.hpp>

#include "mongoose/mongoose.h"

#include "common.hpp">
#include "utils.hpp"
#include "varz.hpp"


using namespace std;
using namespace boost;
using namespace lab616::utils;

DEFINE_string(endpoint, "tcp://localhost:5555", "Publisher end point.");
DEFINE_string(symbol, "AAPL", "Symbol to subscribe");
DEFINE_bool(dumpmessage, false, "True to dump message.");

DEFINE_VARZ_int64(start_ts, now_micros(), "Start real timestamp in micros.");
DEFINE_VARZ_int64(data_start, 0, "Start event timestamp in micros.");
DEFINE_VARZ_int64(data_ts, 0, "Event timestamp.");
DEFINE_VARZ_int32(messages, 0, "Number of messages.");
DEFINE_VARZ_double(message_rate, 0., "Message rate");

struct Instrument;
static map<int, Instrument*> TICKERS;
static string TICK_TYPES[] = { "BID", "ASK", "LAST" };

struct Instrument {

  Instrument(const string& s, double lastp) : symbol(s), lastPrice(lastp) {
    TICKERS[TICKERS.size()] = this;
  }
  const string symbol;
  double lastPrice;
  string type;
};

template <typename T> static bool receive(zmq::socket_t & socket, T& output) {
  zmq::message_t message;
  socket.recv(&message);
  memcpy(reinterpret_cast<void *>(&output), message.data(), sizeof(T));
  int64_t more;           //  Multipart detection
  size_t more_size = sizeof (more);
  socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
  VLOG(20) << '[' << output << '/' << more << ']' << std::endl;
  return more;
}

static bool receive(zmq::socket_t & socket, std::string* output) {
  zmq::message_t message;
  socket.recv(&message);
  output->assign(static_cast<char*>(message.data()), message.size());
  int64_t more;           //  Multipart detection
  size_t more_size = sizeof (more);
  socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
  VLOG(20) << '"' << *output << '/' << more << '"' << std::endl;
  return more;
}


//////////////////////////////////////////////
class Subscriber {
 public:
  Subscriber(zmq::context_t *context) :
      context(context),
      socket(*context, ZMQ_SUB) {

    socket.connect(FLAGS_endpoint.c_str());
    VLOG(1) << "Subscriber connected @ " << FLAGS_endpoint << endl;

    vector<string> symbols;
    boost::split(symbols, FLAGS_symbol, boost::is_any_of(","));

    vector<string>::iterator itr;
    for (itr = symbols.begin(); itr != symbols.end(); ++itr) {
      string sub = *itr;
      LOG(INFO) << "Adding subscription " << sub << endl;
      socket.setsockopt(ZMQ_SUBSCRIBE, sub.c_str(), sub.length());
    }
  }

  void Run() {
    LOG(INFO) << "Subscriber listening." << endl;

    bool loop = true;
    uint64_t ts = 0;
    int messages = 0;

    while (loop) {

      if (FLAGS_dumpmessage) {

        cout << "--------------------------------" << endl;
        while (1) {
          //  Process all parts of the message
          zmq::message_t message;
          socket.recv(&message);

          //  Dump the message as text or binary
          std::string data(static_cast<char*>(message.data()));
          int size = message.size();

          bool is_text = true;
          int char_nbr;
          unsigned char byte;
          for (char_nbr = 0; char_nbr < size; char_nbr++) {
            byte = data [char_nbr];
            if (byte < 32 || byte > 127)
              is_text = false;
          }
          printf ("[%03d] ", size);

          for (char_nbr = 0; char_nbr < size; char_nbr++) {
            if (is_text)
              printf ("%c", data [char_nbr]);
            else
              printf ("%02X", (unsigned char) data [char_nbr]);
          }
          printf ("\n");

          int64_t more;           //  Multipart detection
          size_t more_size = sizeof (more);
          socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);

          if (!more)
            break;      //  Last message part
        }
      } else {

        // message format = binary frames
        string symbol;
        string type;
        double price;

        assert(receive(socket, &symbol));
        assert(receive(socket, &type));
        assert(receive(socket, ts));
        assert(!receive(socket, price));

        uint64_t receiveTs = now_micros();
        uint64_t latencyMicros = receiveTs - ts;

        LOG(INFO) << messages << ' '
                  << symbol << ' '
                  << type << ' '
                  << ts << ' '
                  << price
                  << " (" << latencyMicros << ")"
                  << endl;
      }

      messages++;

      VARZ_data_ts = ts;
      VARZ_messages = messages;

      if (VARZ_data_start == 0) {
        VARZ_data_start = ts; // set once only.
      } else {
        double duration = (double) (ts - VARZ_data_start);
        double durationSeconds = duration / 1000000.;
        VARZ_message_rate = messages / durationSeconds;
      }
    }
  }

 private:
  zmq::context_t *context;
  zmq::socket_t socket;
};



static const char *mongoose_options[] = {
  "document_root", "www",
  "listening_ports", "8080",
  "num_threads", "3",
  NULL
};

static void get_qsvar(const struct mg_request_info *request_info,
                      const char *name, char *dst, size_t dst_len) {
  const char *qs = request_info->query_string;
  mg_get_var(qs, strlen(qs == NULL ? "" : qs), name, dst, dst_len);
}


static const char * HTTP_200 =
  "HTTP/1.1 200 OK\r\n"
  "Cache: no-cache\r\n"
  "Content-Type: application/json; charset=utf-8\r\n"
  "\r\n";


static void *event_handler(enum mg_event event,
                           struct mg_connection *conn,
                           const struct mg_request_info *request_info) {
  using namespace lab616::utils;
  char buff[] = "yes";
  void *processed = reinterpret_cast<void *>(buff);

  LOG(INFO) << "Request uri = " << request_info->uri
            << " query string = " << request_info->query_string
            << endl;

  if (event == MG_NEW_REQUEST) {

    if (strcmp(request_info->uri, "/echo") == 0) {
      char message[256];
      // Fetch parameter
      get_qsvar(request_info, "message", message, sizeof(message));

      VLOG(20) << "Echo request: " << message << endl;

      mg_printf(conn, "%s", HTTP_200);
      mg_printf(conn, "{ \"message\" : \"%s\" }\n", message);

    } else if (strcmp(request_info->uri, "/varz") == 0) {

      VLOG(20) << "varz request" << endl;
      mg_printf(conn, "%s", HTTP_200);
      mg_printf(conn, "%s", "{");

      vector<lab616::VarzInfo> varzs;
      lab616::GetAllVarzs(&varzs);
      int i = 0;
      for (vector<lab616::VarzInfo>::iterator varz = varzs.begin();
           varz != varzs.end();
           ++varz) {
        mg_printf(conn, "%s\"%s\" : \"%s\"",
                  ((i++ == 0) ? "\n" : ",\n"),
                  varz->name.c_str(), varz->current_value.c_str());
      }

      mg_printf(conn, "\n%s\n", "}");

    } else {
      // No suitable handler found, mark as not processed. Mongoose will
      // try to serve the request.
      processed = NULL;
    }

  } else {
    processed = NULL;
  }

  return processed;
}

////////////////////////////////////////////////////////////////////////////
// Logging command-line flags: --logtostderr --v=1
int main(int argc, char** argv)
{
  google::SetUsageMessage("Prototype for the mongoose httpd.");
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  // Start Httpd
  struct mg_context *ctx;
  ctx = mg_start(&event_handler, mongoose_options);
  assert(ctx != NULL);

  LOG(INFO) << "httpd started." << endl;

  // ZMQ Subscriber
  zmq::context_t context(1);
  Subscriber subscriber(&context);
  subscriber.Run();

  LOG(INFO) << "Stopping httpd." << endl;

  // Stop httpd
  mg_stop(ctx);

  return 0;
}
