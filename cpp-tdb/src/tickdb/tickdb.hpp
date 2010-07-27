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
#include "tickdb/tickdb_format.pb.h"

namespace tickdb {

typedef uint64_t Timestamp;

/**
 * Protobuffer message marshaller.
 * C is a type code, and T is the proto message type.
 */
template <int C, typename T>
class Marshaller
{
 public:
  ~Marshaller() {}

  /** @return The type code. */
  int TypeCode() { return C; }

  /**
   * @param input The proto message
   * @param output The output mutable string.
   * @returns True if success.
   */
  bool SerializeToString(const T& input, std::string* output)
  {
    return input.SerializeToString(output);
  }

  /**
   * @param input The const string buffer.
   * @param output The mutable message to merge results to.
   * @returns True if success.
   */
  bool ParseFromString(const std::string& input, T* output)
  {
    return output.ParseFromString(input);
  }
};

typedef Marshaller<0, tickdb::file::Payload> PayloadMarshaller;


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
