/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.xml;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.xml.XMLStreamConstants;
import org.apache.olingo.odata2.api.xml.XMLStreamReader;
import org.apache.olingo.odata2.core.ep.AbstractProviderTest;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.apache.olingo.odata2.api.xml.XMLStreamReaderFactory.XML_STREAM_READER_FACTORY_CLASS;

/**
 */
public class JavaxStaxReaderWrapperTest extends AbstractProviderTest {

  private static final String BASIC_RESULT =
          "<?xml version='1.0' ?>" +
          "<n0:test xmlns:n0=\"http://defaultNamespace\">" +
          "<n1:second n1:attName=\"attValue\" xmlns:n1=\"namespace\">TEST</n1:second>" +
          "</n0:test>";

  private static final String EMPLOYEE = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
          "<entry xml:base=\"http://root\" xmlns=\"http://www.w3.org/2005/Atom\" " +
          "xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n" +
          "\t<id>http://rootEmployees('1')</id>\n" +
          "\t<title type=\"text\">Walter Winter</title>\n" +
          "\t<updated>1999-01-01T00:00:00Z</updated>\n" +
          "\t<category scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\" term=\"RefScenario" +
          ".Employee\"/>\n" +
          "\t<link href=\"Employees('1')\" rel=\"edit\" title=\"Employee\"/>\n" +
          "\t<link href=\"Employees('1')/$value\" rel=\"edit-media\" type=\"application/octet-stream\"/>\n" +
          "\t<link href=\"Employees('1')/ne_Manager\" rel=\"http://schemas.microsoft" +
          ".com/ado/2007/08/dataservices/related/ne_Manager\" title=\"ne_Manager\" type=\"application/atom+xml;" +
          "type=entry\"/>\n" +
          "\t<link href=\"Employees('1')/ne_Team\" rel=\"http://schemas.microsoft" +
          ".com/ado/2007/08/dataservices/related/ne_Team\" title=\"ne_Team\" type=\"application/atom+xml;" +
          "type=entry\"/>\n" +
          "\t<link href=\"Employees('1')/ne_Room\" rel=\"http://schemas.microsoft" +
          ".com/ado/2007/08/dataservices/related/ne_Room\" title=\"ne_Room\" type=\"application/atom+xml;" +
          "type=entry\"/>\n" +
          "\t<content src=\"Employees('1')/$value\" type=\"application/octet-stream\"/>\n" +
          "\t<m:properties>\n" +
          "\t\t<d:EmployeeId>1</d:EmployeeId>\n" +
          "\t\t<d:EmployeeName>Walter Winter</d:EmployeeName>\n" +
          "\t\t<d:ManagerId>1</d:ManagerId>\n" +
          "\t\t<d:RoomId>1</d:RoomId>\n" +
          "\t\t<d:TeamId>42</d:TeamId>\n" +
          "\t\t<d:Location m:type=\"RefScenario.c_Location\">\n" +
          "\t\t\t<d:City m:type=\"RefScenario.c_City\">\n" +
          "\t\t\t\t<d:PostalCode>33470</d:PostalCode>\n" +
          "\t\t\t\t<d:CityName>Duckburg</d:CityName>\n" +
          "\t\t\t</d:City>\n" +
          "\t\t\t<d:Country>Calisota</d:Country>\n" +
          "\t\t</d:Location>\n" +
          "\t\t<d:Age>52</d:Age>\n" +
          "\t\t<d:EntryDate>1999-01-01T00:00:00</d:EntryDate>\n" +
          "\t\t<d:ImageUrl m:null=\"true\"/>\n" +
          "\t</m:properties>\n" +
          "</entry>";

  public JavaxStaxReaderWrapperTest(StreamWriterImplType type) {
    super(type);
  }

  // CHECKSTYLE:OFF
  @Before
  public void init() {
    //
    System.setProperty(XML_STREAM_READER_FACTORY_CLASS, JavaxStaxStreamFactory.class.getName()); // NOSONAR
    //
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
  }
  // CHECKSTYLE:ON

  @Test
  public void basic() throws Exception {
    StringHelper.Stream stream = StringHelper.toStream(BASIC_RESULT);
    JavaxStaxStreamFactory javaxStaxStreamFactory = new JavaxStaxStreamFactory();
    XMLStreamReader xmlReader = javaxStaxStreamFactory.createXMLStreamReader(stream.asStream());

    final int[] expected = new int[]{
            XMLStreamConstants.START_ELEMENT,
            XMLStreamConstants.START_ELEMENT,
            XMLStreamConstants.CHARACTERS,
            XMLStreamConstants.END_ELEMENT,
            XMLStreamConstants.END_ELEMENT,
            XMLStreamConstants.END_DOCUMENT};
    int pos = 0;
    while(xmlReader.hasNext()) {
      int elementId = xmlReader.next();
      assertEquals("Unexpected type at position: " + pos,
              expected[pos++], elementId);
    }
  }

  @Test
  public void entityProvider() throws Exception {
    Edm edmMock = MockFacade.getMockEdm();

    String contentType = "application/xml";
    EdmEntitySet entitySet = edmMock.getDefaultEntityContainer().getEntitySet("Employees");
    EntityProviderReadProperties properties =
            EntityProviderReadProperties.init().build();

    StringHelper.Stream content = StringHelper.toStream(EMPLOYEE);

    ODataEntry entry = EntityProvider.readEntry(contentType, entitySet, content.asStream(), properties);
    Map<String, Object> employeeData = entry.getProperties();

    // validate
    assertEquals(9, employeeData.size());
    //
    assertEquals("1", employeeData.get("EmployeeId"));
    assertEquals("Walter Winter", employeeData.get("EmployeeName"));
    assertEquals("1", employeeData.get("ManagerId"));
    assertEquals("1", employeeData.get("RoomId"));
    assertEquals("42", employeeData.get("TeamId"));
    assertEquals(Integer.valueOf(52), employeeData.get("Age"));
    assertEquals(915148800000l, ((Calendar)employeeData.get("EntryDate")).getTimeInMillis());
    Map<String, Object> location = (Map<String, Object>) employeeData.get("Location");
    assertEquals("Calisota", location.get("Country"));
    Map<String, Object> city = (Map<String, Object>) location.get("City");
    assertEquals("33470", city.get("PostalCode"));
    assertEquals("Duckburg", city.get("CityName"));
  }
}
