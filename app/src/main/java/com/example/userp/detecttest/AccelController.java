package com.example.userp.detecttest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by userp on 2018-05-23.
 *
 * 일단 안드로이드 폰이 위를 바라보고 있다는 가정 하에 시작
 */

public class AccelController {
    private SensorManager accelManager;
    private SensorEventListener accelListener;
    private Sensor accel;
    private boolean isStarted = false;

    private TextView accelText;
    private TextView accelMaxValueTextView;

    public AccelController(Context context) {
        accelManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        accel = accelManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelListener = new AccelListener();

        accelText = (TextView) ((Activity)context).findViewById(R.id.accelValueTextView);
        accelMaxValueTextView = (TextView) ((Activity)context).findViewById(R.id.accelMaxValueTextView);
    }
    public void startAccel() {
        if(accelManager!=null && isStarted ==false) {
            accelManager.registerListener(accelListener, accel, SensorManager.SENSOR_DELAY_NORMAL);
            isStarted = true;
        }
    }
    public void stopAccel() {
        if(accelManager!=null && isStarted == true) {
            accelManager.unregisterListener(accelListener);
            isStarted = false;
        }
    }

    private class AccelListener implements SensorEventListener {
        private boolean firstTime = true;
        private float minValue[] = new float[3];
        private float maxValue[] = new float[3];
        private float value[] = new float[3];

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for(int i=0; i<3; i++) {
                value[i]= sensorEvent.values[i];
            }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if(firstTime) {
                    for(int i=0; i<3; i++) {
                        minValue[i] = value[i];
                        maxValue[i] = value[i];
                    }
                    firstTime = false;
                }
                for(int i=0; i<3; i++) {
                    if(minValue[i] > value[i]) {
                        minValue[i] = value[i];
                    } else if(maxValue[i] < value[i]) {
                        maxValue[i] = value[i];
                    }
                }
                accelText.setText((long)(minValue[0]*100)+"");
                accelMaxValueTextView.setText((long)(maxValue[0]*100)+"");
                MySystem.getInstance().setAccelXMinData((long)(minValue[0]*100));
                MySystem.getInstance().setAccelXMaxData((long)(maxValue[0]*100));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}
