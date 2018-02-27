package com.hdl.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.hdl.ruler.bean.ScaleMode;
import com.hdl.ruler.utils.CUtils;
import com.hdl.ruler.utils.DateUtils;

/**
 * Created by HDL on 2018/2/7.
 *
 * @author HDL
 */

public class RulerItemView extends View {
    /**
     * 刻度配置
     */
    private Paint smallRulerPaint = new Paint();//小刻度画笔
    private int rulerColor = Color.BLACK;//刻度的颜色
    private int rulerWidthSamll = CUtils.dip2px(0.5f);//小刻度的宽度
    private int rulerHeightSamll = CUtils.dip2px(10);//小刻度的高度
    private static final int DEFAULT_RULER_SPACE = CUtils.dip2px(12);//设置item默认间隔
    private int rulerSpace = DEFAULT_RULER_SPACE;//刻度间的间隔
    private static final int MAX_SCALE = CUtils.dip2px(39);//最大缩放值
    private static final int MIN_SCALE = CUtils.dip2px(6);//最小缩放值
    /**
     * 大刻度
     */
    private Paint largeRulerPaint = new Paint();//大刻度画笔
    private int rulerWidthBig = CUtils.dip2px(0.5f);//大刻度的宽度
    private int rulerHeightBig = CUtils.dip2px(20);//大刻度的高度
    /**
     * 上下两条线
     */
    private Paint upAndDownLinePaint = new Paint();//刻度画笔
    private int upAndDownLineWidth = CUtils.dip2px(1);//上下两条线的宽度
    private int upAndDownLineColor = rulerColor;

    /**
     * 文本画笔
     */
    private TextPaint keyTickTextPaint = new TextPaint();
    private int textColor = 0xff444242;//文本颜色
    private int textSize = CUtils.dip2px(12);//文本大小
    /**
     * 中轴线画笔
     */
    private Paint centerLinePaint = new Paint();
    private int centerLineColor = 0xff6e9fff;//中轴线画笔
    private int centerLineWidth = CUtils.dip2px(2);
    /**
     * 视频区域画笔
     */
    private Paint vedioAreaPaint = new Paint();
    private int vedioBg = 0x336e9fff;//视频背景颜色
    private RectF vedioAreaRect = new RectF();

    /**
     * 选择时间配置
     */
    private Paint selectAreaPaint = new Paint();//选择时间边框
    private int selectTimeBorderColor = 0xfffabb64;//边框颜色
    private Paint vedioArea = new Paint();//已选时间
    private int selectTimeAreaColor = 0x33fabb64;//已选时间颜色
    private float selectTimeStrokeWidth = CUtils.dip2px(8);

    private int viewHeight = CUtils.dip2px(120);
    //除数、刻度精度
    private int divisor;

    public RulerItemView(Context context) {
        this(context, null);
    }

