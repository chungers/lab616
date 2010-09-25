#include <iostream>

#include <tbb/tbb_allocator.h>
#include <tbb/task_scheduler_init.h>
#include <tbb/tick_count.h>
#include <tbb/pipeline.h>

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "tbb_config.hpp"

static int NThread = tbb::task_scheduler_init::automatic;

using namespace std;

DEFINE_int32(threads, 20, "Number of threads");
DEFINE_int32(ticks, 20, "Number of ticks to generate.");
DEFINE_int32(tokens, 20, "Number of tokens in flight.");
DEFINE_int32(sleep, 0, "Number of seconds for strategy to sleep.");
DEFINE_bool(verbose, false, "Verbose.");
DEFINE_bool(tbb_alloc, false, "Use tbb allocator.");

static const TbbPrototype::Config* __config__;

namespace TbbPrototype {

// Implementation where configs come from flags.
Config::Config() :
      threads(FLAGS_threads),
      ticks(FLAGS_ticks),
      tokens(FLAGS_tokens),
      sleep(FLAGS_sleep),
      verbose(FLAGS_verbose),
      tbb_alloc(FLAGS_tbb_alloc)
{
}

const Config* GetConfig()
{
  return __config__;
}

};



int main(int argc, char** argv) {
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);
  ::testing::InitGoogleMock(&argc, argv);

  // -1 ==> Automatic thread count.
  cout << "Starting up scheduler with " << NThread << " threads." << endl;

  TbbPrototype::Config configFromFlags;
  __config__ = &configFromFlags;

  tbb::task_scheduler_init init(TbbPrototype::GetConfig()->threads);

  return RUN_ALL_TESTS();
}
