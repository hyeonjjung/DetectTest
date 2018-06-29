package com.example.userp.detecttest;

/**
 * Created by userp on 2018-05-27.
 */

public class MySystem {

    public static final int SYSTEM_SLEEP = 0;
    /*
    * 차가 시속 20km를 초과하여 본 시스템이 시작할 때
    * */
    public static final int SYSTEM_START = 1;
    public static final int MAGNETIC_STATE = 2;
    public static final int BEACON_STATE = 3;
    public static final int ACCEL_STATE = 4;
    public static final int DRIVER_STATE = 5;
    public static final int NOT_DRIVER_STATE = 6;
    public static final int ACCEL_WAIT_STATE = 7;


    private static final MySystem ourInstance = new MySystem();
    private boolean isStarted = false;
    private int state = SYSTEM_SLEEP;
    private long startTime = 0;
    MagneticState magneticState = null;

    public static MySystem getInstance() {
        return ourInstance;
    }

    private MySystem() {

    }
    public boolean isStarted() {
        return isStarted;
    }
    public void setStart(boolean isStarted) {
        this.isStarted = isStarted;
    }
    public void setStartTime(long time) {
        this.startTime = time;
    }
    public long getStartTime() {
        return startTime;
    }
    public void setMagneticState (MagneticState magneticState) {
        this.magneticState = magneticState;
    }
    public MagneticState getMagneticState() {
        if(magneticState!=null) {
            return magneticState;
        } else {
            return null;
        }
    }

    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }

}
