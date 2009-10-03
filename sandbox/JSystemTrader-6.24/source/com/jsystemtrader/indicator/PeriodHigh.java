package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;

/**
 * Highest high of the period.
 */
public class PeriodHigh extends Indicator {
    private final int periodLength;

    public PeriodHigh(QuoteHistory qh, int periodLength) {
        super(qh);
        this.periodLength = periodLength;
    }

    @Override
    public double calculate() {

        int periodStart = qh.size() - periodLength;
        int periodEnd = qh.size() - 1;
        double high = qh.getPriceBar(periodStart).getHigh();

        for (int bar = periodStart + 1; bar <= periodEnd; bar++) {
            double barHigh = qh.getPriceBar(bar).getHigh();
            if (barHigh > high) {
                high = barHigh;
            }
        }

        value = high;
        return value;
    }
}
