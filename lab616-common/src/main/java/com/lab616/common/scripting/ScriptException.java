/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lab616.common.scripting;

import org.apache.log4j.Logger;

/**
 *
 * @author dchung
 */
public class ScriptException extends RuntimeException {

  // Idea: set up severity and functions to do auto-recovery??
  public enum Severity {
    WARNING,

  }

  private final Object scriptObject;

  public <T extends ScriptObject> ScriptException(T source,
    String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
    this.scriptObject = source;
    Logger.getLogger(source.getClass()).error(getMessage());
  }

  public <T extends ScriptObject> ScriptException(T source, Throwable th,
    String messageFormat, Object... args) {
    super(String.format(messageFormat, args), th);
    this.scriptObject = source;
    Logger.getLogger(source.getClass()).error(getMessage(), th);
  }

  public ScriptObject getScriptObject() {
    // This is safe because the generics in the constructor forces the object
    // reference to be an instance of ScriptObject or its derived classes.
    return ScriptObject.class.cast(scriptObject);
  }
}
