package com.kota.Bahamut.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
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

        BahaAlertDialogContent(
            title = "符號表",
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.DANGER,
                    onClick = { dismiss() }
                )
            )
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                items(symbolList) { sym ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable {
                                _listener?.onSymbolDialogDismissWithSymbol(sym)
                                dismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        BahaText(
                            text = sym,
                            color = colors.textPrimary,
                        )
                    }
                }
            }
        }
    }

    fun setListener(aListener: DialogInsertSymbolListener?) {
        this._listener = aListener
    }
}
