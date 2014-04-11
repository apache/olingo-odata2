package org.apache.olingo.odata2.android.xml;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.xml.*;
import org.apache.olingo.odata2.core.xml.AbstractXmlStreamFactory;

public class AndroidXmlFactory extends AbstractXmlStreamFactory {
  @Override
  public XMLStreamReader createXMLStreamReader(Object content) throws XMLStreamException {
    return new AndroidXmlReader(content).setProperties(readProperties);
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(Object content) throws XMLStreamException {
    return new AndroidXmlWriter(content).setProperties(writeProperties);
  }
}
