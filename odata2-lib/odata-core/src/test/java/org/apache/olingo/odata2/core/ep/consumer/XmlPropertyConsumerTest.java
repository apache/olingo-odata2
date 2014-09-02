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
package org.apache.olingo.odata2.core.ep.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Test;

/**
 * Tests consuming XML properties.
 */
public class XmlPropertyConsumerTest extends AbstractXmlConsumerTest {

  public XmlPropertyConsumerTest(final StreamWriterImplType type) {
    super(type);
  }

  @Test
  public void readIntegerProperty() throws Exception {
    String xml = "<Age xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">67</Age>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    assertEquals(Integer.valueOf(67), resultMap.get("Age"));
  }

  @Test
  public void readIntegerPropertyAsLong() throws Exception {
    String xml = "<Age xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">67</Age>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    EntityProviderReadProperties readProperties = mock(EntityProviderReadProperties.class);
    when(readProperties.getTypeMappings()).thenReturn(createTypeMappings("Age", Long.class));
    Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, readProperties);

    assertEquals(Long.valueOf(67), resultMap.get("Age"));
  }

  @Test
  public void readIntegerPropertyWithNullMapping() throws Exception {
    String xml = "<Age xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">67</Age>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    EntityProviderReadProperties readProperties = mock(EntityProviderReadProperties.class);
    when(readProperties.getTypeMappings()).thenReturn(null);
    Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, readProperties);

    assertEquals(Integer.valueOf(67), resultMap.get("Age"));
  }

  @Test
  public void readIntegerPropertyWithEmptyMapping() throws Exception {
    String xml = "<Age xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">67</Age>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    EntityProviderReadProperties readProperties = mock(EntityProviderReadProperties.class);
    when(readProperties.getTypeMappings()).thenReturn(new HashMap<String, Object>());
    Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, readProperties);

    assertEquals(Integer.valueOf(67), resultMap.get("Age"));
  }

  @Test
  public void readStringProperty() throws Exception {
    String xml = "<EmployeeName xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">Max Mustermann</EmployeeName>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EmployeeName");

    Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    assertEquals("Max Mustermann", resultMap.get("EmployeeName"));
  }

  @Test
  public void readStringPropertyEmpty() throws Exception {
    final String xml = "<EmployeeName xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\" />";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EmployeeName");

    final Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    assertTrue(resultMap.containsKey("EmployeeName"));
    assertEquals("", resultMap.get("EmployeeName"));
  }

  @Test
  public void readStringPropertyNull() throws Exception {
    final String xml = "<EntryDate xmlns=\"" + Edm.NAMESPACE_D_2007_08
        + "\" m:null=\"true\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" />";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EntryDate");

    final Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    assertTrue(resultMap.containsKey("EntryDate"));
    assertNull(resultMap.get("EntryDate"));
  }

  @Test
  public void readStringPropertyNullFalse() throws Exception {
    final String xml = "<EntryDate xmlns=\"" + Edm.NAMESPACE_D_2007_08
        + "\" m:null=\"false\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">1970-01-02T00:00:00</EntryDate>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("EntryDate");

    final Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    assertEquals(86400000L, ((Calendar) resultMap.get("EntryDate")).getTimeInMillis());
  }

  @Test(expected = EntityProviderException.class)
  public void invalidSimplePropertyName() throws Exception {
    final String xml = "<Invalid xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">67</Invalid>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test(expected = EntityProviderException.class)
  public void invalidNullAttribute() throws Exception {
    final String xml = "<Age xmlns=\"" + Edm.NAMESPACE_D_2007_08
        + "\" m:null=\"wrong\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" />";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test(expected = EntityProviderException.class)
  public void nullValueNotAllowed() throws Exception {
    final String xml = "<Age xmlns=\"" + Edm.NAMESPACE_D_2007_08
        + "\" m:null=\"true\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" />";
    XMLStreamReader reader = createReaderForTest(xml, true);
    EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.isNullable()).thenReturn(false);
    when(property.getFacets()).thenReturn(facets);

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test(expected = EntityProviderException.class)
  public void violatedValidation() throws Exception {
    final String xml = "<Name xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">TooLongName</Name>";
    EdmProperty property = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Team")
        .getProperty("Name");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(10);
    when(property.getFacets()).thenReturn(facets);

    new XmlPropertyConsumer().readProperty(createReaderForTest(xml, true), property, null);
  }

  @Test
  public void ignoringValidation() throws Exception {
    final String xml = "<Name xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">TooLongName</Name>";
    EdmProperty property = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Team")
        .getProperty("Name");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.getMaxLength()).thenReturn(10);
    when(property.getFacets()).thenReturn(facets);
    final EntityProviderReadProperties readProperties = mock(EntityProviderReadProperties.class);

    final Map<String, Object> resultMap = new XmlPropertyConsumer()
        .readProperty(createReaderForTest(xml, true), property, readProperties);
    assertTrue(resultMap.containsKey("Name"));
    assertEquals("TooLongName", resultMap.get("Name"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void readComplexProperty() throws Exception {
    String xml =
        "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" m:type=\"RefScenario.c_Location\">" +
            "<Country>Germany</Country>" +
            "<City m:type=\"RefScenario.c_City\">" +
            "<PostalCode>69124</PostalCode>" +
            "<CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    Map<String, Object> locationMap = (Map<String, Object>) resultMap.get("Location");
    assertEquals("Germany", locationMap.get("Country"));
    Map<String, Object> cityMap = (Map<String, Object>) locationMap.get("City");
    assertEquals("69124", cityMap.get("PostalCode"));
    assertEquals("Heidelberg", cityMap.get("CityName"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void readComplexPropertyWithLineBreaks() throws Exception {
    String xml =
        "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" m:type=\"RefScenario.c_Location\">" +
            "    " +
            "<Country>Germany</Country>" +
            "<City m:type=\"RefScenario.c_City\">" +
            "<PostalCode>69124</PostalCode>" +
            "\n" +
            "<CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Location>" +
            "\n        \n ";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    Map<String, Object> locationMap = (Map<String, Object>) resultMap.get("Location");
    assertEquals("Germany", locationMap.get("Country"));
    Map<String, Object> cityMap = (Map<String, Object>) locationMap.get("City");
    assertEquals("69124", cityMap.get("PostalCode"));
    assertEquals("Heidelberg", cityMap.get("CityName"));
  }

  @Test(expected = EntityProviderException.class)
  public void readComplexPropertyInvalidMapping() throws Exception {
    String xml =
        "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" type=\"RefScenario.c_Location\">" +
            "<Country>Germany</Country>" +
            "<City type=\"RefScenario.c_City\">" +
            "<PostalCode>69124</PostalCode>" +
            "<CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    EntityProviderReadProperties readProperties = mock(EntityProviderReadProperties.class);
    when(readProperties.getTypeMappings()).thenReturn(
        createTypeMappings("Location",
            createTypeMappings("City",
                createTypeMappings("PostalCode", Integer.class))));
    try {
      Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, readProperties);
      assertNotNull(resultMap);
    } catch (EntityProviderException e) {
      assertTrue(e.getCause() instanceof EdmSimpleTypeException);
      throw e;
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void readComplexPropertyWithMappings() throws Exception {
    String xml =
        "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" m:type=\"RefScenario.c_Location\">" +
            "<Country>Germany</Country>" +
            "<City m:type=\"RefScenario.c_City\">" +
            "  <PostalCode>69124</PostalCode>" +
            "  <CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);

    EdmProperty locationComplexProperty =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");
    EdmProperty cityProperty = (EdmProperty) ((EdmComplexType) locationComplexProperty.getType()).getProperty("City");
    EdmProperty postalCodeProperty = (EdmProperty) ((EdmComplexType) cityProperty.getType()).getProperty("PostalCode");
    // Change the type of the PostalCode property to one that allows different Java types.
    when(postalCodeProperty.getType()).thenReturn(EdmSimpleTypeKind.Int32.getEdmSimpleTypeInstance());

    // Execute test
    EntityProviderReadProperties readProperties = mock(EntityProviderReadProperties.class);
    when(readProperties.getTypeMappings()).thenReturn(
        createTypeMappings("Location",
            createTypeMappings("City",
                createTypeMappings("CityName", String.class, "PostalCode", Long.class))));
    Map<String, Object> resultMap =
        new XmlPropertyConsumer().readProperty(reader, locationComplexProperty, readProperties);

    // verify
    Map<String, Object> locationMap = (Map<String, Object>) resultMap.get("Location");
    assertEquals("Germany", locationMap.get("Country"));
    Map<String, Object> cityMap = (Map<String, Object>) locationMap.get("City");
    assertEquals(Long.valueOf("69124"), cityMap.get("PostalCode"));
    assertEquals("Heidelberg", cityMap.get("CityName"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void readComplexPropertyWithNamespace() throws Exception {
    String xml =
        "<d:Location m:type=\"RefScenario.c_Location\" " +
            "    xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\"" +
            "    xmlns:d=\"" + Edm.NAMESPACE_D_2007_08 + "\">" +
            "  <d:Country>Germany</d:Country>" +
            "  <d:City m:type=\"RefScenario.c_City\">" +
            "    <d:PostalCode>69124</d:PostalCode>" +
            "    <d:CityName>Heidelberg</d:CityName>" +
            "  </d:City>" +
            "</d:Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    Object prop = new XmlPropertyConsumer().readProperty(reader, property, null);
    Map<String, Object> resultMap = (Map<String, Object>) prop;

    Map<String, Object> locationMap = (Map<String, Object>) resultMap.get("Location");
    assertEquals("Germany", locationMap.get("Country"));
    Map<String, Object> cityMap = (Map<String, Object>) locationMap.get("City");
    assertEquals("69124", cityMap.get("PostalCode"));
    assertEquals("Heidelberg", cityMap.get("CityName"));
  }

  @Test(expected = EntityProviderException.class)
  public void readComplexPropertyWithInvalidChild() throws Exception {
    String xml =
        "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" m:type=\"RefScenario.c_Location\">" +
            "<Invalid>Germany</Invalid>" +
            "<City m:type=\"RefScenario.c_City\">" +
            "<PostalCode>69124</PostalCode>" +
            "<CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test(expected = EntityProviderException.class)
  public void readComplexPropertyWithInvalidDeepChild() throws Exception {
    String xml =
        "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" m:type=\"RefScenario.c_Location\">" +
            "<Country>Germany</Country>" +
            "<City m:type=\"RefScenario.c_City\">" +
            "<Invalid>69124</Invalid>" +
            "<CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test(expected = EntityProviderException.class)
  public void readComplexPropertyWithInvalidName() throws Exception {
    String xml =
        "<Invalid xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" m:type=\"RefScenario.c_Location\">" +
            "<Country>Germany</Country>" +
            "<City m:type=\"RefScenario.c_City\">" +
            "<PostalCode>69124</PostalCode>" +
            "<CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Invalid>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test(expected = EntityProviderException.class)
  public void readComplexPropertyWithInvalidTypeAttribute() throws Exception {
    String xml =
        "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" m:type=\"Invalid\">" +
            "<Country>Germany</Country>" +
            "<City m:type=\"RefScenario.c_City\">" +
            "<PostalCode>69124</PostalCode>" +
            "<CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void readComplexPropertyWithoutTypeAttribute() throws Exception {
    String xml =
        "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\""
            + " xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">" +
            "<Country>Germany</Country>" +
            "<City m:type=\"RefScenario.c_City\">" +
            "<PostalCode>69124</PostalCode>" +
            "<CityName>Heidelberg</CityName>" +
            "</City>" +
            "</Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    Map<String, Object> locationMap = (Map<String, Object>) resultMap.get("Location");
    assertEquals("Germany", locationMap.get("Country"));
    Map<String, Object> cityMap = (Map<String, Object>) locationMap.get("City");
    assertEquals("69124", cityMap.get("PostalCode"));
    assertEquals("Heidelberg", cityMap.get("CityName"));
  }

  @Test
  public void complexPropertyNull() throws Exception {
    String xml = "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08
        + "\" m:null=\"true\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" />";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    final Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    assertTrue(resultMap.containsKey("Location"));
    assertNull(resultMap.get("Location"));
  }

  @Test(expected = EntityProviderException.class)
  public void complexPropertyNullValueNotAllowed() throws Exception {
    final String xml = "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08
        + "\" m:null=\"true\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" />";
    XMLStreamReader reader = createReaderForTest(xml, true);
    EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.isNullable()).thenReturn(false);
    when(property.getFacets()).thenReturn(facets);

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test
  public void complexPropertyNullValueNotAllowedButNotValidated() throws Exception {
    final String xml = "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08
        + "\" m:null=\"true\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" />";
    EdmProperty property = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee")
        .getProperty("Location");
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.isNullable()).thenReturn(false);
    when(property.getFacets()).thenReturn(facets);
    final EntityProviderReadProperties readProperties = mock(EntityProviderReadProperties.class);

    final Map<String, Object> resultMap = new XmlPropertyConsumer()
        .readProperty(createReaderForTest(xml, true), property, readProperties);
    assertFalse(resultMap.isEmpty());
    assertNull(resultMap.get("Location"));
  }

  @Test(expected = EntityProviderException.class)
  public void complexPropertyNullWithContent() throws Exception {
    String xml = "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08
        + "\" m:null=\"true\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "<City><PostalCode/><CityName/></City><Country>Germany</Country>"
        + "</Location>";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    new XmlPropertyConsumer().readProperty(reader, property, null);
  }

  @Test
  public void complexPropertyEmpty() throws Exception {
    final String xml = "<Location xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\" />";
    XMLStreamReader reader = createReaderForTest(xml, true);
    final EdmProperty property =
        (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Location");

    final Map<String, Object> resultMap = new XmlPropertyConsumer().readProperty(reader, property, null);

    assertNotNull(resultMap.get("Location"));
    @SuppressWarnings("unchecked")
    final Map<String, Object> innerMap = (Map<String, Object>) resultMap.get("Location");
    assertTrue(innerMap.isEmpty());
  }

  @Test
  public void collectionSimpleType() throws Exception {
    final String xml = "<AllUsedRoomIds xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">"
        + "<element>1</element>"
        + "<element m:null=\"true\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" />"
        + "<element></element>"
        + "</AllUsedRoomIds>";
    @SuppressWarnings("unchecked")
    final List<String> result = (List<String>) new XmlPropertyConsumer().readCollection(createReaderForTest(xml, true),
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer()
            .getFunctionImport("AllUsedRoomIds")),
        EntityProviderReadProperties.init().build());
    assertNotNull(result);
    assertEquals(Arrays.asList("1", null, ""), result);
  }

  @Test(expected = EntityProviderException.class)
  public void collectionSimpleTypeWrong() throws Exception {
    final String xml = "<AllUsedRoomIds xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">"
        + "<m:element xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\" />"
        + "</AllUsedRoomIds>";
    new XmlPropertyConsumer().readCollection(createReaderForTest(xml, true),
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer()
            .getFunctionImport("AllUsedRoomIds")), null);
  }

  @Test(expected = EntityProviderException.class)
  public void collectionSimpleTypeWrongMapping() throws Exception {
    final String xml = "<AllUsedRoomIds xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\">"
        + "<element>1</element></AllUsedRoomIds>";
    new XmlPropertyConsumer().readCollection(createReaderForTest(xml, true),
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer()
            .getFunctionImport("AllUsedRoomIds")),
        EntityProviderReadProperties.init().addTypeMappings(
            Collections.<String, Object> singletonMap("AllUsedRoomIds", Integer.class)).build());
  }

  @Test(expected = EntityProviderException.class)
  public void collectionSimpleTypeWrongXml() throws Exception {
    final String xml = "<AllUsedRoomIds xmlns=\"" + Edm.NAMESPACE_D_2007_08 + "\"><element>1</element>";
    new XmlPropertyConsumer().readCollection(createReaderForTest(xml, true),
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer()
            .getFunctionImport("AllUsedRoomIds")), null);
  }

  @Test
  public void collectionComplexType() throws Exception {
    final String xml = "<d:AllLocations xmlns:d=\"" + Edm.NAMESPACE_D_2007_08 + "\">"
        + "<d:element><d:City><d:PostalCode>69124</d:PostalCode><d:CityName>Heidelberg</d:CityName></d:City>"
        + "<d:Country>Germany</d:Country></d:element>"
        + "<d:element m:type=\"RefScenario.c_Location\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "<d:City m:type=\"RefScenario.c_City\"><d:PostalCode>69190</d:PostalCode><d:CityName>Walldorf</d:CityName>"
        + "</d:City><d:Country>Germany</d:Country></d:element>"
        + "</d:AllLocations>";
    @SuppressWarnings("unchecked")
    final List<?> result = (List<String>) new XmlPropertyConsumer().readCollection(createReaderForTest(xml, true),
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer()
            .getFunctionImport("AllLocations")),
        EntityProviderReadProperties.init().build());
    assertNotNull(result);
    assertEquals(2, result.size());
    @SuppressWarnings("unchecked")
    final Map<String, Object> secondLocation = (Map<String, Object>) result.get(1);
    assertEquals("Germany", secondLocation.get("Country"));
    @SuppressWarnings("unchecked")
    final Map<String, Object> secondCity = (Map<String, Object>) secondLocation.get("City");
    assertEquals("Walldorf", secondCity.get("CityName"));
  }
}
