package com.example.userp.detecttest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

/**
 * Created by userp on 2018-05-23.
 */

public class AccelController {
    private SensorManager accelManager;
    private SensorEventListener accelListener;
    private Sensor accel;
    private boolean isStarted = false;

    TextView accelText;

    public AccelController(Context context) {
        accelManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        accel = accelManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelListener = new AccelListener();

        accelText = (TextView) ((Activity)context).findViewById(R.id.accelValueTextView);
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
                accelText.setText(minValue[0]+" "+minValue[1]+" "+minValue[2]+"\n"+maxValue[0]+" "+maxValue[1]+" "+maxValue[2]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}
