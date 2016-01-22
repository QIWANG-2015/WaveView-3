package com.waveview.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by SiKang on 2016/1/21.
 */
public class MainActivity extends Activity {
    private GravitySensor mGravitySensor;
    private WaveView mWaveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化重力传感器，并开启监听
        mGravitySensor = GravitySensor.getInstance(MainActivity.this, mHandler);

        mWaveView = (WaveView) this.findViewById(R.id.activity_main_wave);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public boolean handleMessage(Message msg) {
            //更新角度
            mWaveView.setRotation((float) msg.obj);
            return false;
        }
    });

    @Override
    protected void onStart() {
        super.onStart();
        mGravitySensor.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGravitySensor.shutDown();
    }

    @Override
    protected void onDestroy() {
        mGravitySensor.destory();
        super.onDestroy();

    }
}
