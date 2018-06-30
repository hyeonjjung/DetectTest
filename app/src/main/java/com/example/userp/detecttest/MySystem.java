package com.example.userp.detecttest;

/**
 * Created by userp on 2018-05-27
 * 시스템이 동작하지 않고 GPS와 Magnetic field만 동작하기 있을 때에는 state 0
 * 이후에 Magnetic field로 Dash board, front seat, back seat이 감지되었을 경우에도 system state는 변하지 않고 magnetic field state만 변화함
 * 20 km이상으로 동작하면 system start 1
 * 시스템이 시작하면 magnetic state를 확인함 dash board와 back seat라면 바로 결과가 나옴
 * front seat의 경우 beacon state로 돌입하며 주위의 beacon 여부를 확인함
 * 있다고 한다면 accel wating
 */

public class MySystem {

    // Before the system start
    public static final int SYSTEM_SLEEP = 0;
    /*
    * 차가 시속 20km를 초과하여 본 시스템이 시작할 때
    * */
    public static final int SYSTEM_START = 1;

    /*
    * 비콘 detecting/transmitting을 동시에 하는 state
    * */
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
