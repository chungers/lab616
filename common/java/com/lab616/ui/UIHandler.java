// 2009 lab616.com, All Rights Reserved.

package com.lab616.ui;


/**
 * Interface for handling some event in the UI.
 *
 * @author david
 *
 */
public interface UIHandler<S> {
  
  /**
   * Determines if the control matches the given handler.  If true, then the
   * handler's handleUI method will be invoked next..
   * 
   * @param name  The name of the control that is active.
   * @param control The control whose properties can be interrogated.
   * @param state Some application-specific state.
   * @return True if this handler is to handle the UI event.
   */
  boolean match(String name, UIControl control, S state);
  
  /**
   * Performs some processing, if the current state and UI control matches, as
   * determined by {@link match}.
   * 
   * @param control The UI control in scope.
   * @param state The current application state.
   * @return A possibly new application state.
   * @throws Exception Any exceptions.
   */
  S handleUI(UIControl control, S state) throws Exception;
}
