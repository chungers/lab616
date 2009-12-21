// 2009 lab616.com, All Rights Reserved.

package com.lab616.concurrent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.inject.internal.Lists;
import com.lab616.monitoring.Varz;
import com.lab616.monitoring.VarzMap;
import com.lab616.monitoring.Varzs;

/**
 * Simple worker thread that continuously takes work off a queue.
 *
 * @author david
 *
 * @param <T>
 */
public class QueueProcessor<T, V> extends Thread implements Iterable<V> {

  @Varz(name = "work-queue-depth")
  public static Map<String, AtomicInteger> queueDepths = 
    VarzMap.create(AtomicInteger.class);
  
  @Varz(name = "work-queue-processed")
  public static Map<String, AtomicLong> queueProcessed = 
    VarzMap.create(AtomicLong.class);
  
  static {
    Varzs.export(QueueProcessor.class);
  }

  private BlockingQueue<T> workQueue;
  private AtomicBoolean running = new AtomicBoolean(true);
  private long processed = 0;
  private CountDownLatch stoppingLatch = new CountDownLatch(1);
  private Function<T, V> workFunction = null;
  private BlockingQueue<V> resultQueue = null;
  
  protected QueueProcessor(String name, boolean usePriorityQueue) {
    super.setName(name);
    if (usePriorityQueue) {
      this.workQueue = (getInitialCapacity() > 0) ? 
          new PriorityBlockingQueue<T>(getInitialCapacity()) :
            new PriorityBlockingQueue<T>();
    } else {
      this.workQueue = (getInitialCapacity() > 0) ? 
          new LinkedBlockingQueue<T>(getInitialCapacity()) :
            new LinkedBlockingQueue<T>();
    }
  }
  
  /**
   * More functional programming friendly where by including a closure, there is
   * no need to subclass this class.
   * 
   * @param name The name.
   * @param usePriorityQueue If we use priority queue.
   * @param work Work function.
   */
  public QueueProcessor(String name, boolean usePriorityQueue, Function<T, V> work) {
  	this(name, usePriorityQueue);
  	this.workFunction = work;
  	this.resultQueue = new LinkedBlockingQueue<V>();
  }
  
  @Override
	public Iterator<V> iterator() {
  	return new Iterator<V>() {
  		private V nextValue = null;
  		@Override
			public boolean hasNext() {
  			if (resultQueue == null) {
  				return false;
  			}
  			nextValue = resultQueue.poll();
				return nextValue != null;
			}
			@Override
			public V next() {
				return nextValue;
			}
			@Override
			public void remove() {
			}
  	};
	}

	/**
   * Subclass override this to set the capacity.
   * @return The capacity.
   */
  protected int getInitialCapacity() {
    return -1;
  }

  /**
   * Subclass override this to determine if we need to flush the queue on stop.
   * @return True to continue until queue drained.
   */
  protected boolean flushQueueOnStop() {
    return true;
  }
  
  protected Logger getLogger() {
    return Logger.getLogger(getClass());
  }
  
  /**
   * Override to control the running of the worker.  Return true to continue.
   * @return True if continue.
   */
  protected boolean onStart() {
    getLogger().info(getClass().getSimpleName() + " starting.");
    return true;
  }
  
  /**
   * Override to get notification that the thread has stopped.
   */
  protected void onStop(int queueSize) {
    getLogger().info(getClass().getSimpleName() + " stopped.");
    // Nothing.
  }
  
  /**
   * Subclass override this to handle exception.
   * @param e The exception.
   * @return True to continue.
   */
  protected boolean handleException(Exception e) {
    getLogger().error(getClass().getSimpleName() + " exception: ", e);
    // Do nothing.
    return true;
  }
  
  /**
   * Subclass override this to handle interruption.
   * @param e The exception.
   * @return True to continue.
   */
  protected boolean handleInterruption(InterruptedException e) {
    getLogger().error(getClass().getSimpleName() + " interrupted: ", e);
    return true;
  }
  
  /**
   * Set true to stop the run loop, which then stops the thread.
   * @param b True to stop.
   */
  public void setRunning(boolean b) {
    this.running.set(b);
  }
  
  /**
   * Returns if the worker is running.
   * @return True if running.
   */
  public Boolean isRunning() {
    return this.running.get();
  }
  
  public void waitForStop(long timeout, TimeUnit unit) 
    throws InterruptedException {
    getLogger().info(getClass().getSimpleName() + " waiting for queue shutdown.");
    this.stoppingLatch.await(timeout, unit);
  }
  /**
   * Returns a count of how many processed.
   * @return The count.
   */
  public final long getProcessed() {
    return this.processed;
  }
  
  /**
   * Returns how many items still on queue.
   * @return The count.
   */
  public final int getQueueDepth() {
    return this.workQueue.size();
  }

  /**
   * Subclass decides whether to take work from the queue.
   * @return True if to take work from queue.
   */
  protected boolean take() {
    return true;
  }
  
  private void execute() {
    if (!take() || this.workQueue.isEmpty()) return;
    try {
      T work = this.workQueue.take();
      if (work instanceof Runnable) {
        ((Runnable) work).run();
      } else if (work instanceof Callable<?>) {
        try {
          ((Callable<?>) work).call();
        } catch (Exception e) {
          running.set(handleException(e));
        }
      } else {
        try {
        	if (workFunction != null) {
        		V v = workFunction.apply(work);
        		if (resultQueue != null) {
        			resultQueue.put(v);
        		}
        	} else {
            execute(work);
        	}
        } catch (Exception e) {
          running.set(handleException(e));
        }
      }
      this.processed++;
      queueProcessed.get(getName()).incrementAndGet();
      queueDepths.get(getName()).set(getQueueDepth());
    } catch (InterruptedException e) {
      running.set(handleException(e));
    }
  }
  
  /**
   * Thread's run body.
   */
  public void run() {
    running.set(onStart());
    while (running.get()) {
      execute();
    }
    getLogger().info("Stopping " + getName() + " @ queueSize="+ this.workQueue.size());
    if (flushQueueOnStop()) {
      getLogger().info("Flushing queue " + this.workQueue.size());
      while (!this.workQueue.isEmpty()) {
        // flush out final work.
        execute();
      }
    }
    onStop(this.workQueue.size());
    stoppingLatch.countDown(); // Stopped.
  }
  
  public final boolean enqueue(T work) {
    if (!running.get()) return false;
    try {
      this.workQueue.put(work);
      return true;
    } catch (InterruptedException e) {
      running.set(handleInterruption(e));
      return false;
    }
  }
  
  /**
   * Drains the content of this queue to the queue provided.
   * @param qw The queue to drain to.
   */
  public final void drainTo(QueueProcessor<T, V> qw) {
    List<T> sink = Lists.newArrayList();
    this.workQueue.drainTo(sink);
    for (T w : sink) {
      qw.workQueue.add(w);
    }
  }
  
  /**
   * Subclass can override this method to provide the processing needed.  
   * Otherwise, this class can be instantiated with a Function<T,V> where
   * the processor instance becomes an iterable of type V.  In the subclass
   * case, V is Void basically since no values are returned by the unit of work
   * T from the queue.
   * 
   * @param work Some queued unit of work.
   * @throws Exception
   */
  protected void execute(T work) throws Exception {
    // Do nothing.
  }
}
