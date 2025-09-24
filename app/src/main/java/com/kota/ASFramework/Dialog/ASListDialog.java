package com.kota.ASFramework.Dialog;

import android.annotation.SuppressLint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kota.Bahamut.R;

import java.util.Vector;

/* loaded from: classes.dex */
public class ASListDialog extends ASDialog {
  public static final int SIZE_LARGE = 1;
  public static final int SIZE_NORMAL = 0;
  private LinearLayout _content_view = null;
  private LinearLayout _item_block = null;
  private ASListDialogItemClickListener _listener = null;
  private final Vector<ASListDialogItem> _item_list = new Vector<>();
  private TextView _title_label = null;
  private float _dialog_width = 280.0f;
  private ScrollView _scroll_view = null;
  private int _item_text_size = 1;

  private static class ASListDialogItem {
    public Button button = null;
    public String title = null;

    ASListDialogItem() {
    }
  }

  public ASListDialog setItemTextSize(int size) {
    this._item_text_size = size;
    return this;
  }

  @Override // com.kota.ASFramework.Dialog.ASDialog
  public String getName() {
    return "ListDialog";
  }

  public ASListDialog() {
    requestWindowFeature(1);
    setContentView(buildContentView());
    getWindow().setBackgroundDrawable(null);
  }

  @SuppressLint("ResourceAsColor")
  private View buildContentView() {
    int frame_padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.0f, getContext().getResources().getDisplayMetrics());
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f, getContext().getResources().getDisplayMetrics());
    LinearLayout frame = new LinearLayout(getContext());
    frame.setBackgroundResource(R.color.dialog_border_color);
    frame.setPadding(frame_padding, frame_padding, frame_padding, frame_padding);
    LinearLayout content_view = new LinearLayout(getContext());
    content_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    content_view.setBackgroundColor(View.MEASURED_STATE_MASK);
    frame.addView(content_view);
    content_view.setOrientation(LinearLayout.VERTICAL);
    int dialog_width = (((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this._dialog_width, getContext().getResources().getDisplayMetrics())) / 2) * 2;
    this._scroll_view = new ScrollView(getContext());
    this._scroll_view.setLayoutParams(new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
    content_view.addView(this._scroll_view);
    this._content_view = new LinearLayout(getContext());
    this._content_view.setLayoutParams(new FrameLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
    this._content_view.setOrientation(LinearLayout.VERTICAL);
    this._content_view.setGravity(Gravity.CENTER);
    this._scroll_view.addView(this._content_view);
    this._title_label = new TextView(getContext());
    this._title_label.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    this._title_label.setPadding(padding, padding, padding, padding);
    this._title_label.setTextColor(-1);
    this._title_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
    this._title_label.setText("選項");
    this._title_label.setBackgroundColor(-14671840);
    this._title_label.setGravity(Gravity.CENTER);
    this._content_view.addView(this._title_label);
    this._item_block = new LinearLayout(getContext());
    this._item_block.setLayoutParams(new FrameLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
    this._item_block.setOrientation(LinearLayout.VERTICAL);
    this._item_block.setGravity(Gravity.CENTER);
    this._content_view.addView(this._item_block);
    return frame;
  }

  public static ASListDialog createDialog() {
    return new ASListDialog();
  }

  public ASListDialog setListener(ASListDialogItemClickListener aListener) {
    this._listener = aListener;
    return this;
  }

  public ASListDialog setTitle(String aTitle) {
    if (this._title_label != null) {
      this._title_label.setText(aTitle);
    }
    return this;
  }

  public ASListDialog addItems(String[] aItemList) {
    for (String item_title : aItemList) {
      addItem(item_title);
    }
    return this;
  }

  public ASListDialog addItem(String aItemTitle) {
    Button button = createButton();
    button.setOnClickListener(v -> ASListDialog.this.onItemClicked((Button) v));
    button.setOnLongClickListener(v -> ASListDialog.this.onItemLongClicked((Button) v));
    if (aItemTitle == null) {
      button.setVisibility(View.GONE);
    } else {
      if (this._item_list.size() > 0) {
        this._item_block.addView(createDivider());
      }
      button.setText(aItemTitle);
    }
    this._item_block.addView(button);
    ASListDialogItem item = new ASListDialogItem();
    item.button = button;
    item.title = aItemTitle;
    this._item_list.add(item);
    return this;
  }

  public View createDivider() {
    View divider = new View(getContext());
    int divider_height = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getContext().getResources().getDisplayMetrics()));
    divider.setLayoutParams(new LinearLayout.LayoutParams(-1, divider_height));
    divider.setBackgroundColor(-2130706433);
    return divider;
  }

  private Button createButton() {
    Button button = new Button(getContext());
    button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    button.setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60.0f, getContext().getResources().getDisplayMetrics()));
    button.setGravity(Gravity.CENTER);
    if (this._item_text_size == 0) {
      button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
    } else if (this._item_text_size == 1) {
      button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
    } else if (this._item_text_size == 2) {
      button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeUltraLarge());
    }
    button.setBackground(ASLayoutParams.getInstance().getListItemBackgroundDrawable());
    button.setTextColor(ASLayoutParams.getInstance().getListItemTextColor());
    button.setSingleLine(true);
    return button;
  }

  private int indexOfButton(Button aButton) {
    for (int i = 0; i < this._item_list.size(); i++) {
      ASListDialogItem item = this._item_list.get(i);
      if (item.button == aButton) {
        int index = i;
        return index;
      }
    }
    return -1;
  }

  private void onItemClicked(Button button) {
    if (this._listener != null) {
      int index = indexOfButton(button);
      if (index != -1) {
        this._listener.onListDialogItemClicked(this, index, this._item_list.get(index).title);
      }
      dismiss();
    }
  }

  private boolean onItemLongClicked(Button button) {
    int index;
    boolean result = false;
    if (this._listener != null && (index = indexOfButton(button)) != -1) {
      result = this._listener.onListDialogItemLongClicked(this, index, this._item_list.get(index).title);
    }
    if (result) {
      dismiss();
    }
    return result;
  }

  public ASListDialog setDialogWidth(float width) {
    this._dialog_width = width;
    int dialog_width = (((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this._dialog_width, getContext().getResources().getDisplayMetrics())) / 2) * 2;
    this._scroll_view.setLayoutParams(new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
    this._content_view.setLayoutParams(new FrameLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
    return this;
  }
}