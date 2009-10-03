package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;


/**
 * Relative Strength Index. Implemented up to this specification:
 * http://en.wikipedia.org/wiki/Relative_strength
 */
public class RSI extends Indicator {
    private final int periodLength;

    public RSI(QuoteHistory qh, int periodLength) {
        super(qh);
        this.periodLength = periodLength;
    }

    @Override
    public double calculate() {
        int qhSize = qh.size();
        int lastBar = qhSize - 1;
        int firstBar = lastBar - periodLength + 1;

        double gains = 0, losses = 0;

        for (int bar = firstBar + 1; bar <= lastBar; bar++) {
            double change = qh.getPriceBar(bar).getClose() - qh.getPriceBar(bar - 1).getClose();
            gains += Math.max(0, change);
            losses += Math.max(0, -change);
        }

        double change = gains + losses;

        value = (change == 0) ? 50 : (100 * gains / change);
        return value;
    }
}
