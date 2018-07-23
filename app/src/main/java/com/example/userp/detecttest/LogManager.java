package com.example.userp.detecttest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private Context mContext;

    private final String TAG = "LogManager";

    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private File appDirectory;
    private FileWriter fileWriter;

    private String fileName;

    private boolean isFileAvailable = false;

    public LogManager(Context context, String fileName) {
        mContext = context;
        this.fileName = fileName;

        permissionCheck();
        fileInit(fileName);

    }
    private void fileInit (String fileName) {
        if(isExternalStorageWritable()) {
            appDirectory = new File(Environment.getExternalStorageDirectory()+"/"+fileName);

            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }
        }
    }
    public void makeFile() {
        if(!isFileAvailable) {
            try {
                fileWriter = new FileWriter(new File(appDirectory, System.currentTimeMillis() + ".csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Failed to make file...");
        }
    }
    private void permissionCheck() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity) mContext,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    /*
    private void writeFile(String time, String state) {
        try {
            //fileWriter.write(String.format("%s, %f, %s\n", time, currentSpeed, state));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
    private static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
    public void stopWritingFile() {
        if(isFileAvailable) {
            try {
                fileWriter.close();
                isFileAvailable = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Failed to stop writing file...");
        }
    }
}
