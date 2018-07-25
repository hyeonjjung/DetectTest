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

    private static LogManager mLogManager = new LogManager();

    private File appDirectory;
    private static FileWriter fileWriter;

    private String fileName = "DriverDetection";

    private boolean isFileAvailable = false;

    private LogManager() {
        fileInit(fileName);
    }

    public static LogManager getInstance() {
        return mLogManager;
    }
    private void fileInit (String fileName) {
        if(isExternalStorageWritable()) {
            appDirectory = new File(Environment.getExternalStorageDirectory()+"/"+fileName);

            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }
        }
    }
    public void makeFile(String location) {
        if(!isFileAvailable) {
            try {
                fileWriter = new FileWriter(new File(appDirectory, location+System.currentTimeMillis() + ".csv"));
                isFileAvailable = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Failed to make file...");
        }
        Log.d(TAG, fileWriter.toString());
    }


    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    /*
    * Log file에 들어가야하는 column 정리
    *
    *  Timestamp / state / sensor
    *
    * */

    public void writeFile(String sensor) {
        try {
            fileWriter.write(String.format("%s, %s, %s, %s\n", getCurrentTimeStamp(), MySystem.getInstance().getState(), MySystem.getInstance().getMagneticState().getState(), sensor));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "writeFile "+fileWriter.toString());
    }

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
