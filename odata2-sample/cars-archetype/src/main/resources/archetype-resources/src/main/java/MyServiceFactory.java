#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import ${groupId}.odata2.api.ODataService;
import ${groupId}.odata2.api.ODataServiceFactory;
import ${groupId}.odata2.api.edm.provider.EdmProvider;
import ${groupId}.odata2.api.exception.ODataException;
import ${groupId}.odata2.api.processor.ODataContext;
import ${groupId}.odata2.api.processor.ODataSingleProcessor;

public class MyServiceFactory extends ODataServiceFactory {

  @Override
  public ODataService createService(ODataContext ctx) throws ODataException {

    EdmProvider edmProvider = new MyEdmProvider();

    ODataSingleProcessor singleProcessor = new MyODataSingleProcessor();

    return createODataSingleProcessorService(edmProvider, singleProcessor);
  }
}
