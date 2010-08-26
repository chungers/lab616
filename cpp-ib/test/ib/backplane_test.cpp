
#include "ib/backplane.hpp"

#include <map>
#include <iostream>
#include <vector>

#include <boost/algorithm/string.hpp>
#include <boost/thread.hpp>

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <sigc++/sigc++.h>


using namespace std;
using namespace ib::events;
using namespace boost;

namespace {


class EventReceiver : public ib::Receiver<BidAsk>
{
 public:
  EventReceiver() {}

  virtual void operator()(const BidAsk& bid_ask)
  {
    cout << "Got id " << bid_ask.id() << endl;
  }
};

TEST(BackPlaneTest, Test1)
{
  BidAsk bid_ask;
  bid_ask.set_time_stamp(100000);
  bid_ask.mutable_bid()->set_price(100.);

  boost::scoped_ptr<ib::BackPlane> backplane(ib::BackPlane::Create());
  EventReceiver receiver;
  backplane->Register(&receiver);


}
} // Namespace
