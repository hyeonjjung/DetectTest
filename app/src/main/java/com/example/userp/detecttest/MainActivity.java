package com.example.userp.detecttest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;


import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /*** Temperate Start Buttons ***/
    private Button startGPSBtn = null;
    private Button startMagneticBtn = null;
    private Button startBeaconMonitoringBtn = null;
    private Button startAccelBtn = null;

    /** Sensor **/
    private GPSController gpsController = null;
    private MagnetController magnetController = null;
    private AccelController accelController = null;

    private MySystem mySystem = MySystem.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsController = new GPSController(this);
        magnetController = new MagnetController(this);
        accelController = new AccelController(this);

        //initializing starting buttons
        startBeaconMonitoringBtn = (Button)findViewById(R.id.startBeaconMonitoringBtn);
        startAccelBtn = (Button)findViewById(R.id.startAccelBtn);

        gpsController.startGPS();
        magnetController.startMangetometer();


        startBeaconMonitoringBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startBeaconScan();
            }
        });
        startAccelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accelController.startAccel();
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        mySystem.setState(MySystem.SYSTEM_SLEEP);
    }

}


