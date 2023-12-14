/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.jpa.processor.api.model.mapping;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 *
 * By default Java Persistence Unit name is taken as EDM schema name. This can be overriden using
 * JPAPersistenceUnitMapType.
 *
 *
 * <p>
 * Java class for JPAPersistenceUnitMapType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="JPAPersistenceUnitMapType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="EDMSchemaNamespace" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="JPAEntityTypes"
 * type="{http://www.apache.org/olingo/odata2/jpa/processor/api/model/mapping}JPAEntityTypesMapType"/>
 * &lt;element name="JPAEmbeddableTypes"
 * type="{http://www.apache.org/olingo/odata2/jpa/processor/api/model/mapping}JPAEmbeddableTypesMapType"/>
 * &lt;/sequence>
 * &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JPAPersistenceUnitMapType", propOrder = {"edmSchemaNamespace", "jpaEntityTypes", "jpaEmbeddableTypes"})
public class JPAPersistenceUnitMapType {

    @XmlElement(name = "EDMSchemaNamespace")
    protected String edmSchemaNamespace;
    @XmlElement(name = "JPAEntityTypes", required = true)
    protected JPAEntityTypesMapType jpaEntityTypes;
    @XmlElement(name = "JPAEmbeddableTypes", required = true)
    protected JPAEmbeddableTypesMapType jpaEmbeddableTypes;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the edmSchemaNamespace property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getEDMSchemaNamespace() {
        return edmSchemaNamespace;
    }

    /**
     * Sets the value of the edmSchemaNamespace property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setEDMSchemaNamespace(final String value) {
        edmSchemaNamespace = value;
    }

    /**
     * Gets the value of the jpaEntityTypes property.
     * 
     * @return possible object is {@link JPAEntityTypesMapType }
     * 
     */
    public JPAEntityTypesMapType getJPAEntityTypes() {
        return jpaEntityTypes;
    }

    /**
     * Sets the value of the jpaEntityTypes property.
     * 
     * @param value allowed object is {@link JPAEntityTypesMapType }
     * 
     */
    public void setJPAEntityTypes(final JPAEntityTypesMapType value) {
        jpaEntityTypes = value;
    }

    /**
     * Gets the value of the jpaEmbeddableTypes property.
     * 
     * @return possible object is {@link JPAEmbeddableTypesMapType }
     * 
     */
    public JPAEmbeddableTypesMapType getJPAEmbeddableTypes() {
        return jpaEmbeddableTypes;
    }

    /**
     * Sets the value of the jpaEmbeddableTypes property.
     * 
     * @param value allowed object is {@link JPAEmbeddableTypesMapType }
     * 
     */
    public void setJPAEmbeddableTypes(final JPAEmbeddableTypesMapType value) {
        jpaEmbeddableTypes = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setName(final String value) {
        name = value;
    }

}
