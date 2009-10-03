package com.jsystemtrader.platform.schedule;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.strategy.*;

import java.util.*;

public class TradingSchedule {
    private TradingInterval tradingInterval;
    private final Strategy strategy;
    private TimeZone timeZone;
    private Calendar tradingCalendar;

    public TradingSchedule(Strategy strategy) {
        this.strategy = strategy;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Calendar getCalendar() {
        tradingCalendar.setTimeInMillis(System.currentTimeMillis());
        return tradingCalendar;
    }

    public void setTradingInterval(TradingInterval tradingInterval) throws JSystemTraderException {
        this.tradingInterval = tradingInterval;
        this.timeZone = tradingInterval.getTimeZone();
        tradingCalendar = Calendar.getInstance();
        tradingCalendar.setTimeZone(this.timeZone);
    }


    private boolean inInterval() {
        Calendar now = strategy.getCalendar();
        int nowMunutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int startMunutes = tradingInterval.getStartMinutes();
        int endMunutes = tradingInterval.getEndMinutes();

        if (tradingInterval.getExclusionMode())
            return nowMunutes > startMunutes || nowMunutes < endMunutes;

        return nowMunutes >= startMunutes && nowMunutes < endMunutes;
    }

    public boolean isTimeToTrade() {
        return inInterval();
    }

    public boolean isTimeToClose() {
        return !tradingInterval.getAllowCarry() && !inInterval();
    }

    public void waitForTradingInterval() throws InterruptedException {
        long timeToSleepMillis = 5 * 1000;// 5 seconds
        while (!isTimeToTrade()) {
            Thread.sleep(timeToSleepMillis);
        }
    }
}
