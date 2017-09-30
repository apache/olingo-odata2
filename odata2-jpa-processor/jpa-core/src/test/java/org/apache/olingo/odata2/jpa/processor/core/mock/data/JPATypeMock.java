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
package org.apache.olingo.odata2.jpa.processor.core.mock.data;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/* ========================================================================= */
public class JPATypeMock {

  enum JPATypeMockEnum {VALUE, MORE_VALUE}

  public static final String ENTITY_NAME = "JPATypeMock";
  public static final String PROPERTY_NAME_MINT = "mInt";
  public static final String PROPERTY_NAME_MSTRING = "mString";
  public static final String PROPERTY_NAME_MDATETIME = "mDateTime";
  public static final String PROPERTY_NAME_MBLOB = "mBlob";
  public static final String PROPERTY_NAME_CLOB = "mClob";
  public static final String PROPERTY_NAME_MCHAR = "mChar";
  public static final String PROPERTY_NAME_MCHARARRAY = "mCharArray";
  public static final String PROPERTY_NAME_MC = "mC";
  public static final String PROPERTY_NAME_MCARRAY = "mCArray";
  public static final String PROPERTY_NAME_MKEY = "key";
  public static final String PROPERTY_NAME_MCOMPLEXTYPE = "complexType";
  public static final String PROPERTY_NAME_ENUM = "mSomeEnum";
  public static final String PROPERTY_NAME_XMLADAPTER = "propertyWithXmlAdapter";

  public static final String NAVIGATION_PROPERTY_X = "mRelatedEntity";
  public static final String NAVIGATION_PROPERTY_XS = "mRelatedEntities";

  private JPATypeEmbeddableMock key;
  private JPATypeEmbeddableMock complexType;
  private int mInt;
  private String mString;
  private Calendar mDateTime;
  private Blob mBlob;
  private Clob mClob;
  private char mC;
  private char[] mCArray;
  private Character mChar;
  private Character[] mCharArray;
  private JPATypeMockEnum mSomeEnum;
  
  private EntityWithXmlAdapterOnProperty propertyWithXmlAdapter;

  public Clob getMClob() {
    return mClob;
  }

  public void setMClob(final Clob mClob) {
    this.mClob = mClob;
  }

  public char getMC() {
    return mC;
  }

  public void setMC(final char mC) {
    this.mC = mC;
  }

  public char[] getMCArray() {
    return mCArray;
  }

  public void setMCArray(final char[] mCArray) {
    this.mCArray = mCArray;
  }

  public Character getMChar() {
    return mChar;
  }

  public void setMChar(final Character mChar) {
    this.mChar = mChar;
  }

  public Character[] getMCharArray() {
    return mCharArray;
  }

  public void setMCharArray(final Character[] mCharArray) {
    this.mCharArray = mCharArray;
  }
  
  private JPARelatedTypeMock mRelatedEntity;
  private List<JPARelatedTypeMock> mRelatedEntities = new ArrayList<JPATypeMock.JPARelatedTypeMock>();

  public String getMString() {
    return mString;
  }

  public void setMString(final String mString) {
    this.mString = mString;
  }

  public JPATypeEmbeddableMock getKey() {
    return key;
  }

  public void setKey(final JPATypeEmbeddableMock key) {
    this.key = key;
  }

  public int getMInt() {
    return mInt;
  }

  public void setMInt(final int mInt) {
    this.mInt = mInt;
  }

  public Calendar getMDateTime() {
    return mDateTime;
  }

  public void setMDateTime(final Calendar mDateTime) {
    this.mDateTime = mDateTime;
  }

  public JPARelatedTypeMock getMRelatedEntity() {
    return mRelatedEntity;
  }

  public void setMRelatedEntity(final JPARelatedTypeMock mRelatedEntity) {
    this.mRelatedEntity = mRelatedEntity;
  }

  public List<JPARelatedTypeMock> getMRelatedEntities() {
    return mRelatedEntities;
  }

  public void setMRelatedEntities(final List<JPARelatedTypeMock> mRelatedEntities) {
    this.mRelatedEntities = mRelatedEntities;
  }

  public JPATypeEmbeddableMock getComplexType() {
    return complexType;
  }

  public void setComplexType(final JPATypeEmbeddableMock complexType) {
    this.complexType = complexType;
  }

  public Blob getMBlob() {
    return mBlob;
  }

