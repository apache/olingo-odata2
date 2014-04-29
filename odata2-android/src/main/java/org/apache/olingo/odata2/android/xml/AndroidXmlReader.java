package org.apache.olingo.odata2.android.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.olingo.odata2.api.xml.NamespaceContext;
import org.apache.olingo.odata2.api.xml.QName;
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamReader;
import org.apache.olingo.odata2.api.xml.XMLStreamConstants;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class AndroidXmlReader implements XMLStreamReader {

  private final XmlPullParser parser;

  public AndroidXmlReader(Object content) {
    if(content instanceof InputStream) {
      try {
        parser = Xml.newPullParser();
        parser.setInput((InputStream)content, null);
      } catch (XmlPullParserException e) {
        throw new RuntimeException("Error during AndroidXmlReader init with message" + e.getMessage(), e);
      }
    } else {
      throw new IllegalArgumentException("Unsupported input content. Only InputStream is supported.");
    }
  }

  public AndroidXmlReader setProperties(Map<String, Object> properties) throws XMLStreamException {
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      setProperty(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public AndroidXmlReader setProperty(String name, Object value) throws XMLStreamException {
    try {
      parser.setProperty(name, value);
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("During property setting an XmlPullParser Exception occurred: "
              + e.getMessage(), e);
    }
    return this;
  }

  @Override
  public void close() throws XMLStreamException {
  }

  @Override
  public int getAttributeCount() {
    return parser.getAttributeCount();
  }

  @Override
  public String getAttributeLocalName(int index) {
    return parser.getAttributeName(index);
  }

  @Override
  public String getAttributeNamespace(int index) {
    return parser.getAttributeName(index);
  }

  @Override
  public String getAttributePrefix(int index) {
    return parser.getAttributePrefix(index);
  }

  @Override
  public String getAttributeValue(int index) {
    return parser.getAttributeValue(index);
  }

  @Override
  public String getAttributeValue(String namespaceURI, String localName) {
    String attributeValue = parser.getAttributeValue(namespaceURI, localName);
    return attributeValue;
  }

  @Override
  public String getElementText() throws XMLStreamException {
    // FIXME
    String text;
    try {
      if(parser.getEventType() == XmlPullParser.START_TAG) {
        parser.next();
      }
      text = parser.getText();
      parser.next();
    } catch (Exception e) {
      e.printStackTrace();
      throw new XMLStreamException("Failure during step forward after 'getText'.", e);
    }
    return text;
  }

  @Override
  public String getLocalName() {
    return parser.getName();
  }

  @Override
  public QName getName() {
    final String namespaceUri = getNamespaceURI();
    return new QName() {
      @Override
      public String getNamespaceURI() {
        return namespaceUri;
      }
    };
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    String tmp = null;
    try {
      int depth = parser.getDepth();
      tmp = parser.getNamespacePrefix(depth);
    } catch (XmlPullParserException e) {
      e.printStackTrace();
    }
    final String prefix = tmp;
    // TODO Auto-generated method stub
    NamespaceContext nctx = new NamespaceContext() {
      public String getPrefix(String index) {
        return prefix;
      }
    };
    return nctx;
  }

  @Override
  public int getNamespaceCount() {
    // FIXME
    try {
      int depth = parser.getDepth();
      int nsStart = parser.getNamespaceCount(depth-1);
      int nsEnd = parser.getNamespaceCount(depth);
      int nsCount = nsEnd - nsStart;
      return nsCount;
    } catch (XmlPullParserException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public String getNamespacePrefix(int index) throws XMLStreamException {
    try {
      return parser.getNamespacePrefix(index);
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  @Override
  public String getNamespaceURI() {
    return parser.getNamespace();
  }

  @Override
  public String getNamespaceURI(int index) throws XMLStreamException {
    try {
      int nsCount = getNamespaceCount();
      if(index > nsCount) {
        throw new XMLStreamException("Out of namespace index");
      }
      int depthAndIndex = parser.getDepth() + index - 1;
      return parser.getNamespaceUri(depthAndIndex);
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  @Override
  public String getNamespaceURI(String prefix) {
    return parser.getNamespace(prefix);
  }

  @Override
  public String getPrefix() {
    return parser.getPrefix();
  }

  @Override
  public String getText() {
    return parser.getText();
  }

  @Override
  public boolean hasName() {
    return parser.getName() != null;
  }

  @Override
  public boolean hasNext() throws XMLStreamException {
    try {
      return parser.getEventType() != XmlPullParser.END_DOCUMENT;
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean isCharacters() throws XMLStreamException {
    try {
      return !parser.isWhitespace();
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean isEndElement() throws XMLStreamException {
    try {
      int eventType = parser.getEventType();
      return eventType == XmlPullParser.END_DOCUMENT
              || eventType == XmlPullParser.END_TAG;
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean isStartElement() throws XMLStreamException {
    try {
      int eventType = parser.getEventType();
      return eventType == XmlPullParser.START_DOCUMENT
              || eventType == XmlPullParser.START_TAG;
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  @Override
  public void next() throws XMLStreamException {
    try {
      parser.next();
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  @Override
  public int nextTag() throws XMLStreamException {
    try {
      return parser.nextTag();
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  @Override
  public void require(int eventType, String namespace, String tag) throws XMLStreamException {
    try {
      int xmlPullEventType = mapEventType(eventType);
      parser.require(xmlPullEventType, namespace, tag);
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new XMLStreamException("Got XmlPullParserException with message: " + e.getMessage(), e);
    }
  }

  private int mapEventType(int eventType) {
    switch (eventType) {
      case XMLStreamConstants.START_ELEMENT: return XmlPullParser.START_TAG;
      case XMLStreamConstants.END_ELEMENT: return XmlPullParser.END_TAG;
      case XMLStreamConstants.START_DOCUMENT: return XmlPullParser.START_DOCUMENT;
      case XMLStreamConstants.END_DOCUMENT: return XmlPullParser.END_DOCUMENT;
      case XMLStreamConstants.DTD: return XmlPullParser.DOCDECL;
      default:
        throw new IllegalArgumentException("Unknown event type ('" +
                eventType + "') for mapping.");
    }
  }
}