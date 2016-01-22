package com.waveview.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by SiKang on 2016/1/21.
 */
public class WaveView extends View {
    private final String TAG = "WaveViewTest";
    private Path mPath;
    private Paint mCirclePaint;
    private Paint mWavePaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    private float mWaveHeightProportion;
    private int mWaveRange;
    private Point mLeftWavePoint;
    private Point mRightWavePoint;
    private boolean leftUp;


    public WaveView(Context context) {
        super(context);

        init(context);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        //波纹路径
        mPath = new Path();
        //圆球画笔
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setAlpha(95);

        //波纹画笔
        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setAlpha(95);
        mWavePaint.setColor(Color.parseColor("#87CEFF"));
        mWavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        //用来控制波浪的贝塞尔曲线的两个控制点坐标
        mLeftWavePoint = new Point();
        mRightWavePoint = new Point();

        leftUp = true;//左右浮动方向判断（true为左上右下）
        mWaveHeightProportion = 0.6f;//水位百分比

    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            mWaveRange = mHeight / 8;//波纹上下浮动范围（总高度的8分之一）
            //用来绘制底板图
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            //左边波纹位置初始化
            //这里初始化的图像概念是，左边的水浪处于最低潮，右边的处于最高潮，浪的x轴位置分别是控件的4分之一和最右边
            mLeftWavePoint.x = mWidth / 4;
            mLeftWavePoint.y = -mWaveRange;
            //右边波纹位置初始化
            mRightWavePoint.x = mWidth;
            mRightWavePoint.y = mWaveRange;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //根据百分比得到实际水位
        int waveHeight = (int) (mHeight - mHeight * mWaveHeightProportion);
        //水位一帧浮动的高度
        int waveYRange = mWaveRange / 30;
        /**
         * 水位每一帧浮动的宽度
         * 这里240是因为，高度浮动范围是 /30，而高度范围是有正负的，所以就是说水波的上下运动是60次刷新完成的
         * 而横向我想要的地洞范围只有空间的四分之一（一边水波为控件的一半，水波的一半就是控件的4分之一），就是mWidth/4；
         * 有了然后分60次完成，每一次就是 mWidth/4/60 = mWidth / 240
         * */
        int waveXRange = mWidth / 240;

        /**
         * 这里主要对每一帧 贝塞尔曲线的控制点位置做修改，不需要太注重我的写法，主要思路就是通过不断移动两个控制点模拟波浪
         * 最好是通过实践自己推理出一套能想到最好的曲线运动
         * 我的做法是 两个控制点 y轴相对方向匀速运动   x轴同向运动 具体效果可以运行看看
         * */
        //左上右下
        if (leftUp) {
            mLeftWavePoint.x -= waveXRange;
            mRightWavePoint.x -= waveXRange;
            mLeftWavePoint.y += waveYRange;
            mRightWavePoint.y -= waveYRange;
            if (mLeftWavePoint.y >= mWaveRange) {
                leftUp = !leftUp;
            }
        } else {
            //左下右上
            mLeftWavePoint.x += waveXRange;
            mRightWavePoint.x += waveXRange;
            mLeftWavePoint.y -= waveYRange;
            mRightWavePoint.y += waveYRange;
            if (mRightWavePoint.y >= mWaveRange) {
                leftUp = !leftUp;
            }
        }

        //波纹两端浮动高度（波纹起点和终点配合波浪做轻微浮动，左右两端固定不动太没有真实感了）
        int mBeginHeight = mLeftWavePoint.y / 5;
        int mEndHeight = mRightWavePoint.y / 5;
        //重置波纹路径
        mPath.reset();
        mPath.moveTo(0, waveHeight + mBeginHeight);
        Log.d(TAG, mLeftWavePoint.y + "  ----------  " + mRightWavePoint.y);
        mPath.cubicTo(mLeftWavePoint.x, waveHeight + mLeftWavePoint.y, mRightWavePoint.x, waveHeight + mRightWavePoint.y, getWidth(), waveHeight + mEndHeight);
        mPath.lineTo(getWidth(), getHeight());
        mPath.lineTo(0, getHeight());
        mPath.close();

        //绘制圆
        mCanvas.drawCircle(mWidth / 2, mHeight / 2, (mWidth < mHeight ? mWidth : mHeight) / 2, mCirclePaint);
        //绘制波纹
        mCanvas.drawPath(mPath, mWavePaint);
        //将bitmap画到VIEW的画布上
        canvas.drawBitmap(mBitmap, 0, 0, mCirclePaint);
        //水纹荡起到顶部时，减慢速度模拟下落时重力抵消的缓冲效果
        if (mLeftWavePoint.y >= mWaveRange / 5 * 4 || mRightWavePoint.y >= mWaveRange / 5 * 4)
            postInvalidateDelayed(40);
        else
            postInvalidateDelayed(5);

    }

    boolean add = false;

    //按下水位持续上升，松开恢复
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            add = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (add) {
                        if (mWaveHeightProportion < 0.9) {
                            mWaveHeightProportion += 0.01;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            add = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!add) {
                        if (mWaveHeightProportion > 0.4)
                            mWaveHeightProportion -= 0.01;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
        }
        return true;
    }


}
