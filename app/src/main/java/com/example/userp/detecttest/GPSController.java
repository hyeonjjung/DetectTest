package com.example.userp.detecttest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by userp on 2018-05-23.
 */

public class GPSController {
    private static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private double currentSpeed = 0;
    private static int GPS_UPDATE_MIN_DISTANCE = 0;
    private static int GPS_UPDATE_MIN_TIME = 0;
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    private Context context;

    TextView gpsValueTextView;
    TextView gpsStateTextView;
    TextView gpsSpeedTextView;
    TextView currentTextVuew;

    public GPSController(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new SpeedActionListener();

        gpsValueTextView = (TextView) ((Activity)context).findViewById(R.id.gpsTextView);
        gpsStateTextView = (TextView) ((Activity)context).findViewById(R.id.resultSpeedTextView);
        gpsSpeedTextView = (TextView) ((Activity)context).findViewById(R.id.speedTextView);
        currentTextVuew = (TextView) ((Activity)context).findViewById(R.id.stateTextView);
    }
    public void startGPS() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_MIN_TIME, GPS_UPDATE_MIN_DISTANCE, locationListener);
    }

    private class SpeedActionListener implements LocationListener {
        Location lastLocation = null;
        float bearing = 0;
        double lastSpeed = 0;
        int leftTurnCount = 0;
        int rightTurnCount = 0;

        MySystem mySystem = MySystem.getInstance();
        BeaconController beaconController = new BeaconController();
        BeaconScanController beaconScanController = new BeaconScanController(context);
        AccelController accelController = new AccelController(context);

        @Override
        public void onLocationChanged(Location location) {
            if(location!= null) {
                currentSpeed = location.getSpeed() * 3.6;
                gpsSpeedTextView.setText("Current speed :"+currentSpeed);
                currentTextVuew.setText("Current state is "+mySystem.getState()+ " "+mySystem.getMagneticState().getState());

                //20km/h 이상일 경우 처음 시스템이 시작할때
                if(currentSpeed > 20 && mySystem.getState() == MySystem.SYSTEM_SLEEP) {
                    mySystem.setState(MySystem.SYSTEM_START);
                    mySystem.setStartTime(System.currentTimeMillis());

                    Toast.makeText(context, "Start system!"+mySystem.getMagneticState().getState(), Toast.LENGTH_SHORT).show();

                    if(mySystem.getMagneticState() != null) {
                        if ((mySystem.getStartTime() - mySystem.getMagneticState().getTime()) < 1000 * 60 * 10) {
                            if (mySystem.getMagneticState().getState() == 1) {
                                Toast.makeText(context, "I'm in the dash board!", Toast.LENGTH_SHORT).show();
                                mySystem.setState(MySystem.DRIVER_STATE);
                            } else if (mySystem.getMagneticState().getState() == 3) {
                                Toast.makeText(context, "I'm in the front seat!", Toast.LENGTH_SHORT).show();
                                mySystem.setState(MySystem.BEACON_STATE);

                                //BeaconTransmitter start for 5 second
                                beaconController.startBeaconTransmitter(context);
                                beaconStopTask beaconStopTask = new beaconStopTask();
                                beaconStopTask.execute();

                                //Beacon Scanner start for 5 second
                                beaconScanController.startBeaconScan();
                            } else if (mySystem.getMagneticState().getState() == 0) {
                                mySystem.setState(MySystem.NOT_DRIVER_STATE);
                                Toast.makeText(context, "I'm in the back seat!", Toast.LENGTH_SHORT).show();
                            } else {
                                mySystem.setState(MySystem.NOT_DRIVER_STATE);
                                Toast.makeText(context, "Nothing", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(context, "Magnetic state is null", Toast.LENGTH_SHORT).show();
                    }
                }
                //방위각을 통한 회전 감지
                // 방위각의 변화가 적으면 직진 감소하면 좌회전 증가하면 우회전
                if(lastLocation != null && lastLocation != location) {
                    if(currentSpeed > 10 && lastSpeed > 10) {
                        if (bearing - location.bearingTo(lastLocation) > 5) { //감소
                            //좌회전
                            if(leftTurnCount == 1) {
                                //좌회전
                                gpsStateTextView.setText("좌회전 "+System.currentTimeMillis());
                            }
                            leftTurnCount++;
                        } else if (location.bearingTo(lastLocation) - bearing > 5) { // 증가
                            //우회전
                            if(rightTurnCount == 1) {
                                //우회전
                                gpsStateTextView.setText("우회전 "+System.currentTimeMillis());
                            }
                            rightTurnCount ++;
                        } else { //나중에는 회전 이후 몇초 이후에 자동으로 정지하게 만들기
                            gpsStateTextView.setText("Nothing");
                            leftTurnCount = 0;
                            rightTurnCount = 0;
                        }
                    }
                    bearing = location.bearingTo(lastLocation);
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
                    Thread.sleep(5000);
                    beaconController.stopBeaconTransmitter();
                    beaconScanController.stopBeaconScan();
                    if(mySystem.getState() == MySystem.BEACON_STATE) {
                        mySystem.setState(MySystem.DRIVER_STATE);
                    } else if(mySystem.getState() == MySystem.ACCEL_WAIT_STATE) {
                        mySystem.setState(MySystem.ACCEL_STATE);
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
