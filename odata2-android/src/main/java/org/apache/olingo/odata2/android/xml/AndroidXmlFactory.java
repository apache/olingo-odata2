package org.apache.olingo.odata2.android.xml;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.xml.XMLStreamReader;
import org.apache.olingo.odata2.api.xml.XMLStreamReaderFactory;
import org.apache.olingo.odata2.api.xml.XMLStreamWriter;
import org.apache.olingo.odata2.api.xml.XMLStreamWriterFactory;

public class AndroidXmlFactory implements XMLStreamReaderFactory, XMLStreamWriterFactory {
  @Override
  public XMLStreamReader createXMLStreamReader(Object content) {
    return new AndroidXmlReader(content);
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(Object content) throws EntityProviderException {
    return new AndroidXmlWriter(content);
  }
}
