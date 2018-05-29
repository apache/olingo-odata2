package org.apache.olingo.odata2.core.rest;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorMessageHandler {
  private static final Pattern EXCEPTION_NAME_PATTERN = Pattern.compile("^([a-zA-Z0-9]+\\.[a-zA-Z0-9._]+:)");

  private static HashSet<String> IGNORED = new HashSet<String>();

  private String error;
  private int status;
  private String method;

  private String stackTrace;

  private static final String PRIMARY_KEY = "primaryKey";
  private static final String PRIMARY_KEY_ERROR = "primaryKeyError";
  private static final String FOREIGN_KEY = "foreignKey";
  private static final String FOREIGN_KEY_ERROR = "foreignKeyError";


  private static final String ERROR_HANDLES = "errorHandles";

  static {
    IGNORED.add("java.lang.reflect.InvocationTargetException");
    IGNORED.add("java.lang.NullPointerException");
  }

  private static boolean hasIgnoredException(Throwable ex) {
    for (String s : IGNORED) {
      if ((ex.getMessage() != null && ex.getMessage().contains(s)) || ex.getClass().getCanonicalName().equals(s)) {
        return true;
      }
    }

    return false;
  }

  private static boolean hasThrowable(Throwable ex, String clazz) {
    while (ex != null) {
      if (ex.getClass().getName().equalsIgnoreCase(clazz)) {
        return true;
      }

      ex = ex.getCause();
    }

    return false;
  }

  public static RuntimeException createException(Throwable ex) {
    final String message = getExceptionMessage(ex);
    return new RuntimeException(ex) {
      @Override
      public String getMessage() {
        return message;
      }
    };
  }

  public static String getExceptionMessage(Throwable ex) {

    String message = null;

    if (ex != null) {
      if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty() && !hasIgnoredException(ex)) {
        message = ex.getMessage();
        Matcher matcher = EXCEPTION_NAME_PATTERN.matcher(message);
        while (matcher.find()) {
          message = message.substring(matcher.group(1).length()).trim();
          matcher = EXCEPTION_NAME_PATTERN.matcher(message);
        }
      } else {
        if (ex.getCause() != null) {
          return getExceptionMessage(ex.getCause());
        }
      }
    }

    if (message == null || message.trim().isEmpty()) {
      return "Error nor specified";
    }

    return message;

  }
}
