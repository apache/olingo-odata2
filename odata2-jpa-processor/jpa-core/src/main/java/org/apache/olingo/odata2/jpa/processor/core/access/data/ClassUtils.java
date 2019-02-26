package org.apache.olingo.odata2.jpa.processor.core.access.data;

import com.google.gson.internal.Primitives;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ClassUtils {

  private static final Set<Class<?>> NATIVE_TYPES = getNativeTypes();

  public static boolean isComplexType(Class<?> clazz) {
    return !NATIVE_TYPES.contains(clazz) && !Primitives.isWrapperType(clazz);
  }

  private  static Set<Class<?>> getNativeTypes() {
    Set<Class<?>> types = new HashSet<Class<?>>();

    types.add(String.class);
    types.add(BigDecimal.class);
    types.add(Number.class);
    types.add(Date.class);
    types.add(java.sql.Date.class);
    types.add(Time.class);
    types.add(Timestamp.class);
    types.add(Calendar.class);

    return types;
  }

}
