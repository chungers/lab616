// 2009 lab616.com, All Rights Reserved.

package com.lab616.aws.sdb;

import java.util.List;
import java.util.Map;

import com.amazonaws.sdb.model.BatchPutAttributesRequest;
import com.amazonaws.sdb.model.ReplaceableAttribute;
import com.amazonaws.sdb.model.ReplaceableItem;
import com.google.inject.Inject;
import com.google.inject.internal.ImmutableList;
import com.google.inject.internal.Lists;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;

/**
 * A SimpleDB domain, which corresponds to a table.  Create a Domain for each
 * writing thread as there are states that are not thread-safe.
 *
 * @author david
 *
 */
public class Domain {

  @Flag(name="domain-batch-size")
  public static Integer DEFAULT_BATCH_SIZE = 100;
  
  static {
    Flags.register(Domain.class);
  }
  
  enum State {
    ACTIVE,
    DELETED;
  }
  
  private State state = State.ACTIVE;
  private int putBatchSize;
  private List<ReplaceableItem> batch = Lists.newArrayList();
  
  final private SimpleDB service;
  final private String domainName;
  
  public Domain(SimpleDB service, String domainName) {
    this.service = service;
    this.domainName = domainName;
    this.putBatchSize = DEFAULT_BATCH_SIZE;
  }
  
  /**
   * Returns the domain name.
   * 
   * @return The domain name.
   */
  public final String getName() {
    return this.domainName;
  }
  
  /**
   * Returns the state of the domain.
   * @return The state (active or deleted).
   */
  public State getState() {
    return this.state;
  }
  
  /**
   * Mark this domain as deleted.
   */
  void deleted() {
    this.state = State.DELETED;
  }

  /**
   * A simple interface for mapping an object to SimpleDB constructs.
   *
   * @param <T> The type of the object.
   */
  public interface ObjectMapper<T> {
    
    public String getItemName(T object);
    
    public List<String> getAttributes(T object);
    
    public String getValue(T object, String attribute);
  }
 
  private <T> ReplaceableItem fromObject(ObjectMapper<T> mapper, T row) {
    ReplaceableItem item = new ReplaceableItem();
    item.setItemName(mapper.getItemName(row));
    List<ReplaceableAttribute> attrs = Lists.newArrayList();
    item.setAttribute(attrs);
    for (String attribute : mapper.getAttributes(row)) {
      ReplaceableAttribute attr = new ReplaceableAttribute();
      attr.setName(attribute);
      attr.setValue(mapper.getValue(row, attribute));
      attr.setReplace(true);
      attrs.add(attr);
    }
    return item;
  }

  private void sendBatch() {
    BatchPutAttributesRequest request = new BatchPutAttributesRequest();
    request.setDomainName(getName());
    List<ReplaceableItem> copy = Lists.newArrayList();
    copy.addAll(this.batch);
    request.setItem(copy);
    copy.clear();
    
    this.service.performAsync(request);
  }

  public <T> void put(ObjectMapper<T> mapper, T row) {
    if (this.batch.size() == putBatchSize) {
      sendBatch();
    }
    
    this.batch.add(fromObject(mapper, row));
  }
  
  
  /**
   * Put / insert objects.  This operation is completely non-blocking in that
   * asynchronous put operations are executed by the SimpleDB service.  The
   * responses of these put operations are returned in the future where errors
   * are logged and possibly retried.
   * 
   * @param mapper A mapper for translating object properties to attributes.
   * @param rows The objects.
   */
  public <T> void put(ObjectMapper<T> mapper, Iterable<T> rows) {
    BatchPutAttributesRequest request = new BatchPutAttributesRequest();
    request.setDomainName(getName());
    List<ReplaceableItem> items = Lists.newArrayList();
    request.setItem(items);

    int ct = 0;
    for (T row : rows) {
      // Add to the list of items.
      items.add(fromObject(mapper, row));
      
      if (ct++ > putBatchSize) {
        // send the request.
        this.service.performAsync(request);
        
        // reset the state.
        ct = 0;
        request = new BatchPutAttributesRequest();
        request.setDomainName(getName());
        items = Lists.newArrayList();
        request.setItem(items);
      }
    }
    
    // Dispatch the final one
    if (request.getItem().size() > 0) {
      this.service.performAsync(request);
    }
  }

  /**
   * Notifies the Domain that shut down is imminent.
   */
  public void stop() {
    sendBatch();
  }
}
