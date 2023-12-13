/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.ep.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.servicedocument.AtomInfo;
import org.apache.olingo.odata2.api.servicedocument.Categories;
import org.apache.olingo.odata2.api.servicedocument.Category;
import org.apache.olingo.odata2.api.servicedocument.Collection;
import org.apache.olingo.odata2.api.servicedocument.ExtensionElement;
import org.apache.olingo.odata2.api.servicedocument.Fixed;
import org.apache.olingo.odata2.api.servicedocument.ServiceDocument;
import org.apache.olingo.odata2.api.servicedocument.Workspace;
import org.junit.Test;

public class AtomServiceDocumentConsumerTest extends AbstractXmlConsumerTest {

    public AtomServiceDocumentConsumerTest(final StreamWriterImplType type) {
        super(type);
    }

    private static final String NAMESPACE = "http://www.foo.bar/Data";
    private static final String PREFIX = "foo";

    @Test
    public void checkDecodingOfEntitySetNames() throws Exception {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        ServiceDocument svcDocument = svcDocumentParser.readServiceDokument(createStreamReader("/serviceDocument.xml"));
        assertNotNull(svcDocument);

        EdmEntitySetInfo edmEntitySetInfo = svcDocument.getEntitySetsInfo()
                                                       .get(6);
        assertEquals(":EncodedName", edmEntitySetInfo.getEntitySetName());
        assertEquals("%3AEncodedName", edmEntitySetInfo.getEntitySetUri()
                                                       .toASCIIString());
    }

    @Test
    public void testServiceDocument() throws IOException, EntityProviderException {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        ServiceDocument svcDocument = svcDocumentParser.readServiceDokument(createStreamReader("/svcExample.xml"));
        assertNotNull(svcDocument);
        AtomInfo atomInfo = svcDocument.getAtomInfo();
        assertNotNull(atomInfo);
        assertNotNull(atomInfo.getWorkspaces());
        for (Workspace workspace : atomInfo.getWorkspaces()) {
            assertEquals("Data", workspace.getTitle()
                                          .getText());
            assertEquals(10, workspace.getCollections()
                                      .size());
            for (Collection collection : workspace.getCollections()) {
                assertNotNull(collection.getHref());
                if ("TypeOneEntityCollection".equals(collection.getHref())) {
                    assertEquals("TypeOneEntityCollection", collection.getTitle()
                                                                      .getText());
                    assertFalse(collection.getCommonAttributes()
                                          .getAttributes()
                                          .isEmpty());
                    assertEquals("content-version", collection.getCommonAttributes()
                                                              .getAttributes()
                                                              .get(0)
                                                              .getName());
                    assertEquals(NAMESPACE, collection.getCommonAttributes()
                                                      .getAttributes()
                                                      .get(0)
                                                      .getNamespace());
                    assertEquals(PREFIX, collection.getCommonAttributes()
                                                   .getAttributes()
                                                   .get(0)
                                                   .getPrefix());
                    assertEquals("1", collection.getCommonAttributes()
                                                .getAttributes()
                                                .get(0)
                                                .getText());
                    assertFalse(collection.getExtesionElements()
                                          .isEmpty());
                    for (ExtensionElement extElement : collection.getExtesionElements()) {
                        assertEquals(PREFIX, extElement.getPrefix());
                        assertEquals("member-title", extElement.getName());
                        assertEquals(NAMESPACE, extElement.getNamespace());
                    }
                }
            }
        }
    }

    @Test
    public void testExtensionsWithAttributes() throws IOException, EntityProviderException {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        ServiceDocument svcDocument = svcDocumentParser.readServiceDokument(createStreamReader("/svcExample.xml"));
        assertNotNull(svcDocument);
        AtomInfo atomInfo = svcDocument.getAtomInfo();
        assertNotNull(atomInfo);
        assertNotNull(atomInfo.getExtesionElements());
        assertEquals(2, atomInfo.getExtesionElements()
                                .size());
        for (ExtensionElement extElement : atomInfo.getExtesionElements()) {
            assertEquals("link", extElement.getName());
            assertEquals("atom", extElement.getPrefix());
            assertEquals(2, extElement.getAttributes()
                                      .size());
            assertEquals("rel", extElement.getAttributes()
                                          .get(0)
                                          .getName());
            assertEquals("http://localhost/odata/TEST_APPLICATION/", extElement.getAttributes()
                                                                               .get(1)
                                                                               .getText());
            assertEquals("href", extElement.getAttributes()
                                           .get(1)
                                           .getName());
        }

    }

