// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.ib.api.simulator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ib.client.EWrapper;
import com.lab616.ib.api.ApiBuilder;
import com.lab616.ib.api.ApiMethods;
import com.lab616.ib.api.proto.TWSProto;
import com.lab616.ib.api.proto.TWSProto.Event;
import com.lab616.util.Time;

/**
 * Simple data source that exposes an EWrapper interface for sending events
 * as the simulator's DataSource.
 * @author david
 *
 */
public class EWrapperDataSource extends DataSource {

	private final BlockingQueue<TWSProto.Event> queue;
	private TWSProto.Event lastEvent = null;
	private boolean disconnected = false;
	
	public EWrapperDataSource(String sourceName) {
		super(sourceName);
		this.queue = new LinkedBlockingQueue<TWSProto.Event>();
	}
	
	@Override
	protected void source(BlockingQueue<Event> sink) throws Exception {
		// Just write from one queue to another
		do {
			sink.put(lastEvent = queue.take());
			checkDisconnectedMessage(lastEvent);
		} while (!finished());
	}

	@Override
	public boolean finished() {
		return disconnected && queue.isEmpty();
	}
	
	private void checkDisconnectedMessage(TWSProto.Event event) {
		boolean discoMessage = event != null &&
		(event.getMethod() == TWSProto.Method.error &&
				event.getField(1).getIntValue() == 504);
		if (discoMessage) {
			disconnected = true;
		}
	}
	
	/**
	 * Returns an EWrapper interface for sending events by calling the api.
	 * @return The interface.
	 */
  public final EWrapper getEWrapper() {
    return (EWrapper)Proxy.newProxyInstance(
         EWrapper.class.getClassLoader(), 
         new Class[] { EWrapper.class }, 
         new InvocationHandler() {
        	 public Object invoke(Object proxy, Method m, Object[] args) 
             throws Throwable {
          	 try {
        				ApiBuilder builder = ApiMethods.get(m.getName());
         				queue.put(builder.buildProto(getResource(), Time.now(), args));
         				return null;
          	 } catch (NullPointerException e) {
          		 // When the method is unknow.  Try to invoke the handler itself.
          		 // This would work for hashCode(), equals(), etc.
          		 try {
          			 return m.invoke(this, args);
          		 } catch (Exception e2) {
          			 throw new RuntimeException(e2);
          		 }
          	 }
           }
         });
   }
  
  /**
   * Returns the last EWrapper invocation in the form of a TWSProto.Event proto.
   * @return The last event.
   */
  public final TWSProto.Event getLastEvent() {
  	return lastEvent;
  }
}
