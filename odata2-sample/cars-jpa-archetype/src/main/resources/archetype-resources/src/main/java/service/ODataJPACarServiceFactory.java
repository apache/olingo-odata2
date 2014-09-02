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
package ${package}.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

import ${package}.model.Address;
import ${package}.model.Car;
import ${package}.model.Driver;
import ${package}.model.Key;
import ${package}.model.Manufacturer;

public class ODataJPACarServiceFactory extends ODataJPAServiceFactory {

	private static final String PUNIT_NAME = "MyFormula";
	private static final int PAGE_SIZE = 5;

	/** Load Sample Data **/
	static {

		List<Car> cars = new ArrayList<Car>();
		Calendar mfDate = Calendar.getInstance();
		mfDate.set(2013, 02, 01);
		Address address = new Address("S1", "C1", "Z1", "CN");

		Manufacturer mf = new Manufacturer(1, "SuperCar", mfDate, address, cars);

		Calendar bDate = Calendar.getInstance();
		mfDate.set(1980, 02, 19);
		Driver driver = new Driver(1L, "Speeder", "Super", "Bolt", null, bDate);

		Key key = new Key(1, 2);
		Calendar carDate = Calendar.getInstance();
		carDate.set(2014, 02, 20);
		Car car = new Car(key, "M1", 20000.0, 2014, carDate.getTime(), mf,
				driver);
		cars.add(car);
		driver.setCar(car);

		EntityManager em = Persistence.createEntityManagerFactory(PUNIT_NAME)
				.createEntityManager();
		em.getTransaction().begin();
		em.persist(mf);
		em.persist(driver);
		em.persist(car);
		em.getTransaction().commit();

	}

	@Override
	public ODataJPAContext initializeODataJPAContext()
			throws ODataJPARuntimeException {
		ODataJPAContext oDataJPAContext = getODataJPAContext();
		oDataJPAContext.setEntityManagerFactory(Persistence
				.createEntityManagerFactory(PUNIT_NAME));
		oDataJPAContext.setPersistenceUnitName(PUNIT_NAME);

		oDataJPAContext.setPageSize(PAGE_SIZE);
		setDetailErrors(true);

		return oDataJPAContext;
	}
}
