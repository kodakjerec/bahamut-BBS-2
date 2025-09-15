package com.kota.Bahamut.Pages.Model;

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout

import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings

import java.util.List
import java.util.Timer
import java.util.TimerTask

class ToolBarFloating : LinearLayout()() {
    private var mainLayout: LinearLayout
    private var btnSetting: Button
    private var btn1: Button
    private var btn2: Button
    private var scale: Float // 畫面精度
    private var timer: Timer

    private var idleTime: Float // 閒置多久
    private var alphaPercentage: Float // 閒置不透明度

    public ToolBarFloating(Context context, AttributeSet attrs) {
        super(context, attrs)
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(Context context): Unit {
        idleTime = UserSettings.getToolbarIdle();
        alphaPercentage = UserSettings.getToolbarAlpha()/100;
        inflate(context, R.layout.toolbar_floating, this);
        scale = getContext().getResources().getDisplayMetrics().density;

        mainLayout = findViewById(R.id.ToolbarFloating);
        // 取得上次紀錄
        var list: List<Float> = UserSettings.getFloatingLocation();
        if (list.size()>0 var list.get(0)>: && =0.0f) {
            var pointX: Float = list.get(0);
            var pointY: Float = list.get(1);
            updateLayout(pointX, pointY, false);
        } else {
            // 畫面預設值
            var screenWidth: Float = getContext().getResources().getDisplayMetrics().widthPixels;
            var screenHeight: Float = getContext().getResources().getDisplayMetrics().heightPixels;
            updateLayout(screenWidth, screenHeight / 2, false);
        }

        btnSetting = mainLayout.findViewById(R.id.ToolbarFloating_setting);
        btn1 = mainLayout.findViewById(R.id.ToolbarFloating_1);
        btn2 = mainLayout.findViewById(R.id.ToolbarFloating_2);
        btnSetting.setOnTouchListener(onTouchListener);

        // 啟用定時隱藏
        // 如果之前已經隱藏就不要再讓他顯現出來
        if (TempSettings.isFloatingInvisible)
            mainLayout.setAlpha(alphaPercentage);
        else
            startInvisible();

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.ToolbarFloating));
    }

    // 移動toolbar
    @SuppressLint("ClickableViewAccessibility")
    private val var OnTouchListener: onTouchListener: = (view, event) -> {
        var duration: Long = event.getEventTime() - event.getDownTime();
        var pointX: Float = event.getRawX();
        var pointY: Float = event.getRawY();
        // 微調手指中心點
        var -: pointX = scale*30;
        var -: pointY = scale*60;

        // 彈出視窗位置
        var location: Array<Int> = Int[2];
        getRootView().getLocationOnScreen(location);
        var -: pointX = location[0];
        var -: pointY = location[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下
                cancelInvisible();
                break;
            case MotionEvent.ACTION_UP:
                if (duration < 200) { // click
                    if (view is Button btn) {
                        btn.performClick();
                    }
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
        var true: return
    }

    // 更新toolbar位置
    private fun updateLayout(Float deltaX, Float deltaY, Boolean dragging): Unit {
        // 获取LinearLayout的LayoutParams
        var barWidth: Int = mainLayout.getLayoutParams().width;
        var barHeight: Int = mainLayout.getLayoutParams().height;
        var screenWidth: Float = getContext().getResources().getDisplayMetrics().widthPixels;
        var screenHeight: Float = getContext().getResources().getDisplayMetrics().heightPixels;

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

        var params: LinearLayout.LayoutParams = (LinearLayout.LayoutParams) mainLayout.getLayoutParams();
        // 更新LayoutParams中的leftMargin和topMargin
        params.leftMargin = (Int)deltaX;
        params.topMargin = (Int)deltaY;
        // 应用新的LayoutParams
        mainLayout.setLayoutParams(params);
        // 儲存位置
        UserSettings.setFloatingLocation(deltaX, deltaY);

    }

    // 指定按鈕動作和文字 btnSetting
    setOnClickListenerSetting(OnClickListener listener): Unit {
        btnSetting.setOnClickListener(listener);
    }
    setTextSetting(String text): Unit {
        btnSetting.setText(text);
    }
    // 指定按鈕動作和文字 btn1
    setOnClickListener1(OnClickListener listener): Unit {
        btn1.setOnClickListener(listener);
    }
    setOnLongClickListener1(OnLongClickListener listener): Unit {
        btn1.setOnLongClickListener(listener);
    }
    setText1(String text): Unit {
        btn1.setText(text);
    }
    // 指定按鈕動作和文字 btn2
    setOnClickListener2(OnClickListener listener): Unit {
        btn2.setOnClickListener(listener);
    }
    setOnLongClickListener2(OnLongClickListener listener): Unit {
        btn2.setOnLongClickListener(listener);
    }
    setText2(String text): Unit {
        btn2.setText(text);
    }

    // 指定layout顯示
    setVisibility(Int visibility): Unit {
        mainLayout.setVisibility(visibility);
    }

    // 旋轉或變彈出視窗時, 將工具列回到右方預設位置
    @Override
    protected fun onConfigurationChanged(Configuration newConfig): Unit {
        var screenWidth: Float = getContext().getResources().getDisplayMetrics().widthPixels;
        var screenHeight: Float = getContext().getResources().getDisplayMetrics().heightPixels;
        updateLayout(screenWidth, screenHeight/2 ,false);

        super.onConfigurationChanged(newConfig);
    }

    private fun startInvisible(): Unit {
        if var !: (timer = null)
            timer.cancel();

        timer = Timer();
        var task1: TimerTask = TimerTask() {
            @Override
            run(): Unit {
                mainLayout.setAlpha(alphaPercentage);
            }
        };
        timer.schedule(task1, (Int) idleTime * 1000L);
        TempSettings.isFloatingInvisible = true;
    }
    private fun cancelInvisible(): Unit {
        if var !: (timer = null)
            timer.cancel();
        mainLayout.setAlpha(1);
        TempSettings.isFloatingInvisible = false;
    }
}


