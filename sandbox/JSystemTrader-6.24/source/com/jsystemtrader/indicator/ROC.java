package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;

/**
 * Rate of Price Change.
 */
public class ROC extends Indicator {
    private final int lookBackLength;

    public ROC(QuoteHistory qh, int lookBackLength) {
        super(qh);
        this.lookBackLength = lookBackLength;
    }

    @Override
    public double calculate() {
        int thenBar = qh.size() - 1 - lookBackLength;

        double priceNow = qh.getLastPriceBar().getClose();
        double priceThen = qh.getPriceBar(thenBar).getClose();

        value = priceNow - priceThen;
        return value;
    }
}
