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
package org.apache.olingo.odata2.android.xml;

import junit.framework.Assert;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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

/**
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class AndroidXmlWriterTest {

  private static final String BASIC_RESULT =
          "<?xml version='1.0' encoding='UTF-8' ?>" +
          "<test xmlns=\"http://defaultNamespace\">" +
          "<n0:second n0:attName=\"attValue\" xmlns:n0=\"namespace\" />" +
          "</test>";

  // CHECKSTYLE:OFF
  @Before
  public void init() {
    //
    System.setProperty(XML_STREAM_WRITER_FACTORY_CLASS, AndroidXmlFactory.class.getName()); // NOSONAR
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
  public void basic() throws XMLStreamException {
    Writer writer = new StringWriter();
    AndroidXmlWriter xmlWriter = new AndroidXmlWriter(writer);

    xmlWriter.writeStartDocument();
    xmlWriter.setDefaultNamespace("http://defaultNamespace");
    xmlWriter.writeStartElement("test");
    xmlWriter.writeStartElement("namespace", "second");
    xmlWriter.writeAttribute("attName", "attValue");
    xmlWriter.writeEndElement();
    xmlWriter.writeEndElement();
    xmlWriter.writeEndDocument();

    xmlWriter.flush();

    //
    Assert.assertEquals(BASIC_RESULT, writer.toString());
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
    System.out.println(xmlString);

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
