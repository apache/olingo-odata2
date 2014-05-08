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

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.xml.*;
import org.apache.olingo.odata2.core.xml.AbstractXmlStreamFactory;

public class AndroidXmlFactory extends AbstractXmlStreamFactory {
  private static final String DEFAULT_CHARSET = "UTF-8";

  @Override
  public XMLStreamReader createXMLStreamReader(Object content) throws XMLStreamException, EntityProviderException {
    AndroidXmlReader reader = new AndroidXmlReader(content).setProperties(readProperties);

    // verify charset encoding set in content is supported (if not set UTF-8 is used as defined in
    // 'http://www.w3.org/TR/2008/REC-xml-20081126/')
    String characterEncodingInContent = reader.getCharacterEncodingScheme();
    if (characterEncodingInContent != null && !DEFAULT_CHARSET.equalsIgnoreCase(characterEncodingInContent)) {
      throw new EntityProviderException(EntityProviderException
              .UNSUPPORTED_CHARACTER_ENCODING.addContent(characterEncodingInContent));
    }

    return reader;
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(Object content) throws XMLStreamException {
    return new AndroidXmlWriter(content).setProperties(writeProperties);
  }
}
