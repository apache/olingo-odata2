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
package org.apache.olingo.odata2.client.core.ep.deserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAction;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmAssociationSetEnd;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmContentKind;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.client.api.ODataClient;
import org.apache.olingo.odata2.client.api.edm.ClientEdm;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;
import org.apache.olingo.odata2.client.api.edm.EdmSchema;
import org.apache.olingo.odata2.client.api.edm.EdmUsing;
import org.apache.olingo.odata2.client.core.edm.EdmMetadataAssociationEnd;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.EdmTestProvider;
import org.junit.Test;

public class XmlMetadataDeserializerTest extends AbstractXmlDeserializerTest {

  public XmlMetadataDeserializerTest(final StreamWriterImplType type) {
    super(type);
  }

  private static final String DEFAULT_VALUE = "Photo";
  private static final String FC_TARGET_PATH = "Содержание";
  private static final String FC_NS_URI = "http://localhost";
  private static final String FC_NS_PREFIX = "ру";
  private static final Boolean FC_KEEP_IN_CONTENT = Boolean.FALSE;
  private static final String NAMESPACE = "RefScenario";
  private static final String NAMESPACE2 = "RefScenario2";
  private static final String NAMESPACE3 = "RefScenario3";
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
  
  private final String customxml = "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
      + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
      + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
      + "<EntityType Name= \"Employee\" m:FC_TargetPath=\"SyndicationTitle\" m:HasStream=\"true\">"
      + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
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
  private final String edmxRefFor1680364709 = 
      "<edmx:Edmx xmlns:edmx=\"http://schemas.microsoft.com/ado/2007/06/edmx\" " +
      "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns:sap=\"" +
      "http://www.sap.com/Protocols/SAPData\" Version=\"1.0\">" +
      "<edmx:Reference xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" " +
      "Uri=\"https://host:port/sap/opu/odata/IWFND/CATALOGSERVICE;v=2/Vocabularies"+
      "(TechnicalName='%2FIWBEP%2FVOC_COMMON'"+
      ",Version='0001',SAP__Origin='')/$value\">" +
      "<edmx:Include Alias=\"Common\" Namespace=\"com.sap.vocabularies.Common.v1\"/>" +
      "</edmx:Reference>" + 
      "</edmx:Edmx>";
  
  @Test
  public void twoEdmxWithValidation() throws Exception {
    InputStream reader = createStreamReader(edmxRefFor1680364709); 
    EdmDataServices result = ODataClient.newInstance().readMetadata(reader, true);   
    assertNotNull(result);

  }
  
  @Test
  public void twoEdmxWithoutValidation() throws Exception {
    InputStream reader = createStreamReader(edmxRefFor1680364709); 
    EdmDataServices result = ODataClient.newInstance().readMetadata(reader, true);
    assertNotNull(result);

  }
  @Test
  public void testMetadataDokumentWithWhitepaces() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "           <EntityType Name= \"Photo\">"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Id\" />"
        + "               </Key>"
        + "               <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "               <MyAnnotation xmlns=\"http://company.com/odata\">   "
        + "                 <child> value1</child>"
        + "                 <child>value2</child>"
        + "               </MyAnnotation>"
        + "           </EntityType>"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    InputStream reader = createStreamReader(metadata); 
    EdmDataServices result = ODataClient.newInstance().readMetadata(reader, true);
    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmEntityType> entityTypes = result.getEdm().getSchemas().get(0).getEntityTypes();
    assertEquals(1, entityTypes.size());
    EdmEntityType entityType = entityTypes.get(0);
    EdmAnnotations annotation= entityType.getAnnotations();
    List<EdmAnnotationElement> annotationElements = annotation.getAnnotationElements();
    assertEquals(1, annotationElements.size());
    EdmAnnotationElement annotationElement = annotationElements.get(0);
    List<EdmAnnotationElement> childElements = annotationElement.getChildElements();
    assertEquals(2, childElements.size());

