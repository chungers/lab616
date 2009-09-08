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
 * Domain Metadata  Samples
 *
 *
 */
public class DomainMetadataSample {

    /**
     * Just add few required parameters, and try the service
     * Domain Metadata functionality
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
         * sample for Domain Metadata 
         ***********************************************************************/
         DomainMetadataRequest request = new DomainMetadataRequest();
        
         // @TODO: set request parameters here

         // invokeDomainMetadata(service, request);

    }


                                
    /**
     * Domain Metadata  request sample
     * The DomainMetadata operation returns some domain metadata values, such as the
     * number of items, attribute names and attribute values along with their sizes.
     *   
     * @param service instance of AmazonSimpleDB service
     * @param request Action to invoke
     */
    public static void invokeDomainMetadata(AmazonSimpleDB service, DomainMetadataRequest request) {
        try {
            
            DomainMetadataResponse response = service.domainMetadata(request);

            
            System.out.println ("DomainMetadata Action Response");
            System.out.println ("=============================================================================");
            System.out.println ();

            System.out.println("    DomainMetadataResponse");
            System.out.println();
            if (response.isSetDomainMetadataResult()) {
                System.out.println("        DomainMetadataResult");
                System.out.println();
                DomainMetadataResult  domainMetadataResult = response.getDomainMetadataResult();
                if (domainMetadataResult.isSetItemCount()) {
                    System.out.println("            ItemCount");
                    System.out.println();
                    System.out.println("                " + domainMetadataResult.getItemCount());
                    System.out.println();
                }
                if (domainMetadataResult.isSetItemNamesSizeBytes()) {
                    System.out.println("            ItemNamesSizeBytes");
                    System.out.println();
                    System.out.println("                " + domainMetadataResult.getItemNamesSizeBytes());
                    System.out.println();
                }
                if (domainMetadataResult.isSetAttributeNameCount()) {
                    System.out.println("            AttributeNameCount");
                    System.out.println();
                    System.out.println("                " + domainMetadataResult.getAttributeNameCount());
                    System.out.println();
                }
                if (domainMetadataResult.isSetAttributeNamesSizeBytes()) {
                    System.out.println("            AttributeNamesSizeBytes");
                    System.out.println();
                    System.out.println("                " + domainMetadataResult.getAttributeNamesSizeBytes());
                    System.out.println();
                }
                if (domainMetadataResult.isSetAttributeValueCount()) {
                    System.out.println("            AttributeValueCount");
                    System.out.println();
                    System.out.println("                " + domainMetadataResult.getAttributeValueCount());
                    System.out.println();
                }
                if (domainMetadataResult.isSetAttributeValuesSizeBytes()) {
                    System.out.println("            AttributeValuesSizeBytes");
                    System.out.println();
                    System.out.println("                " + domainMetadataResult.getAttributeValuesSizeBytes());
                    System.out.println();
                }
                if (domainMetadataResult.isSetTimestamp()) {
                    System.out.println("            Timestamp");
                    System.out.println();
                    System.out.println("                " + domainMetadataResult.getTimestamp());
                    System.out.println();
                }
            } 
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
