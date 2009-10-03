package com.jsystemtrader.platform.position;

import java.text.*;
import java.util.*;

/**
 * Encapsulates P&L information.
 */
public class ProfitAndLoss {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yy");

    private final long date;
    private final double value;

    public ProfitAndLoss(long date, double value) {
        this.date = date;
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" date: ").append(getShortDate());
        sb.append(" value: ").append(value);

        return sb.toString();
    }

    public double getValue() {
        return value;
    }

    public long getDate() {
        return date;
    }

    private String getShortDate() {
        return dateFormat.format(new Date(date));
    }
}
