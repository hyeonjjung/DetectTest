package com.example.userp.detecttest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by userp on 2018-05-23.
 */

public class GPSController {

    private final String TAG = "GPSController";
    private static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private double currentSpeed = 0;
    private static int GPS_UPDATE_MIN_DISTANCE = 10; //10 meters
    private static int GPS_UPDATE_MIN_TIME = 1000*60*1; // 1 minute
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;
    double latitude;
    double longitude;

    private List<Address> addressList;

    private Context context;

    TextView gpsValueTextView;
    TextView gpsStateTextView;
    TextView gpsSpeedTextView;
    TextView currentTextVuew;

    private LogManager logManager;

    public GPSController(final Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new SpeedActionListener();

        gpsValueTextView = (TextView) ((Activity)context).findViewById(R.id.gpsTextView);
        gpsStateTextView = (TextView) ((Activity)context).findViewById(R.id.resultSpeedTextView);
        gpsSpeedTextView = (TextView) ((Activity)context).findViewById(R.id.speedTextView);
        currentTextVuew = (TextView) ((Activity)context).findViewById(R.id.stateTextView);

    }
    public void startGPS() {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_MIN_TIME, GPS_UPDATE_MIN_DISTANCE, locationListener);
        if(locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Geocoder gcd = new Geocoder(context);
                try {
                    List<Address> locales = gcd.getFromLocation(latitude, longitude, 1);
                    MySystem.getInstance().setCountryCode(locales.get(0).getCountryCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SpeedActionListener implements LocationListener {
        Location lastLocation = null;
        float bearing = 0;
        double lastSpeed = 0;
        int leftTurnCount = 0;
        int rightTurnCount = 0;

        MySystem mySystem = MySystem.getInstance();
        BeaconController beaconController = new BeaconController(context);
        BeaconScanController beaconScanController = new BeaconScanController(context);
        AccelController accelController = new AccelController(context);

        @Override
        public void onLocationChanged(Location location) {
            if(location!= null) {
                Geocoder gcd = new Geocoder(context.getApplicationContext(), Locale.getDefault());
                try {
                    addressList = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, addressList.get(0).getCountryCode());
                Log.d(TAG, location.getLongitude()+"");

                currentSpeed = location.getSpeed() * 3.6;
                gpsSpeedTextView.setText("Current speed :"+currentSpeed);
                currentTextVuew.setText("Current state is "+mySystem.getState()+ " "+mySystem.getMagneticState().getState());

                //20km/h 이상일 경우 처음 시스템이 시작할때
                if(currentSpeed > 20 && mySystem.getState() == MySystem.SYSTEM_SLEEP) {
                    mySystem.setState(MySystem.SYSTEM_START);
                    mySystem.setStartTime(System.currentTimeMillis());
                    LogManager.getInstance().writeFile("GPS");

                    Toast.makeText(context, "Start system!"+mySystem.getMagneticState().getState(), Toast.LENGTH_SHORT).show();

                    if(mySystem.getMagneticState() != null && mySystem.getState() == MySystem.SYSTEM_START) {
                        /*
                        * If Magnetic field data is detected until 10 minutes after car driving starting
                        * */
                        if ((mySystem.getStartTime() - mySystem.getMagneticState().getTime()) < 1000 * 60 * 10) {
                            if (mySystem.getMagneticState().getState() == MagneticState.DASH_BOARD) {
                                mySystem.setState(MySystem.DRIVER_STATE);
                                LogManager.getInstance().writeFile("Magnetic");
                            } else if (mySystem.getMagneticState().getState() == MagneticState.FRONT_SEAT) {

                                mySystem.setState(MySystem.BEACON_STATE);
                                LogManager.getInstance().writeFile("Magnetic");

                                //BeaconTransmitter & Scanner start for 3 second
                                beaconController.startBeaconTransmitter(0, 0);
                                beaconScanController.startBeaconScan();
                                beaconStopTask beaconStopTask = new beaconStopTask();
                                beaconStopTask.execute();

                            } else if (mySystem.getMagneticState().getState() == MagneticState.BACK_SEAT) {
                                mySystem.setState(MySystem.NOT_DRIVER_STATE);
                                LogManager.getInstance().writeFile("Magnetic");
                            } else {
                                mySystem.setState(MySystem.NOT_DRIVER_STATE);
                                LogManager.getInstance().writeFile("Magnetic");
                            }
                        }
                    } else if (mySystem.getState() == MySystem.DRIVER_STATE || mySystem.getState() == MySystem.NOT_DRIVER_STATE) {
                        beaconScanController.stopBeaconScan();
                        beaconController.stopBeaconTransmitter();
                    }
                }
                //방위각을 통한 회전 감지
                // 방위각의 변화가 적으면 직진 감소하면 좌회전 증가하면 우회전
                if(mySystem.getState() == MySystem.ACCEL_STATE) {
                    if (lastLocation != null && lastLocation != location) {
                        if (currentSpeed > 10 && lastSpeed > 10) {
                            if (bearing - location.bearingTo(lastLocation) > 5) { //감소
                                //좌회전
                                if (leftTurnCount == 1) {
                                    mySystem.setState(MySystem.ACCEL_BEACON_STATE);
                                    LogManager.getInstance().writeFile("GPS_turn");
                                    beaconController.startBeaconTransmitter(mySystem.getAccelXMinData(), 1);
                                    gpsStateTextView.setText("Turn is Left " + System.currentTimeMillis());
                                }
                                leftTurnCount++;
                            } else if (location.bearingTo(lastLocation) - bearing > 5) { // 증가
                                //우회전
                                if (rightTurnCount == 1) {
                                    mySystem.setState(MySystem.ACCEL_BEACON_STATE);
                                    LogManager.getInstance().writeFile("GPS_turn");
                                    beaconController.startBeaconTransmitter(mySystem.getAccelXMaxData(), 2);
                                    gpsStateTextView.setText("Turn is Right " + System.currentTimeMillis());
                                }
                                rightTurnCount++;
                            } else { //나중에는 회전 이후 몇초 이후에 자동으로 정지하게 만들기
                                gpsStateTextView.setText("Turn is Nothing");
                                leftTurnCount = 0;
                                rightTurnCount = 0;
                            }
                        }
                        bearing = location.bearingTo(lastLocation);
                    }
                }

                lastLocation = location;
                lastSpeed =  currentSpeed;
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

        public class beaconStopTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(3000);

                    beaconController.stopBeaconTransmitter();
                    beaconScanController.stopBeaconScan();

                    if(mySystem.getState() == MySystem.BEACON_STATE) {
                        mySystem.setState(MySystem.DRIVER_STATE);
                        LogManager.getInstance().writeFile("Beacon");
                    } else if(mySystem.getState() == MySystem.ACCEL_WAIT_STATE) {
                        mySystem.setState(MySystem.ACCEL_STATE);
                        LogManager.getInstance().writeFile("Time");
                        accelController.startAccel();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }
}
