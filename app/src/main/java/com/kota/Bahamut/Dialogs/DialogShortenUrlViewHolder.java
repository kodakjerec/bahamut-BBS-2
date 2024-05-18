package com.kota.Bahamut.Dialogs;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kota.Bahamut.R;

public class DialogShortenUrlViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView title;
    private final TextView description;
    public int index = 0;
    public DialogShortenUrlItemViewListener mListener;

    public DialogShortenUrlViewHolder(View view, DialogShortenUrlItemViewListener listener) {
        super(view);

        title = view.findViewById(R.id.thumbnail_title);
        description = view.findViewById(R.id.thumbnail_description);

        this.mListener = listener;
        view.setOnClickListener(this);
    }

    public void setTitle(String aTitle) {
        if (title != null) {
            title.setText(aTitle);
            title.setContentDescription("Url Title: "+aTitle);
        }
    }
    public void setDescription(String aDescription) {
        if (description != null) {
            description.setText(aDescription);
            description.setContentDescription("Url Description: "+aDescription);
        }
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onDialogShortenUrlItemViewClicked(this);
        }
    }
}
