package com.example.userp.detecttest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by userp on 2018-04-12.
 */

public class BeaconController {

    private static final String TAG = "BeaconController";

    private BeaconTransmitter beaconTransmitter = null;
    private BeaconManager beaconManager = null;
    private BeaconConsumer beaconConsumer = null;

    public void startBeaconTransmitter(Context context) {
        if(checkPrerequisites(context)) {
            beaconTransmitter = new BeaconTransmitter(context, new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
            Beacon beacon = new Beacon.Builder()
                    .setId1("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6")
                    .setId2("1")
                    .setId3("2")
                    .setManufacturer(0x0000) // Choose a number of 0x00ff or less as some devices cannot detect beacons with a manufacturer code > 0x00ff
                    .setTxPower(-59)
                    .setDataFields(Arrays.asList(new Long[]{0l}))
                    .build();
            beaconTransmitter.startAdvertising(beacon);
            Log.d(TAG, "start beacon advertising");
        }
    }
    public void stopBeaconTransmitter() {
        if (beaconTransmitter != null) {
            beaconTransmitter.stopAdvertising();
        }
    }

    private boolean checkPrerequisites(Context context) {

        if (android.os.Build.VERSION.SDK_INT < 18) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bluetooth LE not supported by this device's operating system");
            builder.setMessage("You will not be able to transmit as a Beacon");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                }

            });
            builder.show();
            return false;
        }
        if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bluetooth LE not supported by this device");
            builder.setMessage("You will not be able to transmit as a Beacon");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                }

            });
            builder.show();
            return false;
        }
        try {
            ((BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().getBluetoothLeAdvertiser();
        } catch(Exception e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bluetooth LE advertising unavailable");
            builder.setMessage("Sorry, the operating system on this device does not support Bluetooth LE advertising.  As of July 2014, only the Android L preview OS supports this feature in user-installed apps.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                }
            });
            builder.show();
            return false;
        }
        return true;
    }
}
