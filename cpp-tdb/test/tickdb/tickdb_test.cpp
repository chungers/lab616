#include <iostream>
#include <sstream>
#include <string>
#include <sys/stat.h>
#include <boost/bind.hpp>
#include <boost/format.hpp>
#include <boost/function.hpp>
#include <boost/filesystem/operations.hpp>

#include <glog/logging.h>
#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include <kccompare.h>
#include <kctreedb.h>

#include "tickdb/tickdb.hpp"

using namespace std;
using namespace tickdb;

namespace {

namespace tf = tickdb::file;

//typedef ColumnMarshaller<1, tickdb::file::Payload> PayloadMarshaller;


TEST(TickDbTest, TestMarshaller)
{
  tf::Payload payload;

  string buffer;

  ColumnMarshaller<1, tf::Payload> m;

  EXPECT_TRUE(m.SerializeToColumnBuffer(payload, &buffer));

}

} // namespace
