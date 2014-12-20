package org.apache.olingo.odata2.jpa.processor.ref.extension;

import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType.Type;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.jpa.processor.ref.util.CustomerImageLoader;

public class CustomerImageProcessor {

  @EdmFunctionImport(returnType = @ReturnType(type = Type.SIMPLE))
  public byte[] getImage(
      @EdmFunctionImportParameter(name = "CustomerId", facets = @EdmFacets(nullable = false)) Long customerId) {
    return CustomerImageLoader.loadImage(customerId);
  }
}
