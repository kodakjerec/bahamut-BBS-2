package com.kota.Bahamut.dialogs

import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.theme.ThemeStore
import com.kota.Bahamut.service.CommonFunctions.getThemeColor
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.dialog.ASDialog
import com.kota.telnetUI.textView.TelnetTextViewLarge

class DialogPostToolbarEdit(
    private val postArticlePage: PostArticlePage,
    theme: Int = ThemeStore.getDialogThemeResId()
) : ASDialog(theme) {

    private val listContainer: LinearLayout

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_post_toolbar_edit)
        if (window != null) window?.setBackgroundDrawable(null)

        val mainLayout = findViewById<LinearLayout>(R.id.dialog_post_toolbar_edit_content_view)!!
        listContainer = findViewById(R.id.PostToolbarEdit_list)!!

        findViewById<Button>(R.id.PostToolbarEdit_Done)?.setOnClickListener {
            dismiss()
        }

        setDialogWidth(mainLayout)
        refreshList()
    }

    /** 重新動態繪製按鈕列表與上移/下移按鈕 */
    fun refreshList() {
        listContainer.removeAllViews()

        val keys = UserSettings.propertiesPostToolbarOrder.split(",").toMutableList()
        val context = context

        keys.forEachIndexed { index, key ->
            // 分組標頭：Row 1 (前 3 個項目)
            if (index == 0) {
                val headerView = TelnetTextViewLarge(context).apply {
                    text = context.getString(R.string.post_toolbar_row_1)
                    setTextColor(getThemeColor(context, R.attr.bahamut_chapterTextColor))
                    setBackgroundColor(getThemeColor(context, R.attr.bahamut_dialogTitleBackground))
                    setPadding(24, 12, 16, 12)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                listContainer.addView(headerView)
            }

            // 分組標頭：Row 2 (後 4 個項目)
            if (index == 3) {
                val headerView = TelnetTextViewLarge(context).apply {
                    text = context.getString(R.string.post_toolbar_row_2)
                    setTextColor(getThemeColor(context, R.attr.bahamut_chapterTextColor))
                    setBackgroundColor(getThemeColor(context, R.attr.bahamut_dialogTitleBackground))
                    setPadding(24, 12, 16, 12)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 0)
                    }
                }
                listContainer.addView(headerView)
            }

            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(12, 8, 12, 8)
                }
            }

            // 按鈕名稱
            val nameTextView = TelnetTextViewLarge(context).apply {
                text = getButtonNameForKey(key)
                setTextColor(getThemeColor(context, R.attr.bahamut_defaultTextColor))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            // 上移按鈕
            val upButton = Button(context, null, 0, R.style.ToolbarItem).apply {
                text = "▲"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    (44 * context.resources.displayMetrics.density).toInt(),
                    (44 * context.resources.displayMetrics.density).toInt()
                ).apply {
                    setMargins(4, 0, 4, 0)
                }
                isEnabled = index > 0
                setOnClickListener {
                    if (index > 0) {
                        val temp = keys[index]
                        keys[index] = keys[index - 1]
                        keys[index - 1] = temp
                        saveAndApply(keys)
                    }
                }
            }

            // 下移按鈕
            val downButton = Button(context, null, 0, R.style.ToolbarItem).apply {
                text = "▼"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    (44 * context.resources.displayMetrics.density).toInt(),
                    (44 * context.resources.displayMetrics.density).toInt()
                ).apply {
                    setMargins(4, 0, 4, 0)
                }
                isEnabled = index < keys.size - 1
                setOnClickListener {
                    if (index < keys.size - 1) {
                        val temp = keys[index]
                        keys[index] = keys[index + 1]
                        keys[index + 1] = temp
                        saveAndApply(keys)
                    }
                }
            }

            rowLayout.addView(nameTextView)
            rowLayout.addView(upButton)
            rowLayout.addView(downButton)

            listContainer.addView(rowLayout)

            // 分隔線
            if (index < keys.size - 1) {
                val divider = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    )
                    setBackgroundResource(R.color.divider_color)
                }
                listContainer.addView(divider)
            }
        }
    }

    private fun saveAndApply(keys: List<String>) {
        val newOrder = keys.joinToString(",")
        UserSettings.propertiesPostToolbarOrder = newOrder
        postArticlePage.applyToolbarOrder()
        refreshList()
    }

    private fun getButtonNameForKey(key: String): String {
        return when (key) {
            "REFERENCE" -> "引用"
            "SYMBOL" -> "符號"
            "FACE" -> "表情"
            "COLOR" -> "上色"
            "FILE" -> "檔案"
            "SHORTEN_URL" -> "短網址"
            "SHORTEN_IMAGE" -> "縮圖"
            else -> key
        }
    }
}
