package com.example.userp.detecttest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by userp on 2018-05-23.
 */

public class MagnetController {
    private static final String TAG = "MagnetController";

    private SensorManager magnetManager;
    private SensorEventListener magnetListenr;
    private Sensor manget;

    TextView magneticMaxValueTxtview;
    TextView magneticValueTxtview;

    public MagnetController(Context context) {
        magnetManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        manget = magnetManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magnetListenr = new MagnetListener();

        magneticMaxValueTxtview = (TextView) ((Activity)context).findViewById(R.id.magneticMaxValueTextView);
        magneticValueTxtview = (TextView) ((Activity)context).findViewById(R.id.magneticValueTextView);
    }

    public void startMangetometer() {
        if(magnetManager != null) {
            magnetManager.registerListener(magnetListenr, manget, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
    public void stopMagnetometer() {
        if(!magnetManager.equals(null)) {
            magnetManager.unregisterListener(magnetListenr);
        }
    }

    public class MagnetListener implements SensorEventListener{
        float[] value = new float[3];
        float[] lastValue = {0,0,0};
        int state = 0;
        int count = 0;
        float alpha = (float) 0.001;
        float[] maxValue = {0,0,0};
        float[] minValue = {0,0,0};
        int upCount = 0;
        float normalValue = 0;
        boolean firstTime = true;

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for(int i = 0 ; i <sensorEvent.values.length; i++) {
                value[i] = sensorEvent.values[i];
            }
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD: // 단위는 마이크로테슬라
                    for (int i = 0; i < 3; i ++) {
                        if (state == 0) {    //기울기가 급격하게 변하는 구간
                            magneticMaxValueTxtview.setText("State 0 and "+count);
                            count++;
                            if(count < 3){
                                if(Math.abs(value[i]-lastValue[i]) > 10){  //급감소
                                    state = 1;
                                } else if (value[i]-lastValue[i] > 1) {   //앞좌석 감지 시작 - 상승
                                    state = 2;
                                } else if (lastValue[i] - value[i] > 1) {   //앞좌석 감지 시작 - 하강
                                    state = 3;
                                }
                            } else {
                                state = 0;
                                count = 0;
                            }
                        } else if (state == 1) {    //일정 시간내에 다시 원래 값으로 돌아오는 구간
                            magneticMaxValueTxtview.setText("State 1 and "+count);
                            if(count < 100) {   //상승
                                if(value[i] - lastValue[i]>0) { //조금씩 상승
                                    upCount ++;
                                }
                                if(upCount > 30) {
                                    magneticValueTxtview.setText("I detect engine!");
                                }
                            } else {
                                count = 0;
                                upCount = 0;
                                state = 0;
                            }
                            count++;
                        } else if(state == 2) {
                            magneticMaxValueTxtview.setText("State 2 and "+count);
                            if(count < 100) {
                                if(value[i] - lastValue[i]<0) { //조금씩 하강
                                    upCount ++;
                                }
                                if(upCount > 30) {
                                    magneticValueTxtview.setText("I'm front seat!");
                                }
                            } else {
                                count = 0;
                                upCount = 0;
                                state = 0;
                            }
                            count++;
                        } else if(state == 3) {
                            if(count < 100) {   //상승
                                if(value[i] - lastValue[i]>0) { //조금씩 상승
                                    upCount ++;
                                }
                                if(upCount > 30) {
                                    magneticValueTxtview.setText("I'm front seat!");
                                }
                            } else {
                                count = 0;
                                upCount = 0;
                                state = 0;
                            }
                            count++;
                        } else {
                            magneticValueTxtview.setText("State 0");
                        }
                        lastValue[i] = value[i];
                    }

                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}
