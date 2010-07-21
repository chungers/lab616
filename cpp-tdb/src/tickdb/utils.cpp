#include <sstream>
#include <boost/algorithm/string.hpp>
#include <boost/date_time.hpp>
#include <boost/date_time/local_time_adjustor.hpp>
#include <boost/format.hpp>


#include <tickdb/utils.hpp>

using namespace std;

namespace tickdb {
namespace internal {

// http://stackoverflow.com/questions/1466756/c-equivalent-of-java-bytebuffer
template <typename T>
std::stringstream& BufferPut(std::stringstream& str, const T& value)
{
  union coercion { T value; char data[ sizeof(T) ]; } c;
  c.value = value;
  str.write(c.data, sizeof(T));
  return str;
}

template <typename T>
std::stringstream& BufferGet(std::stringstream& str, T& value)
{
  union coercion { T value; char data[ sizeof(T) ]; } c;
  c.value = value;
  str.read(c.data, sizeof(T));
  value = c.value;
  return str;
}

static const boost::posix_time::ptime EPOCH(boost::gregorian::date(1970, 1, 1));

// For now, mostly for printing debug information.
boost::posix_time::ptime GetPtime(const tickdb::record::Key& key)
{
  return EPOCH +
      boost::posix_time::milliseconds(key.get_timestamp() / 1000) +
      boost::posix_time::microseconds(key.get_timestamp() % 1000 );
}

} // namespace internal
} // namespace tickdb

namespace tickdb {
namespace record {

using namespace tickdb::internal;

Key::Key(const char* buff, size_t size)
{
  std::stringstream bytes(std::string(buff, size));
  BufferGet(bytes, id_);
  BufferGet(bytes, ts_);
}

std::ostream& operator<<(std::ostream& out, const Key& k)
{
  out << "[" << k.id_ << ":" << tickdb::internal::GetPtime(k) << "]";
  return out;
}

Key::TimestampUtc Key::UtcTimestamp() const
{
  return static_cast<TimestampUtc>(GetPtime(*this));
}

void Key::ToString(string* s) const
{
  using namespace tickdb::internal;
  std::stringstream buff(std::stringstream::out);
  BufferPut(buff, id_);
  BufferPut(buff, ts_);
  s->assign(buff.str());
}
} // namespace record
} // namespace tickdb
