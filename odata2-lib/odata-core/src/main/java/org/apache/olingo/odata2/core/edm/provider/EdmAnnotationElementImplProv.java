package org.apache.olingo.odata2.core.edm.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmAnnotationAttribute;
import org.apache.olingo.odata2.api.edm.EdmAnnotationElement;
import org.apache.olingo.odata2.api.edm.provider.AnnotationElement;

public class EdmAnnotationElementImplProv implements EdmAnnotationElement {

  private AnnotationElement element;
  ArrayList<EdmAnnotationElement> childElements;
  List<EdmAnnotationAttribute> attributes;

  public EdmAnnotationElementImplProv(final AnnotationElement element) {
    this.element = element;
  }

  @Override
  public String getName() {
    return element.getName();
  }

  @Override
  public String getNamespace() {
    return element.getNamespace();
  }

  @Override
  public String getPrefix() {
    return element.getPrefix();
  }

  @Override
  public String getText() {
    return element.getText();
  }

  @Override
  public List<EdmAnnotationElement> getChildElements() {
    if (childElements == null && element.getChildElements() != null) {
      childElements = new ArrayList<EdmAnnotationElement>();
      for (AnnotationElement childElement : element.getChildElements()) {
        childElements.add(new EdmAnnotationElementImplProv(childElement));
      }
    }
    return childElements;
  }

  @Override
  public List<EdmAnnotationAttribute> getAttributes() {
    if (attributes == null && element.getAttributes() != null) {
      attributes = new ArrayList<EdmAnnotationAttribute>();
      attributes.addAll(element.getAttributes());
    }
    return attributes;
  }
}
