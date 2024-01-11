package com.kumi.Bahamut.Pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kumi.Bahamut.Pages.Model.BoardPageItem;

public class BoardPageItemView extends LinearLayout {
  private static int _count = 0;
  
  private TextView _author_label = null;
  
  private ViewGroup _content_view = null;
  
  private TextView _date_label = null;
  
  private View _divider_bottom = null;
  
  private TextView _gy_label = null;
  
  private TextView _gy_title_label = null;
  
  private TextView _mark_label = null;
  
  private TextView _number_label = null;
  
  private TextView _status_label = null;
  
  private TextView _title_label = null;
  
  public BoardPageItemView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public BoardPageItemView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361831, (ViewGroup)this);
    this._status_label = (TextView)findViewById(2131230831);
    this._title_label = (TextView)findViewById(2131230832);
    this._number_label = (TextView)findViewById(2131230830);
    this._date_label = (TextView)findViewById(2131230823);
    this._gy_title_label = (TextView)findViewById(2131230826);
    this._gy_label = (TextView)findViewById(2131230825);
    this._mark_label = (TextView)findViewById(2131230827);
    this._author_label = (TextView)findViewById(2131230820);
    this._content_view = (ViewGroup)findViewById(2131230822);
    this._divider_bottom = findViewById(2131230824);
  }
  
  public void clear() {
    setTitle((String)null);
    setDate((String)null);
    setAuthor((String)null);
    setNumber(0);
    setGYNumber(0);
    setRead(true);
    setReply(false);
    setMark(false);
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public void setAuthor(String paramString) {
    if (this._author_label != null) {
      if (paramString != null) {
        this._author_label.setText(paramString);
        return;
      } 
      this._author_label.setText("讀取中");
    } 
  }
  
  public void setDate(String paramString) {
    if (this._date_label != null) {
      if (paramString != null) {
        this._date_label.setText(paramString);
        return;
      } 
      this._date_label.setText("讀取中");
    } 
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
  
  public void setGYNumber(int paramInt) {
    if (this._gy_label != null) {
      if (paramInt == 0) {
        this._gy_title_label.setVisibility(8);
        this._gy_label.setVisibility(8);
        return;
      } 
    } else {
      return;
    } 
    this._gy_title_label.setVisibility(0);
    this._gy_label.setVisibility(0);
    this._gy_label.setText(String.valueOf(paramInt));
  }
  
  public void setItem(BoardPageItem paramBoardPageItem) {
    if (paramBoardPageItem != null) {
      boolean bool;
      setTitle(paramBoardPageItem.Title);
      setNumber(paramBoardPageItem.Number);
      setDate(paramBoardPageItem.Date);
      setAuthor(paramBoardPageItem.Author);
      setMark(paramBoardPageItem.isMarked);
      setGYNumber(paramBoardPageItem.GY);
      setReply(paramBoardPageItem.isReply);
      if (paramBoardPageItem.isDeleted || paramBoardPageItem.isRead) {
        bool = true;
      } else {
        bool = false;
      } 
      setRead(bool);
      return;
    } 
    clear();
  }
  
  public void setMark(boolean paramBoolean) {
    if (paramBoolean) {
      this._mark_label.setVisibility(0);
      return;
    } 
    this._mark_label.setVisibility(4);
  }
  
  @SuppressLint({"DefaultLocale"})
  public void setNumber(int paramInt) {
    if (this._number_label != null) {
      if (paramInt > 0) {
        String str = String.format("%1$05d", new Object[] { Integer.valueOf(paramInt) });
        this._number_label.setText(str);
        return;
      } 
      this._number_label.setText("讀取中");
    } 
  }
  
  public void setRead(boolean paramBoolean) {
    if (paramBoolean) {
      this._title_label.setTextColor(-8355712);
      return;
    } 
    this._title_label.setTextColor(-1);
  }
  
  public void setReply(boolean paramBoolean) {
    if (this._status_label != null) {
      if (paramBoolean) {
        this._status_label.setText("Re");
        return;
      } 
    } else {
      return;
    } 
    this._status_label.setText("◆");
  }
  
  public void setTitle(String paramString) {
    if (this._title_label != null) {
      if (paramString != null) {
        this._title_label.setText(paramString);
        return;
      } 
      this._title_label.setText("讀取中...");
    } 
  }
  
  public void setVisible(boolean paramBoolean) {
    if (paramBoolean) {
      if (this._content_view.getVisibility() != 0)
        this._content_view.setVisibility(0); 
      return;
    } 
    if (this._content_view.getVisibility() != 8)
      this._content_view.setVisibility(8); 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\BoardPageItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */