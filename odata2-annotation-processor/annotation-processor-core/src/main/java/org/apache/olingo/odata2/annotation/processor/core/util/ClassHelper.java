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
package org.apache.olingo.odata2.annotation.processor.core.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 */
public class ClassHelper {

  private static final String JAR_FILE_ENDING = "jar";
  private static final String JAR_RESOURCE_SEPARATOR = "!";
  private static final char RESOURCE_SEPARATOR = '/';
  private static final char PACKAGE_SEPARATOR = '.';
  private static final File[] EMPTY_FILE_ARRAY = new File[0];
  private static final String CLASSFILE_ENDING = ".class";

  private static final FilenameFilter CLASSFILE_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(final File dir, final String name) {
      return name.endsWith(CLASSFILE_ENDING);
    }
  };

  private static final FileFilter FOLDER_FILTER = new FileFilter() {
    @Override
    public boolean accept(final File pathname) {
      return pathname.isDirectory();
    }
  };

  public static final List<Class<?>> loadClasses(final String packageToScan, final ClassValidator cv) {
    return loadClasses(packageToScan, CLASSFILE_FILTER, cv);
  }

  public static final List<Class<?>> loadClasses(final String packageToScan, final FilenameFilter ff,
      final ClassValidator cv) {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String folderToScan = packageToScan.replace(PACKAGE_SEPARATOR, RESOURCE_SEPARATOR);
    URL url = classLoader.getResource(folderToScan);
    if (url == null) {
      throw new IllegalArgumentException("No folder to scan found for package '" + packageToScan + "'.");
    }

    final Collection<String> fqnForClasses;
    File folder = new File(url.getFile());
    if (folder.isDirectory()) {
      fqnForClasses = getClassFqnFromDir(ff, folder, packageToScan);
    } else if (isJarFile(url)) {
      fqnForClasses = getClassFqnFromJar(url.getFile().substring(5), packageToScan);
    } else {
      fqnForClasses = null;
    }

    if (fqnForClasses == null || fqnForClasses.isEmpty()) {
      return Collections.emptyList();
    }

    List<Class<?>> annotatedClasses = new ArrayList<Class<?>>(fqnForClasses.size());
    for (String fqn : fqnForClasses) {
      try {
        Class<?> c = classLoader.loadClass(fqn);
        if (cv.isClassValid(c)) {
          annotatedClasses.add(c);
        }
      } catch (ClassNotFoundException ex) {
        throw new IllegalArgumentException("Exception during class loading of class '" + fqn +
            " from resource '" + url.getFile() + "'" +
            "' with message '" + ex.getMessage() + "'.");
      }
    }

    return annotatedClasses;
  }

  private static boolean isJarFile(final URL url) {
    String filename = url.getFile();
    int index = filename.indexOf(JAR_RESOURCE_SEPARATOR);
    if (index > JAR_FILE_ENDING.length()) {
      String fileEnding = filename.substring(index - JAR_FILE_ENDING.length(), index);
      return JAR_FILE_ENDING.equalsIgnoreCase(fileEnding);
    }
    return false;
  }

  private static Collection<String> getClassFqnFromDir(final FilenameFilter ff, final File folder,
      final String packageToScan) {
    List<String> classFiles = new ArrayList<String>();
    String[] classFilesForFolder = folder.list(ff);
    for (String name : classFilesForFolder) {
      String fqn = packageToScan + "." + name.substring(0, name.length() - CLASSFILE_ENDING.length());
      classFiles.add(fqn);
    }
    // recursive search
    File[] subfolders = listSubFolder(folder);
    for (File file : subfolders) {
      classFiles.addAll(getClassFqnFromDir(ff, file, packageToScan + PACKAGE_SEPARATOR + file.getName()));
    }
    //
    return classFiles;
  }

  private static Collection<String> getClassFqnFromJar(final String filepath, final String packageToScan) {
    JarFile jarFile = null;

    final String jarFilePath;
    String[] split = filepath.split(JAR_RESOURCE_SEPARATOR);
    if (split.length == 2) {
      jarFilePath = split[0];
    } else {
      throw new IllegalArgumentException("Illegal jar file path '" + filepath + "'.");
    }

    try {
      jarFile = new JarFile(jarFilePath);
      List<String> classFileNames = new ArrayList<String>();
      Enumeration<JarEntry> entries = jarFile.entries();

      while (entries.hasMoreElements()) {
        JarEntry je = entries.nextElement();
        String name = je.getName();
        if (!je.isDirectory() && name.matches(".*" + packageToScan + ".*" + CLASSFILE_ENDING)) {
          String className = name.substring(0, name.length() - CLASSFILE_ENDING.length());
          classFileNames.add(className.replace(RESOURCE_SEPARATOR, PACKAGE_SEPARATOR));
        }
      }

      return classFileNames;
    } catch (IOException e) {
      throw new IllegalArgumentException("Exception during class loading from path '" + jarFilePath +
          "' with message '" + e.getMessage() + "'.");
    } finally {
      if (jarFile != null) {
        try {
          jarFile.close();
        } catch (IOException e) {
          throw new RuntimeException("Error during close of jar file: " + jarFile.getName() + "", e);
        }
      }
    }
  }

  public static Object getFieldValue(final Object instance, final Field field) {
    try {
      synchronized (field) {
        boolean access = field.isAccessible();
        field.setAccessible(true);
        Object value = field.get(instance);
        field.setAccessible(access);
        return value;
      }
    } catch (IllegalArgumentException ex) { // should never happen
      throw new AnnotationRuntimeException(ex);
    } catch (IllegalAccessException ex) { // should never happen
      throw new AnnotationRuntimeException(ex);
    }
  }

  public static void setFieldValue(final Object instance, final Field field, final Object value) {
    try {
      synchronized (field) {
        boolean access = field.isAccessible();
        field.setAccessible(true);
        field.set(instance, value);
        field.setAccessible(access);
      }
    } catch (IllegalArgumentException ex) { // should never happen
      throw new AnnotationRuntimeException(ex);
    } catch (IllegalAccessException ex) { // should never happen
      throw new AnnotationRuntimeException(ex);
    }
  }

  private static File[] listSubFolder(final File folder) {
    File[] subfolders = folder.listFiles(FOLDER_FILTER);
    if (subfolders == null) {
      return EMPTY_FILE_ARRAY;
    }
    return subfolders;
  }

  public interface ClassValidator {
    boolean isClassValid(Class<?> c);
  }
}
