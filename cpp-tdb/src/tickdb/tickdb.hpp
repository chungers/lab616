#ifndef TICKDB_H_
#define TICKDB_H_

#include <string>
#include <boost/function.hpp>
#include "tickdb/tickdb_format.pb.h"

namespace tickdb {

typedef uint64_t Timestamp;

/**
 * Protobuffer message marshaller.
 * C is a type code, and T is the proto message type.
 */
template <int C, typename T>
class ColumnMarshaller
{
 public:
  ColumnMarshaller() {}
  ~ColumnMarshaller() {}

  /** @return The type code. */
  int TypeCode() { return C; }

  bool SerializeToColumnBuffer(const T& input, std::string* output)
  {
    tickdb::file::Row_Column col;
    std::string buff;
    bool ok = input.SerializeToString(&buff);
    if (!ok) return false;
    col.set_type(C);
    col.set_value(buff);
    return col.SerializeToString(output);
  }

  bool ParseFromColumnBuffer(const std::string& input, T* output)
  {
    tickdb::file::Row_Column col;
    bool ok = col.ParseFromString(input);
    if (!ok) return false;
    if (col.type() != C) return false; // Wrong type.
    return output.ParseFromString(col.value());
  }
};


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
