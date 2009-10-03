package com.jsystemtrader.strategy;

import com.ib.client.*;
import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.optimizer.*;
import com.jsystemtrader.platform.quote.*;
import com.jsystemtrader.platform.schedule.*;
import com.jsystemtrader.platform.strategy.*;
import com.jsystemtrader.platform.util.*;

/**
 * This is a strategy whose only purpose is to test JST components under load.
 * It makes a trade every minute.
 */
public class LoadTest extends Strategy {

    public LoadTest(StrategyParams params) throws JSystemTraderException {
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        setStrategy(contract, BarSize.Min1, false);
    }

    @Override
    public StrategyParams initParams() {
        return new StrategyParams();
    }

    @Override
    public TradingInterval initTradingInterval() throws JSystemTraderException {
        return new TradingInterval("00:16", "23:44", "America/New_York", false);
    }

    @Override
    public void onBar() {
        if (position == 0) {
            position = 1;
        } else {
            position = -position;
        }
    }
}
