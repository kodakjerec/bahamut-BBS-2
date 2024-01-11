package com.kumi.Bahamut.Pages.Article;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.kumi.Telnet.Model.TelnetFrame;
import com.kumi.Telnet.TelnetArticleItemView;
import com.kumi.TelnetUI.DividerView;
import com.kumi.TelnetUI.TelnetView;

public class ArticlePage_TelnetItemView extends LinearLayout implements TelnetArticleItemView {
  DividerView _divider_view = null;
  
  TelnetView _telnet_view = null;
  
  public ArticlePage_TelnetItemView(Context paramContext) {
    super(paramContext);
    init(paramContext);
  }
  
  private void init(Context paramContext) {
    ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(2131361821, (ViewGroup)this);
    this._telnet_view = (TelnetView)findViewById(2131230722);
    this._divider_view = (DividerView)findViewById(2131230721);
    setBackgroundDrawable(null);
  }
  
  public int getType() {
    return 1;
  }
  
  public void setDividerhidden(boolean paramBoolean) {
    if (paramBoolean) {
      this._divider_view.setVisibility(8);
      return;
    } 
    this._divider_view.setVisibility(0);
  }
  
  public void setFrame(TelnetFrame paramTelnetFrame) {
    this._telnet_view.setFrame(paramTelnetFrame);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Article\ArticlePage_TelnetItemView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */