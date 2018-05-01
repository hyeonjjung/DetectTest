package com.example.userp.detecttest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import android.widget.Toast;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.w3c.dom.Text;

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

    private TextView txtview, Locationtxtview;
    private TextView resultSpeedTextview;
    private TextView magneticMaxValueTxtview, magneticValueTxtview;
    private TextView accelValueTxtView;

    /*** Beacon Scan ***/
    private BeaconController beaconController;
    private BeaconManager beaconManager = null;
    private boolean isBeacon = false;

    /*** GPS ***/
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    private double currentSpeed = 0;
    private static int GPS_UPDATE_MIN_DISTANCE = 0;
    private static int GPS_UPDATE_MIN_TIME = 0;

    private boolean isSecondSpeed = false;

    /*** Sensor ***/
    private SensorManager sensorManager = null;
    private Sensor mMagnet = null;
    private SensorEventListener sensorEventListener = null;

    private Sensor mAccel = null;

    /*** Temperate Start Buttons ***/
    private Button startGPSBtn = null;
    private Button startMagneticBtn = null;
    private Button startBeaconMonitoringBtn = null;
    private Button startAccelBtn = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtview = (TextView) findViewById(R.id.speedTextView);
        resultSpeedTextview = (TextView) findViewById(R.id.resultSpeedTextView);
        magneticMaxValueTxtview = (TextView) findViewById(R.id.magneticMaxValueTextView);
        magneticValueTxtview = (TextView) findViewById(R.id.magneticValueTextView);
        accelValueTxtView = (TextView) findViewById(R.id.accelValueTextView);

        beaconController = new BeaconController();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new SpeedActionListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_MIN_TIME, GPS_UPDATE_MIN_DISTANCE, locationListener);


        //Sensor configuration
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        mMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorEventListener = new SensorController();
        sensorManager.registerListener(sensorEventListener, mMagnet, SensorManager.SENSOR_DELAY_FASTEST);

        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        //initializing starting buttons
        startGPSBtn = (Button)findViewById(R.id.gpsStartBtn);
        startMagneticBtn = (Button)findViewById(R.id.startMagneticBtn);
        startBeaconMonitoringBtn = (Button)findViewById(R.id.startBeaconMonitoringBtn);
        startAccelBtn = (Button)findViewById(R.id.startAccelBtn);


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
                beaconController.startBeaconTransmitter(getApplication());
            }
        });
        startGPSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startByGPS();
            }
        });
        startMagneticBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        startAccelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAccelerometer();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconController.stopBeaconTransmitter();
        stopBeaconScan();
        sensorManager.unregisterListener(sensorEventListener);
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

    private void startMangetometer() {
        if(!sensorManager.equals(null)) {
            sensorManager.registerListener(sensorEventListener, mMagnet, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
    private void stopMagnetometer() {
        if(!sensorManager.equals(null)) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }
    private void startAccelerometer() {
        if(!sensorManager.equals(null)) {
            sensorManager.registerListener(sensorEventListener, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    private void stopAccelerometer() {
        if(!sensorManager.equals(null)) {
            sensorManager.unregisterListener(sensorEventListener);
        }

    }

    private class SpeedActionListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if(location!= null) {
                currentSpeed = location.getSpeed() * 3.6;
                txtview.setText("Current speed : "+currentSpeed+"km/h");
                if(currentSpeed > 20) {
                    startByGPS();
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
                if(!isBeacon) {
                    Log.d(TAG, "근처의 비콘을 찾지 못함");
                }
                Log.d(TAG, "stopd beacon scan");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    public class SensorController implements SensorEventListener{
        float[] value = new float[3];
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for(int i = 0 ; i <sensorEvent.values.length; i++) {
                value[i] = sensorEvent.values[i];
            }
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD: // 단위는 마이크로테슬라

                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    accelValueTxtView.setText(value[0]+"\n"+value[1]+"\n"+value[2]);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
    /*
    *  GPS speed가 20km/h 이상일 경우
    * */
    private void startByGPS() {
        if(!isSecondSpeed) {    //처음으로 speed > 20
            new TimerTask().execute();
            Log.d(TAG, "start by gps first times");
            isSecondSpeed = true;
        } else {
            isSecondSpeed = false;
            Log.d(TAG, "start by gps second times");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "GPS speed 20 초가", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*
    * GPS speed가 20km/h 5초이내에 다시 초과되는 경우
    * */
    private void startByMagnetometer() {
        sensorManager.registerListener(sensorEventListener, mMagnet, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /*
    * Accelerometer sensor값을 통해서 우회전 혹은 좌회전에서 passenger side인지 혹은 driver side인지 확인
    * */
    private void waitingTrunByAccel() {
        startAccelerometer();

    }
}


