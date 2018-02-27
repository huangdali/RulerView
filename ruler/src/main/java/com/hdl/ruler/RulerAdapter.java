package com.hdl.ruler;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.hdl.ruler.bean.ScaleMode;


/**
 * Created by jiezhi on 4/8/16.
 * Function:
 */
public class RulerAdapter extends RecyclerView.Adapter<RulerAdapter.RulerViewHolder> {
    private static final String TAG = "RulerAdapter";

    private Context context;
    private int width;

    public RulerAdapter(Context context) {
        this.context = context;
        // get device display dimensions
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        int height = size.y;
        Log.v(TAG, "width:" + width + " height:" + height);
    }

    @Override
    public RulerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.d(TAG, "RulerViewHolder");

//        SimpleRulerView simpleRulerView = new SimpleRulerView(context);
//        View view = new RulerItemView(context);
//        view.setLayoutParams(new RecyclerView.LayoutParams(320, RecyclerView.LayoutParams.MATCH_PARENT));
//        View view = LayoutInflater.from(context).inflate(R.layout.ruler_unit_horizontal, null);
//        view.setLayoutParams(new RecyclerView.LayoutParams(200, RecyclerView.LayoutParams.MATCH_PARENT));
//        tv = (TextView) view.findViewById(R.id.ruler_num);
//        tv.setText(String.valueOf(10));
        return new RulerViewHolder(View.inflate(context, R.layout.item_ruler, null));
    }

    @Override
    public void onBindViewHolder(RulerViewHolder holder, int position) {
//        ELog.e("刷新ITEM了");
        int itemWidth = (int) (320 + zoom);
        holder.view.setCurTimeIndex(position - 12*6);
//        if (holder.view.getScaleMode() != scaleMode) {
//            if (scaleMode == ScaleMode.KEY_HOUSE) {//变成小时级别的时候，需要将整体宽度变窄
////                itemWidth = (int) (320f + zoom - 100);
//                itemWidth=50;
//            }
            holder.view.setScaleMode(scaleMode);
//        }
//        if (holder.view.getScaleMode() == ScaleMode.KEY_HOUSE) {
//            ELog.e("统一设置成25了");
//            itemWidth = 25;
//        }
        View view = holder.parentView;
//        ELog.e("320 + zoom = "+(320 + zoom));
        view.setLayoutParams(new RecyclerView.LayoutParams(itemWidth, RecyclerView.LayoutParams.WRAP_CONTENT));
        holder.view.postInvalidate();
//        TextView tv = (TextView) holder.view.findViewById(R.id.ruler_num);
////        ImageView imageView = (ImageView) holder.view.findViewById(R.id.ruler_img);
//
//        // Set blank unit for start and end ruler
//        if (position < BLANK_FILL || position > RULER_COUNT + BLANK_FILL) {
////            holder.view = new View(context);
////            imageView.setMinimumWidth(width / 2);
////            imageView.setVisibility(View.INVISIBLE);
////            tv.setVisibility(View.GONE);
//            tv.setText(".");
//        } else {
//            tv.setText(String.valueOf(((position - RULER_COUNT / 2) - BLANK_FILL - 1) * 10));
//        }
    }


    @Override
    public int getItemCount() {
        return 6*48;
    }

    class RulerViewHolder extends RecyclerView.ViewHolder {
        private RulerItemView view;
        private View parentView;

        public RulerViewHolder(View itemView) {
            super(itemView);
            parentView = itemView;
            view = (RulerItemView) itemView.findViewById(R.id.riv_ruler_item);
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
}
