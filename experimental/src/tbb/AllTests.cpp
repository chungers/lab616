#include <iostream>

#include <tbb/tbb_allocator.h>
#include <tbb/task_scheduler_init.h>
#include <tbb/tick_count.h>
#include <tbb/pipeline.h>

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <gmock/gmock.h>
#include <gtest/gtest.h>

static int NThread = tbb::task_scheduler_init::automatic;

using namespace std;

DEFINE_int32(threads, 20, "Number of the threads for TBB.");

int main(int argc, char** argv) {
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);
  ::testing::InitGoogleMock(&argc, argv);

  // -1 ==> Automatic thread count.
  cout << "Starting up scheduler with " << NThread << " threads." << endl;

  // Manually set to 10 threads
  tbb::task_scheduler_init init(FLAGS_threads);

  return RUN_ALL_TESTS();
}
