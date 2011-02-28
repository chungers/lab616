
#include <iostream>
#include <glog/logging.h>

#include "utils.hpp"
#include "varz.hpp"
#include "varz_test.hpp"

DEFINE_VARZ_int64(micros, lab616::utils::now_micros(), "Time in micros.");
DEFINE_VARZ_double(avg, 0., "The average.");
DEFINE_VARZ_string(client, "TestClient", "The name of the client.");

DECLARE_VARZ_int32(messages);

using namespace std;

namespace prototype {


VarzClient::VarzClient() {
  cerr << "Instantiated VarzClient." << endl;
  ts = lab616::utils::now_micros();
  VARZ_client = "test client";
}

void VarzClient::receiveMessage(const char* message) {
  VARZ_messages++;
  // sleep for some time
  lab616::utils::sleep_micros(rand() % 100);
  uint64_t now = lab616::utils::now_micros();
  VARZ_micros = now - ts;
  VARZ_avg = ((double) VARZ_micros) / ((double) VARZ_messages);
  ts = now;

  VLOG(20) << "Received " << message
           << ", micros = " << VARZ_micros
           << ", total = " << VARZ_messages
           << endl;
}

}
