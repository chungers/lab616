package com.jbooktrader.platform.optimizer;

public enum PerformanceMetric {
    Trades("Trades"),
    NetProfit("Net Profit"),
    MaxDD("Max DD"),
    PF("Profit Factor"),
    Kelly("Kelly"),
    PI("PI");

    private final String name;

    PerformanceMetric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PerformanceMetric getColumn(String name) {
        for (PerformanceMetric performanceMetric : values()) {
            if (performanceMetric.name.equals(name)) {
                return performanceMetric;
            }
        }
        return null;
    }
}
