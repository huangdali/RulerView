package com.ruler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.hdl.ruler.RulerView;
import com.hdl.ruler.bean.OnBarMoveListener;
import com.hdl.ruler.utils.DateUtils;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private RulerView rulerView;
    private long currentTimeMillis;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textview);
        rulerView = findViewById(R.id.ruler_view);
        rulerView.setOnBarMoveListener(new OnBarMoveListener() {
            @Override
            public void onDragBar(boolean isLeftDrag, long currentTime) {

            }

            @Override
            public void onBarMoving(long currentTime) {
                currentTimeMillis=currentTime;
                tv.setText(DateUtils.getDateTime(currentTime));
            }

            @Override
            public void onBarMoveFinish(long currentTime) {

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
    }

    public void onSetAdd(View view) {
        rulerView.setCurrentTimeMillis(System.currentTimeMillis());
        rulerView.startMove();
    }

    public void onPuaseMove(View view) {
        rulerView.stopMove();
    }

    public void onStartMove(View view) {
        rulerView.startMove();
    }
}
