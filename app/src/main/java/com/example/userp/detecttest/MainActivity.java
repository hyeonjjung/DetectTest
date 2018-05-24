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

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final String TAG = "MainActivity";

    private static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private static final String MY_UUID = "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6";


    /*** Beacon Scan ***/
    private BeaconController beaconController;
    private BeaconManager beaconManager = null;
    private boolean isBeacon = false;

    private boolean isSecondSpeed = false;

    /*** Temperate Start Buttons ***/
    private Button startGPSBtn = null;
    private Button startMagneticBtn = null;
    private Button startBeaconMonitoringBtn = null;
    private Button startAccelBtn = null;

    /** Sensor **/
    private GPSController gpsController = null;
    private MagnetController magnetController = null;
    private AccelController accelController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsController = new GPSController(this);
        magnetController = new MagnetController(this);
        accelController = new AccelController(this);

        beaconController = new BeaconController();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));


        //initializing starting buttons
        startGPSBtn = (Button)findViewById(R.id.gpsStartBtn);
        startMagneticBtn = (Button)findViewById(R.id.startMagneticBtn);
        startBeaconMonitoringBtn = (Button)findViewById(R.id.startBeaconMonitoringBtn);
        startAccelBtn = (Button)findViewById(R.id.startAccelBtn);


        startBeaconMonitoringBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBeaconScan();
                beaconController.startBeaconTransmitter(getApplication());
            }
        });
        startMagneticBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                magnetController.startMangetometer();
            }
        });
        startAccelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accelController.startAccel();
            }
        });
        startGPSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsController.startGPS();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconController.stopBeaconTransmitter();
        stopBeaconScan();
    }

    @Override
    public void onBeaconServiceConnect() {
        new StopBeaconTask().execute();
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if(collection.size() > 0) {
                    Log.d(TAG, "Find Beacon !");
                    Iterator<Beacon> iterator = collection.iterator();
                    while(iterator.hasNext()) {
                        Beacon beacon = iterator.next();
                        UUID uuid = beacon.getId1().toUuid();
                        int major = beacon.getId2().toInt();
                        int minor = beacon.getId3().toInt();
                        Log.d(TAG, beacon.getDataFields().get(0).toString());
                        if(uuid.toString() == MY_UUID) {
                            isBeacon = true;
                            stopBeaconScan();
                        }
                    }
                } else {
                    Log.d(TAG, "I can't find beacon...");
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }
    private void startBeaconScan() {
        isBeacon = false;
        if(!beaconManager.isBound(MainActivity.this)) {
            beaconManager.bind(MainActivity.this);
        }
    }
    private void stopBeaconScan() {
        if(beaconManager.isBound(MainActivity.this)) {
            beaconManager.unbind(MainActivity.this);
        }
    }


    private class TimerTask extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {
            try {
                Thread.sleep(5000);
                isSecondSpeed = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /*
    * 비콘 스캔 시작 후 5초 뒤에 자동으로 정지
    * */
    private class StopBeaconTask  extends AsyncTask<URL, Integer, Long> {
        @Override
        protected Long doInBackground(URL... urls) {
            try {
                Thread.sleep(5000);
                stopBeaconScan();
                if (!isBeacon) {
                    Log.d(TAG, "근처의 비콘을 찾지 못함");
                }
                Log.d(TAG, "stopd beacon scan");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}


