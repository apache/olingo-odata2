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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAction;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmContentKind;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.AnnotationAttribute;
import org.apache.olingo.odata2.api.edm.provider.AnnotationElement;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.DataServices;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.EdmTestProvider;
import org.junit.Test;

public class XmlMetadataConsumerTest extends AbstractXmlConsumerTest {

  public XmlMetadataConsumerTest(final StreamWriterImplType type) {
    super(type);
  }

  private static final String DEFAULT_VALUE = "Photo";
  private static final String FC_TARGET_PATH = "Содержание";
  private static final String FC_NS_URI = "http://localhost";
  private static final String FC_NS_PREFIX = "ру";
  private static final Boolean FC_KEEP_IN_CONTENT = Boolean.FALSE;
  private static final String NAMESPACE = "RefScenario";
  private static final String NAMESPACE2 = "RefScenario2";
  private static final String MIME_TYPE = "image/jpeg";
  private static final String ASSOCIATION = "ManagerEmployees";
  private static final int MAX_LENGTH = 4;

  private final String[] propertyNames = { "EmployeeId", "EmployeeName", "Location" };

  private final String xml = "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
      + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
      + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
      + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
      + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "<Property Name=\""
      + propertyNames[1] + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>" + "<Property Name=\""
      + propertyNames[2] + "\" Type=\"RefScenario.c_Location\" Nullable=\"false\"/>" + "</EntityType>"
      + "<ComplexType Name=\"c_Location\">" + "<Property Name=\"Country\" Type=\"Edm.String\"/>" + "</ComplexType>"
      + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";

  private final String xml2 = "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06
      + "\" xmlns:prefix=\"namespace\">" + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
      + Edm.NAMESPACE_M_2007_08 + "\">" + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\""
      + Edm.NAMESPACE_EDM_2008_01 + "\">" + "<prefix:schemaElement>text3</prefix:schemaElement>"
      + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
      + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "<Property Name=\""
      + propertyNames[1] + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>" + "<Property Name=\""
      + propertyNames[2] + "\" Type=\"RefScenario.c_Location\" Nullable=\"false\"/>" + "</EntityType>"
      + "<ComplexType Name=\"c_Location\">" + "<Property Name=\"Country\" Type=\"Edm.String\"/>" + "</ComplexType>"
      + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";

  private final String xmlWithBaseType = "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06
      + "\">" + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
      + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
      + "<EntityType Name= \"Employee\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>" + "<Property Name=\""
      + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "<Property Name=\"" + propertyNames[1]
      + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>" + "<Property Name=\"" + propertyNames[2]
      + "\" Type=\"RefScenario.c_Location\" Nullable=\"false\"/>" + "</EntityType>"
      + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">" + "</EntityType>"
      + "<ComplexType Name=\"c_Location\">" + "<Property Name=\"Country\" Type=\"Edm.String\"/>" + "</ComplexType>"
      + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";

  private final String xmlWithAssociation =
      "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
          + Edm.NAMESPACE_EDMX_2007_06
          + "\">"
          + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
          + Edm.NAMESPACE_M_2007_08
          + "\">"
          + "<Schema Namespace=\""
          + NAMESPACE
          + "\" xmlns=\""
          + Edm.NAMESPACE_EDM_2008_09
          + "\">"
          + "<EntityType Name= \"Employee\">"
          + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
          + "<Property Name=\""
          + propertyNames[0]
          + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
          + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" " +
          "FromRole=\"r_Employees\" ToRole=\"r_Manager\" />"
          + "</EntityType>"
          + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
          + "<NavigationProperty Name=\"nm_Employees\" Relationship=\"RefScenario.ManagerEmployees\" " +
          "FromRole=\"r_Manager\" ToRole=\"r_Employees\" />"
          + "</EntityType>" + "<Association Name=\"" + ASSOCIATION + "\">"
          + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"r_Employees\">"
          + "<OnDelete Action=\"Cascade\"/>" + "</End>"
          + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>" + "</Association>"
          + "</Schema>" + "<Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
          + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
          + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
          + "<EntitySet Name=\"Managers\" EntityType=\"RefScenario.Manager\"/>" + "<AssociationSet Name=\""
          + ASSOCIATION + "\" Association=\"RefScenario." + ASSOCIATION + "\">"
          + "<End EntitySet=\"Managers\" Role=\"r_Manager\"/>" + "<End EntitySet=\"Employees\" Role=\"r_Employees\"/>"
          + "</AssociationSet>" + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";

  private final String xmlWithTwoSchemas = "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06
      + "\">" + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
      + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
      + "<EntityType Name= \"Employee\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>" + "<Property Name=\""
      + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "<Property Name=\"" + propertyNames[1]
      + "\" Type=\"Edm.String\"/>" + "</EntityType>" + "</Schema>" + "<Schema Namespace=\"" + NAMESPACE2
      + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">" + "<EntityType Name= \"Photo\">"
      + "<Key><PropertyRef Name=\"Id\"/></Key>"
      + "<Property Name=\"Id\" Type=\"Edm.Int32\" Nullable=\"false\" ConcurrencyMode=\"Fixed\" MaxLength=\""
      + MAX_LENGTH + "\"/>" + "<Property Name=\"Name\" Type=\"Edm.String\" Unicode=\"true\" DefaultValue=\""
      + DEFAULT_VALUE
      + "\" FixedLength=\"false\"/>" + "<Property Name=\"BinaryData\" Type=\"Edm.Binary\" m:MimeType=\"" + MIME_TYPE
      + "\"/>" + "<Property Name=\"Содержание\" Type=\"Edm.String\" m:FC_TargetPath=\"" + FC_TARGET_PATH
      + "\" m:FC_NsUri=\"" + FC_NS_URI + "\"" + " m:FC_NsPrefix=\"" + FC_NS_PREFIX + "\" m:FC_KeepInContent=\""
      + FC_KEEP_IN_CONTENT + "\" m:FC_ContentKind=\"text\" >" + "</Property>" + "</EntityType>" + "</Schema>"
      + "</edmx:DataServices>" + "</edmx:Edmx>";

