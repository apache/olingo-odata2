package org.apache.olingo.odata2.jpa.processor.core.mock.data;

public class XmlAdapter extends
	javax.xml.bind.annotation.adapters.XmlAdapter<String, EntityWithXmlAdapterOnProperty> {

	@Override
	public String marshal(EntityWithXmlAdapterOnProperty arg0) throws Exception {
		return "self";
	}

	@Override
	public EntityWithXmlAdapterOnProperty unmarshal(String arg0) throws Exception {
		return new EntityWithXmlAdapterOnProperty();
	}

}