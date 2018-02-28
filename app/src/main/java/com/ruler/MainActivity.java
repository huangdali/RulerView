package com.ruler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.hdl.elog.ELog;
import com.hdl.ruler.RulerView;
import com.hdl.ruler.TipView;
import com.hdl.ruler.bean.OnBarMoveListener;
import com.hdl.ruler.bean.TimeSlot;
import com.hdl.ruler.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private RulerView rulerView;
    private long currentTimeMillis;
    private TipView tvTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textview);
        rulerView = findViewById(R.id.ruler_view);
        tvTip = findViewById(R.id.tv_tip);
        rulerView.setCurrentTimeMillis(System.currentTimeMillis());

//        rulerView.startMove();
        rulerView.setOnBarMoveListener(new OnBarMoveListener() {
            @Override
            public void onDragBar(boolean isLeftDrag, long currentTime) {

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
        data.add(new TimeSlot(DateUtils.getTodayStart(System.currentTimeMillis()), DateUtils.getTodayStart(System.currentTimeMillis()) - 5 * 60 * 1000, DateUtils.getTodayEnd(System.currentTimeMillis()) + 15 * 60 * 1000));
        ELog.e(data);
        rulerView.setVedioTimeSlot(data);
    }

    public void onSetAdd(View view) {
//        rulerView.setCurrentTimeMillis(System.currentTimeMillis());
        rulerView.startMove();
    }

    public void onPuaseMove(View view) {
        rulerView.stopMove();
    }

    public void onStartMove(View view) {
        rulerView.startMove();
    }

    public void onCurrentTime(View view) {
        rulerView.setCurrentTimeMillis(System.currentTimeMillis());
        rulerView.startMove();
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
}
