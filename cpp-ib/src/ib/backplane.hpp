#ifndef IB_BACKPLANE_H_
#define IB_BACKPLANE_H_

#include <sigc++/sigc++.h>

#include "common.hpp"
#include "ib/ib_events.pb.h"

using namespace ib::events;

namespace ib {

template <class T> struct Predicate
{
  virtual bool operator()(const T& arg) = 0;
};

template <typename T_arg1, typename T_functor>
struct ConditionalFunctor
{
  ConditionalFunctor(Predicate<T_arg1>* p, T_functor* t)
      : predicate(p), functor(t) {}
  Predicate<T_arg1>* predicate;
  T_functor* functor;

  inline void operator()(T_arg1 arg1)
  {
    if ((*predicate)(arg1)) (*functor)(arg1);
  }
};

struct BidAskReceiver : sigc::trackable, NoCopyAndAssign {
  virtual void operator()(const BidAsk& bid_ask) = 0;
};


class BackPlane : NoCopyAndAssign {

 public:

  typedef sigc::signal<void, const BidAsk&> BidAskSignal;
  typedef ConditionalFunctor<BidAsk, BidAskReceiver> BidAskFilter;

  BackPlane();
  ~BackPlane() {}

  void Register(BidAskFilter* r);
  void Register(BidAskReceiver* r);

 private:
  BidAskSignal bid_ask_signal_;

};


} // namespace ib
#endif // IB_BACKPLANE_H_
