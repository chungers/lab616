#include "common.hpp"

#include <iostream>
#include <map>
#include <vector>

#include <boost/algorithm/string.hpp>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/thread.hpp>

#include <glog/logging.h>
#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <sigc++/sigc++.h>

#include <tbb/pipeline.h>
#include <tbb/tick_count.h>
#include <tbb/task_scheduler_init.h>
#include <tbb/tbb_allocator.h>


using namespace std;

/* !!!!  See AllTests.cpp for initialization of TBB scheduler.  !!! */

// Various attempts. Not all of them work so use a macro to point to
// the working version / attempt.
#define IMPL case4
static const int TICKS = 5;
static int NThread = tbb::task_scheduler_init::automatic;


//////////////////////////////////////////////////////////////////////
struct Message
{
  enum Type { BID = 0, ASK = 1 };

  ~Message() {}

  virtual Type tc() = 0;
};

struct Bid : Message
{
 public:
  int t;
  string symbol;
  double price;
  int volume;

  virtual Message::Type tc() { return BID; }
};

class Ask : public Message
{
 public:
  int t;
  string symbol;
  double price;
  int volume;

  virtual Type tc() { return ASK; }
};

template <typename M>
M* NewInstance(int i, const string& symbol, double price, int vol)
{
  M* m = new M();
  m->t = i;
  m->symbol = symbol;
  m->price = price;
  m->volume = vol;
  return m;
}

static boost::mutex cout_mutex;;

template<typename M>
ostream& Print(const string& t, M* m)
{
  boost::mutex::scoped_lock lock(cout_mutex);
  cout << t << "[t=" << m-> t
       << ",symbol=" << m->symbol
       << ",price=" << m->price
       << ",vol=" << m->volume
       << "]";
  return cout;
}

//////////////////////////////////////////////////////////////////////
template <typename M> struct Task {
  virtual void operator()(const M& m) = 0;
};

typedef Task<Bid> BidTask;
typedef Task<Ask> AskTask;

class Strategy;
typedef map<string, Strategy*> StrategyMap;

//////////////////////////////////////////////////////////////////////
struct Strategy : public BidTask, public AskTask {
  Strategy(const string& symbol) : symbol(symbol) {}

  string symbol;

  virtual void operator()(const Bid& bid)
  {
    EXPECT_EQ(symbol, bid.symbol);
    boost::mutex::scoped_lock lock(cout_mutex);
    Self() << "@ t=" << bid.t << ", " << bid.symbol
           << ", Bid = " << bid.price << endl;
  }

  virtual void operator()(const Ask& ask)
  {
    EXPECT_EQ(symbol, ask.symbol);
    boost::mutex::scoped_lock lock(cout_mutex);
    Self() << "@ t=" << ask.t << ", " << ask.symbol
           << ", Ask = " << ask.price << endl;
  }

  ostream& Self()
  {
    cout << "Strategy[" << symbol << "]: ";
    return cout;
  }
};

TEST(TbbPrototype, StrategyTest)
{
  Strategy s("AAPL");
  Bid* bid = NewInstance<Bid>(1, "AAPL", 100., 20);
  s(*bid);

  Strategy* p = new Strategy("PCLN");
  Ask* ask = NewInstance<Ask>(1, "PCLN", 200., 30);
  (*p)(*ask);
  delete p;
};

//////////////////////////////////////////////////////////////////////
class TaskFilter : public tbb::filter, NoCopyAndAssign {
 public:
  TaskFilter(const string& stage) :
      tbb::filter(parallel),
      stage_(stage)
  {}

  virtual void* operator()(void* task)
  {
    return IMPL(task);
  }

 private:

  // Various attemtps to implement the operator()
  void* case1(void* task);
  void* case2(void* task);
  void* case3(void* task);
  void* case4(void* task);

  string stage_;
};

class InputFilter : public tbb::filter, NoCopyAndAssign {
 public:
  InputFilter(const string& id, int events, const StrategyMap& sm) :
      filter(serial_in_order),
      id_(id),
      messages_(events), sent_(0),
      strategy_map_(sm)
  {}

  ~InputFilter() {}

  virtual void* operator()(void* task)
  {
    return IMPL(task);
  }

 private:

