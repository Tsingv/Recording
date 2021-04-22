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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity<button> extends AppCompatActivity {

    private static final String TAG = "MANMAN";
    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.CAMERA};
    boolean mPassPermissions = true;
    private ScreenRecordService srecorder = null;
    private ScreenRecordService.Binder binder = null;
    private int mResultCode;
    private Intent mData;
    private MediaProjectionManager mMediaProjectionManager;

    private WindowManager mWindowManager;
    private int mWindowWidth;
    private int mWindowHeight;
    private DisplayMetrics displayMetrics;
    private int mScreenDensity;

    private int REQUEST_MEDIA_PROJECTION = 12999;
    private int REQUEST_SHORTCUT = 17182;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWindowWidth = mWindowManager.getDefaultDisplay().getWidth();
        mWindowHeight = mWindowManager.getDefaultDisplay().getHeight();
        displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.densityDpi;

        Button button = (Button) findViewById(R.id.btn_stop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("STOP", "CLICKED");
                if(srecorder != null) {
                    try {
                        srecorder.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    unbindService(conn);
                    srecorder = null;
                }
            }
        });
        Button button2 = (Button) findViewById(R.id.btn_start);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("START", "CLICKED");
                if(srecorder == null) {
                    mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                }
            }
        });
        Button button3 = (Button) findViewById(R.id.btn_shot);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ShortCut", "CLICKED");
                Intent intent = new Intent(MainActivity.this, ScreenRecordService.class);
                intent.putExtra("width", mWindowWidth);
                intent.putExtra("height", mWindowHeight);
                startService(intent);
            }
        });

        judgePermissions();
        if(!mPassPermissions) {
            ActivityCompat.requestPermissions(this, this.permissions, 99);
        }


    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (ScreenRecordService.Binder) service;
            srecorder = binder.getService();
            binder.setData(mResultCode, mData, mMediaProjectionManager, mWindowWidth, mWindowHeight, mScreenDensity);
            Toast.makeText(MainActivity.this, "ready", Toast.LENGTH_SHORT).show();
            srecorder.start();
            Log.e("HERE", "started");
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
            Log.e("BLOCKED", "You've binded");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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