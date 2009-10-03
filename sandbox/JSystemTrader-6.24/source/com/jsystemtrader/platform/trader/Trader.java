package com.jsystemtrader.platform.trader;

import com.ib.client.*;
import com.jsystemtrader.platform.backtest.*;
import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.position.*;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import com.jsystemtrader.platform.quote.*;
import com.jsystemtrader.platform.report.*;
import com.jsystemtrader.platform.strategy.*;
import static com.jsystemtrader.platform.preferences.JSTPreferences.PortfolioSync;
import java.math.*;
import java.util.*;

/**
 * This class acts as a "wrapper" in the IB's API terminology.
 */
public class Trader extends IBWrapperAdapter {
    private final Report eventReport;
    private final TraderAssistant traderAssistant;
    private final List<ErrorListener> errorListeners = new ArrayList<ErrorListener>();
    private volatile boolean isPendingHistRequest;
    private boolean _portfolioSyncEnabled;

    /// Constructor used for downloading data hitory or doing real/paper trading.
    public Trader() throws JSystemTraderException {
        _portfolioSyncEnabled = PreferencesHolder.getInstance().getBool(PortfolioSync);
        traderAssistant = new TraderAssistant(this);
        eventReport = Dispatcher.getReporter();
        traderAssistant.connect();
        if (!traderAssistant.isRealAccountDisclaimerAccepted()) {
            throw new JSystemTraderException("You may restart TWS and login to a paper trading (simulated) account.");
        }
        traderAssistant.timeSyncCheck();

    }

    /// Constructor used for backtesting and optimizing
    public Trader(String histDataFileName) throws JSystemTraderException {
        _portfolioSyncEnabled = false;
        traderAssistant = new BackTestTraderAssistant(this, histDataFileName);
        eventReport = Dispatcher.getReporter();
    }

    public void addErrorListener(ErrorListener errorListener) {
        if (!errorListeners.contains(errorListener)) {
            errorListeners.add(errorListener);
        }
    }

    public synchronized void removeErrorListener(ErrorListener errorListener) {
        errorListeners.remove(errorListener);
    }

    private void fireError(int errorCode, String errorMsg) {
        for (ErrorListener errorListener : errorListeners) {
            errorListener.error(errorCode, errorMsg);
        }
    }

    public TraderAssistant getAssistant() {
        return traderAssistant;
    }

    public boolean getIsPendingHistRequest() {
        return isPendingHistRequest;
    }

    public void setIsPendingHistRequest(boolean isPendingHistRequest) {
        this.isPendingHistRequest = isPendingHistRequest;
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        try {
            if (key.equalsIgnoreCase("AccountCode")) {
                synchronized (this) {
                    traderAssistant.setAccountCode(value);
                    notifyAll();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        String newsBulletin = "Msg ID: " + msgId + " Msg Type: " + msgType + " Msg: " + message + " Exchange: " + origExchange;
        eventReport.report(newsBulletin);
    }


    @Override
    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
        try {
            QuoteHistory qh = traderAssistant.getStrategy(reqId).getQuoteHistory();
            if (date.startsWith("finished")) {
                qh.setIsHistRequestCompleted(true);
                String msg = qh.getStrategyName() + ": Historical data request finished. ";
                msg += "Bars returned:  " + qh.size();

                eventReport.report(msg);
                synchronized (this) {
                    isPendingHistRequest = false;
                    notifyAll();
                }
            } else {
                Strategy strategy = traderAssistant.getStrategy(reqId);
                long priceBarDate = (Long.parseLong(date) + strategy.getBarSize().toSeconds()) * 1000;
                PriceBar priceBar = new PriceBar(priceBarDate, open, high, low, close, volume);
                qh.addHistoricalPriceBar(priceBar);
                if (priceBarDate <= System.currentTimeMillis()) { //is the bar completed?
                    strategy.validateIndicators();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void execDetails(int orderId, Contract contract, Execution execution) {
        try {
            Map<Integer, OpenOrder> openOrders = traderAssistant.getOpenOrders();
            OpenOrder openOrder = openOrders.get(orderId);
            if (openOrder != null) {
                openOrder.add(execution);
                if (openOrder.isFilled()) {
                    Strategy strategy = openOrder.getStrategy();
                    PositionManager positionManager = strategy.getPositionManager();
                    positionManager.update(openOrder);
                    openOrders.remove(orderId);
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void realtimeBar(int strategyId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
        try {
            QuoteHistory qh = traderAssistant.getStrategy(strategyId).getQuoteHistory();
            qh.update(open, high, low, close, volume);
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }
    
    @Override
    public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue,
                                double averageCost, double unrealizedPNL, double realizedPNL, String accountName)
    {
        if(_portfolioSyncEnabled)
        {
            traderAssistant.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName);
        }
    }


    @Override
    public void error(Exception e) {
        eventReport.report(e.toString());
    }

    @Override
    public void error(String error) {
        eventReport.report(error);
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        boolean isOrderFilled = !(traderAssistant.getOpenOrders().containsKey(orderId));
        boolean isValidCommission = (orderState.m_commission != Double.MAX_VALUE);
        if (isOrderFilled && isValidCommission) {
            BigDecimal commission = new BigDecimal(orderState.m_commission);
            commission = commission.setScale(2, BigDecimal.ROUND_HALF_UP);
            double roundedCommission = commission.doubleValue();
            String msg = "Commission for order " + orderId + ": " + orderState.m_commissionCurrency + " " + roundedCommission;
            //TODO: in trading mode, account for commissions in P&L calcs
            eventReport.report(msg);
        }
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        try {
            String msg = errorCode + ": " + errorMsg;
            eventReport.report(msg);

            // handle errors 1101 and 1102
            boolean isConnectivityRestored = (errorCode == 1101 || errorCode == 1102);
            if (isConnectivityRestored) {
                eventReport.report("Checking for executions while TWS was disconnected from the IB server.");
                traderAssistant.requestExecutions();
            }

            fireError(errorCode, errorMsg);// send a notification to all error listeners
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void nextValidId(int orderId) {
        traderAssistant.setOrderID(orderId);
    }

    @Override
    synchronized public void currentTime(long time) {
        traderAssistant.setServerTime(time);
        notifyAll();
    }

}
