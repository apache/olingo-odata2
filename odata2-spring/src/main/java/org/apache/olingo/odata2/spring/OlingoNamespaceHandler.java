package org.apache.olingo.odata2.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


public class OlingoNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("server", new OlingoServerDefinitionParser());
	}

}
