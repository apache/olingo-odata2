package org.apache.olingo.odata2.client.core.ep.deserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmAssociationEnd;
import org.apache.olingo.odata2.api.edm.EdmAssociationSet;
import org.apache.olingo.odata2.api.edm.EdmAssociationSetEnd;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;
import org.apache.olingo.odata2.client.api.edm.EdmSchema;
import org.junit.Test;

public class XmlMetadataAssociationTest {
  private static final String NAMESPACE = "RefScenario";
  private static final String NAMESPACE2 = "RefScenario2";
  private static final String ASSOCIATION = "ManagerEmployees";
  
  private final String[] propertyNames = { "EmployeeId", "EmployeeName", "Location" };
  
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
  private final String xmlWithAssociationWithRC =
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
          + "<End Type=\"RefScenario.Manager\" Multiplicity=\"1\" Role=\"r_Manager\"/>"
          + "<ReferentialConstraint><Principal Role=\"r_Employees\">"
          + "<PropertyRef Name=\"EmployeeId\"/></Principal><Dependent Role=\"r_Manager\">"
          + "<PropertyRef Name=\"EmployeeId\"/></Dependent></ReferentialConstraint>"          
          + "</Association>"
          + "</Schema>" + "<Schema Namespace=\"" + NAMESPACE2 + "\" xmlns=\"" + Edm.NAMESPACE_EDM_2008_09 + "\">"
          + "<EntityContainer Name=\"Container1\" m:IsDefaultEntityContainer=\"true\">"
          + "<EntitySet Name=\"Employees\" EntityType=\"RefScenario.Employee\"/>"
          + "<EntitySet Name=\"Managers\" EntityType=\"RefScenario.Manager\"/>" + "<AssociationSet Name=\""
          + ASSOCIATION + "\" Association=\"RefScenario." + ASSOCIATION + "\">"
          + "<End EntitySet=\"Managers\" Role=\"r_Manager\"/>" + "<End EntitySet=\"Employees\" Role=\"r_Employees\"/>"
          + "</AssociationSet>" + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>" + "</edmx:Edmx>";

