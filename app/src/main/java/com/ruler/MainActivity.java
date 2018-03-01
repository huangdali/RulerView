package com.ruler;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hdl.ruler.RulerView;
import com.hdl.ruler.TipView;
import com.hdl.ruler.bean.OnBarMoveListener;
import com.hdl.ruler.bean.OnSelectedTimeListener;
import com.hdl.ruler.bean.TimeSlot;
import com.hdl.ruler.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private TextView tvSelectTime;
    private RulerView rulerView;
    private long currentTimeMillis;
    private TipView tvTip;
    private RelativeLayout rlLanspceContent;
    private FrameLayout fl_ruler_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textview);
        tvTip = findViewById(R.id.tv_tip);
        rlLanspceContent = findViewById(R.id.rl_lanspace_ruler_content);
        fl_ruler_content = findViewById(R.id.fl_ruler_content);
        tvSelectTime = findViewById(R.id.tv_select_time);
        initRuler();
    }

    private void initRuler() {
        rulerView = findViewById(R.id.ruler_view);
        rulerView.setCurrentTimeMillis(System.currentTimeMillis());
        rulerView.setOnSelectedTimeListener(new OnSelectedTimeListener() {
            @Override
            public void onDragging(long startTime, long endTime) {
                tvSelectTime.setText(DateUtils.getDateTime(startTime) + " --- " + DateUtils.getDateTime(endTime));
            }

            @Override
            public void onMaxTime() {
                Toast.makeText(MainActivity.this, "不能超过10分钟", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMinTime() {
                Toast.makeText(MainActivity.this, "不能低于一分钟", Toast.LENGTH_SHORT).show();
            }
        });
//        rulerView.openMove();
        rulerView.setOnBarMoveListener(new OnBarMoveListener() {
            @Override
            public void onDragBar(boolean isLeftDrag, long currentTime) {
                currentTimeMillis = currentTime;
                tv.setText(DateUtils.getDateTime(currentTime));
            }

            @Override
            public void onBarMoving(long currentTime) {
                currentTimeMillis = currentTime;
                tv.setText(DateUtils.getDateTime(currentTime));
            }

            @Override
            public void onBarMoveFinish(long currentTime) {
                currentTimeMillis = currentTime;
                tv.setText(DateUtils.getDateTime(currentTime));
            }

            @Override
            public void onMoveExceedStartTime() {

            }

            @Override
            public void onMoveExceedEndTime() {

            }

            @Override
            public void onMaxScale() {

            }

            @Override
            public void onMinScale() {

            }
        });
        List<TimeSlot> data = new ArrayList<>();
//        data.add(new TimeSlot(DateUtils.getTodayStart(System.currentTimeMillis()), System.currentTimeMillis() - 60 * 60 * 1000, System.currentTimeMillis()));
//        data.add(new TimeSlot(DateUtils.getTodayStart(System.currentTimeMillis()), DateUtils.getTodayStart(System.currentTimeMillis()) + 11* 60 * 60 * 1000, DateUtils.getTodayStart(System.currentTimeMillis()) + 14 * 60 * 60 * 1000 + 26 * 60 * 1000));
//        data.add(new TimeSlot(DateUtils.getTodayStart(System.currentTimeMillis()), DateUtils.getTodayStart(System.currentTimeMillis()) + 15 * 60 * 60 * 1000 + 60 * 1000, DateUtils.getTodayStart(System.currentTimeMillis()) + 15 * 60 * 60 * 1000 + 16 * 60 * 1000));
//        data.add(new TimeSlot(DateUtils.getTodayStart(System.currentTimeMillis()), DateUtils.getTodayStart(System.currentTimeMillis()) + 16 * 60 * 60 * 1000 + 60 * 1000, DateUtils.getTodayStart(System.currentTimeMillis()) + 16 * 60 * 60 * 1000 + 5* 60 * 1000));
//        data.add(new TimeSlot(DateUtils.getTodayStart(System.currentTimeMillis()), DateUtils.getTodayStart(System.currentTimeMillis()) - 5 * 60 * 1000, DateUtils.getTodayEnd(System.currentTimeMillis()) + 15 * 60 * 1000));
//        ELog.e(data);
        rulerView.setVedioTimeSlot(data);
    }

    public void onSetAdd(View view) {
//        rulerView.setCurrentTimeMillis(System.currentTimeMillis());
        rulerView.openMove();
    }

    public void onPuaseMove(View view) {
        rulerView.closeMove();
    }

    public void onStartMove(View view) {
        rulerView.openMove();
    }

    public void onCurrentTime(View view) {
        rulerView.setCurrentTimeMillis(System.currentTimeMillis());
        rulerView.openMove();
    }

    boolean isOpen;

    public void onScollSwitch(View view) {
        rulerView.setIsCanScrollBar(isOpen);
        isOpen = !isOpen;
    }

    public void onShowOrHideRight(View view) {
        tvTip.setShowRightTip(true);
    }

    public void onShowOrHideLeft(View view) {
        tvTip.setShowLeftTip(true);
    }

    public void onShowOrHideLeftLandscape(View view) {
        tvTip.setShowLeftTipLandscape(true);
    }

    public void onShowOrHideRightLandscape(View view) {
        tvTip.setShowRightTipLandscape(true);
    }

    boolean isSelectTimeArea = false;

    public void onTimeSelected(View view) {
        isSelectTimeArea = !isSelectTimeArea;
        rulerView.setSelectTimeArea(isSelectTimeArea);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fl_ruler_content.removeAllViews();
            fl_ruler_content.setVisibility(View.GONE);
            rlLanspceContent.setVisibility(View.VISIBLE);
            rlLanspceContent.addView(rulerView);
        } else {
            rlLanspceContent.removeAllViews();
            rlLanspceContent.setVisibility(View.GONE);
            fl_ruler_content.setVisibility(View.VISIBLE);
            fl_ruler_content.addView(rulerView);
        }
    }
}
