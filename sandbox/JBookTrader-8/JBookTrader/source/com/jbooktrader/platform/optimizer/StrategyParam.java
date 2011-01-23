package com.jbooktrader.platform.optimizer;

/**
 */
public class StrategyParam {
    private int min, max;
    private int value, step;
    private final String name;

    public StrategyParam(String name, int min, int max, int step, int value) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
    }

    // copy constructor
    public StrategyParam(StrategyParam param) {
        this(param.name, param.min, param.max, param.step, param.value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{").append(name).append(":");
        sb.append(min).append("-");
        sb.append(max).append("-");
        sb.append(step).append("-");
        sb.append(value).append("}");

        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
