
#include "common.hpp"

#include <map>
#include <vector>

#include <boost/algorithm/string.hpp>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/thread.hpp>

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <sigc++/sigc++.h>

#include "ib/ib_events.pb.h"


using namespace std;
using namespace ib::events;
using namespace boost;

namespace {



// Test for thread local
boost::mutex io_mutex;
boost::thread_specific_ptr<int> ptr;

typedef boost::mutex::scoped_lock lock;
struct ThreadCounter
{
  ThreadCounter(int id) : id(id) {}
  int id;
  void operator()()
  {
    int before, after;
    if (ptr.get() == 0) ptr.reset(new int(0));
    for (int i = 0; i < 10; ++i) {
      before = (*ptr);
      after = ++(*ptr);
      EXPECT_EQ(before + 1, after);
      sleep(0.5);
      lock lock_cout(io_mutex);
      cout << id << ": " << (*ptr) << endl;
    }
  }
};

TEST(SignalPrototype, TestThreadLocal)
{
  boost::thread_group threads;
  for (int i = 0 ; i != 10 ; ++i) {
    threads.create_thread(ThreadCounter(i));
  }
  threads.join_all();
}

struct Print {
  Print(const BidAsk& b) : bid_ask(b) {}
  const BidAsk& bid_ask;

  inline friend ostream& operator<<(ostream& out, const Print& p)
  {
    const BidAsk& bid_ask = p.bid_ask;
    bool bid = bid_ask.has_bid();
    double price = bid ? bid_ask.bid().price() : bid_ask.ask().price();
    int size = bid ? bid_ask.bid().size() : bid_ask.ask().size();
    out << "BidAsk[id=" << bid_ask.id()
        << ",type=" << (bid ? "BID" : "ASK")
        << ",price=" << price
        << ",size=" << size
        << "]";
    return out;
  }
};

struct BidAskReceiver : sigc::trackable, NoCopyAndAssign {
  BidAskReceiver(int id) : id(id), received(0) {}
  int id;
  int received;
  inline void operator()(const BidAsk& bid_ask)
  {
    cout << endl << "\t\tBidAskReceiver[" << id << "] @" << ++received << ": "
         << Print(bid_ask);
   }
};

typedef map<int, sigc::connection> ConnectionMap;
typedef boost::scoped_ptr<BidAskReceiver> BidAskReceiverPtr;

TEST(SignalPrototype, PrototypeSignals)
{
  BidAsk bid_ask;
  bid_ask.set_time_stamp(100000);
  bid_ask.set_id(1);
  bid_ask.mutable_bid()->set_price(100.);

  ConnectionMap cm;
  BidAskReceiverPtr r1(new BidAskReceiver(1));
  BidAskReceiverPtr r2(new BidAskReceiver(2));

  EXPECT_EQ(0, r1->received);
  EXPECT_EQ(0, r2->received);

  // Create the signal:
  sigc::signal<void, const BidAsk&> s;

  // Connect
  cm[r1->id] = s.connect(sigc::mem_fun(r1.get(), &BidAskReceiver::operator()));
  cm[r2->id] = s.connect(sigc::mem_fun(r2.get(), &BidAskReceiver::operator()));

  EXPECT_EQ(0, r1->received);
  EXPECT_EQ(0, r2->received);

  // Send the signal
  s.emit(bid_ask);
  EXPECT_EQ(1, r1->received);
  EXPECT_EQ(1, r2->received);

  cm[r1->id].block();
  s.emit(bid_ask);
  EXPECT_EQ(1, r1->received);
  EXPECT_EQ(2, r2->received);
}

typedef sigc::signal<void, const BidAsk&> BidAskSignal;
typedef boost::scoped_ptr<BidAskSignal> BidAskSignalPtr;

// Filter that selects by id and emits on match.
struct BidAskFilter : sigc::trackable, NoCopyAndAssign {
  BidAskFilter(int id)
      : id(id), received(0), accepted(0), signal(new BidAskSignal()) {}
  int id;
  int received;
  int accepted;
  BidAskSignalPtr signal;

