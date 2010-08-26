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

template <class T> struct Receiver : sigc::trackable, NoCopyAndAssign
{
  virtual void operator()(const T& arg) = 0;
};

template <typename T_arg1, typename T_functor>
struct ConditionalFunctor
{
  ConditionalFunctor(Predicate<T_arg1>* p, T_functor* t)
      : predicate(p), functor(t)
  {
    CHECK(predicate);
    CHECK(functor);
  }

  Predicate<T_arg1>* predicate;
  T_functor* functor;

  inline void operator()(T_arg1 arg1)
  {
    if ((*predicate)(arg1)) (*functor)(arg1);
  }
};

class BackPlane : NoCopyAndAssign {

 public:

  ~BackPlane() {}

 public:
  static BackPlane* Create();
  virtual void Register(Receiver<Connect>* r,
                        Predicate<Connect>* predicate = NULL) = 0;

  virtual void Register(Receiver<Disconnect>* r,
                        Predicate<Disconnect>* predicate = NULL) = 0;

  virtual void Register(Receiver<BidAsk>* r,
                        Predicate<BidAsk>* predicate = NULL) = 0;

 protected:
  BackPlane();
};


} // namespace ib
#endif // IB_BACKPLANE_H_
