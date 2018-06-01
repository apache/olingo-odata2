package org.apache.olingo.odata2.core;

import org.apache.olingo.odata2.api.edm.provider.EntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloneUtils {
  private static Object clone(Object obj) {

    if (obj == null)
      return null;

    Object newObj = null;

    if (obj instanceof Map) {
      newObj = new HashMap();
      ((Map)newObj).putAll((Map) obj);
    }

    else if (obj instanceof List) {
      newObj = new ArrayList();
      ((List)newObj).addAll((List) obj);
    } else {
      newObj = obj;
    }

    return newObj;

  }

  private static Object getFieldValue(Class clazz, Object obj, String name) throws IllegalAccessException, NoSuchFieldException {
    Field f = clazz.getDeclaredField(name);
    f.setAccessible(true);
    return f.get(obj);
  }

  public static Object getClone(Object obj) {
    Object newObj = null;
    try {
      newObj = obj.getClass().newInstance();

      for (Field f : obj.getClass().getDeclaredFields()) {
        f.setAccessible(true);
        f.set(newObj, clone(getFieldValue(obj.getClass(), obj, f.getName())));
      }

      for (Field f : obj.getClass().getSuperclass().getDeclaredFields()) {
        f.setAccessible(true);
        f.set(newObj, clone(getFieldValue(obj.getClass().getSuperclass(), obj, f.getName())));
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return newObj;
  }
}
