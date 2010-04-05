
#include "mocks/MockEWrapper.h"
#include "mocks/MockEClient.h"

#include <gmock/gmock.h>
#include <gtest/gtest.h>


namespace {

TEST(SimpleTest2, HelloWorld2) {
  EXPECT_EQ(2, 1+1);
}

} // namespace

