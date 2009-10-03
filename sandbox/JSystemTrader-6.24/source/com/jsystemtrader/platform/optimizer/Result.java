package com.jsystemtrader.platform.optimizer;

/**
 * Optimization results table model.
 */
public class Result {
    private final double totalProfit, maxDrawdown, profitFactor,kelly;
    private final int trades;
    private final String tradeDistribution;
    private final StrategyParams params;

    public Result(StrategyParams params,
                  double totalProfit,
                  double maxDrawdown,
                  int trades,
                  double profitFactor,
                  double kelly,
                  String tradeDistribution) {
        this.params = params;
        this.totalProfit = totalProfit;
        this.maxDrawdown = maxDrawdown;
        this.trades = trades;
        this.profitFactor = profitFactor;
        this.kelly = kelly;
        this.tradeDistribution = tradeDistribution;
    }

    public StrategyParams getParams() {
        return params;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public int getTrades() {
        return trades;
    }

    public double getProfitFactor() {
        return profitFactor;
    }
    public double getKelly() {
        return kelly;
    }

    public String getTradeDistribution() {
        return tradeDistribution;
    }
}
