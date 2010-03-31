#ifndef MARKET_DATA_CLIENT_H_
#define MARKET_DATA_CLIENT_H_


class MarketDataClient
{
public:

  MarketDataClient();
  ~MarketDataClient();

  void processMessages();

public:

  bool connect(const char * host, unsigned int port, int clientId = 0);
  void disconnect() const;
  bool isConnected() const;
  void error(const int id, const int errorCode, const IBString errorString);
  
  void tickPrice(TickerId tickerId, TickType field, double price,
                 int canAutoExecute);
  void tickSize(TickerId tickerId, TickType field, int size);
  void tickOptionComputation(TickerId tickerId, TickType tickType,
                             double impliedVol, double delta,
                             double modelPrice, double pvDividend);
  void tickGeneric(TickerId tickerId, TickType tickType, double value);
  void tickString(TickerId tickerId, TickType tickType, const IBString& value);
  void tickEFP(TickerId tickerId, TickType tickType, double basisPoints,
               const IBString& formattedBasisPoints,
               double totalDividends, int holdDays,
               const IBString& futureExpiry, double dividendImpact,
               double dividendsToExpiry);
  void updateMktDepth(TickerId id, int position, int operation, int side,
                      double price, int size);
  void updateMktDepthL2(TickerId id, int position, IBString marketMaker,
                        int operation, int side, double price, int size);
  void historicalData(TickerId reqId, const IBString& date, double open,
                      double high, double low, double close,
                      int volume, int barCount, double WAP, int hasGaps);
  void tickSnapshotEnd(int reqId);
};

#endif // MARKET_DATA_CLIENT_H_

