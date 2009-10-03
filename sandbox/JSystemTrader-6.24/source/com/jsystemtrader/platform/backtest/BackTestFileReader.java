package com.jsystemtrader.platform.backtest;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.quote.*;
import org.joda.time.*;
import org.joda.time.format.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Reads and validates a data file containing historical price bars. The data file
 * is used for backtesting and optimization of trading strategies.
 */
public class BackTestFileReader {
    private static final String lineSep = System.getProperty("line.separator");
    private static final String propertyFileExtension = ".props";
    private final double spread, slippage, commission;
    private final List<PriceBar> priceBars = new ArrayList<PriceBar>();
    private final Properties properties = new Properties();
    private int columns;
    private Date currentDateTime;

    public List<PriceBar> getPriceBars() {
        return priceBars;
    }

    public double getBidAskSpread() {
        return spread;
    }

    public double getSlippage() {
        return slippage;
    }

    public double getCommission() {
        return commission;
    }

    public BackTestFileReader(String fileName) throws JSystemTraderException {
        String line = null;
        int lineNumber = 0;

        try {
            File propertyFile = new File(fileName + propertyFileExtension);
            if (propertyFile.exists())
                properties.load(new FileInputStream(propertyFile));
            else
                properties.load(new FileInputStream(fileName));
        } catch (IllegalArgumentException e) {
            String msg = "Problem loading file " + fileName + ": binary format was detected. ";
            msg += e.getMessage();
            throw new JSystemTraderException(msg);
        } catch (Exception e) {
            String msg = "Problem loading file " + fileName + ": " + e.getMessage();
            throw new JSystemTraderException(msg);
        }

        try {
            spread = getPropAsDouble("bidAskSpread");
            if (spread < 0) {
                throw new JSystemTraderException("\"" + "Bid/Ask spread" + "\"" + " must be greater or equal to 0.");
            }

            slippage = getPropAsDouble("slippage");
            if (slippage < 0) {
                throw new JSystemTraderException("\"" + "Slippage" + "\"" + " must be greater or equal to 0.");
            }

            commission = getPropAsDouble("commission");
            if (commission < 0) {
                throw new JSystemTraderException("\"" + "Commission" + "\"" + " must be greater or equal to 0.");
            }

            columns = getPropAsInt("columns");
            int dateColumn = getPropAsColumn("dateColumn");
            int timeColumn = getPropAsColumn("timeColumn");
            int openColumn = getPropAsColumn("openColumn");
            int highColumn = getPropAsColumn("highColumn");
            int lowColumn = getPropAsColumn("lowColumn");
            int closeColumn = getPropAsColumn("closeColumn");

            boolean hasVolume = exists("volumeColumn");
            int volumeColumn = hasVolume ? getPropAsColumn("volumeColumn") : 0;

            String separator = getPropAsString("separator");
            String dateFormat = getPropAsString("dateFormat");
            String timeFormat = getPropAsString("timeFormat");
            String timeZone = getPropAsString("timeZone");

            TimeZone tz = TimeZone.getTimeZone(timeZone);
            if (!tz.getID().equals(timeZone)) {
                String msg = "The specified time zone " + "\"" + timeZone + "\"" + " does not exist." + lineSep;
                msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
                throw new JSystemTraderException(msg);
            }

            DateTimeZone jodaTimeZone = DateTimeZone.forID(timeZone);
            DateTimeFormatter jodaDateFormat = DateTimeFormat.forPattern(dateFormat + timeFormat);
            jodaDateFormat = jodaDateFormat.withZone(jodaTimeZone);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat + timeFormat);
            simpleDateFormat.setTimeZone(tz);

            // Enforce strict interpretation of date and time formats
            simpleDateFormat.setLenient(false);

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            Dispatcher.getReporter().report("Loading historical data file");

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                boolean isComment = line.startsWith("#");
                boolean isProperty = line.contains("=");
                boolean isBlankLine = (line.trim().length() == 0);

                if (isComment || isProperty || isBlankLine) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line, separator);

