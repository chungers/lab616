package com.jsystemtrader.platform.model;

public class JSystemTraderException extends Exception {
    public JSystemTraderException(String message) {
        super(message);
    }

    public JSystemTraderException(Exception e) {
        super(e);
    }

    public JSystemTraderException(String message, Throwable cause) {
        super(message, cause);
    }
}
