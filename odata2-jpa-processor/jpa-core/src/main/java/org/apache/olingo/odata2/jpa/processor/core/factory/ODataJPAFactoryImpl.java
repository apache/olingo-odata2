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
package org.apache.olingo.odata2.jpa.processor.core.factory;

import java.util.Locale;

import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPADefaultProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAResponseBuilder;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmMappingModelAccess;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAMethodContext.JPAMethodContextBuilder;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAProcessor;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAMessageService;
import org.apache.olingo.odata2.jpa.processor.api.factory.JPAAccessFactory;
import org.apache.olingo.odata2.jpa.processor.api.factory.JPQLBuilderFactory;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAAccessFactory;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext.JPQLContextBuilder;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextView;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement.JPQLStatementBuilder;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmModelView;
import org.apache.olingo.odata2.jpa.processor.core.ODataJPAContextImpl;
import org.apache.olingo.odata2.jpa.processor.core.ODataJPAResponseBuilderDefault;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAFunctionContext;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAProcessorImpl;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmMappingModelService;
import org.apache.olingo.odata2.jpa.processor.core.edm.ODataJPAEdmProvider;
import org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLJoinSelectContext;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLJoinSelectSingleContext;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLJoinSelectSingleStatementBuilder;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLJoinStatementBuilder;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLSelectContext;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLSelectSingleContext;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLSelectSingleStatementBuilder;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLSelectStatementBuilder;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmModel;

public class ODataJPAFactoryImpl extends ODataJPAFactory {

  @Override
  public JPQLBuilderFactory getJPQLBuilderFactory() {
    return JPQLBuilderFactoryImpl.create();
  };

  @Override
  public JPAAccessFactory getJPAAccessFactory() {
    return JPAAccessFactoryImpl.create();
  };

  @Override
  public ODataJPAAccessFactory getODataJPAAccessFactory() {
    return ODataJPAAccessFactoryImpl.create();
  };

  private static class JPQLBuilderFactoryImpl implements JPQLBuilderFactory {

    private static JPQLBuilderFactoryImpl factory = null;

    private JPQLBuilderFactoryImpl() {}

    @Override
    public JPQLStatementBuilder getStatementBuilder(final JPQLContextView context) {
      JPQLStatementBuilder builder = null;
      switch (context.getType()) {
      case SELECT:
      case SELECT_COUNT: // for $count, Same as select
        builder = new JPQLSelectStatementBuilder(context);
        break;
      case SELECT_SINGLE:
        builder = new JPQLSelectSingleStatementBuilder(context);
        break;
      case JOIN:
      case JOIN_COUNT: // for $count, Same as join
        builder = new JPQLJoinStatementBuilder(context);
        break;
      case JOIN_SINGLE:
        builder = new JPQLJoinSelectSingleStatementBuilder(context);
        break;
      default:
        break;
      }

      return builder;
    }

    @Override
    public JPQLContextBuilder getContextBuilder(final JPQLContextType contextType) {
      JPQLContextBuilder contextBuilder = null;

      switch (contextType) {
      case SELECT:
        JPQLSelectContext selectContext = new JPQLSelectContext(false);
        contextBuilder = selectContext.new JPQLSelectContextBuilder();
        break;
      case SELECT_SINGLE:
        JPQLSelectSingleContext singleSelectContext = new JPQLSelectSingleContext();
        contextBuilder = singleSelectContext.new JPQLSelectSingleContextBuilder();
        break;
      case JOIN:
        JPQLJoinSelectContext joinContext = new JPQLJoinSelectContext(false);
        contextBuilder = joinContext.new JPQLJoinContextBuilder();
        break;
      case JOIN_SINGLE:
        JPQLJoinSelectSingleContext joinSingleContext = new JPQLJoinSelectSingleContext();
        contextBuilder = joinSingleContext.new JPQLJoinSelectSingleContextBuilder();
        break;
      case SELECT_COUNT:
        JPQLSelectContext selectCountContext = new JPQLSelectContext(true);
        contextBuilder = selectCountContext.new JPQLSelectContextBuilder();
        break;
      case JOIN_COUNT:
        JPQLJoinSelectContext joinCountContext = new JPQLJoinSelectContext(true);
        contextBuilder = joinCountContext.new JPQLJoinContextBuilder();
        break;
      default:
        break;
      }

      return contextBuilder;
    }

    private static JPQLBuilderFactory create() {
      if (factory == null) {
        return new JPQLBuilderFactoryImpl();
      } else {
        return factory;
      }
    }

    @Override
    public JPAMethodContextBuilder getJPAMethodContextBuilder(final JPQLContextType contextType) {

      JPAMethodContextBuilder contextBuilder = null;
      switch (contextType) {
      case FUNCTION:
        JPAFunctionContext methodConext = new JPAFunctionContext();
        contextBuilder = methodConext.new JPAFunctionContextBuilder();

        break;
      default:
        break;
      }
      return contextBuilder;
    }

  }

  private static class ODataJPAAccessFactoryImpl implements ODataJPAAccessFactory {

    private static ODataJPAAccessFactoryImpl factory = null;

    private ODataJPAAccessFactoryImpl() {}

    @Override
    public ODataSingleProcessor createODataProcessor(final ODataJPAContext oDataJPAContext) {
      return new ODataJPADefaultProcessor(oDataJPAContext) { };
    }

    @Override
    public EdmProvider createJPAEdmProvider(final ODataJPAContext oDataJPAContext) {
      return new ODataJPAEdmProvider(oDataJPAContext);
    }

    @Override
    public ODataJPAContext createODataJPAContext() {
      return new ODataJPAContextImpl();
    }

    private static ODataJPAAccessFactoryImpl create() {
      if (factory == null) {
        return new ODataJPAAccessFactoryImpl();
      } else {
        return factory;
      }
    }

    @Override
    public ODataJPAMessageService getODataJPAMessageService(final Locale locale) {
      return ODataJPAMessageServiceDefault.getInstance(locale);
    }

    @Override
    public ODataJPAResponseBuilder getODataJPAResponseBuilder(final ODataJPAContext oDataJPAContext) {
      return new ODataJPAResponseBuilderDefault(oDataJPAContext);
    }

  }

  private static class JPAAccessFactoryImpl implements JPAAccessFactory {

    private static JPAAccessFactoryImpl factory = null;

    private JPAAccessFactoryImpl() {}

    @Override
    public JPAEdmModelView getJPAEdmModelView(final ODataJPAContext oDataJPAContext) {
      JPAEdmModelView view = null;

      view = new JPAEdmModel(oDataJPAContext);
      return view;
    }

    @Override
    public JPAProcessor getJPAProcessor(final ODataJPAContext oDataJPAContext) {
      JPAProcessor jpaProcessor = new JPAProcessorImpl(oDataJPAContext);

      return jpaProcessor;
    }

    private static JPAAccessFactoryImpl create() {
      if (factory == null) {
        return new JPAAccessFactoryImpl();
      } else {
        return factory;
      }
    }

    @Override
    public JPAEdmMappingModelAccess getJPAEdmMappingModelAccess(final ODataJPAContext oDataJPAContext) {
      JPAEdmMappingModelAccess mappingModelAccess = new JPAEdmMappingModelService(oDataJPAContext);

      return mappingModelAccess;
    }

    @Override
    public JPAEdmMapping getJPAEdmMappingInstance() {
      return new JPAEdmMappingImpl();
    }

  }
}
