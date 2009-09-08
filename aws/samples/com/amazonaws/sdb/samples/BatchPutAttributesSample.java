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
import com.amazonaws.sdb.mock.AmazonSimpleDBMock;

/**
 *
 * Batch Put Attributes  Samples
 *
 *
 */
public class BatchPutAttributesSample {

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
         ***********************************************************************/
        AmazonSimpleDB service = new AmazonSimpleDBClient(accessKeyId, secretAccessKey);
        
        /************************************************************************
         * Uncomment to try advanced configuration options. Available options are:
         *
         *  - Signature Version
         *  - Proxy Host and Proxy Port
         *  - Service URL
         *  - User Agent String to be sent to Amazon Simple DB   service
         *
         ***********************************************************************/
        // AmazonSimpleDBConfig config = new AmazonSimpleDBConfig();
        // config.setSignatureVersion("0");
        // AmazonSimpleDB service = new AmazonSimpleDBClient(accessKeyId, secretAccessKey, config);
 
        /************************************************************************
         * Uncomment to try out Mock Service that simulates Amazon Simple DB 
         * responses without calling Amazon Simple DB  service.
         *
         * Responses are loaded from local XML files. You can tweak XML files to
         * experiment with various outputs during development
         *
         * XML files available under com/amazonaws/sdb/mock tree
         *
         ***********************************************************************/
        // AmazonSimpleDB service = new AmazonSimpleDBMock();

        /************************************************************************
         * Setup request parameters and uncomment invoke to try out 
         * sample for Batch Put Attributes 
         ***********************************************************************/
         BatchPutAttributesRequest request = new BatchPutAttributesRequest();
        
         // @TODO: set request parameters here

         // invokeBatchPutAttributes(service, request);

    }


                                            
    /**
     * Batch Put Attributes  request sample
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
     * @param request Action to invoke
     */
    public static void invokeBatchPutAttributes(AmazonSimpleDB service, BatchPutAttributesRequest request) {
        try {
            
            BatchPutAttributesResponse response = service.batchPutAttributes(request);

            
            System.out.println ("BatchPutAttributes Action Response");
            System.out.println ("=============================================================================");
            System.out.println ();

            System.out.println("    BatchPutAttributesResponse");
            System.out.println();
            if (response.isSetResponseMetadata()) {
                System.out.println("        ResponseMetadata");
                System.out.println();
                ResponseMetadata  responseMetadata = response.getResponseMetadata();
                if (responseMetadata.isSetRequestId()) {
                    System.out.println("            RequestId");
                    System.out.println();
                    System.out.println("                " + responseMetadata.getRequestId());
                    System.out.println();
                }
                if (responseMetadata.isSetBoxUsage()) {
                    System.out.println("            BoxUsage");
                    System.out.println();
                    System.out.println("                " + responseMetadata.getBoxUsage());
                    System.out.println();
                }
            } 
            System.out.println();

           
        } catch (AmazonSimpleDBException ex) {
            
            System.out.println("Caught Exception: " + ex.getMessage());
            System.out.println("Response Status Code: " + ex.getStatusCode());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("Error Type: " + ex.getErrorType());
            System.out.println("Request ID: " + ex.getRequestId());
            System.out.print("XML: " + ex.getXML());
        }
    }
                
}
