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
package org.apache.olingo.odata2.jpa.processor.core.access.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAPage.JPAPageBuilder;
import org.junit.Test;

public class JPAPageBuilderDeltaTest {

  private static final int PAGE_SIZE = 10;

  @Test
  public void testBuildDefaultDelta() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();

    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .build();

    assertEquals(20, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());
    assertEquals(10, page.getPagedEntities().size());

    List<Object> pagedEntities = page.getPagedEntities();

    assertEquals("9", pagedEntities.get(0));
    assertEquals("18", pagedEntities.get(9));
  }

  @Test
  public void testBuildWithNoSkipTokenDelta() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();

    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("0")
        .build();

    assertEquals(10, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    List<Object> pagedEntities = page.getPagedEntities();
    assertEquals(10, pagedEntities.size());
    assertEquals("0", pagedEntities.get(0));
    assertEquals("9", pagedEntities.get(9));
  }

  @Test
  public void testBuildDefaultZeroPageDelta() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();

    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(0)
        .skipToken("10")
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(0, page.getPageSize());
    assertNotNull(page.getPagedEntities());
    assertEquals(0, page.getPagedEntities().size());

  }

  @Test
  public void testBuildWithNullSkipToken() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();

    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken(null)
        .build();

    assertEquals(10, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());

    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);
    assertEquals("0", pagedEntities.get(0));
    assertEquals("9", pagedEntities.get(9));
  }

  @Test
  public void testBuildWithInvalidSkipToken() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    try {
      pageBuilder.entities(mockEntities())
          .skipToken("AB");
    } catch (NumberFormatException e) {
      return;
    }
    fail("Exception Expected");
  }

  @Test
  public void testBuildWithTop() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();

    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("20")
        .top(5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(20, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());

    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);
    assertEquals(5, pagedEntities.size());
    assertEquals("19", pagedEntities.get(0));
    assertEquals("23", pagedEntities.get(4));
  }

  @Test
  public void testBuildWithTopZeroPage() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(0)
        .skipToken("10")
        .top(5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(0, page.getPageSize());
    assertNotNull(page.getPagedEntities());
    assertEquals(5, page.getPagedEntities().size());
  }

  @Test
  public void testBuildWithSkipZeroPage() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(0)
        .skipToken("10")
        .skip(5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(0, page.getPageSize());
    assertNotNull(page.getPagedEntities());

    assertEquals(0, page.getPagedEntities().size());
  }

  @Test
  public void testBuildWithTopSkipZeroPage() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(0)
        .skipToken("10")
        .skip(5)
        .top(5)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(0, page.getPageSize());
    assertNotNull(page.getPagedEntities());
    assertEquals(5, page.getPagedEntities().size());
  }

  @Test
  public void testBuildWithTopExceeds() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(15)
        .build();

    assertEquals(20, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);
    assertEquals(10, pagedEntities.size());
    assertEquals("9", pagedEntities.get(0));
    assertEquals("18", pagedEntities.get(9));
  }

  @Test
  public void testBuildWithTopSkipExceeds() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(5)
        .skip(10)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    List<Object> pagedEntities = page.getPagedEntities();
    assertEquals(0, pagedEntities.size());

  }

  @Test
  public void testBuildWithTopSkipMore() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(5)
        .skip(9)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);

    assertEquals("18", pagedEntities.get(0));
    assertEquals(1, pagedEntities.size());
  }

  @Test
  public void testBuildWithTopMoreSkip() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(15)
        .skip(9)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);

    assertEquals("18", pagedEntities.get(0));
    assertEquals(1, pagedEntities.size());
  }

  @Test
  public void testBuildWithTopXSkipX() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(15)
        .skip(15)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);

    assertEquals(0, pagedEntities.size());
  }

  @Test
  public void testBuildWithNegativeTop() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(-5)
        .build();

    assertEquals(20, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);

    assertEquals("9", pagedEntities.get(0));
    assertEquals(10, pagedEntities.size());
  }

  @Test
  public void testBuildWithNegativeTopSkipToken() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("-10")
        .top(-5)
        .skip(-1)
        .build();

    assertEquals(10, page.getNextPage());
    assertEquals(0, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);

    assertEquals(10, pagedEntities.size());

  }

  @Test
  public void testBuildWithNoRecords() {
    JPAPageBuilder pageBuilder = new JPAPageBuilder();
    JPAPage page = pageBuilder.entities(mockEntities())
        .pageSize(PAGE_SIZE)
        .skipToken("10")
        .top(1)
        .skip(1)
        .build();

    assertEquals(0, page.getNextPage());
    assertEquals(10, page.getStartPage());
    assertEquals(PAGE_SIZE, page.getPageSize());
    List<Object> pagedEntities = page.getPagedEntities();
    assertNotNull(pagedEntities);

    assertEquals(1, pagedEntities.size());
    assertEquals("10", pagedEntities.get(0));
  }

  private List<Object> mockEntities() {
    List<Object> entities = new ArrayList<Object>();
    for (int i = 0; i < 30; i++) {
      entities.add(String.valueOf(i));
    }
    return entities;
  }
}
