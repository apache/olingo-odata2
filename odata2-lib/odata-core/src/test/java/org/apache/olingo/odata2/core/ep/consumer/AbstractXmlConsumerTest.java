package org.apache.olingo.odata2.core.ep.consumer;

import java.util.Arrays;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class AbstractXmlConsumerTest extends AbstractConsumerTest {

  public enum StreamWriterImplType {
    WOODSTOCKIMPL, SUNINTERNALIMPL;
  }

  // CHECKSTYLE:OFF
  public AbstractXmlConsumerTest(final StreamWriterImplType type) {
    switch (type) {
    case WOODSTOCKIMPL:
      System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory"); // NOSONAR
      System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory"); // NOSONAR
      break;
    case SUNINTERNALIMPL:
      System.setProperty("javax.xml.stream.XMLInputFactory", "com.sun.xml.internal.stream.XMLInputFactoryImpl"); // NOSONAR
      System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.internal.stream.XMLOutputFactoryImpl"); // NOSONAR
      break;
    default:
      System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.internal.stream.XMLOutputFactoryImpl"); // NOSONAR
      System.setProperty("javax.xml.stream.XMLInputFactory", "com.sun.xml.internal.stream.XMLInputFactoryImpl"); // NOSONAR
      break;
    }
  }

  // CHECKSTYLE:On

  @Parameterized.Parameters
  public static List<Object[]> data() {
    // If desired this can be made dependent on runtime variables
    Object[][] a = new Object[2][1];
    a[0][0] = StreamWriterImplType.WOODSTOCKIMPL;
    a[1][0] = StreamWriterImplType.SUNINTERNALIMPL;

    return Arrays.asList(a);
  }
}
