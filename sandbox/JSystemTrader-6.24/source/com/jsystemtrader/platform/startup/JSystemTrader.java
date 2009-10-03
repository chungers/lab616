package com.jsystemtrader.platform.startup;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.util.*;
import java.io.*;

/**
 * Application starter.
 */
public class JSystemTrader {
    private static String appPath;

    public static final String APP_NAME = "JSystemTrader";

    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private JSystemTrader() throws JSystemTraderException, IOException
    {
        Dispatcher.setReporter("EventReport");
        new MainController();
    }

    /**
     * Starts JSystemTrader application.
     *
     * @param args
     */
    public static void main(String[] args) {
        new AppInstanceChecker(APP_NAME);

        try {
            if (args.length != 1) {
                throw new JSystemTraderException("Usage: JSystemTrader <JSystemTrader Directory>");
            }
            appPath = args[0];
            new JSystemTrader();
        } catch (Exception t) {
            MessageDialog.showError(null, t.toString() + "\n" + t.getMessage());
            Dispatcher.getReporter().report(t);
            t.printStackTrace();
        }
    }

    public static String getAppPath() {
        return appPath;
    }

}
