package com.kumi.Bahamut.Pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kumi.Bahamut.Pages.Model.MailBoxPageItem;

public class MailBoxPage_ItemView extends LinearLayout {
  private static int _count = 0;
  
  private TextView _author = null;
  
  private TextView _date = null;
  
  private View _divider_bottom = null;
  
  private TextView _mark = null;
  
  private TextView _number = null;
  
  private TextView _reply = null;
  
  private TextView _status = null;
  
  private TextView _title = null;
  
  public MailBoxPage_ItemView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public MailBoxPage_ItemView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361845, (ViewGroup)this);
    this._status = (TextView)findViewById(2131230875);
    this._title = (TextView)findViewById(2131230876);
    this._number = (TextView)findViewById(2131230873);
    this._date = (TextView)findViewById(2131230870);
    this._mark = (TextView)findViewById(2131230872);
    this._author = (TextView)findViewById(2131230869);
    this._reply = (TextView)findViewById(2131230874);
    this._divider_bottom = findViewById(2131230871);
  }
  
  public void clear() {
    setTitle((String)null);
    setDate((String)null);
    setAuthor((String)null);
    setIndex(0);
    setRead(true);
    setReply(false);
    setMark(false);
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public void setAuthor(String paramString) {
    if (this._author != null) {
      if (paramString != null) {
        this._author.setText(paramString);
        return;
      } 
      this._author.setText("讀取中");
    } 
  }
  
  public void setDate(String paramString) {
    if (this._date != null) {
      if (paramString != null) {
        this._date.setText(paramString);
        return;
      } 
      this._date.setText("讀取中");
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
  
  @SuppressLint({"DefaultLocale"})
  public void setIndex(int paramInt) {
    if (this._number != null) {
      if (paramInt > 0) {
        String str = String.format("%1$05d", new Object[] { Integer.valueOf(paramInt) });
        this._number.setText(str);
        return;
      } 
      this._number.setText("讀取中");
    } 
  }
  
  public void setItem(MailBoxPageItem paramMailBoxPageItem) {
    if (paramMailBoxPageItem != null) {
      setTitle(paramMailBoxPageItem.Title);
      setIndex(paramMailBoxPageItem.Number);
      setDate(paramMailBoxPageItem.Date);
      setAuthor(paramMailBoxPageItem.Author);
      setReply(paramMailBoxPageItem.isReply);
      setRead(paramMailBoxPageItem.isRead);
      setMark(paramMailBoxPageItem.isMarked);
      return;
    } 
    clear();
  }
  
  public void setMark(boolean paramBoolean) {
    if (paramBoolean) {
      this._mark.setVisibility(0);
      return;
    } 
    this._mark.setVisibility(4);
  }
  
  public void setRead(boolean paramBoolean) {
    if (paramBoolean) {
      this._status.setText("◇");
      this._title.setTextColor(-8355712);
      return;
    } 
    this._status.setText("◆");
    this._title.setTextColor(-1);
  }
  
  public void setReply(boolean paramBoolean) {
    if (paramBoolean) {
      this._reply.setVisibility(0);
      return;
    } 
    this._reply.setVisibility(4);
  }
  
  public void setTitle(String paramString) {
    if (this._title != null) {
      if (paramString != null) {
        this._title.setText(paramString);
        return;
      } 
      this._title.setText("讀取中...");
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\MailBoxPage_ItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */