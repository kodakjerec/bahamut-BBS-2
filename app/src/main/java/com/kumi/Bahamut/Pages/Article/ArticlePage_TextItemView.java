package com.kumi.Bahamut.Pages.Article;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kumi.Telnet.TelnetArticleItemView;
import com.kumi.TelnetUI.DividerView;

public class ArticlePage_TextItemView extends LinearLayout implements TelnetArticleItemView {
  TextView _author_label = null;
  
  TextView _content_label = null;
  
  ViewGroup _content_view = null;
  
  DividerView _divider_view = null;
  
  int _quote = 0;
  
  public ArticlePage_TextItemView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public ArticlePage_TextItemView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  private void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361822, (ViewGroup)this);
    this._author_label = (TextView)findViewById(2131230733);
    this._content_label = (TextView)findViewById(2131230730);
    this._divider_view = (DividerView)findViewById(2131230732);
    this._content_view = (ViewGroup)findViewById(2131230731);
    setBackgroundDrawable(null);
  }
  
  public void draw(Canvas paramCanvas) {
    super.draw(paramCanvas);
  }
  
  public int getType() {
    return 0;
  }
  
  public void setAuthor(String paramString1, String paramString2) {
    if (this._author_label != null) {
      StringBuffer stringBuffer = new StringBuffer();
      if (paramString1 != null)
        stringBuffer.append(paramString1); 
      if (paramString2 != null && paramString2.length() > 0)
        stringBuffer.append("(" + paramString2 + ")"); 
      stringBuffer.append(" 說:");
      this._author_label.setText(stringBuffer.toString());
    } 
  }
  
  public void setContent(String paramString) {
    if (this._content_label != null)
      this._content_label.setText(paramString); 
  }
  
  public void setDividerhidden(boolean paramBoolean) {
    if (paramBoolean) {
      this._divider_view.setVisibility(8);
      return;
    } 
    this._divider_view.setVisibility(0);
  }
  
  public void setQuote(int paramInt) {
    this._quote = paramInt;
    if (paramInt > 0) {
      this._author_label.setTextColor(-8323200);
      this._content_label.setTextColor(-14614752);
      return;
    } 
    this._author_label.setTextColor(-1);
    this._content_label.setTextColor(-4144960);
  }
  
  public void setVisible(boolean paramBoolean) {
    if (paramBoolean) {
      this._content_view.setVisibility(0);
      return;
    } 
    this._content_view.setVisibility(8);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Article\ArticlePage_TextItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */