package org.apache.olingo.odata2.core.batch;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.core.batch.v2.BatchParserCommon;
import org.apache.olingo.odata2.core.batch.v2.BatchParserCommon.HeaderField;
import org.junit.Test;

public class BatchParserCommonTest {
  
  private static final String CRLF = "\r\n";
  
  @Test
  public void testMultipleHeader() throws BatchException {
    String[] messageRaw = new String[] {
        "Content-Id: 1" + CRLF,
        "Content-Id: 2" + CRLF,
        "content-type: Application/http" + CRLF,
        "content-transfer-encoding: Binary" + CRLF
      };
    List<String> message = new ArrayList<String>();
    message.addAll(Arrays.asList(messageRaw));
    
    
    final Map<String, HeaderField> header = BatchParserCommon.consumeHeaders(message);
    assertNotNull(header);
    
    final HeaderField contentIdHeaders = header.get(BatchHelper.HTTP_CONTENT_ID.toLowerCase(Locale.ENGLISH));
    assertNotNull(contentIdHeaders);
    assertEquals(2, contentIdHeaders.getValues().size());
    assertEquals("1", contentIdHeaders.getValues().get(0));
    assertEquals("2", contentIdHeaders.getValues().get(1));
  }
  
  @Test
  public void testMultipleHeaderSameValue() throws BatchException {
    String[] messageRaw = new String[] {
        "Content-Id: 1" + CRLF,
        "Content-Id: 1" + CRLF,
        "content-type: Application/http" + CRLF,
        "content-transfer-encoding: Binary" + CRLF
      };
    List<String> message = new ArrayList<String>();
    message.addAll(Arrays.asList(messageRaw));
    
    
    final Map<String, HeaderField> header = BatchParserCommon.consumeHeaders(message);
    assertNotNull(header);
    
    final HeaderField contentIdHeaders = header.get(BatchHelper.HTTP_CONTENT_ID.toLowerCase(Locale.ENGLISH));
    assertNotNull(contentIdHeaders);
    assertEquals(1, contentIdHeaders.getValues().size());
    assertEquals("1", contentIdHeaders.getValues().get(0));
  }
  
  @Test
  public void testHeaderSperatedByComma() throws BatchException {
    String[] messageRaw = new String[] {
        "Content-Id: 1" + CRLF,
        "Upgrade: HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11" + CRLF,
        "content-type: Application/http" + CRLF,
        "content-transfer-encoding: Binary" + CRLF
      };
    List<String> message = new ArrayList<String>();
    message.addAll(Arrays.asList(messageRaw));
    
    
    final Map<String, HeaderField> header = BatchParserCommon.consumeHeaders(message);
    assertNotNull(header);
    
    final HeaderField upgradeHeader = header.get("upgrade");
    assertNotNull(upgradeHeader);
    assertEquals(4, upgradeHeader.getValues().size());
    assertEquals("HTTP/2.0", upgradeHeader.getValues().get(0));
    assertEquals("SHTTP/1.3", upgradeHeader.getValues().get(1));
    assertEquals("IRC/6.9", upgradeHeader.getValues().get(2));
    assertEquals("RTA/x11", upgradeHeader.getValues().get(3));
  }
  
  @Test
  public void testMultipleAcceptHeader() throws BatchException {
    String[] messageRaw = new String[] {
        "Accept: application/atomsvc+xml;q=0.8, application/json;odata=verbose;q=0.5, */*;q=0.1" + CRLF,
        "Accept: text/plain;q=0.3" + CRLF,
        "Accept-Language:en-US,en;q=0.7,en-UK;q=0.9" + CRLF,
        "content-type: Application/http" + CRLF,
        "content-transfer-encoding: Binary" + CRLF
      };
    List<String> message = new ArrayList<String>();
    message.addAll(Arrays.asList(messageRaw));
    
    
    final Map<String, HeaderField> header = BatchParserCommon.consumeHeaders(message);
    assertNotNull(header);
    
    final HeaderField acceptHeader = header.get(HttpHeaders.ACCEPT.toLowerCase());
    assertNotNull(acceptHeader);
    assertEquals(4, acceptHeader.getValues().size());
  }
  
