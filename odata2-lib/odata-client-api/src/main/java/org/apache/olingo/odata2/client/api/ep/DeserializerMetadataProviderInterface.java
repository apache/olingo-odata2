package org.apache.olingo.odata2.client.api.ep;

import java.io.InputStream;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.client.api.edm.EdmDataServices;

/**
 * 
 * Interface for metadata deserializer methods.
 *
 */
public interface DeserializerMetadataProviderInterface {


  /**
   * Read (de-serialize) data from metadata <code>inputStream</code> (as {@link InputStream}) and provide Edm as
   * {@link Edm}
   * 
   * @param inputStream the given input stream
   * @param validate has to be true if metadata should be validated
   * @return Edm as {@link Edm}
   * @throws EntityProviderException, EdmException if reading of data (de-serialization) fails
   */
  EdmDataServices readMetadata(InputStream content, boolean validate) 
      throws EntityProviderException, EdmException; //NOPMD  - suppressed

}
