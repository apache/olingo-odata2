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

import java.util.Collection;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

public class XmlErrorDocumentProducer {

  public void writeErrorDocument(final XMLStreamWriter writer, final String errorCode, final String message,
      final Locale locale, final String innerError) throws XMLStreamException {
      ODataErrorContext context = new ODataErrorContext();
      context.setErrorCode(errorCode);
      context.setMessage(message);
      context.setLocale(locale);
      context.setInnerError(innerError);

      writeErrorDocument(writer, context);
  }

  public void writeErrorDocument(final XMLStreamWriter writer, ODataErrorContext context) throws XMLStreamException {
    Locale locale = context.getLocale();
    String errorCode = context.getErrorCode();
    String message = context.getMessage();
    String innerError = context.getInnerError();
    Collection<ODataErrorContext> errorDetails = context.getErrorDetails();

    writer.writeStartDocument();
    writer.writeStartElement(FormatXml.M_ERROR);
    writer.writeDefaultNamespace(Edm.NAMESPACE_M_2007_08);
    writeSimpleElement(writer, FormatXml.M_CODE, errorCode);
    writer.writeStartElement(FormatXml.M_MESSAGE);
    if (locale != null) {
      writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, FormatXml.XML_LANG, getLocale(locale));
    } else {
      writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, FormatXml.XML_LANG, "");
    }
    if (message != null) {
      writer.writeCharacters(message);
    }
    writer.writeEndElement();

    if (!errorDetails.isEmpty()) {
    	writeErrorDetails(writer, errorDetails);
    } else if (innerError != null) {
      writeSimpleElement(writer, FormatXml.M_INNER_ERROR, innerError);
    }

    writer.writeEndDocument();
  }

  private void writeErrorDetails(final XMLStreamWriter writer, Collection<ODataErrorContext> errorDetails)
          throws XMLStreamException {
      writer.writeStartElement(FormatXml.M_INNER_ERROR);
      writer.writeStartElement(FormatXml.M_ERROR_DETAILS);
      for (ODataErrorContext detail : errorDetails) {
            writer.writeStartElement(FormatXml.M_ERROR_DETAIL);

            writeSimpleElement(writer, FormatXml.M_CODE, detail.getErrorCode());
            writeSimpleElement(writer, FormatXml.M_MESSAGE, detail.getMessage());
            writeSimpleElement(writer, FormatXml.M_TARGET, detail.getTarget());
            writeSimpleElement(writer, FormatXml.M_SEVERITY, detail.getSeverity());

            writer.writeEndElement();
      }
      writer.writeEndElement();
      writer.writeEndElement();
  }

  /**
   * Gets language and country as defined in RFC 4646 based on {@link Locale}.
   */
  private String getLocale(final Locale locale) {
    if (locale.getCountry().isEmpty()) {
      return locale.getLanguage();
    } else {
      return locale.getLanguage() + "-" + locale.getCountry();
    }
  }

  private void writeSimpleElement(final XMLStreamWriter writer, String elementName, String value)
          throws XMLStreamException {
      writer.writeStartElement(elementName);
      if (null != value) {
          writer.writeCharacters(value);
      }
      writer.writeEndElement();
  }
}
