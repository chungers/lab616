#include "utils.hpp"
#include "varz.hpp"
#include "varz_test.hpp"

#include <iostream>

using namespace std;
using namespace prototype;

DEFINE_VARZ_int32(messages, -10, "Number of messages.");
DECLARE_VARZ_int64(micros);
DECLARE_VARZ_double(avg);
DECLARE_VARZ_string(client);

int  main(int argc,char** argv)
{
  lab616::utils::init_random();

  cerr << "Starting. messages = " <<  VARZ_messages << endl;

  VARZ_messages = 0;
  cerr << "messages = " <<  VARZ_messages << endl;

  VarzClient vc;

  cerr << "Constructed client of name " << VARZ_client << endl;

  int iterations = 10000;
  for (int i = 0; i < iterations; ++i) {
    vc.receiveMessage("test");
  }

  cerr << "Final messages = " << VARZ_messages << endl;
  cerr << "Elaspsed " << VARZ_micros << endl;
  cerr << "Average " << VARZ_avg << endl;

  // Get all the varzs:
  using namespace lab616;
  vector<VarzInfo> varzs;
  GetAllVarzs(&varzs);

  for (vector<VarzInfo>::iterator allVarzs = varzs.begin();
       allVarzs != varzs.end();
       ++allVarzs) {
    VarzInfo info = *allVarzs;
    cerr << "Name = " << info.name << ", "
         << "Type = " << info.type << ", "
         << "Desc = " << info.description << ", "
         << "From = " << info.filename << ", "
         << "Now  = " << info.current_value << ", "
         << "Init = " << info.initial_value
         << endl;

  }

  return 0;
}
