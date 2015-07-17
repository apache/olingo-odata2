/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.batch;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.olingo.odata2.core.batch.v2.BufferedReaderIncludingLineEndings;
import org.apache.olingo.odata2.core.batch.v2.Line;
import org.junit.Test;

public class BufferedReaderIncludingLineEndingsTest {

  private static final String TEXT_COMBINED = "Test\r" +
      "Test2\r\n" +
      "Test3\n" +
      "Test4\r" +
      "\r" +
      "\r\n" +
      "\r\n" +
      "Test5\n" +
      "Test6\r\n" +
      "Test7\n" +
      "\n";

  private static final String TEXT_SMALL = "Test\r" +
      "123";
  private static final String TEXT_EMPTY = "";

  @Test
  public void testSimpleText() throws IOException {
    final String TEXT = "Test";
    BufferedReaderIncludingLineEndings reader = create(TEXT);

    assertEquals(TEXT, reader.readLine());
    assertNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

  @Test
  public void testNoText() throws IOException {
    final String TEXT = "";
    BufferedReaderIncludingLineEndings reader = create(TEXT);

    assertNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

  @Test
  public void testNoBytes() throws IOException {
    BufferedReaderIncludingLineEndings reader =
        new BufferedReaderIncludingLineEndings(new ByteArrayInputStream(new byte[0]));

    assertNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

  @Test
  public void testCRLF() throws IOException {
    final String TEXT = "Test\r\n" +
        "Test2";

    BufferedReaderIncludingLineEndings reader = create(TEXT);

    assertEquals("Test\r\n", reader.readLine());
    assertEquals("Test2", reader.readLine());
    assertNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

  @Test
  public void testLF() throws IOException {
    final String TEXT = "Test\n" +
        "Test2";

    BufferedReaderIncludingLineEndings reader = create(TEXT);

    assertEquals("Test\n", reader.readLine());
    assertEquals("Test2", reader.readLine());
    assertNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

  @Test
  public void testCR() throws IOException {
    final String TEXT = "Test\r" +
        "Test2";

    BufferedReaderIncludingLineEndings reader = create(TEXT);

    assertEquals("Test\r", reader.readLine());
    assertEquals("Test2", reader.readLine());
    assertNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

  @Test
  public void testCombined() throws IOException {
    BufferedReaderIncludingLineEndings reader = create(TEXT_COMBINED);

    assertEquals("Test\r", reader.readLine());
    assertEquals("Test2\r\n", reader.readLine());
    assertEquals("Test3\n", reader.readLine());
    assertEquals("Test4\r", reader.readLine());
    assertEquals("\r", reader.readLine());
    assertEquals("\r\n", reader.readLine());
    assertEquals("\r\n", reader.readLine());
    assertEquals("Test5\n", reader.readLine());
    assertEquals("Test6\r\n", reader.readLine());
    assertEquals("Test7\n", reader.readLine());
    assertEquals("\n", reader.readLine());
    assertNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

  @Test
  public void testCombinedBufferSizeTwo() throws IOException {
    BufferedReaderIncludingLineEndings reader = create(TEXT_COMBINED, 2);

    assertEquals("Test\r", reader.readLine());
    assertEquals("Test2\r\n", reader.readLine());
    assertEquals("Test3\n", reader.readLine());
    assertEquals("Test4\r", reader.readLine());
    assertEquals("\r", reader.readLine());
    assertEquals("\r\n", reader.readLine());
    assertEquals("\r\n", reader.readLine());
    assertEquals("Test5\n", reader.readLine());
    assertEquals("Test6\r\n", reader.readLine());
    assertEquals("Test7\n", reader.readLine());
    assertEquals("\n", reader.readLine());
    assertNull(reader.readLine());
    assertNull(reader.readLine());
    reader.close();
  }

  @Test
  public void testCombinedBufferSizeOne() throws IOException {
    final String TEXT = "Test\r" +
        "Test2\r\n" +
        "Test3\n" +
        "Test4\r" +
        "\r" +
        "\r\n" +
        "\r\n" +
        "Test5\n" +
        "Test6\r\n" +
        "Test7\n" +
        "\r\n";

    BufferedReaderIncludingLineEndings reader = create(TEXT, 1);

    assertEquals("Test\r", reader.readLine());
    assertEquals("Test2\r\n", reader.readLine());
    assertEquals("Test3\n", reader.readLine());
    assertEquals("Test4\r", reader.readLine());
    assertEquals("\r", reader.readLine());
    assertEquals("\r\n", reader.readLine());
    assertEquals("\r\n", reader.readLine());
    assertEquals("Test5\n", reader.readLine());
    assertEquals("Test6\r\n", reader.readLine());
    assertEquals("Test7\n", reader.readLine());
    assertEquals("\r\n", reader.readLine());
    assertNull(reader.readLine());
    assertNull(reader.readLine());

    reader.close();
  }

  @Test
  public void testDoubleLF() throws IOException {
    final String TEXT = "Test\r" +
        "\r";

    BufferedReaderIncludingLineEndings reader = create(TEXT, 1);

    assertEquals("Test\r", reader.readLine());
    assertEquals("\r", reader.readLine());
    reader.close();
  }



  @Test(expected = IllegalArgumentException.class)
  public void testFailBufferSizeZero() throws IOException {
    BufferedReaderIncludingLineEndings reader = create(TEXT_EMPTY, 0);
    reader.close();
  }

  @Test(expected = NullPointerException.class)
  public void testInputStreamIsNull() throws IOException {
    // Same behaviour like BufferedReader
    BufferedReaderIncludingLineEndings reader = new BufferedReaderIncludingLineEndings(null);
    reader.close();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFailBufferSizeNegative() throws IOException {
    BufferedReaderIncludingLineEndings reader = create(TEXT_EMPTY, -1);
    reader.close();
  }

  @Test
  public void testToList() throws IOException {
    BufferedReaderIncludingLineEndings reader = create(TEXT_COMBINED);
    List<Line> stringList = reader.toLineList();

    assertEquals(11, stringList.size());
    assertEquals("Test\r", stringList.get(0).toString());
    assertEquals("Test2\r\n", stringList.get(1).toString());
    assertEquals("Test3\n", stringList.get(2).toString());
    assertEquals("Test4\r", stringList.get(3).toString());
    assertEquals("\r", stringList.get(4).toString());
    assertEquals("\r\n", stringList.get(5).toString());
    assertEquals("\r\n", stringList.get(6).toString());
    assertEquals("Test5\n", stringList.get(7).toString());
    assertEquals("Test6\r\n", stringList.get(8).toString());
    assertEquals("Test7\n", stringList.get(9).toString());
    assertEquals("\n", stringList.get(10).toString());
    reader.close();
  }

  private BufferedReaderIncludingLineEndings create(final String inputString) throws UnsupportedEncodingException {
    return new BufferedReaderIncludingLineEndings(new ByteArrayInputStream(inputString.getBytes("UTF-8")));
  }

  private BufferedReaderIncludingLineEndings create(final String inputString, int bufferSize)
      throws UnsupportedEncodingException {
    return new BufferedReaderIncludingLineEndings(new ByteArrayInputStream(inputString.getBytes("UTF-8")), bufferSize);
  }
}
