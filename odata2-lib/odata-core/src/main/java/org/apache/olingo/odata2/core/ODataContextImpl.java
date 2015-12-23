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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataDebugCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.uri.PathInfo;

/**
 * Context.
 */
public class ODataContextImpl implements ODataContext {

  private static final String ODATA_BATCH_PARENT_CONTEXT = "~odataBatchParentContext";
  private static final String ODATA_REQUEST = "~odataRequest";
  private static final String DEBUG_MODE = "~debugMode";
  private static final String SERVICE = "~service";
  private static final String SERVICE_FACTORY = "~serviceFactory";
  private static final String PATH_INFO = "~pathInfo";
  private static final String RUNTIME_MEASUREMENTS = "~runtimeMeasurements";
  private static final String HTTP_METHOD = "~httpMethod";

  private Map<String, Object> parameterTable = new HashMap<String, Object>();

  private List<Locale> acceptableLanguages;

  public ODataContextImpl(final ODataRequest request, final ODataServiceFactory factory) {
    setServiceFactory(factory);
    setRequest(request);
    setPathInfo(request.getPathInfo());
    setHttpMethod(request.getHttpMethod());
    setAcceptableLanguages(request.getAcceptableLanguages());
    setDebugMode(checkDebugMode(request.getQueryParameters()));
  }

  @Override
  public void setParameter(final String name, final Object value) {
    parameterTable.put(name, value);
  }

  @Override
  public void removeParameter(final String name) {
    parameterTable.remove(name);
  }

  @Override
  public Object getParameter(final String name) {
    return parameterTable.get(name);
  }

  @Override
  public boolean isInDebugMode() {
    return getParameter(DEBUG_MODE) != null && (Boolean) getParameter(DEBUG_MODE);
  }

  @Override
  public void setDebugMode(final boolean debugMode) {
    setParameter(DEBUG_MODE, debugMode);
  }

  public void setService(final ODataService service) {
    setParameter(SERVICE, service);
  }

  @Override
  public ODataService getService() throws ODataException {
    return (ODataService) getParameter(SERVICE);
  }

  public void setPathInfo(final PathInfo uriInfo) {
    setParameter(PATH_INFO, uriInfo);
  }

  @Override
  public PathInfo getPathInfo() throws ODataException {
    return (PathInfo) getParameter(PATH_INFO);
  }

  public void setServiceFactory(final ODataServiceFactory serviceFactory) {
    setParameter(SERVICE_FACTORY, serviceFactory);
  }

  @Override
  public ODataServiceFactory getServiceFactory() {
    return (ODataServiceFactory) getParameter(SERVICE_FACTORY);
  }

  @Override
  public int startRuntimeMeasurement(final String className, final String methodName) {
    if (isInDebugMode()) {
      List<RuntimeMeasurement> runtimeMeasurements = getRuntimeMeasurements();
      int handleId = runtimeMeasurements.size();

      final RuntimeMeasurement measurement = new RuntimeMeasurementImpl();
      measurement.setTimeStarted(System.nanoTime());
      measurement.setClassName(className);
      measurement.setMethodName(methodName);
      measurement.setMemoryStarted(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());

      runtimeMeasurements.add(measurement);

      return handleId;
    } else {
      return 0;
    }
  }

  @Override
  public void stopRuntimeMeasurement(final int handle) {
    if (isInDebugMode()) {
      long stopTime = System.nanoTime();
      long stopMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();

      RuntimeMeasurement runtimeMeasurement = getRuntimeMeasurement(handle);
      if (runtimeMeasurement != null) {
        runtimeMeasurement.setTimeStopped(stopTime);
        runtimeMeasurement.setMemoryStopped(stopMemory);
      }
    }
  }

  private RuntimeMeasurement getRuntimeMeasurement(final int handle) {
    List<RuntimeMeasurement> runtimeMeasurements = getRuntimeMeasurements();
    if (handle >= 0 && handle < runtimeMeasurements.size()) {
      return runtimeMeasurements.get(handle);
    }
    return null;
  }

