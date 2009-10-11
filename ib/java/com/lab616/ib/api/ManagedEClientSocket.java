// 2009 lab616.com, All Rights Reserved.

package com.lab616.ib.api;

import java.io.IOException;
import java.net.Socket;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Order;

/**
 * Instrumented EClientSocket to provide ability to intercept critical
 * calls such as placing and canceling orders, account access. Derived
 * classes can implement auditing at the client method level, instead
 * of relying on events being generated which are asynchronous to
 * actual method execution / socket read/writes.
 *
 * @author david
 *
 */
public class ManagedEClientSocket extends EClientSocket {
  
  private final EWrapper eWrapper;
  public ManagedEClientSocket(EWrapper wrapper) {
   super(wrapper); 
   eWrapper = wrapper;
  }

  protected EWrapper getEWrapper() {
    return eWrapper;
  }
  
  @Override
  public synchronized void eConnect(Socket socket, int clientId)
      throws IOException {
    if (pre_eConnect(socket, clientId)) {
      super.eConnect(socket, clientId);
      post_eConnect(socket, clientId);
    }
  }
  
  protected boolean pre_eConnect(Socket socket, int clientId)
    throws IOException {
    return true;
  }

  protected void post_eConnect(Socket socket, int clientId)
    throws IOException {
  }
  
  @Override
  public synchronized void eConnect(String host, int port, int clientId) {
    if (pre_eConnect(host, port, clientId)) {
      super.eConnect(host, port, clientId);
      post_eConnect(host, port, clientId);
    }
  }

  protected boolean pre_eConnect(String host, int port, int clientId) {
    return true;
  }
  
  protected void post_eConnect(String host, int port, int clientId) {
  }
  
  @Override
  public synchronized void eDisconnect() {
    if (pre_eDisconnect()) {
      super.eDisconnect();
      post_eDisconnect();
    }
  }

  protected boolean pre_eDisconnect() {
    return true;
  }
  
  protected void post_eDisconnect() {
  }

  @Override
  public synchronized void placeOrder(int id, Contract contract, Order order) {
    if (pre_placeOrder(id, contract, order)) {
      super.placeOrder(id, contract, order);
      post_placeOrder(id, contract, order);
    }
  }

  protected boolean pre_placeOrder(int id, Contract contract, Order order) {
    throw new RuntimeException("Must provide explicit implementation.");
  }

  protected void post_placeOrder(int id, Contract contract, Order order) {
  }

  @Override
  public synchronized void reqAccountUpdates(boolean subscribe, String acctCode) {
    if (pre_reqAccountUpdates(subscribe, acctCode)) {
      super.reqAccountUpdates(subscribe, acctCode);
      post_reqAccountUpdates(subscribe, acctCode);
    }
  }

  protected boolean pre_reqAccountUpdates(boolean subscribe, String acctCode) {
    return true;
  }

  protected void post_reqAccountUpdates(boolean subscribe, String acctCode) {
  }

  @Override
  public synchronized void reqAllOpenOrders() {
    if (pre_reqAllOpenOrders()) {
      super.reqAllOpenOrders();
      post_reqAllOpenOrders();
    }
  }

  protected boolean pre_reqAllOpenOrders() {
    return true;
  }
  
  protected void post_reqAllOpenOrders() {
  }

  @Override
  public synchronized void reqManagedAccts() {
    if (pre_reqManagedAccts()) {
      super.reqManagedAccts();
      post_reqManagedAccts();
    }
  }

  protected boolean pre_reqManagedAccts() {
    return true;
  }
  
  protected void post_reqManagedAccts() {
  }

  @Override
  public synchronized void reqMktData(int tickerId, Contract contract,
      String genericTickList, boolean snapshot) {
    if (pre_reqMktData(tickerId, contract, genericTickList, snapshot)) {
      super.reqMktData(tickerId, contract, genericTickList, snapshot);
      post_reqMktData(tickerId, contract, genericTickList, snapshot);
    }
  }

  protected boolean pre_reqMktData(int tickerId, Contract contract,
      String genericTickList, boolean snapshot) {
    return true;
  }

  protected void post_reqMktData(int tickerId, Contract contract,
      String genericTickList, boolean snapshot) {
  }

  @Override
  public synchronized void reqMktDepth(int tickerId, Contract contract,
      int numRows) {
    if (pre_reqMktDepth(tickerId, contract, numRows)) {
      super.reqMktDepth(tickerId, contract, numRows);
      post_reqMktDepth(tickerId, contract, numRows);
    }
  }

  protected boolean pre_reqMktDepth(int tickerId, Contract contract,
      int numRows) {
    return true;
  }
  
  protected void post_reqMktDepth(int tickerId, Contract contract,
      int numRows) {
  }

  @Override
  public synchronized void reqRealTimeBars(int tickerId, Contract contract,
      int barSize, String whatToShow, boolean useRTH) {
    if (pre_reqRealTimeBars(tickerId, contract, barSize, whatToShow, useRTH)) {
      super.reqRealTimeBars(tickerId, contract, barSize, whatToShow, useRTH);
      post_reqRealTimeBars(tickerId, contract, barSize, whatToShow, useRTH);
    }
  }
  
  protected boolean pre_reqRealTimeBars(int tickerId, Contract contract,
      int barSize, String whatToShow, boolean useRTH) {
    return true;
  }
  
  protected void post_reqRealTimeBars(int tickerId, Contract contract,
      int barSize, String whatToShow, boolean useRTH) {
  }
  
}
