package com.example.userp.detecttest;

/**
 * Created by userp on 2018-05-27.
 */

public class MagneticState {
    private long time;

    /*
    *  state 0 is none (back seat)
    *  state 1 is dash board
    *  state 2 is front seat
    * */
    private int state = 0;

    public static int NON_STATE = 0;
    public static int DASH_BOARD = 1;
    public static int FRONT_SEAT = 2;
    public static int BACK_SEAT = 3;

    public MagneticState(long time, int state) {
        this.time = time;
        this.state = state;
    }
    public MagneticState() {

    }
    public long getTime() {
        return time;
    }
    public int getState() {
        return state;
    }
}
