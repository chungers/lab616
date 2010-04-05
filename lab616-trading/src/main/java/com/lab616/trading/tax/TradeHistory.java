// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.trading.tax;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lab616.common.Pair;
import com.lab616.trading.trades.proto.Trades.OrderType;
import com.lab616.trading.trades.proto.Trades.Trade;

/**
 * Container class for a set of trades used to compute Schedule-D capital gains/losses.
 * 
 * @author david
 *
 */
public class TradeHistory {

  public static abstract class Visitor<T, V> {
    private V value;
    protected Visitor(V initial) {
      value = initial;
    }
    public abstract void visit(T t);
    protected void set(V v) {
      value = v;
    }
    public V get() {
      return value;
    }
  }
  
  // All trades loaded.
  private List<Trade> history = Lists.newArrayList();
  
  // Key = security name (e.g. 'FAS')
  private Map<String, List<Trade>> allTrades = Maps.newHashMap();
  private Map<OrderType, AtomicInteger> counts = Maps.newHashMap();
  
  public TradeHistory() {
    counts.put(OrderType.BUY, new AtomicInteger(0));
    counts.put(OrderType.SELL, new AtomicInteger(0));
    counts.put(OrderType.BUY_OPEN, new AtomicInteger(0));
    counts.put(OrderType.SELL_CLOSE, new AtomicInteger(0));
    counts.put(OrderType.OPTION_EXPIRE, new AtomicInteger(0));
  }
  
  public void load(Iterable<Trade> trades) {
    for (Trade t : trades) {
      history.add(t);
      String key = t.getSecurity();
      if (!allTrades.containsKey(key)) {
        List<Trade> list = Lists.newArrayList();
        allTrades.put(key, list);
      }
      allTrades.get(key).add(t);
      counts.get(t.getOrderType()).incrementAndGet();
    }
  }

  public Integer getCount(OrderType type) {
    return counts.get(type).get();
  }
  
  public List<Pair<OrderType, Integer>> getOrderTypeStats() {
    List<Pair<OrderType, Integer>> stats = Lists.newArrayList();
    for (Entry<OrderType, AtomicInteger> e : counts.entrySet()) {
      stats.add(Pair.of(e.getKey(), e.getValue().get()));
    }
    return stats;
  }
  
  public int getTrades() {
    return history.size();
  }

  public <V> List<Trade> getBySecurity(final String symbol, Visitor<Trade, V>... v) {
    return filterBy(new Predicate<Trade>() {
      @Override
      public boolean apply(Trade t) {
        return symbol.equalsIgnoreCase(t.getSecurity());
      }
    }, v);
  }

  public <V> List<Trade> getByOrderType(final OrderType type, Visitor<Trade, V>... v) {
    return filterBy(new Predicate<Trade>() {
      @Override
      public boolean apply(Trade t) {
        return type == t.getOrderType();
      }
    }, v);
  }

  public <V> List<Trade> filterBy(Predicate<Trade> p, Visitor<Trade, V>... v) {
    List<Trade> out = Lists.newArrayList();
    for (Trade t : history) {
      if (p.apply(t)) {
        out.add(t);
        for (Visitor<Trade, V> vv : v) {
          vv.visit(t);
        }
      }
    }
    return out;
  }
  
  public <V> void visit(Visitor<Trade, V>... v) {
    for (Trade t : history) {
      for (Visitor<Trade, V> vv : v) {
        vv.visit(t);
      }
    }
  }
}
