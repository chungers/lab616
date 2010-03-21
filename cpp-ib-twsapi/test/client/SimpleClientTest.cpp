
#include <client/SimpleClient.h>

#include <gmock/gmock.h>
#include <gtest/gtest.h>
namespace {

TEST(SimpleTest1, HelloWorld) {
  EXPECT_EQ(2, 1+1);
}

} // namespace
