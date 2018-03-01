package com.hdl.ruler.bean;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Scroller;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 辅助滑动监听类
 */
public class ScaleScroller {

    private Context context;
    private GestureDetector gestureDetector; //滑动手势
    private Scroller scroller; //滑动辅助类
    private ScrollingListener listener;
    private static int lastX;
    private final int ON_FLING = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean isFinished = scroller.computeScrollOffset();
            int curX = scroller.getCurrX();
            lastX = curX;
            if (isFinished)
                handler.sendEmptyMessage(ON_FLING);
            else
                listener.onScrollFinished();
        }
    };

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isDouble == false) {
                final int minX = -0x7fffffff;
                final int maxX = 0x7fffffff;
                lastX = 0;
                scroller.fling(0, 0, (int) -velocityX, 0, minX, maxX, 0, 0);
                handler.sendEmptyMessage(ON_FLING);
            }
            return false;
        }
    };

    public ScaleScroller(Context context, ScrollingListener listener) {
        this.context = context;
        this.listener = listener;
        init();
    }

    private void init() {
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
        gestureDetector.setIsLongpressEnabled(false);
        scroller = new Scroller(context);
    }

    private float beforeLength, afterLenght, mScale;
    private boolean isDouble = false;
    private double time;
    private float lastDistanceX;
    private boolean isCanScroll = true;//是否可以拖动--->刚缩放完成1秒内不能拖动（防止时间抖动）

    //由外部传入event事件
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isDouble = false;
            scroller.forceFinished(true);
            lastX = (int) event.getX();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (event.getPointerCount() == 1 && !isDouble && isCanScroll) {
                int distanceX = (int) (event.getX() - lastX);
                if (distanceX != 0) {
                    if (Math.abs(Math.abs(distanceX) - Math.abs(lastDistanceX)) <150) {//防止快速滑动导致数据跳远过大
                        listener.onScroll(distanceX);
                        lastX = (int) event.getX();
                        lastDistanceX = distanceX;
                    }
                }
            } else if (event.getPointerCount() == 2 && isDouble) {
                isCanScroll = false;//不能在拖动
                afterLenght = getDistance(event);// 获取两点的距离
                if (beforeLength == 0) {
                    beforeLength = afterLenght;
                }
                float gapLenght = afterLenght - beforeLength;// 变化的长度
                if (Math.abs(gapLenght) > 5f) {
                    mScale = afterLenght / beforeLength;// 求的缩放的比例
                    listener.onZoom(mScale, time);
                    beforeLength = afterLenght;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getPointerCount() == 1 && !isDouble) {
                listener.onScrollFinished();
            } else if (isDouble) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isCanScroll = true;//1秒之后才能继续拖动
                    }
                }, 500);
                listener.onZoomFinished();
            }
            return false;
        } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
            if (event.getPointerCount() == 2) {
                beforeLength = getDistance(event);
                isDouble = true;
            }
        }
        gestureDetector.onTouchEvent(event);
        return false;
    }

    /**
     * 计算两点的距离
     **/
    private float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    public interface ScrollingListener {
        /**
         * 滑动时
         *
         * @param distance
         */
        void onScroll(int distance);

        /**
         * 缩放结束
         */
        void onZoomFinished();

        /**
         * 滑动结束
         */
        void onScrollFinished();

        /**
         * 缩放时
         *
         * @param mScale
         * @param time
         */
        void onZoom(float mScale, double time);
    }

}
