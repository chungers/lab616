
#include <string>
#include <vector>
#include <boost/ptr_container/ptr_vector.hpp>
#include <boost/scoped_ptr.hpp>
#include <glog/logging.h>
#include "ib/backplane.hpp"
#include "ib/ticker_id.hpp"

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


class BackPlaneImpl : public BackPlane
{
 public:
  BackPlaneImpl()
  {
  }

  ~BackPlaneImpl()
  {
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
      connect_filters_.push_back(filter);
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
      disconnect_filters_.push_back(filter);
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
      bid_ask_filters_.push_back(filter);
    }
  }

  virtual void OnConnect(Timestamp t, Id id)
  {
    boost::scoped_ptr<Connect> connect(new Connect());
    connect->set_id(id);
    connect->set_time_stamp(t);
    connect_signal_.emit(*connect);
  }

  virtual void OnDisconnect(Timestamp t, Id id)
  {
    boost::scoped_ptr<Disconnect> disconnect(new Disconnect());
    disconnect->set_id(id);
    disconnect->set_time_stamp(t);
    disconnect_signal_.emit(*disconnect);
  }

  virtual void OnBid(Timestamp t, Id id, double price)
  {
    boost::scoped_ptr<BidAsk> bidask(new BidAsk());
    bidask->set_id(id);
    bidask->set_time_stamp(t);
    BidAsk_Bid* bid = bidask->mutable_bid();
    bid->set_price(price);
    bid_ask_signal_.emit(*bidask);
  }

  virtual void OnBid(Timestamp t, Id id, int size)
  {
    boost::scoped_ptr<BidAsk> bidask(new BidAsk());
    bidask->set_id(id);
    bidask->set_time_stamp(t);
    BidAsk_Bid* bid = bidask->mutable_bid();
    bid->set_size(size);
    bid_ask_signal_.emit(*bidask);
  }

  virtual void OnAsk(Timestamp t, Id id, double price)
  {
    boost::scoped_ptr<BidAsk> bidask(new BidAsk());
    bidask->set_id(id);
    bidask->set_time_stamp(t);
    BidAsk_Ask* ask = bidask->mutable_ask();
    ask->set_price(price);
    bid_ask_signal_.emit(*bidask);
  }

  virtual void OnAsk(Timestamp t, Id id, int size)
  {
    boost::scoped_ptr<BidAsk> bidask(new BidAsk());
    bidask->set_id(id);
    bidask->set_time_stamp(t);
    BidAsk_Ask* ask = bidask->mutable_ask();
    ask->set_size(size);
    bid_ask_signal_.emit(*bidask);
  }

 private:
  ConnectSignal connect_signal_;
  boost::ptr_vector<ConnectFilter> connect_filters_;

  DisconnectSignal disconnect_signal_;
  boost::ptr_vector<DisconnectFilter> disconnect_filters_;

  BidAskSignal bid_ask_signal_;
  boost::ptr_vector<BidAskFilter> bid_ask_filters_;
};

BackPlane* BackPlane::Create()
{
  return new BackPlaneImpl();
}


namespace signal {

int GetTickerId(const std::string& symbol)
{ return ib::internal::SymbolToTickerId(symbol); }

void GetSymbol(const int id, std::string* symbol)
{ ib::internal::SymbolFromTickerId(id, symbol); }

Selection& Selection::Add(const std::string& symbol)
{ return Selection::Add(GetTickerId(symbol)); }

} // namespace signal
} // namespace ib
