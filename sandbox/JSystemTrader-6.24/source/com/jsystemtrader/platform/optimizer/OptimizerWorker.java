package com.jsystemtrader.platform.optimizer;

import com.jsystemtrader.platform.backtest.*;
import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.position.*;
import com.jsystemtrader.platform.strategy.*;
import com.jsystemtrader.platform.util.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 */
public class OptimizerWorker extends BackTestStrategyRunner {
    private final List<Result> results;
    private final int minTrades;
    private final CountDownLatch remainingTasks;
    private final Constructor<?> strategyConstructor;
    private final LinkedList<StrategyParams> tasks;

    public OptimizerWorker(Constructor<?> strategyConstructor, LinkedList<StrategyParams> tasks, List<Result> results, int minTrades, CountDownLatch remainingTasks) throws IOException, JSystemTraderException {
        super(null);
        this.results = results;
        this.minTrades = minTrades;
        this.remainingTasks = remainingTasks;
        this.strategyConstructor = strategyConstructor;
        this.tasks = tasks;
    }

    @Override
    public void run() {
        StrategyParams params;

        try {
            while (true) {
                synchronized (tasks) {
                    if (tasks.isEmpty())
                        break;
                    params = tasks.removeFirst();
                }

                Strategy strategy = (Strategy) strategyConstructor.newInstance(params);
                setStrategy(strategy);
                backTest();

                PositionManager positionManager = strategy.getPositionManager();
                int trades = positionManager.getTrades();

                if (trades >= minTrades) {
                    double totalPL = positionManager.getTotalProfitAndLoss();
                    double profitFactor = positionManager.getProfitFactor();
                    double maxDrawdown = positionManager.getMaxDrawdown();
                    double kelly = positionManager.getKelly();
                    String tradeDistribution = strategy.getTradeDistribution();

                    Result result = new Result(params, totalPL, maxDrawdown, trades, profitFactor,kelly,tradeDistribution);
                    synchronized (results) {
                        results.add(result);
                    }
                }

                synchronized (remainingTasks) {
                    remainingTasks.countDown();
                }

            }
        } catch (Exception t) {
            eventReport.report(t);
            String msg = "Encountered unexpected error while running strategy optimizer: " + t.getMessage();
            MessageDialog.showError(null, msg);
        }
    }
}
