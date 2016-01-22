package com.waveview.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    private int mLeftWave;
    private int mRightWave;
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

        leftUp = true;//左右浮动方向判断（true为左上右下）
        mWaveHeightProportion = 0.6f;//水位百分比

    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            mWaveRange = mHeight / 20;//波纹上下浮动范围（总高度的10%）
            //用来绘制底板图
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mLeftWave = -mWaveRange;//左边波纹高度初始化
            mRightWave = mWaveRange;//右边波纹高度初始化
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //根据百分比得到实际水位
        int waveHeight = (int) (mHeight - mHeight * mWaveHeightProportion);
        //水位一帧浮动的高度
        int waveYRange = mWaveRange / 30;
        Log.d(TAG, waveHeight + "");
        //左上右下
        if (leftUp) {
            mLeftWave += waveYRange;
            mRightWave -= waveYRange;
            if (mLeftWave >= mWaveRange) {
                leftUp = !leftUp;
            }
        } else {//左下右上
            mLeftWave -= waveYRange;
            mRightWave += waveYRange;
            if (mRightWave >= mWaveRange) {
                leftUp = !leftUp;
            }
        }
        //波纹两端浮动高度（波纹起点和重点配合波浪做轻微浮动）
        int mBeginHeight = mLeftWave / 5;
        int mEndHeight = mRightWave / 5;
        //重置波纹路径
        mPath.reset();
        mPath.moveTo(0, waveHeight + mBeginHeight);
        mPath.cubicTo(mWidth / 4, waveHeight + mLeftWave, mWidth / 4 * 3, waveHeight + mRightWave, getWidth(), waveHeight + mEndHeight);
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
        if (mLeftWave >= mWaveRange / 5 * 4 || mRightWave >= mWaveRange / 5 * 4)
            postInvalidateDelayed(40);
        else
            postInvalidateDelayed(10);

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