  // Various attemtps to implement the operator()
  void* case1(void* task);
  void* case2(void* task);
  void* case3(void* task);
  void* case4(void* task);

  string id_;
  int messages_;
  int sent_;
  const StrategyMap& strategy_map_;
};

void CleanUp(map<string, Strategy*>* m)
{
  map<string, Strategy*>::iterator itr;
  for (itr = m->begin(); itr != m->end(); ++itr) {
    cout << "Deleting " << itr->second << endl;
    delete itr->second;
  }
}

//////////////////////////////////////////////////////////////////////
////  Case 1: Using Simple Data Object
void* InputFilter::case1(void* task)
{
  if (++sent_ <= messages_) {
    Bid* bid = NewInstance<Bid>(sent_, "PCLN", 1.0, sent_);
    Print<Bid>("--> BID", bid) << endl;
    return bid;
  }
  return NULL;
}

void* TaskFilter::case1(void* task)
{
  Bid* bid = static_cast<Bid*>(task);
  Print<Bid>("  1111>>>> BID", bid) << endl;
  return task;
}

//////////////////////////////////////////////////////////////////////
////  Case 2: Using Container Message Object with void*
class Event
{
 public:
  enum Type { BID = 0, ASK = 1 };

  ~Event() { /* TODO: leak here. */ }
  Event(Bid* bid) : type_(BID), event_(bid) {}
  Event(Ask* ask) : type_(ASK), event_(ask) {}

 private:
  Type type_;
  void* event_;

 public:
  Type Type() const { return type_; }
  void* Get() const { return event_; }
};

TEST(TbbPrototype, EventClass)
{
  Bid* bid = NewInstance<Bid>(1, "PCLN", 100., 10);
  Event event(bid);
  cout << "Event type = " << event.Type() << ", pt = " << event.Get() << endl;
  switch (event.Type()) {
    case Event::BID :
      Print<Bid>(">>>BID", static_cast<Bid*>(event.Get())) << endl; break;
    case Event::ASK :
      Print<Ask>(">>>ASK", static_cast<Ask*>(event.Get())) << endl; break;
  }
};

void* InputFilter::case2(void* task)
{
  if (++sent_ <= messages_) {
    Bid* bid = NewInstance<Bid>(sent_, "PCLN", 1.0, sent_);
    Print<Bid>("--> BID", bid) << endl;
    Event* event = new Event(bid);
    return event;
  }
  return NULL;
}

void* TaskFilter::case2(void* task)
{
  Event& event = * static_cast<Event*>(task);
  cout << "Event type = " << event.Type() << ", pt = " << event.Get() << endl;
  switch (event.Type()) {
    case Event::BID :
      Print<Bid>(" 2222>>>>BID", static_cast<Bid*>(event.Get())) << endl; break;
    case Event::ASK :
      Print<Ask>( "2222>>>>ASK", static_cast<Ask*>(event.Get())) << endl; break;
  }
  return task;
}


//////////////////////////////////////////////////////////////////////
////  Case 3: Using StrategyClosure

class StrategyClosure
{
 public:
  enum Type { BID = 0, ASK = 1 };

  StrategyClosure(Strategy* strategy, Type t, void* m):
      strategy_(strategy), t_(t), message_(m) {}
  ~StrategyClosure()
  {
    //    delete message_;
  }

  void call()
  {
    switch (t_) {
      case BID:
        (*strategy_)(* static_cast<Bid*>(message_));
        break;
      case ASK:
        (*strategy_)(* static_cast<Ask*>(message_));
        break;
    }
  }

 private:
  Strategy* strategy_;
  Type t_;
  void* message_;
};

TEST(TbbPrototype, StrategyClosureTest)
{
  Strategy s("AAPL");
  Bid* bid = NewInstance<Bid>(1, "AAPL", 100., 20);
  s(*bid);

  StrategyClosure sc(&s, StrategyClosure::BID, bid);
  sc.call();

  Strategy* p = new Strategy("PCLN");
  Ask* ask = NewInstance<Ask>(1, "PCLN", 200., 30);
  (*p)(*ask);

  StrategyClosure* pc = new StrategyClosure(p, StrategyClosure::ASK, ask);
  pc->call();
  delete p;
  delete pc;
};

