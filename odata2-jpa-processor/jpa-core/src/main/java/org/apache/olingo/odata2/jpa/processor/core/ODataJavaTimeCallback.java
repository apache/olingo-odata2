package org.apache.olingo.odata2.jpa.processor.core;

import org.apache.olingo.odata2.api.ODataCallback;

import java.util.Calendar;

/**
 * Created by marcinpiela on 05/10/2024.
 */
public interface ODataJavaTimeCallback extends ODataCallback {
    Object convert(Calendar calendar, String javaTimeType);
}
