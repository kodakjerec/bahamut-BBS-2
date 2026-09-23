package com.kota.Bahamut.dialogs

import android.database.DataSetObserver
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.asFramework.dialog.ASDialog

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
        this.mainView.adapter = this
        findViewById<View>(R.id.cancel)?.setOnClickListener {
            dismiss()
        }
        val mainLayout = findViewById<View>(R.id.dialog_insert_symbol_main_layout)
        setDialogWidthHeight(mainLayout)
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
        val itemSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            52f, // > 48dp 方便點選
            context.resources.displayMetrics
        ).toInt()

        var view = convertView
        if (view == null) {
            val textView = TextView(context)
            textView.layoutParams = AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                itemSizePx
            )
            textView.gravity = Gravity.CENTER
            textView.textSize = 22f

            // 水波紋點擊回饋背景
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            textView.setBackgroundResource(outValue.resourceId)

            view = textView
        }
        val textView = view as TextView
        textView.text = getItem(position)

        // 套用主題顏色
        textView.setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_defaultTextColor))

        return textView
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
        return true
    }

    override fun isEnabled(position: Int): Boolean {
        return true
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
