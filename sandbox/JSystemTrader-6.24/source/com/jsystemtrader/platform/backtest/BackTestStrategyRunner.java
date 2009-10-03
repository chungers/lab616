package com.jsystemtrader.platform.backtest;


import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.position.*;
import com.jsystemtrader.platform.quote.*;
import com.jsystemtrader.platform.report.*;
import com.jsystemtrader.platform.strategy.*;

import java.io.*;
import java.util.*;

/**
 * Runs a trading strategy in the backtesting mode using a data file containing
 * historical price bars. There is a one-to-one map between the strategy class
 * and the strategy runner. That is, if 5 strategies are selected to run,
 * there will be 5 instances of the StrategyRunner created.
 */
public class BackTestStrategyRunner extends Thread {
    private Strategy strategy;
    protected final Report eventReport;
    private final BackTestTraderAssistant backTestAssistant;
    private final boolean isOptimized;

    public BackTestStrategyRunner(Strategy strategy) throws IOException, JSystemTraderException {
        this.strategy = strategy;
        isOptimized = (Dispatcher.getMode() == Dispatcher.Mode.OPTIMIZATION);
        eventReport = Dispatcher.getReporter();
        backTestAssistant = (BackTestTraderAssistant) Dispatcher.getTrader().getAssistant();

        if (!isOptimized) {
            backTestAssistant.addStrategy(strategy);
            eventReport.report(strategy.getName() + ": strategy started");
            Report strategyReport = new Report(strategy.getName());
            strategyReport.report(strategy.getStrategyReportHeaders());
            strategy.setReport(strategyReport);
        }
    }

    protected void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    protected void backTest() {

        QuoteHistory qh = strategy.getQuoteHistory();
        PositionManager positionManager = strategy.getPositionManager();
        Calendar calendar = strategy.getCalendar();

        for (PriceBar priceBar : backTestAssistant.priceBars) {
            qh.addHistoricalPriceBar(priceBar);
            calendar.setTimeInMillis(priceBar.getDate());

            // recalculate and validate indicators even if we are outside of the trading interval
            strategy.validateIndicators();
            if (strategy.canTrade()) {
                if (strategy.hasValidIndicators()) {
                    strategy.onBar();
                }
            }
            positionManager.trade();

            if (!isOptimized) {
                strategy.update();
            }
        }

        // go flat at the end of the test period
        strategy.closeOpenPositions();
        positionManager.trade();
        if (!isOptimized) {
            strategy.update();
        }
    }


    @Override
    public void run() {
        try {
            backTest();
            eventReport.report(strategy.getName() + ": is now inactive.");
        } catch (Exception t) {
            /* Exceptions should never happen. If an exception of any type
             * occurs, it would indicate a serious JSystemTrader bug, and there
             * is nothing we can do to recover at runtime. Report the error for the
             * "after-run" analysis.
             */
            eventReport.report(t);
        } finally {
            Dispatcher.strategyCompleted();
        }

    }
}
