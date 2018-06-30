package com.example.userp.detecttest;

/**
 * Created by userp on 2018-06-30.
 */

public class TurnState {
    public static int NONE_STATE = 0;
    public static int RIGHT_TURN = 1;
    public static int LEFT_TURN = 2;

    private int state = NONE_STATE;


    public TurnState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