  public void setMBlob(final Blob mBlob) {
    this.mBlob = mBlob;
  }

  public JPATypeMockEnum getMSomeEnum() {
    return mSomeEnum;
  }

  public void setMSomeEnum(JPATypeMockEnum mSomeEnum) {
    this.mSomeEnum = mSomeEnum;
  }
  
  @XmlJavaTypeAdapter(XmlAdapter.class)
  public EntityWithXmlAdapterOnProperty getPropertyWithXmlAdapter() {
    return propertyWithXmlAdapter;
  }

  public void setPropertyWithXmlAdapter(EntityWithXmlAdapterOnProperty propertyWithXmlAdapter) {
    this.propertyWithXmlAdapter = propertyWithXmlAdapter;
  }

  /* ========================================================================= */
  public static class JPATypeEmbeddableMock {

    public static final String ENTITY_NAME = "JPATypeEmbeddableMock";
    public static final String PROPERTY_NAME_MSHORT = "mShort";
    public static final String PROPERTY_NAME_MEMBEDDABLE = "mEmbeddable";
    public static final String PROPERTY_NAME_MDATE = "mDate";
    public static final String PROPERTY_NAME_MDATE1 = "mDate1";
    public static final String PROPERTY_NAME_MTIMESTAMP = "mTimestamp";
    public static final String PROPERTY_NAME_MTIME = "mTime";

    private Short mShort;
    private JPATypeEmbeddableMock2 mEmbeddable;
    private Date mDate;
    private java.sql.Date mDate1;
    private Timestamp mTimestamp;
    private Time mTime;

    public Short getMShort() {
      return mShort;
    }

    public void setMShort(final Short mShort) {
      this.mShort = mShort;
    }

    public JPATypeEmbeddableMock2 getMEmbeddable() {
      return mEmbeddable;
    }

    public void setMEmbeddable(final JPATypeEmbeddableMock2 mEmbeddable) {
      this.mEmbeddable = mEmbeddable;
    }
    
    public Timestamp getMTimestamp() {
      return mTimestamp;
    }

    public void setMTimestamp(final Timestamp mTimestamp) {
      this.mTimestamp = mTimestamp;
    }

    public Time getMTime() {
      return mTime;
    }
    
    public void setMTime(final Time mTime) {
      this.mTime = mTime;
    }

    public void setMDate(final Date mDate) {
      this.mDate = mDate;
    }
    
    public Date getMDate() {
      return mDate;
    }

    public void setMDate1(final java.sql.Date mDate1) {
      this.mDate1 = mDate1;
    }
    
    public java.sql.Date getMDate1() {
      return mDate1;
    }

  }

  /* ========================================================================= */
  public static class JPATypeEmbeddableMock2 {

    public static final String ENTITY_NAME = "JPATypeEmbeddableMock2";
    public static final String PROPERTY_NAME_MUUID = "mUUID";
    public static final String PROPERTY_NAME_MFLOAT = "mFloat";

    private UUID mUUID;
    private Float mFloat;

    public UUID getMUUID() {
      return mUUID;
    }

    public void setMUUID(final UUID mUUID) {
      this.mUUID = mUUID;
    }

    public Float getMFloat() {
      return mFloat;
    }

    public void setMFloat(final Float mFloat) {
      this.mFloat = mFloat;
    }

  }

  /* ========================================================================= */
  public static final class JPARelatedTypeMock {
    public static final String ENTITY_NAME = "JPARelatedTypeMock";
    public static final String PROPERTY_NAME_MLONG = "mLong";
    public static final String PROPERTY_NAME_MDOUBLE = "mDouble";
    public static final String PROPERTY_NAME_MBYTE = "mByte";
    public static final String PROPERTY_NAME_MBYTEARRAY = "mByteArray";

    private long mLong;
    private double mDouble;
    private byte mByte;
    private byte mByteArray[];

    public long getMLong() {
      return mLong;
    }

    public void setMLong(final long key) {
      mLong = key;
    }

    public double getMDouble() {
      return mDouble;
    }

    public void setMDouble(final double mDouble) {
      this.mDouble = mDouble;
    }

    public byte getMByte() {
      return mByte;
    }

    public void setMByte(final byte mByte) {
      this.mByte = mByte;
    }

    public byte[] getMByteArray() {
      return mByteArray;
    }

    public void setMByteArray(final byte mByteArray[]) {
      this.mByteArray = mByteArray;
    }

  }
}
