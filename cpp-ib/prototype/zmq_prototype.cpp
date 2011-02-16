#include "common.hpp">
#include "utils.hpp"

#include <iostream>
#include <map>
#include <stdio.h>
#include <string.h>
#include <sys/time.h>
#include <unistd.h>
#include <time.h>
#include <vector>

#include <boost/algorithm/string.hpp>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/thread.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <zmq.hpp>


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

inline uint64_t now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<uint64_t>(tv.tv_sec) * 1000000 + tv.tv_usec;
}

struct Instrument;
static map<int, Instrument*> TICKERS;
static string TICK_TYPES[] = { "BID", "ASK", "LAST" };

struct Instrument {

  Instrument(const string& s, double lastp) : symbol(s), lastPrice(lastp) {
    TICKERS[TICKERS.size()] = this;
  }
  string symbol;
  double lastPrice;
  string type;
};

inline void GeneratePrice(Instrument* ticker) {
  int sign = ((rand() % 10 - 4) > 0) ? +1 : -1;  // 60% in favor of +
  double percentChange = (rand() % 21) / 100.f; // 0 - 0.2% change
  double priceChange = sign * percentChange / 100.f * ticker->lastPrice;
  VLOG(6) << sign
          << " * %=" << percentChange
          << " change=" << priceChange
          << endl;

  ticker->lastPrice += priceChange;
  ticker->type = TICK_TYPES[rand() % 3];
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
      GeneratePrice(ticker);
      PublishPrice(ticker);
      
      if (FLAGS_sleep > 0) {
        // Sleep by X / size(tickers) so that we get roughly the
        // frequency specified in the flag for all instruments (instead of 1/N).
        sleep_micros((int)(FLAGS_sleep * 1000 / TICKERS.size()));
      }
    }
  }

 private:
  void PublishPrice(Instrument* ticker) {
    int64_t now = now_micros();
    VLOG(4) << now 
	    << ' ' << ticker->symbol 
	    << ' ' << ticker->type
	    << ' ' << ticker->lastPrice << endl;

    if (FLAGS_dataframe) {

      // message format = binary data frames
      
    } else if (FLAGS_testmessage) {
      // message format = test string 'TEST'

      zmq::message_t event(6);
      memcpy((void *) event.data(), "TEST", 5);

      VLOG(4) << now << ", Pushing event " << (char*)event.data() << endl;
      socket.send(event);
    } else {

      // message format = string
      
      ostringstream mem;
      mem << ticker->symbol
          << ' ' << now_micros()
          << ' ' << ticker->type
          << ' ' << ticker->lastPrice;
      zmq::message_t event(mem.str().length() + 1);
      memcpy((void*) event.data(), mem.str().c_str(), mem.str().length());

      VLOG(4) << now << ", Pushing event " << (char*)event.data() << endl;
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

    socket.setsockopt(ZMQ_SUBSCRIBE, FLAGS_symbol.c_str(), FLAGS_symbol.length());
  }
  
  void Run() {
    LOG(INFO) << "Subscriber listening." << endl;

    bool loop = true;
    int messages = 0;
    while (loop) {

      zmq::message_t event;
      socket.recv(&event);

      if (FLAGS_dataframe) {
        // message format = binary frames
        
      } else if (FLAGS_testmessage) {
        // message format = constant 'TEST'
        zmq::message_t reply;
        socket.recv(&reply);
        
        LOG(INFO) << "Received reply: "<< ", data = " << (char *) reply.data() << endl;
      } else {
        // message format = string
        string symbol;
	string type;
        uint64_t ts;
        double price;

        VLOG(2) << "Received: " << event.data() << endl;
        istringstream iss(static_cast<char*>(event.data()));
        iss >> symbol >> ts >> type >> price;

        LOG(INFO) << messages 
		  << ' ' << ts 
		  << ' ' << symbol 
		  << ' ' << type
		  << ' ' << price << endl;
        messages++;
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
