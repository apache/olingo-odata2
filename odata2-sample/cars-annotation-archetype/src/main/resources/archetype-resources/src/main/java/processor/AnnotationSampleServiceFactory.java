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
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.processor;

import ${package}.model.Address;
import ${package}.model.Car;
import ${package}.model.Driver;
import ${package}.model.Manufacturer;
import java.util.Calendar;
import java.util.Locale;
import org.apache.olingo.odata2.annotation.processor.api.AnnotationServiceFactory;
import org.apache.olingo.odata2.annotation.processor.core.datasource.DataStore;
import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataDebugCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AnnotationSampleServiceFactory extends ODataServiceFactory {

  /**
   * Instance holder for all annotation relevant instances which should be used as singleton
   * instances within the ODataApplication (ODataService)
   */
  private static class AnnotationInstances {
    final static String MODEL_PACKAGE = "${package}.model";
    final static ODataService ANNOTATION_ODATA_SERVICE;
    
    static {
      try {
        ANNOTATION_ODATA_SERVICE = AnnotationServiceFactory.createAnnotationService(MODEL_PACKAGE);
        initializeSampleData();
      } catch (ODataApplicationException ex) {
        throw new RuntimeException("Exception during sample data generation.", ex);
      } catch (ODataException ex) {
        throw new RuntimeException("Exception during data source initialization generation.", ex);
      }
    }
  }

  @Override
  public ODataService createService(final ODataContext context) throws ODataException {
    // Edm via Annotations and ListProcessor via AnnotationDS with AnnotationsValueAccess
    return AnnotationInstances.ANNOTATION_ODATA_SERVICE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ODataCallback> T getCallback(final Class<? extends ODataCallback> callbackInterface) {
    return (T) (callbackInterface.isAssignableFrom(ScenarioErrorCallback.class)
            ? new ScenarioErrorCallback() : callbackInterface.isAssignableFrom(ODataDebugCallback.class)
            ? new ScenarioDebugCallback() : super.getCallback(callbackInterface));
  }

  /*
   * Helper classes and methods
   */
  private final class ScenarioDebugCallback implements ODataDebugCallback {

    @Override
    public boolean isDebugEnabled() {
      return true;
    }
  }

  private class ScenarioErrorCallback implements ODataErrorCallback {

    private final Logger LOG = LoggerFactory.getLogger(ScenarioErrorCallback.class);

    @Override
    public ODataResponse handleError(final ODataErrorContext context) throws ODataApplicationException {
      if (context.getHttpStatus() == HttpStatusCodes.INTERNAL_SERVER_ERROR) {
        LOG.error("Internal Server Error", context.getException());
      }

      return EntityProvider.writeErrorDocument(context);
    }

  }

  private static <T> DataStore<T> getDataStore(Class<T> clz) throws DataStore.DataStoreException {
    return DataStore.createInMemory(clz, true);
  }

  private static void initializeSampleData() throws ODataApplicationException {
    DataStore<Car> carDs = getDataStore(Car.class);
    Calendar updated = Calendar.getInstance();
    Car c1 = createCar("F1 W02", 167189.00, 2011, updated);
    carDs.create(c1);
    Car c2 = createCar("F1 W04", 242189.99, 2013, updated);
    carDs.create(c2);
    Car c3 = createCar("FF2013", 199189.11, 2013, updated);
    carDs.create(c3);
    Car c4 = createCar("FF2014", 299189.11, 2014, updated);
    carDs.create(c4);

    DataStore<Driver> driverDs = getDataStore(Driver.class);
    Driver d1 = createDriver("Mic", "Shoemaker", "The Fast", createDateTime(1985, 6, 27), c1);
    driverDs.create(d1);
    Driver d2 = createDriver("Nico", "Mulemountain", null, createDateTime(1969, 1, 3), c2);
    driverDs.create(d2);
    Driver d3 = createDriver("Kimi", "Heikkinen", "Iceman", createDateTime(1979, 10, 17), c3);
    driverDs.create(d3);

    Address addressStar = createAddress("Star Street 137", "Stuttgart", "70173", "Germany");
    Manufacturer manStar = createManufacturer("Star Powered Racing", addressStar, createDateTime(1954, 7, 4), c1, c2);

    Address addressHorse = createAddress("Horse Street 1", "Maranello", "41053", "Italy");
    Manufacturer manHorse = createManufacturer("Horse Powered Racing", addressHorse, createDateTime(1929, 11, 16), c3, c4);

    DataStore<Manufacturer> manDs = getDataStore(Manufacturer.class);
    manDs.create(manStar);
    manDs.create(manHorse);
  }

  private static Calendar createDateTime(int year, int month, int day) {
    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    cal.clear();
    cal.set(year, month - 1, day);
    return cal;
  }

  private static Car createCar(String name, double price, int modelyear, Calendar updated) {
    Car car = new Car();
    car.setModel(name);
    car.setModelYear(modelyear);
    car.setPrice(price);
    car.setUpdated(updated.getTime());
    return car;
  }

  private static Driver createDriver(String name, String lastname, String nickname, Calendar birthday, Car car) {
    Driver driver = new Driver();
    driver.setName(name);
    driver.setLastname(lastname);
    driver.setNickname(nickname);
    driver.setBirthday(birthday);
    driver.setCar(car);
    car.setDriver(driver);
    return driver;
  }

  private static Manufacturer createManufacturer(String name, Address address, Calendar founded, Car... cars) {
    Manufacturer m = new Manufacturer();
    m.setName(name);
    m.setAddress(address);
    m.setFounded(founded);
    for (Car car : cars) {
      car.setManufacturer(m);
      m.getCars().add(car);
    }
    return m;
  }

  private static Address createAddress(final String street, final String city, final String zipCode,
          final String country) {
    Address address = new Address();
    address.setStreet(street);
    address.setCity(city);
    address.setZipCode(zipCode);
    address.setCountry(country);
    return address;
  }
}
