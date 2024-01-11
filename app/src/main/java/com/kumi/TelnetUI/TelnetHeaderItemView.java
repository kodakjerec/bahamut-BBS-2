package com.kumi.TelnetUI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TelnetHeaderItemView extends LinearLayout {
  private TextView _detail_1 = null;
  
  private TextView _detail_2 = null;
  
  private TextView _title = null;
  
  private ImageButton mMenuButton;
  
  private View mMenuDivider;
  
  public TelnetHeaderItemView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public TelnetHeaderItemView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361842, (ViewGroup)this);
    this._title = (TextView)findViewById(2131231070);
    this._detail_1 = (TextView)findViewById(2131230979);
    this._detail_2 = (TextView)findViewById(2131230980);
    this.mMenuDivider = findViewById(2131231005);
    this.mMenuButton = (ImageButton)findViewById(2131231004);
  }
  
  public void setData(String paramString1, String paramString2, String paramString3) {
    setTitle(paramString1);
    setDetail1(paramString2);
    setDetail2(paramString3);
  }
  
  public void setDetail1(String paramString) {
    if (this._detail_1 != null)
      this._detail_1.setText(paramString); 
  }
  
  public void setDetail2(String paramString) {
    if (this._detail_2 != null)
      this._detail_2.setText(paramString); 
  }
  
  public void setMenuButton(View.OnClickListener paramOnClickListener) {
    if (paramOnClickListener == null) {
      this.mMenuDivider.setVisibility(8);
      this.mMenuButton.setVisibility(8);
      this.mMenuButton.setOnClickListener(null);
      return;
    } 
    this.mMenuDivider.setVisibility(0);
    this.mMenuButton.setVisibility(0);
    this.mMenuButton.setOnClickListener(paramOnClickListener);
  }
  
  public void setTitle(String paramString) {
    if (this._title != null)
      this._title.setText(paramString); 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TelnetUI\TelnetHeaderItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */