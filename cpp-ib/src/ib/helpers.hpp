#ifndef IB_HELPERS_H_
#define IB_HELPERS_H_

#include <algorithm>
#include <cctype>
#include <string>
#include <sstream>

using namespace std;

// Max code value representing ZZZZ << 11.
const int MAX_CODE_VALUE = 1088389120 + (1 << 12 - 1);
const int MAX_CHARS = 4;
const int SCALE[] = {
  1,            // 27 ^ 0
  27,           // 27 ^ 1
  27 * 27,      // 27 ^ 2
  27 * 27 * 27  // 27 ^ 3
};

// Compute the ticker id based on the symbol.
// Key points:
// 1. The string length is at most 4 characters (e.g. 'AAPL')
// 2. The character set (0, A..Z) is basically base 27.
// 3. First compute the value based on the character value of
//    the string.  For example, 'AAPL' is computed as
//    'A' * 27^3 + 'A' * 27^2 + 'P' * 27^1 + 'L' * 27^0, where
//    each character takes on the numeric value as the offset
//    from 'A'+1, e.g. 'A' = 1, 'B' = 2, .... 'Z' = 26.
// 4. The computed sum is then left shifted 11 places.  This
//    pushes the sum to the 11 most significant bits and leaves
//    the lower 11 bits for storing other information, such
//    as encoding option values.
int to_ticker_id(const string& s)
{
  int v = 0;
  int len = s.length();
  for (int i = 0; i < len; i++) {
    v += (toupper(s.at(i)) - 'A' + 1) * SCALE[len-(i+1)];
  }
  return v << 11;
}

// Computes the symbol from the input code value.  See above
// for description of the encoding scheme.  Basically this does
// in reverse what the encoding step does .
string from_ticker_id(int code)
{
  ostringstream sbuff;
  int m = code >> 11;
  for (int i = MAX_CHARS - 1; i >= 0; i--) {
    int c = m / SCALE[i];
    m %= SCALE[i];
    if (c > 0) sbuff << (char)(c + 'A' - 1);
  }
  return sbuff.str();
}

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