    public RulerItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDivisor();
        initPaint();
    }

    /**
     * 设置精度
     */
    private void setDivisor() {
        switch (scaleMode) {
            case KEY_HOUSE:
                divisor = 10;
                break;
            case KEY_MINUTE:
            default:
                divisor = 1;
        }
    }

    private ScaleMode scaleMode = ScaleMode.KEY_MINUTE;

    public ScaleMode getScaleMode() {
        return scaleMode;
    }

    public void setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
        setDivisor();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        smallRulerPaint.setAntiAlias(true);
        smallRulerPaint.setColor(rulerColor);
        smallRulerPaint.setStrokeWidth(rulerWidthSamll);

        largeRulerPaint.setAntiAlias(true);
        largeRulerPaint.setColor(rulerColor);
        largeRulerPaint.setStrokeWidth(rulerWidthBig);

        keyTickTextPaint.setAntiAlias(true);
        keyTickTextPaint.setColor(textColor);
        keyTickTextPaint.setTextSize(textSize);

        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setStrokeWidth(centerLineWidth);
        centerLinePaint.setColor(centerLineColor);

        vedioAreaPaint.setAntiAlias(true);
        vedioAreaPaint.setColor(vedioBg);

        upAndDownLinePaint.setAntiAlias(true);
        upAndDownLinePaint.setColor(upAndDownLineColor);
        upAndDownLinePaint.setStrokeWidth(upAndDownLineWidth);

        selectAreaPaint.setColor(selectTimeBorderColor);
        selectAreaPaint.setAntiAlias(true);
        selectAreaPaint.setStrokeCap(Paint.Cap.ROUND);
        selectAreaPaint.setStyle(Paint.Style.STROKE);
        selectAreaPaint.setStrokeWidth(selectTimeStrokeWidth);

        vedioArea.setColor(selectTimeAreaColor);
        vedioArea.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawUpAndDownLine(canvas);
        drawRuler(canvas);
    }

    /**
     * 画刻度尺
     *
     * @param canvas
     */
    private void drawRuler(Canvas canvas) {
        float viewWidth = getWidth();
        float itemWidth = viewWidth / (10 / divisor);
        float rightX = 0;
        if (scaleMode == ScaleMode.KEY_HOUSE) {
            //小时级别的画法
            if ((timeIndex / 10) % 6 == 0) { //大刻度
                //画上面的大刻度
                canvas.drawLine(0, 0, 0, rulerHeightSamll * 2, largeRulerPaint);
                //画下面的大刻度
                canvas.drawLine(0, viewHeight, 0, viewHeight - rulerHeightSamll * 2, largeRulerPaint);
                float timeStrWidth = keyTickTextPaint.measureText(DateUtils.getHourMinute(timeIndex));
//                canvas.drawText(DateUtils.getHourMinute(timeIndex), timeStrWidth + rulerWidthSamll, viewHeight + CUtils.dip2px(14), keyTickTextPaint);
                canvas.drawText(DateUtils.getHourMinute(timeIndex), -timeStrWidth/2, viewHeight + CUtils.dip2px(14), keyTickTextPaint);
            } else {//小刻度
                //画上面的小刻度
                canvas.drawLine(0, 0, 0, rulerHeightSamll, smallRulerPaint);
                //画下面的小刻度
                canvas.drawLine(0, viewHeight, 0, viewHeight - rulerHeightSamll, smallRulerPaint);
            }
//            selectAreaPaint.setStrokeWidth(1);
//            canvas.drawLine(0, 0, 0, viewHeight, selectAreaPaint);
        } else {
            for (int i = 0; i < 60; i++) {
                if (i == 0 || i == 59) {
                    //画上面的大刻度
                    canvas.drawLine(rightX, 0, rightX, rulerHeightSamll * 2, smallRulerPaint);
                    //画下面的大刻度
                    canvas.drawLine(rightX, viewHeight, rightX, viewHeight - rulerHeightSamll * 2, smallRulerPaint);
                    rightX += itemWidth;
                    if (i == 0) {
                        float timeStrWidth = keyTickTextPaint.measureText(DateUtils.getHourMinute(timeIndex));
                        canvas.drawText(DateUtils.getHourMinute(timeIndex), -timeStrWidth/2, viewHeight + CUtils.dip2px(14), keyTickTextPaint);
//                        canvas.drawText(DateUtils.getHourMinute(timeIndex), rightX - timeStrWidth + rulerWidthSamll, viewHeight + CUtils.dip2px(14), keyTickTextPaint);
                    }
//                    else {
//                        selectAreaPaint.setStrokeWidth(1);
//                        canvas.drawLine(0, 0, 0, viewHeight, selectAreaPaint);
//                    }
                } else if (i % divisor == 0) {
                    //画上面的小刻度
                    canvas.drawLine(rightX, 0, rightX, rulerHeightSamll, largeRulerPaint);
                    //画下面的小刻度
                    canvas.drawLine(rightX, viewHeight, rightX, viewHeight - rulerHeightSamll, largeRulerPaint);
                    rightX += itemWidth;
                }

            }
        }
    }

    /**
     * 画上下两条线
     *
     * @param canvas
     */
    private void drawUpAndDownLine(Canvas canvas) {
        int viewWidth = getWidth();
        //画上下两条线
        canvas.drawLine(0, 0, viewWidth, 0, upAndDownLinePaint);
        canvas.drawLine(0, viewHeight, viewWidth, viewHeight, upAndDownLinePaint);
    }

    /**
     * 设置当前时间（处于第几个小时）
     *
     * @param index
     */
    public void setCurTimeIndex(int index) {
        timeIndex = index * 10;
    }

    private int timeIndex;
}
