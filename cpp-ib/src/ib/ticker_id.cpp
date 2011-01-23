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

static const int BASE = 27;
static const int MAX_CHARS = 4;
static const int SCALE[] = {
  1,                  // BASE ^ 0
  BASE,               // BASE ^ 1
  BASE * BASE,        // BASE ^ 2
  BASE * BASE * BASE  // BASE ^ 3
};

static const int OFFSET = 11;
static const int MAX_OPTION_PART = 1 << (OFFSET + 1) - 1;
static const int MID = 1 << (OFFSET - 1);
// Max code value representing ZZZZ << 11.
// Max option value on either side (call or put) is 1023.
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
void SymbolFromTickerId(int code, string* output)
{
  ostringstream sbuff;
  int m = code >> OFFSET;
  for (int i = MAX_CHARS - 1; i >= 0; i--) {
    int c = m / SCALE[i];
    m %= SCALE[i];
    if (c > 0) sbuff << (char)(c + 'A' - 1);
  }
  output->assign(sbuff.str());
}

// Encoded option.  Note that this is not a complete
// specification of an option contract.  There is not
// enough bits to encode the expiry of the option.
struct EncodedOption {

  EncodedOption() : symbol(""), call_option(true), strike(0.0) {}

  string symbol;
  bool call_option;
  double strike;

  // For printing to output stream.
  inline friend ostream& operator<<(ostream& os, const EncodedOption& opt)
  {
    os << "Option(" << opt.symbol << "," << ((opt.call_option) ? "CALL" : "PUT")
       << "," << opt.strike << ")";
    return os;
  }

  inline EncodedOption& operator=(const EncodedOption& rhs)
  {
    if (this == &rhs) return *this;
    symbol = rhs.symbol;
    call_option = rhs.call_option;
    strike = rhs.strike;
    return *this;
  }
};

void EncodedOptionFromTickerId(int code, EncodedOption* output)
{
  int option_mask = (code >> OFFSET) << OFFSET;
  string sym;
  SymbolFromTickerId(option_mask, &sym);
  int opt = code - ((code >> OFFSET) << OFFSET) - MID;
  bool call = opt > 0;
  double strike = (call) ? (double) opt : (double) (-1 * opt);
  output->symbol = sym;
  output->call_option = call;
  output->strike = strike;
}

// Returns true if the input ticker id is encoded with contract information.
bool IsTickerIdForOption(unsigned int id)
{
  return (id % (1 << OFFSET));
}

} // namespace internal
} // namespace ib
#endif // IB_TICKER_ID_H_
