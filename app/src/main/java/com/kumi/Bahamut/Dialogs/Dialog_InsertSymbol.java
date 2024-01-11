package com.kumi.Bahamut.Dialogs;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.kumi.ASFramework.Dialog.ASDialog;

public class Dialog_InsertSymbol extends ASDialog implements AdapterView.OnItemClickListener, ListAdapter {
  GridView _grid_view = null;
  
  private Dialog_InsertSymbol_Listener _listener = null;
  
  String _symbols = "├─┼┴┬┤┌┐╞═╪╡│▕└┘╭╮╰╯╔╦╗╠═╬╣╓╥╖╒╤╕║╚╩╝╟╫╢╙╨╜╞╪╡╘╧╛＿ˍ▁▂▃▄▅▆▇█▏▎▍▌▋▊▉◢◣◥◤﹣﹦≡｜∣∥–︱—︳╴¯￣﹉﹊﹍﹎﹋﹌﹏︴∕﹨╱╲／＼↑↓←→↖↗↙↘㊣◎○●⊕⊙○●△▲☆★◇◆□■▽▼§￥〒￠￡※♀♂〔〕【】《》（）｛｝﹙﹚『』﹛﹜﹝﹞＜＞≦≧﹤﹥「」︵︶︷︸︹︺︻︼︽︾〈〉︿﹀∩∪﹁﹂﹃﹄ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρστυφχψω╳＋﹢－×÷＝≠≒∞ˇ±√⊥∠∟⊿㏒㏑∫∮∵∴";
  
  public Dialog_InsertSymbol() {
    requestWindowFeature(1);
    setContentView(2131361841);
    getWindow().setBackgroundDrawable(null);
    this._grid_view = (GridView)findViewById(2131230930);
    this._grid_view.setOnItemClickListener(this);
    String[] arrayOfString = new String[this._symbols.length()];
    for (byte b = 0; b < arrayOfString.length; b++)
      arrayOfString[b] = "" + this._symbols.charAt(b); 
    ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), 2131361868, (Object[])arrayOfString);
    this._grid_view.setAdapter((ListAdapter)arrayAdapter);
  }
  
  public boolean areAllItemsEnabled() {
    return false;
  }
  
  public int getCount() {
    return this._symbols.length();
  }
  
  public String getItem(int paramInt) {
    char c = this._symbols.charAt(paramInt);
    return "" + c;
  }
  
  public long getItemId(int paramInt) {
    return paramInt;
  }
  
  public int getItemViewType(int paramInt) {
    return 0;
  }
  
  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
    TextView textView;
    View view = paramView;
    if (paramView == null) {
      textView = new TextView(getContext());
      textView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
    } 
    textView.setText(getItem(paramInt));
    return (View)textView;
  }
  
  public int getViewTypeCount() {
    return 1;
  }
  
  public boolean hasStableIds() {
    return false;
  }
  
  public boolean isEmpty() {
    return false;
  }
  
  public boolean isEnabled(int paramInt) {
    return false;
  }
  
  public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
    if (this._listener != null) {
      String str = getItem(paramInt);
      this._listener.onSymbolDialogDismissWithSymbol(str);
    } 
    dismiss();
  }
  
  public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {}
  
  public void setListsner(Dialog_InsertSymbol_Listener paramDialog_InsertSymbol_Listener) {
    this._listener = paramDialog_InsertSymbol_Listener;
  }
  
  public void show() {
    if (getCurrentOrientation() == 1) {
      this._grid_view.setNumColumns(4);
    } else {
      this._grid_view.setNumColumns(8);
    } 
    super.show();
  }
  
  public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {}
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Dialogs\Dialog_InsertSymbol.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */