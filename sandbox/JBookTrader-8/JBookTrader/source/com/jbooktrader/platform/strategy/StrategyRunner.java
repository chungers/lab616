package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.*;

import java.util.*;
import java.util.concurrent.*;

public class StrategyRunner {
    private final Collection<Strategy> strategies;
    private final TraderAssistant traderAssistant;
    private final NTPClock ntpClock;
    private Collection<MarketBook> marketBooks;
    private final Dispatcher dispatcher;
    private static StrategyRunner instance;

    class SnapshotHandler implements Runnable {
        public void run() {
            try {
                long ntpTime = ntpClock.getTime();
                long delay = 1000 - ntpTime % 1000;
                Thread.sleep(delay);
                long snapshotTime = ntpTime + delay;

                if (marketBooks != null) {
                    for (MarketBook marketBook : marketBooks) {
                        MarketSnapshot snapshot = marketBook.getNextMarketSnapshot(snapshotTime);
                        if (snapshot != null) {
                            marketBook.setSnapshot(snapshot);
                            marketBook.saveSnapshot(snapshot);
                        }
                    }

                    synchronized (strategies) {
                        for (Strategy strategy : strategies) {
                            strategy.getIndicatorManager().updateIndicators();
                            strategy.process();
                        }
                    }

                    dispatcher.fireModelChanged(Event.TimeUpdate, snapshotTime);
                }
            } catch (Throwable t) {
                dispatcher.getEventReport().report(t);
            }
        }
    }

    public static synchronized StrategyRunner getInstance() {
        if (instance == null) {
            instance = new StrategyRunner();
        }
        return instance;
    }

    private StrategyRunner() {
        dispatcher = Dispatcher.getInstance();
        ntpClock = dispatcher.getNTPClock();
        traderAssistant = dispatcher.getTrader().getAssistant();
        strategies = new LinkedList<Strategy>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new SnapshotHandler(), 0, 500, TimeUnit.MILLISECONDS);
    }

    public void addListener(Strategy strategy) {
        synchronized (strategies) {
            strategies.add(strategy);
            marketBooks = traderAssistant.getAllMarketBooks().values();
        }
    }

}

