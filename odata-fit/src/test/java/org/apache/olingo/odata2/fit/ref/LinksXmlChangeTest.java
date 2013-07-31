/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 *        or more contributor license agreements.  See the NOTICE file
 *        distributed with this work for additional information
 *        regarding copyright ownership.  The ASF licenses this file
 *        to you under the Apache License, Version 2.0 (the
 *        "License"); you may not use this file except in compliance
 *        with the License.  You may obtain a copy of the License at
 * 
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 *        Unless required by applicable law or agreed to in writing,
 *        software distributed under the License is distributed on an
 *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *        KIND, either express or implied.  See the License for the
 *        specific language governing permissions and limitations
 *        under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.fit.ref;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.edm.Edm;

/**
 * Tests employing the reference scenario changing links in XML format.
 *  
 */
public final class LinksXmlChangeTest extends AbstractRefTest {

  private static final String XML_DECLARATION = "<?xml version='1.0' encoding='utf-8'?>";

  @Test
  public void createLink() throws Exception {
    final String uriString = "Rooms('101')/$links/nr_Employees";
    final String requestBody = XML_DECLARATION
        + "<uri xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">" + getEndpoint() + "Employees('1')</uri>";
    postUri(uriString, requestBody, HttpContentType.APPLICATION_XML, HttpStatusCodes.NO_CONTENT);
    assertEquals(requestBody, getBody(callUri(uriString + "('1')")));

    postUri(uriString, requestBody.replace("'1'", "'99'"), HttpContentType.APPLICATION_XML, HttpStatusCodes.NOT_FOUND);
  }

  @Test
  public void updateLink() throws Exception {
    final String uriString = "Employees('2')/$links/ne_Room";
    final String requestBody =
        "<uri xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">" + getEndpoint() + "Rooms('3')</uri>";
    putUri(uriString, requestBody, HttpContentType.APPLICATION_XML, HttpStatusCodes.NO_CONTENT);
    assertEquals(XML_DECLARATION + requestBody, getBody(callUri(uriString)));

    final String uriString2 = "Rooms('1')/$links/nr_Employees('1')";
    callUri(ODataHttpMethod.PATCH, uriString2, null, null, requestBody.replace("Rooms", "Employees"), HttpContentType.APPLICATION_XML, HttpStatusCodes.NO_CONTENT);
    notFound(uriString2);
    checkUri(uriString2.replace("Employees('1')", "Employees('3')"));

    putUri(uriString.replace("'2'", "'99'"), requestBody, HttpContentType.APPLICATION_XML, HttpStatusCodes.NOT_FOUND);
    putUri(uriString, requestBody.replace("'3'", "'999'"), HttpContentType.APPLICATION_XML, HttpStatusCodes.NOT_FOUND);
    putUri("Teams('1')/nt_Employees('2')/$links/ne_Room", requestBody, HttpContentType.APPLICATION_XML, HttpStatusCodes.BAD_REQUEST);
  }
}
