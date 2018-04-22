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
import android.os.Handler;
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
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final String TAG = "MainActivity";

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private static final String MY_UUID = "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6";

    private TextView txtview, Locationtxtview, beaconTxtview, resultTxtview;
    private Button startBeaconMonitoringBtn;

    BeaconController beaconController;

    private BeaconManager beaconManager = null;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    private double currentSpeed = 0;
    private static int GPS_UPDATE_MIN_DISTANCE = 0;
    private static int GPS_UPDATE_MIN_TIME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtview = (TextView) findViewById(R.id.speedTextView);
        beaconTxtview = (TextView) findViewById(R.id.findedBeaconTextView);
        resultTxtview = (TextView) findViewById(R.id.resultTextView);

        startBeaconMonitoringBtn = (Button)findViewById(R.id.startBeaconMonitoringBtn);
        beaconController = new BeaconController();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new SpeedActionListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_MIN_TIME, GPS_UPDATE_MIN_DISTANCE, locationListener);

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

        startBeaconMonitoringBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBeaconScan();
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

                        }
                    }
                } else {
                    Log.d(TAG, "I can't find beacon...");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "onRunnable()");
                            resultTxtview.setText("I'm Driver!");

                        }
                    });
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
        }
    }
    public void stopBeaconScan() {
        if(beaconManager.isBound(MainActivity.this)) {
            beaconManager.unbind(MainActivity.this);
        }
    }

    private class SpeedActionListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if(location!= null) {
                currentSpeed = location.getSpeed() * 3.6;
                txtview.setText("Current speed : "+currentSpeed+"km/h");
                if(currentSpeed > 20) {
                    startBeaconScan();
                }
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}

