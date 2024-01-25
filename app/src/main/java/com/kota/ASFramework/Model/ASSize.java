package com.kota.ASFramework.Model;

public class ASSize {
  public int height = 0;
  
  public int width = 0;
  
  public ASSize() {}
  
  public ASSize(int paramInt1, int paramInt2) {
    this.width = paramInt1;
    this.height = paramInt2;
  }
  
  public boolean isZero() {
    return (this.width == 0 && this.height == 0);
  }
  
  public void set(int paramInt1, int paramInt2) {
    this.width = paramInt1;
    this.height = paramInt2;
  }
  
  public void set(ASSize paramASSize) {
    set(paramASSize.width, paramASSize.height);
  }
  
  public String toString() {
    return "(" + this.width + "," + this.height + ")";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Model\ASSize.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */