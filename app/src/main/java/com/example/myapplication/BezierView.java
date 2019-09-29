package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BezierView extends View {

    private static final int DEFAULT_STROKE_WIDTH = 4;
    public static final int DEFAULT_BEZIER_COLOR = Color.RED;
    public static final int DEFAULT_LINE_POINT_COLOR = Color.GRAY;
    public static int POINT_SIZE = 9;

    private Paint mPaint,mLinePointPaint;
    private Path mPath;
    private List<PointF> mPoints;     //点集，控制点和数据点

    public BezierView(Context context) {
        super(context);
        init();
    }

    public BezierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BezierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(DEFAULT_BEZIER_COLOR);
        mLinePointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePointPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        mLinePointPaint.setStyle(Paint.Style.STROKE);
        mLinePointPaint.setColor(DEFAULT_LINE_POINT_COLOR);

        mPath = new Path();
        mPoints = new ArrayList<>();
        initPoints();
    }

    private void initPoints(){
        mPoints.clear();
        Random random = new Random();
        for (int i = 0; i< POINT_SIZE; i++){
            int x = random.nextInt(800) + 200;
            int y = random.nextInt(800) + 200;
            PointF pointF = new PointF(x,y);
            mPoints.add(pointF);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        PointF pointF;
        for (int i=0;i<mPoints.size();i++){
            pointF = mPoints.get(i);
            if(i > 0){      //第二个点后开始画连接线
                canvas.drawLine(mPoints.get(i - 1).x,mPoints.get(i- 1).y,
                        pointF.x,pointF.y,mLinePointPaint);
            }
            //画数据点
            canvas.drawCircle(pointF.x,pointF.y,12,mLinePointPaint);
        }
        //画贝塞尔曲线
        buildBezierPoints();
        canvas.drawPath(mPath,mPaint);
    }

    private void buildBezierPoints(){
        mPath.reset();
        List<PointF> pointFs = new ArrayList<>();
        int order = mPoints.size() - 1;   //贝塞尔曲线的阶数,比数据点个数少1
        //画的密集度，点数越多，曲线越圆滑
        float delta = 1.0f / 1000;
        for (float t = 0;t <= 1; t += delta){
            //Bezier点集
            PointF pointF = new PointF(deCasteljauX(order,0,t),deCasteljauY(order,0,t));
            pointFs.add(pointF);
            if(pointFs.size() == 1){
                mPath.moveTo(pointFs.get(0).x,pointFs.get(0).y);
            }else{
                mPath.lineTo(pointF.x,pointF.y);
            }
        }
    }

    /**
     * deCasteljau算法
     * p(i,j) =  (1-t) * p(i-1,j)  +  t * p(i-1,j+1);
     *
     * @param order 阶数
     * @param index 控制点（第几个点）
     * @param t 时间
     */
    private float deCasteljauX(int order,int index,float t){
        if(order == 1){   //一阶的话
            return (1 - t) * mPoints.get(index).x + t * mPoints.get(index + 1).x;
        }
        return (1 - t) * deCasteljauX(order - 1,index,t) +
                t * deCasteljauX(order - 1,index + 1,t);
    }

    /**
     *
     * @param order 阶数
     * @param index 控制点（第几个点）
     * @param t 时间
     */
    private float deCasteljauY(int order,int index,float t){
        if(order == 1){
            return (1 - t) * mPoints.get(index).y + t * mPoints.get(index).y;
        }
        return (1 - t) * deCasteljauY(order - 1,index,t) +
                t * deCasteljauY(order - 1,index + 1,t);
    }

    //设置曲线宽度
    public void setStrokeWidth(int strokeWidth){
        mPaint.setStrokeWidth(strokeWidth);
        mLinePointPaint.setStrokeWidth(strokeWidth);
    }

    //设置曲线颜色
    public void setStrokeColor(int color){
        mPaint.setColor(color);
    }

    //设置线段和点颜色
    public void setLinePointColor(int color){
        mLinePointPaint.setColor(color);
    }

    //设置点的个数
    public void setPointSize(int size){
        POINT_SIZE = size;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //点击后刷新曲线
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            init();
            invalidate();
        }
        return true;
    }
}
