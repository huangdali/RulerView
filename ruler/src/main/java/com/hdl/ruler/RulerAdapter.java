package com.hdl.ruler;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hdl.ruler.bean.ScaleMode;
import com.hdl.ruler.bean.TimeSlot;
import com.hdl.ruler.utils.CUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by hdl on 1/3/18.
 */
public class RulerAdapter extends RecyclerView.Adapter<RulerAdapter.RulerViewHolder> {
    private static final String TAG = "RulerAdapter";

    private Context context;
    private int width;
    private final AnimationSet animationSetRight;
    private final AnimationSet animationSetLeft;
//    private LinearLayout.LayoutParams leftParams, rightParams;

    public RulerAdapter(Context context) {
        this.context = context;
        // get device display dimensions
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        int height = size.y;
        AlphaAnimation alphaAnimationRight = new AlphaAnimation(0, 1f);
        TranslateAnimation translateAnimationRight = new TranslateAnimation(0, 0,
                0, -CUtils.dip2px(3),
                0, 0, 0, 0);
        alphaAnimationRight.setDuration(1000);
        alphaAnimationRight.setRepeatCount(30);
        alphaAnimationRight.setRepeatMode(AlphaAnimation.REVERSE);
        translateAnimationRight.setDuration(1000);
        translateAnimationRight.setRepeatCount(30);
        translateAnimationRight.setRepeatMode(AlphaAnimation.REVERSE);
        animationSetRight = new AnimationSet(false);
        animationSetRight.addAnimation(alphaAnimationRight);
        animationSetRight.addAnimation(translateAnimationRight);
        animationSetRight.setDuration(1000);
        animationSetRight.setRepeatCount(30);


        AlphaAnimation alphaAnimationLeft = new AlphaAnimation(1f, 0f);
        TranslateAnimation translateAnimationLeft = new TranslateAnimation(0, 0,
                0, CUtils.dip2px(3),
                0, 0, 0, 0);
        alphaAnimationLeft.setDuration(1000);
        alphaAnimationLeft.setRepeatCount(30);
        alphaAnimationLeft.setRepeatMode(AlphaAnimation.REVERSE);
        translateAnimationLeft.setDuration(1000);
        translateAnimationLeft.setRepeatCount(30);
        translateAnimationLeft.setRepeatMode(AlphaAnimation.REVERSE);
        animationSetLeft = new AnimationSet(false);
        animationSetLeft.addAnimation(alphaAnimationRight);
        animationSetLeft.addAnimation(translateAnimationLeft);
        animationSetLeft.setDuration(1000);
        animationSetLeft.setRepeatCount(30);

    }

    @Override
    public RulerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RulerViewHolder(View.inflate(context, R.layout.item_ruler, null));
    }

    @Override
    public void onBindViewHolder(RulerViewHolder holder, int position) {
        int itemWidth = (int) (320 + zoom);
        holder.parentView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, viewHeight));
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //竖屏
            holder.iv_tip_left.setImageResource(R.mipmap.ic_last_day);
            holder.iv_tip_right.setImageResource(R.mipmap.ic_next_day);
        } else {
            //横屏
            holder.iv_tip_left.setImageResource(R.mipmap.ic_last_day_landscape);
            holder.iv_tip_right.setImageResource(R.mipmap.ic_next_day_landscape);
        }

        holder.view.setCurTimeIndex(position - 12 * 6);
        holder.view.setScaleMode(scaleMode);
        holder.view.setVedioTimeSlot(vedioTimeSlot);
        View view = holder.parentView;
        view.setLayoutParams(new RecyclerView.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        holder.view.postInvalidate();
        holder.view.setViewHeight(viewHeight);
        if (position == (12 + 24) * 6) {//24点之后的item
            holder.ll_next_day_tip.bringToFront();
            holder.ll_next_day_tip.setVisibility(View.VISIBLE);
            holder.ivRight.startAnimation(animationSetRight);// 给图片设置动画
        } else {
            holder.ll_next_day_tip.setVisibility(View.GONE);
        }
        if (position == 12 * 6 - 1) {//00:00之前的item
            holder.ll_last_day_tip.setVisibility(View.VISIBLE);
            holder.ivLeft.startAnimation(animationSetLeft);// 给图片设置动画
        } else {
            holder.ll_last_day_tip.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return 6 * 48;
    }

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
        notifyDataSetChanged();
    }

    class RulerViewHolder extends RecyclerView.ViewHolder {
        private RulerItemView view;
        private View parentView;
        private LinearLayout ll_next_day_tip;
        private LinearLayout ll_last_day_tip;
        private ImageView ivLeft, iv_tip_left;
        private ImageView ivRight, iv_tip_right;

        public RulerViewHolder(View itemView) {
            super(itemView);
            parentView = itemView;
            view = (RulerItemView) itemView.findViewById(R.id.riv_ruler_item);
            ll_next_day_tip = (LinearLayout) itemView.findViewById(R.id.ll_next_day_tip);
            ll_last_day_tip = (LinearLayout) itemView.findViewById(R.id.ll_last_day_tip);
            ivLeft = (ImageView) itemView.findViewById(R.id.iv_left);
            ivRight = (ImageView) itemView.findViewById(R.id.iv_right);
            iv_tip_left = (ImageView) itemView.findViewById(R.id.iv_tip_left);
            iv_tip_right = (ImageView) itemView.findViewById(R.id.iv_tip_right);
        }
    }

    private float zoom;

    /**
     * 设置缩放值
     *
     * @param zoom
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
//        ELog.e("设置缩放值 zoom = " + zoom);
        notifyDataSetChanged();
    }

    private ScaleMode scaleMode = ScaleMode.KEY_MINUTE;

    public void setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
        notifyDataSetChanged();
    }

    private int viewHeight = CUtils.dip2px(178);
    /**
     * 当前模式
     */
    private int orientation = Configuration.ORIENTATION_LANDSCAPE;

    /**
     * 设置view的高度
     *
     * @param viewHeight
     */
    public void setViewHeight(int viewHeight) {
        if (this.viewHeight < viewHeight) {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        }
        this.viewHeight = viewHeight;
        notifyDataSetChanged();
    }
}
