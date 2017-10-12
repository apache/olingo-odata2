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
package org.apache.olingo.odata2.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.uri.PathSegment;

/**
 *  
 */
public class ODataPathSegmentImpl implements PathSegment {

  private String path;
  private Map<String, List<String>> matrixParameter;

  /**
   * Constructor for an path segment object.
   * @param path path of created path segment
   * @param matrixParameters Map of Lists of matrix parameters for this path segment (can be null if no matrix
   *                         parameters should be set for this path segment)
   */
  public ODataPathSegmentImpl(final String path, final Map<String, List<String>> matrixParameters) {
    this.path = path;

    Map<String, List<String>> unmodifiableMap = new HashMap<String, List<String>>();
    if (matrixParameters != null) {
      for (Entry<String, List<String>> matrixParam : matrixParameters.entrySet()) {
        List<String> values = matrixParam.getValue();
        List<String> tempList = values == null ? null: Collections.unmodifiableList(new ArrayList<String>(values));
        unmodifiableMap.put(matrixParam.getKey(), tempList);
      }
    }

    matrixParameter = Collections.unmodifiableMap(unmodifiableMap);
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Map<String, List<String>> getMatrixParameters() {
    return matrixParameter;
  }

}
