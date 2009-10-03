package com.jsystemtrader.platform.trader;

import com.ib.client.*;
import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.position.*;
import com.jsystemtrader.platform.preferences.JSTPreferences;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import com.jsystemtrader.platform.quote.*;
import com.jsystemtrader.platform.report.*;
import com.jsystemtrader.platform.startup.*;
import com.jsystemtrader.platform.strategy.*;
import com.jsystemtrader.platform.util.*;

import javax.swing.*;
import java.util.*;


public class TraderAssistant {
    private final String host, advisorAccountID;
    private final int port, clientID;

    /**
     * Space the sequential historical requests by at least 10 seconds.
     *
     * IB's historical data server imposes limitations on the number of
     * historical requests made within a certain period of time. At this
     * time, this limit appears to be 60 requests in 5 minutes.
     */
    private static final int MAX_REQUEST_FREQUENCY_MILLIS = 10100;
    private EClientSocket socket;
    private final Map<Integer, Strategy> strategies;
    protected final Map<Integer, OpenOrder> openOrders;
    protected final Map<String, PortfolioMirrorItem> portfolioMirror;
    protected final Report eventReport;
    private int nextStrategyID;
    protected int orderID;
    private String accountCode;// used to determine if TWS is running against real or paper trading account
    private long serverTime;
    private int serverVersion;
    protected final Trader trader;
    private long lastRequestTime;


    public TraderAssistant(Trader trader) throws JSystemTraderException {
        this.trader = trader;

        eventReport = Dispatcher.getReporter();
        
        // contains strategies and their IDs
        strategies = new HashMap<Integer, Strategy>();
        // contains orders and their IDs
        openOrders = new HashMap<Integer, OpenOrder>();
        // contains portfolio replication
        portfolioMirror = new HashMap<String, PortfolioMirrorItem>();

        PreferencesHolder properties = PreferencesHolder.getInstance();
        boolean isAdvisorAccountUsed = properties.getBool(JSTPreferences.AdvisorAccount);
        if (isAdvisorAccountUsed) {
            advisorAccountID = properties.get(JSTPreferences.AdvisorAccountNumber);
        } else {
            advisorAccountID = "";
        }

        host = properties.get(JSTPreferences.Host);
        port = properties.getInt(JSTPreferences.Port);
        clientID = properties.getInt(JSTPreferences.ClientID);
    }

    public Map<Integer, OpenOrder> getOpenOrders() {
        return openOrders;
    }

    public Strategy getStrategy(int strategyId) {
        return strategies.get(strategyId);
    }

    public void connect() throws JSystemTraderException {

        eventReport.report("Connecting to TWS");
        socket = new EClientSocket(trader);

        if (!socket.isConnected()) {
            socket.eConnect(host, port, clientID);
        }
        if (!socket.isConnected()) {
            throw new JSystemTraderException("Could not connect to TWS. See report for details.");
        }

        // IB Log levels: 1=SYSTEM 2=ERROR 3=WARNING 4=INFORMATION 5=DETAIL
        socket.setServerLogLevel(2);
        socket.reqNewsBulletins(true);
        serverVersion = socket.serverVersion();

        eventReport.report("Connected to TWS");

    }

    public int getServerVersion() {
        return serverVersion;
    }


    public void disconnect() {
        if (socket != null && socket.isConnected()) {
            socket.cancelNewsBulletins();
            socket.eDisconnect();
        }
    }

