/******************************************************************************* 
 *  Copyright 2008 Amazon Technologies, Inc.
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  
 *  You may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 *  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 * ***************************************************************************** 
 *    __  _    _  ___ 
 *   (  )( \/\/ )/ __)
 *   /__\ \    / \__ \
 *  (_)(_) \/\/  (___/
 * 
 *  Amazon Simple DB Java Library
 *  API Version: 2009-04-15
 *  Generated: Mon May 11 14:17:00 PDT 2009 
 * 
 */



package com.amazonaws.sdb.samples;

import java.util.List;
import java.util.ArrayList;
import com.amazonaws.sdb.*;
import com.amazonaws.sdb.model.*;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 *
 * List Domains  Samples
 *
 *
 */
public class ListDomainsAsyncSample {

    /**
     * Just add few required parameters, and try the service
     * List Domains functionality
     *
     * @param args unused
     */
    public static void main(String... args) {
        
        /************************************************************************
         * Access Key ID and Secret Acess Key ID, obtained from:
         * http://aws.amazon.com
         ***********************************************************************/
         String accessKeyId = "<Your Access Key ID>";
         String secretAccessKey = "<Your Secret Access Key>";

        /************************************************************************
         * Instantiate Http Client Implementation of Amazon Simple DB 
         * 
         * Important! Number of threads in executor should match number of connections
         * for http client.
         *
         ***********************************************************************/

         AmazonSimpleDBConfig config = new AmazonSimpleDBConfig().withMaxConnections (100);
         ExecutorService executor = Executors.newFixedThreadPool(100);
         AmazonSimpleDBAsync service = new AmazonSimpleDBAsyncClient(accessKeyId, secretAccessKey, config, executor);

        /************************************************************************
         * Setup requests parameters and invoke parallel processing. Of course
         * in real world application, there will be much more than a couple of
         * requests to process.
         ***********************************************************************/
         ListDomainsRequest requestOne = new ListDomainsRequest();
         // @TODO: set request parameters here

         ListDomainsRequest requestTwo = new ListDomainsRequest();
         // @TODO: set second request parameters here

         List<ListDomainsRequest> requests = new ArrayList<ListDomainsRequest>();
         requests.add(requestOne);
         requests.add(requestTwo);

         invokeListDomains(service, requests);

         executor.shutdown();

    }


                            
    /**
     * List Domains request sample
     * The ListDomains operation lists all domains associated with the Access Key ID. It returns
     * domain names up to the limit set by MaxNumberOfDomains. A NextToken is returned if there are more
     * than MaxNumberOfDomains domains. Calling ListDomains successive times with the
     * NextToken returns up to MaxNumberOfDomains more domain names each time.
     *   
     * @param service instance of AmazonSimpleDB service
     * @param requests list of requests to process
     */
    public static void invokeListDomains(AmazonSimpleDBAsync service, List<ListDomainsRequest> requests) {
        List<Future<ListDomainsResponse>> responses = new ArrayList<Future<ListDomainsResponse>>();
        for (ListDomainsRequest request : requests) {
            responses.add(service.listDomainsAsync(request));
        }
        for (Future<ListDomainsResponse> future : responses) {
            while (!future.isDone()) {
                Thread.yield();
            }
            try {
                ListDomainsResponse response = future.get();
                // Original request corresponding to this response, if needed:
                ListDomainsRequest originalRequest = requests.get(responses.indexOf(future));
                System.out.println("Response request id: " + response.getResponseMetadata().getRequestId());
            } catch (Exception e) {
                if (e.getCause() instanceof AmazonSimpleDBException) {
                    AmazonSimpleDBException exception = AmazonSimpleDBException.class.cast(e.getCause());
                    System.out.println("Caught Exception: " + exception.getMessage());
                    System.out.println("Response Status Code: " + exception.getStatusCode());
                    System.out.println("Error Code: " + exception.getErrorCode());
                    System.out.println("Error Type: " + exception.getErrorType());
                    System.out.println("Request ID: " + exception.getRequestId());
                    System.out.print("XML: " + exception.getXML());
                } else {
                    e.printStackTrace();
                }
            }
        }
    }
                                
}