  @Test
  public void testMultipleAcceptHeaderSameValue() throws BatchException {
    String[] messageRaw = new String[] {
        "Accept: application/atomsvc+xml;q=0.8, application/json;odata=verbose;q=0.5, */*;q=0.1" + CRLF,
        "Accept: application/atomsvc+xml;q=0.8" + CRLF,
        "Accept-Language:en-US,en;q=0.7,en-UK;q=0.9" + CRLF,
        "content-type: Application/http" + CRLF,
        "content-transfer-encoding: Binary" + CRLF
      };
    List<String> message = new ArrayList<String>();
    message.addAll(Arrays.asList(messageRaw));
    
    
    final Map<String, HeaderField> header = BatchParserCommon.consumeHeaders(message);
    assertNotNull(header);
    
    final HeaderField acceptHeader = header.get(HttpHeaders.ACCEPT.toLowerCase());
    assertNotNull(acceptHeader);
    assertEquals(3, acceptHeader.getValues().size());
  }
  
  @Test
  public void testMultipleAccepLanguagetHeader() throws BatchException {
    String[] messageRaw = new String[] {
        "Accept-Language:en-US,en;q=0.7,en-UK;q=0.9" + CRLF,
        "Accept-Language: de-DE;q=0.3" + CRLF,
        "content-type: Application/http" + CRLF,
        "content-transfer-encoding: Binary" + CRLF
      };
    List<String> message = new ArrayList<String>();
    message.addAll(Arrays.asList(messageRaw));
    
    final Map<String, HeaderField> header = BatchParserCommon.consumeHeaders(message);
    assertNotNull(header);
    
    final HeaderField acceptLanguageHeader = header.get(HttpHeaders.ACCEPT_LANGUAGE.toLowerCase());
    assertNotNull(acceptLanguageHeader);
    assertEquals(4, acceptLanguageHeader.getValues().size());
  }
  
  @Test
  public void testMultipleAccepLanguagetHeaderSameValue() throws BatchException {
    String[] messageRaw = new String[] {
        "Accept-Language:en-US,en;q=0.7,en-UK;q=0.9" + CRLF,
        "Accept-Language:en-US,en;q=0.7" + CRLF,
        "content-type: Application/http" + CRLF,
        "content-transfer-encoding: Binary" + CRLF
      };
    List<String> message = new ArrayList<String>();
    message.addAll(Arrays.asList(messageRaw));
    
    final Map<String, HeaderField> header = BatchParserCommon.consumeHeaders(message);
    assertNotNull(header);
    
    final HeaderField acceptLanguageHeader = header.get(HttpHeaders.ACCEPT_LANGUAGE.toLowerCase());
    assertNotNull(acceptLanguageHeader);
    assertEquals(3, acceptLanguageHeader.getValues().size());
  }
  
  @Test
  public void testRemoveEndingCRLF() {
    String line = "Test\r\n";
    assertEquals("Test", BatchParserCommon.removeEndingCRLF(line));
  }

  @Test
  public void testRemoveLastEndingCRLF() {
    String line = "Test\r\n\r\n";
    assertEquals("Test\r\n", BatchParserCommon.removeEndingCRLF(line));
  }

  @Test
  public void testRemoveEndingCRLFWithWS() {
    String line = "Test\r\n            ";
    assertEquals("Test", BatchParserCommon.removeEndingCRLF(line));
  }

  @Test
  public void testRemoveEndingCRLFNothingToRemove() {
    String line = "Hallo\r\nBla";
    assertEquals("Hallo\r\nBla", BatchParserCommon.removeEndingCRLF(line));
  }

  @Test
  public void testRemoveEndingCRLFAll() {
    String line = "\r\n";
    assertEquals("", BatchParserCommon.removeEndingCRLF(line));
  }

  @Test
  public void testRemoveEndingCRLFSpace() {
    String line = "\r\n                      ";
    assertEquals("", BatchParserCommon.removeEndingCRLF(line));
  }

  @Test
  public void testRemoveLastEndingCRLFWithWS() {
    String line = "Test            \r\n";
    assertEquals("Test            ", BatchParserCommon.removeEndingCRLF(line));
  }

  @Test
  public void testRemoveLastEndingCRLFWithWSLong() {
    String line = "Test            \r\nTest2    \r\n";
    assertEquals("Test            \r\nTest2    ", BatchParserCommon.removeEndingCRLF(line));
  }
}
