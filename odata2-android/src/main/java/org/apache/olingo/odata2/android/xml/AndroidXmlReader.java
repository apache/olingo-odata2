package org.apache.olingo.odata2.android.xml;

import java.io.IOException;
import java.io.InputStream;

import org.apache.olingo.odata2.core.xml.NamespaceContext;
import org.apache.olingo.odata2.core.xml.QName;
import org.apache.olingo.odata2.core.xml.XMLStreamConstants;
import org.apache.olingo.odata2.core.xml.XMLStreamException;
import org.apache.olingo.odata2.core.xml.XMLStreamReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class AndroidXmlReader extends XMLStreamReader {

  private final InputStream xmlContentStream;
  private final XmlPullParser parser;
  
  public AndroidXmlReader(Object content) {
    if(content instanceof InputStream) {
      parser = Xml.newPullParser();
      this.xmlContentStream = (InputStream) content;
      
      try {
        parser.setInput(xmlContentStream, null);
      } catch (XmlPullParserException e) {
        throw new RuntimeException("Error during AndroidXmlReader init", e);
      }
    } else {
      throw new IllegalArgumentException("Unsupported input content. Only InputStream is supported.");
    }
  }
  
  @Override
  public void close() throws XMLStreamException {
  }

  @Override
  public int getAttributeCount() {
    return parser.getAttributeCount();
  }

  @Override
  public String getAttributeLocalName(int arg0) {
    return parser.getAttributeName(arg0);
  }

  @Override
  public String getAttributeNamespace(int arg0) {
    return parser.getAttributeName(arg0);
  }

  @Override
  public String getAttributePrefix(int arg0) {
    return parser.getAttributePrefix(arg0);
  }

  @Override
  public String getAttributeValue(int arg0) {
    return parser.getAttributeValue(arg0);
  }

  @Override
  public String getAttributeValue(String arg0, String arg1) {
    String attributeValue = parser.getAttributeValue(arg0, arg1);
    return attributeValue;
  }

  @Override
  public String getElementText() throws XMLStreamException {
    // FIXME
    return parser.getText();
  }

  @Override
  public String getLocalName() {
    // FIXME
    String name = parser.getName();
    return name;
  }

  @Override
  public QName getName() {
    // FIXME
    QName qname = new QName(getNamespaceURI()) {};
    return qname;
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
      @Override
      public String getPrefix(String arg0) {
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
  public String getNamespacePrefix(int arg0) {
    // FIXME
    try {
      return parser.getNamespacePrefix(arg0);
    } catch (XmlPullParserException e) {
      e.printStackTrace();
      throw new RuntimeException("Android", e);
    }
  }

  @Override
  public String getNamespaceURI() {
    // FIXME
//    try {
//      int depth = parser.getDepth();
//      String nsUri = parser.getNamespaceUri(depth);
//      return nsUri;
//    } catch (XmlPullParserException e) {
//      e.printStackTrace();
//      throw new RuntimeException("Android", e);
//    }
    String nsUri = parser.getNamespace();
    return nsUri;
  }
  
  

  @Override
  public String getNamespaceURI(int index) {
    // FIXME
    try {
      int nsCount = getNamespaceCount();
      if(index > nsCount) {
        throw new RuntimeException("Out of namespace index");        
      }
      int depthAndIndex = parser.getDepth() + index;
      String namespaceUri = parser.getNamespaceUri(depthAndIndex);
      return namespaceUri;
    } catch (XmlPullParserException e) {
      e.printStackTrace();
      throw new RuntimeException("Android", e);
    }
  }

  @Override
  public String getNamespaceURI(String arg0) {
    // FIXME
    String namespaceUri = parser.getNamespace(arg0);
    return namespaceUri;
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
    // FIXME
    return parser.getName() != null;
  }

  @Override
  public boolean hasNext() throws XMLStreamException {
    // TODO Auto-generated method stub
    int eventType;
    try {
      eventType = parser.getEventType();
    } catch (XmlPullParserException e) {
      throw new RuntimeException("Android", e);
    }
    return eventType != XmlPullParser.END_DOCUMENT;
  }

  @Override
  public boolean isCharacters() {
    // FIXME
    try {
      return !parser.isWhitespace();
    } catch (XmlPullParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean isEndElement() {
    // TODO Auto-generated method stub
    try {
      int eventType = parser.getEventType();
      return eventType == XmlPullParser.END_DOCUMENT 
          || eventType == XmlPullParser.END_TAG;
    } catch (XmlPullParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new RuntimeException("Android", e);
    }
  }

  @Override
  public boolean isStartElement() {
    try {
      int eventType = parser.getEventType();
      return eventType == XmlPullParser.START_DOCUMENT 
          || eventType == XmlPullParser.START_TAG;
    } catch (XmlPullParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new RuntimeException("Android", e);
    }
  }

  @Override
  public void next() throws XMLStreamException {
    try {
      parser.next();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new RuntimeException("Android", e);
    }
  }

  @Override
  public int nextTag() throws XMLStreamException {
    try {
      return parser.nextTag();
    } catch (XmlPullParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new RuntimeException("Android", e);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new RuntimeException("Android", e);
    }
  }

  @Override
  public void require(int eventType, String arg1, String arg2) throws XMLStreamException {
    // TODO Auto-generated method stub
    try {
      int xmlPullEventType = mapEventType(eventType);
      parser.require(xmlPullEventType, arg1, arg2);
    } catch (XmlPullParserException e) {
      throw new XMLStreamException("Requirement not fulfilled", e) {};
    } catch (IOException e) {
      throw new RuntimeException("Android", e);
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
