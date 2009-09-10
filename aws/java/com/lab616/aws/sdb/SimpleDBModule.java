// 2009 lab616.com, All Rights Reserved.

package com.lab616.aws.sdb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.lab616.common.flags.Flag;
import com.lab616.common.flags.Flags;

/**
 * Guice module for SimpleDB
 *
 * @author david
 *
 */
public class SimpleDBModule implements Module {

  @Flag(name = "aws-sdb-accessKeyId", required = true)
  public static String accessKeyId;
  
  @Flag(name = "aws-sdb-secretAccessKey", required = true)
  public static String secretAccessKey;
  
  @Flag(name = "aws-sdb-max-connections")
  public static Integer maxConnections = 60;

  static {
    Flags.register(SimpleDBModule.class);
  }
  
  public void configure(Binder binder) {
    binder.bindConstant()
      .annotatedWith(Names.named("aws-sdb-accessKeyId"))
      .to(accessKeyId);
    
    binder.bindConstant()
      .annotatedWith(Names.named("aws-sdb-secretAccessKey"))
      .to(secretAccessKey);
    
    binder.bindConstant()
      .annotatedWith(Names.named("aws-sdb-max-connections"))
      .to(maxConnections);
   
    binder.bind(ExecutorService.class)
      .annotatedWith(Names.named("aws-sdb-threadPool"))
      .toProvider(new Provider<ExecutorService> () {
        public ExecutorService get() {
          return Executors.newFixedThreadPool(maxConnections);
        }
      }).in(Scopes.SINGLETON);

    binder.bind(ExecutorService.class)
      .annotatedWith(Names.named("aws-sdb-put-retry"))
      .toProvider(new Provider<ExecutorService> () {
        public ExecutorService get() {
          return Executors.newFixedThreadPool(10);
        }
      }).in(Scopes.SINGLETON);
    
    binder.bind(SimpleDB.class).in(Scopes.SINGLETON);
  }
}
