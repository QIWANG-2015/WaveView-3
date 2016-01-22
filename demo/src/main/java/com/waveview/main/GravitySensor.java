package com.waveview.main;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

/**
 * Created by SiKang on 2016/1/5.
 */
public class GravitySensor {
    private final String TAG = "GravitySensorTest";
    private static GravitySensor mVibratorSensor = null;
    private SensorManager sensorManager;
    private Handler mHandler;

    private GravitySensor(Context context, Handler mHandler) {
        this.mHandler = mHandler;
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
    }

    //获取实例
    public static GravitySensor getInstance(Context context, Handler mHandler) {
        if (mVibratorSensor == null) {
            synchronized (GravitySensor.class) {
                if (mVibratorSensor == null) {
                    mVibratorSensor = new GravitySensor(context, mHandler);
                }
            }
        }
        return mVibratorSensor;
    }

    private float nowAngle = 0;
    /**
     * 重力感应监听
     */
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            //粗略实现效果，只用X轴的值，计算出倾斜度（范围 （-10） - 10，算出比例，用于界面倾斜，范围限制 （-40°）-40°）
            float x = values[0];//取出X轴的值
            //得到实际角度
            float angle = 40 * (x / 10);
            //如果变化大于0.1刷新UI
            if (Math.abs(Math.abs(angle) - Math.abs(nowAngle)) > 0.1f) {
                Log.d(TAG,"111");
                Message msg = new Message();
                msg.obj = nowAngle = angle;
                mHandler.sendMessageDelayed(msg, 300);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public void start() {
        // 注册监听器
        // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
        if (sensorManager != null) {
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void shutDown() {
        //注销监听器
        sensorManager.unregisterListener(sensorEventListener);
    }

    public void destory() {
        sensorManager=null;
        mVibratorSensor = null;
    }
}
