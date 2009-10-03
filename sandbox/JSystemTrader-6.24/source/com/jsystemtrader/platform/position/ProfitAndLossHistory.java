package com.jsystemtrader.platform.position;

import java.util.*;

/**
 * Holds P&L history.
 */
public class ProfitAndLossHistory {
    private final List<ProfitAndLoss> history;

    public ProfitAndLossHistory() {
        history = new ArrayList<ProfitAndLoss>();
    }

    public void clear() {
        history.clear();
    }

    /**
     * Synchronized so that the strategy performance chart can be accessed while
     * the priceBars are still being added to the priceBar history.
     */
    public synchronized void add(ProfitAndLoss profitAndLoss) {
        history.add(profitAndLoss);
    }

    public List<ProfitAndLoss> getHistory() {
        return history;
    }

}
