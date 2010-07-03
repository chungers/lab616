#include <string.h>
#include <iostream>
#include <vector>
#include <boost/algorithm/string.hpp>
#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include <ib/helpers.hpp>
#include <ib/ticker_id.hpp>


using namespace std;

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

string symbols =
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


} // namespace
