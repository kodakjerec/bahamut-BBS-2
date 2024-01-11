package com.kumi.Bahamut.Pages.Article;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kumi.Telnet.TelnetArticleItemView;

public class ArticlePage_TimeTimeView extends RelativeLayout implements TelnetArticleItemView {
  TextView _time_label = null;
  
  public ArticlePage_TimeTimeView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public ArticlePage_TimeTimeView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  public ArticlePage_TimeTimeView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
    init();
  }
  
  public int getType() {
    return 3;
  }
  
  public void init() {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361823, (ViewGroup)this);
    this._time_label = (TextView)findViewById(2131230734);
  }
  
  public void setTime(String paramString) {
    if (this._time_label != null)
      this._time_label.setText(paramString); 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Article\ArticlePage_TimeTimeView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */