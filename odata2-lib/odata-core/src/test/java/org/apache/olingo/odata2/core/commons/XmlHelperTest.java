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
package org.apache.olingo.odata2.core.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import com.ctc.wstx.exc.WstxParsingException;

public class XmlHelperTest {

    public static String XML = "<?xml version=\"1.0\"?>" + "<extract>" + "  <data>&rules;</data>" + "</extract>";

    public static String XML_XXE = "<?xml version=\"1.0\"?>"
            + "  <!DOCTYPE foo [" + "    <!ENTITY rules SYSTEM \"" + XmlHelperTest.class.getResource("/xxe.xml")
                                                                                        .toString()
            + "\">" + "  ]>" + "<extract>" + "  <data>&rules;</data>" + "</extract>";

    public static String XML_LOL = "<?xml version=\"1.0\"?>" + "    <!DOCTYPE lolz [" + "        <!ENTITY lol \"lol\">"
            + "        <!ELEMENT lolz (#PCDATA)>" + "        <!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">"
            + "        <!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">"
            + "        <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">"
            + "        <!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">"
            + "        <!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">"
            + "        <!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">"
            + "        <!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">"
            + "        <!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">"
            + "        <!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">" + "    ]>"
            + "    <lolz>&lol9;</lolz>";

    public static String XML_DOCTYPE =
            "<?xml version=\"1.0\" standalone=\"yes\"?>" + "<!DOCTYPE hallo [<!ELEMENT hallo (#PCDATA)>]>" + "<hallo>Hallo Welt!</hallo>";

    public static String XML_PROCESSING = "<?xml version=\"1.0\"?>" + "<?apache include file=\"somefile.html\" ?>" + "<extract>"
            + "  <data>&rules;</data>" + "</extract>";

    private static Object beforeXmlInputFactory;

    @BeforeClass
    public static void beforeClass() {
        // CHECKSTYLE:OFF
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory"); // NOSONAR
        //
        beforeXmlInputFactory = replaceXmlInputFactoryInstance(XMLInputFactory.newInstance());
        // CHECKSTYLE:ON
    }

    @AfterClass
    public static void afterClass() {
        replaceXmlInputFactoryInstance(beforeXmlInputFactory);
    }

    private static Object replaceXmlInputFactoryInstance(Object newInstance) {
        try {
            Field field = XmlHelper.XmlInputFactoryHolder.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            Object replaced = field.get(null);
            field.set(null, newInstance);
            return replaced;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createReader() throws Exception {
        InputStream content = new ByteArrayInputStream(XML.getBytes("UTF-8"));
        XMLStreamReader streamReader = XmlHelper.createStreamReader(content);
        assertNotNull(streamReader);
    }

    @Test
    public void xxeWithoutProtection() throws Exception {
        InputStream content = new ByteArrayInputStream(XML_XXE.getBytes("UTF-8"));
        XMLStreamReader streamReader = createStreamReaderWithExternalEntitySupport(content);

        boolean foundExternalEntity = false;

        while (streamReader.hasNext()) {
            streamReader.next();

            if (streamReader.hasText() && "some text".equals(streamReader.getText())) {
                foundExternalEntity = true;
                break;
            }

        }
        assertTrue(foundExternalEntity);
    }

    @Test(expected = XMLStreamException.class)
    public void xxeWithProtection() throws Exception {
        InputStream content = new ByteArrayInputStream(XML_XXE.getBytes("UTF-8"));
        XMLStreamReader streamReader = XmlHelper.createStreamReader(content);

        while (streamReader.hasNext()) {
            streamReader.next();
        }
    }

    public XMLStreamReader createStreamReaderWithExternalEntitySupport(final InputStream content) throws Exception {
        XMLStreamReader streamReader;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, true);

        return factory.createXMLStreamReader(content, "UTF-8");
    }

    @Test(expected = XMLStreamException.class)
    public void lolWithProtection() throws Exception {
        InputStream content = new ByteArrayInputStream(XML_LOL.getBytes("UTF-8"));
        XMLStreamReader streamReader = XmlHelper.createStreamReader(content);

        while (streamReader.hasNext()) {
            streamReader.next();
        }
    }

    @Test
    public void lolApiWithProtection() throws Exception {
        try {
            InputStream content = new ByteArrayInputStream(XML_LOL.getBytes("UTF-8"));
            EdmEntitySet entitySet = MockFacade.getMockEdm()
                                               .getDefaultEntityContainer()
                                               .getEntitySet("Employees");
            EntityProvider.readEntry("application/xml", entitySet, content, EntityProviderReadProperties.init()
                                                                                                        .build());

            fail();
        } catch (EntityProviderException e) {
            assertEquals(WstxParsingException.class, e.getCause()
                                                      .getClass());
        }
    }

    @Test
    public void xxeApiWithProtection() throws Exception {
        try {
            InputStream content = new ByteArrayInputStream(XML_XXE.getBytes("UTF-8"));
            EdmEntitySet entitySet = MockFacade.getMockEdm()
                                               .getDefaultEntityContainer()
                                               .getEntitySet("Employees");

            EntityProvider.readEntry("application/xml", entitySet, content, EntityProviderReadProperties.init()
                                                                                                        .build());

            fail();
        } catch (EntityProviderException e) {
            assertEquals(WstxParsingException.class, e.getCause()
                                                      .getClass());
        }
    }

    @Test
    public void xmlDoctypeApiWithProtection() throws Exception {
        try {
            InputStream content = new ByteArrayInputStream(XML_DOCTYPE.getBytes("UTF-8"));
            EdmEntitySet entitySet = MockFacade.getMockEdm()
                                               .getDefaultEntityContainer()
                                               .getEntitySet("Employees");

            EntityProvider.readEntry("application/xml", entitySet, content, EntityProviderReadProperties.init()
                                                                                                        .build());

            fail();
        } catch (EntityProviderException e) {
            assertEquals(WstxParsingException.class, e.getCause()
                                                      .getClass());
        }
    }

    @Test
    @Ignore("not way to disable in parser")
    public void xmlProcessingApiWithProtection() throws Exception {
        try {
            InputStream content = new ByteArrayInputStream(XML_PROCESSING.getBytes("UTF-8"));
            EdmEntitySet entitySet = MockFacade.getMockEdm()
                                               .getDefaultEntityContainer()
                                               .getEntitySet("Employees");

            EntityProvider.readEntry("application/xml", entitySet, content, EntityProviderReadProperties.init()
                                                                                                        .build());

            fail();
        } catch (EntityProviderException e) {
            e.printStackTrace();
            assertEquals(WstxParsingException.class, e.getCause()
                                                      .getClass());
        }
    }
}