    assertEquals(" value1", childElements.get(0).getText());
    assertEquals("value2", childElements.get(1).getText());
  }

  @Test
  public void testMetadataDokumentWithWhitepacesMultiline() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "           <EntityType Name= \"Photo\">"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Id\" />"
        + "               </Key>"
        + "               <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "               <MyAnnotation xmlns=\"http://company.com/odata\">   "
        + "                 <child> value1\n"
        + "                 long long long multiline attribute</child>"
        + "                 <child>value2</child>"
        + "               </MyAnnotation>"
        + "           </EntityType>"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    EdmDataServices result = parser.readMetadata(reader, true);

    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmEntityType> entityTypes = result.getEdm().getSchemas().get(0).getEntityTypes();
    assertEquals(1, entityTypes.size());
    EdmEntityType entityType = entityTypes.get(0);
    EdmAnnotations annotations = entityType.getAnnotations();
    List<EdmAnnotationElement> annotationElements = annotations.getAnnotationElements();
    assertEquals(1, annotationElements.size());
    EdmAnnotationElement annotationElement = annotationElements.get(0);
    List<EdmAnnotationElement> childElements = annotationElement.getChildElements();
    assertEquals(2, childElements.size());

    assertEquals(" value1\n" +
        "                 long long long multiline attribute", childElements.get(0).getText());
    assertEquals("value2", childElements.get(1).getText());
  }

  @Test
  public void testMetadataDokumentWithWhitepaces2() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "           <EntityType Name= \"Photo\">"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Id\" />"
        + "               </Key>"
        + "               <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "               <MyAnnotation xmlns=\"http://company.com/odata\">   "
        + "                 <child> value1"
        + "</child></MyAnnotation>"
        + "           </EntityType>"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    EdmDataServices result = parser.readMetadata(reader, true);

    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmEntityType> entityTypes = result.getEdm().getSchemas().get(0).getEntityTypes();
    assertEquals(1, entityTypes.size());
    EdmEntityType entityType = entityTypes.get(0);
    EdmAnnotations annotations = entityType.getAnnotations();
    List<EdmAnnotationElement> annotationElements = annotations.getAnnotationElements();
    assertEquals(1, annotationElements.size());
    EdmAnnotationElement annotationElement = annotationElements.get(0);
    List<EdmAnnotationElement> childElements = annotationElement.getChildElements();
    assertEquals(1, childElements.size());

    assertEquals(" value1", childElements.get(0).getText());
  }

  @Test
  public void ODATAJAVA_77_testMetadataDokumentWithMultiLevelEntityType() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "         <EntityType Name= \"Parameter\">"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Id\" />"
        + "               </Key>"
        + "               <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "           <EntityType Name= \"ConfigParameter\" BaseType= \"RefScenario2.Parameter\" />"
        + "           <EntityType Name= \"DataConfigParameter\" BaseType= \"RefScenario2.ConfigParameter\" />"
        + "           <EntityType Name= \"StringDataConfigParameter\" BaseType= \"RefScenario2.DataConfigParameter\" />"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    EdmDataServices result = parser.readMetadata(reader, true);

    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmEntityType> entityTypes = result.getEdm().getSchemas().get(0).getEntityTypes();
    assertEquals(4, entityTypes.size());

  }

  @Test
  public void testMultiplebaseType() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "        <Using Namespace=\"com.sap.banking.common.endpoint.v1_0.beans\" Alias=\"common\"/>"
        + "        <Using Namespace=\"com.sap.banking.messages.endpoint.v1_0.beans\" Alias=\"messages\"/>" 
        + "         <EntityType Name= \"StringDataConfigParameter\" BaseType= \"RefScenario2.DataConfigParameter\" />"
        + "           <EntityType Name= \"DataConfigParameter\" BaseType= \"RefScenario2.ConfigParameter\" >"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Name\" />"
        + "               </Key>"
        + "               <Property Name=\"Name\" Type=\"Edm.String\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "           <EntityType Name= \"ConfigParameter\" BaseType= \"RefScenario2.Parameter\" >"
        + "              <Property Name=\"ConfigName\" Type=\"Edm.String\" Nullable=\"false\" />"
        + "            </EntityType>"
        + "         <EntityType Name= \"Parameter\">"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Id\" />"
        + "               </Key>"
        + "               <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    EdmDataServices result = parser.readMetadata(reader, true);

    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmEntityType> entityTypes = result.getEdm().getSchemas().get(0).getEntityTypes();
    assertEquals(4, entityTypes.size());
    for(EdmEntityType entityType:entityTypes){
      if(entityType.getName().equalsIgnoreCase("StringDataConfigParameter")){
        assertEquals(3, entityType.getPropertyNames().size());
        assertEquals("Name", entityType.getPropertyNames().get(0));  
        assertEquals("ConfigName", entityType.getPropertyNames().get(1));  
        assertEquals("Id", entityType.getPropertyNames().get(2)); 
      }else if(entityType.getName().equalsIgnoreCase("DataConfigParameter")){
        assertEquals(3, entityType.getPropertyNames().size());
        assertEquals("Name", entityType.getPropertyNames().get(0));  
        assertEquals("ConfigName", entityType.getPropertyNames().get(1));  
        assertEquals("Id", entityType.getPropertyNames().get(2)); 
      }else if(entityType.getName().equalsIgnoreCase("ConfigParameter")){
        assertEquals(2, entityType.getPropertyNames().size());
        assertEquals("ConfigName", entityType.getPropertyNames().get(0));  
        assertEquals("Id", entityType.getPropertyNames().get(1));    
      }else if(entityType.getName().equalsIgnoreCase("Parameter")){
        assertEquals(1, entityType.getPropertyNames().size());
        assertEquals("Id", entityType.getPropertyNames().get(0));
      }
      
    }

  }
  
  @Test
  public void testUsing() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "        <Using Namespace=\"com.sap.banking.common.endpoint.v1_0.beans\" Alias=\"common\"/>"
        + "        <Using Namespace=\"com.sap.banking.messages.endpoint.v1_0.beans\" Alias=\"messages\"/>"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    EdmDataServices result = parser.readMetadata(reader, false);

    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmUsing> using = result.getEdm().getSchemas().get(0).getUsings();
    assertEquals(2, using.size());
    

  }
  
  @Test
  public void testMultiplebaseTypeOrderChange() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "           <EntityType Name= \"ConfigParameter\" BaseType= \"RefScenario2.Parameter\" >"
        + "              <Property Name=\"ConfigName\" Type=\"Edm.String\" Nullable=\"false\" />"
        + "            </EntityType>"
        + "           <EntityType Name= \"DataConfigParameter\" BaseType= \"RefScenario2.ConfigParameter\" >"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Name\" />"
        + "               </Key>"
        + "               <Property Name=\"Name\" Type=\"Edm.String\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "         <EntityType Name= \"Parameter\">"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Id\" />"
        + "               </Key>"
        + "               <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "         <EntityType Name= \"StringDataConfigParameter\" BaseType= \"RefScenario2.DataConfigParameter\" />"
        + "         <EntityType Name= \"AnotherDataConfigParameter\" BaseType= \"RefScenario2.DataConfigParameter\" />"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    EdmDataServices result = parser.readMetadata(reader, true);

    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmEntityType> entityTypes = result.getEdm().getSchemas().get(0).getEntityTypes();
    assertEquals(5, entityTypes.size());
    for(EdmEntityType entityType:entityTypes){
      if(entityType.getName().equalsIgnoreCase("StringDataConfigParameter")){
        assertEquals(3, entityType.getPropertyNames().size());
        assertEquals("Name", entityType.getPropertyNames().get(0));  
        assertEquals("ConfigName", entityType.getPropertyNames().get(1));  
        assertEquals("Id", entityType.getPropertyNames().get(2)); 
      }else if(entityType.getName().equalsIgnoreCase("DataConfigParameter")){
        assertEquals(3, entityType.getPropertyNames().size());
        assertEquals("Name", entityType.getPropertyNames().get(0));  
        assertEquals("ConfigName", entityType.getPropertyNames().get(1));  
        assertEquals("Id", entityType.getPropertyNames().get(2)); 
      }else if(entityType.getName().equalsIgnoreCase("ConfigParameter")){
        assertEquals(2, entityType.getPropertyNames().size());
        assertEquals("ConfigName", entityType.getPropertyNames().get(0));  
        assertEquals("Id", entityType.getPropertyNames().get(1));    
      }else if(entityType.getName().equalsIgnoreCase("Parameter")){
        assertEquals(1, entityType.getPropertyNames().size());
        assertEquals("Id", entityType.getPropertyNames().get(0));
      }else if(entityType.getName().equalsIgnoreCase("AnotherDataConfigParameter")){
        assertEquals(3, entityType.getPropertyNames().size());
        assertEquals("Name", entityType.getPropertyNames().get(0));  
        assertEquals("ConfigName", entityType.getPropertyNames().get(1));  
        assertEquals("Id", entityType.getPropertyNames().get(2)); 
      }
      
    }

  }
  
  @Test
  public void ODATAJAVA_77_testBaseTypeKey() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "         <EntityType Name= \"Parameter\">"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Id\" />"
        + "               </Key>"
        + "               <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "           <EntityType Name= \"ConfigParameter\" BaseType= \"RefScenario2.Parameter\" />"
        + "           <EntityType Name= \"DataConfigParameter\" BaseType= \"RefScenario2.ConfigParameter\" >"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Name\" />"
        + "               </Key>"
        + "               <Property Name=\"Name\" Type=\"Edm.String\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "           <EntityType Name= \"StringDataConfigParameter\" BaseType= \"RefScenario2.DataConfigParameter\" />"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    EdmDataServices result = parser.readMetadata(reader, true);

    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmEntityType> entityTypes = result.getEdm().getSchemas().get(0).getEntityTypes();
    assertEquals(4, entityTypes.size());

  }

  @Test
  public void ODATAJAVA_77_testEntityTypeKey() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "         <EntityType Name= \"Parameter\">"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Id\" />"
        + "               </Key>"
        + "               <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "           <EntityType Name= \"ConfigParameter\" BaseType= \"RefScenario2.Parameter\" />"
        + "           <EntityType Name= \"DataConfigParameter\" BaseType= \"RefScenario2.ConfigParameter\" />"
        + "           <EntityType Name= \"StringDataConfigParameter\" BaseType= \"RefScenario2.DataConfigParameter\" >"
        + "               <Key> "
        + "                 <PropertyRef Name=\"Name\" />"
        + "               </Key>"
        + "               <Property Name=\"Name\" Type=\"Edm.String\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    EdmDataServices result = parser.readMetadata(reader, true);

    assertEquals(1, result.getEdm().getSchemas().size());
    List<EdmEntityType> entityTypes = result.getEdm().getSchemas().get(0).getEntityTypes();
    assertEquals(4, entityTypes.size());

  }

  @Test(expected = EntityProviderException.class)
  public void ODATAJAVA_77_ExceptionScenario() throws Exception {
    final String metadata = ""
        + "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "   <edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "       <Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "           <EntityType Name= \"ConfigParameter\" BaseType= \"RefScenario2.Parameter\" />"
        + "           <EntityType Name= \"DataConfigParameter\" BaseType= \"RefScenario2.ConfigParameter\" />"
        + "           <EntityType Name= \"StringDataConfigParameter\" BaseType= \"RefScenario2.DataConfigParameter\" />"
        + "         <EntityType Name= \"Parameter\">"
        + "            <Property Name=\"Id\" Type=\"Edm.Int16\" Nullable=\"false\" />"
        + "           </EntityType>"
        + "       </Schema>"
        + "  </edmx:DataServices>"
        + "</edmx:Edmx>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(metadata);
    parser.readMetadata(reader, true);
  }

  @Test
  public void stringValueForMaxLegthFacet() throws Exception {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithStringValueForMaxLengthFacet);
    EdmDataServices result = parser.readMetadata(reader, true);

    List<String> properties = result.getEdm().getSchemas().get(0).getEntityTypes().get(0).getKeyPropertyNames();
    assertEquals(1, properties.size());

    EdmProperty property = (EdmProperty) result.getEdm().getSchemas().get(0).getEntityTypes().get(0).getProperty("Id");
    EdmFacets facets = property.getFacets();
    assertEquals(new Integer(Integer.MAX_VALUE), facets.getMaxLength());

    property = (EdmProperty) result.getEdm().getSchemas().get(0).getEntityTypes().get(0).getProperty("Name");
    facets = property.getFacets();
    assertEquals(new Integer(Integer.MAX_VALUE), facets.getMaxLength());
  }


  @Test
  public void test() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    int i = 0;
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      assertEquals(NAMESPACE, schema.getNamespace());
      assertEquals(1, schema.getEntityTypes().size());
      assertEquals("Employee", schema.getEntityTypes().get(0).getName());
      assertEquals(Boolean.TRUE, schema.getEntityTypes().get(0).hasStream());
      for (EdmProperty propertyRef : schema.getEntityTypes().get(0).getKeyProperties()) {
        assertEquals("EmployeeId", propertyRef.getName());
      }
      for (String name : schema.getEntityTypes().get(0).getPropertyNames()) {
        assertEquals(propertyNames[i], name);
        EdmProperty property = (EdmProperty) schema.getEntityTypes().get(0).getProperty(name);
        if ("Location".equals(property.getName())) {
          EdmProperty cProperty =  property;
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
  public void testCustom() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(customxml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    EdmSchema schema = result.getEdm().getSchemas().get(0);
    assertNotNull(result.getEdm().getSchemas().get(0).getEntityTypes().get(0).getCustomizableFeedMappings());
    assertEquals("SyndicationTitle", schema.getEntityTypes().get(0).getCustomizableFeedMappings().getFcTargetPath());

  }
  @Test
  public void testOtherEdmNamespace() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    int i = 0;
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml2);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      assertEquals(NAMESPACE, schema.getNamespace());
      assertEquals(1, schema.getEntityTypes().size());
      assertEquals("Employee", schema.getEntityTypes().get(0).getName());
      for (EdmProperty propertyRef : schema.getEntityTypes().get(0).getKeyProperties()) {
        assertEquals("EmployeeId", propertyRef.getName());
      }
      for (String name : schema.getEntityTypes().get(0).getPropertyNames()) {
        EdmEntityType entity = schema.getEntityTypes().get(0);
        assertEquals(propertyNames[i], name);
        if ("Location".equals(name)) {
          EdmProperty cProperty = (EdmProperty) entity.getProperty(name);
          assertEquals("c_Location", cProperty.getType().getName());
        } else if ("EmployeeName".equals(name)) {
          assertNotNull(((EdmProperty) entity.getProperty(name)).getCustomizableFeedMappings());
        }
        i++;
      }
      for (EdmAnnotationElement annoElement : schema.getAnnotationElements()) {
        assertEquals("prefix", annoElement.getPrefix());
        assertEquals("namespace", annoElement.getNamespace());
        assertEquals("schemaElement", annoElement.getName());
        assertEquals("text3", annoElement.getText());
      }
    }
  }

  @Test
  public void testBaseType() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    int i = 0;
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithBaseType);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      assertEquals(NAMESPACE, schema.getNamespace());
      assertEquals(2, schema.getEntityTypes().size());
      assertEquals("Employee", schema.getEntityTypes().get(0).getName());
      for (String propertyRef : schema.getEntityTypes().get(0).getKeyPropertyNames()) {
           assertEquals("EmployeeId", propertyRef);
      }
      for (String property : schema.getEntityTypes().get(0).getPropertyNames()) {
        assertEquals(propertyNames[i], property);
        i++;
      }

    }
  }

  @Test
  public void testComplexTypeWithBaseType() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmComplexType complexType : schema.getComplexTypes()) {
        if ("c_Location".equals(complexType.getName())) {
          assertNotNull(complexType.getBaseType());
          assertEquals("c_BaseType_for_Location", complexType.getBaseType().getName());
          assertEquals("RefScenario", complexType.getBaseType().getNamespace());
        } else if ("c_Other_Location".equals(complexType.getName())) {
          assertNotNull(complexType.getBaseType());
          assertEquals("c_BaseType_for_Location", complexType.getBaseType().getName());
          assertEquals("RefScenario", complexType.getBaseType().getNamespace());
        } else if ("c_BaseType_for_Location".equals(complexType.getName())) {
          assertNotNull(complexType);
        } else {
          assertTrue(false);
        }
      }

    }
  }

  @Test(expected = EntityProviderException.class)
  public void testComplexTypeWithInvalidBaseType() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testComplexTypeWithInvalidBaseType2() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testMissingEdmxCloseTag() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xml = "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
        + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
        + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
        + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
        + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "<Property Name=\""
        + propertyNames[1] + "\" Type=\"Edm.String\" m:FC_TargetPath=\"SyndicationTitle\"/>" + "<Property Name=\""
        + propertyNames[2] + "\" Type=\"RefScenario.c_Location\" Nullable=\"false\"/>" + "</EntityType>"
        + "<ComplexType Name=\"c_Location\">" + "<Property Name=\"Country\" Type=\"Edm.String\"/>" + "</ComplexType>"
        + "</Schema>" + "</edmx:DataServices>";

    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
    parser.readMetadata(reader, true);
  }

  @Test
  public void testAssociation() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithAssociation);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    assertEquals("EdmImpl", result.getEdm().toString());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      assertEquals(schema.getNamespace()+"."+schema.getAlias(), schema.toString());
      for (EdmEntityType entityType : schema.getEntityTypes()) {
        assertEquals("RefScenario."+entityType.getName(), entityType.toString());
        if ("Manager".equals(entityType.getName())) {
          assertEquals("RefScenario", entityType.getBaseType().getNamespace());
          assertEquals("Employee", entityType.getBaseType().getName());
          for (String name : entityType.getNavigationPropertyNames()) {
            EdmNavigationProperty navProperty = (EdmNavigationProperty) entityType.getProperty(name);          
            assertEquals(name, navProperty.toString());
            assertEquals("r_Manager", navProperty.getFromRole());
            assertEquals("r_Employees", navProperty.getToRole());
            assertEquals("RefScenario", navProperty.getRelationship().getNamespace());
            assertEquals(ASSOCIATION, navProperty.getRelationship().getName());
          }
        }
        if ("Employee".equals(entityType.getName())) {
          for (String name : entityType.getNavigationPropertyNames()) {
            EdmNavigationProperty navProperty = (EdmNavigationProperty) entityType.getProperty(name);
            assertEquals(name, navProperty.toString());
            assertEquals("r_Employees", navProperty.getFromRole());
            assertEquals("RefScenario", navProperty.getRelationship().getNamespace());
            assertEquals(ASSOCIATION, navProperty.getRelationship().getName());
          }
        }
      }
      for (EdmAssociation association : schema.getAssociations()) {
        EdmMetadataAssociationEnd end;
        assertEquals(ASSOCIATION, association.getName()); 
        assertEquals("RefScenario."+association.getName(), association.toString());
        if ("Employee".equals(association.getEnd1().getEntityType().getName())) {
          end = (EdmMetadataAssociationEnd) association.getEnd1();
        } else {
          end = (EdmMetadataAssociationEnd) association.getEnd2();
        }  
        assertEquals("null null", end.toString());
        assertEquals(EdmMultiplicity.MANY, end.getMultiplicity());
        assertEquals("r_Employees", end.getRole());
        assertEquals(EdmAction.Cascade, end.getOnDelete().getAction());
      }
    }
  }

  @Test
  public void testTwoSchemas() throws XMLStreamException, EntityProviderException,
  EdmException, UnsupportedEncodingException {
    int i = 0;
    String schemasNs[] = { NAMESPACE, NAMESPACE2 };
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithTwoSchemas);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    assertEquals(2, result.getEdm().getSchemas().size());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      assertEquals(schemasNs[i], schema.getNamespace());
      assertEquals(1, schema.getEntityTypes().size());
      i++;

    }
  }

  @Test
  public void testProperties() throws EntityProviderException, 
  XMLStreamException, EdmException, UnsupportedEncodingException {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithTwoSchemas);
    EdmDataServices result = parser.readMetadata(reader, true);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityType entityType : schema.getEntityTypes()) {
        if ("Employee".equals(entityType.getName())) {
          for (String name : entityType.getPropertyNames()) {
            EdmProperty property = (EdmProperty)(entityType.getProperty(name));
            if (propertyNames[0].equals(name)) {
              assertNotNull(property.getFacets());
              assertEquals(Boolean.FALSE, property.getFacets().isNullable());
            } else if (propertyNames[1].equals(name)) {
              assertNull(property.getFacets());
            }
          }
        } else if ("Photo".equals(entityType.getName())) {
          for (String name : entityType.getPropertyNames()) {
            EdmProperty sProperty = (EdmProperty) entityType.getProperty(name);
            if ("Id".equals(sProperty.getName())) {
              assertEquals(Boolean.FALSE, sProperty.getFacets().isNullable());
              assertEquals(EdmConcurrencyMode.Fixed, sProperty.getFacets().getConcurrencyMode());
              assertEquals(new Integer(MAX_LENGTH), sProperty.getFacets().getMaxLength());
              assertTrue(sProperty.isSimple());
              assertEquals(EdmSimpleTypeKind.Int32.toString(), sProperty.getType().getName());
              assertNull(sProperty.getCustomizableFeedMappings());
            }
            if ("Name".equals(sProperty.getName())) {
              assertEquals(Boolean.TRUE, sProperty.getFacets().isUnicode());
              assertEquals(DEFAULT_VALUE, sProperty.getFacets().getDefaultValue());
              assertEquals(Boolean.FALSE, sProperty.getFacets().isFixedLength());
              assertEquals(EdmSimpleTypeKind.String.name(), sProperty.getType().getName());
              //assertEquals(EdmSimpleTypeKind.String, sProperty.getType());
              assertNull(sProperty.getCustomizableFeedMappings());
            }
            if ("Содержание".equals(sProperty.getName())) {
              assertEquals(FC_TARGET_PATH, sProperty.getCustomizableFeedMappings().getFcTargetPath());
              assertEquals(FC_NS_URI, sProperty.getCustomizableFeedMappings().getFcNsUri());
              assertEquals(FC_NS_PREFIX, sProperty.getCustomizableFeedMappings().getFcNsPrefix());
              assertEquals(FC_KEEP_IN_CONTENT, sProperty.getCustomizableFeedMappings().isFcKeepInContent());
              assertEquals(EdmContentKind.text, sProperty.getCustomizableFeedMappings().getFcContentKind());
            }
            if ("BinaryData".equals(sProperty.getName())) {
              assertEquals(MIME_TYPE, sProperty.getMimeType());
            }
          }
        }
      }
    }
  }

  @Test
  public void testEntitySet() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("Container1", result.getEdm().getEntityContainer("Container1").getName());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        assertEquals("Container1", container.getName());
        assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
        for (EdmEntitySet entitySet : container.getEntitySets()) {
          assertEquals("Employees", entitySet.getName());
          assertEquals("Employee", entitySet.getEntityType().getName());
          assertEquals(NAMESPACE, entitySet.getEntityType().getNamespace());
        }
      }
    }
  }

  @Test
  public void testAssociationSet() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithAssociation);
    EdmDataServices result = parser.readMetadata(reader, true);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        assertEquals(NAMESPACE2, schema.getNamespace());
        assertEquals("Container1", container.getName());
        assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
        assertNotNull(result.getEdm().getDefaultEntityContainer());
        assertEquals("Container1", result.getEdm().getDefaultEntityContainer().getName());
        for (EdmAssociationSet assocSet : container.getAssociationSets()) {
          assertEquals(ASSOCIATION, assocSet.getName());
          assertEquals(ASSOCIATION, assocSet.getAssociation().getName());
          assertEquals(NAMESPACE, assocSet.getAssociation().getNamespace());
          EdmAssociationSetEnd end;
          if ("Employees".equals(assocSet.getEnd("r_Employees").getEntitySet().getName())) {
            end = assocSet.getEnd("r_Employees");
          } else {
            end = assocSet.getEnd("r_Manager");
          }
          assertEquals("r_Employees", end.getRole());
        }
      }
    }
  }

 

  @Test()
  public void testAlias() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xml =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" Alias=\"RS\"  xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>" + "<Property Name=\""
            + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityType Name=\"Manager\" BaseType=\"RS.Employee\" m:HasStream=\"true\">" + "</EntityType>"
            + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      assertEquals("RS", schema.getAlias());
      for (EdmEntityType entityType : schema.getEntityTypes()) {
        if ("Manager".equals(entityType.getName())) {
          assertEquals("Employee", entityType.getBaseType().getName());
          assertEquals("RefScenario", entityType.getBaseType()
              .getNamespace());
        }
      }

    }
  }

  @Test(expected = EntityProviderException.class)
  public void testEntityTypeWithoutKeys() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmlWithoutKeys =
        "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\">" + "<Property Name=\"" + propertyNames[0]
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "</EntityType>" + "</Schema>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithoutKeys);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidBaseType() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmlWithInvalidBaseType =
        "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Manager\" BaseType=\"Employee\" m:HasStream=\"true\">" + "</EntityType>"
            + "</Schema>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidBaseType);
    parser.readMetadata(reader, true);
  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidRole() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociation);
    parser.readMetadata(reader, true);
  }
 
  @Test(expected = EntityProviderException.class)
  public void testInvalidRelationship() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociation);
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociation);
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociation);
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociation);
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociation);
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithAssociation);
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

  @Test(expected = EdmException.class)
  public void testInvalidAssociation() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociationSet);
    parser.readMetadata(reader, true);

  }

  @Test(expected = EdmException.class)
  public void testInvalidAssociationEnd() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociationSetEnd);
    parser.readMetadata(reader, true);

  }

  @Test(expected = EdmException.class)
  public void testInvalidAssociationEnd2() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociationSetEnd);
    parser.readMetadata(reader, true);

  }

  @Test(expected = EntityProviderException.class)
  public void testInvalidEntitySet() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    parser.readMetadata(reader, true);

  }

  @Test
  public void testEntityTypeInOtherSchema() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        assertEquals("Container1", container.getName());
        for (EdmEntitySet entitySet : container.getEntitySets()) {
          assertEquals(NAMESPACE2, entitySet.getEntityType().getNamespace());
          assertEquals("Photo", entitySet.getEntityType().getName());
        }
      }
    }
  }

  @Test
  public void testMultipleSchemas() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Photos\" EntityType=\"" + NAMESPACE2 + ".Photo\"/>" + "</EntityContainer>"
            + "<EntityContainer Name=\"Container2\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Sign\" EntityType=\"" + NAMESPACE3 + ".Sign\"/>" + "</EntityContainer>"
            + "</Schema>"

            + "<Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Photo\">" + "<Key><PropertyRef Name=\"Id\"/></Key>"
            + "<Property Name=\"Id\" Type=\"Edm.Int32\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityType Name= \"Name\">" + "<Key><PropertyRef Name=\"NameId\"/></Key>"
            + "<Property Name=\"NameId\" Type=\"Edm.Int32\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityContainer Name=\"Container2\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Name\" EntityType=\"" + NAMESPACE2 + ".Name\"/>"
            + "<EntitySet Name=\"Photo\" EntityType=\"" + NAMESPACE2 + ".Photo\"/>"
            + "</EntityContainer>"
            + "</Schema>"

            + "<Schema Namespace=\"" + NAMESPACE3 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Sign\">" + "<Key><PropertyRef Name=\"Id\"/></Key>"
            + "<Property Name=\"Id\" Type=\"Edm.Int32\" Nullable=\"false\"/>" + "</EntityType>" + "</Schema>"
            + "</edmx:DataServices>"
            + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        if (container.getName().equals("Container1")) {
          assertEquals("Container1", container.getName());
          for (EdmEntitySet entitySet : container.getEntitySets()) {
            if (entitySet.getEntityType().getNamespace().equals(NAMESPACE2)) {
              assertEquals(NAMESPACE2, entitySet.getEntityType().getNamespace());
              assertEquals("Photo", entitySet.getEntityType().getName());
            } else if (entitySet.getEntityType().getNamespace().equals(NAMESPACE3)) {
              assertEquals(NAMESPACE3, entitySet.getEntityType().getNamespace());
              assertEquals("Sign", entitySet.getEntityType().getName());
            }
          }
        } else if (container.getName().equals("Container2")) {

          assertEquals("Container2", container.getName());
          for (EdmEntitySet entitySet : container.getEntitySets()) {
            if (NAMESPACE2.equals(entitySet.getEntityType().getNamespace())) {
              assertEquals(NAMESPACE2, entitySet.getEntityType().getNamespace());
              if (entitySet.getEntityType().getName().equals("Name")) {
                assertEquals("Name", entitySet.getEntityType().getName());
              } else {
                assertEquals("Photo", entitySet.getEntityType().getName());
              }
            } else {
              assertEquals(NAMESPACE3, entitySet.getEntityType().getNamespace());
              assertEquals("Sign", entitySet.getEntityType().getName());

            }
          }
        }
      }
    }
  }

  @Test
  public void testEntityType() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
    final String xmWithEntityContainer =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\">"
            + "<edmx:DataServices m:DataServiceVersion=\"2.0\" xmlns:m=\"" + Edm.NAMESPACE_M_2007_08 + "\">"
            + "<Schema Namespace=\"" + NAMESPACE + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Employee\" m:HasStream=\"true\">" + "<Key><PropertyRef Name=\"EmployeeId\"/></Key>"
            + "<Property Name=\"" + propertyNames[0] + "\" Type=\"Edm.String\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Photos\" EntityType=\"" + NAMESPACE2 + ".Photo\"/>" + "</EntityContainer>"
            + "<EntityContainer Name=\"Container2\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Sign\" EntityType=\"" + NAMESPACE3 + ".Sign\"/>" + "</EntityContainer>"
            + "</Schema>"

            + "<Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Photo\">" + "<Key><PropertyRef Name=\"Id\"/></Key>"
            + "<Property Name=\"Id\" Type=\"Edm.Int32\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityType Name= \"Name\">" + "<Key><PropertyRef Name=\"NameId\"/></Key>"
            + "<Property Name=\"NameId\" Type=\"Edm.Int32\" Nullable=\"false\"/>" + "</EntityType>"
            + "<EntityContainer Name=\"Container2\" m:IsDefaultEntityContainer=\"true\">"
            + "<EntitySet Name=\"Name\" EntityType=\"" + NAMESPACE2 + ".Name\"/>"
            + "<EntitySet Name=\"Photo\" EntityType=\"" + NAMESPACE2 + ".Photo\"/>"
            + "</EntityContainer>"
            + "</Schema>"

            + "<Schema Namespace=\"" + NAMESPACE3 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
            + "<EntityType Name= \"Sign\">" + "<Key><PropertyRef Name=\"Id\"/></Key>"
            + "<Property Name=\"Id\" Type=\"Edm.Int32\" Nullable=\"false\"/>" + "</EntityType>" + "</Schema>"
            + "</edmx:DataServices>"
            + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmWithEntityContainer);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    assertEquals(NAMESPACE, result.getEdm().getSchemas().get(0).getNamespace());
    assertEquals(NAMESPACE2, result.getEdm().getSchemas().get(1).getNamespace());
    assertEquals(NAMESPACE3, result.getEdm().getSchemas().get(2).getNamespace());
    
    assertEquals("Employee", result.getEdm().getSchemas().get(0).getEntityTypes().get(0).getName());
    assertNotNull(result.getEdm().getSchemas().get(0).getEntityTypes().get(0).getProperty("EmployeeId"));

    assertEquals("Photo", result.getEdm().getSchemas().get(1).getEntityTypes().get(0).getName());
    assertNotNull(result.getEdm().getSchemas().get(1).getEntityTypes().get(0).getProperty("Id"));   
    assertEquals("Name", result.getEdm().getSchemas().get(1).getEntityTypes().get(1).getName());
    assertEquals("NameId", result.getEdm().getSchemas().get(1).getEntityTypes()
        .get(1).getKeyProperties().get(0).getName());  

    assertEquals("Sign", result.getEdm().getSchemas().get(2).getEntityTypes().get(0).getName());
    assertNotNull(result.getEdm().getSchemas().get(2).getEntityTypes().get(0).getProperty("Id"));
    
  }

  @Test
  public void testMetadataRelatedEntitySet() throws XMLStreamException, 
  EntityProviderException, EdmException, IOException {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    String xml = readFile("metadataForRelatedEntity.xml");
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals(1, result.getEdm().getSchemas().size());
    ClientEdm edm = result.getEdm();
    assertNotNull(edm);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        if (container.getName().equals("CUAN_BUSINESS_DOCUMENT_IMP_SRV_Entities")) {
          assertEquals("CUAN_BUSINESS_DOCUMENT_IMP_SRV_Entities", container.getName());
          for (EdmEntitySet entitySet : container.getEntitySets()) {
            for(EdmEntityType entityType:schema.getEntityTypes()){
              List<String> navigationPropertyNames = entityType.getNavigationPropertyNames();
              for (String navigationPropertyName : navigationPropertyNames) {
                EdmNavigationProperty navigationProperty = (EdmNavigationProperty) entityType
                    .getProperty(navigationPropertyName);
                if(((navigationProperty.getName().equals("BusinessDocuments") &&                    
                    ("ImportHeaders").equals(entitySet.getName())) ||
                    (((navigationProperty.getName().equals("ProductItems") ||  
                        navigationProperty.getName().equals("Offers") ||
                        navigationProperty.getName().equals("Company") ||
                        navigationProperty.getName().equals("AdditionalObjectReferences") ||
                        navigationProperty.getName().equals("Person")) &&                         
                    ("BusinessDocuments").equals(entitySet.getName()))))
                    && !navigationProperty.getName().equals(entitySet.getName()) ){
                  assertNotNull(entitySet.getRelatedEntitySet(navigationProperty));
                }else{
                  assertNull(entitySet.getRelatedEntitySet(navigationProperty));
                }
                }
              
            }
          }
        }
      }  

    }
  }
  
  @Test
  public void testMetadataWithNavigatons() throws XMLStreamException, 
  EntityProviderException, EdmException, IOException {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    String xml = readFile("metadataWithNavigations.xml");
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals(2, result.getEdm().getSchemas().size());
    ClientEdm edm = result.getEdm();
    assertNotNull(edm);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        assertNotNull(container.getEntitySets());
        int i=0;
        for(EdmEntitySet entitySet:container.getEntitySets()){
          assertEquals(edm.getSchemas().get(1).getEntityTypes().get(i).getName(), 
              entitySet.getEntityType().getName());
          assertEquals(edm.getSchemas().get(1).getEntityTypes().get(i++).getKeyProperties().get(0), 
              entitySet.getEntityType().getKeyProperties().get(0));
        }
      }  

    }
  }
  
  protected String readFile(final String filename) throws IOException {
    InputStream in = getFileAsStream(filename);

    byte[] tmp = new byte[8192];
    int count = in.read(tmp);
    StringBuilder b = new StringBuilder();
    while (count >= 0) {
      b.append(new String(tmp, 0, count));
      count = in.read(tmp);
    }

    return b.toString();
  }
    
  @Test
  public void scenarioTest() throws XMLStreamException, 
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
            + "<Property Name=\""
            + "Id"
            + "\" Type=\"Edm.String\" Nullable=\"false\"/>"
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xml);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals(2, result.getEdm().getSchemas().size());
    ClientEdm edm = result.getEdm();
    assertEquals(4, edm.getEntitySets().size());
    assertEquals(0, edm.getFunctionImports().size());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
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
        for (EdmEntityType entityType : schema.getEntityTypes()) {
          assertEquals(2, entityType.getKeyProperties().size());
        }
      }
    }
  }

  @Test
  public void testRefScenario() throws Exception {
    EdmProvider testProvider = new EdmTestProvider();
    ODataResponse response = EntityProvider.writeMetadata(testProvider.getSchemas(), null);

    String stream = StringHelper.inputStreamToString((InputStream) response.getEntity());
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    EdmDataServices result = parser.readMetadata(createStreamReader(stream), true);
   
    assertNotNull(result);
  }

  @Test
  public void testAnnotations() throws XMLStreamException,
  EntityProviderException, EdmException, UnsupportedEncodingException {
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
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithAnnotations);
    EdmDataServices result = parser.readMetadata(reader, false);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      assertEquals(1, schema.getAnnotationElements().size());
      for (EdmAnnotationElement annoElement : schema.getAnnotationElements()) {
        for (EdmAnnotationElement childAnnoElement : annoElement.getChildElements()) {
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
      for (EdmEntityType entityType : schema.getEntityTypes()) {
        assertEquals(1, entityType.getAnnotations().getAnnotationAttributes().size());
        EdmAnnotationAttribute attr = entityType.getAnnotations().getAnnotationAttributes().get(0);
        assertEquals("href", attr.getName());
        assertEquals("prefix1", attr.getPrefix());
        assertEquals("namespaceForAnno", attr.getNamespace());
        assertEquals("http://foo", attr.getText());
        for (String name : entityType.getPropertyNames()) {
          if ("EmployeeName".equals(name)) {
            EdmProperty property = (EdmProperty) entityType.getProperty(name);
            assertEquals(2, property.getAnnotations().getAnnotationElements().size());
            for (EdmAnnotationElement anElement : property.getAnnotations().getAnnotationElements()) {
              if ("propertyAnnoElement".equals(anElement.getName())) {
                assertEquals("text", anElement.getText());
              }
            }
            for (EdmAnnotationAttribute anAttribute : property.getAnnotations().getAnnotationAttributes()) {
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

  
  private InputStream createStreamReader(final String xml) throws
  XMLStreamException, UnsupportedEncodingException {
    return new ByteArrayInputStream(xml.getBytes("UTF-8"));
  }

}
