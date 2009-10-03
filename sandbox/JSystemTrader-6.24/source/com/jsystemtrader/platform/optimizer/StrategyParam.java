package com.jsystemtrader.platform.optimizer;

/**
 */
public class StrategyParam {
    private final double min, max, step;
    private double value;
    private final String name;

    private StrategyParam(String name, double min, double max, double step, double value) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
    }

    public StrategyParam(String name, double min, double max, double step) {
        this(name, min, max, step, 0);
    }

    // copy constructor
    public StrategyParam(StrategyParam param) {
        this(param.name, param.min, param.max, param.step, param.value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Name: ").append(name);
        sb.append(" Min: ").append(min);
        sb.append(" Max: ").append(max);
        sb.append(" Step: ").append(step);

        return sb.toString();
    }

    public long iterations() {
        long iterations = (long) ((max - min) / step) + 1;
        return iterations;
    }


    public String getName() {
        return name;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
