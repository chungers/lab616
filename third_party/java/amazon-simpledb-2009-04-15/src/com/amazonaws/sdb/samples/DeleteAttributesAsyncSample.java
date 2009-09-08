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
 * Delete Attributes  Samples
 *
 *
 */
public class DeleteAttributesAsyncSample {

    /**
     * Just add few required parameters, and try the service
     * Delete Attributes functionality
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
         DeleteAttributesRequest requestOne = new DeleteAttributesRequest();
         // @TODO: set request parameters here

         DeleteAttributesRequest requestTwo = new DeleteAttributesRequest();
         // @TODO: set second request parameters here

         List<DeleteAttributesRequest> requests = new ArrayList<DeleteAttributesRequest>();
         requests.add(requestOne);
         requests.add(requestTwo);

         invokeDeleteAttributes(service, requests);

         executor.shutdown();

    }


                                                    
    /**
     * Delete Attributes request sample
     * Deletes one or more attributes associated with the item. If all attributes of an item are deleted, the item is
     * deleted.
     *   
     * @param service instance of AmazonSimpleDB service
     * @param requests list of requests to process
     */
    public static void invokeDeleteAttributes(AmazonSimpleDBAsync service, List<DeleteAttributesRequest> requests) {
        List<Future<DeleteAttributesResponse>> responses = new ArrayList<Future<DeleteAttributesResponse>>();
        for (DeleteAttributesRequest request : requests) {
            responses.add(service.deleteAttributesAsync(request));
        }
        for (Future<DeleteAttributesResponse> future : responses) {
            while (!future.isDone()) {
                Thread.yield();
            }
            try {
                DeleteAttributesResponse response = future.get();
                // Original request corresponding to this response, if needed:
                DeleteAttributesRequest originalRequest = requests.get(responses.indexOf(future));
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
