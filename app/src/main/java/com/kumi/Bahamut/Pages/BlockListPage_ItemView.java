package com.kumi.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BlockListPage_ItemView extends LinearLayout implements View.OnClickListener {
  private Button _delete_button = null;
  
  private View _divider_bottom = null;
  
  private View _divider_top = null;
  
  private TextView _name_label = null;
  
  public int index = 0;
  
  public BlockListPage_ItemView_Listener listener = null;
  
  public BlockListPage_ItemView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public BlockListPage_ItemView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361824, (ViewGroup)this);
    this._name_label = (TextView)findViewById(2131230786);
    this._delete_button = (Button)findViewById(2131230785);
    this._delete_button.setOnClickListener(this);
    this._divider_top = findViewById(2131230788);
    this._divider_bottom = findViewById(2131230787);
  }
  
  public void onClick(View paramView) {
    if (this.listener != null)
      this.listener.onBlockListPage_ItemView_Clicked(this); 
  }
  
  public void setDividerBottomVisible(boolean paramBoolean) {
    if (this._divider_bottom != null) {
      if (paramBoolean) {
        if (this._divider_bottom.getVisibility() != 0)
          this._divider_bottom.setVisibility(0); 
        return;
      } 
    } else {
      return;
    } 
    if (this._divider_bottom.getVisibility() != 8)
      this._divider_bottom.setVisibility(8); 
  }
  
  public void setDividerTopVisible(boolean paramBoolean) {
    if (this._divider_top != null) {
      if (paramBoolean) {
        if (this._divider_top.getVisibility() != 0)
          this._divider_top.setVisibility(0); 
        return;
      } 
    } else {
      return;
    } 
    if (this._divider_top.getVisibility() != 8)
      this._divider_top.setVisibility(8); 
  }
  
  public void setName(String paramString) {
    if (this._name_label != null && this._name_label != null)
      this._name_label.setText(paramString); 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\BlockListPage_ItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */