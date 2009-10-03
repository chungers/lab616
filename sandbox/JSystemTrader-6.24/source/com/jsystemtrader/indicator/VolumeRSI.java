package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;


/**
 */
public class VolumeRSI extends Indicator {
    private final int periodLength;

    public VolumeRSI(QuoteHistory qh, int periodLength) {
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
            long volume = qh.getPriceBar(bar).getVolume();

            gains += Math.max(0, change * volume);
            losses += Math.max(0, -change * volume);
        }

        double change = gains + losses;

        double rsi = (change == 0) ? 50 : (100 * gains / change);
        value = (rsi - 50) * 2;

        return value;
    }
}
