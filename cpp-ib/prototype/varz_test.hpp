#ifndef VARZ_TEST_H_
#define VARZ_TEST_H_

namespace prototype {

// Test class
class VarzClient {

 public:
  VarzClient();
  void receiveMessage(const char* message);

 private:
  uint64_t ts;
};

}
#endif
