package org.apache.olingo.odata2.jpa.processor.core.mock.data;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class EntityWithXmlAdapterOnProperty {
	private EntityWithXmlAdapterOnProperty self;

	@XmlJavaTypeAdapter(XmlAdapter.class)
	public EntityWithXmlAdapterOnProperty getSelf() {
		return self;
	}

	public void setSelf(EntityWithXmlAdapterOnProperty self) {
		this.self = self;
	}
}