    @Test
  public void testAssociationSet() throws XMLStreamException, EntityProviderException,
  EdmException, UnsupportedEncodingException {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream content = createStreamReader(xmlWithAssociation);
    EdmDataServices result = parser.readMetadata(content, true);
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        assertEquals(NAMESPACE2, schema.getNamespace());
        assertEquals("Container1", container.getName());
        assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
        for (EdmAssociationSet assocSet : container.getAssociationSets()) {
          assertNotNull(assocSet.getEntityContainer());
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
          assertEquals("Employees", end.getEntitySet().getName());
        }
      }
    }
  }
    
    @Test
    public void testAssociationSetWithRC() throws XMLStreamException, EntityProviderException,
    EdmException, UnsupportedEncodingException {
      XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
      InputStream content = createStreamReader(xmlWithAssociationWithRC);
      EdmDataServices result = parser.readMetadata(content, true);
      EdmAssociation associations = (EdmAssociation) result.getEdm().getSchemas().get(0).getAssociations().get(0);
      assertEquals(1, associations.getReferentialConstraint().getPrincipal().getPropertyRefNames().size());
      assertEquals(1, associations.getReferentialConstraint().getDependent().getPropertyRefNames().size());
      assertEquals("EmployeeId", associations.getReferentialConstraint().getPrincipal().getPropertyRefNames().get(0));
      assertEquals("EmployeeId", associations.getReferentialConstraint().getDependent().getPropertyRefNames().get(0));
      
    }
    
    @Test
    public void testRelatedEntitySet() throws XMLStreamException, EntityProviderException,
    EdmException, UnsupportedEncodingException {
      XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
      InputStream content = createStreamReader(xmlWithAssociation);
      EdmDataServices result = parser.readMetadata(content, true);
      for (EdmSchema schema : result.getEdm().getSchemas()) {
        for (EdmEntityContainer container : schema.getEntityContainers()) {
          assertEquals(NAMESPACE2, schema.getNamespace());
          assertEquals("Container1", container.getName());
          assertEquals(Boolean.TRUE, container.isDefaultEntityContainer());
          for (EdmEntitySet entitySet : container.getEntitySets()) {
            assertNotNull(entitySet.getEntityContainer());
            for (EdmEntityType entityType : result.getEdm().getSchemas().get(0).getEntityTypes()) {
              for (String navigationPropertyName : entityType.getNavigationPropertyNames()) {
                EdmNavigationProperty navigationProperty = (EdmNavigationProperty) entityType
                    .getProperty(navigationPropertyName);
                if(entitySet.getName().equals("Managers") && navigationProperty.getName().equals("nm_Employees")){
                  assertNotNull(entitySet.getRelatedEntitySet(navigationProperty));
                }
              }
            }
          }
        }
      }
    }
    
  @Test
  public void testAssociation() throws XMLStreamException, EntityProviderException,
  EdmException, UnsupportedEncodingException {
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithAssociation);
    EdmDataServices result = parser.readMetadata(reader, true);
    assertEquals("2.0", result.getDataServiceVersion());
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmAssociation association : schema.getAssociations()) {
        EdmAssociationEnd end;
        assertEquals(ASSOCIATION, association.getName());
        if ("Employee".equals(association.getEnd1().getEntityType().getName())) {
          end = association.getEnd1();
        } else {
          end = association.getEnd2();
        }
        assertEquals(EdmMultiplicity.MANY, end.getMultiplicity());
        assertEquals("r_Employees", end.getRole());
      }
    }
  }
  
  private InputStream createStreamReader(final String xml) throws XMLStreamException,
  UnsupportedEncodingException {
    return new ByteArrayInputStream(xml.getBytes("UTF-8"));
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
  
  @Test
  public void testAnnoationsOnAssociationSet() throws Exception {
    final String employees = "r_Employees";
    final String manager = "r_Manager";
    final String xmlWithInvalidAssociationSetEnd =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\" "
            + "xmlns:sap=\"http://www.sap.com/Protocols/SAPData\">"
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
            + ASSOCIATION + "\" Association=\"RefScenario." + ASSOCIATION + "\" "
            + "sap:creatable=\"true\" sap:updatable=\"true\" "
            + "sap:deletable=\"false\">"
            + "<End EntitySet=\"Managers\" Role=\"" + manager + "\"/>" + "<End EntitySet=\"Employees\" Role=\""
            + employees + "\"/>" + "</AssociationSet>" + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>"
            + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociationSetEnd);
    EdmDataServices result = parser.readMetadata(reader, true);
    List<String> annotationList = new ArrayList<String>();
    annotationList.add("creatable");
    annotationList.add("updatable");
    annotationList.add("deletable");
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        for (EdmAssociationSet associationSet : container.getAssociationSets()) {
          assertEquals("ManagerEmployees", associationSet.getName());
          int i = 0;
          for (EdmAnnotationAttribute annotationAttr : associationSet.getAnnotations().getAnnotationAttributes()) {
            assertEquals(annotationList.get(i), annotationAttr.getName());
            assertEquals("sap", annotationAttr.getPrefix());
            assertEquals("http://www.sap.com/Protocols/SAPData", annotationAttr.getNamespace());
            i++;
          }
        }
      }
    }
    
  }
  
  @Test
  public void testRelatedEntitySetWithCyclicAssociation() throws Exception {
    final String employees = "r_Employees";
    final String manager = "r_Manager";
    final String xmlWithInvalidAssociationSetEnd =
        "<edmx:Edmx Version=\"1.0\" xmlns:edmx=\"" + Edm.NAMESPACE_EDMX_2007_06 + "\" "
            + "xmlns:sap=\"http://www.sap.com/Protocols/SAPData\">"
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
            + ASSOCIATION + "\" Association=\"RefScenario." + ASSOCIATION + "\" "
            + "sap:creatable=\"true\" sap:updatable=\"true\" "
            + "sap:deletable=\"false\">"
            + "<End EntitySet=\"Managers\" Role=\"" + manager + "\"/>" + "<End EntitySet=\"Employees\" Role=\""
            + employees + "\"/>" + "</AssociationSet>" + "</EntityContainer>" + "</Schema>" + "</edmx:DataServices>"
            + "</edmx:Edmx>";
    XmlMetadataDeserializer parser = new XmlMetadataDeserializer();
    InputStream reader = createStreamReader(xmlWithInvalidAssociationSetEnd);
    EdmDataServices result = parser.readMetadata(reader, true);
    List<String> annotationList = new ArrayList<String>();
    annotationList.add("creatable");
    annotationList.add("updatable");
    annotationList.add("deletable");
    for (EdmSchema schema : result.getEdm().getSchemas()) {
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        for (EdmEntitySet entitySet : container.getEntitySets()) {
          for(EdmEntityType entityType:schema.getEntityTypes()){
            List<String> navigationPropertyNames = entityType.getNavigationPropertyNames();
            for (String navigationPropertyName : navigationPropertyNames) {
              EdmNavigationProperty navigationProperty = (EdmNavigationProperty) entityType
                  .getProperty(navigationPropertyName);
              if((entitySet.getName().equals("Managers") && navigationProperty.getName().equals("nm_Employees")) || 
                  entitySet.getName().equals("Employees") && navigationProperty.getName().equals("ne_Manager") ){
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
