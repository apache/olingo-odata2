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
package org.apache.olingo.odata2.core.annotation.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *
 */
public class ClassHelper {
  
  private static final File[] EMPTY_FILE_ARRAY = new File[0];
  
  private static final FilenameFilter CLASSFILE_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(CLASSFILE_ENDING);
    }
    public static final String CLASSFILE_ENDING = ".class";
  };
  
  private static final FileFilter FOLDER_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };
  
  public static final List<Class<?>> loadClasses(String packageToScan, ClassValidator cv) {
    return loadClasses(packageToScan, CLASSFILE_FILTER, cv);
  }

  
  public static final List<Class<?>> loadClasses(String packageToScan, FilenameFilter ff, ClassValidator cv) {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String folderToScan = packageToScan.replace(".", "/");
    URL url = classLoader.getResource(folderToScan);
    if(url == null) {
      throw new IllegalArgumentException("No folder to scan found for package '" + packageToScan + "'.");
    }
    File folder = new File(url.getFile());
    File[] classFiles = folder.listFiles(ff);
    if(classFiles == null) {
      classFiles = EMPTY_FILE_ARRAY;
    }

    List<Class<?>> annotatedClasses = new ArrayList<Class<?>>(classFiles.length);
    for (File file : classFiles) {
      String name = file.getName();
      String fqn = packageToScan + "." + name.substring(0, name.length() - 6);
      try {
        Class<?> c = classLoader.loadClass(fqn);
        if (cv.isClassValid(c)) {
          annotatedClasses.add(c);
        }
      } catch (ClassNotFoundException ex) {
        throw new IllegalArgumentException("Exception during class loading of class '" + fqn + 
                "' with message '" + ex.getMessage() + "'.");
      }
    }
    
    // recursive search
    File[] subfolders = listSubFolder(folder);
    for (File file : subfolders) {
      List<Class<?>> subFolderClazzes = loadClasses(packageToScan + "." + file.getName(), ff, cv);
      annotatedClasses.addAll(subFolderClazzes);
    }
    //
    
    return annotatedClasses;
  }
  
  public static Object getFieldValue(Object instance, Field field) {
    try {
      boolean access = field.isAccessible();
      field.setAccessible(true);
      Object value = field.get(instance);
      field.setAccessible(access);
      return value;
    } catch (IllegalArgumentException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    } catch (IllegalAccessException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    }
  }

  public static void setFieldValue(Object instance, Field field, Object value) {
    try {
      boolean access = field.isAccessible();
      field.setAccessible(true);
      field.set(instance, value);
      field.setAccessible(access);
    } catch (IllegalArgumentException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    } catch (IllegalAccessException ex) { // should never happen
      throw new ODataRuntimeException(ex);
    }
  }


  private static File[] listSubFolder(File folder) {
    File[] subfolders = folder.listFiles(FOLDER_FILTER);
    if(subfolders == null) {
      return EMPTY_FILE_ARRAY;
    }
    return subfolders;
  }
  
  public interface ClassValidator {
    boolean isClassValid(Class<?> c);
  }
}
