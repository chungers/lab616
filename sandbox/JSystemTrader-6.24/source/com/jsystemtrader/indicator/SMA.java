package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;

/**
 * Simple Moving Average.
 */
public class SMA extends Indicator {
    private final int length;

    public SMA(QuoteHistory qh, int length) {
        super(qh);
        this.length = length;
    }

    @Override
    public double calculate() {
        int endBar = qh.size() - 1;
        int startBar = endBar - length;
        double sma = 0;

        for (int bar = startBar; bar <= endBar; bar++) {
            sma += qh.getPriceBar(bar).getClose();
        }

        value = sma / (endBar - startBar + 1);
        return value;
    }
}
