package com.jsystemtrader.platform.quote;

import com.opentick.*;

import java.text.*;
import java.util.*;

/**
 * Encapsulates the price bar information.
 */
public class PriceBar {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yy zzz");

    private long date;
    private double open, high, low, close;
    private long volume;

    /**
     * This constructor is used to create a new historical bar
     */
    public PriceBar(long date, double open, double high, double low, double close, long volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    /**
     * This constructor is used to create a new historical bar when OpenTick is the data source
     */
    public PriceBar(OTOHLC OTBar) {
        date = OTBar.getTimestamp() * 1000L;
        open = OTBar.getOpenPrice();
        high = OTBar.getHighPrice();
        low = OTBar.getLowPrice();
        close = OTBar.getClosePrice();
        volume = OTBar.getVolume();
    }


    /**
     * This constructor used to create a new real time bar
     */
    public PriceBar(double open, double high, double low, double close, long volume) {
        this(0, open, high, low, close, volume);
    }

    /**
     * This constructor used to create a new real time bar whose OHLC values
     * are the same as the last completed bar.
     */
    public PriceBar(double price) {
        this(0, price, price, price, price, 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" date: ").append(getShortDate());
        sb.append(" open: ").append(open);
        sb.append(" high: ").append(high);
        sb.append(" low: ").append(low);
        sb.append(" close: ").append(close);
        sb.append(" volume: ").append(volume);

        return sb.toString();
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public double getMidpoint() {
        return (low + high) / 2;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public long getVolume() {
        return volume;
    }

    public long getDate() {
        return date;
    }

    public String getShortDate() {
        synchronized (dateFormat) {
            return dateFormat.format(new Date(date));
        }
    }
}
