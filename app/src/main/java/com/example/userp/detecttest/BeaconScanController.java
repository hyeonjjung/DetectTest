package com.example.userp.detecttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by userp on 2018-05-27.
 */

public class BeaconScanController implements BeaconConsumer {
    private static final String TAG = "BeaconScanController";

    Context context;

    private static final String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private static final String MY_UUID = "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6";

    private BeaconManager beaconManager = null;
    private boolean isBeacon = false;

    private MySystem mySystem;

    private TextView stateTextView = null;

    public BeaconScanController(Context context) {
        this.context = context;
        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));

        mySystem = MySystem.getInstance();

        stateTextView = (TextView) ((Activity)context).findViewById(R.id.beaconScannerTextView);
    }
    public void startBeaconScan() {
        isBeacon = false;
        if(!beaconManager.isBound(this)) {
            beaconManager.bind(this);
            stateTextView.setText("Scanner start");
        }
    }
    public void stopBeaconScan() {
        if(beaconManager.isBound(this)) {
            beaconManager.unbind(this);
            stateTextView.setText("Scanner stop");
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect()");
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if(collection.size() > 0) {
                    Iterator<Beacon> iterator = collection.iterator();
                    while(iterator.hasNext()) {
                        Beacon beacon = iterator.next();
                        UUID uuid = beacon.getId1().toUuid();
                        int major = beacon.getId2().toInt();
                        int minor = beacon.getId3().toInt();
                        if(uuid.toString().toUpperCase().equals(MY_UUID)) {
                            //Log.d(TAG, "beacon data is "+beacon.getDataFields());
                            //Log.d(TAG, "connected "+beacon.getDataFields().get(0)+" "+beacon.getDataFields().get(1));
                            isBeacon = true;
                            if(mySystem.getState() == MySystem.BEACON_STATE) {
                                mySystem.setState(MySystem.ACCEL_WAIT_STATE);
                            } else if(mySystem.getState() == MySystem.ACCEL_BEACON_STATE) {
                                if(mySystem.getCountryCode().equalsIgnoreCase("KR")) {
                                    if (mySystem.getTurnState() == MySystem.LEFT_TURN) {
                                        if (mySystem.getAccelXMinData() < beacon.getDataFields().get(0)) {
                                            mySystem.setState(MySystem.NOT_DRIVER_STATE);
                                            stateTextView.setText("Current state is " + MySystem.getInstance().getState());
                                        } else {
                                            mySystem.setState(MySystem.DRIVER_STATE);
                                            stateTextView.setText("Current state is " + MySystem.getInstance().getState());
                                        }
                                    } else if (mySystem.getTurnState() == MySystem.RIGHT_TURN) {
                                        if (mySystem.getAccelXMaxData() > beacon.getDataFields().get(0)) {
                                            mySystem.setState(MySystem.DRIVER_STATE);
                                            stateTextView.setText("Current state is " + MySystem.getInstance().getState());
                                        } else {
                                            mySystem.setState(MySystem.NOT_DRIVER_STATE);
                                            stateTextView.setText("Current state is " + MySystem.getInstance().getState());
                                        }
                                    } else {

                                    }
                                } else if (mySystem.getCountryCode().equalsIgnoreCase("JP")) {
                                    if (mySystem.getTurnState() == MySystem.RIGHT_TURN) {
                                        if (mySystem.getAccelXMinData() < beacon.getDataFields().get(0)) {
                                            mySystem.setState(MySystem.NOT_DRIVER_STATE);
                                            stateTextView.setText("Current state is " + MySystem.getInstance().getState());
                                        } else {
                                            mySystem.setState(MySystem.DRIVER_STATE);
                                            stateTextView.setText("Current state is " + MySystem.getInstance().getState());
                                        }
                                    } else if (mySystem.getTurnState() == MySystem.LEFT_TURN) {
                                        if (mySystem.getAccelXMaxData() > beacon.getDataFields().get(0)) {
                                            mySystem.setState(MySystem.DRIVER_STATE);
                                            stateTextView.setText("Current state is " + MySystem.getInstance().getState());
                                        } else {
                                            mySystem.setState(MySystem.NOT_DRIVER_STATE);
                                            stateTextView.setText("Current state is " + MySystem.getInstance().getState());
                                        }
                                    } else {

                                    }

                                }
                            }
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

    @Override
    public Context getApplicationContext() {
        return context;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);

    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return context.bindService(intent, serviceConnection, i);
    }
}
