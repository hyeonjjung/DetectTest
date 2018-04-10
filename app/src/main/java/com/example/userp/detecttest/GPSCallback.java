package com.example.userp.detecttest;

import android.location.Location;

/**
 * Created by userp on 2018-04-10.
 */

public interface GPSCallback {
    public abstract void onGPSUpdate(Location location);
}
