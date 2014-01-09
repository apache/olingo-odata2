#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<!--

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

-->
<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Apache Olingo - OData2 Library</title>
<style type="text/css">
body { font-family: Arial, sans-serif; font-size: 13px; line-height: 18px;
       color: blue; background-color: ${symbol_pound}ffffff; }
a { color: blue; text-decoration: none; }
a:focus { outline: thin dotted ${symbol_pound}4076cb; outline-offset: -1px; }
a:hover, a:active { outline: 0; }
a:hover { color: ${symbol_pound}404a7e; text-decoration: underline; }
h1, h2, h3, h4, h5, h6 { margin: 9px 0; font-family: inherit; font-weight: bold;
                         line-height: 1; color: blue; }
h1 { font-size: 36px; line-height: 40px; }
h2 { font-size: 30px; line-height: 40px; }
h3 { font-size: 24px; line-height: 40px; }
h4 { font-size: 18px; line-height: 20px; }
h5 { font-size: 14px; line-height: 20px; }
h6 { font-size: 12px; line-height: 20px; }
.logo { float: right; }
ul { padding: 0; margin: 0 0 9px 25px; }
ul ul { margin-bottom: 0; }
li { line-height: 18px; }
hr { margin: 18px 0;
     border: 0; border-top: 1px solid ${symbol_pound}cccccc; border-bottom: 1px solid ${symbol_pound}ffffff; }
table { border-collapse: collapse; border-spacing: 10px; }
th, td { border: 1px solid; padding: 20px; }
.code { font-family: "Courier New", monospace; font-size: 13px; line-height: 18px; }
</style>
</head>
<body>
	<h1>Apache Olingo - OData2 Library</h1>
	<hr />
	<h2>Reference Scenario</h2>
	<table>
		<tr>
			<td valign="top">
				<h3>Service Document and Metadata</h3>
				<ul>
					<li><a href="MyFormula.svc?_wadl" target="_blank">wadl</a></li>
					<li><a href="MyFormula.svc/" target="_blank">service document</a></li>
					<li><a href="MyFormula.svc/${symbol_dollar}metadata" target="_blank">metadata</a></li>
				</ul>
				<h3>EntitySets</h3>
				<ul>
					<li><a href="MyFormula.svc/Manufacturers" target="_blank">Manufacturers</a></li>
					<li><a href="MyFormula.svc/Manufacturers/?${symbol_dollar}expand=Cars" target="_blank">Manufacturers/?${symbol_dollar}expand=Cars</a></li>
					<li><a href="MyFormula.svc/Cars" target="_blank">Cars</a></li>
					<li><a href="MyFormula.svc/Cars/?${symbol_dollar}expand=Driver" target="_blank">Cars/?${symbol_dollar}expand=Driver</a></li>
					<li><a href="MyFormula.svc/Drivers" target="_blank">Drivers</a></li>
					<li><a href="MyFormula.svc/Drivers/?${symbol_dollar}expand=Car" target="_blank">Drivers/?${symbol_dollar}expand=Car</a></li>
					<li><a href="MyFormula.svc/Drivers/?${symbol_dollar}orderby=Lastname" target="_blank">Drivers/?${symbol_dollar}orderby=Lastname</a></li>
					<li><a href="MyFormula.svc/Drivers/?${symbol_dollar}filter=year(Birthday)%20gt%201981" target="_blank">Drivers/?${symbol_dollar}filter=year(Birthday) gt 1981</a></li>
				</ul>
				<h3>Entities</h3>
				<ul>
					<li><a href="MyFormula.svc/Manufacturers('1')" target="_blank">Manufacturers('1')</a></li>
					<li><a href="MyFormula.svc/Manufacturers('1')/Cars" target="_blank">Manufacturers('1')/Cars</a></li>
					<li><a href="MyFormula.svc/Cars('1')" target="_blank">Cars('1')</a></li>
					<li><a href="MyFormula.svc/Cars('1')/Driver" target="_blank">Cars('1')/Driver</a></li>
					<li><a href="MyFormula.svc/Cars('1')/?${symbol_dollar}expand=Manufacturer" target="_blank">Cars('1')/?${symbol_dollar}expand=Manufacturer</a></li>
					<li><a href="MyFormula.svc/Drivers(1)" target="_blank">Drivers(1)</a></li>
					<li><a href="MyFormula.svc/Drivers(1)/Car" target="_blank">Drivers(1)/Car</a></li>
				</ul>
			</td>
			<td valign="top">
				&nbsp;
			</td>
			<td valign="bottom">
				<div class="code">
					<%
					  String version = "gen/version.html";
					%>
					<%
					  try {
					%>
					<jsp:include page='<%=version%>' />
					<%
					  } catch (Exception e) {
					%>
					<p>IDE Build</p>
					<%
					  }
					%>
				</div>
			</td>
		</tr>
	</table>
</body>
</html>
