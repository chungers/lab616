// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.omnibus.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author david
 *
 */
public class HttpGet {

  private final DefaultHttpClient httpClient = new DefaultHttpClient();

  private final String path;
  private StringBuffer queries = new StringBuffer();
  private URL composedUrl = null;
  private int params = 0;
  private HttpResponse response = null;
  
  private int responseStatusCode = -1;
  
  public HttpGet(String path) {
    this.path = path;
  }

  public HttpGet(String host, int port, String path) {
    String h = (host.startsWith("http")) ? host : "http://" + host;
    this.path = h + ":" + port + path;
  }

  
  public HttpGet add(String param, String value) {
    try {
      if (params++ > 0) {
        queries.append("&");
      }
      queries.append(String.format("%s=%s", param, URLEncoder.encode(value, "UTF-8")));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    return this;
  }
  
  public int getStatusCode() {
    return responseStatusCode;
  }
  
  public String getUrl() throws IOException {
    if (composedUrl == null) {
      buildUrl();
    }
    return (composedUrl != null) ? composedUrl.toExternalForm() : null;
  }

  private void buildUrl() throws IOException {
    StringBuffer buff = new StringBuffer(path);
    if (params > 0) {
      buff.append("?");
      buff.append(queries);
    }
    composedUrl = new URL(buff.toString());
  }

  public byte[] fetch() throws IOException {
    if (composedUrl == null) {
      buildUrl();
    }
    org.apache.http.client.methods.HttpGet get = 
      new org.apache.http.client.methods.HttpGet(getUrl());
    
    response = httpClient.execute(get);
    responseStatusCode = response.getStatusLine().getStatusCode();
    
    if (response.getEntity() != null) {
      ByteArrayOutputStream c = new ByteArrayOutputStream();
      response.getEntity().writeTo(c);
      c.flush();
      c.close();
      return c.toByteArray();
    }
    return null;
  }
}
