package org.apache.olingo.odata2.spring;

import org.apache.cxf.jaxrs.spring.JAXRSServerFactoryBeanDefinitionParser;
import org.apache.olingo.odata2.core.rest.ODataExceptionMapperImpl;
import org.apache.olingo.odata2.core.rest.app.ODataApplication;
import org.apache.olingo.odata2.core.rest.spring.ODataRootLocator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class OlingoServerDefinitionParser extends JAXRSServerFactoryBeanDefinitionParser {

	public OlingoServerDefinitionParser() {
		super();
		setBeanClass(SpringJAXRSServerFactoryBean.class);
	}

	@Override
	protected void mapAttribute(BeanDefinitionBuilder bean, Element e,  String name, String val) {
		if ("id".equals(name) || "address".equals(name)) {
			mapToProperty(bean, name, val);
		}
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder bean) {
		super.doParse(element, parserContext, bean);
		ManagedList<BeanDefinition> services = new ManagedList<BeanDefinition>(3);

		if (!parserContext.getRegistry().containsBeanDefinition("OlingoODataExceptionHandler")) {
			AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(ODataExceptionMapperImpl.class).getBeanDefinition();
			definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
			BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, "OlingoODataExceptionHandler", new String[0]);
			registerBeanDefinition(holder, parserContext.getRegistry());
		}

		if (!parserContext.getRegistry().containsBeanDefinition("OlingoODataProvider")) {
			AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(ODataApplication.MyProvider.class).getBeanDefinition();
			definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
			BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, "OlingoODataProvider", new String[0]);
			registerBeanDefinition(holder, parserContext.getRegistry());
		}

		if (!element.hasAttribute("factory")) {
			if (!parserContext.getRegistry().containsBeanDefinition("OlingoODataRootLocator")) {
				AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(ODataRootLocator.class).getBeanDefinition();
				definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
				BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, "OlingoODataRootLocator", new String[0]);
				registerBeanDefinition(holder, parserContext.getRegistry());
			}
			services.add(parserContext.getRegistry().getBeanDefinition("OlingoODataRootLocator"));
		}
		else {
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ODataRootLocator.class);
			builder.setScope(BeanDefinition.SCOPE_PROTOTYPE);
			builder.addPropertyReference("serviceFactory", element.getAttribute("factory"));
			AbstractBeanDefinition definition = builder.getBeanDefinition();
			BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, "OlingoODataRootLocator-"+element.getAttribute("factory"), new String[0]);
			registerBeanDefinition(holder, parserContext.getRegistry());
			services.add(definition);

		}

		services.add(parserContext.getRegistry().getBeanDefinition("OlingoODataExceptionHandler"));
		services.add(parserContext.getRegistry().getBeanDefinition("OlingoODataProvider"));
		bean.addPropertyValue("serviceBeans", services);
	}

}
