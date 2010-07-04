#include <string.h>
#include <iostream>
#include <vector>
#include <boost/algorithm/string.hpp>
#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include <ib/helpers.hpp>
#include <ib/ticker_id.hpp>


using namespace std;
using namespace ib::internal;

namespace {

TEST(HelpersTest, TestStringToUpper)
{
  EXPECT_EQ("AAPL", to_upper("aapl"));
  EXPECT_EQ("AAPL", to_upper("Aapl"));
  EXPECT_EQ("AAPL", to_upper("AaPl"));
  EXPECT_EQ("AAPL", to_upper("aapL"));
  EXPECT_EQ("AAPL", to_upper("AAPL"));
  EXPECT_EQ("XYZ", to_upper("XYZ"));
  EXPECT_EQ("XYZ", to_upper("xyz"));
}

TEST(HelpersTest, TestStringToLower)
{
  EXPECT_EQ("xyz", to_lower("xyz"));
  EXPECT_EQ("xyz", to_lower("XYZ"));
}

static string symbols =
    "FAS,FAZ,SRS,GS,AAPL,GOOG,URE,RIMM,TBT,BAC,DXD,\
DXO,FSLR,FXP,LDK,QID,QLD,REW,SDS,SKF,BK,JPM,\
MS,SMN,SSO,TYH,TYP,UYM,XLE,XLV,AYI,AMZN,DDM,\
C,COF,AXP,RTH";

int test_encode(const string& symbol)
{
  int code = SymbolToTickerId(symbol);
  string from_code = SymbolFromTickerId(code);
  EXPECT_EQ(to_upper(symbol), from_code);
  return code;
}


int test_encode_option(const string& symbol, bool callOption,
                       double strike)
{
  int code = SymbolToTickerId(symbol, callOption, strike);
  string from_code = SymbolFromTickerId(code);
  EXPECT_EQ(to_upper(symbol), from_code);
  return code;
}

TEST(HelpersTest, TestEncodingAndDecodingTickerId)
{
  vector<string> list;
  vector<string>::iterator itr;

  boost::split(list, symbols, boost::is_any_of(","));

  for (itr = list.begin(); itr != list.end(); itr++) {
    string s = *itr;
    EXPECT_EQ(test_encode(s), test_encode(to_lower(s)));
  }
}

TEST(HelpersTest, TestEncodingOptions)
{
  vector<string> list;
  vector<string>::iterator itr;
  double strike(10.0);

  boost::split(list, symbols, boost::is_any_of(","));
  for (itr = list.begin(); itr != list.end(); itr++) {
    string s = *itr;
    int a = test_encode_option(s, true, strike);
    int b = test_encode_option(to_lower(s), true, strike);
    EXPECT_EQ(a, b);
  }

  strike = 100;
  int i = 0;
  for (itr = list.begin(); itr != list.end(); itr++, i++) {
    string s = *itr;
    strike += 20;
    int a = SymbolToTickerId(s, true, strike);
    EncodedOption eo = EncodedOptionFromTickerId(a);
    EXPECT_EQ(s, eo.symbol);
    EXPECT_EQ(true, eo.call_option);
    EXPECT_EQ(strike, eo.strike);
  }
}

} // namespace
