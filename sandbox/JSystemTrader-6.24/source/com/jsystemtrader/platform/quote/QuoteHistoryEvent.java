package com.jsystemtrader.platform.quote;

public class QuoteHistoryEvent {
    public enum EventType {
        MARKET_CHANGE, NEW_BAR
    }

    private final EventType eventType;

    public QuoteHistoryEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getType() {
        return eventType;
    }

}
