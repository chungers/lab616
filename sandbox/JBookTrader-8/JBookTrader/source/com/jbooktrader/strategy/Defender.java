package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Defender extends StrategyES {

    // Technical indicators
    private Indicator tensionInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "Fast Period";
    private static final String SLOW_PERIOD = "Slow Period";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";


    // Strategy parameters values
    private final int entry, exit;

    public Defender(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        entry = getParam(ENTRY);
        exit = getParam(EXIT);
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
        addParam(FAST_PERIOD, 150, 450, 1, 358);
        addParam(SLOW_PERIOD, 4500, 9000, 100, 7643);
        addParam(ENTRY, 18, 25, 1, 21);
        addParam(EXIT, 6, 12, 1, 9);
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
        } else if (Math.abs(tension) <= exit) {
            setPosition(0);
        }
    }
}
