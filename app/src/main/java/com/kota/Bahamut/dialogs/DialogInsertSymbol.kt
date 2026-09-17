package com.kota.Bahamut.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog

class DialogInsertSymbol : ASDialog() {
    private var _listener: DialogInsertSymbolListener? = null
    private val symbols: String =
        "├─┼┴┬┤┌┐╞═╪╡│▕└┘╭╮╰╯╔╦╗╠═╬╣╓╥╖╒╤╕║╚╩╝╟╫╢╙╨╜╞╪╡╘╧╛＿ˍ▁▂▃▄▅▆▇█▏▎▍▌▋▊▉◢◣◥◤﹣﹦≡｜∣∥–︱—︳╴¯￣﹉﹊﹍﹎﹋﹌﹏︴∕﹨╱╲／＼↑↓←→↖↗↙↘㊣◎○●⊕⊙○●△▲☆★◇◆□■▽▼§￥〒￠￡※♀♂〔〕【】《》（）｛｝﹙﹚『』﹛﹜﹝﹞＜＞≦≧﹤﹥「」︵︶︷︸︹︺︻︼︽︾〈〉︿﹀∩∪﹁﹂﹃﹄ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρστυφχψω╳＋﹢－×÷＝≠≒∞ˇ±√⊥∠∟⊿㏒㏑∫∮∵∴"

    override val name: String?
        get() = "BahamutInsertSymbolDialog"

    init {
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val colors = AppTheme.colors
        val symbolList = symbols.map { it.toString() }

        Box(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 360.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.pageBackground)
                .border(1.dp, colors.dialogBorder, RoundedCornerShape(6.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 標題列
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.dialogTitleBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "符號表",
                        color = colors.titleBarTitle,
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 40.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(symbolList) { sym ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        _listener?.onSymbolDialogDismissWithSymbol(sym)
                                        dismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sym,
                                    color = colors.textPrimary,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }

                // 關閉按鈕
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.dialogTitleBackground)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    BahaButton(
                        text = CommonFunctions.getContextString(R.string.cancel),
                        type = ButtonType.SECONDARY,
                        onClick = { dismiss() },
                        minHeight = 36.dp
                    )
                }
            }
        }
    }

    fun setListener(aListener: DialogInsertSymbolListener?) {
        this._listener = aListener
    }
}
