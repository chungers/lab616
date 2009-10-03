package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;


/**
 * Relative Strength Index of the market depth balance
 * Specification: http://en.wikipedia.org/wiki/Relative_strength
 */
public class DepthBalanceRSI extends Indicator {
    private final double multiplier;
    private double emaUp, emaDown;
    private double previousBalance;

    public DepthBalanceRSI(int periodLength) {
        multiplier = 2.0 / (periodLength + 1.0);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        if (previousBalance != 0) {
            double change = balance - previousBalance;
            double up = (change > 0) ? change : 0;
            double down = (change < 0) ? -change : 0;
            emaUp += (up - emaUp) * multiplier;
            emaDown += (down - emaDown) * multiplier;
            double sum = emaUp + emaDown;
            value = (sum == 0) ? 50 : (100 * emaUp / sum);
        } else {
            value = 50;
        }
        previousBalance = balance;
        value -= 50;
    }

    @Override
    public void reset() {
        previousBalance = 0;
        emaUp = emaDown = 0;
    }


}

