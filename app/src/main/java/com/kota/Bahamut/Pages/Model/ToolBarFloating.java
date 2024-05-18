package com.kota.Bahamut.Pages.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ToolBarFloating extends LinearLayout {
    private LinearLayout _myView;
    private Button _btnSetting;
    private Button _btn1;
    private Button _btn2;
    private float scale; // 畫面精度
    private Timer timer;

    private float _idle; // 閒置多久
    private float _alpha; // 閒置不透明度

    public ToolBarFloating(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context) {
        _idle = UserSettings.getToolbarIdle();
        _alpha = UserSettings.getToolbarAlpha()/100;
        inflate(context, R.layout.toolbar_floating, this);
        scale = getContext().getResources().getDisplayMetrics().density;

        _myView = (LinearLayout) findViewById(R.id.ToolbarFloating);
        // 取得上次紀錄
        List<Float> list = UserSettings.getFloatingLocation();
        if (list.size()>0 && list.get(0)>=0.0f) {
            float pointX = list.get(0);
            float pointY = list.get(1);
            updateLayout(pointX, pointY, false);
        } else {
            // 畫面預設值
            float screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            float screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
            updateLayout(screenWidth, screenHeight / 2, false);
        }

        _btnSetting = (Button)_myView.findViewById(R.id.ToolbarFloating_setting);
        _btnSetting.setOnTouchListener(onTouchListener);

        _btn1 = (Button) _myView.findViewById(R.id.ToolbarFloating_1);
        _btn2 = (Button) _myView.findViewById(R.id.ToolbarFloating_2);

        // 啟用定時隱藏
        // 如果之前已經隱藏就不要再讓他顯現出來
        if (TempSettings.isFloatingInvisible)
            _myView.setAlpha(_alpha);
        else
            startInvisible();
    }

    // 移動toolbar
    @SuppressLint("ClickableViewAccessibility")
    private final OnTouchListener onTouchListener = (view, event) -> {
        long duration = event.getEventTime() - event.getDownTime();
        float pointX = event.getRawX();
        float pointY = event.getRawY();
        // 微調手指中心點
        pointX -= scale*30;
        pointY -= scale*60;

        // 彈出視窗位置
        int[] location = new int[2];
        getRootView().getLocationOnScreen(location);
        pointX -= location[0];
        pointY -= location[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下
                cancelInvisible();
                break;
            case MotionEvent.ACTION_UP:
                if (duration < 200) { // click
                    _btnSetting.performClick();
                } else { // 将LinearLayout的位置更新到最终的位置
                    updateLayout(pointX, pointY , false);
                }
                startInvisible();
                break;
            case MotionEvent.ACTION_MOVE:
                // 更新LinearLayout的位置
                updateLayout(pointX, pointY, true);
                break;
        }
        return true;
    };

    // 更新toolbar位置
    private void updateLayout(float deltaX, float deltaY, boolean dragging) {
        // 获取LinearLayout的LayoutParams
        int barWidth = _myView.getLayoutParams().width;
        int barHeight = _myView.getLayoutParams().height;
        float screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        float screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;

        // X軸錯誤處理
        if (deltaX < 0) {
            deltaX = 0;
        } else if ((deltaX + barWidth) > screenWidth) {
            deltaX = screenWidth - barWidth;
        } else {
            if (!dragging) {
                // 吸附X軸
                if (deltaX > screenWidth / 2) {
                    deltaX = screenWidth - barWidth;
                } else {
                    deltaX = 0;
                }
            }
        }

        // Y軸錯誤處理
        if ((deltaY + barHeight) > screenHeight) {
            deltaY = screenHeight - barHeight;
        } else if (deltaY < 0) {
            deltaY = 0;
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) _myView.getLayoutParams();
        // 更新LayoutParams中的leftMargin和topMargin
        params.leftMargin = (int)deltaX;
        params.topMargin = (int)deltaY;
        // 应用新的LayoutParams
        _myView.setLayoutParams(params);
        // 儲存位置
        UserSettings.setFloatingLocation(deltaX, deltaY);

    }

    // 指定按鈕動作和文字 btnSetting
    public void setOnClickListenerSetting(OnClickListener listener) {
        _btnSetting.setOnClickListener(listener);
    }
    public void setTextSetting(String text) {
        _btnSetting.setText(text);
    }
    // 指定按鈕動作和文字 btn1
    public void setOnClickListener1(OnClickListener listener) {
        _btn1.setOnClickListener(listener);
    }
    public void setOnLongClickListener1(OnLongClickListener listener) {
        _btn1.setOnLongClickListener(listener);
    }
    public void setText1(String text) {
        _btn1.setText(text);
    }
    // 指定按鈕動作和文字 btn2
    public void setOnClickListener2(OnClickListener listener) {
        _btn2.setOnClickListener(listener);
    }
    public void setOnLongClickListener2(OnLongClickListener listener) {
        _btn2.setOnLongClickListener(listener);
    }
    public void setText2(String text) {
        _btn2.setText(text);
    }

    // 指定layout顯示
    public void setVisibility(int visibility) {
        _myView.setVisibility(visibility);
    }

    // 旋轉或變彈出視窗時, 將工具列回到右方預設位置
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        float screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        float screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        updateLayout(screenWidth, screenHeight/2 ,false);

        super.onConfigurationChanged(newConfig);
    }

    private void startInvisible() {
        if (timer != null)
            timer.cancel();

        timer = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                _myView.setAlpha(_alpha);
            }
        };
        timer.schedule(task1, (int)_idle* 1000L);
        TempSettings.isFloatingInvisible = true;
    }
    private void cancelInvisible() {
        if (timer != null)
            timer.cancel();
        _myView.setAlpha(1);
        TempSettings.isFloatingInvisible = false;
    }
}
