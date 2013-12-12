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
 * <p>Annotation for definition of EdmFactes on an EdmProperty (for an EdmEntityType or EdmComplexType
 * which contains the EdmProperty as a field).</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface EdmFacets {
  /**
   * The maximum length of the type in use.
   * A negative value indicates for the EDM provider an unset/default value.
   * 
   * @return the maximum length of the type in use as Integer
   */
  int maxLength() default -1;

  /**
   * The scale of the type in use.
   * A negative value indicates for the EDM provider an unset/default value.
   * 
   * @return the scale of the type in use as Integer
   */
  int scale() default -1;

  /**
   * The precision of the type in use.
   * A negative value indicates for the EDM provider an unset/default value.
   * 
   * @return the precision of the type in use as Integer
   */
  int precision() default -1;

  /**
   * The information if the type in use is nullable.
   * The default value for nullable is <code>false</code>.
   * 
   * @return <code>true</code> if the type in use is nullable, <code>false</code> otherwise.
   */
  boolean nullable() default false;
}
