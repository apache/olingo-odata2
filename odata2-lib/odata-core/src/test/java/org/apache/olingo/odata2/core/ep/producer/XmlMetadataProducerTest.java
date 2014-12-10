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
package org.apache.olingo.odata2.core.ep.producer;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.AnnotationAttribute;
import org.apache.olingo.odata2.api.edm.provider.AnnotationElement;
import org.apache.olingo.odata2.api.edm.provider.DataServices;
import org.apache.olingo.odata2.api.edm.provider.Documentation;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.core.ep.AbstractXmlProducerTestHelper;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

public class XmlMetadataProducerTest extends AbstractXmlProducerTestHelper {

  private XMLOutputFactory xmlStreamWriterFactory;

  public XmlMetadataProducerTest(final StreamWriterImplType type) {
    super(type);
  }

  @Before
  public void before() {
    xmlStreamWriterFactory = XMLOutputFactory.newInstance();
  }

  @Test
  public void writeValidMetadata() throws Exception {
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    annotationElements.add(new AnnotationElement().setName("test").setText("hallo"));
    Schema schema = new Schema().setAnnotationElements(annotationElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    String metadata = StringHelper.inputStreamToString(csb.getInputStream());
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:test", metadata);
  }

  @Test
  public void writeValidMetadata2() throws Exception {
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationElement> childElements = new ArrayList<AnnotationElement>();
    childElements
        .add(new AnnotationElement().setName("schemaElementTest2").setText("text2").setNamespace("namespace1"));

    List<AnnotationAttribute> elementAttributes = new ArrayList<AnnotationAttribute>();
    elementAttributes.add(new AnnotationAttribute().setName("rel").setText("self"));
    elementAttributes.add(new AnnotationAttribute().setName("href").setText("http://google.com").setPrefix("pre")
        .setNamespace("namespaceForAnno"));

    List<AnnotationElement> element3List = new ArrayList<AnnotationElement>();
    element3List.add(new AnnotationElement().setName("schemaElementTest4").setText("text4").setAttributes(
        elementAttributes));
    childElements.add(new AnnotationElement().setName("schemaElementTest3").setText("text3").setPrefix("prefix")
        .setNamespace("namespace2").setChildElements(element3List));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setText("text1").setChildElements(
        childElements));

    schemaElements.add(new AnnotationElement().setName("test"));
    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("b", "namespace1");
    prefixMap.put("prefix", "namespace2");
    prefixMap.put("pre", "namespaceForAnno");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    String metadata = StringHelper.inputStreamToString(csb.getInputStream());
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:test", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:schemaElementTest1/b:schemaElementTest2", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:schemaElementTest1/prefix:schemaElementTest3", metadata);
    assertXpathExists(
        "/edmx:Edmx/edmx:DataServices/a:Schema/a:schemaElementTest1/prefix:schemaElementTest3/" +
            "a:schemaElementTest4[@rel=\"self\" and @pre:href=\"http://google.com\"]",
        metadata);

  }

  @Test
  public void writeValidMetadata3() throws Exception {
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    annotationElements.add(new AnnotationElement().setName("test").setText("hallo)"));
    Schema schema = new Schema().setAnnotationElements(annotationElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    List<PropertyRef> keys = new ArrayList<PropertyRef>();
    keys.add(new PropertyRef().setName("Id"));
    Key key = new Key().setKeys(keys);
    List<Property> properties = new ArrayList<Property>();
    properties.add(new SimpleProperty().setName("Id").setType(EdmSimpleTypeKind.String));
    EntityType entityType = new EntityType().setName("testType").setKey(key).setProperties(properties);
    entityType.setDocumentation(new Documentation());

    List<PropertyRef> keys2 = new ArrayList<PropertyRef>();
    keys2.add(new PropertyRef().setName("SecondId"));
    Key key2 = new Key().setKeys(keys2);
    List<Property> properties2 = new ArrayList<Property>();
    properties2.add(new SimpleProperty().setName("SecondId").setType(EdmSimpleTypeKind.String));
    EntityType entityType2 = new EntityType().setName("SecondTestType").setKey(key2).setProperties(properties2);
    entityType2.setDocumentation(new Documentation().setSummary("Doc_TlDr").setLongDescription("Some long desc."));
    List<EntityType> entityTypes = new ArrayList<EntityType>();
    entityTypes.add(entityType);
    entityTypes.add(entityType2);
    schema.setEntityTypes(entityTypes);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    CircleStreamBuffer csb = new CircleStreamBuffer();
    OutputStreamWriter writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    String metadata = StringHelper.inputStreamToString(csb.getInputStream());
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:test", metadata);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:EntityType[@Name=\"testType\"]", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/" +
            "a:EntityType[@Name=\"SecondTestType\"]/a:Documentation/a:Summary", metadata);
  }

