package com.jsystemtrader.platform.position;

/**
 * Encapsulates the strategy position information
 */
public class Position {
    private final int position;
    private final long date;
    private final double avgFillPrice;
    private final double orderAvgFillPrice;

    public Position(long date, int position, double avgFillPrice, double orderAvgFillPrice) {
        this.date = date;
        this.position = position;
        this.avgFillPrice = avgFillPrice;
        this.orderAvgFillPrice = orderAvgFillPrice;
    }

    public int getPosition() {
        return position;
    }

    public long getDate() {
        return date;
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }
    
    public double getOrderAvgFillPrice() {
        return orderAvgFillPrice;
    }
}