  inline void operator()(const BidAsk& bid_ask)
  {
    cout << endl << "\tBidAskFilter[" << id << "]("
         << ++received << "/"
         << accepted << ") received: " << Print(bid_ask)
         << " ====>";
    if (bid_ask.id() == id) {
      ++accepted;
      signal->emit(bid_ask);
    }
   }
};

typedef boost::scoped_ptr<BidAskFilter> BidAskFilterPtr;

TEST(SignalPrototype, PrototypeFiltering)
{
  BidAsk bid_ask;
  bid_ask.set_time_stamp(100000);
  bid_ask.set_id(1);
  bid_ask.mutable_bid()->set_price(100.);

  BidAskReceiverPtr r1(new BidAskReceiver(1));
  BidAskReceiverPtr r1a(new BidAskReceiver(1));
  BidAskReceiverPtr r2(new BidAskReceiver(2));

  EXPECT_EQ(0, r1->received);
  EXPECT_EQ(0, r2->received);

  // Create the signal:
  BidAskSignal signal;
  BidAskFilterPtr filt1(new BidAskFilter(1));
  BidAskFilterPtr filt2(new BidAskFilter(2));

  // Connect
  signal.connect(sigc::mem_fun(filt1.get(), &BidAskFilter::operator()));
  signal.connect(sigc::mem_fun(filt2.get(), &BidAskFilter::operator()));

  filt1->signal->connect(sigc::mem_fun(r1.get(), &BidAskReceiver::operator()));
  filt1->signal->connect(sigc::mem_fun(r1a.get(), &BidAskReceiver::operator()));
  filt2->signal->connect(sigc::mem_fun(r2.get(), &BidAskReceiver::operator()));

  EXPECT_EQ(0, r1->received);
  EXPECT_EQ(0, r1a->received);
  EXPECT_EQ(0, r2->received);

  // Send the signal
  bid_ask.set_id(1);
  cout << endl << "Sending " << Print(bid_ask);
  signal.emit(bid_ask);
  EXPECT_EQ(1, r1->received);
  EXPECT_EQ(1, r1a->received);
  EXPECT_EQ(0, r2->received);
  cout << endl;

  bid_ask.set_id(2);
  cout << endl << "Sending " << Print(bid_ask);
  signal.emit(bid_ask);
  cout << endl << "Sending " << Print(bid_ask);
  signal.emit(bid_ask);
  EXPECT_EQ(1, r1->received);
  EXPECT_EQ(1, r1a->received);
  EXPECT_EQ(2, r2->received);
  cout << endl;
}

boost::thread_specific_ptr<int> received_count;
boost::thread_specific_ptr<string> received_note;

void receive(const BidAsk& bid_ask, const string& note)
{
  cout << "\t\tReceive(" << note << "," << Print(bid_ask) << ")";
  ++(*received_count);
  (*received_note) = note;
};

TEST(SignalPrototype, PrototypeBindFunction)
{
  received_count.reset(new int(0));
  received_note.reset(new string);

  BidAsk bid_ask;
  bid_ask.set_time_stamp(100000);
  bid_ask.set_id(1);
  bid_ask.mutable_bid()->set_price(100.);

  BidAskReceiver r1(1);
  BidAskReceiver r2(2);

  EXPECT_EQ(0, r1.received);
  EXPECT_EQ(0, r2.received);

  // Create the signal:
  BidAskSignal signal;

  // Connect
  signal.connect(sigc::mem_fun(&r1, &BidAskReceiver::operator()));
  signal.connect(sigc::bind(sigc::ptr_fun(receive), "Rebound"));

  // Send the signal
  bid_ask.set_id(1);
  cout << endl << "Sending " << Print(bid_ask);
  signal.emit(bid_ask);
  EXPECT_EQ(1, r1.received);
  EXPECT_EQ(0, r2.received);
  EXPECT_EQ(1, *received_count);
  EXPECT_EQ("Rebound", *received_note);
  cout << endl;
}

template <class T_accept, class T_functor, class T_arg1>
struct mybind_functor : NoCopyAndAssign
{
  mybind_functor(T_accept* ac, T_functor* t) : ac(ac), functor(t) {}
  T_accept* ac;
  T_functor* functor;

  void operator()(T_arg1 _A_arg1)
  {
    if ((*ac)(_A_arg1)) (*functor)(_A_arg1);
  }
};

struct acceptor {
  inline bool operator()(const BidAsk& bid_ask)
  {
    return bid_ask.id() == 1;
  }
};

TEST(SignalPrototype, PrototypeAcceptor)
{
  BidAsk bid_ask;
  bid_ask.set_time_stamp(100000);
  bid_ask.mutable_bid()->set_price(100.);

  BidAskReceiver r1(1);

  // Create the signal:
  BidAskSignal signal;
  acceptor acc;

  typedef mybind_functor<acceptor, BidAskReceiver, BidAsk> Acceptor;
  Acceptor bound(&acc, &r1);

  // Connect
  signal.connect(sigc::mem_fun(&bound, &Acceptor::operator()));

  bid_ask.set_id(1);
  cout << endl << "Sending " << Print(bid_ask);
  signal.emit(bid_ask);
  EXPECT_EQ(1, r1.received);
  cout << endl;

  bid_ask.set_id(2);
  cout << endl << "Sending " << Print(bid_ask);
  signal.emit(bid_ask);
  signal.emit(bid_ask);
  EXPECT_EQ(1, r1.received);
  cout << endl;
}


template <class T_functor, class T_arg1>
struct ConditionalSlot : NoCopyAndAssign
{
  typedef boost::function<bool(const T_arg1)> Condition;

  ConditionalSlot(Condition ac, T_functor* t) : ac(ac), functor(t) {}
  Condition ac;
  T_functor* functor;

  void operator()(const T_arg1& _A_arg1)
  {
    if ((*ac)(_A_arg1)) (*functor)(_A_arg1);
  }
};

bool accept2(const BidAsk& bid_ask)
{
  return bid_ask.id() == 2;
}

TEST(SignalPrototype, PrototypeConditionalSlot)
{
  BidAsk bid_ask;
  bid_ask.set_time_stamp(100000);
  bid_ask.mutable_bid()->set_price(100.);

  BidAskReceiver r1(1);

  // Create the signal:
  BidAskSignal signal;

  typedef ConditionalSlot<BidAskReceiver, const BidAsk&> ConditionalBidAsk;
  //ConditionalBidAsk c(boost::bind(accept2), &r1);

  // Connect
  //  signal.connect(sigc::mem_fun(&c, &ConditionalBidAsk::operator()));

  bid_ask.set_id(1);
  cout << endl << "Sending " << Print(bid_ask);
  signal.emit(bid_ask);
  //EXPECT_EQ(1, r1.received);
  cout << endl;

  bid_ask.set_id(2);
  cout << endl << "Sending " << Print(bid_ask);
  signal.emit(bid_ask);
  signal.emit(bid_ask);
  //EXPECT_EQ(1, r1.received);
  cout << endl;
}


} // Namespace
