package com.kota.Bahamut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.ui.theme.AppTheme

@Composable
fun RightArrow (
    onClick: (() -> Unit)? = null
){
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(20.dp)
            .clickable { onClick?.invoke() }
            .background(colors.surface),
        contentAlignment = Alignment.Center
    ) {
        BahaText(
            text = ">",
            color = colors.textPrimary
        )
    }
}