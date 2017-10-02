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

import java.net.URISyntaxException;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataJPAContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.PathInfoMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.EdmMockUtilV2;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.EntityWithXmlAdapterOnProperty;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPARelatedTypeMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPATypeEmbeddableMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.JPATypeMock.JPATypeEmbeddableMock2;
import org.apache.olingo.odata2.jpa.processor.core.mock.data.ODataEntryMockUtil;
import org.junit.Assert;
import org.junit.Test;

public class JPAEntityTest {

  private JPAEntity jpaEntity = null;

  @Test
  public void testCreateODataEntryWithComplexType() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, true);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryWithComplexType(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
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

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntry(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), ODataEntryMockUtil.VALUE_MINT);
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertEquals(ODataEntryMockUtil.VALUE_C.charAt(0), jpaTypeMock.getMC());
    assertEquals(ODataEntryMockUtil.VALUE_CARRAY, new String(jpaTypeMock.getMCArray()));
    assertEquals(ODataEntryMockUtil.VALUE_CHAR, jpaTypeMock.getMChar().toString());
    assertEquals(ODataEntryMockUtil.VALUE_ENUM, jpaTypeMock.getMSomeEnum());
    assertEquals(ODataEntryMockUtil.VALUE_CHARARRAY, JPAEntityParser.toString(jpaTypeMock.getMCharArray()));
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));
  }

  @Test
  public void testCreateODataEntryWithXmlAdapter() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntry(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getPropertyWithXmlAdapter().getClass(), EntityWithXmlAdapterOnProperty.class);
  }

  @Test
  public void testCreateODataEntryWithInline() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryWithInline(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
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

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryProperties(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), ODataEntryMockUtil.VALUE_MINT);
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));
  }

  @Test
  public void testCreateODataEntryPropertyWithOutCallBack() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContextWithoutCallBack());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryProperties(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      assertEquals(ODataJPARuntimeException.ERROR_JPA_BLOB_NULL.getKey(), e.getMessageReference().getKey());
      return;
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    fail(ODataJPATestConstants.EXCEPTION_EXPECTED);
  }

  @Test
  public void testUpdateODataEntry() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      JPATypeMock jpaTypeMock = new JPATypeMock();
      jpaEntity.setJPAEntity(jpaTypeMock);
      jpaEntity.update(ODataEntryMockUtil.mockODataEntry(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
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

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      JPATypeMock jpaTypeMock = new JPATypeMock();
      jpaEntity.setJPAEntity(jpaTypeMock);
      jpaEntity.update(ODataEntryMockUtil.mockODataEntryProperties(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), 0);// Key should not be changed
    assertEquals(jpaTypeMock.getMString(), ODataEntryMockUtil.VALUE_MSTRING);
    assertEquals(ODataEntryMockUtil.VALUE_ENUM, jpaTypeMock.getMSomeEnum());
    assertTrue(jpaTypeMock.getMDateTime().equals(ODataEntryMockUtil.VALUE_DATE_TIME));
  }

  private ODataJPAContext mockODataJPAContext() throws ODataException {
    PathInfoMock pathInfoMock = new PathInfoMock();
    try {
      pathInfoMock.setServiceRootURI("http://olingo.apache.org/service.svc");
    } catch (URISyntaxException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    ODataContextMock contextMock = new ODataContextMock();
    contextMock.setPathInfo(pathInfoMock.mock());
    ODataContext context = contextMock.mock();
    ODataJPAContext jpaContext = ODataJPAContextMock.mockODataJPAContext(context);
    return jpaContext;
  }

  private ODataJPAContext mockODataJPAContextWithoutCallBack() throws ODataException {
    ODataContext context = new ODataContextMock().mockWithoutOnJPAWriteContent();
    ODataJPAContext jpaContext = ODataJPAContextMock.mockODataJPAContext(context);
    return jpaContext;
  }
  
  @Test
  public void testUpdateODataEntryWithNullValue() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, false);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      JPATypeMock jpaTypeMock = new JPATypeMock();
      jpaEntity.setJPAEntity(jpaTypeMock);
      jpaEntity.update(ODataEntryMockUtil.mockODataEntryWithNullValue(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), 0);// Key should not be changed
    assertEquals(jpaTypeMock.getMString(), null);
    assertEquals(jpaTypeMock.getMDateTime(), null);
    assertEquals(jpaTypeMock.getMBlob(), null);
    assertEquals(jpaTypeMock.getMCArray(), null);
    assertEquals(jpaTypeMock.getMChar(), null);
    Assert.assertArrayEquals(jpaTypeMock.getMCharArray(), null);
    assertEquals(jpaTypeMock.getMClob(), null);
  }
  
  @Test
  public void testCreateODataEntryWithComplexTypeWithNullValues() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, true);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryWithComplexTypeWithNullValue(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    JPATypeMock jpaTypeMock = (JPATypeMock) jpaEntity.getJPAEntity();
    assertEquals(jpaTypeMock.getMInt(), ODataEntryMockUtil.VALUE_MINT);
    assertEquals(jpaTypeMock.getMString(), "Mock");
    JPATypeEmbeddableMock jpaEmbeddableMock = jpaTypeMock.getComplexType();
    assertNotNull(jpaEmbeddableMock);

    assertEquals(jpaEmbeddableMock.getMShort(), null);
    assertEquals(jpaEmbeddableMock.getMDate(), null);
    assertEquals(jpaEmbeddableMock.getMDate1(), null);
    assertEquals(jpaEmbeddableMock.getMTime(), null);
    assertEquals(jpaEmbeddableMock.getMTimestamp(), null);
    JPATypeEmbeddableMock2 jpaEmbeddableMock2 = jpaEmbeddableMock.getMEmbeddable();
    assertNotNull(jpaEmbeddableMock2);
    assertEquals(jpaEmbeddableMock2.getMFloat(), null);
    assertEquals(jpaEmbeddableMock2.getMUUID(), null);
  }
  
  @Test
  public void testCreateODataEntryWithComplexTypeWithMoreProperties() {
    try {
      EdmEntitySet edmEntitySet = EdmMockUtilV2.mockEdmEntitySet(JPATypeMock.ENTITY_NAME, true);
      EdmEntityType edmEntityType = edmEntitySet.getEntityType();

      jpaEntity = new JPAEntity(edmEntityType, edmEntitySet, mockODataJPAContext());
      jpaEntity.create(ODataEntryMockUtil.mockODataEntryWithComplexType(JPATypeMock.ENTITY_NAME));
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (EdmException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage()
          + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
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
    assertEquals(jpaEmbeddableMock.getMDate().getDate(), ODataEntryMockUtil.VALUE_DATE.getDate());
    assertEquals(jpaEmbeddableMock.getMDate().getDay(), ODataEntryMockUtil.VALUE_DATE.getDay());
    assertEquals(jpaEmbeddableMock.getMDate1().getDate(), ODataEntryMockUtil.VALUE_DATE1.getDate());
    assertEquals(jpaEmbeddableMock.getMTime().getTime(), ODataEntryMockUtil.VALUE_TIME.getTime());
    JPATypeEmbeddableMock2 jpaEmbeddableMock2 = jpaEmbeddableMock.getMEmbeddable();
    assertNotNull(jpaEmbeddableMock2);
    assertEquals(jpaEmbeddableMock2.getMFloat(), ODataEntryMockUtil.VALUE_MFLOAT, 1);
    assertEquals(jpaEmbeddableMock2.getMUUID(), ODataEntryMockUtil.VALUE_UUID);
  }
}
