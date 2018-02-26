package com.hdl.ruler;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.hdl.elog.ELog;
import com.hdl.ruler.bean.OnBarMoveListener;
import com.hdl.ruler.utils.CUtils;
import com.hdl.ruler.utils.DateUtils;

import java.util.Timer;
import java.util.TimerTask;


/**
 * 视频时间刻度尺
 * Created by HDL on 2018.2.23
 *
 * @function 刻度尺
 */
public class RulerView extends RecyclerView {
    private Context context;
    /**
     * 一天的时间
     */
//    private final int ONEDAY_TIME = 24 * 60 * 60 * 1000;
    /**
     * 当前时间的毫秒值
     */
    private long currentTimeMillis;
    /**
     * 滑动结果回调
     */
    private OnBarMoveListener onBarMoveListener;
    /**
     * 线性布局
     */
    private LinearLayoutManager manager;
    /**
     * 屏幕的宽度
     */
    private int mScreenWidth = 0;
    /**
     * 第一个可见item的位置
     */
    private int firstVisableItemPosition = 0;
    /**
     * 中心点距离左边所占用的时长
     */
    private int centerPointDuration;
    /**
     * 中轴线画笔
     */
    private Paint centerLinePaint = new Paint();
    private int centerLineColor = 0xff6e9fff;//中轴线画笔颜色
    private int centerLineWidth = CUtils.dip2px(2);
    /**
     * 调用setCurrentTimeMillis时的时间（由于currentTimeMillis随时都在变，需要记录设置时的时间来计算是否超出当天的时间）
     */
    private long startTimeMillis;
    /**
     * 两小时
     */
    private static final int TWO_HOUR = 2 * 60 * 60 * 1000;
    /**
     * 是否是自动滑动的
     */
    private boolean isAutoScroll = true;

    /**
     * 左边屏幕的时刻
     */
    private long leftTime;
    private RulerAdapter adapter;

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
//        mScroller = new ScaleScroller(getContext(), this);
        if (!isInEditMode()) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            ta.recycle();
            init(context);
        }
    }

    private boolean isDouble;
    private int lastX;
    private boolean isCanScroll;
    private float beforeLength, afterLenght, mScale;

    private void init(final Context context) {
        initPaint();
        manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(manager);
        adapter = new RulerAdapter(context);
        setAdapter(adapter);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
        //中心点距离左边所占用的时长
        centerPointDuration = (int) ((mScreenWidth / 2f) / (((320.0 + zoom) / (10 * 60 * 1000))));
        addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isAutoScroll) {
                    isAutoScroll = false;
                    return;
                }
                View firstVisibleItem = manager.findViewByPosition(manager.findFirstVisibleItemPosition());
                firstVisableItemPosition = manager.findFirstVisibleItemPosition();
                //获取左屏幕的偏移量
                int leftScrollXCalculated = (int) (Math.abs(firstVisibleItem.getLeft()) + firstVisableItemPosition * (320 + zoom));
                currentTimeMillis = (long) (DateUtils.getTodayStart(startTimeMillis) + leftScrollXCalculated / ((320.0 + zoom) / (10 * 60 * 1000)) + centerPointDuration) - TWO_HOUR;
                //实时回调拖动时间
                if (onBarMoveListener != null) {
                    onBarMoveListener.onBarMoving(currentTimeMillis);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {//滑动结束
                    //1、滑动结束时，判断是否是当天的时间，如果不是则需要回退到零界点（00:00:00,23:59:59）
                    ELog.e("currentTimeMillis = " + DateUtils.getDateTime(currentTimeMillis));
                    isAutoScroll = true;
                    if (currentTimeMillis < DateUtils.getTodayStart(startTimeMillis)) {
                        ELog.e("上一天了");
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onMoveExceedStartTime();
                        }
                        Toast.makeText(context, "上一天", Toast.LENGTH_SHORT).show();
                        setCurrentTimeMillis(DateUtils.getTodayStart(startTimeMillis));
                        toTodayStartPostion();
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onBarMoveFinish(DateUtils.getTodayStart(startTimeMillis));
                        }
                    } else if (currentTimeMillis > DateUtils.getTodayEnd(startTimeMillis)) {
                        ELog.e("下一天了 ");
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onMoveExceedEndTime();
                        }
                        setCurrentTimeMillis(DateUtils.getTodayEnd(startTimeMillis));
                        toTodayEndPostion();
                        Toast.makeText(context, "下一天", Toast.LENGTH_SHORT).show();
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onBarMoveFinish(DateUtils.getTodayEnd(startTimeMillis));
                        }
                    } else {
                        ELog.e("当天");
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onBarMoveFinish(currentTimeMillis);
                        }
                    }

                } else {//开始滑动
                    stopMove();
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    ELog.e("单指按下");
                    isDouble = false;
                    lastX = (int) event.getX();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (event.getPointerCount() == 2 && isDouble) {
//                        ELog.e("双指移动");
                        isCanScroll = false;//不能在拖动
                        afterLenght = getDistance(event);// 获取两点的距离
                        if (beforeLength == 0) {
                            beforeLength = afterLenght;
                        }
                        float gapLenght = afterLenght - beforeLength;// 变化的长度
                        if (Math.abs(gapLenght) > 5f) {
                            mScale = afterLenght / beforeLength;// 求的缩放的比例
//                    listener.onZoom(mScale, time);
//                            ELog.e("双指缩放了mScale = " + mScale);
                            if (mScale > 1) {
//                                zoom += CUtils.dip2px(2);
                                zoom +=10;
                            } else {
                                zoom -=10;
                            }
                            isAutoScroll = false;
                            centerPointDuration = (int) ((mScreenWidth / 2f) / (((320.0 + zoom) / (10 * 60 * 1000))));
                            adapter.setZoom(zoom);
                            setCurrentTimeMillis(lastTimeMillis);
                            beforeLength = afterLenght;
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isDouble) {
                        isAutoScroll = false;
                        ELog.e("双指抬起");
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                isCanScroll = true;//1秒之后才能继续拖动
                            }
                        }, 500);
