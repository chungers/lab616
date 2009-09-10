// 2009 lab616.com, All Rights Reserved.

package com.lab616.aws.sdb;

import com.amazonaws.sdb.AmazonSimpleDBException;

/**
 * Exception thrown when interfacing with SimpleDB.
 *
 * @author david
 *
 */
public class SimpleDBException extends RuntimeException {

  private static final long serialVersionUID = -6541915386433965297L;

  public SimpleDBException(AmazonSimpleDBException ex) {
    super(ex.getMessage());
  }
}
