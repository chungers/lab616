package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;

/**
 * Velocity of volume
 */
public class VolumeVelocityEMA extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fast, slow;

    public VolumeVelocityEMA(int fastPeriod, int slowPeriod) {
        super(fastPeriod, slowPeriod);
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
    }

    @Override
    public void calculate() {
        double volume = marketBook.getSnapshot().getVolume();
        fast += (volume - fast) * fastMultiplier;
        slow += (volume - slow) * slowMultiplier;

        value = 100 * (fast - slow) / (fast + slow);
    }

    @Override
    public void reset() {
        fast = slow = marketBook.getSnapshot().getVolume();
        value = 0;
    }
}
