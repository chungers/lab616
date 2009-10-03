package com.jsystemtrader.platform.util;

import java.io.*;
import java.nio.channels.*;

/**
 * Ensures that only one instance of an app is running.
 */
public class AppInstanceChecker {
    public AppInstanceChecker(String appName) {
        try {
            File file = new File(System.getProperty("user.home"), appName + ".tmp");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
            if (channel.tryLock() == null) {
                MessageDialog.showMessage(null, appName + " is already running.");
                System.exit(0);
            }
        } catch (Exception e) {
            MessageDialog.showMessage(null, e.getMessage());
        }
    }
}
