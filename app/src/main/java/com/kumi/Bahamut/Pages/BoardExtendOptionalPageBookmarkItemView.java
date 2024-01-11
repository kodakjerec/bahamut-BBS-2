package com.kumi.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kumi.Bahamut.DataModels.Bookmark;

public class BoardExtendOptionalPageBookmarkItemView extends LinearLayout {
  private TextView _author_label = null;
  
  private View _divider_bottom = null;
  
  private View _divider_top = null;
  
  private TextView _gy_label = null;
  
  private TextView _mark_label = null;
  
  private TextView _title_label = null;
  
  public BoardExtendOptionalPageBookmarkItemView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public BoardExtendOptionalPageBookmarkItemView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361827, (ViewGroup)this);
    this._title_label = (TextView)findViewById(2131230804);
    this._author_label = (TextView)findViewById(2131230794);
    this._mark_label = (TextView)findViewById(2131230802);
    this._gy_label = (TextView)findViewById(2131230800);
    this._divider_top = findViewById(2131230799);
    this._divider_bottom = findViewById(2131230798);
  }
  
  public void clear() {
    setTitle((String)null);
    setAuthor((String)null);
    setGYNumber((String)null);
    setMark(false);
  }
  
  public void setAuthor(String paramString) {
    if (this._author_label != null) {
      if (paramString != null && paramString.length() > 0) {
        this._author_label.setText(paramString);
        return;
      } 
      this._author_label.setText("未輸入");
    } 
  }
  
  public void setBookmark(Bookmark paramBookmark) {
    if (paramBookmark != null) {
      boolean bool;
      setTitle(paramBookmark.getKeyword());
      setAuthor(paramBookmark.getAuthor());
      if (paramBookmark.getMark() == "m") {
        bool = true;
      } else {
        bool = false;
      } 
      setMark(bool);
      setGYNumber(paramBookmark.getGy());
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
  
  public void setGYNumber(String paramString) {
    if (this._gy_label != null) {
      if (paramString != null && paramString.length() > 0) {
        this._gy_label.setText(paramString);
        return;
      } 
      this._gy_label.setText("0");
    } 
  }
  
  public void setMark(boolean paramBoolean) {
    if (paramBoolean) {
      this._mark_label.setVisibility(0);
      return;
    } 
    this._mark_label.setVisibility(4);
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


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\BoardExtendOptionalPageBookmarkItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */