package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;


/**
 */
public class SmoothedVRSI extends Indicator {
    private final int lookBackPeriod;
    private final double multiplier;

    public SmoothedVRSI(QuoteHistory qh, int lookBackPeriod, int smoothingPeriod) {
        super(qh);
        this.lookBackPeriod = lookBackPeriod;
        multiplier = 2. / (smoothingPeriod + 1.);
    }

    @Override
    public double calculate() {
        int lastBar = qh.size() - 1;
        int firstBar = lastBar - lookBackPeriod + 1;

        double gains = 0, losses = 0;

        for (int bar = firstBar + 1; bar <= lastBar; bar++) {
            double change = qh.getPriceBar(bar).getClose() - qh.getPriceBar(bar - 1).getClose();
            double volume = qh.getPriceBar(bar).getVolume();

            gains += (Math.max(0, change * volume) - gains) * multiplier;
            losses += (Math.max(0, -change * volume) - losses) * multiplier;
        }

        double change = gains + losses;

        double rsi = (change == 0) ? 50 : (100 * gains / change);
        value = (rsi - 50);

        return value;
    }
}
