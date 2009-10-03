package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;

/**
 * Middle Bollinger Band
 */
public class BollingerMiddle extends Indicator {
    private final int length;

    public BollingerMiddle(QuoteHistory qh, int length) {
        super(qh);
        this.length = length;
    }

    @Override
    public double calculate() {
        int lastBar = qh.size() - 1;
        int firstBar = lastBar - length + 1;

        double sum = 0;
        for (int bar = firstBar; bar <= lastBar; bar++) {
            sum += qh.getPriceBar(bar).getClose();
        }

        value = sum / length;
        return value;
    }
}
