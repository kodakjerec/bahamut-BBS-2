package com.kota.Bahamut.dialogs

import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.TextView
import com.kota.asFramework.dialog.ASDialog
import com.kota.Bahamut.R

class Dialog_InsertSymbol : ASDialog(), OnItemClickListener, ListAdapter {
    var _grid_view: GridView
    private var _listener: Dialog_InsertSymbol_Listener? = null
    var _symbols: String =
        "├─┼┴┬┤┌┐╞═╪╡│▕└┘╭╮╰╯╔╦╗╠═╬╣╓╥╖╒╤╕║╚╩╝╟╫╢╙╨╜╞╪╡╘╧╛＿ˍ▁▂▃▄▅▆▇█▏▎▍▌▋▊▉◢◣◥◤﹣﹦≡｜∣∥–︱—︳╴¯￣﹉﹊﹍﹎﹋﹌﹏︴∕﹨╱╲／＼↑↓←→↖↗↙↘㊣◎○●⊕⊙○●△▲☆★◇◆□■▽▼§￥〒￠￡※♀♂〔〕【】《》（）｛｝﹙﹚『』﹛﹜﹝﹞＜＞≦≧﹤﹥「」︵︶︷︸︹︺︻︼︽︾〈〉︿﹀∩∪﹁﹂﹃﹄ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρστυφχψω╳＋﹢－×÷＝≠≒∞ˇ±√⊥∠∟⊿㏒㏑∫∮∵∴"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_insert_symbol)
        if (getWindow() != null) getWindow()!!.setBackgroundDrawable(null)
        this._grid_view = findViewById<GridView>(R.id.SymbolDialog_GridView)
        this._grid_view.setOnItemClickListener(this)
        val list = arrayOfNulls<String>(this._symbols.length)
        for (i in list.indices) {
            list[i] = this._symbols.get(i).toString()
        }
        this._grid_view.setAdapter(
            ArrayAdapter<String?>(
                getContext(),
                R.layout.simple_list_item_1,
                list
            )
        )
        setDialogWidth()
    }

    override fun onItemClick(adapterView: AdapterView<*>?, arg1: View?, index: Int, id: Long) {
        if (this._listener != null) {
            this._listener!!.onSymbolDialogDismissWithSymbol(getItem(index))
        }
        dismiss()
    }

    override fun getCount(): Int {
        return this._symbols.length
    }

    override fun getItem(position: Int): String {
        return this._symbols.get(position).toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = TextView(getContext())
            convertView.setLayoutParams(ViewGroup.LayoutParams(100, 100))
        }
        (convertView as TextView).setText(getItem(position))
        return convertView
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    fun setListener(aListener: Dialog_InsertSymbol_Listener?) {
        this._listener = aListener
    }

    public override fun show() {
        if (currentOrientation == 1) {
            this._grid_view.setNumColumns(4)
        } else {
            this._grid_view.setNumColumns(8)
        }
        super.show()
    }

    // 變更dialog寬度
    fun setDialogWidth() {
        val screenHeight = getContext().getResources().getDisplayMetrics().heightPixels
        val screenWidth = getContext().getResources().getDisplayMetrics().widthPixels
        val dialog_height = (screenHeight * 0.7).toInt()
        val dialog_width = (screenWidth * 0.7).toInt()
        val oldLayoutParams = _grid_view.getLayoutParams()
        oldLayoutParams.width = dialog_width
        oldLayoutParams.height = dialog_height
        _grid_view.setLayoutParams(oldLayoutParams)
    }
}
