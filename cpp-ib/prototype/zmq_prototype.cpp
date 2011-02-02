
#include "common.hpp"

#include <map>
#include <unistd.h>
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



using namespace std;
using namespace boost;

DEFINE_bool(server, false, "Start server.");
DEFINE_string(endpoint, "tcp://*:5555", "End point string.");

// Logging command-line flags: --logtostderr --v=1  
int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);
  
  zmq::context_t context(1);

  if (FLAGS_server) {

    VLOG(1) << "Server mode." << endl;
    
    zmq::socket_t socket(context, ZMQ_REP);
    socket.bind(FLAGS_endpoint.c_str());

    LOG(INFO) << "Starting server on " << FLAGS_endpoint << endl;

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
  } else {

    VLOG(1) << "Client mode." << endl;

    zmq::socket_t socket(context, ZMQ_REQ);

    LOG(INFO) << "Connecting to server at " << FLAGS_endpoint << endl;

    socket.connect(FLAGS_endpoint.c_str());

    for (int i = 0; i < 10; ++i) {
      zmq::message_t request(6);
      memcpy((void*) request.data(), "Hello", 6);

      LOG(INFO) << "Sending request " << i << endl;

      socket.send(request);

      // Get reply
      zmq::message_t reply;
      socket.recv(&reply);

      LOG(INFO) << "Received reply: " << i << ", data = " << (char *) reply.data() << endl;
  }
  return 0;
}


}
