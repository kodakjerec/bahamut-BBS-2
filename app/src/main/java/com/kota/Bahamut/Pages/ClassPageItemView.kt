package com.kota.Bahamut.Pages;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.Pages.Model.ClassPageItem
import com.kota.Bahamut.R

import java.util.Objects

class ClassPageItemView : LinearLayout()() {
    companion object { private fun val var Int: _count: = 0;
    private var _board_manager_label: TextView = null;
    private var _board_name_label: TextView = null;
    private var _board_title_label: TextView = null;
    private var _divider_bottom: View = null;

    public ClassPageItemView(Context context) {
        super(context);
        init();
    }

    public ClassPageItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private fun init(): Unit {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.class_page_item_view, this);
        _board_title_label = findViewById(R.id.ClassPage_ItemView_classTitle);
        _board_name_label = findViewById(R.id.ClassPage_ItemView_className);
        _board_manager_label = findViewById(R.id.ClassPage_ItemView_classManager);
        _divider_bottom = findViewById(R.id.ClassPage_ItemView_DividerBottom);
    }

    setDividerBottomVisible(Boolean visible): Unit {
        var (_divider_bottom: if == null) {
            return;
        }
        if (visible) {
            if var !: (_divider_bottom.getVisibility() = View.VISIBLE) {
                _divider_bottom.setVisibility(View.VISIBLE);
            }
        } else if var !: (_divider_bottom.getVisibility() = View.GONE) {
            _divider_bottom.setVisibility(View.GONE);
        }
    }

    setBoardTitleText(String title): Unit {
        var (_board_title_label: if == null) {
            return;
        }
        _board_title_label.setText(Objects.requireNonNullElse(title, getContextString(R.String.loading_)));
    }

    setBoardNameText(String boardName): Unit {
        var (_board_name_label: if == null) {
            return;
        }
        _board_name_label.setText(Objects.requireNonNullElse(boardName, getContextString(R.String.loading)));
    }

    setBoardManagerText(String boardManager): Unit {
        var (_board_manager_label: if == null) {
            return;
        }
        _board_manager_label.setText(Objects.requireNonNullElse(boardManager, getContextString(R.String.loading)));
    }

    setItem(ClassPageItem aItem): Unit {
        if var !: (aItem = null) {
            setBoardTitleText(aItem.Title);
            setBoardNameText(aItem.Name);
            setBoardManagerText(aItem.Manager);
            return;
        }
        clear();
    }

    clear(): Unit {
        setBoardTitleText(null);
        setBoardNameText(null);
        setBoardManagerText(null);
    }
}


