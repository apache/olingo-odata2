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

import junit.framework.Assert;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.xml.XMLStreamConstants;
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamReader;
import org.apache.olingo.odata2.api.xml.XMLStreamWriter;
import org.apache.olingo.odata2.core.ep.AbstractXmlProducerTestHelper;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.apache.olingo.odata2.api.xml.XMLStreamReaderFactory.XML_STREAM_READER_FACTORY_CLASS;
import static org.apache.olingo.odata2.api.xml.XMLStreamWriterFactory.XML_STREAM_WRITER_FACTORY_CLASS;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

/**
 */
public class JavaxStaxStreamFactoryTest extends AbstractXmlProducerTestHelper {

  private JavaxStaxStreamFactory javaxStaxStreamFactory;

  public static final String BASIC_CONTENT = "<?xml version='1.0' encoding='UTF-8'?><start/>";

  public JavaxStaxStreamFactoryTest(StreamWriterImplType type) {
    super(type);
  }

  // CHECKSTYLE:OFF
  @Before
  public void init() {
    //
    System.setProperty(XML_STREAM_READER_FACTORY_CLASS, JavaxStaxStreamFactory.class.getName()); // NOSONAR
    System.setProperty(XML_STREAM_WRITER_FACTORY_CLASS, JavaxStaxStreamFactory.class.getName()); // NOSONAR
    //
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    prefixMap.put("xml", Edm.NAMESPACE_XML_1998);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
    javaxStaxStreamFactory = new JavaxStaxStreamFactory();
  }
  // CHECKSTYLE:ON


  @Test
  public void createReader() throws Exception {
    InputStream stream = StringHelper.encapsulate(BASIC_CONTENT);
    XMLStreamReader xmlReader = javaxStaxStreamFactory.createXMLStreamReader(stream);
    assertNotNull(xmlReader);
    Assert.assertTrue(xmlReader.hasNext());
  }

  @Test(expected = EntityProviderException.class)
  public void createReaderWrongContent() throws Exception {
    XMLStreamReader xmlReader = javaxStaxStreamFactory.createXMLStreamReader("content");
    assertNotNull(xmlReader);
    Assert.assertTrue(xmlReader.hasNext());
  }

  @Test
  public void createWriterStream() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    XMLStreamWriter xmlReader = javaxStaxStreamFactory.createXMLStreamWriter(stream);
    xmlReader.writeStartDocument();
    xmlReader.writeStartElement("start");
    xmlReader.writeEndElement();
    xmlReader.writeEndDocument();
    xmlReader.flush();

    assertNotNull(xmlReader);
    String content = new String(stream.toByteArray());
    assertXpathExists("/start", content);
  }

  @Test
  public void createWriterPrintWriter() throws Exception {

    StringWriter writer = new StringWriter();
    XMLStreamWriter xmlReader = javaxStaxStreamFactory.createXMLStreamWriter(writer);
    xmlReader.writeStartDocument();
    xmlReader.writeStartElement("start");
    xmlReader.writeEndElement();
    xmlReader.writeEndDocument();
    xmlReader.flush();

    assertNotNull(xmlReader);
    assertXpathExists("/start", writer.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createWriterWrongContent() throws Exception {
    XMLStreamWriter xmlReader = javaxStaxStreamFactory.createXMLStreamWriter("fail");
    assertNotNull(xmlReader);
  }
}
