#ifndef IB_HELPERS_H_
#define IB_HELPERS_H_

#include <algorithm>
#include <cctype>
#include <string>
#include <sstream>

using namespace std;

// Converts the input string to all upper-case.
// Returns a transformed version without mutating input.
inline const string to_upper(const string& s)
{
  string copy(s);
  std::transform(copy.begin(), copy.end(), copy.begin(),
                 (int(*)(int)) std::toupper);
  return copy;
}

// Converts the input string to all lower-case.
// Returns a transformed version without mutating input.
inline const string to_lower(const string& s)
{
  string copy(s);
  std::transform(copy.begin(), copy.end(), copy.begin(),
                 (int(*)(int)) std::tolower);
  return copy;
}

#endif // IB_HELPERS_H_
