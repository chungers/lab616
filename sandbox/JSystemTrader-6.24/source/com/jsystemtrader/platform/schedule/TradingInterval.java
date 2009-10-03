package com.jsystemtrader.platform.schedule;

import com.jsystemtrader.platform.model.*;

import java.text.*;
import java.util.*;

/**
 * TradingInterval defines the time period during which a strategy can trade.
 * A trading interval is defined by the interval start and the interval end.
 * Trading can start after the "open" time. Open positions will be closed
 * at the "end" time, unless "allowCarry" is set to true. The "start"
 * and "end" times must be specified in the military time format within
 * the specified time zone.
 * <p/>
 * Example: suppose the strategy defines the following trading interval:
 * setTradingInterval("9:35", "15:45", "America/New_York", false);
 * Then the following trading timeline is formed:
 * -- start trading at 9:35
 * -- close open positions at 15:45 (do not allow position carry)
 * <p/>
 * You can exclude a time period by setting a start time older than the end time
 * Example: suppose the strategy defines the following trading interval:
 * setTradingInterval("15:45", "15:15", "America/New_York", false);
 * Then the following trading timeline is formed:
 * -- start trading now
 * -- close open positions at 15:15 (do not allow position carry)
 * -- restart trading at 15:45
 */
public class TradingInterval {
    private static final String lineSep = System.getProperty("line.separator");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private final Calendar start, end;
    private final int startMinutes, endMinutes;
    private final boolean allowCarry;
    private final TimeZone tz;
    private final boolean exclusionMode;

    public TradingInterval(String startTime, String endTime, String timeZone, boolean allowCarry) throws JSystemTraderException {

        tz = TimeZone.getTimeZone(timeZone);
        if (!tz.getID().equals(timeZone)) {
            String msg = "The specified time zone " + "\"" + timeZone + "\"" + " does not exist." + lineSep;
            msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
            throw new JSystemTraderException(msg);
        }

        start = getTime(startTime);
        end = getTime(endTime);
        startMinutes = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE);
        endMinutes = end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE);
        this.allowCarry = allowCarry;

        if (!end.after(start)) {
            exclusionMode = true;
        } else {
            exclusionMode = false;
        }
    }

    public TradingInterval() throws JSystemTraderException {
        this("0:00", "0:00", "America/New_York", false);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[start: ").append(dateFormat.format(start.getTime()));
        sb.append(" end: ").append(dateFormat.format(end.getTime()));
        return sb.toString();
    }


    private Calendar getTime(String time) throws JSystemTraderException {
        int hours, minutes;
        StringTokenizer st = new StringTokenizer(time, ":");
        int tokens = st.countTokens();
        if (tokens != 2) {
            String msg = "Time " + time + " does not conform to the HH:MM format.";
            throw new JSystemTraderException(msg);
        }

        String hourToken = st.nextToken();
        try {
            hours = Integer.parseInt(hourToken);
        } catch (NumberFormatException nfe) {
            String msg = hourToken + " in " + time + " can not be parsed as hours.";
            throw new JSystemTraderException(msg);
        }

        String minuteToken = st.nextToken();
        try {
            minutes = Integer.parseInt(minuteToken);
        } catch (NumberFormatException nfe) {
            String msg = minuteToken + " in " + time + " can not be parsed as minutes.";
            throw new JSystemTraderException(msg);
        }

        if (hours < 0 || hours > 23) {
            String msg = "Specified hours: " + hours + ". Number of hours must be in the [0..23] range.";
            throw new JSystemTraderException(msg);
        }

        if (minutes < 0 || minutes > 59) {
            String msg = "Specified minutes: " + minutes + ". Number of minutes must be in the [0..59] range.";
            throw new JSystemTraderException(msg);
        }

        Calendar period = Calendar.getInstance(tz);
        period.set(Calendar.HOUR_OF_DAY, hours);
        period.set(Calendar.MINUTE, minutes);
        // set seconds explicitly, otherwise they will be carried from the current time
        period.set(Calendar.SECOND, 0);

        return period;
    }

    public Calendar getStart() {
        return start;
    }

    public Calendar getEnd() {
        return end;
    }

    public int getStartMinutes() {
        return startMinutes;
    }

    public int getEndMinutes() {
        return endMinutes;
    }

    public boolean getAllowCarry() {
        return allowCarry;
    }

    public TimeZone getTimeZone() {
        return tz;
    }


    public boolean getExclusionMode() {
        return exclusionMode;
    }

}
