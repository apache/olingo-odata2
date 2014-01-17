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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataJPAContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.EdmMockUtilV2;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPARelatedTypeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPATypeEmbeddableMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPATypeEmbeddableMock2;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.ODataEntryMockUtil;
import org.junit.Test;

public class JPAEntityTest {

  private JPAEntity jpaEntity = null;

  @Test
  public void testCreateODataEntryWithComplexType() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, true);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, null);
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryWithComplexType(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), ODataEntryMockUtil.VALUE_MINT);
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));
    JPATypeEmbeddableMock jpaEmbeddableMock = jpaTypeMock.getComplexType();
    assertNotNull(jpaEmbeddableMock);

    assertEquals(jpaEmbeddableMock.getMShort(), ODataEntryMockUtil.VALUE_SHORT);
    JPATypeEmbeddableMock2 jpaEmbeddableMock2 = jpaEmbeddableMock.getMEmbeddable();
    assertNotNull(jpaEmbeddableMock2);
    assertEquals(jpaEmbeddableMock2.getMFloat(), ODataEntryMockUtil.VALUE_MFLOAT, 1);
    assertEquals(jpaEmbeddableMock2.getMUUID(), ODataEntryMockUtil.VALUE_UUID);
  }

  @Test
  public void testCreateODataEntry() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, null);
      jpaEntity.create(ODataEntryMockUtil.mockODataEntry(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), ODataEntryMockUtil.VALUE_MINT);
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));
  }

  @Test
  public void testCreateODataEntryWithInline() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, ODataJPAContextMock.mockODataJPAContext());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryWithInline(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), ODataEntryMockUtil.VALUE_MINT);
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));

    JPARelatedTypeMock relatedType = jpaTypeMock.getMRelatedEntity();
    assertEquals(relatedType.getMByte(), ODataEntryMockUtil.VALUE_MBYTE);
    assertEquals(relatedType.getMByteArray(), ODataEntryMockUtil.VALUE_MBYTEARRAY);
    assertEquals(relatedType.getMDouble(), ODataEntryMockUtil.VALUE_MDOUBLE, 0.0);
    assertEquals(relatedType.getMLong(), ODataEntryMockUtil.VALUE_MLONG);
  }

  @Test
  public void testCreateODataEntryProperty() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, null);
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryProperties(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), ODataEntryMockUtil.VALUE_MINT);
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));
  }

  @Test
  public void testUpdateODataEntry() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, ODataJPAContextMock.mockODataJPAContext());
      JPATypeMock jpaTypeMock = new JPATypeMock();
      jpaEntity.setJPAEntity(jpaTypeMock);
      jpaEntity.update(ODataEntryMockUtil.mockODataEntry(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), 0);// Key should not be changed
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));
  }

  @Test
  public void testUpdateODataEntryProperty() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, null);
      JPATypeMock jpaTypeMock = new JPATypeMock();
      jpaEntity.setJPAEntity(jpaTypeMock);
      jpaEntity.update(ODataEntryMockUtil.mockODataEntryProperties(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), 0);// Key should not be changed
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));
  }
}
