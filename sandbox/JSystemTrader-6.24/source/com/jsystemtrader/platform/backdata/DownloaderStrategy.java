package com.jsystemtrader.platform.backdata;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.optimizer.*;
import com.jsystemtrader.platform.schedule.*;
import com.jsystemtrader.platform.strategy.*;

/**
 * This is simply a fake strategy. Its only purpose is to pretend to be a real one so that
 * it can be passed through the framework.
 */
public class DownloaderStrategy extends Strategy {
    @Override
    public void onBar() {
        // nothing to do
    }

    @Override
    public StrategyParams initParams() {
        return new StrategyParams();
    }

    @Override
    public TradingInterval initTradingInterval() throws JSystemTraderException {
        return new TradingInterval();
    }

}
