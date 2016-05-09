/*
 * Copyright 2016 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.olingo.odata2.annotation.processor.core.edm;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty.Multiplicity;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.edm.provider.EdmImplProv;
import org.apache.olingo.odata2.core.uri.UriParserImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.EMPTY_MAP;

public class AnnotationEdmProviderNamespacesTest {

  /**
   * Test with namespace in path segment
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testTwoWayNavigationAB() throws Exception {
    UriParserImpl uriParser = createUriParser(A.class, B.class, C.class, D.class);

    PathSegment ps1 = new ODataPathSegmentImpl("AB.AA('1')", EMPTY_MAP);
    PathSegment ps2 = new ODataPathSegmentImpl("B", EMPTY_MAP);
    uriParser.parse(Arrays.asList(ps1, ps2), EMPTY_MAP);
  }

  /**
   * Test with manual set default namespace for edm
   * (160211_mibo): This can actual not work because the option to manually set the default namespace is missing.
   *                However this test could be used to test the option to manually set the default namespace.
   */
  @SuppressWarnings("unchecked")
  @Test
  @Ignore("Currently not implemented")
  public void testTwoWayNavigationABWithDefaultNamespaceSetManual() throws Exception {
    UriParserImpl uriParser = createUriParser(A.class, B.class, C.class, D.class);

    PathSegment ps1 = new ODataPathSegmentImpl("AA('1')", EMPTY_MAP);
    PathSegment ps2 = new ODataPathSegmentImpl("B", EMPTY_MAP);
    uriParser.parse(Arrays.asList(ps1, ps2), EMPTY_MAP);
  }

  /**
   * Test with namespace in path segment
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testTwoWayNavigationCD() throws Exception {
    UriParserImpl uriParser = createUriParser(C.class, D.class, A.class, B.class);

    PathSegment ps1 = new ODataPathSegmentImpl("CD.CC('1')", EMPTY_MAP);
    PathSegment ps2 = new ODataPathSegmentImpl("D", EMPTY_MAP);
    uriParser.parse(Arrays.asList(ps1, ps2), EMPTY_MAP);
  }

  /**
   * Test with namespace in path segment
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testTwoWayNavigationBug_DefaultEntityContainerGeneratedOnWrongEntities1() throws Exception {
    UriParserImpl uriParser = createUriParser(A.class, B.class, C.class, D.class);

    PathSegment ps1 = new ODataPathSegmentImpl("CD.CC('1')", EMPTY_MAP);
    PathSegment ps2 = new ODataPathSegmentImpl("D", EMPTY_MAP);
    uriParser.parse(Arrays.asList(ps1, ps2), EMPTY_MAP);
  }

  /**
   * Test with namespace in path segment
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testTwoWayNavigationBug_DefaultEntityContainerGeneratedOnWrongEntities2() throws Exception {
    UriParserImpl uriParser = createUriParser(C.class, D.class, A.class, B.class);

    PathSegment ps1 = new ODataPathSegmentImpl("AB.AA('1')", EMPTY_MAP);
    PathSegment ps2 = new ODataPathSegmentImpl("B", EMPTY_MAP);
    uriParser.parse(Arrays.asList(ps1, ps2), EMPTY_MAP);
  }

  private UriParserImpl createUriParser(Class<?> ... annotatedClasses) throws ODataException {
    AnnotationEdmProvider provider = new AnnotationEdmProvider(Arrays.asList(annotatedClasses));
    EdmImplProv edm = new EdmImplProv(provider);
    return new UriParserImpl(edm);
  }

  @EdmEntityType(name = "A", namespace = "AB")
  @EdmEntitySet(name = "AA", container = "AB")
  private class A {
    @EdmKey
    @EdmProperty
    String id;
    @EdmNavigationProperty(toMultiplicity = Multiplicity.ZERO_OR_ONE, toType = B.class)
    B b;
  }

  @EdmEntityType(name = "B", namespace = "AB")
  @EdmEntitySet(container = "AB")
  private class B {
    @EdmNavigationProperty(toMultiplicity = Multiplicity.MANY, toType = A.class)
    List<A> a;
  }

  @EdmEntityType(name = "C", namespace = "CD")
  @EdmEntitySet(name = "CC", container = "CD")
  private class C {
    @EdmKey
    @EdmProperty
    String id;
    @EdmNavigationProperty(toMultiplicity = Multiplicity.ZERO_OR_ONE, toType = D.class)
    D d;
  }

  @EdmEntityType(name = "D", namespace = "CD")
  @EdmEntitySet(container = "CD")
  private class D {
    @EdmNavigationProperty(toMultiplicity = Multiplicity.MANY, toType = C.class)
    List<C> c;
  }
}
