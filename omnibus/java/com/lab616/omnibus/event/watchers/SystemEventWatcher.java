// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event.watchers;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.lab616.monitoring.Varz;
import com.lab616.monitoring.Varzs;
import com.lab616.omnibus.SystemEvent;
import com.lab616.omnibus.event.AbstractEventWatcher;
import com.lab616.omnibus.event.annotation.Statement;

/**
 * @author david
 *
 */
@Statement("select * from SystemEvent")
public class SystemEventWatcher extends AbstractEventWatcher {

	@Varz(name = "system-event-count")
	public static final AtomicLong countEvents = new AtomicLong();
	
	static {
		Varzs.export(SystemEventWatcher.class);
	}

	static Logger logger = Logger.getLogger(SystemEventWatcher.class);
	
	/**
	 * Update method required to implement esper subscriber object.
	 * @param event The event.
	 */
	public void update(SystemEvent event) {
		countEvents.incrementAndGet();
		logger.info(event);
	}
}
