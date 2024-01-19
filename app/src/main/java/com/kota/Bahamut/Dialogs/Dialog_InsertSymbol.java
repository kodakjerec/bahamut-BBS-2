package com.kota.Bahamut.Dialogs;

import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.Bahamut.R;

public class Dialog_InsertSymbol extends ASDialog implements AdapterView.OnItemClickListener, ListAdapter {
    GridView _grid_view = null;
    private Dialog_InsertSymbol_Listener _listener = null;
    String _symbols = "├─┼┴┬┤┌┐╞═╪╡│▕└┘╭╮╰╯╔╦╗╠═╬╣╓╥╖╒╤╕║╚╩╝╟╫╢╙╨╜╞╪╡╘╧╛＿ˍ▁▂▃▄▅▆▇█▏▎▍▌▋▊▉◢◣◥◤﹣﹦≡｜∣∥–︱—︳╴¯￣﹉﹊﹍﹎﹋﹌﹏︴∕﹨╱╲／＼↑↓←→↖↗↙↘㊣◎○●⊕⊙○●△▲☆★◇◆□■▽▼§￥〒￠￡※♀♂〔〕【】《》（）｛｝﹙﹚『』﹛﹜﹝﹞＜＞≦≧﹤﹥「」︵︶︷︸︹︺︻︼︽︾〈〉︿﹀∩∪﹁﹂﹃﹄ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρστυφχψω╳＋﹢－×÷＝≠≒∞ˇ±√⊥∠∟⊿㏒㏑∫∮∵∴";

    public Dialog_InsertSymbol() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_symbol);
        getWindow().setBackgroundDrawable((Drawable) null);
        this._grid_view = (GridView) findViewById(R.id.SymbolDialog_GridView);
        this._grid_view.setOnItemClickListener(this);
        String[] list = new String[this._symbols.length()];
        for (int i = 0; i < list.length; i++) {
            list[i] = "" + this._symbols.charAt(i);
        }
        this._grid_view.setAdapter(new ArrayAdapter<>(getContext(), R.layout.simple_list_item_1, list));
    }

    public void onItemClick(AdapterView<?> adapterView, View arg1, int index, long id) {
        if (this._listener != null) {
            this._listener.onSymbolDialogDismissWithSymbol(getItem(index));
        }
        dismiss();
    }

    public int getCount() {
        return this._symbols.length();
    }

    public String getItem(int position) {
        return "" + this._symbols.charAt(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextView(getContext());
            convertView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        }
        ((TextView) convertView).setText(getItem(position));
        return convertView;
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

    public void registerDataSetObserver(DataSetObserver observer) {
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return false;
    }

    public void setListsner(Dialog_InsertSymbol_Listener aListener) {
        this._listener = aListener;
    }

    public void show() {
        if (getCurrentOrientation() == 1) {
            this._grid_view.setNumColumns(4);
        } else {
            this._grid_view.setNumColumns(8);
        }
        super.show();
    }
}