    /**
     * While TWS was disconnected from the IB server, some order executions may have occured.
     * To detect executions, request them explicitly after the reconnection.
     */
    public void requestExecutions() {
        try {
            eventReport.report("Requested executions.");
            for (OpenOrder openOrder : openOrders.values()) {
                openOrder.reset();
            }
            socket.reqExecutions(new ExecutionFilter());
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    public void getHistoricalData(int strategyId, Contract contract, String endDateTime, String duration, String barSize, String whatToShow, int useRTH, int formatDate) throws InterruptedException {

        // Only one historical data request can hit the socket at a time, so
        // we wait here to be notified that no historical requests are pending.
        synchronized (trader) {
            while (trader.getIsPendingHistRequest()) {
                trader.wait();
            }

            trader.setIsPendingHistRequest(true);
            long elapsedSinceLastRequest = System.currentTimeMillis() - lastRequestTime;
            long remainingTime = MAX_REQUEST_FREQUENCY_MILLIS - elapsedSinceLastRequest;
            if (remainingTime > 0) {
                Thread.sleep(remainingTime);
            }
        }

        QuoteHistory qh = getStrategy(strategyId).getQuoteHistory();
        qh.setIsHistRequestCompleted(false);

        String msg = qh.getStrategyName() + ": " + "Requested Historical data. ";
        msg += "End time: " + endDateTime;
        eventReport.report(msg);

        lastRequestTime = System.currentTimeMillis();
        socket.reqHistoricalData(strategyId, contract, endDateTime, duration, barSize, whatToShow, useRTH, formatDate);
    }

    public void getRealTimeBars(int strategyId, Contract contract, int barSize, String whatToShow, boolean useRTH) {
        socket.reqRealTimeBars(strategyId, contract, barSize, whatToShow, useRTH);
        QuoteHistory qh = getStrategy(strategyId).getQuoteHistory();
        String msg = qh.getStrategyName() + ": " + "Requested real time bars.";
        eventReport.report(msg);
    }

    public synchronized void addStrategy(Strategy strategy) {
        nextStrategyID++;
        strategy.setId(nextStrategyID);
        strategies.put(nextStrategyID, strategy);
        
        PortfolioMirrorItem portfolioMirrorItem = portfolioMirror.get(_generateContractKey(strategy.getContract()));
        if(portfolioMirrorItem!=null)
        {
            strategy.getPositionManager().update(portfolioMirrorItem);
            Dispatcher.fireModelChanged(ModelListener.Event.STRATEGY_UPDATE, strategy);
        }
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime * 1000L;
    }

    public synchronized void placeOrder(Contract contract, Order order, Strategy strategy) {
        try {
            orderID++;
            openOrders.put(orderID, new OpenOrder(orderID, order, strategy));
            socket.placeOrder(orderID, contract, order);
            String msg = strategy.getName() + ": Placed order " + orderID;
            eventReport.report(msg);
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    public void placeMarketOrder(Contract contract, int quantity, String action, Strategy strategy) {
        Order order = new Order();
        order.m_action = action;
        order.m_totalQuantity = quantity;
        order.m_orderType = "MKT";
        if (advisorAccountID.length() != 0)
            order.m_account = advisorAccountID;
        placeOrder(contract, order, strategy);
    }


    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    private long getServerTime() throws JSystemTraderException {
        socket.reqCurrentTime();
        try {
            synchronized (trader) {
                while (serverTime == 0) {
                    trader.wait();
                }
            }
        } catch (InterruptedException ie) {
            throw new JSystemTraderException(ie);
        }

        return serverTime;
    }

    public void timeSyncCheck() throws JSystemTraderException {
        // Make sure that system clock and IB server clocks are in sync
        socket.reqCurrentTime();
        TimeSyncChecker.timeCheck(getServerTime());
    }

    public boolean isRealAccountDisclaimerAccepted() throws JSystemTraderException {
        boolean isAccepted = true;
        socket.reqAccountUpdates(true, advisorAccountID);

        try {
            synchronized (trader) {
                while (accountCode == null) {
                    trader.wait();
                }
            }
        } catch (InterruptedException ie) {
            throw new JSystemTraderException(ie);
        }

        socket.reqAccountUpdates(false, advisorAccountID);
        if (!accountCode.startsWith("D")) {
            String lineSep = System.getProperty("line.separator");
            String warning = "Connected to a real (not simulated) IB account. ";
            warning += "Running " + JSystemTrader.APP_NAME + " against a real" + lineSep;
            warning += "account may cause significant losses in your account. ";
            warning += "Are you sure you want to proceed?";
            int response = JOptionPane
                    .showConfirmDialog(null, warning, JSystemTrader.APP_NAME, JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                isAccepted = false;
                disconnect();
            }
        }

        return isAccepted;
    }

    public synchronized void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost,
                                             double unrealizedPNL, double realizedPNL, String accountName)
    {
        PortfolioMirrorItem portfolioMirrorItem = new PortfolioMirrorItem(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName);
        portfolioMirror.put(_generateContractKey(contract), portfolioMirrorItem);

        for(Strategy strategy : strategies.values())
        {
           if(strategy.getContract().m_symbol.equals(portfolioMirrorItem.getContract().m_symbol))
           {
               strategy.getPositionManager().update(portfolioMirrorItem);
               Dispatcher.fireModelChanged(ModelListener.Event.STRATEGY_UPDATE, strategy);
           }
        }
    }


    private String _generateContractKey(Contract contract)
    {
        if(contract==null)
            return "nocontract";
        
        StringBuilder key = new StringBuilder();
        key.append(contract.m_symbol);
//        if(contract.m_currency!=null)
//        {
//            key.append(".");
//            key.append(contract.m_currency);
//        }
//        key.append("-");
//        key.append(contract.m_exchange);
        if(contract.m_expiry!=null)
        {
            key.append("/");
            key.append(contract.m_expiry.substring(0, 5));
        }
        
        return key.toString();
    }

}
