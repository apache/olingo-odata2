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
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.core.ep.AbstractXmlProducerTestHelper;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.apache.olingo.odata2.api.xml.XMLStreamWriterFactory.XML_STREAM_WRITER_FACTORY_CLASS;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class JavaxStaxWriterWrapperTest extends AbstractXmlProducerTestHelper {

  private static final String BASIC_RESULT =
          "<?xml version='1.0' encoding='UTF-8'?>" +
          "<test><ns1:second attName=\"attValue\"/></test>";

  public JavaxStaxWriterWrapperTest(StreamWriterImplType type) {
    super(type);
  }

  // CHECKSTYLE:OFF
  @Before
  public void init() {
    //
    System.setProperty(XML_STREAM_WRITER_FACTORY_CLASS, JavaxStaxStreamFactory.class.getName()); // NOSONAR
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
    final String defaultNamespace = "http://defaultNamespace";
    final String namespaceNs1 = "namespace";

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("", defaultNamespace);
    prefixMap.put("ns1", namespaceNs1);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));

    Writer content = new StringWriter();
    JavaxStaxStreamFactory javaxStaxStreamFactory = new JavaxStaxStreamFactory();
    JavaxStaxWriterWrapper xmlWriter = (JavaxStaxWriterWrapper) javaxStaxStreamFactory.createXMLStreamWriter(content);

    String encoding = "UTF-8";
    String version = "1.0";

    xmlWriter.writeStartDocument(encoding, version);
    xmlWriter.setDefaultNamespace(defaultNamespace);
    xmlWriter.setPrefix("ns1", namespaceNs1);
    xmlWriter.writeStartElement("test");
    xmlWriter.writeDefaultNamespace(defaultNamespace);
    xmlWriter.writeNamespace("ns1", namespaceNs1);
    xmlWriter.writeStartElement(namespaceNs1, "second");
    xmlWriter.writeAttribute("attName", "attValue");
    xmlWriter.writeEndElement();
    xmlWriter.writeEndElement();
    xmlWriter.writeEndDocument();

    xmlWriter.flush();

    //
    String xmlString = content.toString();
    assertXpathExists("/:test", xmlString);
    assertXpathExists("/:test/ns1:second", xmlString);
    assertXpathExists("/:test/ns1:second[@attName=\"attValue\"]", xmlString);
  }

  @Test
  public void entityProvider() throws Exception {
    Edm edmMock = MockFacade.getMockEdm();

    String contentType = "application/xml";
    EdmEntitySet entitySet = edmMock.getDefaultEntityContainer().getEntitySet("Employees");
    Map<String, Object> data = createEmployeeData();
    EntityProviderWriteProperties properties =
            EntityProviderWriteProperties.serviceRoot(URI.create("http://root")).build();

    ODataResponse entry = EntityProvider.writeEntry(contentType, entitySet, data, properties);
    StringHelper.Stream content = StringHelper.toStream(entry.getEntity());

    String xmlString = content.asString();

    assertXpathEvaluatesTo(Edm.NAMESPACE_ATOM_2005, "/*/namespace::*[name()='']", xmlString);

    assertXpathExists("/:entry", xmlString);
    assertXpathExists("/:entry/:content", xmlString);
    // verify self link
    assertXpathExists("/:entry/:link[@href=\"Employees('1')\"]", xmlString);
    // verify content media link
    assertXpathExists("/:entry/:link[@href=\"Employees('1')/$value\"]", xmlString);
    // verify one navigation link
    assertXpathExists("/:entry/:link[@title='ne_Manager']", xmlString);

    // verify content
    assertXpathExists("/:entry/:content[@type='application/octet-stream']", xmlString);
    // verify properties
    assertXpathExists("/:entry/m:properties", xmlString);
    assertXpathEvaluatesTo("9", "count(/:entry/m:properties/*)", xmlString);
  }


  @Test
  public void writeMetadata() throws Exception {
    Map<String, String> predefinedNamespaces = new HashMap<String, String>();
    predefinedNamespaces.put("annoPrefix", "http://annoNamespace");
    predefinedNamespaces.put("foo", "http://foo");
    predefinedNamespaces.put("annoPrefix2", "http://annoNamespace");
    predefinedNamespaces.put("annoPrefix", "http://annoNamespace");

    ODataResponse response = EntityProvider.writeMetadata(null, predefinedNamespaces);
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertNull("BasicProvider should not set content header", response.getContentHeader());
    String metadata = StringHelper.inputStreamToString((InputStream) response.getEntity());
    assertTrue(metadata.contains("xmlns:foo=\"http://foo\""));
    assertTrue(metadata.contains("xmlns:annoPrefix=\"http://annoNamespace\""));
    assertTrue(metadata.contains("xmlns:annoPrefix2=\"http://annoNamespace\""));
  }

  private Map<String, Object> createEmployeeData() {
    Map<String, Object> employeeData = new HashMap<String, Object>();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    employeeData.put("EmployeeId", "1");
    employeeData.put("ImmageUrl", null);
    employeeData.put("ManagerId", "1");
    employeeData.put("Age", new Integer(52));
    employeeData.put("RoomId", "1");
    employeeData.put("EntryDate", date);
    employeeData.put("TeamId", "42");
    employeeData.put("EmployeeName", "Walter Winter");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    employeeData.put("Location", locationData);

    return employeeData;
  }
}
