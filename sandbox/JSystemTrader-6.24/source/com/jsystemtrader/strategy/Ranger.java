package com.jsystemtrader.strategy;

import com.ib.client.*;
import com.jsystemtrader.indicator.*;
import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.optimizer.*;
import com.jsystemtrader.platform.quote.*;
import com.jsystemtrader.platform.schedule.*;
import com.jsystemtrader.platform.strategy.*;
import com.jsystemtrader.platform.util.*;

/**
 * This sample strategy trades the S&P E-Mini futures contract using a combination
 * of two indicators. One measures a shorter term noise-adjusted trend, and the other
 * one measures a longer term noise-adjusted trend.
 */
public class Ranger extends Strategy {

    // Technical indicators
    private final Indicator rsiInd;

    // Strategy parameters names
    private static final String PERIOD_LENGTH = "Period length";
    private static final String LOW = "low";
    private static final String HIGH = "high";

    // Strategy parameters values
    private final int periodLength;
    private final double high, low;

    //private final double deviations;
    //private final double bandwidth;
    //private final double fastTrendStrength, slowTrendStrength, exit;


    public Ranger(StrategyParams params) throws JSystemTraderException {
        // Define the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        setStrategy(contract, BarSize.Min5, false);

        // Initialize strategy parameter values. If the strategy is running in the optimization
        // mode, the parameter values will be taken from the "params" object. Otherwise, the
        // "params" object will be empty and the parameter values will be initialized to the
        // specified default values.
        periodLength = (int) params.get(PERIOD_LENGTH, 14);
        low = (int) params.get(LOW, 30);
        high = (int) params.get(HIGH, 70);
        //deviations = params.get(DEVIATIONS, 2);
        // bandwidth  = params.get(BANDWIDTH, 5);
        //fastTrendStrength = params.get(FAST_TREND_STRENGTH, 35);
        // slowTrendStrength = params.get(SLOW_TREND_STRENGTH, 89);
        //exit = params.get(EXIT, 95);

        // Instantiate technical indicators
        // bollingerUpperInd = new BollingerUpper(quoteHistory, periodLength, deviations);
        // bollingerLowerInd = new BollingerLower(quoteHistory, periodLength, deviations);
        rsiInd = new RSI(quoteHistory, periodLength);
        //bollingerMiddleInd = new BollingerMiddle(quoteHistory, periodLength);

        // Specify the title and the chart number for each indicator
        // "0" = the same chart as the price chart; "1+" = separate subchart (below the price chart)
        //addIndicator("Bollinger Upper", bollingerUpperInd, 0);
        //addIndicator("Bollinger Lower", bollingerLowerInd, 0);
        addIndicator("RSI", rsiInd, 1);
        //addIndicator("Bollinger Middle", bollingerMiddleInd, 0);

    }

    /**
     * Returns min/max/step values for each strategy parameter. This method is
     * invoked by the strategy optimizer to obtain the strategy parameter ranges.
     */
    @Override
    public StrategyParams initParams() {
        StrategyParams params = new StrategyParams();
        params.add(PERIOD_LENGTH, 2, 100, 1);
        params.add(LOW, 0, 100, 10);
        params.add(HIGH, 0, 100, 10);
        return params;
    }

    /**
     * Define the trading interval and the time zone for that interval
     */
    @Override
    public TradingInterval initTradingInterval() throws JSystemTraderException {
        return new TradingInterval("10:00", "15:40", "America/New_York", true);
    }

    /**
     * This method is invoked by the framework when a new bar is completed and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBar() {
        //double bollingerUpper = bollingerUpperInd.getValue();
        //double bollingerLower = bollingerLowerInd.getValue();
        double rsi = rsiInd.getValue();

        //double last = this.getLastPriceBar().getClose();

        //double diff = fastTrend - slowTrend;

        //int currentPosition = getPositionManager().getPosition();
        //boolean target = (currentPosition > 0 && last > bollingerMiddle);
        //target = target || (currentPosition < 0 && last < bollingerMiddle);

        // boolean inTrend = rsi > 0.7 || rsi < 0.3;

        //if (target) {
        //position = 0;
        //} else {
        //double delta = bollingerUpper - bollingerLower;
        //if (delta > bandwidth) {
        if (rsi < low) {
            position = -1;
        } else if (rsi > high) {
            position = 1;
        }
        //}
        //}
    }
}
