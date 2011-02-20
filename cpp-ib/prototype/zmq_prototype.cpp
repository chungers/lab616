#include "common.hpp">
#include "utils.hpp"

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




static bool frame(zmq::socket_t & socket, const std::string & string, bool last) {
  zmq::message_t message(string.size());
  memcpy(message.data(), string.data(), string.size());
  bool rc = socket.send(message, last ? 0 : ZMQ_SNDMORE);
  return (rc);
}

static bool frame(zmq::socket_t & socket, const std::string & string) {
  return frame(socket, string, false);
}

static bool last(zmq::socket_t & socket, const std::string & string) {
  return frame(socket, string, true);
}

template <typename T> static bool frame(zmq::socket_t & socket, const T& data, bool last) {
  zmq::message_t message(sizeof(T));
  memcpy(message.data(), reinterpret_cast<const void*>(&data), sizeof(T));
  bool rc = socket.send(message, last ? 0 : ZMQ_SNDMORE);
  return (rc);
}

template <typename T> static bool frame(zmq::socket_t & socket, const T& data) {
  return frame(socket, data, false);
}

template <typename T> static bool last(zmq::socket_t & socket, const T& data) {
  return frame(socket, data, true);
}

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

using namespace std;
using namespace boost;
using namespace lab616::utils;

DEFINE_int32(test, 0, "0 = hello server, 1 = pubsub");
DEFINE_bool(server, false, "Start server.");
DEFINE_string(endpoint, "tcp://*:5555", "End point string.");
DEFINE_int32(messages, 10, "number of messages.");
DEFINE_double(sleep, 50.0, "Milliseconds to sleep before sedning test messages.");
DEFINE_bool(dataframe, false, "True if data is sent as binary frames.");
DEFINE_bool(testmessage, false, "Ture if sending string 'TEST'.");
DEFINE_string(symbol, "AAPL", "Symbol to subscribe (for --test=1 pubsub test subscriber.");
DEFINE_bool(dumpmessage, false, "True to dump message.");

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

inline void GeneratePrice(Instrument& ticker) {
  int sign = ((rand() % 10 - 4) > 0) ? +1 : -1;  // 60% in favor of +
  double percentChange = (rand() % 21) / 100.f; // 0 - 0.2% change
  double priceChange = sign * percentChange / 100.f * ticker.lastPrice;
  VLOG(6) << sign
          << " * %=" << percentChange
          << " change=" << priceChange
          << endl;

  ticker.lastPrice += priceChange;
  ticker.type = TICK_TYPES[rand() % 3];
};


//////////////////////////////////////////////
class HelloServer {

 public:
  HelloServer(zmq::context_t *context) :
      context(context),
      socket(*context, ZMQ_REP) {

    socket.bind(FLAGS_endpoint.c_str());
    VLOG(1) << "Hello server bound @ " << FLAGS_endpoint << endl;
  }

  void Run() {
    LOG(INFO) << "Hello server starting." << endl;

    bool loop = true;
    while (loop) {
      zmq::message_t request;

      // Wait for next request
      socket.recv(&request);
      printf("Received request: [%s]\n", (char *)request.data());

      sleep(1);

      zmq::message_t reply(6);
      memcpy((void *) reply.data(), "world", 6);
      socket.send(reply);
    }
  }
  
 private:
  zmq::context_t *context;
  zmq::socket_t socket;
};


//////////////////////////////////////////////
class HelloClient {

 public:
  HelloClient(zmq::context_t *context, int messages) :
      context(context),
      socket(*context, ZMQ_REQ),
      messages(messages) {

    socket.connect(FLAGS_endpoint.c_str());
    VLOG(1) << "Hello client connected to " << FLAGS_endpoint << endl;
  }

  void Run() {
    for (int i = 0; i < messages; ++i) {
      zmq::message_t request(6);
      memcpy((void*) request.data(), "Hello", 6);

      LOG(INFO) << "Sending request " << i << endl;

      socket.send(request);

      // Get reply
      zmq::message_t reply;
      socket.recv(&reply);

      LOG(INFO) << "Received reply: " << i << ", data = " << (char *) reply.data() << endl;
    }
  }
  
 private:
  zmq::context_t *context;
  zmq::socket_t socket;
  int messages;
};

