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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * Consuming (read / deserialization) for OData error document in XML format.
 */
public class XmlErrorDocumentConsumer {
  /**
   * Map containing language code (language - country) to Locale mapping
   * based on Locale.getAvailableLocales()
   * */
  private final static Map<String, Locale> AVAILABLE_LOCALES = new HashMap<String, Locale>();
  static {
    Locale[] locales = Locale.getAvailableLocales();
    for (Locale l : locales) {
      AVAILABLE_LOCALES.put(l.getLanguage() + "-" + l.getCountry(), l);
    }
  }

  /**
   * Deserialize / read OData error document in ODataErrorContext.
   * 
   * @param errorDocument OData error document in XML format
   * @return created ODataErrorContext based on input stream content.
   * @throws EntityProviderException if an exception during read / deserialization occurs.
   */
  public ODataErrorContext readError(final InputStream errorDocument) throws EntityProviderException {
    XMLStreamReader reader = null;
    EntityProviderException cachedException = null;

    try {
      reader = XmlHelper.createStreamReader(errorDocument);
      return parserError(reader);
    } catch (XMLStreamException e) {
      cachedException = new EntityProviderException(EntityProviderException.INVALID_STATE.addContent(
          e.getMessage()), e);
      throw cachedException;
    } catch (EntityProviderException e) {
      cachedException = e;
      throw cachedException;
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      if (reader != null) {
        try {
          reader.close();
        } catch (XMLStreamException e) {
          if (cachedException != null) {
            throw cachedException;
          } else {
            throw new EntityProviderException(
                EntityProviderException.EXCEPTION_OCCURRED.addContent(
                    e.getClass().getSimpleName()), e);
          }
        }
      }
    }
  }

  private ODataErrorContext parserError(final XMLStreamReader reader)
      throws XMLStreamException, EntityProviderException {
    // read xml tag
    reader.require(XMLStreamConstants.START_DOCUMENT, null, null);
    reader.nextTag();

    // read error tag
    reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_M_2007_08, FormatXml.M_ERROR);

    // read error data
    boolean codeFound = false;
    boolean messageFound = false;
    ODataErrorContext errorContext = new ODataErrorContext();
    while (notFinished(reader)) {
      reader.nextTag();
      if (reader.isStartElement()) {
        String name = reader.getLocalName();
        if (FormatXml.M_CODE.equals(name)) {
          codeFound = true;
          handleCode(reader, errorContext);
        } else if (FormatXml.M_MESSAGE.equals(name)) {
          messageFound = true;
          handleMessage(reader, errorContext);
        } else if (FormatXml.M_INNER_ERROR.equals(name)) {
          handleInnerError(reader, errorContext);
        } else {
          throw new EntityProviderException(
              EntityProviderException.INVALID_CONTENT.addContent(name, FormatXml.M_ERROR));
        }
      }
    }
    validate(codeFound, messageFound);

    errorContext.setContentType(ContentType.APPLICATION_XML.toContentTypeString());
    return errorContext;
  }

  private void validate(final boolean codeFound, final boolean messageFound) throws EntityProviderException {
    if (!codeFound) {
      throw new EntityProviderException(
          EntityProviderException.MISSING_PROPERTY.addContent("Mandatory 'code' property not found.'"));
    } else if (!messageFound) {
      throw new EntityProviderException(
          EntityProviderException.MISSING_PROPERTY.addContent("Mandatory 'message' property not found.'"));
    }
  }

  private boolean notFinished(final XMLStreamReader reader) throws XMLStreamException {
    return notFinished(reader, FormatXml.M_ERROR);
  }

  private boolean notFinished(final XMLStreamReader reader, String tagName) throws XMLStreamException {
    boolean finished = reader.isEndElement() && tagName.equals(reader.getLocalName());
    return !finished && reader.hasNext();
  }

  private void handleInnerError(final XMLStreamReader reader, final ODataErrorContext errorContext)
      throws XMLStreamException {
    reader.next();
    String innerError = null;
    if(reader.isCharacters()) {
      innerError = reader.getText();
    } else if(reader.isStartElement()) {
      innerError = handleComplexInnerError(reader);
    }
    errorContext.setInnerError(innerError);
  }

  private String handleComplexInnerError(XMLStreamReader reader) throws XMLStreamException {
    StringBuilder sb = new StringBuilder();
    while(notFinished(reader, FormatXml.M_INNER_ERROR)) {
      if(reader.hasName()) {
        sb.append("<");
        if(reader.isEndElement()) {
          sb.append("/");
        }
        sb.append(reader.getLocalName()).append(">");
      } else if(reader.isCharacters()) {
        sb.append(reader.getText());
      }
      reader.next();
    }
    return sb.toString();
  }

  private void handleMessage(final XMLStreamReader reader, final ODataErrorContext errorContext)
      throws XMLStreamException {
    String lang = reader.getAttributeValue(Edm.NAMESPACE_XML_1998, FormatXml.XML_LANG);
    errorContext.setLocale(getLocale(lang));
    String message = reader.getElementText();
    errorContext.setMessage(message);
  }

  private void handleCode(final XMLStreamReader reader, final ODataErrorContext errorContext)
      throws XMLStreamException {
    String code = reader.getElementText();
    errorContext.setErrorCode(code);
  }

  private Locale getLocale(final String langValue) {
    return AVAILABLE_LOCALES.get(langValue);
  }
}
