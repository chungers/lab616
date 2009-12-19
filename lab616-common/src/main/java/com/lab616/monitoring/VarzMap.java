// 2009 lab616.com, All Rights Reserved.

package com.lab616.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author david
 *
 */
public class VarzMap {

	static Logger logger = Logger.getLogger(VarzMap.class);
	
	private VarzMap() {
		// Do not instantiate.
	}
	
	@SuppressWarnings("serial")
  public static <V> Map<String, V> create(final Class<V> proto) {
		return new HashMap<String, V>() {
			@Override
      public V get(Object key) {
				if (super.containsKey(key)) {
					return super.get(key);
				} else {
					try {
						super.put((String)key, proto.newInstance());
					} catch (Exception e) {
						logger.warn("Cannot set varz map value.", e);
					}
				}
	      return super.get(key);
      }
		};
	}
}
