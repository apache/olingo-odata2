package org.apache.olingo.odata2.ref.processor;


public class Util {

private static final Util instance = new Util();
  
  private byte[] binaryContent = null;
  
  public static Util getInstance() {
    return instance;
  }
  /**
   * @return the binaryContent
   */
  public byte[] getBinaryContent() {
    return binaryContent;
  }

  /**
   * @param binaryContent the binaryContent to set
   */
  public void setBinaryContent(byte[] binaryContent) {
    this.binaryContent = binaryContent;
  }
}