// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.http.servlets;

import java.util.Map;

import com.lab616.omnibus.http.BasicServlet;

/**
 * @author david
 *
 */
public class QuitServlet extends BasicServlet {

  private static final long serialVersionUID = 1L;

  protected void processRequest(Map<String, String> params, 
      ResponseBuilder b) {
		Runtime.getRuntime().exit(0);
  }
}
