package com.kumi.ASFramework.Dialog;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Vector;

public class ASListDialog extends ASDialog {
  public static final int SIZE_LARGE = 1;
  
  public static final int SIZE_NORMAL = 0;
  
  private LinearLayout _content_view = null;
  
  private float _dialog_width = 280.0F;
  
  private LinearLayout _item_block = null;
  
  private Vector<ASListDialogItem> _item_list = new Vector<ASListDialogItem>();
  
  private int _item_text_size = 1;
  
  private ASListDialogItemClickListener _listener = null;
  
  private ScrollView _scroll_view = null;
  
  private TextView _title_label = null;
  
  public ASListDialog() {
    requestWindowFeature(1);
    setContentView(buildContentView());
    getWindow().setBackgroundDrawable(null);
  }
  
  private View buildContentView() {
    int j = (int)TypedValue.applyDimension(1, 3.0F, getContext().getResources().getDisplayMetrics());
    int i = (int)TypedValue.applyDimension(1, 5.0F, getContext().getResources().getDisplayMetrics());
    LinearLayout linearLayout2 = new LinearLayout(getContext());
    linearLayout2.setBackgroundColor(-1);
    linearLayout2.setPadding(j, j, j, j);
    LinearLayout linearLayout1 = new LinearLayout(getContext());
    linearLayout1.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(-2, -2));
    linearLayout1.setBackgroundColor(-16777216);
    linearLayout2.addView((View)linearLayout1);
    linearLayout1.setOrientation(1);
    j = (int)TypedValue.applyDimension(1, this._dialog_width, getContext().getResources().getDisplayMetrics()) / 2 * 2;
    this._scroll_view = new ScrollView(getContext());
    this._scroll_view.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(j, -2));
    linearLayout1.addView((View)this._scroll_view);
    this._content_view = new LinearLayout(getContext());
    this._content_view.setLayoutParams((ViewGroup.LayoutParams)new FrameLayout.LayoutParams(j, -2));
    this._content_view.setOrientation(1);
    this._content_view.setGravity(17);
    this._scroll_view.addView((View)this._content_view);
    this._title_label = new TextView(getContext());
    this._title_label.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(-1, -2));
    this._title_label.setPadding(i, i, i, i);
    this._title_label.setTextColor(-1);
    this._title_label.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
    this._title_label.setText("選項");
    this._title_label.setBackgroundColor(-14671840);
    this._title_label.setGravity(17);
    this._content_view.addView((View)this._title_label);
    this._item_block = new LinearLayout(getContext());
    this._item_block.setLayoutParams((ViewGroup.LayoutParams)new FrameLayout.LayoutParams(j, -2));
    this._item_block.setOrientation(1);
    this._item_block.setGravity(17);
    this._content_view.addView((View)this._item_block);
    return (View)linearLayout2;
  }
  
  private Button createButton() {
    Button button = new Button(getContext());
    button.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(-1, -2));
    button.setMinimumHeight((int)TypedValue.applyDimension(1, 60.0F, getContext().getResources().getDisplayMetrics()));
    button.setGravity(17);
    if (this._item_text_size == 0) {
      button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeNormal());
      button.setBackgroundDrawable(ASLayoutParams.getInstance().getListItemBackgroundDrawable());
      button.setTextColor(ASLayoutParams.getInstance().getListItemTextColor());
      button.setSingleLine(true);
      return button;
    } 
    if (this._item_text_size == 1) {
      button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeLarge());
      button.setBackgroundDrawable(ASLayoutParams.getInstance().getListItemBackgroundDrawable());
      button.setTextColor(ASLayoutParams.getInstance().getListItemTextColor());
      button.setSingleLine(true);
      return button;
    } 
    if (this._item_text_size == 2)
      button.setTextSize(2, ASLayoutParams.getInstance().getTextSizeUltraLarge()); 
    button.setBackgroundDrawable(ASLayoutParams.getInstance().getListItemBackgroundDrawable());
    button.setTextColor(ASLayoutParams.getInstance().getListItemTextColor());
    button.setSingleLine(true);
    return button;
  }
  
  public static ASListDialog createDialog() {
    return new ASListDialog();
  }
  
  private int indexOfButton(Button paramButton) {
    byte b1 = -1;
    for (byte b = 0;; b++) {
      byte b2 = b1;
      if (b < this._item_list.size()) {
        if (((ASListDialogItem)this._item_list.get(b)).button == paramButton)
          return b; 
      } else {
        return b2;
      } 
    } 
  }
  
  private void onItemClicked(Button paramButton) {
    if (this._listener != null) {
      int i = indexOfButton(paramButton);
      if (i != -1)
        this._listener.onListDialogItemClicked(this, i, ((ASListDialogItem)this._item_list.get(i)).title); 
      dismiss();
    } 
  }
  
  private boolean onItemLongClicked(Button paramButton) {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (this._listener != null) {
      int i = indexOfButton(paramButton);
      bool1 = bool2;
      if (i != -1)
        bool1 = this._listener.onListDialogItemLongClicked(this, i, ((ASListDialogItem)this._item_list.get(i)).title); 
    } 
    if (bool1)
      dismiss(); 
    return bool1;
  }
  
  public ASListDialog addItem(String paramString) {
    Button button = createButton();
    button.setOnClickListener(new View.OnClickListener() {
          final ASListDialog this$0;
          
          public void onClick(View param1View) {
            ASListDialog.this.onItemClicked((Button)param1View);
          }
        });
    button.setOnLongClickListener(new View.OnLongClickListener() {
          final ASListDialog this$0;
          
          public boolean onLongClick(View param1View) {
            return ASListDialog.this.onItemLongClicked((Button)param1View);
          }
        });
    if (paramString == null) {
      button.setVisibility(8);
      this._item_block.addView((View)button);
      ASListDialogItem aSListDialogItem1 = new ASListDialogItem();
      aSListDialogItem1.button = button;
      aSListDialogItem1.title = paramString;
      this._item_list.add(aSListDialogItem1);
      return this;
    } 
    if (this._item_list.size() > 0)
      this._item_block.addView(createDivider()); 
    button.setText(paramString);
    this._item_block.addView((View)button);
    ASListDialogItem aSListDialogItem = new ASListDialogItem();
    aSListDialogItem.button = button;
    aSListDialogItem.title = paramString;
    this._item_list.add(aSListDialogItem);
    return this;
  }
  
  public ASListDialog addItems(String[] paramArrayOfString) {
    int i = paramArrayOfString.length;
    for (byte b = 0; b < i; b++)
      addItem(paramArrayOfString[b]); 
    return this;
  }
  
  public View createDivider() {
    View view = new View(getContext());
    view.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(-1, (int)Math.ceil(TypedValue.applyDimension(1, 1.0F, getContext().getResources().getDisplayMetrics()))));
    view.setBackgroundColor(-2130706433);
    return view;
  }
  
  public String getName() {
    return "ListDialog";
  }
  
  public ASListDialog setDialogWidth(float paramFloat) {
    this._dialog_width = paramFloat;
    int i = (int)TypedValue.applyDimension(1, this._dialog_width, getContext().getResources().getDisplayMetrics()) / 2 * 2;
    this._scroll_view.setLayoutParams((ViewGroup.LayoutParams)new LinearLayout.LayoutParams(i, -2));
    this._content_view.setLayoutParams((ViewGroup.LayoutParams)new FrameLayout.LayoutParams(i, -2));
    return this;
  }
  
  public ASListDialog setItemTextSize(int paramInt) {
    this._item_text_size = paramInt;
    return this;
  }
  
  public ASListDialog setListener(ASListDialogItemClickListener paramASListDialogItemClickListener) {
    this._listener = paramASListDialogItemClickListener;
    return this;
  }
  
  public ASListDialog setTitle(String paramString) {
    if (this._title_label != null)
      this._title_label.setText(paramString); 
    return this;
  }
  
  class ASListDialogItem {
    public Button button = null;
    
    final ASListDialog this$0;
    
    public String title = null;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASListDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */