package com.kumi.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kumi.Bahamut.DataModels.Bookmark;

public class BoardExtendOptionalPageHistoryItemView extends LinearLayout {
  private View _divider_bottom = null;
  
  private View _divider_top = null;
  
  private TextView _title_label = null;
  
  public BoardExtendOptionalPageHistoryItemView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public BoardExtendOptionalPageHistoryItemView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361828, (ViewGroup)this);
    this._title_label = (TextView)findViewById(2131230813);
    this._divider_top = findViewById(2131230811);
    this._divider_bottom = findViewById(2131230810);
  }
  
  public void clear() {
    setTitle((String)null);
  }
  
  public void setBookmark(Bookmark paramBookmark) {
    if (paramBookmark != null) {
      setTitle(paramBookmark.getKeyword());
      return;
    } 
    clear();
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
  
  public void setTitle(String paramString) {
    if (this._title_label != null) {
      if (paramString != null && paramString.length() > 0) {
        this._title_label.setText(paramString);
        return;
      } 
      this._title_label.setText("未輸入");
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\BoardExtendOptionalPageHistoryItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */