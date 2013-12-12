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
 * <p>Annotation for definition of an {@link EdmProperty} as <b>media resource content</b> for the {@link EdmEntityType}
 * which contains the {@link EdmProperty}. Additionally an {@link EdmEntityType} will be flagged in the EDM as
 * <code>hasStream == true</code> if an {@link EdmProperty} in conjunction with the {@link EdmMediaResourceContent}
 * annotation is defined.</p>
 * This annotation can not be parameterized, all values like name are defined via the {@link EdmProperty} annotation.
 * In addition the {@link EdmMediaResourceContent} annotation has to be used in conjunction with an {@link EdmProperty}
 * annotation on a field within an {@link EdmEntityType} annotated class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EdmMediaResourceContent {}