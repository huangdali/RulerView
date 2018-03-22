package com.hdl.ruler;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.hdl.ruler.bean.OnBarMoveListener;
import com.hdl.ruler.bean.OnSelectedTimeListener;
import com.hdl.ruler.bean.ScaleMode;
import com.hdl.ruler.bean.TimeSlot;
import com.hdl.ruler.utils.CUtils;
import com.hdl.ruler.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
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
    private MyLinearLayoutManager manager;
    /**
     * 屏幕的宽度
     */
    private int mScreenWidth = 0;
    /**
     * 屏幕的高度
     */
    private int mScreenHeight = 0;
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
     * 选择时间配置
     */
    private Paint selectAreaPaint = new Paint();//选择时间边框
    private int selectTimeBorderColor = 0xfffabb64;//边框颜色
    private Paint vedioArea = new Paint();//已选时间
    private int selectTimeAreaColor = 0x44fabb64;//已选时间颜色
    private float selectTimeStrokeWidth = CUtils.dip2px(8);
    /**
     * 视频区域画笔
     */
    private Paint vedioAreaPaint = new Paint();
    private int vedioBg = 0x336e9fff;//视频背景颜色
    private RectF vedioAreaRect = new RectF();
    /**
     * 调用setCurrentTimeMillis时的时间（由于currentTimeMillis随时都在变，需要记录设置时的时间来计算是否超出当天的时间）
     */
    private long startTimeMillis;
    /**
     * 两小时
     */
    private static final int TWO_HOUR = 12 * 60 * 60 * 1000;
    /**
     * 是否是自动滑动的
     */
    private boolean isAutoScroll = true;

    /**
     * 左边屏幕的时刻
     */
    private long leftTime;
    /**
     * 适配器
     */
    private RulerAdapter adapter;
    /**
     * 缩放模式
     */
    private ScaleMode scaleMode = ScaleMode.KEY_MINUTE;

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
//        setNestedScrollingEnabled(false);
//        mScroller = new ScaleScroller(getContext(), this);
        if (!isInEditMode()) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            ta.recycle();
            init(context);
        }
    }

    private boolean isDouble;
    private float beforeLength, afterLenght, mScale;

    private class MyLinearLayoutManager extends LinearLayoutManager {
        private boolean iscanScrollHorizontally = true;

        public MyLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollHorizontally() {
            return iscanScrollHorizontally;
        }

        public void setIscanScrollHorizontally(boolean iscanScrollHorizontally) {
            this.iscanScrollHorizontally = iscanScrollHorizontally;
        }
    }

    /**
     * 设置是否可以滑动
     *
     * @param isCanScrollBar
     */
    public void setIsCanScrollBar(boolean isCanScrollBar) {
        if (manager != null) {
            manager.setIscanScrollHorizontally(isCanScrollBar);
        }
    }

    private void init(final Context context) {
        initPaint();
        manager = new MyLinearLayoutManager(context);
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
        mScreenHeight = displaymetrics.heightPixels;
//        ELog.e("mScreenWidth = " + mScreenWidth);
        //中心点距离左边所占用的时长
        centerPointDuration = (int) ((mScreenWidth / 2f) / (((320.0 + zoom) / (10 * 60 * 1000))));
        addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isAutoScroll) {
                    isAutoScroll = false;
                    return;
                }
                View firstVisibleItem = manager.findViewByPosition(manager.findFirstVisibleItemPosition());
                if (firstVisibleItem == null) {
                    return;
                }
                firstVisableItemPosition = manager.findFirstVisibleItemPosition();
                //获取左屏幕的偏移量
                int leftScrollXCalculated = (int) (Math.abs(firstVisibleItem.getLeft()) + firstVisableItemPosition * (320 + zoom));
                currentTimeMillis = (long) (DateUtils.getTodayStart(startTimeMillis) + leftScrollXCalculated / ((320.0 + zoom) / (10 * 60 * 1000)) + centerPointDuration) - TWO_HOUR;
                //实时回调拖动时间
                if (onBarMoveListener != null) {
//                    onBarMoveListener.onBarMoving(currentTimeMillis);
                    onBarMoveListener.onDragBar(dx > 0, currentTimeMillis);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {//滑动结束
                    //1、滑动结束时，判断是否是当天的时间，如果不是则需要回退到零界点（00:00:00,23:59:59）
                    isAutoScroll = true;
                    if (currentTimeMillis < DateUtils.getTodayStart(startTimeMillis)) {
                        //上一天
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onMoveExceedStartTime();
                        }
                        setCurrentTimeMillis(DateUtils.getTodayStart(startTimeMillis));
                        toTodayStartPostion();
//                        if (onBarMoveListener != null) {
//                            onBarMoveListener.onBarMoveFinish(DateUtils.getTodayStart(startTimeMillis));
//                        }

                    } else if (currentTimeMillis > DateUtils.getTodayEnd(startTimeMillis)) {
                        //下一天了
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onMoveExceedEndTime();
                        }
                        setCurrentTimeMillis(DateUtils.getTodayEnd(startTimeMillis));
                        toTodayEndPostion();
//                        if (onBarMoveListener != null) {
//                            onBarMoveListener.onBarMoveFinish(DateUtils.getTodayEnd(startTimeMillis));
//                        }
                    } else {
                        //当天
                        if (onBarMoveListener != null) {
                            onBarMoveListener.onBarMoveFinish(currentTimeMillis);
                        }
                    }
                } else {//开始滑动
                    closeMove();
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isSelectTimeArea) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float curX = event.getX();//拿到当前的x轴
                            if (Math.abs(curX - selectTimeAreaDistanceLeft) < Math.abs(curX - selectTimeAreaDistanceRight)) {//左边
                                //左边滑动
                                //1-10分钟
                                float currentInterval = (selectTimeAreaDistanceRight - curX + selectTimeStrokeWidth) / ((320 + zoom) / (10 * 60 * 1000f));
                                if (selectTimeMin < currentInterval && currentInterval < selectTimeMax) {
                                    //可滑动范围内
                                    selectTimeAreaDistanceLeft = curX;
//                                   //实时地将结果回调出去
                                    if (onSelectedTimeListener != null) {
                                        onSelectedTimeListener.onDragging(getSelectStartTime(), getSelectEndTime());
                                    }
                                } else {
                                    //超过时间了
//                                    //实时地将结果回调出去
                                    if (currentInterval >= selectTimeMax) {
                                        onSelectedTimeListener.onMaxTime();
                                    } else if (currentInterval <= selectTimeMin) {
                                        onSelectedTimeListener.onMinTime();
                                    }
                                }
                            } else {//右边
                                //1-10分钟
                                //右边滑动
                                float currentInterval = (curX - (selectTimeAreaDistanceLeft + selectTimeStrokeWidth)) / ((320 + zoom) / (10 * 60 * 1000f));
                                if (selectTimeMin < currentInterval && currentInterval < selectTimeMax) {
                                    selectTimeAreaDistanceRight = curX;
//                                    //实时地将结果回调出去
                                    if (onSelectedTimeListener != null) {
                                        onSelectedTimeListener.onDragging(getSelectStartTime(), getSelectEndTime());
                                    }
                                } else {
                                    //超过时间了
//                                    //实时地将结果回调出去
                                    if (onSelectedTimeListener != null) {
                                        if (currentInterval >= selectTimeMax) {
                                            onSelectedTimeListener.onMaxTime();
                                        } else if (currentInterval <= selectTimeMin) {
                                            onSelectedTimeListener.onMinTime();
                                        }
                                    }
                                }
                            }
                            postInvalidate();
                            break;
                    }
                } else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        //单指按下
                        isDouble = false;
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (event.getPointerCount() == 2 && isDouble) {
                            //双指移动
                            afterLenght = getDistance(event);// 获取两点的距离
                            if (beforeLength == 0) {
                                beforeLength = afterLenght;
                            }
                            float gapLenght = afterLenght - beforeLength;// 变化的长度
                            if (Math.abs(gapLenght) > 5f) {
                                mScale = afterLenght / beforeLength;// 求的缩放的比例
                                //双指缩放了
                                beforeLength = afterLenght;
                                onZooming();
                            }
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (isDouble) {
                            isAutoScroll = false;
                            //双指抬起
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            setIsCanScrollBar(true);//双指抬起的时候，需要解除静止滑动
                                        }
                                    });
                                }
                            }, 100);
                        }
                    } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
                        if (event.getPointerCount() == 2) {
                            //双指按下
                            setIsCanScrollBar(false);//双指按下的时候，需要静止滑动
                            lastTimeMillis = getCurrentTimeMillis();
                            beforeLength = getDistance(event);
                            isDouble = true;
                            isAutoScroll = false;
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * 获取选择的结束时间(默认是当前时间的后两分半钟)
     *
     * @return
     */
    public long getSelectEndTime() {
        if (selectTimeAreaDistanceRight == -1) {
            return currentTimeMillis + 2 * 60 * 1000 + 30 * 1000;
        }
        return currentTimeMillis - (long) ((getWidth() / 2 - selectTimeAreaDistanceRight + selectTimeStrokeWidth / 2) / ((320 + zoom) / (10 * 60 * 1000f)));
    }

    /**
     * 获取选择的开始时间(默认是当前时间的前两分半钟)
     *
     * @return
     */
    public long getSelectStartTime() {
        if (selectTimeAreaDistanceLeft == -1) {
            return currentTimeMillis - 2 * 60 * 1000 - 30 * 1000;
        }
        return currentTimeMillis - (long) ((getWidth() / 2 - selectTimeAreaDistanceLeft - selectTimeStrokeWidth / 2) / ((320 + zoom) / (10 * 60 * 1000f)));
    }

    /**
     * 选择时间最小值，单位毫秒
     */
    private long selectTimeMin = 1 * 60 * 1000;
    /**
     * 选择时间最大值，单位毫秒
     */
    private long selectTimeMax = 10 * 60 * 1000;
    /**
     * 视频时间段集合
     */
    private List<TimeSlot> vedioTimeSlot = new ArrayList<>();

    /**
     * 获取视频时间段
     *
     * @return
     */
    public List<TimeSlot> getVedioTimeSlot() {
        return vedioTimeSlot;
    }

    /**
     * 设置视频时间段
     *
     * @param vedioTimeSlot
     */
    public void setVedioTimeSlot(List<TimeSlot> vedioTimeSlot) {
        this.vedioTimeSlot.clear();
        this.vedioTimeSlot.addAll(vedioTimeSlot);
//        postInvalidate();//重绘
        adapter.setVedioTimeSlot(vedioTimeSlot);
    }

    /**
     * 缩放中
     */
    private void onZooming() {
        if (mScale > 1) {
            zoom += 30;
        } else {
            zoom -= 30;
        }
        if (zoom < -320 / 2) {
            scaleMode = ScaleMode.KEY_HOUSE;
            //小时级别了
            if (Math.abs(320 + zoom) < 30) {//不能小于10dp
                // 强制设置为小时级别的
                zoom = -320 + 30;
                if (onBarMoveListener != null) {
                    onBarMoveListener.onMinScale();
                }
            }
        } else if (zoom < 320 * 1.5) {//不能超过1.5倍
            scaleMode = ScaleMode.KEY_MINUTE;
            isAutoScroll = false;
            //分钟级别了
        } else {
            //已经是最大刻度
            scaleMode = ScaleMode.KEY_MINUTE;
            zoom = 320 * 1.5f;
            if (onBarMoveListener != null) {
                onBarMoveListener.onMaxScale();
            }
        }
        centerPointDuration = (int) ((mScreenWidth / 2f) / (((320.0 + zoom) / (10 * 60 * 1000))));
        setCurrentTimeMillis(lastTimeMillis);
        adapter.setZoom(zoom);
        adapter.setScaleMode(scaleMode);
    }

    /**
     * 记录缩放前的时间
     */
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
        manager.scrollToPositionWithOffset(12 * 6, offset);
    }

    /**
     * 跳转到今天的开始时间
     */
    private void toTodayEndPostion() {
        //计算偏移量
        int offset = getOffsetByDuration(centerPointDuration);
        manager.scrollToPositionWithOffset((12 + 24) * 6, offset);
    }

    /**
     * 根据时长计算偏移量
     *
     * @param duration
     * @return
     */
    private int getOffsetByDuration(long duration) {
        return (int) (((320f + zoom) / (10 * 60 * 1000)) * duration);
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
        lastTimeMillis=currentTimeMillis;
        updateCenteLinePostion();
    }


    /**
     * 更新中心点的位置
     */
    public void updateCenteLinePostion() {
        //左边屏幕的时刻
        leftTime = this.currentTimeMillis - centerPointDuration;
        //根据左边时间计算第一个可以显示的下标
        int leftTimeIndex = DateUtils.getHour(leftTime) * 6 + DateUtils.getMinute(leftTime) / 10 + 12 * 6;
        if (leftTime < DateUtils.getTodayStart(currentTimeMillis)) {//跨天数了，减一天
//            Log.e("hdltag", "updateCenteLinePostion(RulerView.java:523):跨天数了，减一天");
            leftTimeIndex=leftTimeIndex - 24 * 6;
        }
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
    public void openMove() {
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
//                        ELog.e("currentTimeMillis = " + currentTimeMillis);
//                        ELog.e("当前时间：" + DateUtils.getDateTime(currentTimeMillis));
                        updateCenteLinePostion();
                    }
                });
            }
        }, 0, 1000);
    }

    /**
     * 结束移动
     */
    public void closeMove() {
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


        selectAreaPaint.setColor(selectTimeBorderColor);
        selectAreaPaint.setAntiAlias(true);
        selectAreaPaint.setStrokeCap(Paint.Cap.ROUND);
        selectAreaPaint.setStyle(Paint.Style.STROKE);
        selectAreaPaint.setStrokeWidth(selectTimeStrokeWidth);

        vedioArea.setColor(selectTimeAreaColor);
        vedioArea.setAntiAlias(true);

        vedioAreaPaint.setAntiAlias(true);
        vedioAreaPaint.setColor(vedioBg);
    }

    /**
     * 画中心线
     *
     * @param canvas
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawCenterLine(canvas);
        drawSelectTimeArea(canvas);
    }

    private void drawSelectTimeArea(Canvas canvas) {
        if (isSelectTimeArea) {
            if (selectTimeAreaDistanceLeft < 1) {
                selectTimeAreaDistanceLeft = getWidth() / 2f - 2.5f * 60 * 1000 * (((320f + zoom) / (10 * 60 * 1000f))) - selectTimeStrokeWidth / 2;
            }
            if (selectTimeAreaDistanceRight < 1) {
                selectTimeAreaDistanceRight = getWidth() / 2f + 2.5f * 60 * 1000 * (((320f + zoom) / (10 * 60 * 1000f))) + selectTimeStrokeWidth / 2;
            }
            //画左右两条选择视频的线
            selectAreaPaint.setStrokeWidth(selectTimeStrokeWidth);
            canvas.drawLine(selectTimeAreaDistanceLeft, selectTimeStrokeWidth / 2, selectTimeAreaDistanceLeft, viewHeight- CUtils.dip2px(12)*2 - selectTimeStrokeWidth / 2, selectAreaPaint);
            canvas.drawLine(selectTimeAreaDistanceRight, selectTimeStrokeWidth / 2, selectTimeAreaDistanceRight, viewHeight- CUtils.dip2px(12)*2 - selectTimeStrokeWidth / 2, selectAreaPaint);
            //画上下两条选择视频的线1
            selectAreaPaint.setStrokeWidth(selectTimeStrokeWidth / 3);
            canvas.drawLine(selectTimeAreaDistanceLeft, selectTimeStrokeWidth / 6, selectTimeAreaDistanceRight, selectTimeStrokeWidth / 6, selectAreaPaint);
            canvas.drawLine(selectTimeAreaDistanceLeft, viewHeight- CUtils.dip2px(12)*2 - selectTimeStrokeWidth / 6, selectTimeAreaDistanceRight, viewHeight- CUtils.dip2px(12)*2 - selectTimeStrokeWidth / 6, selectAreaPaint);
            //画视频区域
            canvas.drawRect(selectTimeAreaDistanceLeft, 0, selectTimeAreaDistanceRight, viewHeight- CUtils.dip2px(12)*2, vedioArea);
            onSelectedTimeListener.onDragging(getSelectStartTime(), getSelectEndTime());
        }
    }

    private float selectTimeAreaDistanceLeft = -1;
    private float selectTimeAreaDistanceRight = -1;

    /**
     * 画中间线
     *
     * @param canvas
     */
    private void drawCenterLine(Canvas canvas) {
        canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, viewHeight - CUtils.dip2px(12)*2, centerLinePaint);
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

    /**
     * 是否选择时间模式
     */
    private boolean isSelectTimeArea;

    public boolean isSelectTimeArea() {
        return isSelectTimeArea;
    }

    /**
     * 设置是否选择时间区域
     *
     * @param selectTimeArea
     */
    public void setSelectTimeArea(boolean selectTimeArea) {
        lastTimeMillis=getCurrentTimeMillis();
        this.isSelectTimeArea = selectTimeArea;
        if (selectTimeArea) {//选择的时候需要停止选择
            if (scaleMode == ScaleMode.KEY_HOUSE) {
                scaleMode = ScaleMode.KEY_MINUTE;//要恢复到分钟模式，否则刻度精度太高无法选择
                zoom = 300;
                centerPointDuration = (int) ((mScreenWidth / 2f) / (((320.0 + zoom) / (10 * 60 * 1000))));
                setCurrentTimeMillis(lastTimeMillis);
                adapter.setZoom(zoom);
                adapter.setScaleMode(scaleMode);
            }
        }
        selectTimeAreaDistanceLeft = -1;//需要复位
        selectTimeAreaDistanceRight = -1;//需要复位
        setIsCanScrollBar(!isSelectTimeArea);//选择时间时不能滑动
        postInvalidate();
    }

    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        if (heightMode == MeasureSpec.EXACTLY) {//布局里面设置大小，其他都是默认值
//            ELog.e("heightSize = "+heightSize);
//            adapter.setViewHeight(heightSize);
//        }
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        setMeasuredDimension(widthSize, heightSize);
//    }
    private OnSelectedTimeListener onSelectedTimeListener;

    public void setOnSelectedTimeListener(OnSelectedTimeListener onSelectedTimeListener) {
        this.onSelectedTimeListener = onSelectedTimeListener;
    }

    private int viewHeight = CUtils.dip2px(178);
    /**
     * 设置方向
     * @param newConfig
     */
    public void setOrientation(Configuration newConfig){
        onConfigurationChanged(newConfig);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("hdltag", "onConfigurationChanged(RulerView.java:719):------------------横竖屏切换了-----------------" );
        int temp = mScreenHeight;
        mScreenHeight = mScreenWidth;
        mScreenWidth = temp;
        //中心点距离左边所占用的时长
        centerPointDuration = (int) ((mScreenWidth / 2f) / (((320.0 + zoom) / (10 * 60 * 1000))));
        postInvalidate();
        setCurrentTimeMillis(currentTimeMillis);
//        ELog.e("mScreenWidth = " + mScreenWidth);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewHeight = CUtils.dip2px(75);
        } else {
            viewHeight = CUtils.dip2px(178);
        }
//        ELog.e("设置viewheight = "+viewHeight);
        adapter.setViewHeight(viewHeight);
    }
}
