package com.kota.Bahamut.pages.messages

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.asFramework.pageController.ASNavigationController
import java.text.SimpleDateFormat
import java.util.Date

class MessageMainChatItem(context: Context): LinearLayout(context) {
    private var mainLayout: RelativeLayout
    private var txtSenderName: TextView
    private var txtMessage: TextView
    private var txtReceivedDate: TextView
    private var txtUnReadCount: TextView
    private var scale = context.resources.displayMetrics.density

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL

        mainLayout = RelativeLayout(context).apply {
            id = R.id.content_view
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding((12 * scale).toInt(), (8 * scale).toInt(), (12 * scale).toInt(), (8 * scale).toInt())
        }

        txtSenderName = TextView(context).apply {
            id = R.id.mmiSenderName
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
            layoutParams = params
            setTextColor(ContextCompat.getColor(context, R.color.article_page_text_item_author0))
            textSize = 18f
        }

        txtReceivedDate = TextView(context).apply {
            id = R.id.mmiReceivedDate
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
            layoutParams = params
            setTextColor(ContextCompat.getColor(context, R.color.halfWhite))
            textSize = 12f
        }

        txtUnReadCount = TextView(context).apply {
            id = R.id.mmiUnReadCount
            val params = RelativeLayout.LayoutParams(
                (20 * scale).toInt(),
                (20 * scale).toInt()
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.BELOW, R.id.mmiReceivedDate)
                topMargin = (4 * scale).toInt()
            }
            layoutParams = params
            setBackgroundResource(R.drawable.shape_circle_green)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            textSize = 12f
            visibility = GONE
        }

        txtMessage = TextView(context).apply {
            id = R.id.mmiMessage
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.BELOW, R.id.mmiSenderName)
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.START_OF, R.id.mmiUnReadCount)
                topMargin = (4 * scale).toInt()
                marginEnd = (8 * scale).toInt()
            }
            layoutParams = params
            setTextColor(ContextCompat.getColor(context, R.color.article_page_text_item_content0))
            textSize = 14f
            maxLines = 1
        }

        val divider = View(context).apply {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                addRule(RelativeLayout.BELOW, R.id.mmiMessage)
                topMargin = (8 * scale).toInt()
            }
            layoutParams = params
            setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dividerColor))
        }

        mainLayout.addView(txtSenderName)
        mainLayout.addView(txtReceivedDate)
        mainLayout.addView(txtUnReadCount)
        mainLayout.addView(txtMessage)
        mainLayout.addView(divider)
        addView(mainLayout)
    }

    private var myObject: BahaMessageSummarize = BahaMessageSummarize()

    @SuppressLint("SimpleDateFormat")
    fun setContent(fromObject: BahaMessageSummarize) {
        myObject = fromObject
        txtSenderName.text = fromObject.senderName
        txtMessage.text = fromObject.message
        val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm")
        val date = Date(fromObject.receivedDate)
        txtReceivedDate.text = sdf.format(date)
        txtUnReadCount.text = fromObject.unReadCount.toString()
        if (fromObject.unReadCount == 0) {
            txtUnReadCount.visibility = GONE
        } else {
            txtUnReadCount.visibility = VISIBLE
        }
        mainLayout.setOnClickListener(itemClickListener)
    }

    fun getContent(): BahaMessageSummarize = myObject

    private val itemClickListener = OnClickListener { _ ->
        val aPage = PageContainer.instance!!.getMessageSub()
        ASNavigationController.currentController?.pushViewController(aPage)
        aPage.setSenderName(txtSenderName.text.toString())
    }
}