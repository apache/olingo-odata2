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
package org.apache.olingo.odata2.core.ep.consumer;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.servicedocument.*;
import org.apache.olingo.odata2.core.xml.XmlStreamFactory;
import org.apache.olingo.odata2.core.ep.util.FormatXml;
import org.apache.olingo.odata2.core.servicedocument.*;
import org.apache.olingo.odata2.api.xml.XMLStreamConstants;
import org.apache.olingo.odata2.api.xml.XMLStreamException;
import org.apache.olingo.odata2.api.xml.XMLStreamReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AtomServiceDocumentConsumer {
  private String currentHandledStartTagName;
  private static final String DEFAULT_PREFIX = "";

  public ServiceDocumentImpl readServiceDokument(final XMLStreamReader reader) throws EntityProviderException {
    AtomInfoImpl atomInfo = new AtomInfoImpl();
    ServiceDocumentImpl serviceDocument = new ServiceDocumentImpl();
    List<Workspace> workspaces = new ArrayList<Workspace>();
    List<ExtensionElement> extElements = new ArrayList<ExtensionElement>();
    CommonAttributesImpl attributes = new CommonAttributesImpl();
    try {
      while (reader.hasNext()
          && !(reader.isEndElement() && Edm.NAMESPACE_APP_2007.equals(reader.getNamespaceURI()) && FormatXml.APP_SERVICE
              .equals(reader.getLocalName()))) {
        reader.next();
        if (reader.isStartElement()) {
          currentHandledStartTagName = reader.getLocalName();
          if (FormatXml.APP_SERVICE.equals(currentHandledStartTagName)) {
            attributes = parseCommonAttribute(reader);
          } else if (FormatXml.APP_WORKSPACE.equals(currentHandledStartTagName)) {
            workspaces.add(parseWorkspace(reader));
          } else {
            ExtensionElementImpl extElement = parseExtensionElement(reader);
            if (extElement != null) {
              extElements.add(extElement);
            }
          }
        }
      }
      if (workspaces.isEmpty()) {
        throw new EntityProviderException(EntityProviderException.INVALID_STATE
            .addContent("Service element must contain at least one workspace element"));
      }
      reader.close();
      atomInfo.setWorkspaces(workspaces).setCommonAttributes(attributes).setExtesionElements(extElements);

      serviceDocument.setAtomInfo(atomInfo);
      serviceDocument.setEntitySetsInfo(atomInfo.getEntitySetsInfo());
      return serviceDocument;
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  private CommonAttributesImpl parseCommonAttribute(final XMLStreamReader reader) {
    CommonAttributesImpl attribute = new CommonAttributesImpl();
    List<ExtensionAttribute> extAttributes = new ArrayList<ExtensionAttribute>();
    attribute.setBase(reader.getAttributeValue(null, FormatXml.XML_BASE));
    attribute.setLang(reader.getAttributeValue(null, FormatXml.XML_LANG));
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      if (!(FormatXml.XML_BASE.equals(reader.getAttributeLocalName(i))
          && Edm.PREFIX_XML.equals(reader.getAttributePrefix(i))
          || (FormatXml.XML_LANG.equals(reader.getAttributeLocalName(i)) && Edm.PREFIX_XML.equals(reader
              .getAttributePrefix(i)))
          || ("local".equals(reader.getAttributeNamespace(i)) || DEFAULT_PREFIX.equals(reader.getAttributePrefix(i)))))
      {
        extAttributes.add(new ExtensionAttributeImpl()
            .setName(reader.getAttributeLocalName(i))
            .setNamespace(reader.getAttributeNamespace(i))
            .setPrefix(reader.getAttributePrefix(i))
            .setText(reader.getAttributeValue(i)));
      }
    }

    return attribute.setAttributes(extAttributes);
  }

  private WorkspaceImpl parseWorkspace(final XMLStreamReader reader)
      throws XMLStreamException, EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_APP_2007, FormatXml.APP_WORKSPACE);

    TitleImpl title = null;
    List<Collection> collections = new ArrayList<Collection>();
    List<ExtensionElement> extElements = new ArrayList<ExtensionElement>();
    CommonAttributesImpl attributes = parseCommonAttribute(reader);
    while (reader.hasNext()
        && !(reader.isEndElement() && Edm.NAMESPACE_APP_2007.equals(reader.getNamespaceURI()) && FormatXml.APP_WORKSPACE
            .equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        currentHandledStartTagName = reader.getLocalName();
        if (FormatXml.APP_COLLECTION.equals(currentHandledStartTagName)) {
          collections.add(parseCollection(reader));
        } else if (FormatXml.ATOM_TITLE.equals(currentHandledStartTagName)) {
          title = parseTitle(reader);
        } else {
          extElements.add(parseExtensionSansTitleElement(reader));
        }
      }
    }
    if (title == null) {
      throw new EntityProviderException(EntityProviderException.INVALID_STATE
          .addContent("Missing element title for workspace"));
    }
    return new WorkspaceImpl().setTitle(title).setCollections(collections).setAttributes(attributes)
        .setExtesionElements(extElements);
  }

  private CollectionImpl parseCollection(final XMLStreamReader reader) throws XMLStreamException,
      EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_APP_2007, FormatXml.APP_COLLECTION);
    TitleImpl title = null;
    String resourceIdentifier = reader.getAttributeValue(null, FormatXml.ATOM_HREF);
    CommonAttributesImpl attributes = parseCommonAttribute(reader);
    List<ExtensionElement> extElements = new ArrayList<ExtensionElement>();
    List<Accept> acceptList = new ArrayList<Accept>();
    List<Categories> categories = new ArrayList<Categories>();
    if (resourceIdentifier == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent("href"));
    }
    while (reader.hasNext()
        && !(reader.isEndElement() && Edm.NAMESPACE_APP_2007.equals(reader.getNamespaceURI())
        && FormatXml.APP_COLLECTION.equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        currentHandledStartTagName = reader.getLocalName();
        if (FormatXml.ATOM_TITLE.equals(currentHandledStartTagName)) {
          title = parseTitle(reader);
        } else if (FormatXml.APP_ACCEPT.equals(currentHandledStartTagName)) {
          acceptList.add(parseAccept(reader));
        } else if (FormatXml.APP_CATEGORIES.equals(currentHandledStartTagName)) {
          categories.add(parseCategories(reader));
        } else {
          extElements.add(parseExtensionSansTitleElement(reader));
        }
      }
    }
    return new CollectionImpl().setHref(resourceIdentifier).setTitle(title).setCommonAttributes(attributes)
        .setExtesionElements(extElements).setAcceptElements(acceptList).setCategories(categories);
  }

  private TitleImpl parseTitle(final XMLStreamReader reader) throws XMLStreamException {
    reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_TITLE);
    String text = reader.getElementText();
    reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_TITLE);
    return new TitleImpl().setText(text);
  }

  private AcceptImpl parseAccept(final XMLStreamReader reader) throws XMLStreamException {
    reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_APP_2007, FormatXml.APP_ACCEPT);
    CommonAttributesImpl commonAttributes = parseCommonAttribute(reader);
    String text = reader.getElementText();
    reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_APP_2007, FormatXml.APP_ACCEPT);
    return new AcceptImpl().setCommonAttributes(commonAttributes).setText(text);
  }

  private CategoriesImpl parseCategories(final XMLStreamReader reader) throws XMLStreamException,
      EntityProviderException {
    reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_APP_2007, FormatXml.APP_CATEGORIES);
    CategoriesImpl categories = new CategoriesImpl();
    String href = reader.getAttributeValue(null, FormatXml.ATOM_HREF);
    String fixed = reader.getAttributeValue(null, FormatXml.APP_CATEGORIES_FIXED);
    categories.setScheme(reader.getAttributeValue(null, FormatXml.APP_CATEGORIES_SCHEME));
    categories.setHref(href);
    if (href == null) {
      for (int i = 0; i < Fixed.values().length; i++) {
        if (Fixed.values()[i].name().equalsIgnoreCase(fixed)) {
          categories.setFixed(Fixed.values()[i]);
        }
      }
      if (categories.getFixed() == null) {
        categories.setFixed(Fixed.NO);
      }
      List<Category> categoriesList = new ArrayList<Category>();
      while (reader.hasNext()
          && !(reader.isEndElement() && Edm.NAMESPACE_APP_2007.equals(reader.getNamespaceURI())
          && FormatXml.APP_CATEGORIES.equals(reader.getLocalName()))) {
        reader.next();
        if (reader.isStartElement()) {
          currentHandledStartTagName = reader.getLocalName();
          if (FormatXml.ATOM_CATEGORY.equals(currentHandledStartTagName)) {
            categoriesList.add(parseCategory(reader));
          }
        }
      }
      categories.setCategoryList(categoriesList);
    }
    if ((href != null && fixed != null && categories.getScheme() != null) ||
        (href == null && fixed == null && categories.getScheme() == null)) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE
          .addContent("for the element categories"));
    }
    return categories;
  }

  private CategoryImpl parseCategory(final XMLStreamReader reader) throws XMLStreamException {
    reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_ATOM_2005, FormatXml.ATOM_CATEGORY);
    CategoryImpl category = new CategoryImpl();
    category.setScheme(reader.getAttributeValue(null, FormatXml.ATOM_CATEGORY_SCHEME));
    category.setTerm(reader.getAttributeValue(null, FormatXml.ATOM_CATEGORY_TERM));
    category.setLabel(reader.getAttributeValue(null, FormatXml.ATOM_CATEGORY_LABEL));
    CommonAttributesImpl attributes = parseCommonAttribute(reader);
    return category.setCommonAttributes(attributes);
  }

  private ExtensionElementImpl parseExtensionSansTitleElement(final XMLStreamReader reader) throws XMLStreamException,
      EntityProviderException {
    ExtensionElementImpl extElement = new ExtensionElementImpl();
    if (!(Edm.NAMESPACE_APP_2007.equals(reader.getNamespaceURI())
    || (FormatXml.ATOM_TITLE.equals(reader.getLocalName())
    && Edm.NAMESPACE_ATOM_2005.equals(reader.getNamespaceURI())))) {
      extElement = parseElement(reader);
    }
    return extElement;
  }

  private ExtensionElementImpl parseExtensionElement(final XMLStreamReader reader) throws XMLStreamException,
      EntityProviderException {
    ExtensionElementImpl extElement = null;
    if (!Edm.NAMESPACE_APP_2007.equals(reader.getNamespaceURI())) {
      extElement = parseElement(reader);
    }
    return extElement;
  }

  private ExtensionElementImpl parseElement(final XMLStreamReader reader) throws XMLStreamException,
      EntityProviderException {
    List<ExtensionElement> extensionElements = new ArrayList<ExtensionElement>();
    ExtensionElementImpl extElement =
        new ExtensionElementImpl().setName(reader.getLocalName()).setNamespace(reader.getNamespaceURI()).setPrefix(
            reader.getPrefix());
    extElement.setAttributes(parseAttribute(reader));
    while (reader.hasNext()
        && !(reader.isEndElement() && extElement.getName() != null && extElement.getName()
            .equals(reader.getLocalName()))) {
      reader.next();
      if (reader.isStartElement()) {
        extensionElements.add(parseExtensionElement(reader));
      } else if (reader.isCharacters()) {
        String extElementText = "";
        do {
          extElementText = extElementText + reader.getText();
          reader.next();
        } while (reader.isCharacters());
        extElement.setText(extElementText);
      }
    }
    extElement.setElements(extensionElements);
    if (extElement.getText() == null && extElement.getAttributes().isEmpty() && extElement.getElements().isEmpty()) {
      throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent("Invalid extension element"));
    }
    return extElement;
  }

  private List<ExtensionAttribute> parseAttribute(final XMLStreamReader reader) {
    List<ExtensionAttribute> extAttributes = new ArrayList<ExtensionAttribute>();
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      {
        extAttributes.add(new ExtensionAttributeImpl()
            .setName(reader.getAttributeLocalName(i))
            .setNamespace(reader.getAttributeNamespace(i))
            .setPrefix(reader.getAttributePrefix(i))
            .setText(reader.getAttributeValue(i)));
      }
    }

    return extAttributes;
  }

  public ServiceDocumentImpl parseXml(final InputStream in) throws EntityProviderException {
    return readServiceDokument(XmlStreamFactory.createStreamReader(in));
  }
}
