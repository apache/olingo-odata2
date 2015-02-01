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
package org.apache.olingo.odata2.jpa.processor.core.callback;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallback;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallbackResult;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;

public class JPATombstoneCallBack implements TombstoneCallback {

  private static final String DELTA_TOKEN_STRING = "?!deltatoken=";
  private String baseUri;
  private String deltaTokenValue;
  private GetEntitySetUriInfo resultsView;

  public JPATombstoneCallBack(final String baseUri, final GetEntitySetUriInfo resultsView,
      final String deltaTokenValue) {
    this.baseUri = baseUri;
    this.deltaTokenValue = deltaTokenValue;
    this.resultsView = resultsView;
  }

  @Override
  public TombstoneCallbackResult getTombstoneCallbackResult() {
    TombstoneCallbackResult jpaTombstoneCallBackResult = new TombstoneCallbackResult();

    jpaTombstoneCallBackResult.setDeltaLink(buildToken());
    return jpaTombstoneCallBackResult;
  }

  private String buildToken() {
    StringBuilder tokenBuilder = new StringBuilder();
    if (baseUri != null) {
      tokenBuilder.append(baseUri);
    }
    try {
      if (resultsView != null) {
        tokenBuilder.append(resultsView.getTargetEntitySet().getName());
      }
    } catch (EdmException e) {
      // Nothing
    }
    tokenBuilder.append(DELTA_TOKEN_STRING);
    if (deltaTokenValue != null) {
      tokenBuilder.append(deltaTokenValue);
    }
    return tokenBuilder.toString();
  }
}
