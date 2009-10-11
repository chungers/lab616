// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.google.common.collect.Lists;
import com.lab616.omnibus.event.EventEngine.Subscriber;
import com.lab616.omnibus.event.StreamSplitter.Direct.SubscriberClause;


/**
 *
 *
 * @author david
 *
 */
public class StreamSplitter<T> {
  
  static Logger logger = Logger.getLogger(StreamSplitter.class);
  
  static class SplitterException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public SplitterException(String msg) {
      super(msg);
    }
  }
    
  EventDefinition<?> sourceEvent;
  List<WhereClause> wheres = Lists.newArrayList();
  List<Direct.SubscriberClause> subs = Lists.newArrayList();
  
  EPAdministrator engineAdmin;
  
  @SuppressWarnings("unchecked")
  public StreamSplitter(Class<T> eventType,
    Set<EventDefinition> eventDefinitions,
    EPAdministrator admin) {
    this.engineAdmin = admin;
    // Verify that we have this event registered.
    for (EventDefinition<?> d : eventDefinitions) {
      if (d.eventType.equals(eventType)) {
        sourceEvent = d;
      }
    }
    if (sourceEvent == null) {
      throw new SplitterException(eventType + " not registered.");
    }
  }
  
  public StreamSplitter<T> and() {
    return this;
  }
  
  public class WhereClause {
    String dest;
    String whereClause;

    WhereClause(String newStreamName) {
      dest = newStreamName;
      wheres.add(this);
    }
    
    public StreamSplitter<T> where(String expression) {
      whereClause = expression;
      return StreamSplitter.this;
    }
  }
  
  public WhereClause into(String newStreamName) {
    return new WhereClause(newStreamName);
  }
  
  public class Direct {
    
    public class SubscriberClause {
      String select;
      Object sub;
      SubscriberClause(String exp) {
        select = exp;
        subs.add(this);
      }
      
      public <S extends Subscriber<T>> StreamSplitter<T> to(S subscriber) {
        sub = subscriber;
        return StreamSplitter.this;
      }
    }
    
    public SubscriberClause direct(String exp) {
      return new SubscriberClause(exp);
    }
  }
  
  public Direct then() {
    return new Direct();
  }
  
  @SuppressWarnings("unchecked")
  public void build() {
    StringBuffer statement = new StringBuffer("on " + sourceEvent.name + "\n");
    for (WhereClause w : wheres) {
      statement.append(String.format(
          "  insert into %s select * where %s\n", w.dest, w.whereClause));
    }
    this.engineAdmin.createEPL(statement.toString());
    logger.info("Adding stream splitter: " + statement);
    // Now add subscribers
    for (SubscriberClause sub : subs) {
      EPStatement s = this.engineAdmin.createEPL(sub.select);
      s.setSubscriber(sub.sub);
      logger.info("Bound " + sub.select + " to " + sub.sub);
    }
  }
}
