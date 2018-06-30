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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /*** Temperate Start Buttons ***/
    private Button startBeaconMonitoringBtn = null;
    private Button startAccelBtn = null;
    private Button speedBtn = null;
    private Button getStateBtn = null;
    private Button resetBtn = null;

    private Button setMagneticDashBoardBtn = null;
    private Button setMagneticFrontBtn = null;
    private Button setMagneticBackBtn = null;

    /** Sensor **/
    private GPSController gpsController = null;
    private MagnetController magnetController = null;
    private AccelController accelController = null;

    private MySystem mySystem = MySystem.getInstance();

    private TextView currentStateTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsController = new GPSController(this);
        magnetController = new MagnetController(this);
        accelController = new AccelController(this);

        currentStateTextView = (TextView) findViewById(R.id.stateTextView);

        //initializing starting buttons
        startBeaconMonitoringBtn = (Button)findViewById(R.id.startBeaconMonitoringBtn);
        startAccelBtn = (Button)findViewById(R.id.startAccelBtn);
        speedBtn = (Button)findViewById(R.id.speedButton);
        getStateBtn = (Button)findViewById(R.id.getStateButton);
        resetBtn = (Button)findViewById(R.id.resetStateBtn);

        setMagneticBackBtn = (Button)findViewById(R.id.setMagneticBackBtn);
        setMagneticDashBoardBtn = (Button)findViewById(R.id.setMagneticDashBoardBtn);
        setMagneticFrontBtn = (Button) findViewById(R.id.setMagneticFrontBtn);

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
        speedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mySystem.setState(MySystem.SYSTEM_START);
                currentStateTextView.setText("Current state is "+MySystem.getInstance().getState());
            }
        });
        getStateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Current state is "+MySystem.getInstance().getState()+"\nMagnetic state is "+MySystem.getInstance().getMagneticState().getState(), Toast.LENGTH_SHORT).show();
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mySystem.setState(MySystem.SYSTEM_SLEEP);
                mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.NON_STATE));
            }
        });
        setMagneticFrontBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.FRONT_SEAT));
            }
        });
        setMagneticBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.BACK_SEAT));
            }
        });
        setMagneticDashBoardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mySystem.setMagneticState(new MagneticState(System.currentTimeMillis(), MagneticState.DASH_BOARD));
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        mySystem.setState(MySystem.SYSTEM_SLEEP);
    }

}


