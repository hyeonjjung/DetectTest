package com.example.userp.detecttest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.net.URL;
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
    private TextView resultSpeedTextview;
    private TextView resultMagneticTextview, magneticMaxValueTxtview;
    private Button startBeaconMonitoringBtn;

    BeaconController beaconController;

    private BeaconManager beaconManager = null;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    private double currentSpeed = 0;
    private static int GPS_UPDATE_MIN_DISTANCE = 0;
    private static int GPS_UPDATE_MIN_TIME = 0;

    private boolean isSecondSpeed = false;

    private SensorManager sensorManager = null;
    private Sensor mMagnet = null;
    private SensorEventListener mMagnetLis = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtview = (TextView) findViewById(R.id.speedTextView);
        beaconTxtview = (TextView) findViewById(R.id.findedBeaconTextView);
        resultTxtview = (TextView) findViewById(R.id.resultTextView);
        resultSpeedTextview = (TextView) findViewById(R.id.resultSpeedTextView);
        resultMagneticTextview = (TextView) findViewById(R.id.resultMagneticTextView);
        magneticMaxValueTxtview = (TextView) findViewById(R.id.magneticMaxValueTextView);

        startBeaconMonitoringBtn = (Button)findViewById(R.id.startBeaconMonitoringBtn);
        beaconController = new BeaconController();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new SpeedActionListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_MIN_TIME, GPS_UPDATE_MIN_DISTANCE, locationListener);


        //Magnetic Field Sensor configuration
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mMagnetLis = new SensorController();
        sensorManager.registerListener(mMagnetLis, mMagnet, SensorManager.SENSOR_DELAY_FASTEST);

        //For permission check
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                }else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                }else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
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
        sensorManager.unregisterListener(mMagnetLis);
    }

    @Override
    public void onBeaconServiceConnect() {
        new StopBeaconTask().execute();
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
                    if(!isSecondSpeed) {
                        new TimerTask().execute();
                        isSecondSpeed = true;
                    } else {
                        startBeaconScan();
                        isSecondSpeed = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtview.setText("I'm in the car!!!");

                            }
                        });
                    }
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
    private class TimerTask extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private class StopBeaconTask  extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {
            try {
                Thread.sleep(3000);
                stopBeaconScan();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    public class SensorController implements SensorEventListener{
        float v1, v2, v3, maxGap = 0;
        float[] gap = new float[3];
        boolean isFirstValue = true;

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(isFirstValue) {
                isFirstValue = false;
            } else {
                gap[0] = Math.abs(v1 - sensorEvent.values[0]);
                gap[1] = Math.abs(v2 - sensorEvent.values[1]);
                gap[2] = Math.abs(v3 - sensorEvent.values[2]);

                for (int i=0; i<3; i++) {
                    if (gap[i]>=maxGap) {
                        maxGap = gap[i];
                    }
                }
            }

            v1 = sensorEvent.values[0];
            v2 = sensorEvent.values[1];
            v3 = sensorEvent.values[2];

            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magneticMaxValueTxtview.setText("Max value is "+maxGap);
                    if(maxGap > 5) {
                        //DashBoard
                        resultTxtview.setText("I'm driver!");
                        resultMagneticTextview.setText("I'm in the front seat!");

                    } else if (maxGap > 1) {
                        //Front Seat
                        resultMagneticTextview.setText("I'm in the front seat!");

                    } else {
                        //back Seat
                        resultMagneticTextview.setText("I'm in the back seat!");
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}


