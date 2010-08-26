#include <vector>
#include <glog/logging.h>
#include "ib/backplane.hpp"

using namespace std;

namespace ib {

BackPlane::BackPlane()
{

}

typedef sigc::signal< void, const Connect& > ConnectSignal;
typedef ConditionalFunctor< Connect, Receiver<Connect> > ConnectFilter;

typedef sigc::signal< void, const Disconnect& > DisconnectSignal;
typedef ConditionalFunctor< Disconnect, Receiver<Disconnect> > DisconnectFilter;

typedef sigc::signal< void, const BidAsk& > BidAskSignal;
typedef ConditionalFunctor< BidAsk, Receiver<BidAsk> > BidAskFilter;


// template <typename T> void DeleteAll(vector<T*>* v)
// {
//   vector<T*>::iterator itr;
//   for (itr = v->begin(); itr != v->end(); ++itr) {
//     delete *itr;
//   }
// };

class BackPlaneImpl : public BackPlane
{
 public:
  BackPlaneImpl()
  {
  }

  ~BackPlaneImpl()
  {
    vector<ConnectFilter*>::iterator itr1;
    for (itr1 = connect_filters_.begin();
         itr1 != connect_filters_.end();
         ++itr1) {
      delete *itr1;
    }
    vector<DisconnectFilter*>::iterator itr2;
    for (itr2 = disconnect_filters_.begin();
         itr2 != disconnect_filters_.end();
         ++itr2) {
      delete *itr2;
    }
    vector<BidAskFilter*>::iterator itr3;
    for (itr3 = bid_ask_filters_.begin();
         itr3 != bid_ask_filters_.end();
         ++itr3) {
      delete *itr3;
    }
  }

  virtual void Register(Receiver<Connect>* r,
                        Predicate<Connect>* predicate = NULL)
  {
    if (predicate == NULL) {
      connect_signal_.connect(
          sigc::mem_fun(r, &Receiver<Connect>::operator()));
    } else {
      ConnectFilter* filter = new ConnectFilter(predicate, r);
      connect_signal_.connect(
          sigc::mem_fun(filter, &ConnectFilter::operator()));
    }
  }

  virtual void Register(Receiver<Disconnect>* r,
                        Predicate<Disconnect>* predicate = NULL)
  {
    if (predicate == NULL) {
      disconnect_signal_.connect(
          sigc::mem_fun(r, &Receiver<Disconnect>::operator()));
    } else {
      DisconnectFilter* filter = new DisconnectFilter(predicate, r);
      disconnect_signal_.connect(
          sigc::mem_fun(filter, &DisconnectFilter::operator()));
    }
  }

   virtual void Register(Receiver<BidAsk>* r,
                        Predicate<BidAsk>* predicate = NULL)
  {
    if (predicate == NULL) {
      bid_ask_signal_.connect(sigc::mem_fun(r, &Receiver<BidAsk>::operator()));
    } else {
      BidAskFilter* filter = new BidAskFilter(predicate, r);
      bid_ask_signal_.connect(sigc::mem_fun(filter, &BidAskFilter::operator()));
    }
  }

 private:
  ConnectSignal connect_signal_;
  vector<ConnectFilter*> connect_filters_;

  DisconnectSignal disconnect_signal_;
  vector<DisconnectFilter*> disconnect_filters_;

  BidAskSignal bid_ask_signal_;
  vector<BidAskFilter*> bid_ask_filters_;
};

BackPlane* BackPlane::Create()
{
  return new BackPlaneImpl();
}

}
