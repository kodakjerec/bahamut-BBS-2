package com.kota.Bahamut.Dialogs;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.Bahamut.R;

import java.util.Objects;

public class Dialog_SelectSign extends ASDialog implements View.OnClickListener {
    Button _cancel_button;
    Button _confirm_button;
    Dialog_SelectSign_Listener _listener;
    EditText _sign_field;

    public String getName() {
        return "BahamutSelectSignDialog";
    }

    public Dialog_SelectSign() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_select_sign);
        if (getWindow()!=null)
            getWindow().setBackgroundDrawable(null);
        setTitle(getContext().getString(R.string.select_sign));
        this._sign_field = findViewById(R.id.Bahamut_Dialog_Select_Sign_Input_Field);
        this._confirm_button = findViewById(R.id.Bahamut_Dialog_Select_Sign_confirm_Button);
        this._cancel_button = findViewById(R.id.Bahamut_Dialog_Select_Sign_Cancel_Button);
        this._confirm_button.setOnClickListener(this);
        this._cancel_button.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (view == this._confirm_button && this._listener != null) {
            this._listener.onSelectSign(this._sign_field.getText().toString().replace("\n", ""));
        }
        dismiss();
    }

    public void setListener(Dialog_SelectSign_Listener listener) {
        this._listener = listener;
    }
}
