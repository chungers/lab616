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
 * Batch Put Attributes  Samples
 *
 *
 */
public class BatchPutAttributesAsyncSample {

    /**
     * Just add few required parameters, and try the service
     * Batch Put Attributes functionality
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
         BatchPutAttributesRequest requestOne = new BatchPutAttributesRequest();
         // @TODO: set request parameters here

         BatchPutAttributesRequest requestTwo = new BatchPutAttributesRequest();
         // @TODO: set second request parameters here

         List<BatchPutAttributesRequest> requests = new ArrayList<BatchPutAttributesRequest>();
         requests.add(requestOne);
         requests.add(requestTwo);

         invokeBatchPutAttributes(service, requests);

         executor.shutdown();

    }


                                            
    /**
     * Batch Put Attributes request sample
     * The BatchPutAttributes operation creates or replaces attributes within one or more items.
     * You specify the item name with the Item.X.ItemName parameter.
     * You specify new attributes using a combination of the Item.X.Attribute.Y.Name and Item.X.Attribute.Y.Value parameters.
     * You specify the first attribute for the first item by the parameters Item.0.Attribute.0.Name and Item.0.Attribute.0.Value,
     * the second attribute for the first item by the parameters Item.0.Attribute.1.Name and Item.0.Attribute.1.Value, and so on.
     * Attributes are uniquely identified within an item by their name/value combination. For example, a single
     * item can have the attributes { "first_name", "first_value" } and { "first_name",
     * second_value" }. However, it cannot have two attribute instances where both the Item.X.Attribute.Y.Name and
     * Item.X.Attribute.Y.Value are the same.
     * Optionally, the requestor can supply the Replace parameter for each individual value. Setting this value
     * to true will cause the new attribute value to replace the existing attribute value(s). For example, if an
     * item 'I' has the attributes { 'a', '1' }, { 'b', '2'} and { 'b', '3' } and the requestor does a
     * BacthPutAttributes of {'I', 'b', '4' } with the Replace parameter set to true, the final attributes of the
     * item will be { 'a', '1' } and { 'b', '4' }, replacing the previous values of the 'b' attribute
     * with the new value.
     *   
     * @param service instance of AmazonSimpleDB service
     * @param requests list of requests to process
     */
    public static void invokeBatchPutAttributes(AmazonSimpleDBAsync service, List<BatchPutAttributesRequest> requests) {
        List<Future<BatchPutAttributesResponse>> responses = new ArrayList<Future<BatchPutAttributesResponse>>();
        for (BatchPutAttributesRequest request : requests) {
            responses.add(service.batchPutAttributesAsync(request));
        }
        for (Future<BatchPutAttributesResponse> future : responses) {
            while (!future.isDone()) {
                Thread.yield();
            }
            try {
                BatchPutAttributesResponse response = future.get();
                // Original request corresponding to this response, if needed:
                BatchPutAttributesRequest originalRequest = requests.get(responses.indexOf(future));
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
