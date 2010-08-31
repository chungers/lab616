
#include <map>
#include <iostream>
#include <sys/time.h>
#include <vector>

#include <boost/algorithm/string.hpp>
#include <boost/thread.hpp>

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <sigc++/sigc++.h>

#include "ib/backplane.hpp"

using namespace std;
using namespace ib::events;
using namespace boost;

namespace {

typedef uint64_t int64;
inline int64 now_micros()
{
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<int64>(tv.tv_sec) * 1000000 + tv.tv_usec;
}


class BidAskReceiver : public ib::Receiver<BidAsk>
{
 public:
  BidAskReceiver() : invoked(false) {}
  bool invoked;

  virtual void operator()(const BidAsk& bid_ask)
  {
    invoked = true;
  }
};

class ConnectReceiver : public ib::Receiver<Connect>
{
 public:
  ConnectReceiver() : invoked(false) {}
  bool invoked;

  virtual void operator()(const Connect& connect)
  {
    invoked = true;
  }
};


TEST(BackPlaneTest, TestSelectionById)
{
  Connect connect;
  connect.set_time_stamp(now_micros());
  connect.set_id(10);

  Disconnect disconnect;
  disconnect.set_time_stamp(now_micros());
  disconnect.set_id(100);

  BidAsk bid_ask;
  bid_ask.set_time_stamp(now_micros());
  bid_ask.set_id(100);
  bid_ask.mutable_bid()->set_price(100.);

  ib::signal::Selection select;

  select << 10;

  EXPECT_TRUE(select(connect));
  EXPECT_FALSE(select(disconnect));
  EXPECT_FALSE(select(bid_ask));

  select << 100 << 40;

  EXPECT_TRUE(select(connect));
  EXPECT_TRUE(select(disconnect));
  EXPECT_TRUE(select(bid_ask));

  ib::signal::Selection select2 = select;
  EXPECT_TRUE(select2(connect));
  EXPECT_TRUE(select2(disconnect));
  EXPECT_TRUE(select2(bid_ask));

  connect.set_id(30);
  disconnect.set_id(30);
  bid_ask.set_id(30);
  EXPECT_FALSE(select2(connect));
  EXPECT_FALSE(select2(disconnect));
  EXPECT_FALSE(select2(bid_ask));
}


TEST(BackPlaneTest, TestSelectionBySymbol)
{
  int aapl = ib::signal::GetTickerId("AAPL");
  int goog = ib::signal::GetTickerId("GOOG");
  int intc = ib::signal::GetTickerId("INTC");

  Connect connect;
  connect.set_time_stamp(now_micros());
  connect.set_id(aapl);

  Disconnect disconnect;
  disconnect.set_time_stamp(now_micros());
  disconnect.set_id(goog);

  BidAsk bid_ask;
  bid_ask.set_time_stamp(now_micros());
  bid_ask.set_id(intc);
  bid_ask.mutable_bid()->set_price(100.);

  ib::signal::Selection select;

  select << 10;

  EXPECT_FALSE(select(connect));
  EXPECT_FALSE(select(disconnect));
  EXPECT_FALSE(select(bid_ask));

  select << "AAPL";

  EXPECT_TRUE(select(connect));
  EXPECT_FALSE(select(disconnect));
  EXPECT_FALSE(select(bid_ask));

  select << "GOOG" << "INTC";

  EXPECT_TRUE(select(connect));
  EXPECT_TRUE(select(disconnect));
  EXPECT_TRUE(select(bid_ask));
}

TEST(BackPlaneTest, TestExclusionBySymbol)
{
  int aapl = ib::signal::GetTickerId("AAPL");
  int goog = ib::signal::GetTickerId("GOOG");
  int intc = ib::signal::GetTickerId("INTC");

  Connect connect;
  connect.set_time_stamp(now_micros());
  connect.set_id(aapl);

  Disconnect disconnect;
  disconnect.set_time_stamp(now_micros());
  disconnect.set_id(goog);

  BidAsk bid_ask;
  bid_ask.set_time_stamp(now_micros());
  bid_ask.set_id(intc);
  bid_ask.mutable_bid()->set_price(100.);

  ib::signal::Exclusion exclude;

  exclude << 10;

  EXPECT_TRUE(exclude(connect));
  EXPECT_TRUE(exclude(disconnect));
  EXPECT_TRUE(exclude(bid_ask));

  exclude << "AAPL";

  EXPECT_FALSE(exclude(connect));
  EXPECT_TRUE(exclude(disconnect));
  EXPECT_TRUE(exclude(bid_ask));

  exclude << "GOOG" << "INTC";

  EXPECT_FALSE(exclude(connect));
  EXPECT_FALSE(exclude(disconnect));
  EXPECT_FALSE(exclude(bid_ask));
}

TEST(BackPlaneTest, Test1)
{
  BidAsk bid_ask;
  bid_ask.set_time_stamp(now_micros());
  bid_ask.mutable_bid()->set_price(100.);

  boost::scoped_ptr<ib::BackPlane> backplane(ib::BackPlane::Create());

  ConnectReceiver conn_receiver1;
  ConnectReceiver conn_receiver2;
  backplane->Register(&conn_receiver1);
  backplane->Register(&conn_receiver2);

  BidAskReceiver bid_ask_receiver;
  backplane->Register(&bid_ask_receiver);

  for (int i = 0 ; i < 100 ; ++i) {
    conn_receiver1.invoked = false; // reset
    conn_receiver2.invoked = false; // reset

    backplane->OnConnect(now_micros(), 1);

    EXPECT_TRUE(conn_receiver1.invoked);
    EXPECT_TRUE(conn_receiver2.invoked);
  }
}

} // Namespace
