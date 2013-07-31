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

import org.apache.http.HttpResponse;
import org.junit.Test;

import org.apache.olingo.odata2.api.commons.HttpContentType;

/**
 * Tests employing the reference scenario reading links in JSON format.
 *  
 */
public final class LinksJsonReadOnlyTest extends AbstractRefTest {

  @Test
  public void singleLink() throws Exception {
    HttpResponse response = callUri("Employees('1')/$links/ne_Room?$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    assertEquals("{\"d\":{\"uri\":\"" + getEndpoint() + "Rooms('1')\"}}", getBody(response));
  }

  @Test
  public void links() throws Exception {
    HttpResponse response = callUri("Rooms('1')/$links/nr_Employees?$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    assertEquals("{\"d\":[{\"uri\":\"" + getEndpoint() + "Employees('1')\"}]}", getBody(response));

    response = callUri("Rooms('2')/$links/nr_Employees?$skip=99&$inlinecount=allpages&$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
    assertEquals("{\"d\":{\"__count\":\"4\",\"results\":[]}}", getBody(response));
  }
}
