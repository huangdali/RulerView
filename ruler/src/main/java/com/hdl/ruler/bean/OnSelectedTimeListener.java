package com.hdl.ruler.bean;

/**
 * 选择时间监听
 * Created by HDL on 2017/8/2.
 */

public interface OnSelectedTimeListener {
    /**
     * 拖动中
     *
     * @param startTime
     * @param endTime
     */
    void onDragging(long startTime, long endTime);

    /**
     * 超过最大选择时间
     */
    void onMaxTime();

    /**
     * 超过最小选择时间
     */
    void onMinTime();
}
