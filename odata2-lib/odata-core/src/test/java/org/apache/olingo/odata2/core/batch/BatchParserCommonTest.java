package org.apache.olingo.odata2.core.batch;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.odata2.core.batch.v2.BatchParserCommon;
import org.junit.Test;

public class BatchParserCommonTest {

  @Test
  public void testTrimList() {
    final List<String> list = Arrays.asList(new String[] { "123\r\n", "abc", "a\n", "\r", "123" });
    final List<String> trimedList = BatchParserCommon.trimStringListToLength(list, 7);

    assertEquals(2, trimedList.size());
    assertEquals("123\r\n", trimedList.get(0));
    assertEquals("ab", trimedList.get(1));
  }

  @Test
  public void testTrimListWithEmptyString() {
    final List<String> list = Arrays.asList(new String[] { "123\r\n", "", "abc", "a\n", "\r", "123" });
    final List<String> trimedList = BatchParserCommon.trimStringListToLength(list, 7);

    assertEquals(3, trimedList.size());
    assertEquals("123\r\n", trimedList.get(0));
    assertEquals("", trimedList.get(1));
    assertEquals("ab", trimedList.get(2));
  }

  @Test
  public void testTrimListTryToReadMoreThanStringLength() {
    final List<String> list = Arrays.asList(new String[] { "abc\r\n", "123" });
    final List<String> trimedList = BatchParserCommon.trimStringListToLength(list, 100);

    assertEquals(2, trimedList.size());
    assertEquals("abc\r\n", trimedList.get(0));
    assertEquals("123", trimedList.get(1));
  }

  @Test
  public void testTrimListEmpty() {
    final List<String> list = Arrays.asList(new String[0]);
    final List<String> trimedList = BatchParserCommon.trimStringListToLength(list, 7);

    assertEquals(0, trimedList.size());
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
