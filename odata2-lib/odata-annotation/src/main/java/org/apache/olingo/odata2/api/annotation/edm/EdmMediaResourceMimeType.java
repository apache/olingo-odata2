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
package org.apache.olingo.odata2.api.annotation.edm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation for definition of an {@link EdmProperty} as <b>mime type for the media resource</b> 
 * of the {@link EdmEntityType} which contains the {@link EdmProperty}.
 * The value of the {@link EdmMediaResourceMimeType} annotated field will be used as <code>Content-Type</code>
 * of the media content response (of an OData <code>$value</code> request).
 * </p>
 * This annotation can not be parameterized, all values like name are defined via the {@link EdmProperty} annotation.
 * In addition the {@link EdmMediaResourceMimeType} annotation has to be used in conjunction with an 
 * {@link EdmProperty} annotation on a field within an {@link EdmEntityType} annotated class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EdmMediaResourceMimeType {}