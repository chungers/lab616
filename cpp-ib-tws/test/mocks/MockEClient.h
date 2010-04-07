// 2010 - lab616 MockEClient.h

#include "api/Shared/Contract.h"
#include "api/Shared/EClient.h"
#include "api/Shared/Execution.h"
#include "api/Shared/Order.h"
#include "api/Shared/OrderState.h"
#include "api/Shared/ScannerSubscription.h"

#include <gmock/gmock.h>

class MockEClient : public EClient {
public:
  MOCK_METHOD3(eConnect,
               bool(const char *host, unsigned int port, int clientId));
  MOCK_METHOD0(eDisconnect,
               void());
  MOCK_METHOD0(serverVersion,
               int());
  MOCK_METHOD0(TwsConnectionTime,
               IBString());
  MOCK_METHOD4(reqMktData,
               void(TickerId id, const Contract &contract, 	 const IBString& genericTicks, bool snapshot));
  MOCK_METHOD1(cancelMktData,
               void(TickerId id));
  MOCK_METHOD3(placeOrder,
               void(OrderId id, const Contract &contract, const Order &order));
  MOCK_METHOD1(cancelOrder,
               void(OrderId id));
  MOCK_METHOD0(reqOpenOrders,
               void());
  MOCK_METHOD2(reqAccountUpdates,
               void(bool subscribe, const IBString& acctCode));
  MOCK_METHOD2(reqExecutions,
               void(int reqId, const ExecutionFilter& filter));
  MOCK_METHOD1(reqIds,
               void(int numIds));
  MOCK_METHOD0(checkMessages,
               bool());
  MOCK_METHOD2(reqContractDetails,
               void(int reqId, const Contract &contract));
  MOCK_METHOD3(reqMktDepth,
               void(TickerId id, const Contract &contract, int numRows));
  MOCK_METHOD1(cancelMktDepth,
               void(TickerId id));
  MOCK_METHOD1(reqNewsBulletins,
               void(bool allMsgs));
  MOCK_METHOD0(cancelNewsBulletins,
               void());
  MOCK_METHOD1(setServerLogLevel,
               void(int level));
  MOCK_METHOD1(reqAutoOpenOrders,
               void(bool bAutoBind));
  MOCK_METHOD0(reqAllOpenOrders,
               void());
  MOCK_METHOD0(reqManagedAccts,
               void());
  MOCK_METHOD1(requestFA,
               void(faDataType pFaDataType));
  MOCK_METHOD2(replaceFA,
               void(faDataType pFaDataType, const IBString& cxml));
  MOCK_METHOD8(reqHistoricalData,
               void(TickerId id, const Contract &contract, 	 const IBString &endDateTime, const IBString &durationStr, const IBString &barSizeSetting, 	 const IBString &whatToShow, int useRTH, int formatDate));
  MOCK_METHOD6(exerciseOptions,
               void(TickerId id, const Contract &contract, int exerciseAction, int exerciseQuantity, const IBString &account, int override));
  MOCK_METHOD1(cancelHistoricalData,
               void(TickerId tickerId));
  MOCK_METHOD5(reqRealTimeBars,
               void(TickerId id, const Contract &contract, int barSize, 	 const IBString &whatToShow, bool useRTH));
  MOCK_METHOD1(cancelRealTimeBars,
               void(TickerId tickerId));
  MOCK_METHOD1(cancelScannerSubscription,
               void(int tickerId));
  MOCK_METHOD0(reqScannerParameters,
               void());
  MOCK_METHOD2(reqScannerSubscription,
               void(int tickerId, const ScannerSubscription &subscription));
  MOCK_METHOD0(reqCurrentTime,
               void());
  MOCK_METHOD3(reqFundamentalData,
               void(TickerId reqId, const Contract&, const IBString& reportType));
  MOCK_METHOD1(cancelFundamentalData,
               void(TickerId reqId));
};