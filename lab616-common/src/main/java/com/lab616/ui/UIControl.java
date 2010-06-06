// 2009 lab616.com, All Rights Reserved.

package com.lab616.ui;

/**
 * Generic interface to model a UI control, which may be implemented by
 * Java AWT / Swing.
 *
 * @author david
 *
 */
public interface UIControl {

  /**
   * Interface to an underlying Window.
   */
  interface Window {
    void setVisible(Boolean b);
    void resize(int w, int h);
    void dispose();
  }

  /**
   * Interface to an underlying field in a UI control.
   */
  interface Field {
    void setValue(String s);
  }
  
  /**
   * Interface to a user selectable UI option.
   */
  interface Option {
    void select();
  }
  
  /**
   * Interface to a submit button/ button of some type.
   */
  interface Submit {
    void submit();
  }

  /**
   * Returns the window.
   * @return The window.
   */
  Window getWindow();
  
  /**
   * Returns if the control displays a message that matches the given regular
   * expression.
   * 
   * @param regex The regular expression to match.
   * @param regexs Additional alternatives.
   * @return True if the control displays any of the given expressions.
   */
  boolean hasMessage(String regex, String... regexs);
  
  /**
   * Returns a field at the specified index.
   * 
   * @param idx The index starting at 0.
   * @return The field interface.
   */
  Field getField(int idx);
  
  /**
   * Returns an options (e.g. radio button, etc.) of the given name/key.
   * 
   * @param key The name of the option.
   * @return The option interface.
   */
  Option getOption(String key);

  /**
   * Returns a submit which is like a button that when activated, triggers a 
   * user interface state transition.
   * 
   * @param name The name of the submit (button) to match.
   * @param optionalForceEnable True to setEnable(true) if not already enabled.
   * @return The submit interface.
   */
  Submit getSubmit(String name, boolean... optionalForceEnable);
}
