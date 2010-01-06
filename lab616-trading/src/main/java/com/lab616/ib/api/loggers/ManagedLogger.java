package com.lab616.ib.api.loggers;

import com.lab616.ib.api.TWSClientManager.Managed;

/**
 *
 * @author dchung
 */
public interface ManagedLogger<R> extends Managed {

  /**
   * Returns the resource associated with this logger.
   * @return The resource, usually a File.
   */
  public R getResource();

}
