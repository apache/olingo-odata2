package org.apache.olingo.odata2.android.xml;

import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamWriter;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

/**
 */
public class AndroidXmlWriter implements XMLStreamWriter {

  private Writer writer;

  public AndroidXmlWriter(Object output) {
    if (output instanceof OutputStream) {
      writer = new PrintWriter((OutputStream) output);
    } else if (output instanceof Writer) {
      writer = (Writer) output;
    }
  }

  public AndroidXmlWriter setProperties(Map<String, Object> properties) throws XMLStreamException {
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      setProperty(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public AndroidXmlWriter setProperty(String name, Object value) throws XMLStreamException {
    return this;
  }


  @Override
  public void flush() throws XMLStreamException {
    write("flush");
    try {
      if(writer != null) {
        writer.flush();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void setDefaultNamespace(String arg0) throws XMLStreamException {
    write("setDefaultNamespace", arg0);
  }

  @Override
  public void setPrefix(String arg0, String arg1) throws XMLStreamException {
    write("setPrefix", arg0, arg1);

  }

  @Override
  public void writeAttribute(String arg0, String arg1) throws XMLStreamException {
    write("writeAttribute", arg0, arg1);

  }

  @Override
  public void writeAttribute(String arg0, String arg1, String arg2, String arg3) throws XMLStreamException {
    write("writeAttribute", arg0, arg1, arg2);

  }

  @Override
  public void writeCharacters(String arg0) throws XMLStreamException {
    write("writeCharacters", arg0);

  }

  @Override
  public void writeDefaultNamespace(String arg0) throws XMLStreamException {
    write("writeDefaultNamespace", arg0);

  }

  @Override
  public void writeEndDocument() throws XMLStreamException {
    write("writeEndDocument");

  }

  @Override
  public void writeEndElement() throws XMLStreamException {
    write("writeEndElement");

  }

  @Override
  public void writeNamespace(String arg0, String arg1) throws XMLStreamException {
    write("writeNamespace", arg0, arg1);

  }

  @Override
  public void writeStartDocument() throws XMLStreamException {
    write("writeStartDocument");

  }

  @Override
  public void writeStartElement(String arg0) throws XMLStreamException {
    write("writeStartElement", arg0);

  }

  @Override
  public void writeStartElement(String arg0, String arg1) throws XMLStreamException {
    write("writeStartElement", arg0, arg1);

  }

  @Override
  public void writeStartElement(String arg0, String arg1, String arg2) throws XMLStreamException {
    write("writeStartElement", arg0, arg1, arg2);
  }

  private void write(String message, String... content) {
    try {
      if (writer == null) {
        throw new RuntimeException("Writer not initialized.");
//        writer.append("Writer not initialized\n");
      } else {
        writer.append(message).append("\n");
        for (String string : content) {
          writer.append("->").append(string).append("<-\n");
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void writeAttribute(String arg0, String arg1, String arg2) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeStartDocument(String arg0, String arg1) throws XMLStreamException {
    // TODO Auto-generated method stub

  }
}