//                listener.onZoomFinished();
                    }
                } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                    if (event.getPointerCount() == 2) {
//                        ELog.e("双指按下");
                        lastTimeMillis=getCurrentTimeMillis();
                        beforeLength = getDistance(event);
                        isDouble = true;
                        isAutoScroll = false;
                        return true;
                    }
                }
                return false;
            }
        });
    }
    private long lastTimeMillis;
    /**
     * 刻度缩放值
     */
    private float zoom;
    /**
     * 跳转到今天的开始时间
     */
    private void toTodayStartPostion() {
        //计算偏移量
        int offset = getOffsetByDuration(centerPointDuration);
        manager.scrollToPositionWithOffset(2 * 6, offset);
    }

    /**
     * 跳转到今天的开始时间
     */
    private void toTodayEndPostion() {
        //计算偏移量
        int offset = getOffsetByDuration(centerPointDuration);
        manager.scrollToPositionWithOffset((2 + 24) * 6, offset);
    }

    /**
     * 根据时长计算偏移量
     *
     * @param duration
     * @return
     */
    private int getOffsetByDuration(long duration) {
        return (int) (((320f + zoom) / (10 * 60 * 1000)) * DateUtils.getMinuteMillisecond(duration));
    }

    /**
     * 计算两点的距离
     **/
    private float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 设置当前时间
     *
     * @param currentTimeMillis
     */
    public synchronized void setCurrentTimeMillis(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
        startTimeMillis = currentTimeMillis;
        ELog.e(" setCurrentTimeMillis = " + DateUtils.getDateTime(currentTimeMillis));
        updateCenteLinePostion();
    }


    /**
     * 更新中心点的位置
     */
    public void updateCenteLinePostion() {
        //左边屏幕的时刻
        leftTime = this.currentTimeMillis - centerPointDuration;
        //根据左边时间计算第一个可以显示的下标
        int leftTimeIndex = DateUtils.getHour(leftTime) * 6 + DateUtils.getMinute(leftTime) / 10 + 2 * 6;
        //计算偏移量
        int offset = (int) (((320f + zoom) / (10 * 60 * 1000)) * DateUtils.getMinuteMillisecond(leftTime));
        //滑动到指定的item并设置偏移量(offset不能超过320px)
        manager.scrollToPositionWithOffset(leftTimeIndex, (int) (-offset % (320 + zoom)));
    }

    /**
     * 刻度尺移动定时器
     */
    private Timer moveTimer;

    /**
     * 开始移动
     */
    public void startMove() {
        isAutoScroll = true;
        if (moveTimer != null) {
            moveTimer.cancel();
            moveTimer = null;
        }
        moveTimer = new Timer();
        moveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //必须在主线程中更新ui
                post(new Runnable() {
                    @Override
                    public void run() {
                        isAutoScroll = true;
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onBarMoving(currentTimeMillis);
                        }
                        currentTimeMillis += 1000;
                        ELog.e("currentTimeMillis = " + currentTimeMillis);
                        ELog.e("当前时间：" + DateUtils.getDateTime(currentTimeMillis));
                        updateCenteLinePostion();
//                        setCurrentTimeMillis(currentTimeMillis);
                    }
                });
            }
        }, 0, 1000);
    }

    /**
     * 结束移动
     */
    public void stopMove() {
        isAutoScroll = true;
        if (moveTimer != null) {
            moveTimer.cancel();
        }
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setStrokeWidth(centerLineWidth);
        centerLinePaint.setColor(centerLineColor);
    }

    /**
     * 画中心线
     *
     * @param c
     */
    @Override
    public void draw(Canvas c) {
        super.draw(c);
        drawCenterLine(c);
    }


    /**
     * 画中间线
     *
     * @param canvas
     */
    private void drawCenterLine(Canvas canvas) {
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, CUtils.dip2px(120), centerLinePaint);
    }

    /**
     * 设置移动监听
     *
     * @param onBarMoveListener
     */
    public void setOnBarMoveListener(OnBarMoveListener onBarMoveListener) {
        this.onBarMoveListener = onBarMoveListener;
    }

    /**
     * 拿到当前时间
     *
     * @return
     */
    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }
}
