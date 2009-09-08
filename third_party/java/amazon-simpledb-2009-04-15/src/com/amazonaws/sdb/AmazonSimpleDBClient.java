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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SignatureException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import javax.xml.transform.stream.StreamSource;




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
 *
 * AmazonSimpleDBClient is implementation of AmazonSimpleDB based on the
 * Apache <a href="http://jakarta.apache.org/commons/httpclient/">HttpClient</a>.
 *
 */
public  class AmazonSimpleDBClient implements AmazonSimpleDB {

    private final Log log = LogFactory.getLog(AmazonSimpleDBClient.class);

    private String awsAccessKeyId = null;
    private String awsSecretAccessKey = null;
    private AmazonSimpleDBConfig config = null;
    private HttpClient httpClient = null;
    private static JAXBContext  jaxbContext;
    private static ThreadLocal<Unmarshaller> unmarshaller;
    private static Pattern ERROR_PATTERN_ONE = Pattern.compile(".*\\<RequestId>(.*)\\</RequestId>.*\\<Error>" +
            "\\<Code>(.*)\\</Code>\\<Message>(.*)\\</Message>\\</Error>.*(\\<Error>)?.*",
            Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern ERROR_PATTERN_TWO = Pattern.compile(".*\\<Error>\\<Code>(.*)\\</Code>\\<Message>(.*)" +
            "\\</Message>\\</Error>.*(\\<Error>)?.*\\<RequestID>(.*)\\</RequestID>.*",
            Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern ERROR_PATTERN_THREE = Pattern.compile(".*\\<Error>\\<Code>(.*)\\</Code>\\<Message>(.*)" +
            "\\</Message><BoxUsage>(.*)</BoxUsage>\\</Error>.*(\\<Error>)?.*\\<RequestID>(.*)\\</RequestID>.*",
            Pattern.MULTILINE | Pattern.DOTALL);
    private static String DEFAULT_ENCODING = "UTF-8";

    /** Initialize JAXBContext and  Unmarshaller **/
    static {
        try {
            jaxbContext = JAXBContext.newInstance("com.amazonaws.sdb.model", AmazonSimpleDB.class.getClassLoader());
        } catch (JAXBException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        unmarshaller = new ThreadLocal<Unmarshaller>() {
            @Override
            protected synchronized Unmarshaller initialValue() {
                try {
                    return jaxbContext.createUnmarshaller();
                } catch(JAXBException e) {
                    throw new ExceptionInInitializerError(e);
                }
            }
        };
    }


    /**
     * Constructs AmazonSimpleDBClient with AWS Access Key ID and AWS Secret Key
     *
     * @param awsAccessKeyId
     *          AWS Access Key ID
     * @param awsSecretAccessKey
     *          AWS Secret Access Key
     */
    public  AmazonSimpleDBClient(String awsAccessKeyId,String awsSecretAccessKey) {
        this (awsAccessKeyId, awsSecretAccessKey, new AmazonSimpleDBConfig());
    }


    /**
     * Constructs AmazonSimpleDBClient with AWS Access Key ID, AWS Secret Key
     * and AmazonSimpleDBConfig. Use AmazonSimpleDBConfig to pass additional
     * configuration that affects how service is being called.
     *
     * @param awsAccessKeyId
     *          AWS Access Key ID
     * @param awsSecretAccessKey
     *          AWS Secret Access Key
     * @param config
     *          Additional configuration options
     */
    public  AmazonSimpleDBClient(String awsAccessKeyId, String awsSecretAccessKey,
            AmazonSimpleDBConfig config) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.config = config;
        this.httpClient = configureHttpClient();
    }

    // Public API ------------------------------------------------------------//


        
    /**
     * Create Domain 
     *
     * The CreateDomain operation creates a new domain. The domain name must be unique
     * among the domains associated with the Access Key ID provided in the request. The CreateDomain
     * operation may take 10 or more seconds to complete.
     * 
     * @param request
     *          CreateDomainRequest request
     * @return
     *          CreateDomain Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public CreateDomainResponse createDomain(CreateDomainRequest request) throws AmazonSimpleDBException {

        return invoke(CreateDomainResponse.class, convertCreateDomain(request));
    }


        
    /**
     * List Domains 
     *
     * The ListDomains operation lists all domains associated with the Access Key ID. It returns
     * domain names up to the limit set by MaxNumberOfDomains. A NextToken is returned if there are more
     * than MaxNumberOfDomains domains. Calling ListDomains successive times with the
     * NextToken returns up to MaxNumberOfDomains more domain names each time.
     * 
     * @param request
     *          ListDomainsRequest request
     * @return
     *          ListDomains Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public ListDomainsResponse listDomains(ListDomainsRequest request) throws AmazonSimpleDBException {

        return invoke(ListDomainsResponse.class, convertListDomains(request));
    }


        
    /**
     * Domain Metadata 
     *
     * The DomainMetadata operation returns some domain metadata values, such as the
     * number of items, attribute names and attribute values along with their sizes.
     * 
     * @param request
     *          DomainMetadataRequest request
     * @return
     *          DomainMetadata Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public DomainMetadataResponse domainMetadata(DomainMetadataRequest request) throws AmazonSimpleDBException {

        return invoke(DomainMetadataResponse.class, convertDomainMetadata(request));
    }


        
    /**
     * Delete Domain 
     *
     * The DeleteDomain operation deletes a domain. Any items (and their attributes) in the domain
     * are deleted as well. The DeleteDomain operation may take 10 or more seconds to complete.
     * 
     * @param request
     *          DeleteDomainRequest request
     * @return
     *          DeleteDomain Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public DeleteDomainResponse deleteDomain(DeleteDomainRequest request) throws AmazonSimpleDBException {

        return invoke(DeleteDomainResponse.class, convertDeleteDomain(request));
    }


        
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
     *          PutAttributesRequest request
     * @return
     *          PutAttributes Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public PutAttributesResponse putAttributes(PutAttributesRequest request) throws AmazonSimpleDBException {

        return invoke(PutAttributesResponse.class, convertPutAttributes(request));
    }


        
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
     *          BatchPutAttributesRequest request
     * @return
     *          BatchPutAttributes Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public BatchPutAttributesResponse batchPutAttributes(BatchPutAttributesRequest request) throws AmazonSimpleDBException {

        return invoke(BatchPutAttributesResponse.class, convertBatchPutAttributes(request));
    }


        
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
     *          GetAttributesRequest request
     * @return
     *          GetAttributes Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public GetAttributesResponse getAttributes(GetAttributesRequest request) throws AmazonSimpleDBException {

        return invoke(GetAttributesResponse.class, convertGetAttributes(request));
    }


        
    /**
     * Delete Attributes 
     *
     * Deletes one or more attributes associated with the item. If all attributes of an item are deleted, the item is
     * deleted.
     * 
     * @param request
     *          DeleteAttributesRequest request
     * @return
     *          DeleteAttributes Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public DeleteAttributesResponse deleteAttributes(DeleteAttributesRequest request) throws AmazonSimpleDBException {

        return invoke(DeleteAttributesResponse.class, convertDeleteAttributes(request));
    }


        
    /**
     * Select 
     *
     * The Select operation returns a set of item names and associate attributes that match the
     * query expression. Select operations that run longer than 5 seconds will likely time-out
     * and return a time-out error response.
     * 
     * @param request
     *          SelectRequest request
     * @return
     *          Select Response from the service
     *
     * @throws AmazonSimpleDBException
     */
    public SelectResponse select(SelectRequest request) throws AmazonSimpleDBException {

        return invoke(SelectResponse.class, convertSelect(request));
    }




    // Private API ------------------------------------------------------------//

    /**
     * Configure HttpClient with set of defaults as well as configuration
     * from AmazonSimpleDBConfig instance
     *
     */
    private HttpClient configureHttpClient() {

        /* Set http client parameters */
        HttpClientParams httpClientParams = new HttpClientParams();
        httpClientParams.setParameter(HttpMethodParams.USER_AGENT, config.getUserAgent());
        httpClientParams.setParameter(HttpClientParams.RETRY_HANDLER, new HttpMethodRetryHandler() {

            public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
                if (executionCount > 3) {
                    log.debug("Maximum Number of Retry attempts reached, will not retry");
                    return false;
                }
                log.debug("Retrying request. Attempt " + executionCount);
                if (exception instanceof NoHttpResponseException) {
                    log.debug("Retrying on NoHttpResponseException");
                    return true;
                }
                if (exception instanceof InterruptedIOException) {
                    log.debug("Will not retry on InterruptedIOException", exception);
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    log.debug("Will not retry on UnknownHostException", exception);
                    return false;
                }
                if (!method.isRequestSent()) {
                    log.debug("Retrying on failed sent request");
                    return true;
                }
                return false;
            }
        });

        /* Set host configuration */
        HostConfiguration hostConfiguration = new HostConfiguration();

        /* Set connection manager parameters */
        HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
        connectionManagerParams.setConnectionTimeout(50000);
        connectionManagerParams.setSoTimeout(50000);
        connectionManagerParams.setStaleCheckingEnabled(true);
        connectionManagerParams.setTcpNoDelay(true);
        connectionManagerParams.setMaxTotalConnections(100);
        connectionManagerParams.setMaxConnectionsPerHost(hostConfiguration, 100);

        /* Set connection manager */
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setParams(connectionManagerParams);

        /* Set http client */
        httpClient = new HttpClient(httpClientParams, connectionManager);

        /* Set proxy if configured */
        if (config.isSetProxyHost() && config.isSetProxyPort()) {
            log.info("Configuring Proxy. Proxy Host: " + config.getProxyHost() +
                    "Proxy Port: " + config.getProxyPort() );
            hostConfiguration.setProxy(config.getProxyHost(), config.getProxyPort());
            if (config.isSetProxyUsername() &&   config.isSetProxyPassword()) {
                httpClient.getState().setProxyCredentials (new AuthScope(
                                          config.getProxyHost(),
                                          config.getProxyPort()),
                                          new UsernamePasswordCredentials(
                                              config.getProxyUsername(),
                                              config.getProxyPassword()));

            }
         }

        httpClient.setHostConfiguration(hostConfiguration);
        return httpClient;
    }

    /**
     * Invokes request using parameters from parameters map.
     * Returns response of the T type passed to this method
     */
    private <T> T invoke(Class<T> clazz, Map<String, String> parameters)
            throws AmazonSimpleDBException {

        String actionName = parameters.get("Action");
        T response = null;
        String responseBodyString = null;
        PostMethod method = new PostMethod(config.getServiceURL());
        int status = -1;

        log.debug("Invoking" + actionName + " request. Current parameters: " + parameters);

        try {

            /* Set content type and encoding */
            log.debug("Setting content-type to application/x-www-form-urlencoded; charset=" + DEFAULT_ENCODING.toLowerCase());
            method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=" + DEFAULT_ENCODING.toLowerCase());

            /* Add required request parameters and set request body */
            log.debug("Adding required parameters...");
            addRequiredParametersToRequest(method, parameters);
            log.debug("Done adding additional required parameteres. Parameters now: " + parameters);

            boolean shouldRetry = true;
            int retries = 0;
            do {
                log.debug("Sending Request to host:  " + config.getServiceURL());

                try {

                    /* Submit request */
                    status = httpClient.executeMethod(method);



                    /* Consume response stream */
                    responseBodyString = getResponsBodyAsString(method.getResponseBodyAsStream());

                    /* Successful response. Attempting to unmarshal into the <Action>Response type */
                    if (status == HttpStatus.SC_OK) {
                        shouldRetry = false;
                        log.debug("Received Response. Status: " + status + ". " +
                                "Response Body: " + responseBodyString);
                        log.debug("Attempting to unmarshal into the " + actionName + "Response type...");
                        response = clazz.cast(getUnmarshaller().unmarshal(new StreamSource(new StringReader(responseBodyString))));

                        log.debug("Unmarshalled response into " + actionName + "Response type.");

                    } else { /* Unsucessful response. Attempting to unmarshall into ErrorResponse  type */

                        log.debug("Received Response. Status: " + status + ". " +
                                "Response Body: " + responseBodyString);

                        if ((status == HttpStatus.SC_INTERNAL_SERVER_ERROR
                            || status == HttpStatus.SC_SERVICE_UNAVAILABLE)
                            && pauseIfRetryNeeded(++retries)){
                            shouldRetry = true;
                        } else {
                            log.debug("Attempting to unmarshal into the ErrorResponse type...");
                            ErrorResponse errorResponse = (ErrorResponse) getUnmarshaller().unmarshal(new StreamSource(new StringReader(responseBodyString)));

                            log.debug("Unmarshalled response into the ErrorResponse type.");

                            com.amazonaws.sdb.model.Error error = errorResponse.getError().get(0);

                                    throw new AmazonSimpleDBException(error.getMessage(),
                                    status,
                                    error.getCode(),
                                    error.getType(),
                                    null, // not supported in current API
                                    errorResponse.getRequestId(),
                                    errorResponse.toXML());
                        }
                    }
                } catch (JAXBException je) {
                    /* Response cannot be unmarshalled neither as <Action>Response or ErrorResponse types.
                    Checking for other possible errors. */

                    log.debug ("Caught JAXBException", je);
                    log.debug("Response cannot be unmarshalled neither as " + actionName + "Response or ErrorResponse types." +
                            "Checking for other possible errors.");

                    AmazonSimpleDBException awse = processErrors(responseBodyString, status);

                    throw awse;

                } catch (IOException ioe) {
                    log.error("Caught IOException exception", ioe);
                    throw new AmazonSimpleDBException("Internal Error", ioe);
                } catch (Exception e) {
                    log.error("Caught Exception", e);
                    throw new AmazonSimpleDBException(e);
                } finally {
                    method.releaseConnection();
                }
            } while (shouldRetry);

        } catch (AmazonSimpleDBException se) {
            log.error("Caught AmazonSimpleDBException", se);
            throw se;

        } catch (Throwable t) {
            log.error("Caught Exception", t);
            throw new AmazonSimpleDBException(t);
        }
        return response;
    }

    /**
     * Read stream into string
     * @param input stream to read
     */
    private String getResponsBodyAsString(InputStream input) throws IOException {
        String responsBodyString = null;
        try {
            Reader reader = new InputStreamReader(input, DEFAULT_ENCODING);
            StringBuilder b = new StringBuilder();
            char[] c = new char[1024];
            int len;
            while (0 < (len = reader.read(c))) {
                b.append(c, 0, len);
            }
            responsBodyString = b.toString();
        } finally {
            input.close();
        }
        return responsBodyString;
    }

    /**
     * Exponential sleep on failed request. Sleeps and returns true if retry needed
     * @param retries current retry
     * @throws java.lang.InterruptedException
     */
    private boolean pauseIfRetryNeeded(int retries)
          throws InterruptedException {
        if (retries <= config.getMaxErrorRetry()) {
            long delay = (long) (Math.pow(4, retries) * 100L);
            log.debug("Retriable error detected, will retry in " + delay + "ms, attempt numer: " + retries);
            Thread.sleep(delay);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add authentication related and version parameter and set request body
     * with all of the parameters
     */
    private void addRequiredParametersToRequest(PostMethod method, Map<String, String> parameters)
            throws SignatureException {
        parameters.put("Version", config.getServiceVersion());
        parameters.put("SignatureVersion", config.getSignatureVersion());
        parameters.put("Timestamp", getFormattedTimestamp());
        parameters.put("AWSAccessKeyId",  this.awsAccessKeyId);
        parameters.put("Signature", signParameters(parameters, this.awsSecretAccessKey));
        for (Entry<String, String> entry : parameters.entrySet()) {
            method.addParameter(entry.getKey(), entry.getValue());
        }
    }

    private AmazonSimpleDBException processErrors(String responseString, int status)  {
        AmazonSimpleDBException ex = null;
        Matcher matcher = null;
        if (responseString != null && responseString.startsWith("<")) {
            matcher = ERROR_PATTERN_ONE.matcher(responseString);
            if (matcher.matches()) {
                ex = new AmazonSimpleDBException(matcher.group(3), status,
                        matcher.group(2), "Unknown", null, matcher.group(1), responseString);
            } else {
                matcher = ERROR_PATTERN_TWO.matcher(responseString);
                if (matcher.matches()) {
                    ex = new AmazonSimpleDBException(matcher.group(2), status,
                            matcher.group(1), "Unknown", null, matcher.group(4), responseString);
                } else {
                    matcher = ERROR_PATTERN_THREE.matcher(responseString);
                    if (matcher.matches()) {
                log.error("Error found in the response: " + responseString);
                        ex = new AmazonSimpleDBException(matcher.group(2), status,
                                matcher.group(1), "Unknown", matcher.group(3), matcher.group(5), responseString);
            } else {
                ex =  new AmazonSimpleDBException("Internal Error", status);
                log.error("Service Error. Response Status: " + status);
            }
            }
            }
        } else {
            ex =  new AmazonSimpleDBException("Internal Error", status);
            log.error("Service Error. Response Status: " + status);
        }
        return ex;
    }

    /**
     * Formats date as ISO 8601 timestamp
     */
    private String getFormattedTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date());
    }

    /**
     * Computes RFC 2104-compliant HMAC signature for request parameters
     * Implements AWS Signature, as per following spec:
     *
     * If Signature Version is 0, it signs concatenated Action and Timestamp
     *
     * If Signature Version is 1, it performs the following:
     *
     * Sorts all  parameters (including SignatureVersion and excluding Signature,
     * the value of which is being created), ignoring case.
     *
     * Iterate over the sorted list and append the parameter name (in original case)
     * and then its value. It will not URL-encode the parameter values before
     * constructing this string. There are no separators.
     *
     * If Signature Version is 2, string to sign is based on following:
     *
     *    1. The HTTP Request Method followed by an ASCII newline (%0A)
     *    2. The HTTP Host header in the form of lowercase host, followed by an ASCII newline.
     *    3. The URL encoded HTTP absolute path component of the URI
     *       (up to but not including the query string parameters);
     *       if this is empty use a forward '/'. This parameter is followed by an ASCII newline.
     *    4. The concatenation of all query string components (names and values)
     *       as UTF-8 characters which are URL encoded as per RFC 3986
     *       (hex characters MUST be uppercase), sorted using lexicographic byte ordering.
     *       Parameter names are separated from their values by the '=' character
     *       (ASCII character 61), even if the value is empty.
     *       Pairs of parameter and values are separated by the '&' character (ASCII code 38).
     *
     */
    private String signParameters(Map<String, String> parameters, String key)
            throws  SignatureException {

        String signatureVersion = parameters.get("SignatureVersion");
        String algorithm = "HmacSHA1";
        String stringToSign = null;
        if ("0".equals(signatureVersion)) {
            stringToSign = calculateStringToSignV0(parameters);
        } else if ("1".equals(signatureVersion)) {
            stringToSign = calculateStringToSignV1(parameters);
        } else if ("2".equals(signatureVersion)) {
            algorithm = config.getSignatureMethod();
            parameters.put("SignatureMethod", algorithm);
            stringToSign = calculateStringToSignV2(parameters);
        } else {
            throw new SignatureException("Invalid Signature Version specified");
        }
        log.debug("Calculated string to sign: " + stringToSign);
        return sign(stringToSign, key, algorithm);
    }

    /**
     * Calculate String to Sign for SignatureVersion 0
     * @param parameters request parameters
     * @return String to Sign
     * @throws java.security.SignatureException
     */
    private String calculateStringToSignV0(Map<String, String> parameters) {
        StringBuilder data = new StringBuilder();
            data.append(parameters.get("Action")).append(parameters.get("Timestamp"));
        return data.toString();
    }

    /**
     * Calculate String to Sign for SignatureVersion 1
     * @param parameters request parameters
     * @return String to Sign
     * @throws java.security.SignatureException
     */
    private String calculateStringToSignV1(Map<String, String> parameters) {
        StringBuilder data = new StringBuilder();
            Map<String, String> sorted =  new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
            sorted.putAll(parameters);
            Iterator pairs = sorted.entrySet().iterator();
            while (pairs.hasNext()) {
                Map.Entry pair = (Map.Entry)pairs.next();
                data.append(pair.getKey());
                data.append(pair.getValue());
            }
        return data.toString();
    }

    /**
     * Calculate String to Sign for SignatureVersion 2
     * @param parameters request parameters
     * @return String to Sign
     * @throws java.security.SignatureException
     */
    private String calculateStringToSignV2(Map<String, String> parameters)
            throws SignatureException {
        StringBuilder data = new StringBuilder();
        data.append("POST");
        data.append("\n");
        URI endpoint = null;
        try {
            endpoint = new URI(config.getServiceURL().toLowerCase());
        } catch (URISyntaxException ex) {
            log.error("URI Syntax Exception", ex);
            throw new SignatureException("URI Syntax Exception thrown " +
                    "while constructing string to sign", ex);
        }
        data.append(endpoint.getHost());
        data.append("\n");
        String uri = endpoint.getPath();
        if (uri == null || uri.length() == 0) {
            uri = "/";
        }
        data.append(urlEncode(uri, true));
        data.append("\n");
        Map<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(parameters);
        Iterator<Map.Entry<String, String>> pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            String key = pair.getKey();
            data.append(urlEncode(key, false));
            data.append("=");
            String value = pair.getValue();
            data.append(urlEncode(value, false));
            if (pairs.hasNext()) {
                data.append("&");
            }
        }
        return data.toString();
    }

