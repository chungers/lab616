<%
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file 
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
%>
<%@ page contentType="text/html;charset=UTF-8"
  import="java.util.*"
  import="java.net.URLEncoder" 
  import="org.apache.hadoop.io.Text"
  import="org.apache.hadoop.hbase.util.Bytes"
  import="org.apache.hadoop.hbase.util.JvmVersion"
  import="org.apache.hadoop.hbase.master.HMaster"
  import="org.apache.hadoop.hbase.HConstants"
  import="org.apache.hadoop.hbase.master.MetaRegion"
  import="org.apache.hadoop.hbase.client.HBaseAdmin"
  import="org.apache.hadoop.hbase.io.ImmutableBytesWritable"
  import="org.apache.hadoop.hbase.HServerInfo"
  import="org.apache.hadoop.hbase.HServerAddress"
  import="org.apache.hadoop.hbase.HBaseConfiguration"
  import="org.apache.hadoop.hbase.HColumnDescriptor" 
  import="org.apache.hadoop.hbase.HTableDescriptor" %><%
  HMaster master = (HMaster)getServletContext().getAttribute(HMaster.MASTER);
  HBaseConfiguration conf = master.getConfiguration();
  HServerAddress rootLocation = master.getRootRegionLocation();
  Map<byte [], MetaRegion> onlineRegions = master.getOnlineMetaRegions();
  Map<String, HServerInfo> serverToServerInfos =
    master.getServersToServerInfo();
  int interval = conf.getInt("hbase.regionserver.msginterval", 1000)/1000;
  if (interval == 0) {
      interval = 1;
  }
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml">
<head><meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
<title>HBase Master: <%= master.getMasterAddress().getHostname()%>:<%= master.getMasterAddress().getPort() %></title>
<link rel="stylesheet" type="text/css" href="/static/hbase.css" />
</head>
<body>
<a id="logo" href="http://wiki.apache.org/lucene-hadoop/Hbase"><img src="/static/hbase_logo_med.gif" alt="HBase Logo" title="HBase Logo" /></a>
<h1 id="page_title">Master: <%=master.getMasterAddress().getHostname()%>:<%=master.getMasterAddress().getPort()%></h1>
<p id="links_menu"><a href="/logs/">Local logs</a>, <a href="/stacks">Thread Dump</a>, <a href="/logLevel">Log Level</a></p>

<% if (JvmVersion.isBadJvmVersion()) { %>
  <div class="warning">
  Your current JVM version <%= System.getProperty("java.version") %> is known to be
  unstable with HBase. Please see the
  <a href="http://wiki.apache.org/hadoop/Hbase/Troubleshooting#A18">HBase wiki</a>
  for details.
  </div>
<% } %>

<hr id="head_rule" />

<h2>Master Attributes</h2>
<table>
<tr><th>Attribute Name</th><th>Value</th><th>Description</th></tr>
<tr><td>HBase Version</td><td><%= org.apache.hadoop.hbase.util.VersionInfo.getVersion() %>, r<%= org.apache.hadoop.hbase.util.VersionInfo.getRevision() %></td><td>HBase version and svn revision</td></tr>
<tr><td>HBase Compiled</td><td><%= org.apache.hadoop.hbase.util.VersionInfo.getDate() %>, <%= org.apache.hadoop.hbase.util.VersionInfo.getUser() %></td><td>When HBase version was compiled and by whom</td></tr>
<tr><td>Hadoop Version</td><td><%= org.apache.hadoop.util.VersionInfo.getVersion() %>, r<%= org.apache.hadoop.util.VersionInfo.getRevision() %></td><td>Hadoop version and svn revision</td></tr>
<tr><td>Hadoop Compiled</td><td><%= org.apache.hadoop.util.VersionInfo.getDate() %>, <%= org.apache.hadoop.util.VersionInfo.getUser() %></td><td>When Hadoop version was compiled and by whom</td></tr>
<tr><td>HBase Root Directory</td><td><%= master.getRootDir().toString() %></td><td>Location of HBase home directory</td></tr>
<tr><td>Load average</td><td><%= master.getAverageLoad() %></td><td>Average number of regions per regionserver. Naive computation.</td></tr>
<tr><td>Regions On FS</td><td><%= master.countRegionsOnFS() %></td><td>Number of regions on FileSystem. Rough count.</td></tr>
<tr><td>Zookeeper Quorum</td><td><%= master.getZooKeeperWrapper().getQuorumServers() %></td><td>Addresses of all registered ZK servers. For more, see <a href="/zk.jsp">zk dump</a>.</td></tr>
</table>

<h2>Catalog Tables</h2>
<% 
  if (rootLocation != null) { %>
<table>
<tr><th>Table</th><th>Description</th></tr>
<tr><td><a href="/table.jsp?name=<%= Bytes.toString(HConstants.ROOT_TABLE_NAME) %>"><%= Bytes.toString(HConstants.ROOT_TABLE_NAME) %></a></td><td>The -ROOT- table holds references to all .META. regions.</td></tr>
<%
    if (onlineRegions != null && onlineRegions.size() > 0) { %>
<tr><td><a href="/table.jsp?name=<%= Bytes.toString(HConstants.META_TABLE_NAME) %>"><%= Bytes.toString(HConstants.META_TABLE_NAME) %></a></td><td>The .META. table holds references to all User Table regions</td></tr> 
<%  } %>
</table>
<%} %>

<h2>User Tables</h2>
<% HTableDescriptor[] tables = new HBaseAdmin(conf).listTables(); 
   if(tables != null && tables.length > 0) { %>
<table>
<tr><th>Table</th><th>Description</th></tr>
<%   for(HTableDescriptor htDesc : tables ) { %>
<tr><td><a href=/table.jsp?name=<%= htDesc.getNameAsString() %>><%= htDesc.getNameAsString() %></a> </td><td><%= htDesc.toString() %></td></tr>
<%   }  %>

<p> <%= tables.length %> table(s) in set.</p>
</table>
<% } %>

<h2>Region Servers</h2>
<% if (serverToServerInfos != null && serverToServerInfos.size() > 0) { %>
<%   int totalRegions = 0;
     int totalRequests = 0; 
%>

<table>
<tr><th rowspan="<%= serverToServerInfos.size() + 1%>"></th><th>Address</th><th>Start Code</th><th>Load</th></tr>
<%   String[] serverNames = serverToServerInfos.keySet().toArray(new String[serverToServerInfos.size()]);
     Arrays.sort(serverNames);
     for (String serverName: serverNames) {
       HServerInfo hsi = serverToServerInfos.get(serverName);
       String hostname = hsi.getServerAddress().getHostname() + ":" + hsi.getInfoPort();
       String url = "http://" + hostname + "/";
       totalRegions += hsi.getLoad().getNumberOfRegions();
       totalRequests += hsi.getLoad().getNumberOfRequests() / interval;
       long startCode = hsi.getStartCode();
%>
<tr><td><a href="<%= url %>"><%= hostname %></a></td><td><%= startCode %></td><td><%= hsi.getLoad().toString(interval) %></td></tr>
<%   } %>
<tr><th>Total: </th><td>servers: <%= serverToServerInfos.size() %></td><td>&nbsp;</td><td>requests=<%= totalRequests %>, regions=<%= totalRegions %></td></tr>
</table>

<p>Load is requests per second and count of regions loaded</p>
<% } %>
</body>
</html>
