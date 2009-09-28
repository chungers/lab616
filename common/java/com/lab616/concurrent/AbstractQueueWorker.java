// 2009 lab616.com, All Rights Reserved.

package com.lab616.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Simple worker thread that continuously takes work off a queue.
 *
 * @author david
 *
 * @param <T>
 */
public abstract class AbstractQueueWorker<T> extends Thread {

  private BlockingQueue<T> workQueue;
  private Boolean running = true;
  private long processed = 0;
  
  protected AbstractQueueWorker(String name, boolean usePriorityQueue) {
    super.setName(name);
    if (usePriorityQueue) {
      this.workQueue = new PriorityBlockingQueue<T>(getInitialCapacity());
    } else {
      this.workQueue = new LinkedBlockingQueue<T>(getInitialCapacity());
    }
  }
  
  /**
   * Subclass override this to set the capacity.
   * @return The capacity.
   */
  protected int getInitialCapacity() {
    return 100;
  }

  /**
   * Subclass override this to determine if we need to flush the queue on stop.
   * @return True to continue until queue drained.
   */
  protected boolean flushQueueOnStop() {
    return true;
  }
  
  /**
   * Override to control the running of the worker.  Return true to continue.
   * @return True if continue.
   */
  protected boolean onStart() {
    return true;
  }
  
  /**
   * Override to get notification that the thread has stopped.
   */
  protected void onStop(int queueSize) {
    // Nothing.
  }
  
  /**
   * Subclass override this to handle exception.
   * @param e The exception.
   * @return True to continue.
   */
  protected boolean handleException(Exception e) {
    // Do nothing.
    return true;
  }
  
  /**
   * Subclass override this to handle interruption.
   * @param e The exception.
   * @return True to continue.
   */
  protected boolean handleInterruption(InterruptedException e) {
    return true;
  }
  
  /**
   * Set true to stop the run loop, which then stops the thread.
   * @param b True to stop.
   */
  public synchronized void setRunning(boolean b) {
    this.running = b;
  }
  
  /**
   * Returns if the worker is running.
   * @return True if running.
   */
  public synchronized Boolean isRunning() {
    return this.running;
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
  
  private void execute() {
    try {
      T work = this.workQueue.take();
      if (work instanceof Runnable) {
        ((Runnable) work).run();
      } else if (work instanceof Callable<?>) {
        try {
          ((Callable<?>) work).call();
        } catch (Exception e) {
          running = handleException(e);
        }
      } else {
        try {
          execute(work);
        } catch (Exception e) {
          running = handleException(e);
        }
      }
      this.processed++;
    } catch (InterruptedException e) {
      running = handleException(e);
    }
  }
  
  /**
   * Thread's run body.
   */
  public void run() {
    running = onStart();
    while (running) {
      execute();
    }
    if (!running && flushQueueOnStop()) {
      while (!this.workQueue.isEmpty()) {
        // flush out final work.
        execute();
      }
    }
    onStop(this.workQueue.size());
  }
  
  public final boolean enqueue(T work) {
    if (!running) return false;
    try {
      this.workQueue.put(work);
      return true;
    } catch (InterruptedException e) {
      running = handleInterruption(e);
      return false;
    }
  }
  
  protected abstract void execute(T work) throws Exception;
}
