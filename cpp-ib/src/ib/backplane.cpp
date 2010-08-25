
#include "ib/backplane.hpp"

namespace ib {

BackPlane::BackPlane()
{

}

void BackPlane::Register(BackPlane::BidAskFilter* r)
{
  bid_ask_signal_.connect(sigc::mem_fun(r, &BidAskFilter::operator()));
}

void BackPlane::Register(BidAskReceiver* r)
{
  bid_ask_signal_.connect(sigc::mem_fun(r, &BidAskReceiver::operator()));
}


}
