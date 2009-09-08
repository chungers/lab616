
package com.amazonaws.sdb.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://sdb.amazonaws.com/doc/2009-04-15/}SelectResult"/>
 *         &lt;element ref="{http://sdb.amazonaws.com/doc/2009-04-15/}ResponseMetadata"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * Generated by AWS Code Generator
 * <p/>
 * Mon May 11 14:17:05 PDT 2009
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "selectResult",
    "responseMetadata"
})
@XmlRootElement(name = "SelectResponse")
public class SelectResponse {

    @XmlElement(name = "SelectResult", required = true)
    protected SelectResult selectResult;
    @XmlElement(name = "ResponseMetadata", required = true)
    protected ResponseMetadata responseMetadata;

    /**
     * Default constructor
     * 
     */
    public SelectResponse() {
        super();
    }

    /**
     * Value constructor
     * 
     */
    public SelectResponse(final SelectResult selectResult, final ResponseMetadata responseMetadata) {
        this.selectResult = selectResult;
        this.responseMetadata = responseMetadata;
    }

    /**
     * Gets the value of the selectResult property.
     * 
     * @return
     *     possible object is
     *     {@link SelectResult }
     *     
     */
    public SelectResult getSelectResult() {
        return selectResult;
    }

    /**
     * Sets the value of the selectResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link SelectResult }
     *     
     */
    public void setSelectResult(SelectResult value) {
        this.selectResult = value;
    }

    public boolean isSetSelectResult() {
        return (this.selectResult!= null);
    }

    /**
     * Gets the value of the responseMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseMetadata }
     *     
     */
    public ResponseMetadata getResponseMetadata() {
        return responseMetadata;
    }

    /**
     * Sets the value of the responseMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseMetadata }
     *     
     */
    public void setResponseMetadata(ResponseMetadata value) {
        this.responseMetadata = value;
    }

    public boolean isSetResponseMetadata() {
        return (this.responseMetadata!= null);
    }

    /**
     * Sets the value of the SelectResult property.
     * 
     * @param value
     * @return
     *     this instance
     */
    public SelectResponse withSelectResult(SelectResult value) {
        setSelectResult(value);
        return this;
    }

    /**
     * Sets the value of the ResponseMetadata property.
     * 
     * @param value
     * @return
     *     this instance
     */
    public SelectResponse withResponseMetadata(ResponseMetadata value) {
        setResponseMetadata(value);
        return this;
    }
    

    /**
     * 
     * XML string representation of this object
     * 
     * @return XML String
     */
    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<SelectResponse xmlns=\"http://sdb.amazonaws.com/doc/2009-04-15/\">");
        if (isSetSelectResult()) {
            SelectResult  selectResult = getSelectResult();
            xml.append("<SelectResult>");
            xml.append(selectResult.toXMLFragment());
            xml.append("</SelectResult>");
        } 
        if (isSetResponseMetadata()) {
            ResponseMetadata  responseMetadata = getResponseMetadata();
            xml.append("<ResponseMetadata>");
            xml.append(responseMetadata.toXMLFragment());
            xml.append("</ResponseMetadata>");
        } 
        xml.append("</SelectResponse>");
        return xml.toString();
    }

    /**
     * 
     * Escape XML special characters
     */
    private String escapeXML(String string) {
        StringBuffer sb = new StringBuffer();
        int length = string.length();
        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            switch (c) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '\'':
                sb.append("&#039;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }



    /**
     * 
     * JSON string representation of this object
     * 
     * @return JSON String
     */
    public String toJSON() {
        StringBuffer json = new StringBuffer();
        json.append("{\"SelectResponse\" : {");
        json.append(quoteJSON("@xmlns"));
        json.append(" : ");
        json.append(quoteJSON("http://sdb.amazonaws.com/doc/2009-04-15/"));
        boolean first = true;
        json.append(", ");
        if (isSetSelectResult()) {
            if (!first) json.append(", ");
            json.append("\"SelectResult\" : {");
            SelectResult  selectResult = getSelectResult();

            json.append(selectResult.toJSONFragment());
            json.append("}");
            first = false;
        } 
        if (isSetResponseMetadata()) {
            if (!first) json.append(", ");
            json.append("\"ResponseMetadata\" : {");
            ResponseMetadata  responseMetadata = getResponseMetadata();

            json.append(responseMetadata.toJSONFragment());
            json.append("}");
            first = false;
        } 
        json.append("}");
        json.append("}");
        return json.toString();
    }

    /**
     * 
     * Quote JSON string
     */
    private String quoteJSON(String string) {
        StringBuffer sb = new StringBuffer();
        sb.append("\"");
        int length = string.length();
        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            switch (c) {
            case '"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '/':
                sb.append("\\/");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                if (c <  ' ') {
                    sb.append("\\u" + String.format("%03x", Integer.valueOf(c)));
                } else {
                sb.append(c);
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }


}
