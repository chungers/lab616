// 2009 lab616.com, All Rights Reserved.

package com.lab616.ui;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.google.common.collect.Lists;



/**
 * A class that receives AWT window events.
 *
 * @author david
 *
 */
public class AWTWindowEventReceiver<S> 
  extends AWTEventReceiver<WindowEvent, S> {
  
  /**
   * Constructor with a list of handlers.
   * 
   * @param handler The first handler.
   * @param handlers The other handlers.
   */
  public AWTWindowEventReceiver(UIHandler<S> handler, 
      UIHandler<S>... handlers) {
    super(handler, handlers);
  }

  /**
   * Constructor with initial state.
   * 
   * @param initial
   * @param handler
   * @param handlers
   */
  public AWTWindowEventReceiver(S initial, UIHandler<S> handler, 
      UIHandler<S>... handlers) {
    this(handler, handlers);
    setState(initial);
  }

  @Override
  protected Container getContainer(WindowEvent event) {
    return (Container)event.getWindow();
  }

  @Override
  protected long getEventMask() {
    return AWTEvent.WINDOW_EVENT_MASK;
  }

  @Override
  protected List<Integer> getEventsToHandle() {
    return Lists.newArrayList(
        WindowEvent.WINDOW_OPENED);
  }

  @Override
  protected String getTitle(Container container) {
    if (container instanceof JFrame) {
      return ((JFrame)container).getTitle();
    } else if (container instanceof JDialog) {
      return ((JDialog)container).getTitle();
    }
    return null;
  }
}
