/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.uri.expression;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.core.edm.Bit;
import org.apache.olingo.odata2.core.edm.EdmBinary;
import org.apache.olingo.odata2.core.edm.EdmBoolean;
import org.apache.olingo.odata2.core.edm.EdmByte;
import org.apache.olingo.odata2.core.edm.EdmDateTime;
import org.apache.olingo.odata2.core.edm.EdmDateTimeOffset;
import org.apache.olingo.odata2.core.edm.EdmDecimal;
import org.apache.olingo.odata2.core.edm.EdmDouble;
import org.apache.olingo.odata2.core.edm.EdmGuid;
import org.apache.olingo.odata2.core.edm.EdmInt16;
import org.apache.olingo.odata2.core.edm.EdmInt32;
import org.apache.olingo.odata2.core.edm.EdmInt64;
import org.apache.olingo.odata2.core.edm.EdmNull;
import org.apache.olingo.odata2.core.edm.EdmSByte;
import org.apache.olingo.odata2.core.edm.EdmSimpleTypeFacadeImpl;
import org.apache.olingo.odata2.core.edm.EdmSingle;
import org.apache.olingo.odata2.core.edm.EdmString;
import org.apache.olingo.odata2.core.edm.EdmTime;
import org.apache.olingo.odata2.core.edm.Uint7;
import org.junit.Test;

/**
 *
 * Differences to ABAP Parser - for $orderBy it the sortorder (asc/desc) not case insensitive
 * anymore, so ASC/DESC is not allowed
 *
 */
public class TestAbapCompatibility extends TestBase {

