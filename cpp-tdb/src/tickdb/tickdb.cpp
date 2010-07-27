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

#include "tickdb/tickdb_format.pb.h"
#include "tickdb/utils.hpp"
#include "tickdb/tickdb.hpp"

using namespace std;
using namespace kyotocabinet;
using namespace tickdb::record;
using namespace tickdb::file;

namespace tickdb {
namespace internal {

class TickDbImpl : public tickdb::TickDbInterface
{

 public:
  TickDbImpl() {}
  ~TickDbImpl() {}


  virtual bool Open()
  {
    return true;
  }

  virtual bool Close()
  {
    return true;
  }

  template <typename T>
  bool Insert(Timestamp ts, const T& column)
  {
    string str;
    bool serialized = column.SerializeToString(&str);

    bool written = true;
    return serialized && written;
  }
};

} // namespace internal
} // namespace tickdb
