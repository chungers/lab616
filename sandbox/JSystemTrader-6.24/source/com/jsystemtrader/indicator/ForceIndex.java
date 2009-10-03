package com.jsystemtrader.indicator;

import com.jsystemtrader.platform.quote.*;

/**
 * Implementation reference: http://en.wikipedia.org/wiki/Force_Index
 *
 * @author Florent Guiliani
 */
public class ForceIndex extends Indicator {

    public ForceIndex(QuoteHistory qh) {
        super(qh);
    }

    @Override
    public double calculate() {
        int lastBarIndex = qh.size() - 1;
        PriceBar lastBar = qh.getPriceBar(lastBarIndex);
        PriceBar previousBar = qh.getPriceBar(lastBarIndex - 1);
        value = lastBar.getVolume() * (lastBar.getClose() - previousBar.getClose());
        return value;
    }
}

