package com.kumi.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kumi.Bahamut.Pages.Model.ClassPageItem;

public class ClassPageItemView extends LinearLayout {
  private static int _count = 0;
  
  private TextView _board_manager_label = null;
  
  private TextView _board_name_label = null;
  
  private TextView _board_title_label = null;
  
  private View _divider_bottom = null;
  
  public ClassPageItemView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public ClassPageItemView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361834, (ViewGroup)this);
    this._board_title_label = (TextView)findViewById(2131230849);
    this._board_name_label = (TextView)findViewById(2131230848);
    this._board_manager_label = (TextView)findViewById(2131230847);
    this._divider_bottom = findViewById(2131230850);
  }
  
  public void clear() {
    setBoardTitleText((String)null);
    setBoardNameText((String)null);
    setBoardManagerText((String)null);
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public void setBoardManagerText(String paramString) {
    if (this._board_manager_label != null) {
      if (paramString != null) {
        this._board_manager_label.setText(paramString);
        return;
      } 
    } else {
      return;
    } 
    this._board_manager_label.setText("讀取中");
  }
  
  public void setBoardNameText(String paramString) {
    if (this._board_name_label != null) {
      if (paramString != null) {
        this._board_name_label.setText(paramString);
        return;
      } 
    } else {
      return;
    } 
    this._board_name_label.setText("讀取中");
  }
  
  public void setBoardTitleText(String paramString) {
    if (this._board_title_label != null) {
      if (paramString != null) {
        this._board_title_label.setText(paramString);
        return;
      } 
    } else {
      return;
    } 
    this._board_title_label.setText("讀取中...");
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
  
  public void setItem(ClassPageItem paramClassPageItem) {
    if (paramClassPageItem != null) {
      setBoardTitleText(paramClassPageItem.Title);
      setBoardNameText(paramClassPageItem.Name);
      setBoardManagerText(paramClassPageItem.Manager);
      return;
    } 
    clear();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\ClassPageItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */