package com.hdl.ruler.bean;

/**
 * 时间轴移动、拖动的回调
 * Created by HDL on 2017/9/5.
 */

public interface OnBarMoveListener {
    /**
     * 当拖动的时候回调
     *
     * @param isLeftDrag
     * @param currentTime
     */
    void onDragBar(boolean isLeftDrag, long currentTime);

    /**
     * 当时间轴自动移动的时候回调
     *
     * @param currentTime
     */
    void onBarMoving(long currentTime);

    /**
     * 当拖动完成时回调
     *
     * @param currentTime
     */
    void onBarMoveFinish(long currentTime);

    /**
     * 移动超过开始时间
     */
    void onMoveExceedStartTime();

    /**
     * 移动超过结束时间
     */
    void onMoveExceedEndTime();

    /**
     * 超过最大缩放值
     */
    void onMaxScale();

    /**
     * 超过最小缩放值
     */
    void onMinScale();
}