    @Test
    public void testServiceDocument2() throws IOException, EntityProviderException {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        ServiceDocument svcDocument = svcDocumentParser.readServiceDokument(createStreamReader("/svcAtomExample.xml"));
        assertNotNull(svcDocument);
        AtomInfo atomInfo = svcDocument.getAtomInfo();
        assertNotNull(atomInfo);

        assertEquals(2, atomInfo.getWorkspaces()
                                .size());

        Workspace workspace = atomInfo.getWorkspaces()
                                      .get(0);
        assertEquals("Main Site", workspace.getTitle()
                                           .getText());
        assertEquals(2, workspace.getCollections()
                                 .size());

        workspace = atomInfo.getWorkspaces()
                            .get(1);
        assertEquals("Sidebar Blog", workspace.getTitle()
                                              .getText());
        assertEquals(1, workspace.getCollections()
                                 .size());
        Collection collection = workspace.getCollections()
                                         .get(0);
        assertEquals("Remaindered Links", collection.getTitle()
                                                    .getText());

        assertEquals(1, collection.getAcceptElements()
                                  .size());
        assertEquals("application/atom+xml;type=entry", collection.getAcceptElements()
                                                                  .get(0)
                                                                  .getValue());

    }

    @Test
    public void testCategories() throws IOException, EntityProviderException {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        ServiceDocument svcDocument = svcDocumentParser.readServiceDokument(createStreamReader("/svcAtomExample.xml"));
        assertNotNull(svcDocument);
        AtomInfo atomInfo = svcDocument.getAtomInfo();
        assertNotNull(atomInfo);

        assertEquals(2, atomInfo.getWorkspaces()
                                .size());

        Workspace workspace = atomInfo.getWorkspaces()
                                      .get(0);
        assertEquals(2, workspace.getCollections()
                                 .size());
        for (Collection collection : workspace.getCollections()) {
            for (Categories categories : collection.getCategories()) {
                assertEquals("http://example.com/cats/forMain.cats", categories.getHref());
            }
        }

        workspace = atomInfo.getWorkspaces()
                            .get(1);
        for (Collection collection : workspace.getCollections()) {
            for (Categories categories : collection.getCategories()) {
                assertEquals(Fixed.YES, categories.getFixed());
                for (Category category : categories.getCategoryList()) {
                    assertEquals("http://example.org/extra-cats/", category.getScheme());
                }
            }
        }

    }

    @Test
    public void testNestedExtensions() throws IOException, EntityProviderException {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        ServiceDocument svcDocument = svcDocumentParser.readServiceDokument(createStreamReader("/svcAtomExample.xml"));
        assertNotNull(svcDocument);
        AtomInfo atomInfo = svcDocument.getAtomInfo();
        assertNotNull(atomInfo);
        assertNotNull(atomInfo.getExtesionElements());
        for (ExtensionElement extElement : atomInfo.getExtesionElements()) {
            for (ExtensionElement nestedExtElement : extElement.getElements()) {
                if ("extension2".equals(nestedExtElement.getName())) {
                    assertFalse(nestedExtElement.getAttributes()
                                                .isEmpty());
                    assertEquals("attributeValue", nestedExtElement.getAttributes()
                                                                   .get(0)
                                                                   .getText());
                    assertEquals("attr", nestedExtElement.getAttributes()
                                                         .get(0)
                                                         .getName());
                } else if ("extension3".equals(nestedExtElement.getName())) {
                    assertTrue(nestedExtElement.getAttributes()
                                               .isEmpty());
                    assertEquals("value", nestedExtElement.getText());
                } else if ("extension4".equals(nestedExtElement.getName())) {
                    assertEquals("text", nestedExtElement.getText());
                    assertFalse(nestedExtElement.getAttributes()
                                                .isEmpty());
                    assertEquals("attributeValue", nestedExtElement.getAttributes()
                                                                   .get(0)
                                                                   .getText());
                    assertEquals("attr", nestedExtElement.getAttributes()
                                                         .get(0)
                                                         .getName());
                } else {
                    fail();
                }
            }
        }
    }

