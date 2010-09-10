#include <cctype>
#include <cstdio>
#include <cstdlib>
#include <cstring>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include <tbb/tbb_allocator.h>
#include <tbb/task_scheduler_init.h>
#include <tbb/tick_count.h>
#include <tbb/pipeline.h>

static const int NThread = 4;

int main(int argc, char** argv) {
  ::testing::InitGoogleMock(&argc, argv);

  tbb::task_scheduler_init init( NThread );
  return RUN_ALL_TESTS();
}
