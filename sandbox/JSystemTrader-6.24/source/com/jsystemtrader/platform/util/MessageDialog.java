package com.jsystemtrader.platform.util;

import com.jsystemtrader.platform.startup.*;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class to display message and error dialogs.
 */
public class MessageDialog {

    public static void showMessage(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, JSystemTrader.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, JSystemTrader.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

}
