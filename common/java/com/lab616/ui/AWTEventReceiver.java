// 2009 lab616.com, All Rights Reserved.

package com.lab616.ui;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.internal.Lists;



/**
 * A class that receives AWT events and dispatches to a list of handlers for
 * processing.
 *
 * @param W The widget type whose events to listen.
 * @param S The state type.
 * @author david
 *
 */
public abstract class AWTEventReceiver<W, S>  implements AWTEventListener {

  private List<UIHandler<S>> handlers;
  private S state;

  private Logger logger = Logger.getLogger(getClass());
  
  /**
   * Constructor with a list of handlers.
   * 
   * @param handler The handler.
   * @param handlers The handlers.
   */
  public AWTEventReceiver(UIHandler<S> handler, UIHandler<S>... handlers) {
    Toolkit tk = Toolkit.getDefaultToolkit();
    tk.addAWTEventListener(this, getEventMask());
    this.handlers = Lists.newArrayList(handler, handlers);
  }

  /**
   * Constructor with initial state.
   * 
   * @param initial
   * @param handler
   * @param handlers
   */
  public AWTEventReceiver(S initial, UIHandler<S> handler, 
      UIHandler<S>... handlers) {
    this(handler, handlers);
    setState(initial);
  }
  
  /**
   * Derived class must implement and return the event mask such as
   * {@link AWTEvent#WINDOW_EVENT_MASK}
   * @return The event mask.
   */
  protected abstract long getEventMask();
  
  /**
   * Returns a list of events to invoke the handler.
   * @return
   */
  protected abstract List<Integer> getEventsToHandle();
  
  /**
   * Returns the container from the event.
   * @param event The event.
   * @return The container.
   */
  protected abstract Container getContainer(W event);
  
  /**
   * Returns the title of the container.
   * @param container The container.
   * @return The title string.
   */
  protected abstract String getTitle(Container container);

  /**
   * Sets the state of the receiver.
   * 
   * @param state The new state.
   * @return The receiver.
   */
  public final AWTEventReceiver<W, S> setState(S state) {
    this.state = state;
    return this;
  }

  /**
   * Receives AWT UI events and dispatch to handlers for processing.
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eventDispatched(AWTEvent event) {
    if (getEventsToHandle().contains(event.getID())) {
      Container container = getContainer((W)event);
      if (container == null) {
        return;
      }
      // Get the title/match key of the widget
      String title = getTitle(container);
      for (UIHandler<S> handler : handlers) {
        UIControl control = new AWTControl(container);
        if (title == null) {
          title = "";
        }
        if (handler.match(title, control, state)) {
          try {
            state = handler.handleUI(control, state);
          } catch (Exception e) {
            logger.error("Exception while handling.", e);
          }
        }
      }
    }
  }
}
