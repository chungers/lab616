package com.jsystemtrader.platform.model;

public interface ModelListener {
    enum Event {
        STRATEGIES_START, STRATEGIES_END, STRATEGY_UPDATE
    }

    void modelChanged(Event event, Object value);
}