                int tokenCount = st.countTokens();
                if (tokenCount != columns) {
                    String msg = "The descriptor defined " + columns + " columns, ";
                    msg += "but line #" + lineNumber + " contains " + tokenCount + " columns";
                    throw new JSystemTraderException(msg);
                }

                int tokenNumber = 0;
                Map<Integer, String> tokens = new HashMap<Integer, String>();
                while (st.hasMoreTokens()) {
                    tokenNumber++;
                    tokens.put(tokenNumber, st.nextToken());
                }

                String dateToken = tokens.get(dateColumn);
                String timeToken = tokens.get(timeColumn);

                DateTime jodaDate = jodaDateFormat.parseDateTime(dateToken + timeToken);
                Date dateTime = new Date(jodaDate.getMillis());
                long date = dateTime.getTime();
                double open = Double.parseDouble(tokens.get(openColumn));
                double high = Double.parseDouble(tokens.get(highColumn));
                double low = Double.parseDouble(tokens.get(lowColumn));
                double close = Double.parseDouble(tokens.get(closeColumn));
                int volume = hasVolume ? Integer.parseInt(tokens.get(volumeColumn)) : 0;

                if (currentDateTime != null) {
                    if (dateTime.before(currentDateTime)) {
                        String msg = "Date-time of this bar is before the date-time of the previous bar.";
                        throw new JSystemTraderException(msg);
                    }
                }

                boolean isInvalidPriceBar = (open <= 0) || (high <= 0) || (low <= 0) || (close <= 0);

                if (isInvalidPriceBar) {
                    throw new JSystemTraderException("Open, High, Low, and Close must be greater than zero.");
                }

                if (low > high) {
                    throw new JSystemTraderException("Low must be less or equal to high.");
                }

                if (close < low || close > high) {
                    throw new JSystemTraderException("Close must be between low and high.");
                }

                if (open < low || open > high) {
                    throw new JSystemTraderException("Open must be between low and high.");
                }

                PriceBar priceBar = new PriceBar(date, open, high, low, close, volume);
                priceBars.add(priceBar);
                currentDateTime = dateTime;

            }
            reader.close();

        } catch (Exception e) {
            String msg = "";
            if (lineNumber > 0) {
                msg = "Problem parsing line #" + lineNumber + ": " + line + lineSep;
            }

            String description = e.getMessage();
            if (description == null) {
                description = e.toString();
            }
            msg += description;
            throw new JSystemTraderException(msg);
        }
    }

    private String getPropAsString(String property) throws JSystemTraderException {
        String propValue = (String) properties.get(property);
        if (propValue == null) {
            String msg = "Property \"" + property + "\" is not defined in the historical data file.";
            throw new JSystemTraderException(msg);
        }

        return propValue;
    }


    private int getPropAsInt(String property) throws JSystemTraderException {
        String propValue = getPropAsString(property);
        int value;
        try {
            value = Integer.parseInt(propValue);
        } catch (NumberFormatException nfe) {
            String msg = "Value " + propValue + " of property " + property + " is not an integer.";
            throw new JSystemTraderException(msg);
        }

        return value;
    }

    private double getPropAsDouble(String property) throws JSystemTraderException {
        String propValue = getPropAsString(property);
        double value;
        try {
            value = Double.parseDouble(propValue);
        } catch (NumberFormatException nfe) {
            String msg = "Value " + propValue + " of property " + property + " is not a number.";
            throw new JSystemTraderException(msg);
        }

        return value;
    }


    private int getPropAsColumn(String property) throws JSystemTraderException {
        int column = getPropAsInt(property);
        if (column > columns) {
            String msg = "Total number of columns is " + columns + ", ";
            msg += "but property \"" + property + "\" specifies column " + column;
            throw new JSystemTraderException(msg);
        }

        return column;
    }

    private boolean exists(String property) {
        String propValue = (String) properties.get(property);
        return propValue != null;
    }

}
