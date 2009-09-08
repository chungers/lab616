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



package com.amazonaws.sdb;

import com.amazonaws.sdb.model.*;



/**
 * Amazon SimpleDB is a web service for running queries on structured
 * data in real time. This service works in close conjunction with Amazon
 * Simple Storage Service (Amazon S3) and Amazon Elastic Compute Cloud
 * (Amazon EC2), collectively providing the ability to store, process
 * and query data sets in the cloud. These services are designed to make
 * web-scale computing easier and more cost-effective for developers.
 * Traditionally, this type of functionality has been accomplished with
 * a clustered relational database that requires a sizable upfront
 * investment, brings more complexity than is typically needed, and often
 * requires a DBA to maintain and administer. In contrast, Amazon SimpleDB
 * is easy to use and provides the core functionality of a database -
 * real-time lookup and simple querying of structured data without the
 * operational complexity.  Amazon SimpleDB requires no schema, automatically
 * indexes your data and provides a simple API for storage and access.
 * This eliminates the administrative burden of data modeling, index
 * maintenance, and performance tuning. Developers gain access to this
 * functionality within Amazon's proven computing environment, are able
 * to scale instantly, and pay only for what they use.
 * 
 * 
 */
public interface  AmazonSimpleDB {
    

            
    /**
     * Create Domain 
     *
     * The CreateDomain operation creates a new domain. The domain name must be unique
     * among the domains associated with the Access Key ID provided in the request. The CreateDomain
     * operation may take 10 or more seconds to complete.
     *   
     * @param request
     *          CreateDomain Action
     * @return
     *          CreateDomain Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public CreateDomainResponse createDomain(CreateDomainRequest request) throws AmazonSimpleDBException;


            
    /**
     * List Domains 
     *
     * The ListDomains operation lists all domains associated with the Access Key ID. It returns
     * domain names up to the limit set by MaxNumberOfDomains. A NextToken is returned if there are more
     * than MaxNumberOfDomains domains. Calling ListDomains successive times with the
     * NextToken returns up to MaxNumberOfDomains more domain names each time.
     *   
     * @param request
     *          ListDomains Action
     * @return
     *          ListDomains Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public ListDomainsResponse listDomains(ListDomainsRequest request) throws AmazonSimpleDBException;


            
    /**
     * Domain Metadata 
     *
     * The DomainMetadata operation returns some domain metadata values, such as the
     * number of items, attribute names and attribute values along with their sizes.
     *   
     * @param request
     *          DomainMetadata Action
     * @return
     *          DomainMetadata Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public DomainMetadataResponse domainMetadata(DomainMetadataRequest request) throws AmazonSimpleDBException;


            
    /**
     * Delete Domain 
     *
     * The DeleteDomain operation deletes a domain. Any items (and their attributes) in the domain
     * are deleted as well. The DeleteDomain operation may take 10 or more seconds to complete.
     *   
     * @param request
     *          DeleteDomain Action
     * @return
     *          DeleteDomain Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public DeleteDomainResponse deleteDomain(DeleteDomainRequest request) throws AmazonSimpleDBException;


            
    /**
     * Put Attributes 
     *
     * The PutAttributes operation creates or replaces attributes within an item. You specify new attributes
     * using a combination of the Attribute.X.Name and Attribute.X.Value parameters. You specify
     * the first attribute by the parameters Attribute.0.Name and Attribute.0.Value, the second
     * attribute by the parameters Attribute.1.Name and Attribute.1.Value, and so on.
     * Attributes are uniquely identified within an item by their name/value combination. For example, a single
     * item can have the attributes { "first_name", "first_value" } and { "first_name",
     * second_value" }. However, it cannot have two attribute instances where both the Attribute.X.Name and
     * Attribute.X.Value are the same.
     * Optionally, the requestor can supply the Replace parameter for each individual value. Setting this value
     * to true will cause the new attribute value to replace the existing attribute value(s). For example, if an
     * item has the attributes { 'a', '1' }, { 'b', '2'} and { 'b', '3' } and the requestor does a
     * PutAttributes of { 'b', '4' } with the Replace parameter set to true, the final attributes of the
     * item will be { 'a', '1' } and { 'b', '4' }, replacing the previous values of the 'b' attribute
     * with the new value.
     *   
     * @param request
     *          PutAttributes Action
     * @return
     *          PutAttributes Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public PutAttributesResponse putAttributes(PutAttributesRequest request) throws AmazonSimpleDBException;


            
    /**
     * Batch Put Attributes 
     *
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
     * @param request
     *          BatchPutAttributes Action
     * @return
     *          BatchPutAttributes Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public BatchPutAttributesResponse batchPutAttributes(BatchPutAttributesRequest request) throws AmazonSimpleDBException;


            
    /**
     * Get Attributes 
     *
     * Returns all of the attributes associated with the item. Optionally, the attributes returned can be limited to
     * the specified AttributeName parameter.
     * If the item does not exist on the replica that was accessed for this operation, an empty attribute is
     * returned. The system does not return an error as it cannot guarantee the item does not exist on other
     * replicas.
     *   
     * @param request
     *          GetAttributes Action
     * @return
     *          GetAttributes Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public GetAttributesResponse getAttributes(GetAttributesRequest request) throws AmazonSimpleDBException;


            
    /**
     * Delete Attributes 
     *
     * Deletes one or more attributes associated with the item. If all attributes of an item are deleted, the item is
     * deleted.
     *   
     * @param request
     *          DeleteAttributes Action
     * @return
     *          DeleteAttributes Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public DeleteAttributesResponse deleteAttributes(DeleteAttributesRequest request) throws AmazonSimpleDBException;


            
    /**
     * Select 
     *
     * The Select operation returns a set of item names and associate attributes that match the
     * query expression. Select operations that run longer than 5 seconds will likely time-out
     * and return a time-out error response.
     *   
     * @param request
     *          Select Action
     * @return
     *          Select Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public SelectResponse select(SelectRequest request) throws AmazonSimpleDBException;



}