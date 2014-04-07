package org.apache.olingo.odata2.android.xml;

import org.apache.olingo.odata2.api.xml.XMLStreamReader;
import org.apache.olingo.odata2.api.xml.XMLStreamReaderFactory;

public class AndroidXmlReaderFactory implements XMLStreamReaderFactory {
  @Override
  public XMLStreamReader createXMLStreamReader(Object content) {
    return new AndroidXmlReader(content);
  }
}
