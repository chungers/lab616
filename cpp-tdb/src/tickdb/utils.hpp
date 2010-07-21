#ifndef TICKDB_UTILS_H_
#define TICKDB_UTILS_H_

#include <string>
#include <iostream>
#include <boost/date_time.hpp>

namespace tickdb {
namespace record {

class Key {

 public:

  typedef uint32_t Id;
  typedef uint64_t Timestamp;
  typedef boost::posix_time::ptime TimestampUtc;

  Key(const char* b, size_t s);
  Key(Id id, Timestamp ts) : id_(id), ts_(ts) {}
  ~Key() {}

  Id get_id() const { return id_; }
  Timestamp get_timestamp() const { return ts_; }

  bool Same(const Key& other) const
  { return id_ == other.id_; }

  TimestampUtc UtcTimestamp() const;

  Key& operator=(const Key& other)
  { id_ = other.id_; ts_ = other.ts_; return *this; }

  bool operator==(const Key& other) const
  { return id_ == other.id_ && ts_ == other.ts_; }

  bool operator!=(const Key& other) const
  { return !(*this == other); }

  bool operator>(const Key& other) const
  { return ts_ > other.ts_; }

  bool operator>=(const Key& other) const
  { return ts_ >= other.ts_; }

  bool operator<(const Key& other) const
  { return ts_ < other.ts_; }

  bool operator<=(const Key& other) const
  { return ts_ <= other.ts_; }

  friend std::ostream& operator<<(std::ostream& out, const Key& k);

  void ToString(std::string* s) const;

 private:
  Id id_;
  Timestamp ts_;
};



}  // namespace record
}; // namespace tickdb

#endif // TICKDB_UTILS_H_
