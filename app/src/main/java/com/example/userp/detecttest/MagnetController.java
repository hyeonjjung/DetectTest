package com.example.userp.detecttest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
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

    private Context context;

    public MagnetController(Context context) {
        this.context = context;
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
        if(magnetManager!=null) {
            magnetManager.unregisterListener(magnetListenr);
        }
    }
    public void resetMagnetometer() {
        if(magnetManager!=null) {
            magnetManager.unregisterListener(magnetListenr);
            magnetManager.registerListener(magnetListenr, manget, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public class MagnetListener implements SensorEventListener{
        float[] value = new float[3];
        float[] lastValue = {0,0,0};
        int state = 0;
        int count = 0;
        int upCount = 0;
        boolean firstTime = true;
        float FRONT_PARAMETER = (float) 1.5;

        MySystem mySystem = MySystem.getInstance();

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            magneticValueTxtview.setText("start!");
            for(int i = 0 ; i <sensorEvent.values.length; i++) {
                value[i] = sensorEvent.values[i];
            }
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD: // 단위는 마이크로테슬라
                    if(firstTime) {
                        for(int i=0; i<3; i++) {
                            lastValue[i] = value[i];
                            firstTime = false;
                        }
                        mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.NON_STATE));
                    }
                    //for (int i = 0; i < 3; i ++) {
                        magneticValueTxtview.setText("State is "+state);
                        if (state == 0) {    //기울기가 급격하게 변하는 구간
                                if(Math.abs(value[0]-lastValue[0]) > 10 || Math.abs(value[1] - lastValue[1])>10 || Math.abs(value[2] - lastValue[2]) > 10){  //급감소
                                    state = 1;
                                } else if ((value[0]-lastValue[0] > FRONT_PARAMETER) || (value[1] - lastValue[1] > FRONT_PARAMETER) || (value[2] - lastValue[2]) > FRONT_PARAMETER) {   //앞좌석 감지 시작 - 상승
                                    state = 2;
                                } else if ((lastValue[0] - value[0] > FRONT_PARAMETER) || (lastValue[1] - value[1] > FRONT_PARAMETER) || (lastValue[2] - lastValue[2] > FRONT_PARAMETER)) {   //앞좌석 감지 시작 - 하강
                                    state = 3;
                                }
                        } else if (state == 1) {    //일정 시간내에 다시 원래 값으로 돌아오는 구간
                            if(count < 100) {   //상승
                                if((value[0] - lastValue[0]>0) || (value[1] - lastValue[1] >0) ||(value[2] - lastValue[2] > 0)) { //조금씩 상승
                                    upCount ++;
                                }
                                if(upCount > 30) {
                                    if(mySystem.getMagneticState().getState() == MagneticState.NON_STATE ||
                                            mySystem.getMagneticState().getState() == MagneticState.FRONT_SEAT ||
                                            mySystem.getMagneticState().getState() == MagneticState.BACK_SEAT) {
                                        mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.DASH_BOARD));
                                        magneticMaxValueTxtview.setText("Magnetic state is "+mySystem.getMagneticState().getState());
                                    }
                                }
                            } else {
                                count = 0;
                                upCount = 0;
                                state = 0;
                            }
                            count++;
                        } else if(state == 2) {
                            //magneticMaxValueTxtview.setText("State 2 and "+count);
                            if(count < 100) {
                                if((value[0] - lastValue[0]<0) || (value[1] - lastValue[1] < 0) || (value[2] - lastValue[2] < 0)) { //조금씩 하강
                                    upCount ++;
                                }
                                if(upCount > 30) {
                                    if(mySystem.getMagneticState().getState() == MagneticState.NON_STATE ||
                                            mySystem.getMagneticState().getState() == MagneticState.BACK_SEAT) {
                                        mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.FRONT_SEAT));
                                        magneticMaxValueTxtview.setText("Magnetic state is "+mySystem.getMagneticState().getState());
                                    }
                                }
                            } else {
                                count = 0;
                                upCount = 0;
                                state = 0;
                            }
                            count++;
                        } else if(state == 3) {
                            if(count < 100) {   //상승
                                if((value[0] - lastValue[0]>0) || (value[1] - lastValue[1] > 0) || (value[2] - lastValue[2] >0)) { //조금씩 상승
                                    upCount ++;
                                }
                                if(upCount > 30) {
                                    if(mySystem.getMagneticState().getState() == MagneticState.NON_STATE ||
                                            mySystem.getMagneticState().getState() == MagneticState.BACK_SEAT) {
                                        mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.FRONT_SEAT));
                                        magneticMaxValueTxtview.setText("Magnetic state is "+mySystem.getMagneticState().getState());
                                    }
                                }
                            } else {
                                count = 0;
                                upCount = 0;
                                state = 0;
                            }
                            count++;
                        } else {
                            if(mySystem.getMagneticState().getState() == MagneticState.NON_STATE) {
                                mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.BACK_SEAT));
                                magneticMaxValueTxtview.setText("Magnetic state is " + mySystem.getMagneticState().getState());
                            }
                        }
                        for (int i=0; i<3; i++) {
                            lastValue[i] = value[i];
                        }
                    //}

                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}
