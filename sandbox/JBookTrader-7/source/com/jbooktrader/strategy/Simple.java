package com.jbooktrader.strategy;

import com.jbooktrader.indicator.depth.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Simple extends StrategyES {

    // Technical indicators
    private final Indicator depthBalanceEMAInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";
    private final int entry;


    public Simple(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        entry = getParam(ENTRY);
        depthBalanceEMAInd = new DepthBalanceEMA(getParam(PERIOD));
        addIndicator(depthBalanceEMAInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 250, 2500, 1, 1435);
        addParam(ENTRY, 5, 15, 1, 9);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double depthBalanceEMA = depthBalanceEMAInd.getValue();
        if (depthBalanceEMA >= entry) {
            setPosition(1);
        } else if (depthBalanceEMA <= -entry) {
            setPosition(-1);
        }
    }
}