  // Elements with namespace and attributes without namespace
  @Test
  public void writeValidMetadata4() throws Exception {

    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationAttribute> attributesElement1 = new ArrayList<AnnotationAttribute>();
    attributesElement1.add(new AnnotationAttribute().setName("rel").setText("self"));
    attributesElement1.add(new AnnotationAttribute().setName("href").setText("link"));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom").setAttributes(attributesElement1));
    schemaElements.add(new AnnotationElement().setName("schemaElementTest2").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom").setAttributes(attributesElement1));

    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);
    String metadata = StringHelper.inputStreamToString(csb.getInputStream());

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("atom", "http://www.w3.org/2005/Atom");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest2", metadata);
  }

  // Element with namespace and attributes with same namespace
  @Test
  public void writeValidMetadata5() throws Exception {

    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationAttribute> attributesElement1 = new ArrayList<AnnotationAttribute>();
    attributesElement1.add(new AnnotationAttribute().setName("rel").setText("self").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom"));
    attributesElement1.add(new AnnotationAttribute().setName("href").setText("link").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom"));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom").setAttributes(attributesElement1));
    schemaElements.add(new AnnotationElement().setName("schemaElementTest2").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom").setAttributes(attributesElement1));

    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);
    String metadata = StringHelper.inputStreamToString(csb.getInputStream());

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("atom", "http://www.w3.org/2005/Atom");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest2", metadata);
  }

  // Element with namespace childelements with same namespace
  @Test
  public void writeValidMetadata6() throws Exception {

    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationAttribute> attributesElement1 = new ArrayList<AnnotationAttribute>();
    attributesElement1.add(new AnnotationAttribute().setName("rel").setText("self").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom"));
    attributesElement1.add(new AnnotationAttribute().setName("href").setText("link").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom"));

    List<AnnotationElement> elementElements = new ArrayList<AnnotationElement>();
    elementElements.add(new AnnotationElement().setName("schemaElementTest2").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom").setAttributes(attributesElement1));
    elementElements.add(new AnnotationElement().setName("schemaElementTest3").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom").setAttributes(attributesElement1));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setPrefix("atom").setNamespace(
        "http://www.w3.org/2005/Atom").setAttributes(attributesElement1).setChildElements(elementElements));

    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);
    String metadata = StringHelper.inputStreamToString(csb.getInputStream());

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("atom", "http://www.w3.org/2005/Atom");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1/atom:schemaElementTest2",
        metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1/atom:schemaElementTest3",
        metadata);
  }

  // If no name for an AnnotationAttribute is set this has to result in an Exception
  @Test(expected = Exception.class)
  public void writeInvalidMetadata() throws Exception {
    disableLogging(this.getClass());
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    annotationElements.add(new AnnotationElement().setText("hallo"));
    Schema schema = new Schema().setAnnotationElements(annotationElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);
  }

  // Element with predefined namespace
  @Test
  public void writeWithPredefinedNamespaces() throws Exception {
    // prepare
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationAttribute> attributesElement1 = new ArrayList<AnnotationAttribute>();
    attributesElement1.add(new AnnotationAttribute().setName("rel").setText("self").setPrefix("foo").setNamespace(
        "http://www.foo.bar/Protocols/Data"));
    attributesElement1.add(new AnnotationAttribute().setName("href").setText("link").setPrefix("foo").setNamespace(
        "http://www.foo.bar/Protocols/Data"));

    List<AnnotationElement> elementElements = new ArrayList<AnnotationElement>();
    elementElements.add(new AnnotationElement().setName("schemaElementTest2").setPrefix("foo").setNamespace(
        "http://www.foo.bar/Protocols/Data").setAttributes(attributesElement1));
    elementElements.add(new AnnotationElement().setName("schemaElementTest3").setPrefix("foo").setNamespace(
        "http://www.foo.bar/Protocols/Data").setAttributes(attributesElement1));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setPrefix("foo").setNamespace(
        "http://www.foo.bar/Protocols/Data").setAttributes(attributesElement1).setChildElements(elementElements));

    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    // Execute
    Map<String, String> predefinedNamespaces = new HashMap<String, String>();
    predefinedNamespaces.put("foo", "http://www.foo.bar/Protocols/Data");
    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, predefinedNamespaces);
    String metadata = StringHelper.inputStreamToString(csb.getInputStream());

    // Verify
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("foo", "http://www.foo.bar/Protocols/Data");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/foo:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/foo:schemaElementTest1/foo:schemaElementTest2", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/foo:schemaElementTest1/foo:schemaElementTest3", metadata);
  }
}
