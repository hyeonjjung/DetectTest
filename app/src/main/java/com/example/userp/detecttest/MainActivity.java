package com.example.userp.detecttest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Locale;

public class MainActivity extends Activity implements BeaconConsumer {

    private static final String TAG = "MainActivity";

    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private Spinner spinner = null;

    /*** System start and stop button ***/
    private Button startBtn = null;
    private Button stopBtn = null;

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

    private BeaconController beaconController = null;
    private BeaconScanController beaconScanController = null;

    String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private BeaconManager beaconManager;

    private String location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();
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

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.location, android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                location = String.valueOf(adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        gpsController.startGPS();
        magnetController.startMangetometer();

        beaconController = new BeaconController(this);
        beaconScanController = new BeaconScanController(this);

        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);

        stopBtn.setEnabled(false);

        startBeaconMonitoringBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beaconController.startBeaconTransmitter(2550, 2);
                beaconScanController.startBeaconScan();
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
                Toast.makeText(MainActivity.this, "Current state is "+MySystem.getInstance().getState()+"\nMagnetic state is "+MySystem.getInstance().getMagneticState().getState()+"\nCountry Code is "+MySystem.getInstance().getCountryCode(), Toast.LENGTH_SHORT).show();
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

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(false);
                stopBtn.setEnabled(true);
                Toast.makeText(getApplicationContext(), "System start!", Toast.LENGTH_SHORT).show();

                LogManager.getInstance().makeFile(location);
                LogManager.getInstance().writeFile("MainActivity");
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                Toast.makeText(getApplicationContext(), "System stop!", Toast.LENGTH_SHORT).show();

                LogManager.getInstance().writeFile("MainActivity");
                LogManager.getInstance().stopWritingFile();
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        mySystem.setState(MySystem.SYSTEM_SLEEP);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect()");
    }
    private void permissionCheck() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity) this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}


