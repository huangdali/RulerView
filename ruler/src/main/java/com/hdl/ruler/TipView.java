package com.hdl.ruler;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 上一天，下一天切换提示
 * Created by HDL on 2018/1/9.
 *
 * @author HDL
 */

public class TipView extends RelativeLayout {
    private ImageView ivLeft, ivRight;
    private ImageView ivTipLeft, ivTipRight;
    private ImageView ivTipLeftLandscape, ivTipRightLandscape;
    /**
     * 是否显示左边（上一天）提示
     */
    private boolean isShowLeftTip = false;
    /**
     * 是否显示右边（下一天）提示
     */
    private boolean isShowRightTip = false;
    /**
     * 是否显示左边（上一天）提示[横屏状态下]
     */
    private boolean isShowLeftTipLandscape = false;
    /**
     * 是否显示右边（下一天）提示[横屏状态下]
     */
    private boolean isShowRightTipLandscape = false;
    /**
     * 提示框消失时间
     */
    private static final int DURATION_TIP_HIDE = 3000;

    private ObjectAnimator leftAnimation;
    private ObjectAnimator rightAnimation;

    private static final int WHAT_CLOSE_LEFT = 447;
    private static final int WHAT_CLOSE_LEFT_LANDSCAPE = 448;
    private static final int WHAT_CLOSE_RIGHT = 449;
    private static final int WHAT_CLOSE_RIGHT_LANDSCAPE = 446;
    private Context context;

    /**
     * 消失的定时器
     */
    private Timer timer;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (context == null) {
                return;
            }
            switch (msg.what) {
                case WHAT_CLOSE_LEFT:
                    setShowLeftTip(false);
                    break;
                case WHAT_CLOSE_LEFT_LANDSCAPE:
                    setShowLeftTipLandscape(false);
                    break;
                case WHAT_CLOSE_RIGHT:
                    setShowRightTip(false);
                    break;
                case WHAT_CLOSE_RIGHT_LANDSCAPE:
                    setShowRightTipLandscape(false);
                    break;
            }
        }
    };

    public TipView(Context context) {
        this(context, null);
    }

    public TipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        View inflate = View.inflate(context, R.layout.tip_layout, null);
        addView(inflate);
        ivLeft = (ImageView) inflate.findViewById(R.id.iv_left);
        ivRight = (ImageView) inflate.findViewById(R.id.iv_right);
        ivTipLeft = (ImageView) inflate.findViewById(R.id.iv_tip_left);
        ivTipRight = (ImageView) inflate.findViewById(R.id.iv_tip_right);
        ivTipLeftLandscape = (ImageView) inflate.findViewById(R.id.iv_tip_left_landscape);
        ivTipRightLandscape = (ImageView) inflate.findViewById(R.id.iv_tip_right_landscape);
        leftAnimation = ObjectAnimator.ofFloat(ivLeft, "Alpha", 0, 1, 0, 1);
        rightAnimation = ObjectAnimator.ofFloat(ivRight, "Alpha", 0, 1, 0, 1);
        leftAnimation.setDuration(DURATION_TIP_HIDE);
        rightAnimation.setDuration(DURATION_TIP_HIDE);
        leftAnimation.setRepeatMode(ValueAnimator.RESTART);
        rightAnimation.setRepeatMode(ValueAnimator.RESTART);
        setShowLeftTip(false);
        setShowRightTip(false);

    }

    public boolean isShowLeftTip() {
        return isShowLeftTip;
    }

    public void setShowLeftTip(boolean showLeftTip) {
        isShowLeftTip = showLeftTip;
        hideLandscapeTip();
        if (isShowLeftTip) {
            ivTipLeft.setVisibility(VISIBLE);
            ivLeft.setVisibility(VISIBLE);
            ivTipRight.setVisibility(GONE);
            ivRight.setVisibility(GONE);
            leftAnimation.start();
            if (timer != null) {
                timer.cancel();
            }
            timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(WHAT_CLOSE_LEFT);
                }
            },DURATION_TIP_HIDE);
//            postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    setShowLeftTip(false);
//                }
//            }, 3000);
        } else {
            ivTipLeft.setVisibility(GONE);
            ivLeft.setVisibility(GONE);
            leftAnimation.cancel();
        }
    }

    public boolean isShowRightTip() {
        return isShowRightTip;
    }

    public void setShowRightTip(boolean showRightTip) {
        isShowRightTip = showRightTip;
        hideLandscapeTip();
        if (isShowRightTip) {
            ivRight.setVisibility(VISIBLE);
            ivTipRight.setVisibility(VISIBLE);
            ivTipLeft.setVisibility(GONE);
            ivLeft.setVisibility(GONE);
            rightAnimation.start();
            if (timer != null) {
                timer.cancel();
            }
            timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(WHAT_CLOSE_RIGHT);
                }
            },DURATION_TIP_HIDE);
        } else {
            ivRight.setVisibility(GONE);
            ivTipRight.setVisibility(GONE);
            rightAnimation.cancel();
        }
    }

    /**
     * 隐藏横屏提示
     */
    private void hideLandscapeTip() {
        ivTipRightLandscape.setVisibility(GONE);
        ivTipLeftLandscape.setVisibility(GONE);
    }

    /**
     * 隐藏竖屏屏提示
     */
    private void hideTip() {
        ivTipRight.setVisibility(GONE);
        ivTipLeft.setVisibility(GONE);
    }

    public boolean isShowLeftTipLandscape() {
        return isShowLeftTipLandscape;
    }

    public boolean isShowRightTipLandscape() {
        return isShowRightTipLandscape;
    }

    public void setShowLeftTipLandscape(boolean showLeftTipLandscape) {
        isShowLeftTipLandscape = showLeftTipLandscape;
        hideTip();
        if (isShowLeftTipLandscape) {
            ivLeft.setVisibility(VISIBLE);
            ivTipLeftLandscape.setVisibility(VISIBLE);
            ivTipRightLandscape.setVisibility(GONE);
            ivRight.setVisibility(GONE);
            leftAnimation.start();
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
//                    setShowLeftTipLandscape(false);
                    handler.sendEmptyMessage(WHAT_CLOSE_LEFT_LANDSCAPE);
                }

            }, DURATION_TIP_HIDE);
        } else {
            ivLeft.setVisibility(GONE);
            ivTipLeftLandscape.setVisibility(GONE);
            leftAnimation.cancel();
        }
    }

    public void setShowRightTipLandscape(boolean showRightTipLandscape) {
        isShowRightTipLandscape = showRightTipLandscape;
        hideTip();
        if (isShowRightTipLandscape) {
            ivRight.setVisibility(VISIBLE);
            ivTipRightLandscape.setVisibility(VISIBLE);
            ivTipLeftLandscape.setVisibility(GONE);
            ivLeft.setVisibility(GONE);
            rightAnimation.start();
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
//                    setShowRightTipLandscape(false);
                    handler.sendEmptyMessage(WHAT_CLOSE_RIGHT_LANDSCAPE);
                }
            }, DURATION_TIP_HIDE);
        } else {
            ivRight.setVisibility(GONE);
            ivTipRightLandscape.setVisibility(GONE);
            rightAnimation.cancel();
        }
    }
}
