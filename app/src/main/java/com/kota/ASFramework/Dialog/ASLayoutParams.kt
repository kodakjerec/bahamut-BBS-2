package com.kota.ASFramework.Dialog

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable

class ASLayoutParams private constructor() {
    init {
        initial()
    }

    private fun initial() {}

    val alertItemBackgroundDrawable: Drawable
        get() {
            val stateListDrawable = StateListDrawable()
            var colorDrawable = ColorDrawable(-14066)
            stateListDrawable.addState(intArrayOf(16842919, 16842910), colorDrawable)
            colorDrawable = ColorDrawable(-8388608)
            stateListDrawable.addState(intArrayOf(16842910, 16842908), colorDrawable)
            colorDrawable = ColorDrawable(-12582912)
            stateListDrawable.addState(intArrayOf(16842910), colorDrawable)
            colorDrawable = ColorDrawable(-14680064)
            stateListDrawable.addState(IntArray(0), colorDrawable)
            return stateListDrawable
        }

    val alertItemTextColor: ColorStateList
        get() = ColorStateList(
            arrayOf<IntArray?>(
                intArrayOf(16842919, 16842910),
                intArrayOf(16842910, 16842908),
                intArrayOf(16842910),
                intArrayOf()
            ), intArrayOf(-16777216, -16777216, -1, -8355712)
        )

    val defaultTouchBlockHeight: Float
        get() = 60.0f

    val defaultTouchBlockWidth: Float
        get() = 60.0f

    val dialogWidthLarge: Float
        get() = 320.0f

    val dialogWidthNormal: Float
        get() = 270.0f

    val listItemBackgroundDrawable: Drawable
        get() {
            val stateListDrawable = StateListDrawable()
            var colorDrawable = ColorDrawable(-14066)
            stateListDrawable.addState(intArrayOf(16842919, 16842910), colorDrawable)
            colorDrawable = ColorDrawable(-16777216)
            stateListDrawable.addState(intArrayOf(16842910, 16842908), colorDrawable)
            colorDrawable = ColorDrawable(-16777216)
            stateListDrawable.addState(intArrayOf(16842910), colorDrawable)
            colorDrawable = ColorDrawable(-16777216)
            stateListDrawable.addState(IntArray(0), colorDrawable)
            return stateListDrawable
        }

    val listItemTextColor: ColorStateList
        get() = ColorStateList(
            arrayOf<IntArray?>(
                intArrayOf(16842919, 16842910),
                intArrayOf(16842910, 16842908),
                intArrayOf(16842910)
            ), intArrayOf(-16777216, -16777216, -1, -8355712)
        )

    val paddingLarge: Float
        get() = 20.0f

    val paddingNormal: Float
        get() = 10.0f

    val paddingSmall: Float
        get() = 5.0f

    val textSizeLarge: Float
        get() = 24.0f

    val textSizeNormal: Float
        get() = 20.0f

    val textSizeSmall: Float
        get() = 16.0f

    val textSizeUltraLarge: Float
        get() = 28.0f

    companion object {
        private var _instance: ASLayoutParams? = null

        @JvmStatic
        val instance: ASLayoutParams
            get() {
                if (_instance == null) _instance =
                    ASLayoutParams()
                return _instance!!
            }
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASLayoutParams.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


