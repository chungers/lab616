#include <iostream>

#include <tbb/tbb_allocator.h>
#include <tbb/task_scheduler_init.h>
#include <tbb/tick_count.h>
#include <tbb/pipeline.h>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

static int NThread = tbb::task_scheduler_init::automatic;

using namespace std;

int main(int argc, char** argv) {
  ::testing::InitGoogleMock(&argc, argv);

  // -1 ==> Automatic thread count.
  cout << "Starting up scheduler with " << NThread << " threads." << endl;

  // Manually set to 10 threads
  tbb::task_scheduler_init init(10);

  return RUN_ALL_TESTS();
}