  @Override
  public List<RuntimeMeasurement> getRuntimeMeasurements() {
    @SuppressWarnings("unchecked")
    List<RuntimeMeasurement> runtimeMeasurements = (List<RuntimeMeasurement>) getParameter(RUNTIME_MEASUREMENTS);
    if (runtimeMeasurements == null) {
      runtimeMeasurements = new ArrayList<RuntimeMeasurement>();
      setParameter(RUNTIME_MEASUREMENTS, runtimeMeasurements);
    }
    return runtimeMeasurements;
  }

  protected class RuntimeMeasurementImpl implements RuntimeMeasurement {
    private String className;
    private String methodName;
    private long timeStarted;
    private long timeStopped;
    private long memoryStarted;
    private long memoryStopped;

    @Override
    public void setClassName(final String className) {
      this.className = className;
    }

    @Override
    public String getClassName() {
      return className;
    }

    @Override
    public void setMethodName(final String methodName) {
      this.methodName = methodName;
    }

    @Override
    public String getMethodName() {
      return methodName;
    }

    @Override
    public void setTimeStarted(final long start) {
      timeStarted = start;
    }

    @Override
    public long getTimeStarted() {
      return timeStarted;
    }

    @Override
    public void setTimeStopped(final long stop) {
      timeStopped = stop;
    }

    @Override
    public long getTimeStopped() {
      return timeStopped;
    }

    @Override
    public String toString() {
      return className + "." + methodName + ": duration: " + (timeStopped - timeStarted)
          + ", memory: " + (memoryStopped - memoryStarted);
    }

    @Override
    public void setMemoryStarted(final long used) {
      memoryStarted = used;
    }

    @Override
    public void setMemoryStopped(final long used) {
      memoryStopped = used;
    }

    @Override
    public long getMemoryStarted() {
      return memoryStarted;
    }

    @Override
    public long getMemoryStopped() {
      return memoryStopped;
    }
  }

  @Override
  public String getRequestHeader(final String name) {
    ODataRequest request = (ODataRequest) parameterTable.get(ODATA_REQUEST);
    return request.getRequestHeaderValue(name);
  }

  @Override
  public Map<String, List<String>> getRequestHeaders() {
    ODataRequest request = (ODataRequest) parameterTable.get(ODATA_REQUEST);
    return request.getRequestHeaders();
  }

  @Override
  public List<Locale> getAcceptableLanguages() {
    return Collections.unmodifiableList(acceptableLanguages);
  }

  public void setAcceptableLanguages(final List<Locale> acceptableLanguages) {
    this.acceptableLanguages = acceptableLanguages;

    if (this.acceptableLanguages.isEmpty()) {
      final Locale wildcard = new Locale("*");
      this.acceptableLanguages.add(wildcard);
    }
  }

  public void setHttpMethod(final String httpMethod) {
    setParameter(HTTP_METHOD, httpMethod);
  }

  @Override
  public String getHttpMethod() {
    return (String) getParameter(HTTP_METHOD);
  }

  public void setRequest(final ODataRequest request) {
    setParameter(ODATA_REQUEST, request);
  }

  private boolean checkDebugMode(final Map<String, String> queryParameters) {
    final ODataDebugCallback callback = getServiceFactory().getCallback(ODataDebugCallback.class);
    if(callback != null){
      return callback.isDebugEnabled();
    }
    return false;
  }

  public void setBatchParentContext(final ODataContext ctx) {
    setParameter(ODATA_BATCH_PARENT_CONTEXT, ctx);
  }

  @Override
  public ODataContext getBatchParentContext() {
    return (ODataContext) parameterTable.get(ODATA_BATCH_PARENT_CONTEXT);
  }

  @Override
  public boolean isInBatchMode() {
    return parameterTable.containsKey(ODATA_BATCH_PARENT_CONTEXT);
  }

}
