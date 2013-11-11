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
package org.apache.olingo.odata2.core.commons;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

public class XmlHelperTest {

  public static String XML =
      "<?xml version=\"1.0\"?>" +
          "<extract>" +
          "  <data>&rules;</data>" +
          "</extract>";

  public static String XML_XXE =
      "<?xml version=\"1.0\"?>" +
          "  <!DOCTYPE foo [" +
          "    <!ENTITY rules SYSTEM \"" + XmlHelperTest.class.getResource("/xxe.xml").toString() + "\">" +
          "  ]>" +
          "<extract>" +
          "  <data>&rules;</data>" +
          "</extract>";

  @Test
  public void createReader() throws Exception {
    InputStream content = new ByteArrayInputStream(XML.getBytes("UTF-8"));
    XMLStreamReader streamReader = XmlHelper.createStreamReader(content);
    assertNotNull(streamReader);
  }

  @Test
  public void xxeWithoutProtection() throws Exception {
    InputStream content = new ByteArrayInputStream(XML_XXE.getBytes("UTF-8"));
    XMLStreamReader streamReader = createStreamReaderWithExternalEntitySupport(content);

    boolean foundExternalEntity = false;

    while (streamReader.hasNext()) {
      streamReader.next();

      if (streamReader.hasText() && "some text".equals(streamReader.getText())) {
        foundExternalEntity = true;
        break;
      }

    }
    assertTrue(foundExternalEntity);
  }

  @Test(expected = XMLStreamException.class)
  public void xxeWithProtection() throws Exception {
    InputStream content = new ByteArrayInputStream(XML_XXE.getBytes("UTF-8"));
    XMLStreamReader streamReader = XmlHelper.createStreamReader(content);

    while (streamReader.hasNext()) {
      streamReader.next();
    }
  }

  public XMLStreamReader createStreamReaderWithExternalEntitySupport(final InputStream content) throws Exception {
    XMLStreamReader streamReader;
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
    factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, true);

    streamReader = factory.createXMLStreamReader(content, "UTF-8");
    return streamReader;
  }

}
