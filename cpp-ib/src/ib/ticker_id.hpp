#ifndef IB_TICKER_ID_H_
#define IB_TICKER_ID_H_

#include <algorithm>
#include <iostream>
#include <cctype>
#include <string>
#include <sstream>

using namespace std;

namespace ib {
namespace internal {

static const int MAX_CHARS = 4;
static const int SCALE[] = {
  1,            // 27 ^ 0
  27,           // 27 ^ 1
  27 * 27,      // 27 ^ 2
  27 * 27 * 27  // 27 ^ 3
};

static const int OFFSET = 11;
static const int MAX_OPTION_PART = 1 << (OFFSET + 1) - 1;
static const int MID = 1 << (OFFSET - 1);
// Max code value representing ZZZZ << 11.
static const int MAX_CODE_VALUE = 1088389120 + MAX_OPTION_PART;


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
int SymbolToTickerId(const string& s)
{
  int v = 0;
  int len = s.length();
  for (int i = 0; i < len; i++) {
    v += (toupper(s.at(i)) - 'A' + 1) * SCALE[len-(i+1)];
  }
  return v << OFFSET;
}

// For options.  This computes a ticker id based on the
// original ticker id (shifted 11 bits) plus some encoded
// value based on the strike.  Note that this id is unique
// relative to the ticker id for that given strike price.  It
// does not encode information about the expiration since
// there aren't enough bits for that.
int SymbolToTickerId(const string& s, bool isCallOption, const double strike)
{
  int tick_id = SymbolToTickerId(s);
  return tick_id + MID + ((isCallOption) ? +1 : -1) * strike;
}

// Computes the symbol from the input code value.  See above
// for description of the encoding scheme.  Basically this does
// in reverse what the encoding step does .
string SymbolFromTickerId(int code)
{
  ostringstream sbuff;
  int m = code >> OFFSET;
  for (int i = MAX_CHARS - 1; i >= 0; i--) {
    int c = m / SCALE[i];
    m %= SCALE[i];
    if (c > 0) sbuff << (char)(c + 'A' - 1);
  }
  return sbuff.str();
}

struct EncodedOption {
 public:
  EncodedOption(const string& sym,
                const bool call,
                const double strk)
      : symbol(sym), call_option(call), strike(strk) {}
  const string symbol;
  const bool call_option;
  const double strike;

  // For printing to output stream.
  friend ostream& operator<<(ostream& os, const EncodedOption& opt)
  {
    os << "Option(" << opt.symbol << "," << ((opt.call_option) ? "CALL" : "PUT")
       << "," << opt.strike << ")";
    return os;
  }
};

EncodedOption EncodedOptionFromTickerId(int code)
{
  int option_mask = (code >> OFFSET) << OFFSET;
  string sym = SymbolFromTickerId(option_mask);
  int opt = code - ((code >> OFFSET) << OFFSET) - MID;
  bool call = opt > 0;
  double strike = (call) ? (double) opt : (double) (-1 * opt);
  return EncodedOption(sym, call, strike);
}

} // namespace internal
} // namespace ib
#endif // IB_TICKER_ID_H_
