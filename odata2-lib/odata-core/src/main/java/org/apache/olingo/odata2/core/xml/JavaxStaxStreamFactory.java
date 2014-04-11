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

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.xml.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 *
 */
public class JavaxStaxStreamFactory extends AbstractXmlStreamFactory {
  /** Default used charset for reader */
  private static final String DEFAULT_CHARSET = "UTF-8";

  @Override
  public XMLStreamReader createXMLStreamReader(Object content) throws EntityProviderException {
    if (content == null) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
              .addContent("Got not allowed NULL parameter for creation of XMLStreamReader."));
    }
    javax.xml.stream.XMLStreamReader streamReader;
    try {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
      factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
      factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

      if (content instanceof InputStream) {
        streamReader = factory.createXMLStreamReader((InputStream) content, DEFAULT_CHARSET);
        // verify charset encoding set in content is supported (if not set UTF-8 is used as defined in
        // 'http://www.w3.org/TR/2008/REC-xml-20081126/')
        String characterEncodingInContent = streamReader.getCharacterEncodingScheme();
        if (characterEncodingInContent != null && !DEFAULT_CHARSET.equalsIgnoreCase(characterEncodingInContent)) {
          throw new EntityProviderException(EntityProviderException
                  .UNSUPPORTED_CHARACTER_ENCODING.addContent(characterEncodingInContent));
        }
      } else {
        throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
                .addContent("Found not supported content of class '" + content.getClass() + "' to de-serialize."));
      }
      return new JavaxStaxReaderWrapper(streamReader);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
              .getSimpleName()), e);
    }
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(Object content) throws EntityProviderException {
    if(content == null) {
      throw new IllegalArgumentException("Unsupported NULL input content.");
    }

    try {
      XMLOutputFactory xouf = XMLOutputFactory.newFactory();
      if (content instanceof OutputStream) {
        javax.xml.stream.XMLStreamWriter javaxWriter = xouf.createXMLStreamWriter((OutputStream) content);
        return new JavaxStaxWriterWrapper(javaxWriter);
      } else if (content instanceof Writer) {
        javax.xml.stream.XMLStreamWriter javaxWriter = xouf.createXMLStreamWriter((Writer) content);
        return new JavaxStaxWriterWrapper(javaxWriter);
      } else {
        throw new IllegalArgumentException("Unsupported input content with class type '" +
                content.getClass() + "'.");
      }
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
              .getSimpleName()), e);
    }
  }
}