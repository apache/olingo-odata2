package org.apache.olingo.odata2.android.xml;

import org.apache.olingo.odata2.core.xml.XMLStreamReader;
import org.apache.olingo.odata2.core.xml.XMLStreamReader.XMLStreamReaderFactory;

public class AndroidXmlReaderFactory implements XMLStreamReaderFactory {

  @Override
  public XMLStreamReader createXMLStreamReader(Object content) {
    return new AndroidXmlReader(content);
  }
}
