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

/**
 * Configuration for accessing Amazon Simple DB  service
 */
public class AmazonSimpleDBConfig {

    private String serviceVersion = "2009-04-15";
    private String serviceURL = "https://sdb.amazonaws.com";
    private String userAgent = "Amazon Simple DB Java Library";
    private String signatureVersion = "2";
    private String signatureMethod = "HmacSHA256";
    private String proxyHost = null;
    private int proxyPort = -1;
    private String proxyUsername = null;
    private String proxyPassword = null;
    private int maxErrorRetry = 3;
    private int maxConnections = 100;

    /**
     * Gets Version of the API
     *
     * @return Version of the Service
     */
    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Gets SignatureVersion property
     *
     * @return Signature Version for signing requests
     */
    public String getSignatureVersion() {
        return signatureVersion;
    }

    /**
     * Sets SignatureVersion property
     *
     * @param signatureVersion Signature Version for signing requests
     */
    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    /**
     * Sets SignatureVersion property and returns current AmazonSimpleDBConfig
     *
     * @param signatureVersion Signature Version for signing requests
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withSignatureVersion(String signatureVersion) {
        setSignatureVersion(signatureVersion);
        return this;
    }

    /**
     * Checks if SignatureVersion property is set
     *
     * @return true if SignatureVersion property is set
     */
    public boolean isSetSignatureVersion() {
        return true;
    }

    /**
     * Gets SignatureMethod property
     *
     * @return Signature Method for signing requests
     */
    public String getSignatureMethod() {
        return signatureMethod;
    }

    /**
     * Sets SignatureMethod property
     *
     * @param signatureMethod Signature Method for signing requests
     */
    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    /**
     * Sets SignatureMethod property and returns current AmazonSimpleDBConfig
     *
     * @param signatureMethod Signature Method for signing requests
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withSignatureMethod(String signatureMethod) {
        setSignatureMethod(signatureMethod);
        return this;
    }

    /**
     * Checks if SignatureMethod property is set
     *
     * @return true if SignatureMethod property is set
     */
    public boolean isSetSignatureMethod() {
        return true;
    }

    /**
     * Gets UserAgent property
     *
     * @return User Agent String to use when sending request
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets UserAgent property
     *
     * @param userAgent User Agent String to use when sending request
     *
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Sets UserAgent property and returns current AmazonSimpleDBConfig
     *
     * @param userAgent User Agent String to use when sending request
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withUserAgent(String userAgent) {
        setUserAgent(userAgent);
        return this;
    }

    /**
     * Checks if UserAgent property is set
     *
     * @return true if UserAgent property is set
     */
    public boolean isSetUserAgent() {
        return this.userAgent != null;
    }

    /**
     * Gets ServiceURL property
     *
     * @return Service Endpoint URL
     */
    public String getServiceURL() {
        return serviceURL;
    }

    /**
     * Sets ServiceURL property
     *
     * @param serviceURL Service Endpoint URL
     *
     */
    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    /**
     * Sets ServiceURL property and returns current AmazonSimpleDBConfig
     *
     * @param serviceURL Service Endpoint URL
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withServiceURL(String serviceURL) {
        setServiceURL(serviceURL);
        return this;
    }

    /**
     * Checks if ServiceURL property is set
     *
     * @return true if ServiceURL property is set
     */
    public boolean isSetServiceURL() {
        return this.serviceURL != null;
    }

    /**
     * Gets ProxyHost property
     *
     * @return Proxy Host for connection
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Sets ProxyHost property
     *
     * @param proxyHost Proxy Host for connection
     *
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * Sets ProxyHost property and returns current AmazonSimpleDBConfig
     *
     * @param proxyHost Proxy Host for connection
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withProxyHost(String proxyHost) {
        setProxyHost(proxyHost);
        return this;
    }

    /**
     * Checks if ProxyHost property is set
     *
     * @return true if ProxyHost property is set
     */
    public boolean isSetProxyHost() {
        return this.proxyHost != null;
    }

    /**
     * Gets ProxyPort property
     *
     * @return Proxy Port for connection
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets ProxyPort property
     *
     * @param proxyPort Proxy Port for connection
     *
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Sets ProxyPort property and returns current AmazonSimpleDBConfig
     *
     * @param proxyPort Proxy Port for connection
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withProxyPort(int proxyPort) {
        setProxyPort(proxyPort);
        return this;
    }

    /**
     * Checks if ProxyPort property is set
     *
     * @return true if ProxyPort property is set
     */
    public boolean isSetProxyPort() {
        return this.proxyPort != -1;
    }

    /**
     * Gets ProxyUsername property
     *
     * @return Proxy Username
     */
    public String getProxyUsername() {
        return proxyUsername;
    }

    /**
     * Sets ProxyUsername property
     *
     * @param proxyUsername Proxy Username for connection
     *
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    /**
     * Sets ProxyUsername property and returns current AmazonSimpleDBConfig
     *
     * @param proxyUsername Proxy Username for connection
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withProxyUsername(String proxyUsername) {
        setProxyUsername(proxyUsername);
        return this;
    }

    /**
     * Checks if ProxyUsername property is set
     *
     * @return true if ProxyUsername property is set
     */
    public boolean isSetProxyUsername() {
        return this.proxyUsername != null;
    }

    /**
     * Gets ProxyPassword property
     *
     * @return Proxy Password
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Sets ProxyPassword property
     *
     * @param proxyPassword Proxy Password for connection
     *
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * Sets ProxyPassword property and returns current AmazonSimpleDBConfig
     *
     * @param proxyPassword Proxy Password for connection
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withProxyPassword(String proxyPassword) {
        setProxyPassword(proxyPassword);
        return this;
    }

    /**
     * Checks if ProxyPassword property is set
     *
     * @return true if ProxyPassword property is set
     */
    public boolean isSetProxyPassword() {
        return this.proxyPassword != null;
    }

    /**
     * Gets MaxErrorRetry property
     *
     * @return Max number of retries on 500th errors
     */
    public int getMaxErrorRetry() {
        return maxErrorRetry;
    }

    /**
     * Sets MaxErrorRetry property
     *
     * @param maxErrorRetry Max number of retries on 500th errors
     *
     */
    public void setMaxErrorRetry(int maxErrorRetry) {
        this.maxErrorRetry = maxErrorRetry;
    }

    /**
     * Sets MaxErrorRetry property and returns current AmazonSimpleDBConfig
     *
     * @param maxErrorRetry Max number of retries on 500th errors
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withMaxErrorRetry(int maxErrorRetry) {
        setMaxErrorRetry(maxErrorRetry);
        return this;
    }

    /**
     * Checks if MaxErrorRetry property is set
     *
     * @return true if MaxErrorRetry property is set
     */
    public boolean isSetMaxErrorRetry() {
        return this.maxErrorRetry > 0;
    }
   

    /**
     * Gets MaxConnections property
     *
     * @return Max number of http connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Sets MaxConnections property
     *
     * @param maxConnections Max number of http connections
     *
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Sets MaxConnections property and returns current AmazonSimpleDBConfig
     *
     * @param maxConnections Max number of http connections
     *
     * @return AmazonSimpleDBConfig
     */
    public AmazonSimpleDBConfig withMaxConnections(int maxConnections) {
        setMaxConnections(maxConnections);
        return this;
    }

    /**
     * Checks if MaxConnections property is set
     *
     * @return true if MaxConnections property is set
     */
    public boolean isSetMaxConnections() {
        return this.maxConnections > 0;
    }

}