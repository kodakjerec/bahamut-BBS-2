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

class DialogInsertSymbol : ASDialog(), OnItemClickListener, ListAdapter {
    var mainView: GridView
    private var _listener: DialogInsertSymbolListener? = null
    var symbols: String =
        "├─┼┴┬┤┌┐╞═╪╡│▕└┘╭╮╰╯╔╦╗╠═╬╣╓╥╖╒╤╕║╚╩╝╟╫╢╙╨╜╞╪╡╘╧╛＿ˍ▁▂▃▄▅▆▇█▏▎▍▌▋▊▉◢◣◥◤﹣﹦≡｜∣∥–︱—︳╴¯￣﹉﹊﹍﹎﹋﹌﹏︴∕﹨╱╲／＼↑↓←→↖↗↙↘㊣◎○●⊕⊙○●△▲☆★◇◆□■▽▼§￥〒￠￡※♀♂〔〕【】《》（）｛｝﹙﹚『』﹛﹜﹝﹞＜＞≦≧﹤﹥「」︵︶︷︸︹︺︻︼︽︾〈〉︿﹀∩∪﹁﹂﹃﹄ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρστυφχψω╳＋﹢－×÷＝≠≒∞ˇ±√⊥∠∟⊿㏒㏑∫∮∵∴"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_insert_symbol)
        if (window != null) window?.setBackgroundDrawable(null)
        this.mainView = findViewById<GridView>(R.id.SymbolDialog_GridView)
        this.mainView.onItemClickListener = this
        val list = arrayOfNulls<String>(this.symbols.length)
        for (i in list.indices) {
            list[i] = this.symbols[i].toString()
        }
        this.mainView.adapter = ArrayAdapter<String?>(
            context,
            R.layout.simple_list_item_1,
            list
        )
        setDialogWidth(mainView)
    }

    override fun onItemClick(adapterView: AdapterView<*>?, arg1: View?, index: Int, id: Long) {
        if (this._listener != null) {
            this._listener?.onSymbolDialogDismissWithSymbol(getItem(index))
        }
        dismiss()
    }

    override fun getCount(): Int {
        return this.symbols.length
    }

    override fun getItem(position: Int): String {
        return this.symbols[position].toString()
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
            convertView = TextView(context)
            convertView.layoutParams = ViewGroup.LayoutParams(100, 100)
        }
        (convertView as TextView).text = getItem(position)
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

    fun setListener(aListener: DialogInsertSymbolListener?) {
        this._listener = aListener
    }

    override fun show() {
        if (currentOrientation == 1) {
            this.mainView.numColumns = 4
        } else {
            this.mainView.numColumns = 8
        }
        super.show()
    }
}
