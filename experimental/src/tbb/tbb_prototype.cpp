#include "common.hpp"

#include <iostream>
#include <map>
#include <vector>

#include <boost/algorithm/string.hpp>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/thread.hpp>

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <sigc++/sigc++.h>

#include <tbb/pipeline.h>
#include <tbb/tick_count.h>
#include <tbb/task_scheduler_init.h>
#include <tbb/tbb_allocator.h>
#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <cctype>


using namespace std;

/* !!!!  See AllTests.cpp for initialization of TBB scheduler.  !!! */

static int NThread = tbb::task_scheduler_init::automatic;

//////////////////////////////////////////////////////////////////////
struct Bid {
  int t;
  string symbol;
  double price;
  int volume;
};

struct Ask {
  int t;
  string symbol;
  double price;
  int volume;
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

//////////////////////////////////////////////////////////////////////
struct Strategy : public BidTask, public AskTask {
  Strategy(const string& symbol) : symbol(symbol) {}

  string symbol;

  virtual void operator()(const Bid& bid)
  {
    boost::mutex::scoped_lock lock(cout_mutex);
    Print() << "Bid = " << bid.price << endl;
  }

  virtual void operator()(const Ask& ask)
  {
    boost::mutex::scoped_lock lock(cout_mutex);
    Print() << "Ask = " << ask.price << endl;
  }

  ostream& Print()
  {
    cout << "Strategy[" << symbol << "]: ";
    return cout;
  }
};


//////////////////////////////////////////////////////////////////////
typedef map<string, Strategy*> StrategyMap;

class StrategyClosure
{
 public:
  enum Type { BID = 0, ASK = 1 };

  StrategyClosure(Strategy* strategy, Type t, void* m):
      strategy_(*strategy), t_(t), message_(m) {}
  ~StrategyClosure()
  {
    //    delete message_;
  }

  void call()
  {
    switch (t_) {
      case BID:
        strategy_(* static_cast<Bid*>(message_));
        break;
      case ASK:
        strategy_(* static_cast<Ask*>(message_));
        break;
    }
  }

 private:
  Strategy& strategy_;
  Type t_;
  void* message_;
};

class TaskFilter : public tbb::filter {
 public:
  TaskFilter(const string& stage) :
      tbb::filter(parallel),
      stage_(stage)
  {}

  virtual void* operator()(void* task)
  {
    Bid* bid = static_cast<Bid*>(task);
    Print<Bid>("****** BID", bid) << endl;
    return task;
  }

  virtual void* operator0(void* task)
  {
    if (task) {
      // Simply invoke the task closure.
      StrategyClosure& c = * static_cast<StrategyClosure*>(task);
      cout << "Stage[" << stage_ << "]:\t\t\t";
      c.call();
      return task;
    }
    return NULL;
  }

 private:
  const string& stage_;
};

class InputFilter : public tbb::filter {
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
    if (++sent_ <= messages_) {
      Bid* bid = NewInstance<Bid>(sent_, "PCLN", 1.0, sent_);
      Print<Bid>("--> BID", bid) << endl;
      return bid;
    }
    return NULL;
  }

  virtual void* operator0(void* task)
  {
    if (++sent_ <= messages_) {
      string sym = (sent_ % 4 < 2) ? "AAPL" : "NFLX";
      if (sent_ % 2) {
        Bid* bid = NewInstance<Bid>(sent_, sym, 1.0, 10);
        Print<Bid>("--> BID", bid) << endl;
        Strategy* s = strategy_map_.find(sym)->second;
        StrategyClosure* sc = new StrategyClosure(s, StrategyClosure::BID, bid);
        return sc;
      } else {
        Ask* ask = NewInstance<Ask>(sent_, sym, 2.0, 20);
        Print<Ask>("--> ASK", ask) << endl;
        Strategy* s = strategy_map_.find(sym)->second;
        StrategyClosure* sc = new StrategyClosure(s, StrategyClosure::ASK, ask);
        return sc;
      }
    }
    return NULL;
  }

 private:
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
//                             TEST                                 //
//////////////////////////////////////////////////////////////////////
TEST(TbbPrototype, Pipeline)
{
  // Set up the strategies
  map<string, Strategy*> strategies;
  strategies["AAPL"] = new Strategy("AAPL");
  strategies["PCLN"] = new Strategy("PCLN");
  strategies["NFLX"] = new Strategy("NFLX");

  InputFilter input("TickSource", 10, strategies);
  TaskFilter strategy("Strategy");

  tbb::pipeline pipeline;
  pipeline.add_filter(input);
  pipeline.add_filter(strategy);

  cout << "Start..." << endl;
  tbb::tick_count t0 = tbb::tick_count::now();

  pipeline.run(4);

  tbb::tick_count t1 = tbb::tick_count::now();
  cout << "Run time = " << (t1 - t0).seconds() << endl;

  CleanUp(&strategies);
}
