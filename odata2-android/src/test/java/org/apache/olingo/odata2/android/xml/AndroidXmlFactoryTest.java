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
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamReader;
import org.apache.olingo.odata2.api.xml.XMLStreamWriter;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.apache.olingo.odata2.api.xml.XMLStreamReaderFactory.XML_STREAM_READER_FACTORY_CLASS;
import static org.apache.olingo.odata2.api.xml.XMLStreamWriterFactory.XML_STREAM_WRITER_FACTORY_CLASS;

/**
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class AndroidXmlFactoryTest {

  private AndroidXmlFactory streamFactory;

  public static final String BASIC_CONTENT = "<?xml version='1.0' encoding='UTF-8' ?><start />";

  // CHECKSTYLE:OFF
  @Before
  public void init() {
    //
    System.setProperty(XML_STREAM_READER_FACTORY_CLASS, AndroidXmlFactoryTest.class.getName()); // NOSONAR
    System.setProperty(XML_STREAM_WRITER_FACTORY_CLASS, AndroidXmlFactoryTest.class.getName()); // NOSONAR
    //
    streamFactory = new AndroidXmlFactory();
  }
  // CHECKSTYLE:ON


  @Test
//  @Ignore("Will work with robolectric version 2.3")
  public void createReader() throws Exception {
    InputStream stream = StringHelper.encapsulate(BASIC_CONTENT);
    XMLStreamReader xmlReader = streamFactory.createXMLStreamReader(stream);
    assertNotNull(xmlReader);
    Assert.assertTrue(xmlReader.hasNext());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createReaderWrongContent() throws Exception {
    XMLStreamReader xmlReader = streamFactory.createXMLStreamReader("content");
    assertNotNull(xmlReader);
    Assert.assertTrue(xmlReader.hasNext());
  }

  @Test
  public void createWriterStream() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    XMLStreamWriter xmlReader = streamFactory.createXMLStreamWriter(stream);
    xmlReader.writeStartDocument();
    xmlReader.writeStartElement("start");
    xmlReader.writeEndElement();
    xmlReader.writeEndDocument();
    xmlReader.flush();

    assertNotNull(xmlReader);
    assertEquals(BASIC_CONTENT, new String(stream.toByteArray()));
  }

  @Test
  public void createWriterPrintWriter() throws Exception {

    StringWriter writer = new StringWriter();
    XMLStreamWriter xmlReader = streamFactory.createXMLStreamWriter(writer);
    xmlReader.writeStartDocument();
    xmlReader.writeStartElement("start");
    xmlReader.writeEndElement();
    xmlReader.writeEndDocument();
    xmlReader.flush();

    assertNotNull(xmlReader);
    assertEquals(BASIC_CONTENT, writer.toString());
  }

  @Test(expected = XMLStreamException.class)
  public void createWriterWrongContent() throws Exception {
    XMLStreamWriter xmlReader = streamFactory.createXMLStreamWriter("fail");
    assertNotNull(xmlReader);
  }
}
