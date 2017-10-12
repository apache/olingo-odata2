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
package org.apache.olingo.odata2.jpa.processor.core;

import java.util.HashMap;
import java.util.Map;

public class ODataParameterizedWhereExpressionUtil {
  /**
   * Map includes where expression clause as the key and the 
   * ODataParameterizedWhereExpression as the value
   */
  private static Map<String, Map<Integer, Object>> parameterizedQueryMap = new 
      HashMap<String, Map<Integer,Object>>();
  
  private static String jpqlStatement = null;

  /**
   * @return the parameterizedQueryMap
   */
  public static Map<String, Map<Integer, Object>> getParameterizedQueryMap() {
    return parameterizedQueryMap;
  }

  /**
   * @param parameterizedQueryMap the parameterizedQueryMap to set
   */
  public static void setParameterizedQueryMap(Map<String, Map<Integer, Object>> parameterizedQueryMap) {
    ODataParameterizedWhereExpressionUtil.parameterizedQueryMap = parameterizedQueryMap;
  }
  
  public static void setJPQLStatement(String jpqlStatement) {
    ODataParameterizedWhereExpressionUtil.jpqlStatement = jpqlStatement;
  }
  
  public static String getJPQLStatement() {
    return jpqlStatement;
  }
 
}
