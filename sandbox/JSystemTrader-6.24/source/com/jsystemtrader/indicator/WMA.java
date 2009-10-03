package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;

/**
 * Weighted Moving Average.
 */
public class WMA extends Indicator {
    private final int length;

    public WMA(QuoteHistory qh, int length) {
        super(qh);
        this.length = length;
    }

    @Override
    public double calculate() {
        int endBar = qh.size() - 1;
        int startBar = endBar - length;
        double wma = 0;

        for (int bar = startBar; bar <= endBar; bar++) {
            wma += qh.getPriceBar(bar).getClose() * (bar+1);
        }

        value = wma / (length * (length +1) /2);
        return value;
    }
}
