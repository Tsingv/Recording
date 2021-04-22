package com.example.recording;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActivityChooserView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MANMAN";
    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.CAMERA};
    boolean mPassPermissions = true;
    private ScreenRecordService srecorder;
    private ScreenRecordService.Binder binder = null;
    private int mResultCode;
    private Intent mData;
    private MediaProjectionManager mMediaProjectionManager;

    private int REQUEST_MEDIA_PROJECTION = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        judgePermissions();
        if(!mPassPermissions) {
            ActivityCompat.requestPermissions(this, this.permissions, 99);
        }

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ScreenRecordService.Binder) service;
            srecorder = binder.getService();
            binder.setData(mResultCode, mData, mMediaProjectionManager);
            srecorder.start();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    srecorder.stop();
                }
            }, 5000);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK){
            Log.e("MANMAN", "Entered Result");
            Intent intent = new Intent(this, ScreenRecordService.class);
            mResultCode = resultCode;
            mData = data;
//            startForegroundService(intent);
            bindService(intent, conn, BIND_AUTO_CREATE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.e("FFFF", "asdasdadasd");
////        srecorder = new ScreenRecordService();
////        srecorder.start();
//        Intent intent = new Intent(this, ScreenRecordService.class);
//        startService(intent);
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                stopService(intent);
//            }
//        }, 5000);
//
//    }

    private void judgePermissions() {
        boolean permission = true;
        for (int i = 0; i < this.permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, this.permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                // 未授予的权限
                permission = false;
            }
        }
        this.mPassPermissions = permission;
    }

}