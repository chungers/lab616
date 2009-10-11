// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import junit.framework.TestCase;


/**
 *
 *
 * @author david
 *
 */
public class TWSEventTest extends TestCase {

  public void testComparable() throws Exception {
    Set<TWSEvent> sorted = Sets.newTreeSet();

    // Generate a bunch of events
    for (int i = 0; i < 1000; i++) {
      TWSEvent event = new TWSEvent();
      sorted.add(event);
      Thread.sleep(2L);
    }
    
    TWSEvent last = null;
    for (TWSEvent e : sorted) {
      if (last != null) {
        assertTrue(e.getTimestamp() > last.getTimestamp());
      }
      last = e;
    }
  }

  public void testComparable2() throws Exception {
    Map<TWSEvent, Integer> sorted = Maps.newTreeMap();

    // Generate a bunch of events
    for (int i = 0; i < 1000; i++) {
      TWSEvent event = new TWSEvent();
      sorted.put(event, i);
      Thread.sleep(2L);
    }
    
    TWSEvent last = null;
    Integer i = -1;
    for (TWSEvent e : sorted.keySet()) {
      if (last != null) {
        assertTrue(e.getTimestamp() > last.getTimestamp());
        assertTrue(sorted.get(e) > i);
      }
      last = e;
      i = sorted.get(e);
    }
  }
}
