// 2009 lab616.com, All Rights Reserved.

package com.lab616.aws.sdb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.amazonaws.sdb.AmazonSimpleDB;
import com.amazonaws.sdb.AmazonSimpleDBAsync;
import com.amazonaws.sdb.AmazonSimpleDBAsyncClient;
import com.amazonaws.sdb.AmazonSimpleDBClient;
import com.amazonaws.sdb.AmazonSimpleDBConfig;
import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.BatchPutAttributesRequest;
import com.amazonaws.sdb.model.BatchPutAttributesResponse;
import com.amazonaws.sdb.model.CreateDomainRequest;
import com.amazonaws.sdb.model.CreateDomainResponse;
import com.amazonaws.sdb.model.DeleteDomainRequest;
import com.amazonaws.sdb.model.DeleteDomainResponse;
import com.amazonaws.sdb.model.DomainMetadataRequest;
import com.amazonaws.sdb.model.DomainMetadataResponse;
import com.amazonaws.sdb.model.ListDomainsRequest;
import com.amazonaws.sdb.model.ListDomainsResponse;
import com.google.inject.Inject;
import com.google.inject.internal.ImmutableList;
import com.google.inject.internal.Lists;
import com.google.inject.name.Named;
import com.lab616.common.Pair;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;

/**
 * Service object for the Amazon SimpleDB service.
 *
 * @author david
 *
 */
public class SimpleDB {

  @Flag(name="sdb-max-retries")
  public static Integer DEFAULT_RETRIES = 5;
  
  static {
    Flags.register(SimpleDB.class);
  }
  
  static Logger logger = Logger.getLogger(SimpleDB.class);
  
  final private String accessKeyId;
  final private String secretAccessKey;
  
  final AmazonSimpleDB service;
  
  final AmazonSimpleDBAsync serviceAsync;

  // Thread pool that waits for the response and retries if necessary.
  final ExecutorService retryService;
  final ExecutorService clientService;
  
  @Inject
  public SimpleDB(
      @Named("aws-sdb-accessKeyId") String accessKeyId, 
      @Named("aws-sdb-secretAccessKey") String secretAccessKey,
      @Named("aws-sdb-threadPool") ExecutorService executor,
      @Named("aws-sdb-max-connections") int maxConnections,
      @Named("aws-sdb-put-retry") ExecutorService retryService) {
    
    this.accessKeyId = accessKeyId;
    this.secretAccessKey = secretAccessKey;
    
    this.service = new AmazonSimpleDBClient(
        this.accessKeyId, this.secretAccessKey);

    this.serviceAsync = new AmazonSimpleDBAsyncClient(
        accessKeyId, secretAccessKey, 
        new AmazonSimpleDBConfig().withMaxConnections(maxConnections), 
        executor);
    this.clientService = executor;
    this.retryService = retryService;
  }
  
  /**
   * Creates a new domain.
   * 
   * @param domainName The domain name.
   * @param The domain.
   */
  public Domain createDomain(String domainName) {
    CreateDomainRequest request = new CreateDomainRequest(); 
    request.setDomainName(domainName);
    try {
      CreateDomainResponse response = service.createDomain(request);

      if (response.isSetResponseMetadata()) {
        logger.info(String.format("Created domain %s. BoxUsage=%s",
            domainName, response.getResponseMetadata().getBoxUsage()));
      }

      Domain newDomain = new Domain(this, domainName);
      return newDomain;
    } catch (AmazonSimpleDBException e) {
      logger.fatal("Failed to create domain:" + domainName, e);
      throw new SimpleDBException(e);
    }
  }

  /**
   * Deletes a domain.
   * 
   * @param domain The domain.
   */
  public void deleteDomain(Domain domain) {
    deleteDomain(domain.getName());
    domain.deleted();
  }
  
  /**
   * Retrieves metadata about a domain.  Also serves as test for existence.
   * Null if domain does not exist.
   * 
   * @param domainName The domain name.
   * @return The domain.
   */
  public Domain getDomain(String domainName) {
    DomainMetadataRequest request = new DomainMetadataRequest();
    request.setDomainName(domainName);
    
    try {
      DomainMetadataResponse response = service.domainMetadata(request);
      if (response.isSetDomainMetadataResult()) {
        Domain domain = new Domain(this, domainName);
        return domain;
      }
      return null;
    } catch (AmazonSimpleDBException e) {
      logger.fatal("Failed to delete domain:" + domainName, e);
      throw new SimpleDBException(e);
    }
  }
  
  /**
   * Deletes an existing domain.
   * 
   * @param domainName The domain name.
   */
  public boolean deleteDomain(String domainName) {
    DeleteDomainRequest request = new DeleteDomainRequest();
    request.setDomainName(domainName);
    try {
      DeleteDomainResponse response = service.deleteDomain(request);
      
      if (response.isSetResponseMetadata()) {
        logger.info(String.format("Deleted domain %s. BoxUsage=%s",
            domainName, response.getResponseMetadata().getBoxUsage()));
        return true;
      }
      
      return false;
    } catch (AmazonSimpleDBException e) {
      logger.fatal("Failed to delete domain:" + domainName, e);
      throw new SimpleDBException(e);
    }
  }
  
  /**
   * Returns a list of domain names.
   * 
   * @return The list of domain names.
   */
  public List<Domain> getDomains() {
    ListDomainsRequest request = new ListDomainsRequest();
    try {
      ListDomainsResponse response = service.listDomains(request);

      if (response.isSetListDomainsResult()) {
        List<Domain> domains = Lists.newArrayList();
        for (String name : response.getListDomainsResult().getDomainName()) {
          Domain domain = new Domain(this, name);
          domains.add(domain);
        }
        return domains;
      }
      return ImmutableList.of();
    } catch (AmazonSimpleDBException e) {
      logger.fatal("Failed to list domains.", e);
      throw new SimpleDBException(e);
    }
  }
  
  /**
   * Starts performing the put batch request asynchronously.  The response
   * in the future is managed by another thread which will log any errors
   * as well as perform retries.
   * 
   * @param batchRequest The batch request.
   */
  void performAsync(BatchPutAttributesRequest batchRequest) {
    performAsync(Pair.of(batchRequest, DEFAULT_RETRIES));
  }
  
  /**
   * Performs asynchronous batch call and registers a waiter for the responses.
   * Retries if necessary.
   * 
   * @param attempt An attempt to invoke the batch service method.
   */
  private void performAsync(Pair<BatchPutAttributesRequest, Integer> attempt) {
    if (attempt.second < 0) {
      return;
    }
   
    // Call the batch service method.
    final Future<BatchPutAttributesResponse> future = 
      this.serviceAsync.batchPutAttributesAsync(attempt.first);

    // Construct the next attempt, in case we are going to retry.
    final Pair<BatchPutAttributesRequest, Integer> next =
      Pair.of(attempt.first, attempt.second - 1);
        
    this.retryService.submit(new Callable<Boolean>() {
      public Boolean call() throws Exception {
        boolean retry = false;
        while (!future.isDone()) {
          Thread.yield();
        }
        try {
         future.get();
        } catch (Exception e) {
          logger.warn("Exception with batch put:", e);
          retry = next.second > 0;
        }

        if (retry) {
          logger.warn("Retrying " + next.second);
          performAsync(next);
        }
        return retry;
      }
    });
  }
  
  public void stop() {
    try {
      this.clientService.shutdown();
      this.clientService.awaitTermination(10000L, TimeUnit.MILLISECONDS);
      this.retryService.shutdown();
      this.retryService.awaitTermination(20000L, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      
    }
  }
}
