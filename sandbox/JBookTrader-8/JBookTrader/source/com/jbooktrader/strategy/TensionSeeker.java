package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class TensionSeeker extends StrategyES {

    // Technical indicators
    private Indicator tensionInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String ENTRY = "Entry";


    // Strategy parameters values
    private final int entry;

    public TensionSeeker(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        entry = getParam(ENTRY);
    }

    @Override
    public void setIndicators() {
        tensionInd = addIndicator(new Tension(getParam(FAST_PERIOD), getParam(SLOW_PERIOD)));
    }


    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 20, 1200, 1, 210);
        addParam(SLOW_PERIOD, 1000, 14000, 100, 10768);
        addParam(ENTRY, 5, 35, 1, 23);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double tension = tensionInd.getValue();
        if (tension >= entry) {
            setPosition(1);
        } else if (tension <= -entry) {
            setPosition(-1);
        }
    }
}
