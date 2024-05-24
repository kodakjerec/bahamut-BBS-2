package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Context;
import android.widget.TextView;

import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticleItemView;
import com.kota.TelnetUI.TelnetHeaderItemView;

public class ArticlePage_HeaderItemView extends TelnetHeaderItemView implements TelnetArticleItemView {
    TextView _title;

    public ArticlePage_HeaderItemView(Context context) {
        super(context);

        _title = this.findViewById(R.id.title);
        _title.setOnClickListener(titleClickListener);
        // 自動隱藏文字列
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                new ASRunner() {
//                    @Override
//                    public void run() {
//                        _title.performClick();
//                    }
//                }.runInMainThread();
//            }
//        };
//        timer.schedule(task, 2000);
    }

    public int getType() {
        return 2;
    }
    OnClickListener titleClickListener = view -> {
        if (_title.getMaxLines()==1)
            _title.setMaxLines(3);
        else
            _title.setMaxLines(1);
    };
}
