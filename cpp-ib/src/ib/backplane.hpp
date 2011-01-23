#ifndef IB_BACKPLANE_H_
#define IB_BACKPLANE_H_

#include <set>
#include <string>
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

class BackPlane : NoCopyAndAssign
{

 public:

  ~BackPlane() {}

 public:

  // Factory method, constructs an instance.
  static BackPlane* Create();


  virtual void Register(Receiver<Connect>* r,
                        Predicate<Connect>* predicate = NULL) = 0;

  virtual void Register(Receiver<Disconnect>* r,
                        Predicate<Disconnect>* predicate = NULL) = 0;

  virtual void Register(Receiver<BidAsk>* r,
                        Predicate<BidAsk>* predicate = NULL) = 0;

  typedef int64_t Timestamp;
  typedef int Id;

  virtual void OnConnect(Timestamp t, Id id) = 0;

  virtual void OnDisconnect(Timestamp t, Id id) = 0;

  virtual void OnBid(Timestamp t, Id id, double price) = 0;
  virtual void OnBid(Timestamp t, Id id, int size) = 0;

  virtual void OnAsk(Timestamp t, Id id, double price) = 0;
  virtual void OnAsk(Timestamp t, Id id, int size) = 0;

 protected:
  BackPlane();
};


namespace signal {

using namespace ib::events;

int GetTickerId(const std::string& symbol);
void GetSymbol(const int id, std::string* symbol);

class Selection :
    public Predicate<Connect>,
    public Predicate<Disconnect>,
    public Predicate<BidAsk>
{
 public:
  Selection() { }
  ~Selection() { }

  inline Selection& operator<<(int id)
  { return Add(id); }

  inline Selection& operator<<(const std::string& symbol)
  { return Add(symbol); }

  inline Selection& Add(int id)
  {
    ids_.insert(id);
    return *this;
  }

  Selection& Add(const std::string& symbol);

  virtual inline bool operator()(const Connect& connect)
  { return (*this)(connect.id()); }

  virtual inline bool operator()(const Disconnect& disconnect)
  { return (*this)(disconnect.id()); }

  virtual inline bool operator()(const BidAsk& bid_ask)
  { return (*this)(bid_ask.id()); }

 protected:
  std::set<int> ids_;

  inline bool operator()(int id)
  {
    std::set<int>::iterator itr = ids_.find(id);
    return itr != ids_.end();
  }
};

class Exclusion : public Selection
{
 public:
  Exclusion() {}
  ~Exclusion() {}

  virtual inline bool operator()(const Connect& connect)
  { return (*this)(connect.id()); }

  virtual inline bool operator()(const Disconnect& disconnect)
  { return (*this)(disconnect.id()); }

  virtual inline bool operator()(const BidAsk& bid_ask)
  { return (*this)(bid_ask.id()); }

 protected:
  inline bool operator()(int id)
  {
    std::set<int>::iterator itr = ids_.find(id);
    return itr == ids_.end();
  }
};

} // namespace signal
} // namespace ib
#endif // IB_BACKPLANE_H_
