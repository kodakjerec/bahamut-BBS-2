package com.kota.Bahamut.Dialogs

import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.TextView
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.Bahamut.R

class Dialog_InsertSymbol : ASDialog(), AdapterView.OnItemClickListener, ListAdapter {
    private lateinit var _grid_view: GridView
    private var _listener: Dialog_InsertSymbol_Listener? = null
    private val _symbols = "├─┼┴┬┤┌┐╞═╪╡│▕└┘╭╮╰╯╔╦╗╠═╬╣╓╥╖╒╤╕║╚╩╝╟╫╢╙╨╜╞╪╡╘╧╛＿ˍ▁▂▃▄▅▆▇█▏▎▍▌▋▊▉◢◣◥◤﹣﹦≡｜∣∥–︱—︳╴¯￣﹉﹊﹍﹎﹋﹌﹏︴∕﹨╱╲／＼↑↓←→↖↗↙↘㊣◎○●⊕⊙○●△▲☆★◇◆□■▽▼§￥〒￠￡※♀♂〔〕【】《》（）｛｝﹙﹚『』﹛﹜﹝﹞＜＞≦≧﹤﹥「」︵︶︷︸︹︺︻︼︽︾〈〉︿﹀∩∪﹁﹂﹃﹄ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρστυφχψω╳＋﹢－×÷＝≠≒∞ˇ±√⊥∠∟⊿㏒㏑∫∮∵∴"

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_insert_symbol)
        window?.setBackgroundDrawable(null)
        _grid_view = findViewById(R.id.SymbolDialog_GridView)
        _grid_view.onItemClickListener = this
        val list = Array(_symbols.length) { i ->
            _symbols[i].toString()
        }
        _grid_view.adapter = ArrayAdapter(context, R.layout.simple_list_item_1, list)
        setDialogWidth()
    }

    override fun onItemClick(adapterView: AdapterView<*>?, arg1: View?, index: Int, id: Long) {
        _listener?.onSymbolDialogDismissWithSymbol(getItem(index))
        dismiss()
    }

    override fun getCount(): Int {
        return _symbols.length
    }

    fun getItem(position: Int): String {
        return _symbols[position].toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(100, 100)
        }
        (view as TextView).text = getItem(position)
        return view
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
        // Not implemented
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        // Not implemented
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    fun setListener(aListener: Dialog_InsertSymbol_Listener?) {
        _listener = aListener
    }

    override fun show() {
        if (currentOrientation == 1) {
            _grid_view.numColumns = 4
        } else {
            _grid_view.numColumns = 8
        }
        super.show()
    }

    // 變更dialog寬度
    private fun setDialogWidth() {
        val screenHeight = context.resources.displayMetrics.heightPixels
        val screenWidth = context.resources.displayMetrics.widthPixels
        val dialogHeight = (screenHeight * 0.7).toInt()
        val dialogWidth = (screenWidth * 0.7).toInt()
        val oldLayoutParams = _grid_view.layoutParams
        oldLayoutParams.width = dialogWidth
        oldLayoutParams.height = dialogHeight
        _grid_view.layoutParams = oldLayoutParams
    }
}
