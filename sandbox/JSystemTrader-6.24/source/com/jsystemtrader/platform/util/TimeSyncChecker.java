package com.jsystemtrader.platform.util;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import static com.jsystemtrader.platform.preferences.JSTPreferences.TimeLagAllowed;
import com.jsystemtrader.platform.startup.*;

import java.util.*;

/**
 * Utility class to ensure time synchronization between the machine where
 * JSystemTrader is running and the Interactive Brokers' server(s).
 * <p/>
 * It's recommended that a time sync service be running at all times.
 */
public class TimeSyncChecker {
    private static final String lineSep = System.getProperty("line.separator");

    /**
     * Makes sure that the clock on the machine where JSystemTrader is running
     * is in sync with the Interactive Brokers server.
     *
     * @param serverTime long Time as reported by IB server
     *
     * @throws JSystemTraderException If the difference between the two clocks is greater than the tolerance
     */
    public static void timeCheck(long serverTime) throws JSystemTraderException {
        long timeNow = System.currentTimeMillis();
        // Difference in seconds between IB server time and local machine's time
        long difference = (timeNow - serverTime) / 1000;

        int tolerance = Integer.parseInt(PreferencesHolder.getInstance().get(TimeLagAllowed));

        if (Math.abs(difference) > tolerance) {
            String msg = "This computer's clock is out of sync with the IB server clock." + lineSep;

            msg += lineSep + "IB Server Time: " + new Date(serverTime);
            msg += lineSep + "Computer Time: " + new Date(timeNow);
            msg += lineSep + "Rounded Difference: " + difference + " seconds";
            msg += lineSep + "Tolerance: " + tolerance + " seconds";
            msg += lineSep + lineSep;
            msg += "Set the machine's clock to the correct time, and restart " + JSystemTrader.APP_NAME + "." + lineSep;
            msg += "A time synchronization service or client is recommended.";

            throw new JSystemTraderException(msg);
        }
    }
}