//////////////////////////////////////////////
class Publisher {
 public:
  Publisher(zmq::context_t *context) :
      context(context),
      socket(*context, ZMQ_PUB) {

    socket.bind(FLAGS_endpoint.c_str());
    VLOG(1) << "Publisher bound @ " << FLAGS_endpoint << endl;


    // Initialize instruments
    Instrument* amzn = new Instrument("AMZN", 180.0);
    Instrument* appl = new Instrument("AAPL", 350.0);
    Instrument* bidu = new Instrument("BIDU", 100.0);
    Instrument* goog = new Instrument("GOOG", 600.0);
    Instrument* nflx = new Instrument("NFLX", 200.0);
    Instrument* pcln = new Instrument("PCLN", 400.0);
  }
  
  void Run() {
    LOG(INFO) << "Publisher starting." << endl;

    bool loop = true;
    while (loop) {

      int pick = rand() % TICKERS.size();
      Instrument* ticker = TICKERS[pick];
      GeneratePrice(*ticker);
      PublishPrice(*ticker);
      
      if (FLAGS_sleep > 0) {
        // Sleep by X / size(tickers) so that we get roughly the
        // frequency specified in the flag for all instruments (instead of 1/N).
        sleep_micros((int)(FLAGS_sleep * 1000 / TICKERS.size()));
      }
    }
  }

 private:
  void PublishPrice(const Instrument& ticker) {
    int64_t now = now_micros();
    VLOG(4) << ticker.symbol 
	    << ' ' << now 
	    << ' ' << ticker.type
	    << ' ' << ticker.lastPrice << endl;

    if (FLAGS_dataframe) {

      // message format = binary data frames
      frame(socket, ticker.symbol);
      frame(socket, now);
      frame(socket, ticker.type);
      last(socket, ticker.lastPrice);

    } else if (FLAGS_testmessage) {
      // message format = test string 'TEST'

      zmq::message_t event(6);
      memcpy((void *) event.data(), "TEST", 5);
      socket.send(event);

    } else {

      // message format = string
      ostringstream mem;
      mem << ticker.symbol
          << ' ' << now_micros()
          << ' ' << ticker.type
          << ' ' << ticker.lastPrice;
      zmq::message_t event(mem.str().length() + 1);
      memcpy((void*) event.data(), mem.str().c_str(), mem.str().length());
      socket.send(event);
    }
  }

  
 private:
  zmq::context_t *context;
  zmq::socket_t socket;
};


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
    int messages = 0;
    while (loop) {

      messages++;

      if (FLAGS_dataframe) {
        
	// message format = binary frames
	uint64_t ts = 0;
	string symbol;
	string type;
	double price;

        bool more = false;
        assert(receive(socket, &symbol));
        assert(receive(socket, ts));
	assert(receive(socket, &type));
	assert(!receive(socket, price));

        uint64_t receiveTs = now_micros();
        uint64_t latencyMicros = receiveTs - ts;
        
	LOG(INFO) << messages << ' ' 
		  << symbol << ' '
		  << ts << ' '
		  << type << ' ' 
		  << price
                  << " (" << latencyMicros << ")"
		  << endl;

      } else if (FLAGS_dumpmessage) {

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
      } else if (FLAGS_testmessage) {

        // message format = constant 'TEST'

        zmq::message_t event;
        socket.recv(&event);
        
        LOG(INFO) << messages << ' ' << (char *) event.data() << endl;
      } else {

        // message format = string

	zmq::message_t event;
	socket.recv(&event);

        string symbol;
	string type;
        uint64_t ts;
        double price;

        VLOG(5) << "Received: " << event.data() << endl;
        istringstream iss(static_cast<char*>(event.data()));
        iss >> symbol >> ts >> type >> price;

        uint64_t receiveTs = now_micros();
        uint64_t latencyMicros = receiveTs - ts;

        LOG(INFO) << messages 
		  << ' ' << ts 
		  << ' ' << symbol 
		  << ' ' << type
		  << ' ' << price
                  << " (" << latencyMicros << ")"
                  << endl;
      }
    }
  }

 private:
  zmq::context_t *context;
  zmq::socket_t socket;
};






////////////////////////////////////////////////////////////////////////////
// Logging command-line flags: --logtostderr --v=1
int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  init_random();

  zmq::context_t context(1);

  switch (FLAGS_test) {

    /////////////////////////////////////
    // Hello World!  
    case 0 :
      if (FLAGS_server) {
        HelloServer server(&context);
        server.Run();
      } else {
        HelloClient client(&context, FLAGS_messages);
        client.Run();
      }
      break;

    /////////////////////////////////////
    // PubSub
    case 1 :
      if (FLAGS_server) {
        Publisher publisher(&context);
        publisher.Run();
      } else {
        Subscriber subscriber(&context);
        subscriber.Run();
      }
      break;

    default:
      LOG(ERROR) << "Unknown test case: " << FLAGS_test << endl;
      return -1;
  }
  return 0;
}