    private String urlEncode(String value, boolean path) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, DEFAULT_ENCODING)
                                        .replace("+", "%20")
                                        .replace("*", "%2A")
                                        .replace("%7E","~");
            if (path) {
                encoded = encoded.replace("%2F", "/");
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unsupported Encoding Exception", ex);
            throw new RuntimeException(ex);
        }
        return encoded;
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     */
    private String sign(String data, String key, String algorithm) throws SignatureException {
        byte [] signature;
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.getBytes(), algorithm));
            signature = Base64.encodeBase64(mac.doFinal(data.getBytes(DEFAULT_ENCODING)));
        } catch (Exception e) {
            throw new SignatureException("Failed to generate signature: " + e.getMessage(), e);
        }

        return new String(signature);
    }

    /**
     * Get unmarshaller for current thread
     */
    private Unmarshaller getUnmarshaller() {
        return unmarshaller.get();
    }
    
    
    
    
    
    
    
                    
   /**
     * Convert CreateDomainRequest to name value pairs
     */
    private Map<String, String> convertCreateDomain(CreateDomainRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "CreateDomain");
        if (request.isSetDomainName()) {
            params.put("DomainName", request.getDomainName());
        }

        return params;
    }
        
        
    
                    
   /**
     * Convert ListDomainsRequest to name value pairs
     */
    private Map<String, String> convertListDomains(ListDomainsRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "ListDomains");
        if (request.isSetMaxNumberOfDomains()) {
            params.put("MaxNumberOfDomains", request.getMaxNumberOfDomains() + "");
        }
        if (request.isSetNextToken()) {
            params.put("NextToken", request.getNextToken());
        }

        return params;
    }
        
        
    
    
                    
   /**
     * Convert DomainMetadataRequest to name value pairs
     */
    private Map<String, String> convertDomainMetadata(DomainMetadataRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "DomainMetadata");
        if (request.isSetDomainName()) {
            params.put("DomainName", request.getDomainName());
        }

        return params;
    }
        
        
    
    
                    
   /**
     * Convert DeleteDomainRequest to name value pairs
     */
    private Map<String, String> convertDeleteDomain(DeleteDomainRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "DeleteDomain");
        if (request.isSetDomainName()) {
            params.put("DomainName", request.getDomainName());
        }

        return params;
    }
        
        
    
                    
   /**
     * Convert PutAttributesRequest to name value pairs
     */
    private Map<String, String> convertPutAttributes(PutAttributesRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "PutAttributes");
        if (request.isSetDomainName()) {
            params.put("DomainName", request.getDomainName());
        }
        if (request.isSetItemName()) {
            params.put("ItemName", request.getItemName());
        }
        java.util.List<ReplaceableAttribute> attributeList = request.getAttribute();
        int attributeListIndex = 1;
        for (ReplaceableAttribute attribute : attributeList) {
            if (attribute.isSetName()) {
                params.put("Attribute" + "."  + attributeListIndex + "." + "Name", attribute.getName());
            }
            if (attribute.isSetValue()) {
                params.put("Attribute" + "."  + attributeListIndex + "." + "Value", attribute.getValue());
            }
            if (attribute.isSetReplace()) {
                params.put("Attribute" + "."  + attributeListIndex + "." + "Replace", attribute.isReplace() + "");
            }

            attributeListIndex++;
        }

        return params;
    }
        
        
    
                    
   /**
     * Convert BatchPutAttributesRequest to name value pairs
     */
    private Map<String, String> convertBatchPutAttributes(BatchPutAttributesRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "BatchPutAttributes");
        if (request.isSetDomainName()) {
            params.put("DomainName", request.getDomainName());
        }
        java.util.List<ReplaceableItem> itemList = request.getItem();
        int itemListIndex = 1;
        for (ReplaceableItem item : itemList) {
            if (item.isSetItemName()) {
                params.put("Item" + "."  + itemListIndex + "." + "ItemName", item.getItemName());
            }
            java.util.List<ReplaceableAttribute> attributeList = item.getAttribute();
            int attributeListIndex = 1;
            for (ReplaceableAttribute attribute : attributeList) {
                if (attribute.isSetName()) {
                    params.put("Item" + "."  + itemListIndex + "." + "Attribute" + "."  + attributeListIndex + "." + "Name", attribute.getName());
                }
                if (attribute.isSetValue()) {
                    params.put("Item" + "."  + itemListIndex + "." + "Attribute" + "."  + attributeListIndex + "." + "Value", attribute.getValue());
                }
                if (attribute.isSetReplace()) {
                    params.put("Item" + "."  + itemListIndex + "." + "Attribute" + "."  + attributeListIndex + "." + "Replace", attribute.isReplace() + "");
                }

                attributeListIndex++;
            }

            itemListIndex++;
        }

        return params;
    }
        
        
    
                    
   /**
     * Convert GetAttributesRequest to name value pairs
     */
    private Map<String, String> convertGetAttributes(GetAttributesRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "GetAttributes");
        if (request.isSetDomainName()) {
            params.put("DomainName", request.getDomainName());
        }
        if (request.isSetItemName()) {
            params.put("ItemName", request.getItemName());
        }
        java.util.List<String> attributeNameList  =  request.getAttributeName();
        int attributeNameListIndex = 1;
        for  (String attributeName : attributeNameList) { 
            params.put("AttributeName" + "."  + attributeNameListIndex, attributeName);
            attributeNameListIndex++;
        }	

        return params;
    }
        
        
    
    
                    
   /**
     * Convert DeleteAttributesRequest to name value pairs
     */
    private Map<String, String> convertDeleteAttributes(DeleteAttributesRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "DeleteAttributes");
        if (request.isSetDomainName()) {
            params.put("DomainName", request.getDomainName());
        }
        if (request.isSetItemName()) {
            params.put("ItemName", request.getItemName());
        }
        java.util.List<Attribute> attributeList = request.getAttribute();
        int attributeListIndex = 1;
        for (Attribute attribute : attributeList) {
            if (attribute.isSetName()) {
                params.put("Attribute" + "."  + attributeListIndex + "." + "Name", attribute.getName());
            }
            if (attribute.isSetValue()) {
                params.put("Attribute" + "."  + attributeListIndex + "." + "Value", attribute.getValue());
            }

            attributeListIndex++;
        }

        return params;
    }
        
        
    
                    
   /**
     * Convert SelectRequest to name value pairs
     */
    private Map<String, String> convertSelect(SelectRequest request) {
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Action", "Select");
        if (request.isSetSelectExpression()) {
            params.put("SelectExpression", request.getSelectExpression());
        }
        if (request.isSetNextToken()) {
            params.put("NextToken", request.getNextToken());
        }

        return params;
    }
        
        
    
    

}