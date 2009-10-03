package com.jsystemtrader.platform.util;

import javax.swing.table.*;
import java.text.*;


public class DoubleRenderer extends DefaultTableCellRenderer {
    private final DecimalFormat df;
    private static final int SCALE = 4;
    private static int SCALE_10;

    {
        int temp = 10;
        for (int i = 0; i < SCALE; i++) {
            temp = temp * 10;
        }
        SCALE_10 = temp;
    }

    public DoubleRenderer() {
        df = (DecimalFormat) NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(SCALE);
        df.setGroupingUsed(false);
    }

    @Override
    public void setValue(Object value) {
        String text = "";
        if (value != null) {
            if (value.getClass() == Double.class) {
                if (!Double.isInfinite((Double) value) && !Double.isNaN((Double) value)) {
                    double temp = ((Double)value).doubleValue() * SCALE_10;

                    temp = Math.floor(temp) / SCALE_10;

                    text = df.format(temp);
                }
                else {
                    text = "N/A";
                }
            } else if (value.getClass() == Integer.class) {
                text = value.toString();
            } else if (value.getClass() == String.class) {
                text = value.toString();
            } else {
                throw new RuntimeException("Could not convert " + value.getClass() + " to a number");
            }
        }

        setHorizontalAlignment(RIGHT);
        setText(text+" ");
    }
}
