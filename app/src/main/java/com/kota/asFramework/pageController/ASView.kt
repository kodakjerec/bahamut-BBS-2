package com.kota.asFramework.pageController

import android.content.Context
import android.util.AttributeSet
import android.view.View

class ASView : View {
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?) : super(context)
}