    @Test(expected = EntityProviderException.class)
    public void testWithoutTitle() throws IOException, EntityProviderException {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        svcDocumentParser.readServiceDokument(createStreamReader("/svcDocWithoutTitle.xml"));
    }

    @Test(expected = EntityProviderException.class)
    public void testSvcWithoutWorkspaces() throws IOException, EntityProviderException {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        svcDocumentParser.readServiceDokument(createStreamReader("/invalidSvcExample.xml"));
    }

    @Test
    public void testServiceDocument3() throws IOException, EntityProviderException {
        AtomServiceDocumentConsumer svcDocumentParser = new AtomServiceDocumentConsumer();
        ServiceDocument svcDocument = svcDocumentParser.readServiceDokument(createStreamReader("/serviceDocExample.xml"));
        assertNotNull(svcDocument);
        AtomInfo atomInfo = svcDocument.getAtomInfo();
        assertNotNull(atomInfo);
        for (Workspace workspace : atomInfo.getWorkspaces()) {
            assertEquals("Data", workspace.getTitle()
                                          .getText());
            assertEquals(9, workspace.getCollections()
                                     .size());
            for (Collection collection : workspace.getCollections()) {
                assertNotNull(collection.getHref());
                if ("TravelagencyCollection".equals(collection.getHref())) {
                    assertEquals("TravelagencyCollection", collection.getTitle()
                                                                     .getText());
                    assertEquals(2, collection.getCommonAttributes()
                                              .getAttributes()
                                              .size());
                    assertEquals("content-version", collection.getCommonAttributes()
                                                              .getAttributes()
                                                              .get(1)
                                                              .getName());
                    assertEquals(NAMESPACE, collection.getCommonAttributes()
                                                      .getAttributes()
                                                      .get(1)
                                                      .getNamespace());
                    assertEquals(PREFIX, collection.getCommonAttributes()
                                                   .getAttributes()
                                                   .get(1)
                                                   .getPrefix());
                    assertEquals("1", collection.getCommonAttributes()
                                                .getAttributes()
                                                .get(1)
                                                .getText());
                    assertFalse(collection.getExtesionElements()
                                          .isEmpty());
                    for (ExtensionElement extElement : collection.getExtesionElements()) {
                        if ("member-title".equals(extElement.getName())) {
                            assertEquals(PREFIX, extElement.getPrefix());
                            assertEquals(NAMESPACE, extElement.getNamespace());
                            assertEquals("Travelagency", extElement.getText());
                        } else if ("collectionLayout".equals(extElement.getName())) {
                            assertEquals("gp", extElement.getPrefix());
                            assertEquals("http://www.foo.bar/Data/GP", extElement.getNamespace());
                            assertNotNull(extElement.getAttributes());
                            assertEquals(2, extElement.getAttributes()
                                                      .size());
                            assertEquals("display-order", extElement.getAttributes()
                                                                    .get(0)
                                                                    .getName());
                            assertEquals("0010", extElement.getAttributes()
                                                           .get(0)
                                                           .getText());
                            assertEquals("top-level", extElement.getAttributes()
                                                                .get(1)
                                                                .getName());
                            assertEquals("true", extElement.getAttributes()
                                                           .get(1)
                                                           .getText());
                        } else if ("link".equals(extElement.getName())) {
                            assertEquals(Edm.NAMESPACE_ATOM_2005, extElement.getNamespace());
                            assertEquals(4, extElement.getAttributes()
                                                      .size());
                            assertEquals("TravelagencyCollection/OpenSearchDescription.xml", extElement.getAttributes()
                                                                                                       .get(0)
                                                                                                       .getText());
                            assertEquals("href", extElement.getAttributes()
                                                           .get(0)
                                                           .getName());
                        } else {
                            fail();
                        }
                    }
                }
            }
        }
    }

    private XMLStreamReader createStreamReader(final String fileName) throws IOException, EntityProviderException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        InputStream in = AtomServiceDocumentConsumerTest.class.getResourceAsStream(fileName);
        if (in == null) {
            throw new IOException("Requested file '" + fileName + "' was not found.");
        }
        XMLStreamReader streamReader;
        try {
            streamReader = factory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            throw new EntityProviderException(EntityProviderException.COMMON.addContent("Invalid Service Document"));
        }

        return streamReader;
    }
}
