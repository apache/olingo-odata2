package org.apache.olingo.odata2.jpa.processor.core.access.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VirtualClassWrapper implements VirtualClassInterface {

  private Object object;

  public VirtualClassWrapper(Object object) {
    this.object = object;
  }

  @Override
  public Object get(String name) {
    if (object instanceof JsonObject) {
      Object value = null;
      JsonElement entry = ((JsonObject) object).get(name);
      if (entry != null) {
        if (entry.isJsonPrimitive()) {
          if (entry.getAsJsonPrimitive().isBoolean()) {
            value = entry.getAsJsonPrimitive().getAsBoolean();
          } else if (entry.getAsJsonPrimitive().isNumber()) {
            value = entry.getAsJsonPrimitive().getAsNumber();
          } else if (entry.getAsJsonPrimitive().isString()) {
            value = entry.getAsJsonPrimitive().getAsString();
          } else {
            value = entry.toString();
          }
        }
      }
      return value;
    }
    return null;
  }

  @Override
  public VirtualClassInterface set(String name, Object value) {
    if (object instanceof JsonObject) {
      ((JsonObject) object).addProperty(name, value != null ? String.valueOf(value) : null);
    }

    return this;
  }

  @Override
  public Object getObject() {
    return object;
  }
}
