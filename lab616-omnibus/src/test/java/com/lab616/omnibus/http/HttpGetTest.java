// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.omnibus.http;

import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class HttpGetTest extends TestCase {

  public void testGet() throws Exception {
    HttpGet get = null;

    get = new HttpGet("http://foo.com", 3456, "/path")
    .add("p1", "v1").add("p2", "v2");
    assertEquals("http://foo.com:3456/path?p1=v1&p2=v2", get.getUrl());

    get = new HttpGet("foo.com", 3456, "/path2")
    .add("p1", "v1").add("p2", "v2");
    assertEquals("http://foo.com:3456/path2?p1=v1&p2=v2", get.getUrl());

    get = new HttpGet("http://foo.com/a")
    .add("p1", "v1").add("p2", "v2");
    assertEquals("http://foo.com/a?p1=v1&p2=v2", get.getUrl());

    get = new HttpGet("http://foo.com/a/b/c")
    .add("p1", "v1").add("p2", "v2").add("p", "v");
    assertEquals("http://foo.com/a/b/c?p1=v1&p2=v2&p=v", get.getUrl());

    get = new HttpGet("http://foo.com/a/b/c");
    assertEquals("http://foo.com/a/b/c", get.getUrl());
  }


}
