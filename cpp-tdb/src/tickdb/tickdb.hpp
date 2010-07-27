#ifndef TICKDB_H_
#define TICKDB_H_

#include <iostream>
#include <sstream>
#include <string>
#include <sys/stat.h>
#include <boost/bind.hpp>
#include <boost/format.hpp>
#include <boost/function.hpp>
#include <boost/filesystem/operations.hpp>

#include <glog/logging.h>

namespace tickdb {

typedef uint64_t Timestamp;

class TickDbInterface
{
 public:

  virtual ~TickDbInterface() {}

  virtual bool Open() = 0;
  virtual bool Close() = 0;

  template <typename T> bool Insert(Timestamp ts, const T& column);
};

} // namespace tickdb

#endif // TICKDB_H_