void* InputFilter::case3(void* task)
{
  if (++sent_ <= messages_) {
    string sym = (sent_ % 4 < 2) ? "AAPL" : "NFLX";
    if (sent_ % 2) {
      Bid* bid = NewInstance<Bid>(sent_, sym, 1.0, 10);
      Print<Bid>("--> BID", bid) << endl;
      Strategy* s = strategy_map_.find(sym)->second;
      CHECK(s);
      StrategyClosure* sc = new StrategyClosure(s, StrategyClosure::BID, bid);
      return sc;
    } else {
      Ask* ask = NewInstance<Ask>(sent_, sym, 2.0, 20);
      Print<Ask>("--> ASK", ask) << endl;
      Strategy* s = strategy_map_.find(sym)->second;
      CHECK(s);
      StrategyClosure* sc = new StrategyClosure(s, StrategyClosure::ASK, ask);
      return sc;
    }
  }
  return NULL;
}

void* TaskFilter::case3(void* task)
{
  if (task) {
    cout << task << ".... Stage[" << stage_ << "]:\t\t\t";
    // Simply invoke the task closure.
    StrategyClosure& c = * static_cast<StrategyClosure*>(task);
    c.call();
    return task;
  }
  return NULL;
}


//////////////////////////////////////////////////////////////////////
////  Case 4: Using Generic Closure

struct Callable
{
  ~Callable() {}
  virtual void call() = 0;
};

template <typename M>
class SClosure : public Callable
{
 public:

  SClosure(Strategy* strategy, M* m):
      strategy_(strategy), message_(m) {}
  ~SClosure()
  {
    //    delete message_;
  }

  virtual void call()
  {
    (*strategy_)(*message_);
  }

 private:
  Strategy* strategy_;
  M* message_;
};

TEST(TbbPrototype, CallableTest)
{
  Strategy s("AAPL");
  Bid* bid = NewInstance<Bid>(1, "AAPL", 100., 20);
  s(*bid);

  SClosure<Bid> sc(&s, bid);
  sc.call();
}

void* InputFilter::case4(void* task)
{
  if (++sent_ <= messages_) {
    string sym = (sent_ % 4 < 2) ? "AAPL" : "NFLX";
    if (sent_ % 2) {
      Bid* bid = NewInstance<Bid>(sent_, sym, 1.0, 10);
      Print<Bid>("--> BID", bid) << endl;
      Strategy* s = strategy_map_.find(sym)->second;
      CHECK(s);
      SClosure<Bid>* sc = new SClosure<Bid>(s, bid);
      return sc;
    } else {
      Ask* ask = NewInstance<Ask>(sent_, sym, 2.0, 20);
      Print<Ask>("--> ASK", ask) << endl;
      Strategy* s = strategy_map_.find(sym)->second;
      CHECK(s);
      SClosure<Ask>* sc = new SClosure<Ask>(s, ask);
      return sc;
    }
  }
  return NULL;
}

void* TaskFilter::case4(void* task)
{
  if (task) {
    cout << task << ".... Stage[" << stage_ << "]:\t\t\t";
    // Simply invoke the task closure.
    Callable& c = * static_cast<Callable*>(task);
    c.call();
    return task;
  }
  return NULL;
}



//////////////////////////////////////////////////////////////////////
//                             TEST                                 //
//////////////////////////////////////////////////////////////////////
TEST(TbbPrototype, Pipeline)
{
  // Set up the strategies
  map<string, Strategy*> strategies;
  strategies["AAPL"] = new Strategy("AAPL");
  strategies["PCLN"] = new Strategy("PCLN");
  strategies["NFLX"] = new Strategy("NFLX");

  InputFilter input("TickSource", TICKS, strategies);
  TaskFilter strategy("Strategy");

  tbb::pipeline pipeline;
  pipeline.add_filter(input);
  pipeline.add_filter(strategy);

  cout << "Start..." << endl;
  tbb::tick_count t0 = tbb::tick_count::now();

  pipeline.run(10 * 4);

  tbb::tick_count t1 = tbb::tick_count::now();
  cout << "Run time = " << (t1 - t0).seconds() << endl;

  CleanUp(&strategies);
}
