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

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for JPAEmbeddableTypesMapType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="JPAEmbeddableTypesMapType">
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="JPAEmbeddableType"
 * type="{http://www.apache.org/olingo/odata2/jpa/processor/api/model/mapping}JPAEmbeddableTypeMapType"
 * maxOccurs="unbounded" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JPAEmbeddableTypesMapType", propOrder = {"jpaEmbeddableType"})
public class JPAEmbeddableTypesMapType {

    @XmlElement(name = "JPAEmbeddableType")
    protected List<JPAEmbeddableTypeMapType> jpaEmbeddableType;

    /**
     * Gets the value of the jpaEmbeddableType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is why
     * there is not a <CODE>set</CODE> method for the jpaEmbeddableType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getJPAEmbeddableType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link JPAEmbeddableTypeMapType }
     * 
     * 
     */
    public List<JPAEmbeddableTypeMapType> getJPAEmbeddableType() {
        if (jpaEmbeddableType == null) {
            jpaEmbeddableType = new ArrayList<JPAEmbeddableTypeMapType>();
        }
        return jpaEmbeddableType;
    }

}
