#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import static ${package}.MyEdmProvider.ENTITY_SET_NAME_CARS;
import static ${package}.MyEdmProvider.ENTITY_SET_NAME_MANUFACTURERS;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ${groupId}.odata2.api.edm.EdmEntitySet;
import ${groupId}.odata2.api.edm.EdmLiteralKind;
import ${groupId}.odata2.api.edm.EdmProperty;
import ${groupId}.odata2.api.edm.EdmSimpleType;
import ${groupId}.odata2.api.ep.EntityProvider;
import ${groupId}.odata2.api.ep.EntityProviderWriteProperties;
import ${groupId}.odata2.api.ep.EntityProviderWriteProperties.ODataEntityProviderPropertiesBuilder;
import ${groupId}.odata2.api.exception.ODataException;
import ${groupId}.odata2.api.exception.ODataNotFoundException;
import ${groupId}.odata2.api.exception.ODataNotImplementedException;
import ${groupId}.odata2.api.processor.ODataResponse;
import ${groupId}.odata2.api.processor.ODataSingleProcessor;
import ${groupId}.odata2.api.uri.KeyPredicate;
import ${groupId}.odata2.api.uri.info.GetEntitySetUriInfo;
import ${groupId}.odata2.api.uri.info.GetEntityUriInfo;

public class MyODataSingleProcessor extends ODataSingleProcessor {

  private final DataStore dataStore;

  public MyODataSingleProcessor() {
    dataStore = new DataStore();
  }

  @Override
  public ODataResponse readEntitySet(GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {

    EdmEntitySet entitySet;

    if (uriInfo.getNavigationSegments().size() == 0) {
      entitySet = uriInfo.getStartEntitySet();

      if (ENTITY_SET_NAME_CARS.equals(entitySet.getName())) {
        return EntityProvider.writeFeed(contentType, entitySet, dataStore.getCars(),
            EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
      } else if (ENTITY_SET_NAME_MANUFACTURERS.equals(entitySet.getName())) {
        return EntityProvider.writeFeed(contentType, entitySet, dataStore.getManufacturers(),
            EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (uriInfo.getNavigationSegments().size() == 1) {
      // navigation first level, simplified example for illustration purposes only
      entitySet = uriInfo.getTargetEntitySet();

      if (ENTITY_SET_NAME_CARS.equals(entitySet.getName())) {
        int manufacturerKey = getKeyValue(uriInfo.getKeyPredicates().get(0));

        List<Map<String, Object>> cars = new ArrayList<Map<String, Object>>();
        cars.addAll(dataStore.getCarsFor(manufacturerKey));

        return EntityProvider.writeFeed(contentType, entitySet, cars, EntityProviderWriteProperties.serviceRoot(
            getContext().getPathInfo().getServiceRoot()).build());
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
    }

    throw new ODataNotImplementedException();
  }

  @Override
  public ODataResponse readEntity(GetEntityUriInfo uriInfo, String contentType) throws ODataException {

    if (uriInfo.getNavigationSegments().size() == 0) {
      EdmEntitySet entitySet = uriInfo.getStartEntitySet();

      if (ENTITY_SET_NAME_CARS.equals(entitySet.getName())) {
        int id = getKeyValue(uriInfo.getKeyPredicates().get(0));
        Map<String, Object> data = dataStore.getCar(id);

        if (data != null) {
          URI serviceRoot = getContext().getPathInfo().getServiceRoot();
          ODataEntityProviderPropertiesBuilder propertiesBuilder =
              EntityProviderWriteProperties.serviceRoot(serviceRoot);

          return EntityProvider.writeEntry(contentType, entitySet, data, propertiesBuilder.build());
        }
      } else if (ENTITY_SET_NAME_MANUFACTURERS.equals(entitySet.getName())) {
        int id = getKeyValue(uriInfo.getKeyPredicates().get(0));
        Map<String, Object> data = dataStore.getManufacturer(id);

        if (data != null) {
          URI serviceRoot = getContext().getPathInfo().getServiceRoot();
          ODataEntityProviderPropertiesBuilder propertiesBuilder =
              EntityProviderWriteProperties.serviceRoot(serviceRoot);

          return EntityProvider.writeEntry(contentType, entitySet, data, propertiesBuilder.build());
        }
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (uriInfo.getNavigationSegments().size() == 1) {
      // navigation first level, simplified example for illustration purposes only
      EdmEntitySet entitySet = uriInfo.getTargetEntitySet();

      Map<String, Object> data = null;

      if (ENTITY_SET_NAME_MANUFACTURERS.equals(entitySet.getName())) {
        int carKey = getKeyValue(uriInfo.getKeyPredicates().get(0));
        data = dataStore.getManufacturerFor(carKey);
      }

      if (data != null) {
        return EntityProvider.writeEntry(contentType, uriInfo.getTargetEntitySet(),
            data, EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
    }

    throw new ODataNotImplementedException();
  }

  private int getKeyValue(KeyPredicate key) throws ODataException {
    EdmProperty property = key.getProperty();
    EdmSimpleType type = (EdmSimpleType) property.getType();
    return type.valueOfString(key.getLiteral(), EdmLiteralKind.DEFAULT, property.getFacets(), Integer.class);
  }
}
