package com.example.userp.detecttest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements GPSCallback, BeaconConsumer {

    private static final String TAG = "MainActivity";

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";

    private GPSManager gpsManager = null;
    private double speed = 0.0;
    Boolean isGPSEnabled = false;
    LocationManager locationManager;
    double currentSpeed, kmphSpeed;
    TextView txtview, Locationtxtview, beaconTxtview;

    BeaconController beaconController;

    private Button startBtn = null;
    private Button startBeaconBtn = null;
    private Button startBeaconMonitoringBtn = null;

    private BeaconManager beaconManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtview = (TextView) findViewById(R.id.speedTextView);
        beaconTxtview = (TextView) findViewById(R.id.findedBeaconTextView);

        startBtn = (Button) findViewById(R.id.startBtn);
        startBeaconBtn = (Button) findViewById(R.id.startBeaconBtn);
        startBeaconMonitoringBtn = (Button) findViewById(R.id.startBeaconMonitoringBtn);


        beaconController = new BeaconController();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));


        //For permission check
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                }else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                }
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                }else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentSpeed(txtview);
            }
        });

        startBeaconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beaconController.startBeaconTransmitter(getApplicationContext());
            }
        });

        startBeaconMonitoringBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBeaconScan();
            }
        });
    }

    public void getCurrentSpeed(View view) {
        txtview.setText("Waiting for GPS");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gpsManager = new GPSManager(MainActivity.this);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            gpsManager.startListening(getApplicationContext());
            gpsManager.setGPSCallback(this);
        } else {
            gpsManager.showSettingsAlert();
        }
    }

    @Override
    protected void onDestroy() {
        if (gpsManager != null) {
            gpsManager.stopListening();
            gpsManager.setGPSCallback(null);
            gpsManager = null;
        }
        super.onDestroy();
        beaconController.stopBeaconTransmitter();
        stopBeaconScan();
    }

    @Override
    public void onGPSUpdate(Location location) {
        speed = location.getSpeed();
        currentSpeed = round(speed, 3, BigDecimal.ROUND_HALF_UP);
        kmphSpeed = round((currentSpeed * 3.6), 3, BigDecimal.ROUND_HALF_UP);
        txtview.setText(kmphSpeed + "km/h");

    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if(collection.size() > 0) {
                    Iterator<Beacon> iterator = collection.iterator();
                    while(iterator.hasNext()) {
                        Beacon beacon = iterator.next();
                        String address = beacon.getBluetoothAddress();
                        double rssi = beacon.getRssi();
                        int txPower = beacon.getTxPower();
                        UUID id1 = beacon.getId1().toUuid();
                        int major = beacon.getId2().toInt();
                        int minor = beacon.getId3().toInt();
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }
    public void startBeaconScan() {
        if(!beaconManager.isBound(MainActivity.this)) {
            beaconManager.bind(MainActivity.this);
            Log.d(TAG, "start Beacon monitoring...");
        }
    }
    public void stopBeaconScan() {
        if(beaconManager.isBound(MainActivity.this)) {
            beaconManager.unbind(MainActivity.this);
        }
    }
}

