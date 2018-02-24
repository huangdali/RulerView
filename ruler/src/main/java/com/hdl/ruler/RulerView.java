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
import android.view.View;
import android.view.WindowManager;

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
     * 上一次滑动结束的时间
     */
    private long lastScrolledTimeMillis = 0;

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (!isInEditMode()) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            ta.recycle();
            init(context);
        }
    }

    private void init(final Context context) {
        initPaint();
        manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(manager);
        RulerAdapter adapter = new RulerAdapter(context);
        setAdapter(adapter);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
        //中心点距离左边所占用的时长
        centerPointDuration = (int) ((mScreenWidth / 2f) / ((320.0 / (10 * 60 * 1000))));
        //calculate value on current device
        addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View firstVisibleItem = manager.findViewByPosition(manager.findFirstVisibleItemPosition());
                int leftScrollXCalculated = 0;
                firstVisableItemPosition = manager.findFirstVisibleItemPosition();
                //获取左边的屏幕的偏移量
                leftScrollXCalculated += Math.abs(firstVisibleItem.getLeft()) + firstVisableItemPosition * 320;
                ELog.e("hdl", "leftScrollXCalculated = " + leftScrollXCalculated);
                //实时回调拖动时间
                if (onBarMoveListener != null) {
                    lastScrolledTimeMillis = (long) (DateUtils.getTodayStart(currentTimeMillis) + leftScrollXCalculated / (320.0 / (10 * 60 * 1000)) + centerPointDuration)- 2 * 60 * 60 * 1000;
                    onBarMoveListener.onBarMoving(lastScrolledTimeMillis );
                }
                ELog.e("lastScrolledTimeMillis  = " + DateUtils.getDateTime(lastScrolledTimeMillis));
                ELog.e("currentTimeMillis  = " + DateUtils.getDateTime(currentTimeMillis));
                if (lastScrolledTimeMillis < DateUtils.getTodayStart(currentTimeMillis)) {
                    ELog.e("上一天了");
//                    setNestedScrollingEnabled(true);
                } else if (lastScrolledTimeMillis > DateUtils.getTodayEnd(currentTimeMillis)) {
                    ELog.e("下一天了");
//                    setNestedScrollingEnabled(true);
                } else {
                    ELog.e("当天");
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {//滑动结束了
//                    startMove();
//                    currentTimeMillis = lastScrolledTimeMillis;
                    if (lastScrolledTimeMillis > DateUtils.getTodayStart(currentTimeMillis) || lastScrolledTimeMillis < DateUtils.getTodayEnd(currentTimeMillis)) {
                        //当天范围才设置当前时间
                        setCurrentTimeMillis(lastScrolledTimeMillis);
                    }
                    if (onBarMoveListener != null) {
                        onBarMoveListener.onBarMoveFinish(lastScrolledTimeMillis);
                    }
                } else {//开始滑动
                    stopMove();
                }
            }
        });
        //默认当前时间
        setCurrentTimeMillis(System.currentTimeMillis());
    }

    /**
     * 左边屏幕所占用的时刻
     */
    private long leftTime;

    /**
     * 设置当前时间
     *
     * @param currentTimeMillis
     */
    public synchronized void setCurrentTimeMillis(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
        leftTime = this.currentTimeMillis - centerPointDuration;
        int leftTimeIndex = DateUtils.getHour(leftTime) * 6 + DateUtils.getMinute(leftTime) / 10 + 2 * 6;
        //计算偏移量
        int offset = (int) ((320f / (10 * 60 * 1000)) * DateUtils.getMinuteMillisecond(leftTime));
        //滑动到指定的item并设置偏移量(offset不能超过320px)
        manager.scrollToPositionWithOffset(leftTimeIndex, -offset % 320);
    }

    /**
     * 刻度尺移动定时器
     */
    private Timer moveTimer;

    /**
     * 开始移动
     */
    public void startMove() {
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
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onBarMoving(getCurrentTimeMillis());
                        }
                        currentTimeMillis += 1000;
                        setCurrentTimeMillis(currentTimeMillis);
                    }
                });
            }
        }, 0, 1000);
    }

    /**
     * 结束移动
     */
    public void stopMove() {
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
        return currentTimeMillis - 2 * 60 * 60 * 1000;
    }
}
