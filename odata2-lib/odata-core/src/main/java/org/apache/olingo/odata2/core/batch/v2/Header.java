package org.apache.olingo.odata2.core.batch.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class Header implements Cloneable {

  private final Map<String, HeaderField> headers = new HashMap<String, HeaderField>();
  
  public static List<String> splitValuesByComma(final String headerValue) {
    final List<String> singleValues = new ArrayList<String>();

    String[] parts = headerValue.split(",");
    for (final String value : parts) {
      singleValues.add(value.trim());
    }

    return singleValues;
  }
  
  public void addHeader(final String name, final String value) {
    final HeaderField headerField = getHeaderFieldOrDefault(name);
    final List<String> headerValues = headerField.getValues();

    if (!headerValues.contains(value)) {
      headerValues.add(value);
    }
  }

  public void addHeader(final String name, final List<String> values) {
    final HeaderField headerField = getHeaderFieldOrDefault(name);
    final List<String> headerValues = headerField.getValues();

    for (final String value : values) {
      if (!headerValues.contains(value)) {
        headerValues.add(value);
      }
    }
  }
  
  public boolean isHeaderMatching(final String name, final Pattern pattern) {
    if(getHeaders(name).size() != 1 ) {
      return false;
    } else {
      return pattern.matcher(getHeaders(name).get(0)).matches();
    }
  }
  
  public void removeHeaders(final String name) {
    headers.remove(name.toLowerCase(Locale.ENGLISH));
  }

  public String getHeader(final String name) {
    final HeaderField headerField = getHeaderField(name);

    if (headerField == null) {
      return null;
    } else {
      final List<String> headerValues = headerField.getValues();
      final StringBuilder result = new StringBuilder();

      for (final String value : headerValues) {
        result.append(value);
        result.append(", ");
      }
      
      if(result.length()>0) {
        result.delete(result.length() - 2, result.length());
      }
      
      return result.toString();
    }
  }

  public String getHeaderNotNull(final String name) {
    final String value = getHeader(name);

    return (value == null) ? "" : value;
  }

  public List<String> getHeaders(final String name) {
    final HeaderField headerField = getHeaderField(name);

    return (headerField == null) ? new ArrayList<String>() : headerField.getValues();
  }

  public HeaderField getHeaderField(final String name) {
    return headers.get(name.toLowerCase(Locale.ENGLISH));
  }

  public Map<String, String> toSingleMap() {
    final Map<String, String> singleMap = new HashMap<String, String>();

    for (final String key : headers.keySet()) {
      HeaderField field = headers.get(key);
      singleMap.put(field.getFieldName(), getHeader(key));
    }

    return singleMap;
  }

  public Map<String, List<String>> toMultiMap() {
    final Map<String, List<String>> singleMap = new HashMap<String, List<String>>();

    for (final String key : headers.keySet()) {
      HeaderField field = headers.get(key);
      singleMap.put(field.getFieldName(), field.getValues());
    }

    return singleMap;
  }

  private HeaderField getHeaderFieldOrDefault(final String name) {
    HeaderField headerField = headers.get(name.toLowerCase(Locale.ENGLISH));

    if (headerField == null) {
      headerField = new HeaderField(name);
      headers.put(name.toLowerCase(Locale.ENGLISH), headerField);
    }

    return headerField;
  }

  @Override
  public Header clone() {
    final Header newInstance = new Header();

    for (final String key : headers.keySet()) {
      newInstance.headers.put(key, headers.get(key).clone());
    }

    return newInstance;
  }

  public static class HeaderField implements Cloneable {
    private String fieldName;
    private List<String> values;

    public HeaderField(final String fieldName) {
      this(fieldName, new ArrayList<String>());
    }

    public HeaderField(final String fieldName, final List<String> values) {
      this.fieldName = fieldName;
      this.values = values;
    }

    public String getFieldName() {
      return fieldName;
    }

    public List<String> getValues() {
      return values;
    }

    public void setValues(final List<String> values) {
      this.values = values;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      HeaderField other = (HeaderField) obj;
      if (fieldName == null) {
        if (other.fieldName != null) {
          return false;
        }
      } else if (!fieldName.equals(other.fieldName)) {
        return false;
      }
      return true;
    }

    @Override
    public HeaderField clone() {
      List<String> newValues = new ArrayList<String>();
      newValues.addAll(values);

      return new HeaderField(fieldName, newValues);
    }
  }
}