    @Test
    public void nullTests() {

        // unary negate/not

        GetPTF("  - null").aSerialized("{- null}")
                          .aEdmType(EdmSimpleTypeFacadeImpl.getEdmSimpleType(EdmSimpleTypeKind.Null));
        GetPTF("not null").aSerialized("{not null}")
                          .aEdmType(EdmSimpleTypeFacadeImpl.getEdmSimpleType(EdmSimpleTypeKind.Null));

        // binary add/sub
        GetPTF("-1   add null").aSerialized("{-1 add null}")
                               .aEdmType(EdmSimpleTypeFacadeImpl.getEdmSimpleType(EdmSimpleTypeKind.SByte));
        GetPTF("null add   -1").aSerialized("{null add -1}")
                               .aEdmType(EdmSimpleTypeFacadeImpl.getEdmSimpleType(EdmSimpleTypeKind.SByte));

        GetPTF("130  add null").aSerialized("{130 add null}")
                               .aEdmType(EdmByte.getInstance());
        GetPTF("null add  130").aSerialized("{null add 130}")
                               .aEdmType(EdmByte.getInstance());

        GetPTF("12345 add  null").aSerialized("{12345 add null}")
                                 .aEdmType(EdmInt16.getInstance());
        GetPTF("null  add 12345").aSerialized("{null add 12345}")
                                 .aEdmType(EdmInt16.getInstance());

        GetPTF("1234512345  add       null").aSerialized("{1234512345 add null}")
                                            .aEdmType(EdmInt32.getInstance());
        GetPTF("null        add 1234512345").aSerialized("{null add 1234512345}")
                                            .aEdmType(EdmInt32.getInstance());

        GetPTF("12345L add   null").aSerialized("{12345L add null}")
                                   .aEdmType(EdmInt64.getInstance());
        GetPTF("null   add 12345L").aSerialized("{null add 12345L}")
                                   .aEdmType(EdmInt64.getInstance());

        GetPTF("1.1F add null").aSerialized("{1.1F add null}")
                               .aEdmType(EdmSingle.getInstance());
        GetPTF("null add 1.1F").aSerialized("{null add 1.1F}")
                               .aEdmType(EdmSingle.getInstance());

        GetPTF("1.1D add null").aSerialized("{1.1D add null}")
                               .aEdmType(EdmDouble.getInstance());
        GetPTF("null add 1.1D").aSerialized("{null add 1.1D}")
                               .aEdmType(EdmDouble.getInstance());

        GetPTF("1.1M add null").aSerialized("{1.1M add null}")
                               .aEdmType(EdmDecimal.getInstance());
        GetPTF("null add 1.1M").aSerialized("{null add 1.1M}")
                               .aEdmType(EdmDecimal.getInstance());

        // binary mul/div/mod

        GetPTF("-1   mul null").aSerialized("{-1 mul null}")
                               .aEdmType(EdmSimpleTypeFacadeImpl.getEdmSimpleType(EdmSimpleTypeKind.SByte));
        GetPTF("null mul   -1").aSerialized("{null mul -1}")
                               .aEdmType(EdmSimpleTypeFacadeImpl.getEdmSimpleType(EdmSimpleTypeKind.SByte));

        GetPTF("130  mul null").aSerialized("{130 mul null}")
                               .aEdmType(EdmByte.getInstance());
        GetPTF("null mul  130").aSerialized("{null mul 130}")
                               .aEdmType(EdmByte.getInstance());

        GetPTF("12345 mul  null").aSerialized("{12345 mul null}")
                                 .aEdmType(EdmInt16.getInstance());
        GetPTF("null  mul 12345").aSerialized("{null mul 12345}")
                                 .aEdmType(EdmInt16.getInstance());

        GetPTF("1234512345  mul       null").aSerialized("{1234512345 mul null}")
                                            .aEdmType(EdmInt32.getInstance());
        GetPTF("null        mul 1234512345").aSerialized("{null mul 1234512345}")
                                            .aEdmType(EdmInt32.getInstance());

        GetPTF("12345L mul   null").aSerialized("{12345L mul null}")
                                   .aEdmType(EdmInt64.getInstance());
        GetPTF("null   mul 12345L").aSerialized("{null mul 12345L}")
                                   .aEdmType(EdmInt64.getInstance());

        GetPTF("1.1F mul null").aSerialized("{1.1F mul null}")
                               .aEdmType(EdmSingle.getInstance());
        GetPTF("null mul 1.1F").aSerialized("{null mul 1.1F}")
                               .aEdmType(EdmSingle.getInstance());

        GetPTF("1.1D mul null").aSerialized("{1.1D mul null}")
                               .aEdmType(EdmDouble.getInstance());
        GetPTF("null mul 1.1D").aSerialized("{null mul 1.1D}")
                               .aEdmType(EdmDouble.getInstance());

        GetPTF("1.1M mul null").aSerialized("{1.1M mul null}")
                               .aEdmType(EdmDecimal.getInstance());
        GetPTF("null mul 1.1M").aSerialized("{null mul 1.1M}")
                               .aEdmType(EdmDecimal.getInstance());

        // relational gt/ge/lt/le

        GetPTF("'TEST' gt null").aSerialized("{'TEST' gt null}")
                                .aEdmType(EdmBoolean.getInstance());
        GetPTF("null   gt 'TEST'").aSerialized("{null gt 'TEST'}")
                                  .aEdmType(EdmBoolean.getInstance());

        GetPTF("time'PT19H02M01S' gt              null").aSerialized("{time'PT19H02M01S' gt null}")
                                                        .aEdmType(EdmBoolean.getInstance());
        GetPTF("null              gt time'PT19H02M01S'").aSerialized("{null gt time'PT19H02M01S'}")
                                                        .aEdmType(EdmBoolean.getInstance());

        GetPTF("datetime'2011-07-31T23:30:59' gt null").aSerialized("{datetime'2011-07-31T23:30:59' gt null}")
                                                       .aEdmType(EdmBoolean.getInstance());
        GetPTF("null                          gt datetime'2011-07-31T23:30:59'").aSerialized("{null gt datetime'2011-07-31T23:30:59'}")
                                                                                .aEdmType(EdmBoolean.getInstance());

        GetPTF("datetimeoffset'2002-10-10T12:00:00-05:00' gt null").aSerialized("{datetimeoffset'2002-10-10T12:00:00-05:00' gt null}")
                                                                   .aEdmType(EdmBoolean.getInstance());
        GetPTF("null  gt datetimeoffset'2002-10-10T12:00:00-05:00'").aSerialized("{null gt datetimeoffset'2002-10-10T12:00:00-05:00'}")
                                                                    .aEdmType(EdmBoolean.getInstance());

        GetPTF("guid'12345678-1234-1234-1234-123456789012' gt null").aSerialized("{guid'12345678-1234-1234-1234-123456789012' gt null}")
                                                                    .aEdmType(EdmBoolean.getInstance());
        GetPTF("null gt guid'12345678-1234-1234-1234-123456789012'").aSerialized("{null gt guid'12345678-1234-1234-1234-123456789012'}")
                                                                    .aEdmType(EdmBoolean.getInstance());

        GetPTF("-1   gt null").aSerialized("{-1 gt null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null gt   -1").aSerialized("{null gt -1}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("130  gt null").aSerialized("{130 gt null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null gt  130").aSerialized("{null gt 130}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("12345 gt  null").aSerialized("{12345 gt null}")
                                .aEdmType(EdmBoolean.getInstance());
        GetPTF("null  gt 12345").aSerialized("{null gt 12345}")
                                .aEdmType(EdmBoolean.getInstance());

        GetPTF("1234512345  gt       null").aSerialized("{1234512345 gt null}")
                                           .aEdmType(EdmBoolean.getInstance());
        GetPTF("null        gt 1234512345").aSerialized("{null gt 1234512345}")
                                           .aEdmType(EdmBoolean.getInstance());

        GetPTF("12345L gt   null").aSerialized("{12345L gt null}")
                                  .aEdmType(EdmBoolean.getInstance());
        GetPTF("null   gt 12345L").aSerialized("{null gt 12345L}")
                                  .aEdmType(EdmBoolean.getInstance());

        GetPTF("1.1F gt null").aSerialized("{1.1F gt null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null gt 1.1F").aSerialized("{null gt 1.1F}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("1.1D gt null").aSerialized("{1.1D gt null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null gt 1.1D").aSerialized("{null gt 1.1D}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("1.1M gt null").aSerialized("{1.1M gt null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null gt 1.1M").aSerialized("{null gt 1.1M}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("X'1234567890ABCDEF' gt null").aSerialized("{X'1234567890ABCDEF' gt null}")
                                             .aEdmType(EdmBoolean.getInstance());
        GetPTF("null gt X'1234567890ABCDEF'").aSerialized("{null gt X'1234567890ABCDEF'}")
                                             .aEdmType(EdmBoolean.getInstance());

        // equlity eq/ne

        GetPTF("'TEST' eq null").aSerialized("{'TEST' eq null}")
                                .aEdmType(EdmBoolean.getInstance());
        GetPTF("null   eq 'TEST'").aSerialized("{null eq 'TEST'}")
                                  .aEdmType(EdmBoolean.getInstance());

        GetPTF("time'PT19H02M01S'   eq               null").aSerialized("{time'PT19H02M01S' eq null}")
                                                           .aEdmType(EdmBoolean.getInstance());
        GetPTF("null                eq  time'PT19H02M01S'").aSerialized("{null eq time'PT19H02M01S'}")
                                                           .aEdmType(EdmBoolean.getInstance());

        GetPTF("datetime'2011-07-31T23:30:59'   eq                           null").aSerialized("{datetime'2011-07-31T23:30:59' eq null}")
                                                                                   .aEdmType(EdmBoolean.getInstance());
        GetPTF("null                            eq  datetime'2011-07-31T23:30:59'").aSerialized("{null eq datetime'2011-07-31T23:30:59'}")
                                                                                   .aEdmType(EdmBoolean.getInstance());

        GetPTF("datetimeoffset'2002-10-10T12:00:00-05:00' eq null").aSerialized("{datetimeoffset'2002-10-10T12:00:00-05:00' eq null}")
                                                                   .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq datetimeoffset'2002-10-10T12:00:00-05:00'").aSerialized("{null eq datetimeoffset'2002-10-10T12:00:00-05:00'}")
                                                                   .aEdmType(EdmBoolean.getInstance());

        GetPTF("guid'12345678-1234-1234-1234-123456789012' eq null").aSerialized("{guid'12345678-1234-1234-1234-123456789012' eq null}")
                                                                    .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq guid'12345678-1234-1234-1234-123456789012'").aSerialized("{null eq guid'12345678-1234-1234-1234-123456789012'}")
                                                                    .aEdmType(EdmBoolean.getInstance());

        GetPTF("-1   eq null").aSerialized("{-1 eq null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq   -1").aSerialized("{null eq -1}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("130  eq null").aSerialized("{130 eq null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq  130").aSerialized("{null eq 130}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("12345 eq  null").aSerialized("{12345 eq null}")
                                .aEdmType(EdmBoolean.getInstance());
        GetPTF("null  eq 12345").aSerialized("{null eq 12345}")
                                .aEdmType(EdmBoolean.getInstance());

        GetPTF("1234512345  eq       null").aSerialized("{1234512345 eq null}")
                                           .aEdmType(EdmBoolean.getInstance());
        GetPTF("null        eq 1234512345").aSerialized("{null eq 1234512345}")
                                           .aEdmType(EdmBoolean.getInstance());

        GetPTF("12345L eq   null").aSerialized("{12345L eq null}")
                                  .aEdmType(EdmBoolean.getInstance());
        GetPTF("null   eq 12345L").aSerialized("{null eq 12345L}")
                                  .aEdmType(EdmBoolean.getInstance());

        GetPTF("1.1F eq null").aSerialized("{1.1F eq null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq 1.1F").aSerialized("{null eq 1.1F}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("1.1D eq null").aSerialized("{1.1D eq null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq 1.1D").aSerialized("{null eq 1.1D}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("1.1M eq null").aSerialized("{1.1M eq null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq 1.1M").aSerialized("{null eq 1.1M}")
                              .aEdmType(EdmBoolean.getInstance());

        GetPTF("X'1234567890ABCDEF' eq null").aSerialized("{X'1234567890ABCDEF' eq null}")
                                             .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq X'1234567890ABCDEF'").aSerialized("{null eq X'1234567890ABCDEF'}")
                                             .aEdmType(EdmBoolean.getInstance());

        GetPTF("true eq null").aSerialized("{true eq null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null eq true").aSerialized("{null eq true}")
                              .aEdmType(EdmBoolean.getInstance());

        // logical and/or
        GetPTF("true and null").aSerialized("{true and null}")
                               .aEdmType(EdmBoolean.getInstance());
        GetPTF("null and true").aSerialized("{null and true}")
                               .aEdmType(EdmBoolean.getInstance());

        GetPTF("true or null").aSerialized("{true or null}")
                              .aEdmType(EdmBoolean.getInstance());
        GetPTF("null or true").aSerialized("{null or true}")
                              .aEdmType(EdmBoolean.getInstance());

    }

    @Test
    public void abapTestParameterPromotion() // copy of ABAP method test_parameter_promotion
    {

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>binary( ).
        // lcl_helper=>veri_type( iv_expression = `X'1234567890ABCDEF'` io_expected_type = lo_simple_type ).
        GetPTF("X'1234567890ABCDEF'").aEdmType(EdmBinary.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>boolean( ).
        // lcl_helper=>veri_type( iv_expression = `true` io_expected_type = lo_simple_type ).
        GetPTF("true").aEdmType(EdmBoolean.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>get_instance( iv_name = 'Bit' ).
        // lcl_helper=>veri_type( iv_expression = `1` io_expected_type = lo_simple_type ).
        GetPTF("1").aEdmType(Bit.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>get_instance( iv_name = 'Bit' ).
        // lcl_helper=>veri_type( iv_expression = `0` io_expected_type = lo_simple_type ).
        GetPTF("0").aEdmType(Bit.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>get_instance( iv_name = 'UInt7' ).
        // /lcl_helper=>veri_type( iv_expression = `123` io_expected_type = lo_simple_type ).
        GetPTF("123").aEdmType(Uint7.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>byte( ).
        // lcl_helper=>veri_type( iv_expression = `130` io_expected_type = lo_simple_type ).
        GetPTF("130").aEdmType(EdmByte.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>datetime( ).
        // lcl_helper=>veri_type( iv_expression = `datetime'2011-07-31T23:30:59'` io_expected_type =
        // lo_simple_type ).
        GetPTF("datetime'2011-07-31T23:30:59'").aEdmType(EdmDateTime.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>datetimeoffset( ).
        // lcl_helper=>veri_type( iv_expression = `datetimeoffset'2002-10-10T12:00:00-05:00'`
        // io_expected_type =
        // lo_simple_type ).
        GetPTF("datetimeoffset'2002-10-10T12:00:00-05:00'").aEdmType(EdmDateTimeOffset.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>decimal( ).
        // lcl_helper=>veri_type( iv_expression = `1.1M` io_expected_type = lo_simple_type ).
        GetPTF("1.1M").aEdmType(EdmDecimal.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>double( ).
        // lcl_helper=>veri_type( iv_expression = `1.1D` io_expected_type = lo_simple_type ).
        GetPTF("1.1D").aEdmType(EdmDouble.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>double( ).
        // lcl_helper=>veri_type( iv_expression = `1.1E+02D` io_expected_type = lo_simple_type ).
        GetPTF("1.1E+02D").aEdmType(EdmDouble.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>guid( ).
        // lcl_helper=>veri_type( iv_expression = `guid'12345678-1234-1234-1234-123456789012'`
        // io_expected_type =
        // lo_simple_type ).
        GetPTF("guid'12345678-1234-1234-1234-123456789012'").aEdmType(EdmGuid.getInstance());

        // -->FLOAT not available on OData library for JAVA
        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>float( ).
        // lcl_helper=>veri_type( iv_expression = `1.1F` io_expected_type = lo_simple_type ).
        // GetPTF("1.1F").aEdmType(EdmFloat.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int16( ).
        // lcl_helper=>veri_type( iv_expression = `12345` io_expected_type = lo_simple_type ).
        GetPTF("12345").aEdmType(EdmInt16.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `1234512345` io_expected_type = lo_simple_type ).
        GetPTF("1234512345").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int64( ).
        // lcl_helper=>veri_type( iv_expression = `12345L` io_expected_type = lo_simple_type ).
        GetPTF("12345L").aEdmType(EdmInt64.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>sbyte( ).
        // lcl_helper=>veri_type( iv_expression = `-12` io_expected_type = lo_simple_type ).
        GetPTF("-12").aEdmType(EdmSByte.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>single( ).
        // lcl_helper=>veri_type( iv_expression = `1.1F` io_expected_type = lo_simple_type ).
        GetPTF("1.1F").aEdmType(EdmSingle.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>string( ).
        // lcl_helper=>veri_type( iv_expression = `'TEST'` io_expected_type = lo_simple_type ).
        GetPTF("'TEST'").aEdmType(EdmString.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>time( ).
        // lcl_helper=>veri_type( iv_expression = `time'P1998Y02M01D'` io_expected_type = lo_simple_type ).
        GetPTF("time'PT19H02M01S'").aEdmType(EdmTime.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>time( ).
        // lcl_helper=>veri_type( iv_expression = `time'P1998Y02M01DT23H14M33S'` io_expected_type =
        // lo_simple_type ).
        GetPTF("time'PT23H14M33S'").aEdmType(EdmTime.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>double( ).
        // lcl_helper=>veri_type( iv_expression = `1.1D add 1` io_expected_type = lo_simple_type ).
        GetPTF("1.1D add 1").aEdmType(EdmDouble.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>double( ).
        // lcl_helper=>veri_type( iv_expression = `1 add 1.1D` io_expected_type = lo_simple_type ).
        GetPTF("1 add 1.1D").aEdmType(EdmDouble.getInstance());

        // "lcl_helper=>veri_type( iv_expression = `null` io_expected_type =
        // /IWCOR/cl_DS_edm_simple_type=>null( ) ).
        GetPTF("null").aEdmType(EdmNull.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>boolean( ).
        // lcl_helper=>veri_type( iv_expression = `time'P1998Y02M01D' eq time'P1998Y02M01D'`
        // io_expected_type =
        // lo_simple_type ).
        GetPTF("time'PT19H02M01S' eq time'PT19H02M01S'").aEdmType(EdmBoolean.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>boolean( ).
        // lcl_helper=>veri_type( iv_expression = `time'P1998Y02M01D' lt time'P1998Y02M01D'`
        // io_expected_type =
        // lo_simple_type ).
        GetPTF("time'PT19H02M01S' lt time'PT19H02M01S'").aEdmType(EdmBoolean.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `hour(datetime'2011-07-31T23:30:59')` io_expected_type =
        // lo_simple_type ).
        GetPTF("hour(datetime'2011-07-31T23:30:59')").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `minute(datetime'2011-07-31T23:30:59')` io_expected_type =
        // lo_simple_type
        // ).
        GetPTF("minute(datetime'2011-07-31T23:30:59')").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `second(datetime'2011-07-31T23:30:59')` io_expected_type =
        // lo_simple_type
        // ).
        GetPTF("second(datetime'2011-07-31T23:30:59')").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `hour(time'P1998Y02M01D')` io_expected_type =
        // lo_simple_type ).
        GetPTF("hour(time'PT19H02M01S')").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `minute(time'P1998Y02M01D')` io_expected_type =
        // lo_simple_type ).
        GetPTF("minute(time'PT19H02M01S')").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `second(time'P1998Y02M01D')` io_expected_type =
        // lo_simple_type ).
        GetPTF("second(time'PT19H02M01S')").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `hour(datetimeoffset'2002-10-10T12:00:00-05:00')`
        // io_expected_type =
        // lo_simple_type ).
        GetPTF("hour(datetimeoffset'2002-10-10T12:00:00-05:00')").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `minute(datetimeoffset'2002-10-10T12:00:00-05:00')`
        // io_expected_type =
        // lo_simple_type ).
        GetPTF("minute(datetimeoffset'2002-10-10T12:00:00-05:00')").aEdmType(EdmInt32.getInstance());

        // lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        // lcl_helper=>veri_type( iv_expression = `second(datetimeoffset'2002-10-10T12:00:00-05:00')`
        // io_expected_type =
        // lo_simple_type ).
        GetPTF("second(datetimeoffset'2002-10-10T12:00:00-05:00')").aEdmType(EdmInt32.getInstance());

        GetPTF("replace('aBa','B','CCC')").aEdmType(EdmString.getInstance());
    }

    @Test
    public void abapTestOrderByParser() // copy of ABAP method test_orderby_parser
    {
        // lcl_helper=>veri_orderby( iv_expression = 'a' iv_expected = '{oc({o(a asc)})}' ). "default return
        // lower asc
        GetPTO("a").aSerialized("{oc({o(a, asc)})}");

        // lcl_helper=>veri_orderby( iv_expression = 'a,b' iv_expected = '{oc({o(a asc)},{o(b asc)})}' ).
        GetPTO("a,b").aSerialized("{oc({o(a, asc)},{o(b, asc)})}");

        // lcl_helper=>veri_orderby( iv_expression = 'a,b,c' iv_expected = '{oc({o(a asc)},{o(b asc)},{o(c
        // asc)})}' ).
        GetPTO("a,b,c").aSerialized("{oc({o(a, asc)},{o(b, asc)},{o(c, asc)})}");

        // see comment of class
        // //lcl_helper=>veri_orderby( iv_expression = 'a ASC' iv_expected = '{oc({o(a asc)})}' ).
        // GetPTO("a ASC").aSerialized("{oc({o(a, asc)})}");

        // lcl_helper=>veri_orderby( iv_expression = 'a asc' iv_expected = '{oc({o(a asc)})}' ).
        GetPTO("a asc").aSerialized("{oc({o(a, asc)})}");

        // lcl_helper=>veri_orderby( iv_expression = 'a DESC' iv_expected = '{oc({o(a desc)})}' ).
        // -->GetPTO("a DESC").aSerialized("{oc({o(a, desc)})}");

        // see comment of class (case sensitive)
        // //lcl_helper=>veri_orderby( iv_expression = 'a DESC,b DESC'
        // // iv_expected = '{oc({o(a desc)},{o(b desc)})}' ).
        // GetPTO("a DESC,b DESC").aSerialized("{oc({o(a, desc)},{o(b, desc)})}");

        // see comment of class (case sensitive)
        // //lcl_helper=>veri_orderby( iv_expression = 'a ASC, b DESC'
        // // iv_expected = '{oc({o(a asc)},{o(b desc)})}' ).
        // GetPTO("a ASC, b DESC").aSerialized("{oc({o(a, asc)},{o(b, desc)})}");

        // see comment of class (case sensitive)
        // //lcl_helper=>veri_orderby( iv_expression = '2 mul 6 eq 12 DESC'
        // // iv_expected = '{oc({o({{2 mul 6} eq 12} desc)})}' ).
        // GetPTO("2 mul 6 eq 12 DESC").aSerialized("{oc({o({{2 mul 6} eq 12}, desc)})}");

        // lcl_helper=>veri_orderby( iv_expression = `concat( 'Start_' , starttime ) desc`
        // iv_expected = `{oc({o({concat(Start_,starttime)} desc)})}` ).
        GetPTO("concat(   'Start_'  ,   starttime   ) desc").aSerialized("{oc({o({concat('Start_',starttime)}, desc)})}");

    }

    @Test
    public void abapTestFilterParser() { // copy of ABAP method test_filter_parser
        // lcl_helper=>veri_expression( iv_expression = 'W/X' iv_expected = '{W/X}' ).
        GetPTF("W/X").aSerialized("{W/X}");

        // lcl_helper=>veri_expression( iv_expression = 'W/X eq TEST' iv_expected = '{{W/X} eq TEST}' ).
        GetPTF("W/X eq TEST").aSerialized("{{W/X} eq TEST}");

        // lcl_helper=>veri_expression( iv_expression = 'ABC eq W/X eq TEST' iv_expected = '{{ABC eq {W/X}}
        // eq TEST}' ).
        GetPTF("ABC eq W/X eq TEST").aSerialized("{{ABC eq {W/X}} eq TEST}");

        // lcl_helper=>veri_expression( iv_expression = 'ABC eq W / X eq TEST' iv_expected = '{{ABC eq
        // {W/X}} eq TEST}' ).
        GetPTF("ABC eq W / X eq TEST").aSerialized("{{ABC eq {W/X}} eq TEST}");

        // lcl_helper=>veri_expression( iv_expression = 'W/X/Y/Z' iv_expected = '{{{W/X}/Y}/Z}' ).
        GetPTF("W/X/Y/Z").aSerialized("{{{W/X}/Y}/Z}");

        // lcl_helper=>veri_expression( iv_expression = 'X' iv_expected = 'X' ).
        GetPTF("X").aSerialized("X");

        // lcl_helper=>veri_expression( iv_expression = '-X' iv_expected = '{- X}' ).
        GetPTF("-X").aSerialized("{- X}");

        // lcl_helper=>veri_expression( iv_expression = 'not X' iv_expected = '{not X}' ).
        GetPTF("not X").aSerialized("{not X}");

        // lcl_helper=>veri_expression( iv_expression = 'X mul Y' iv_expected = '{X mul Y}' ).
        GetPTF("X mul Y").aSerialized("{X mul Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X div Y' iv_expected = '{X div Y}' ).
        GetPTF("X div Y").aSerialized("{X div Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X mod Y' iv_expected = '{X mod Y}' ).
        GetPTF("X mod Y").aSerialized("{X mod Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X add Y' iv_expected = '{X add Y}' ).
        GetPTF("X add Y").aSerialized("{X add Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X sub Y' iv_expected = '{X sub Y}' ).
        GetPTF("X sub Y").aSerialized("{X sub Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X lt Y' iv_expected = '{X lt Y}' ).
        GetPTF("X lt Y").aSerialized("{X lt Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X gt Y' iv_expected = '{X gt Y}' ).
        GetPTF("X gt Y").aSerialized("{X gt Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X le Y' iv_expected = '{X le Y}' ).
        GetPTF("X le Y").aSerialized("{X le Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X ge Y' iv_expected = '{X ge Y}' ).
        GetPTF("X ge Y").aSerialized("{X ge Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X eq Y' iv_expected = '{X eq Y}' ).
        GetPTF("X eq Y").aSerialized("{X eq Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X ne Y' iv_expected = '{X ne Y}' ).
        GetPTF("X ne Y").aSerialized("{X ne Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X and Y' iv_expected = '{X and Y}' ).
        GetPTF("X and Y").aSerialized("{X and Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X or Y' iv_expected = '{X or Y}' ).
        GetPTF("X or Y").aSerialized("{X or Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X mul Y eq Z' iv_expected = '{{X mul Y} eq Z}' ).
        GetPTF("X mul Y eq Z").aSerialized("{{X mul Y} eq Z}");

        // lcl_helper=>veri_expression( iv_expression = 'X eq Y mul Z' iv_expected = '{X eq {Y mul Z}}' ).
        GetPTF("X eq Y mul Z").aSerialized("{X eq {Y mul Z}}");

        // lcl_helper=>veri_expression( iv_expression = '(X)' iv_expected = 'X' ).
        GetPTF("(X)").aSerialized("X");

        // lcl_helper=>veri_expression( iv_expression = '(X or Y)' iv_expected = '{X or Y}' ).
        GetPTF("(X or Y)").aSerialized("{X or Y}");

        // lcl_helper=>veri_expression( iv_expression = 'X mul (Y eq Z)' iv_expected = '{X mul {Y eq Z}}' ).
        GetPTF("X mul (Y eq Z)").aSerialized("{X mul {Y eq Z}}");

        // lcl_helper=>veri_expression( iv_expression = '(X eq Y) mul Z' iv_expected = '{{X eq Y} mul Z}' ).
        GetPTF("(X eq Y) mul Z").aSerialized("{{X eq Y} mul Z}");

        // lcl_helper=>veri_expression( iv_expression = 'indexof(X,Y)' iv_expected = '{indexof(X,Y)}' ).
        GetPTF("indexof(X,Y)").aSerialized("{indexof(X,Y)}");

        // lcl_helper=>veri_expression( iv_expression = 'concat(X, Y)' iv_expected = '{concat(X,Y)}' ).
        GetPTF("concat(X, Y)").aSerialized("{concat(X,Y)}");

        // lcl_helper=>veri_expression( iv_expression = 'concat(X,Y,Z, 1,2,3)' iv_expected =
        // '{concat(X,Y,Z,1,2,3)}' ).
        GetPTF("concat(X,Y,Z, 1,2,3)").aSerialized("{concat(X,Y,Z,1,2,3)}");

        // lcl_helper=>veri_expression( iv_expression = 'a eq b eq c eq d' iv_expected = '{{{a eq b} eq c}
        // eq d}' ).
        GetPTF("a eq b eq c eq d").aSerialized("{{{a eq b} eq c} eq d}");

        // lcl_helper=>veri_expression( iv_expression = 'a mul b eq c mul d' iv_expected = '{{a mul b} eq {c
        // mul d}}' ).
        GetPTF("a mul b eq c mul d").aSerialized("{{a mul b} eq {c mul d}}");

        // lcl_helper=>veri_expression( iv_expression = 'a mul concat(X,Y,Z) eq c mul d' iv_expected = '{{a
        // mul
        // {concat(X,Y,Z)}} eq {c mul d}}' ).
        GetPTF("a mul concat(X,Y,Z) eq c mul d").aSerialized("{{a mul {concat(X,Y,Z)}} eq {c mul d}}");

        // lcl_helper=>veri_expression( iv_expression = 'a mul (concat(X,Y,Z) eq c) mul d' iv_expected =
        // '{{a mul
        // {{concat(X,Y,Z)} eq c}} mul d}' ).
        GetPTF("a mul (concat(X,Y,Z) eq c) mul d").aSerialized("{{a mul {{concat(X,Y,Z)} eq c}} mul d}");

        // lcl_helper=>veri_expression( iv_expression = '- not X' iv_expected = '{- {not X}}' ).
        GetPTF("- not X").aSerialized("{- {not X}}");

        // =>veri_expression( iv_expression = 'concat(-X,Y)' iv_expected = '{concat({- X},Y)}' ).
        GetPTF("concat(-X,Y)").aSerialized("{concat({- X},Y)}");

        // lcl_helper=>veri_expression( iv_expression = 'concat(not X,Y)' iv_expected = '{concat({not
        // X},Y)}' ).
        GetPTF("concat(not X,Y)").aSerialized("{concat({not X},Y)}");

        // lcl_helper=>veri_expression( iv_expression = 'concat(- not X,Y)' iv_expected = '{concat({- {not
        // X}},Y)}' ).
        GetPTF("concat(- not X,Y)").aSerialized("{concat({- {not X}},Y)}");

        // lcl_helper=>veri_expression( iv_expression = 'not concat(-X,Y)' iv_expected = '{not {concat({-
        // X},Y)}}' ).
        GetPTF("not concat(-X,Y)").aSerialized("{not {concat({- X},Y)}}");

        // lcl_helper=>veri_expression( iv_expression = 'a eq not concat(-X,Y)' iv_expected = '{a eq {not
        // {concat({-
        // X},Y)}}}' ).
        GetPTF("a eq not concat(-X,Y)").aSerialized("{a eq {not {concat({- X},Y)}}}");

        // lcl_helper=>veri_expression( iv_expression = 'a eq b or c eq d and e eq f' iv_expected = '{{a eq
        // b} or {{c eq d}
        // and {e eq f}}}' ).
        GetPTF("a eq b or c eq d and e eq f").aSerialized("{{a eq b} or {{c eq d} and {e eq f}}}");

        // lcl_helper=>veri_expression( iv_expression = 'a eq b and c eq d or e eq f' iv_expected = '{{{a eq
        // b} and {c eq
        // d}} or {e eq f}}' ).
        GetPTF("a eq b and c eq d or e eq f").aSerialized("{{{a eq b} and {c eq d}} or {e eq f}}");

        // lcl_helper=>veri_expression( iv_expression = 'a eq 1.1E+02D' iv_expected = '{a eq 1.1E+02}' ).
        GetPTF("a eq 1.1E+02D").aSerialized("{a eq 1.1E+02D}");

        // lcl_helper=>veri_expression_ex(
        // iv_expression = `concat('a' 'b')`
        // iv_expected_textid = /iwcor/cx_ds_expr_syntax_error=>function_invalid_parameter
        // iv_expected_msg = 'Invalid parameter for function ''concat''' ).
        GetPTF("concat('a' 'b')").aExMsgText("\")\" or \",\" expected after position 10 in \"concat('a' 'b')\".");

        // lcl_helper=>veri_expression_ex(
        // iv_expression = `concat('125')`
        // iv_expected_textid = /iwcor/cx_ds_expr_syntax_error=>function_to_few_parameter
        // iv_expected_msg = 'Too few parameters for function ''concat''' ).
        GetPTF("concat('125')").aExMsgText("No applicable method found for \"concat\" at position 1 in \"concat('125')\" with the "
                + "specified arguments. Method \"concat\" requires 2 or more arguments.");

        // lcl_helper=>veri_expression_ex(
        // iv_expression = `indexof('a','b','c')`
        // iv_expected_textid = /iwcor/cx_ds_expr_syntax_error=>function_to_many_parameter
        // iv_expected_msg = 'Too many parameters for function ''indexof''' ).
        GetPTF("indexof('a','b','c')").aExMsgText(
                "No applicable method found for \"indexof\" at position 1 in \"indexof('a','b','c')\" with "
                        + "the specified arguments. Method \"indexof\" requires exact 2 argument(s).");

        // lcl_helper=>veri_expression_ex(
        // iv_expression = `replace('aBa','B','CCC')`
        // iv_expected_textid = /iwcor/cx_ds_expr_syntax_error=>function_invalid
        // iv_expected_msg = `Invalid function 'replace' detected` ).
        // -->see test method abapMethodRleplaceNotAllowed()

        GetPTF("replace('a','b')").aExMsgText("No applicable method found for \"replace\" at position 1 in \"replace('a','b')\" "
                + "with the specified arguments. Method \"replace\" requires exact 3 argument(s).");

        GetPTF("replace('a',1,2)").aExMsgText(
                "No applicable method found for \"replace\" at position 1 in " + "\"replace('a',1,2)\" for the specified argument types.");
    }

}