  private final String xmlWithStringValueForMaxLengthFacet = "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
      + Edm.NAMESPACE_EDMX_2007_06
      + "\">" + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
      + "<Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
      + "<EntityType Name= \"Photo\"><Key><PropertyRef Name=\"Id\"/></Key><Property Name=\"Id\" Type=\"Edm.Int32\" " +
      "Nullable=\"false\" MaxLength=\"Max\"/><Property Name=\"Name\" Type=\"Edm.Int32\" MaxLength=\"max\"/>"
      + "</EntityType></Schema></edmx:DataServices></edmx:Edmx>";

  @Test
  public void stringValueForMaxLegthFacet() throws Exception {
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithStringValueForMaxLengthFacet);
    DataServices result = parser.readMetadata(reader, true);

    List<Property> properties = result.getSchemas().get(0).getEntityTypes().get(0).getProperties();
    assertEquals(2, properties.size());

    Property property = getForName(properties, "Id");
    EdmFacets facets = property.getFacets();
    assertEquals(new Integer(Integer.MAX_VALUE), facets.getMaxLength());

    property = getForName(properties, "Name");
    facets = property.getFacets();
    assertEquals(new Integer(Integer.MAX_VALUE), facets.getMaxLength());
  }

  private Property getForName(final List<Property> properties, final String propertyName) {
    for (Property property : properties) {
      if (property.getName().equals(propertyName)) {
        return property;
      }
    }
    fail("Should have found property:" + propertyName);
    return null;
  }

  @Test
  public void test() throws XMLStreamException, EntityProviderException {
    int i = 0;
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xml);
    DataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (Schema schema : result.getSchemas()) {
      assertEquals(NAMESPACE, schema.getNamespace());
      assertEquals(1, schema.getEntityTypes().size());
      assertEquals("Employee", schema.getEntityTypes().get(0).getName());
      assertEquals(Boolean.TRUE, schema.getEntityTypes().get(0).isHasStream());
      for (PropertyRef propertyRef : schema.getEntityTypes().get(0).getKey().getKeys()) {
        assertEquals("EmployeeId", propertyRef.getName());
      }
      for (Property property : schema.getEntityTypes().get(0).getProperties()) {
        assertEquals(propertyNames[i], property.getName());
        if ("Location".equals(property.getName())) {
          ComplexProperty cProperty = (ComplexProperty) property;
          assertEquals(NAMESPACE, cProperty.getType().getNamespace());
          assertEquals("c_Location", cProperty.getType().getName());
        } else if ("EmployeeName".equals(property.getName())) {
          assertNotNull(property.getCustomizableFeedMappings());
          assertEquals("SyndicationTitle", property.getCustomizableFeedMappings().getFcTargetPath());
          assertNull(property.getCustomizableFeedMappings().getFcContentKind());
        }
        i++;
      }
      assertEquals(1, schema.getComplexTypes().size());
      assertEquals("c_Location", schema.getComplexTypes().get(0).getName());
    }
  }

  @Test
  public void testOtherEdmNamespace() throws XMLStreamException, EntityProviderException {
    int i = 0;
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xml2);
    DataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (Schema schema : result.getSchemas()) {
      assertEquals(NAMESPACE, schema.getNamespace());
      assertEquals(1, schema.getEntityTypes().size());
      assertEquals("Employee", schema.getEntityTypes().get(0).getName());
      for (PropertyRef propertyRef : schema.getEntityTypes().get(0).getKey().getKeys()) {
        assertEquals("EmployeeId", propertyRef.getName());
      }
      for (Property property : schema.getEntityTypes().get(0).getProperties()) {
        assertEquals(propertyNames[i], property.getName());
        if ("Location".equals(property.getName())) {
          ComplexProperty cProperty = (ComplexProperty) property;
          assertEquals("c_Location", cProperty.getType().getName());
        } else if ("EmployeeName".equals(property.getName())) {
          assertNotNull(property.getCustomizableFeedMappings());
        }
        i++;
      }
      for (AnnotationElement annoElement : schema.getAnnotationElements()) {
        assertEquals("prefix", annoElement.getPrefix());
        assertEquals("namespace", annoElement.getNamespace());
        assertEquals("schemaElement", annoElement.getName());
        assertEquals("text3", annoElement.getText());
      }
    }
  }

  @Test
  public void testBaseType() throws XMLStreamException, EntityProviderException {
    int i = 0;
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithBaseType);
    DataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (Schema schema : result.getSchemas()) {
      assertEquals(NAMESPACE, schema.getNamespace());
      assertEquals(2, schema.getEntityTypes().size());
      assertEquals("Employee", schema.getEntityTypes().get(0).getName());
      for (PropertyRef propertyRef : schema.getEntityTypes().get(0).getKey().getKeys()) {
        assertEquals("EmployeeId", propertyRef.getName());
      }
      for (Property property : schema.getEntityTypes().get(0).getProperties()) {
        assertEquals(propertyNames[i], property.getName());
        i++;
      }

    }
  }

  @Test
  public void testComplexTypeWithBaseType() throws XMLStreamException, EntityProviderException {
    final String xml =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" Alias=\"RS\"  xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\"" + propertyNames[2] + "\" Type=\"RefScenario.c_Location\" Nullable=\"false\"/>"
            + "</EntityType>" + "<ComplexType Name=\"c_BaseType_for_Location\" Abstract=\"true\">"
            + "<Property Name=\"Country\" Type=\"Edm.String\"/>" + "</ComplexType>"
            + "<ComplexType Name=\"c_Location\" BaseType=\"RefScenario.c_BaseType_for_Location\">" + "</ComplexType>"
            + "<ComplexType Name=\"c_Other_Location\" BaseType=\"RS.c_BaseType_for_Location\">" + "</ComplexType>"
            + "</Schema>"
            + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xml);
    DataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (Schema schema : result.getSchemas()) {
      for (ComplexType complexType : schema.getComplexTypes()) {
        if ("c_Location".equals(complexType.getName())) {
          assertNotNull(complexType.getBaseType());
          assertTrue(!complexType.isAbstract());
          assertEquals("c_BaseType_for_Location", complexType.getBaseType().getName());
          assertEquals("RefScenario", complexType.getBaseType().getNamespace());
        } else if ("c_Other_Location".equals(complexType.getName())) {
          assertNotNull(complexType.getBaseType());
          assertTrue(!complexType.isAbstract());
          assertEquals("c_BaseType_for_Location", complexType.getBaseType().getName());
          assertEquals("RS", complexType.getBaseType().getNamespace());
        } else if ("c_BaseType_for_Location".equals(complexType.getName())) {
          assertNotNull(complexType.isAbstract());
          assertTrue(complexType.isAbstract());
        } else {
          assertTrue(false);
        }
      }

    }
  }

  @Test(expected = EntityProviderException.class)
  public void testComplexTypeWithInvalidBaseType() throws XMLStreamException, EntityProviderException {
    final String xml =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\"" + propertyNames[2] + "\" Type=\"RefScenario.c_Location\" Nullable=\"false\"/>"
            + "</EntityType>" + "<ComplexType Name=\"c_BaseType_for_Location\" Abstract=\"true\">"
            + "<Property Name=\"Country\" Type=\"Edm.String\"/>" + "</ComplexType>"
            + "<ComplexType Name=\"c_Location\" BaseType=\"RefScenario.Employee\">" + "</ComplexType>" + "</Schema>"
            + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xml);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testComplexTypeWithInvalidBaseType2() throws XMLStreamException, EntityProviderException {
    final String xml =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\"" + propertyNames[2] + "\" Type=\"RefScenario.c_Location\" Nullable=\"false\"/>"
            + "</EntityType>" + "<ComplexType Name=\"c_BaseType_for_Location\" Abstract=\"true\">"
            + "<Property Name=\"Country\" Type=\"Edm.String\"/>" + "</ComplexType>"
            + "<ComplexType Name=\"c_Location\" BaseType=\"c_BaseType_for_Location\">" + "</ComplexType>" + "</Schema>"
            + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xml);
    parser.readMetadata(reader, true);
  }

  @Test
  public void testAssociation() throws XMLStreamException, EntityProviderException {
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithAssociation);
    DataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (Schema schema : result.getSchemas()) {
      for (EntityType entityType : schema.getEntityTypes()) {
        if ("Manager".equals(entityType.getName())) {
          assertEquals("RefScenario", entityType.getBaseType().getNamespace());
          assertEquals("Employee", entityType.getBaseType().getName());
          for (NavigationProperty navProperty : entityType.getNavigationProperties()) {
            assertEquals("r_Manager", navProperty.getFromRole());
            assertEquals("r_Employees", navProperty.getToRole());
            assertEquals("RefScenario", navProperty.getRelationship().getNamespace());
            assertEquals(ASSOCIATION, navProperty.getRelationship().getName());
          }
        }
        if ("Employee".equals(entityType.getName())) {
          for (NavigationProperty navProperty : entityType.getNavigationProperties()) {
            assertEquals("r_Employees", navProperty.getFromRole());
            assertEquals("RefScenario", navProperty.getRelationship().getNamespace());
            assertEquals(ASSOCIATION, navProperty.getRelationship().getName());
          }
        }
      }
      for (Association association : schema.getAssociations()) {
        AssociationEnd end;
        assertEquals(ASSOCIATION, association.getName());
        if ("Employee".equals(association.getEnd1().getType().getName())) {
          end = association.getEnd1();
        } else {
          end = association.getEnd2();
        }
        assertEquals(EdmMultiplicity.MANY, end.getMultiplicity());
        assertEquals("r_Employees", end.getRole());
        assertEquals(EdmAction.Cascade, end.getOnDelete().getAction());
      }
    }
  }

  @Test
  public void testTwoSchemas() throws XMLStreamException, EntityProviderException {
    int i = 0;
    String schemasNs[] = { NAMESPACE, NAMESPACE2 };
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithTwoSchemas);
    DataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    assertEquals(2, result.getSchemas().size());
    for (Schema schema : result.getSchemas()) {
      assertEquals(schemasNs[i], schema.getNamespace());
      assertEquals(1, schema.getEntityTypes().size());
      i++;

    }
  }

  @Test
  public void testProperties() throws EntityProviderException, XMLStreamException {
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithTwoSchemas);
    DataServices result = parser.readMetadata(reader, true);
    for (Schema schema : result.getSchemas()) {
      for (EntityType entityType : schema.getEntityTypes()) {
        if ("Employee".equals(entityType.getName())) {
          for (Property property : entityType.getProperties()) {
            if (propertyNames[0].equals(property.getName())) {
              assertNotNull(property.getFacets());
              assertEquals(Boolean.FALSE, property.getFacets().isNullable());
            } else if (propertyNames[1].equals(property.getName())) {
              assertNull(property.getFacets());
            }
          }
        } else if ("Photo".equals(entityType.getName())) {
          for (Property property : entityType.getProperties()) {
            SimpleProperty sProperty = (SimpleProperty) property;
            if ("Id".equals(property.getName())) {
              assertEquals(Boolean.FALSE, property.getFacets().isNullable());
              assertEquals(EdmConcurrencyMode.Fixed, property.getFacets().getConcurrencyMode());
              assertEquals(new Integer(MAX_LENGTH), property.getFacets().getMaxLength());

              assertEquals(EdmSimpleTypeKind.Int32, sProperty.getType());
              assertNull(property.getCustomizableFeedMappings());
            }
            if ("Name".equals(property.getName())) {
              assertEquals(Boolean.TRUE, property.getFacets().isUnicode());
              assertEquals(DEFAULT_VALUE, property.getFacets().getDefaultValue());
              assertEquals(Boolean.FALSE, property.getFacets().isFixedLength());
              assertEquals(EdmSimpleTypeKind.String, sProperty.getType());
              assertNull(property.getCustomizableFeedMappings());
            }
            if ("Содержание".equals(property.getName())) {
              assertEquals(FC_TARGET_PATH, property.getCustomizableFeedMappings().getFcTargetPath());
              assertEquals(FC_NS_URI, property.getCustomizableFeedMappings().getFcNsUri());
              assertEquals(FC_NS_PREFIX, property.getCustomizableFeedMappings().getFcNsPrefix());
              assertEquals(FC_KEEP_IN_CONTENT, property.getCustomizableFeedMappings().isFcKeepInContent());
              assertEquals(EdmContentKind.text, property.getCustomizableFeedMappings().getFcContentKind());
            }
            if ("BinaryData".equals(property.getName())) {
              assertEquals(MIME_TYPE, property.getMimeType());
            }
          }
        }
      }
    }
  }

  @Test
  public void testEntitySet() throws XMLStreamException, EntityProviderException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\"" + propertyNames[1] + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>" + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>" + "</EntityContainer>"
            + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmWithEntityContainer);
    DataServices result = parser.readMetadata(reader, true);
    for (Schema schema : result.getSchemas()) {
      for (EntityContainer container : schema.getEntityContainers()) {
        assertEquals("Container1", container.getName());
        assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
        for (EntitySet entitySet : container.getEntitySets()) {
          assertEquals("Employees", entitySet.getName());
          assertEquals("Employee", entitySet.getEntityType().getName());
          assertEquals(NAMESPACE, entitySet.getEntityType().getNamespace());
        }
      }
    }
  }

  @Test
  public void testAssociationSet() throws XMLStreamException, EntityProviderException {
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithAssociation);
    DataServices result = parser.readMetadata(reader, true);
    for (Schema schema : result.getSchemas()) {
      for (EntityContainer container : schema.getEntityContainers()) {
        assertEquals(NAMESPACE2, schema.getNamespace());
        assertEquals("Container1", container.getName());
        assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
        for (AssociationSet assocSet : container.getAssociationSets()) {
          assertEquals(ASSOCIATION, assocSet.getName());
          assertEquals(ASSOCIATION, assocSet.getAssociation().getName());
          assertEquals(NAMESPACE, assocSet.getAssociation().getNamespace());
          AssociationSetEnd end;
          if ("Employees".equals(assocSet.getEnd1().getEntitySet())) {
            end = assocSet.getEnd1();
          } else {
            end = assocSet.getEnd2();
          }
          assertEquals("r_Employees", end.getRole());
        }
      }
    }
  }

  @Test
  public void testFunctionImport() throws XMLStreamException, EntityProviderException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "<FunctionImport Name=\"RoomSearch\" ReturnType=\"Collection(RefScenario.Room)\" " +
            "EntitySet=\"Rooms\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q1\" Type=\"Edm.String\" Nullable=\"true\" />"
            + "<Parameter Name=\"q2\" Type=\"Edm.Int32\" Nullable=\"false\" />"
            + "</FunctionImport>"
            + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmWithEntityContainer);
    DataServices result = parser.readMetadata(reader, true);
    for (Schema schema : result.getSchemas()) {
      for (EntityContainer container : schema.getEntityContainers()) {
        assertEquals("Container1", container.getName());
        assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());

        assertEquals(2, container.getFunctionImports().size());
        FunctionImport functionImport1 = container.getFunctionImports().get(0);

        assertEquals("EmployeeSearch", functionImport1.getName());
        assertEquals("Employees", functionImport1.getEntitySet());
        assertEquals(NAMESPACE, functionImport1.getReturnType().getTypeName().getNamespace());
        assertEquals("Employee", functionImport1.getReturnType().getTypeName().getName());
        assertEquals(EdmMultiplicity.MANY, functionImport1.getReturnType().getMultiplicity());
        assertEquals("GET", functionImport1.getHttpMethod());
        assertEquals(2, functionImport1.getParameters().size());

        assertEquals("q1", functionImport1.getParameters().get(0).getName());
        assertEquals(EdmSimpleTypeKind.String, functionImport1.getParameters().get(0).getType());
        assertEquals(Boolean.TRUE, functionImport1.getParameters().get(0).getFacets().isNullable());

        assertEquals("q2", functionImport1.getParameters().get(1).getName());
        assertEquals(EdmSimpleTypeKind.Int32, functionImport1.getParameters().get(1).getType());
        assertEquals(Boolean.FALSE, functionImport1.getParameters().get(1).getFacets().isNullable());

        FunctionImport functionImport2 = container.getFunctionImports().get(1);

        assertEquals("RoomSearch", functionImport2.getName());
        assertEquals("Rooms", functionImport2.getEntitySet());
        assertEquals(NAMESPACE, functionImport2.getReturnType().getTypeName().getNamespace());
        assertEquals("Room", functionImport2.getReturnType().getTypeName().getName());
        assertEquals(EdmMultiplicity.MANY, functionImport2.getReturnType().getMultiplicity());
        assertEquals("GET", functionImport2.getHttpMethod());
        assertEquals(2, functionImport2.getParameters().size());

        assertEquals("q1", functionImport2.getParameters().get(0).getName());
        assertEquals(EdmSimpleTypeKind.String, functionImport2.getParameters().get(0).getType());
        assertEquals(Boolean.TRUE, functionImport2.getParameters().get(0).getFacets().isNullable());

        assertEquals("q2", functionImport2.getParameters().get(1).getName());
        assertEquals(EdmSimpleTypeKind.Int32, functionImport2.getParameters().get(1).getType());
        assertEquals(Boolean.FALSE, functionImport2.getParameters().get(1).getFacets().isNullable());

      }
    }
  }

  @Test()
  public void testAlias() throws XMLStreamException, EntityProviderException {
    final String xml =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" Alias=\"RS\"  xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>" + "<Property Name=\""
            + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityType Name=\"Manager\" BaseType=\"RS.Employee\" m:HasStream=\"true\">" + "</EntityType>"
            + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xml);
    DataServices result = parser.readMetadata(reader, true);
    for (Schema schema : result.getSchemas()) {
      assertEquals("RS", schema.getAlias());
      for (EntityType entityType : schema.getEntityTypes()) {
        if ("Manager".equals(entityType.getName())) {
          assertEquals("Employee", entityType.getBaseType().getName());
          assertEquals("RS", entityType.getBaseType().getNamespace());
        }
      }

    }
  }

  @Test(expected = EntityProviderException.class)
  public void testEntityTypeWithoutKeys() throws XMLStreamException, EntityProviderException {
    final String xmlWithoutKeys =
        "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\">" + "<Property Name=\"" + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "</EntityType>" + "</Schema>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithoutKeys);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidBaseType() throws XMLStreamException, EntityProviderException {
    final String xmlWithInvalidBaseType =
        "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Manager\" BaseType=\"Employee\" m:HasStream=\"true\">" + "</EntityType>"
            + "</Schema>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidBaseType);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidRole() throws XMLStreamException, EntityProviderException {
    final String xmlWithInvalidAssociation =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "</EntityType>"
            + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "<NavigationProperty Name=\"nm_Employees\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"Manager\" ToRole=\"Employees\" />"
            + "</EntityType>" + "<Association Name=\"ManagerEmployees\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"r_Employees\"/>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>" + "</Association>"
            + "</Schema>"
            + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociation);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidRelationship() throws XMLStreamException, EntityProviderException {
    final String xmlWithInvalidAssociation =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployee\" " +
            "FromRole=\"r_Employees\" ToRole=\"r_Manager\" />"
            + "</EntityType>" + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "</EntityType>" + "<Association Name=\"ManagerEmployees\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"r_Employees\"/>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>" + "</Association>"
            + "</Schema>"
            + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociation);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testMissingRelationship() throws Exception {
    final String xmlWithInvalidAssociation =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>" + "<Property Name=\""
            + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" />" + "</EntityType>"
            + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">" + "</EntityType>"
            + "<Association Name=\"ManagerEmployees\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"r_Employees\"/>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>"
            + "</Association></Schema></edmx:DataServices></edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociation);
    try {
      parser.readMetadata(reader, true);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.MISSING_ATTRIBUTE.getKey(), e.getMessageReference().getKey());
      assertEquals(2, e.getMessageReference().getContent().size());
      assertEquals("Relationship", e.getMessageReference().getContent().get(0));
      assertEquals("NavigationProperty", e.getMessageReference().getContent().get(1));
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void testMissingEntityType() throws Exception {
    final String xmlWithInvalidAssociation =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"r_Employees\" ToRole=\"r_Manager\" />"
            + "</EntityType>" + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "</EntityType>" + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" />" + "</EntityContainer>" + "<Association Name=\"ManagerEmployees\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"r_Employees\"/>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>"
            + "</Association></Schema></edmx:DataServices></edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociation);
    try {
      parser.readMetadata(reader, true);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.MISSING_ATTRIBUTE.getKey(), e.getMessageReference().getKey());
      assertEquals(2, e.getMessageReference().getContent().size());
      assertEquals("EntityType", e.getMessageReference().getContent().get(0));
      assertEquals("EntitySet", e.getMessageReference().getContent().get(1));
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void testMissingType() throws Exception {
    final String xmlWithInvalidAssociation =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"r_Employees\" ToRole=\"r_Manager\" />"
            + "</EntityType>" + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "</EntityType>" + "<Association Name=\"ManagerEmployees\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"r_Employees\"/>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>"
            + "</Association></Schema></edmx:DataServices></edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociation);
    try {
      parser.readMetadata(reader, true);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.MISSING_ATTRIBUTE.getKey(), e.getMessageReference().getKey());
      assertEquals(2, e.getMessageReference().getContent().size());
      assertEquals("Type", e.getMessageReference().getContent().get(0));
      assertEquals("Property", e.getMessageReference().getContent().get(1));
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void testMissingTypeAtAssociation() throws Exception {
    final String xmlWithInvalidAssociation =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"r_Employees\" ToRole=\"r_Manager\" />"
            + "</EntityType>" + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "</EntityType>" + "<Association Name=\"ManagerEmployees\">"
            + "<End Multiplicity=\"*\" Role=\"r_Employees\"/>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>"
            + "</Association></Schema></edmx:DataServices></edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociation);
    try {
      parser.readMetadata(reader, true);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.MISSING_ATTRIBUTE.getKey(), e.getMessageReference().getKey());
      assertEquals(2, e.getMessageReference().getContent().size());
      assertEquals("Type", e.getMessageReference().getContent().get(0));
      assertEquals("End", e.getMessageReference().getContent().get(1));
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void testMissingTypeAtFunctionImport() throws Exception {
    final String xml =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[1]
            + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<FunctionImport Name=\"EmployeeSearch\" ReturnType=\"Collection(RefScenario.Employee)\" " +
            "EntitySet=\"Employees\" m:HttpMethod=\"GET\">"
            + "<Parameter Name=\"q\" Nullable=\"true\" />" + "</FunctionImport>"
            + "</EntityContainer></Schema></edmx:DataServices></edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xml);
    try {
      parser.readMetadata(reader, true);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.MISSING_ATTRIBUTE.getKey(), e.getMessageReference().getKey());
      assertEquals(2, e.getMessageReference().getContent().size());
      assertEquals("Type", e.getMessageReference().getContent().get(0));
      assertEquals("Parameter", e.getMessageReference().getContent().get(1));
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void testMissingAssociation() throws Exception {
    final String xmlWithAssociation =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"r_Employees\" ToRole=\"r_Manager\" />"
            + "</EntityType>" + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>" + "<AssociationSet Name=\""
            + ASSOCIATION
            // + "\" Association=\"RefScenario." + ASSOCIATION
            + "\">" + "<End EntitySet=\"Employees\" Role=\"r_Employees\"/>" + "</AssociationSet>"
            + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices></edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithAssociation);
    try {
      parser.readMetadata(reader, true);
    } catch (EntityProviderException e) {
      assertEquals(EntityProviderException.MISSING_ATTRIBUTE.getKey(), e.getMessageReference().getKey());
      assertEquals(2, e.getMessageReference().getContent().size());
      assertEquals("Association", e.getMessageReference().getContent().get(0));
      assertEquals("AssociationSet", e.getMessageReference().getContent().get(1));
      throw e;
    }
  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidAssociation() throws XMLStreamException, EntityProviderException {
    final String xmlWithInvalidAssociationSet =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"r_Employees\" ToRole=\"r_Manager\" />"
            + "</EntityType>"
            + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "<NavigationProperty Name=\"nm_Employees\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"r_Manager\" ToRole=\"r_Employees\" />"
            + "</EntityType>" + "<Association Name=\"" + ASSOCIATION + "\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"r_Employees\">"
            + "<OnDelete Action=\"Cascade\"/>" + "</End>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>" + "</Association>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<EntitySet Name=\"Managers\" EntityType=\"RefScenario.Manager\"/>" + "<AssociationSet Name=\""
            + ASSOCIATION + "\" Association=\"RefScenario2." + ASSOCIATION + "\">"
            + "<End EntitySet=\"Managers\" Role=\"r_Manager\"/>"
            + "<End EntitySet=\"Employees\" Role=\"r_Employees\"/>" + "</AssociationSet>" + "</EntityContainer>"
            + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociationSet);
    parser.readMetadata(reader, true);

  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidAssociationEnd() throws XMLStreamException, EntityProviderException {
    final String employees = "r_Employees";
    final String manager = "r_Manager";
    final String xmlWithInvalidAssociationSetEnd =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>" + "<Property Name=\""
            + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" FromRole=\""
            + employees + "\" ToRole=\"" + manager + "\" />" + "</EntityType>"
            + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "<NavigationProperty Name=\"nm_Employees\" Relationship=\"RefScenario.ManagerEmployees\" FromRole=\""
            + manager + "\" ToRole=\"" + employees + "\" />" + "</EntityType>" + "<Association Name=\"" + ASSOCIATION
            + "\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"" + employees + "1" + "\"/>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"" + manager + "\"/>" + "</Association>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<EntitySet Name=\"Managers\" EntityType=\"RefScenario.Manager\"/>" + "<AssociationSet Name=\""
            + ASSOCIATION + "\" Association=\"RefScenario2." + ASSOCIATION + "\">"
            + "<End EntitySet=\"Managers\" Role=\"" + manager + "\"/>" + "<End EntitySet=\"Employees\" Role=\""
            + employees + "\"/>" + "</AssociationSet>" + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>"
            + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociationSetEnd);
    parser.readMetadata(reader, true);

  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidAssociationEnd2() throws XMLStreamException, EntityProviderException {
    final String employees = "r_Employees";
    final String manager = "r_Manager";
    final String xmlWithInvalidAssociationSetEnd =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>" + "<Property Name=\""
            + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" FromRole=\""
            + employees + "\" ToRole=\"" + manager + "\" />" + "</EntityType>"
            + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "<NavigationProperty Name=\"nm_Employees\" Relationship=\"RefScenario.ManagerEmployees\" FromRole=\""
            + manager + "\" ToRole=\"" + employees + "\" />" + "</EntityType>" + "<Association Name=\"" + ASSOCIATION
            + "\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"" + employees + "\"/>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"" + manager + "\"/>" + "</Association>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
            + "<EntitySet Name=\"Managers\" EntityType=\"RefScenario.Manager\"/>" + "<AssociationSet Name=\""
            + ASSOCIATION + "\" Association=\"RefScenario2." + ASSOCIATION + "\">"
            + "<End EntitySet=\"Managers\" Role=\"" + manager + "\"/>" + "<End EntitySet=\"Managers\" Role=\""
            + manager + "\"/>" + "</AssociationSet>" + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>"
            + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithInvalidAssociationSetEnd);
    parser.readMetadata(reader, true);

  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidEntitySet() throws XMLStreamException, EntityProviderException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\"" + propertyNames[1] + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>"
            + "</EntityType>" + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Mitarbeiter\"/>" + "</EntityContainer>"
            + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmWithEntityContainer);
    parser.readMetadata(reader, true);

  }

  @Test
  public void testEntityTypeInOtherSchema() throws XMLStreamException, EntityProviderException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Photos\" EntityType=\"" + NAMESPACE2 + ".Photo\"/>" + "</EntityContainer>"
            + "</Schema>" + "<Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Photo\">" + "<Key><PropertyRef Name=\"Id\"/></Key>"
            + "<Property Name=\"Id\" Type=\"Edm.Int32\" Nullable=\"false\"/>" + "</EntityType>" + "</Schema>"
            + "</edmx:DataServices>"
            + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmWithEntityContainer);
    DataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (Schema schema : result.getSchemas()) {
      for (EntityContainer container : schema.getEntityContainers()) {
        assertEquals("Container1", container.getName());
        for (EntitySet entitySet : container.getEntitySets()) {
          assertEquals(NAMESPACE2, entitySet.getEntityType().getNamespace());
          assertEquals("Photo", entitySet.getEntityType().getName());
        }
      }
    }
  }

  @Test
  public void scenarioTest() throws XMLStreamException, EntityProviderException {
    final String ASSOCIATION2 = "TeamEmployees";
    final String xml =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" Alias=\"RS\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\""
            + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\""
            + propertyNames[2]
            + "\" Type=\"RefScenario.c_Location\" Nullable=\"false\"/>"
            + "<NavigationProperty Name=\"ne_Manager\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"r_Employees\" ToRole=\"r_Manager\" />"
            + "<NavigationProperty Name=\"ne_Team\" Relationship=\"RefScenario.TeamEmployees\" " +
            "FromRole=\"r_Employees\" ToRole=\"r_Team\" />"
            + "</EntityType>"
            + "<EntityType Name=\"Manager\" BaseType=\"RefScenario.Employee\" m:HasStream=\"true\">"
            + "<NavigationProperty Name=\"nm_Employees\" Relationship=\"RefScenario.ManagerEmployees\" " +
            "FromRole=\"r_Manager\" ToRole=\"r_Employees\" />"
            + "</EntityType>"
            + "<EntityType Name=\"Team\">"
            + "<Key>"
            + "<PropertyRef Name=\"Id\"/>"
            + "</Key>"
            + "<NavigationProperty Name=\"nt_Employees\" Relationship=\"RefScenario.TeamEmployees\"" +
            " FromRole=\"r_Team\" ToRole=\"r_Employees\" />"
            + "</EntityType>" + "<ComplexType Name=\"c_Location\">"
            + "<Property Name=\"Country\" Type=\"Edm.String\"/>" + "</ComplexType>" + "<Association Name=\""
            + ASSOCIATION + "\">" + "<End Type=\"RS.Employee\" Multiplicity=\"*\" Role=\"r_Employees\">"
            + "<OnDelete Action=\"Cascade\"/>" + "</End>"
            + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>" + "</Association>"
            + "<Association Name=\"" + ASSOCIATION2 + "\">"
            + "<End Type=\"RefScenario.Employee\" Multiplicity=\"*\" Role=\"r_Employees\"/>"
            + "<End Type=\"RefScenario.Team\" Multiplicity=\"1\" Role=\"r_Team\"/>" + "</Association>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Employees\" EntityType=\"RS.Employee\"/>"
            + "<EntitySet Name=\"Managers\" EntityType=\"RefScenario.Manager\"/>"
            + "<EntitySet Name=\"Teams\" EntityType=\"RefScenario.Team\"/>" + "<AssociationSet Name=\"" + ASSOCIATION
            + "\" Association=\"RefScenario." + ASSOCIATION + "\">"
            + "<End EntitySet=\"Managers\" Role=\"r_Manager\"/>"
            + "<End EntitySet=\"Employees\" Role=\"r_Employees\"/>" + "</AssociationSet>" + "<AssociationSet Name=\""
            + ASSOCIATION2 + "\" Association=\"RefScenario." + ASSOCIATION2 + "\">"
            + "<End EntitySet=\"Teams\" Role=\"r_Team\"/>" + "<End EntitySet=\"Employees\" Role=\"r_Employees\"/>"
            + "</AssociationSet>" + "</EntityContainer>" + "</Schema>" + "<Schema Namespace=\"" + NAMESPACE2
            + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">" + "<EntityType Name= \"Photo\">" + "<Key>"
            + "<PropertyRef Name=\"Id\"/>" + "<PropertyRef Name=\"Name\"/>" + "</Key>"
            + "<Property Name=\"Id\" Type=\"Edm.Int32\" Nullable=\"false\" ConcurrencyMode=\"Fixed\" MaxLength=\""
            + MAX_LENGTH + "\"/>" + "<Property Name=\"Name\" Type=\"Edm.String\" Unicode=\"true\" DefaultValue=\""
            + DEFAULT_VALUE + "\" FixedLength=\"false\"/>"
            + "<Property Name=\"BinaryData\" Type=\"Edm.Binary\" m:MimeType=\"" + MIME_TYPE + "\"/>"
            + "<Property Name=\"Содержание\" Type=\"Edm.String\" m:FC_TargetPath=\"" + FC_TARGET_PATH
            + "\" m:FC_NsUri=\"" + FC_NS_URI + "\"" + " m:FC_NsPrefix=\"" + FC_NS_PREFIX + "\" m:FC_KeepInContent=\""
            + FC_KEEP_IN_CONTENT + "\" m:FC_ContentKind=\"text\" >" + "</Property>" + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Photos\" EntityType=\"RefScenario2.Photo\"/>" + "</EntityContainer>" + "</Schema>"
            + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xml);
    DataServices result = parser.readMetadata(reader, true);
    assertEquals(2, result.getSchemas().size());
    for (Schema schema : result.getSchemas()) {
      if (NAMESPACE.equals(schema.getNamespace())) {
        assertEquals(3, schema.getEntityTypes().size());
        assertEquals(1, schema.getComplexTypes().size());
        assertEquals(2, schema.getAssociations().size());
        assertEquals(1, schema.getEntityContainers().size());
      } else if (NAMESPACE2.equals(schema.getNamespace())) {
        assertEquals(1, schema.getEntityTypes().size());
        assertEquals(0, schema.getComplexTypes().size());
        assertEquals(0, schema.getAssociations().size());
        assertEquals(1, schema.getEntityContainers().size());
        for (EntityType entityType : schema.getEntityTypes()) {
          assertEquals(2, entityType.getKey().getKeys().size());
        }
      }
    }
  }

  @Test
  public void testRefScenario() throws Exception {
    EdmProvider testProvider = new EdmTestProvider();
    ODataResponse response = EntityProvider.writeMetadata(testProvider.getSchemas(), null);

    String stream = StringHelper.inputStreamToString((InputStream) response.getEntity());
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    DataServices result = parser.readMetadata(createStreamReader(stream), true);

    ODataResponse response2 = EntityProvider.writeMetadata(result.getSchemas(), null);
    String streamAfterParse = StringHelper.inputStreamToString((InputStream) response2.getEntity());
    assertEquals(stream, streamAfterParse);
  }

  @Test
  public void testAnnotations() throws XMLStreamException, EntityProviderException {
    final String xmlWithAnnotations =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\""
            + Edm.NAMESPACE_EDMX_2007_06
            + "\" xmlns:annoPrefix=\"http://annoNamespace\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\""
            + Edm.NAMESPACE_M_2007_08
            + "\">"
            + "<Schema Namespace=\""
            + NAMESPACE
            + "\" xmlns=\""
            + Edm.NAMESPACE_EDM_2008_09
            + "\">"
            + "<EntityType Name= \"Employee\" prefix1:href=\"http://foo\" xmlns:prefix1=\"namespaceForAnno\">"
            + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"EmployeeId\" Type=\"Edm.String\" Nullable=\"false\"/>"
            + "<Property Name=\"EmployeeName\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\" " +
            "annoPrefix:annoName=\"annoText\">"
            + "<annoPrefix:propertyAnnoElement>text</annoPrefix:propertyAnnoElement>"
            + "<annoPrefix:propertyAnnoElement2 />"
            + "</Property>"
            + "</EntityType>"
            + "<annoPrefix:schemaElementTest1>"
            + "<prefix:schemaElementTest2 xmlns:prefix=\"namespace\">text3"
            + "</prefix:schemaElementTest2>"
            + "<annoPrefix:schemaElementTest3 rel=\"self\" pre:href=\"http://foo\" " +
            "xmlns:pre=\"namespaceForAnno\">text4</annoPrefix:schemaElementTest3>"
            + " </annoPrefix:schemaElementTest1>" + "</Schema>"
            + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataConsumer parser = new XmlMetadataConsumer();
    XMLStreamReader reader = createStreamReader(xmlWithAnnotations);
    DataServices result = parser.readMetadata(reader, false);
    for (Schema schema : result.getSchemas()) {
      assertEquals(1, schema.getAnnotationElements().size());
      for (AnnotationElement annoElement : schema.getAnnotationElements()) {
        for (AnnotationElement childAnnoElement : annoElement.getChildElements()) {
          if ("schemaElementTest2".equals(childAnnoElement.getName())) {
            assertEquals("prefix", childAnnoElement.getPrefix());
            assertEquals("namespace", childAnnoElement.getNamespace());
            assertEquals("text3", childAnnoElement.getText());
          } else if ("schemaElementTest3".equals(childAnnoElement.getName())) {
            assertEquals("text4", childAnnoElement.getText());
            assertEquals("rel", childAnnoElement.getAttributes().get(0).getName());
            assertEquals("self", childAnnoElement.getAttributes().get(0).getText());
            assertEquals("", childAnnoElement.getAttributes().get(0).getPrefix());
            assertNull(childAnnoElement.getAttributes().get(0).getNamespace());
            assertEquals("href", childAnnoElement.getAttributes().get(1).getName());
            assertEquals("pre", childAnnoElement.getAttributes().get(1).getPrefix());
            assertEquals("namespaceForAnno", childAnnoElement.getAttributes().get(1).getNamespace());
            assertEquals("http://foo", childAnnoElement.getAttributes().get(1).getText());
          } else {
            throw new EntityProviderException(null, "xmlWithAnnotations");
          }
        }
      }
      for (EntityType entityType : schema.getEntityTypes()) {
        assertEquals(1, entityType.getAnnotationAttributes().size());
        AnnotationAttribute attr = entityType.getAnnotationAttributes().get(0);
        assertEquals("href", attr.getName());
        assertEquals("prefix1", attr.getPrefix());
        assertEquals("namespaceForAnno", attr.getNamespace());
        assertEquals("http://foo", attr.getText());
        for (Property property : entityType.getProperties()) {
          if ("EmployeeName".equals(property.getName())) {
            assertEquals(2, property.getAnnotationElements().size());
            for (AnnotationElement anElement : property.getAnnotationElements()) {
              if ("propertyAnnoElement".equals(anElement.getName())) {
                assertEquals("text", anElement.getText());
              }
            }
            for (AnnotationAttribute anAttribute : property.getAnnotationAttributes()) {
              assertEquals("annoName", anAttribute.getName());
              assertEquals("annoPrefix", anAttribute.getPrefix());
              assertEquals("annoText", anAttribute.getText());
              assertEquals("http://annoNamespace", anAttribute.getNamespace());
            }
          }
        }
      }
    }
  }

  private XMLStreamReader createStreamReader(final String xml) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
    factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
    XMLStreamReader streamReader = factory.createXMLStreamReader(new StringReader(xml));

    return streamReader;
  }

}
