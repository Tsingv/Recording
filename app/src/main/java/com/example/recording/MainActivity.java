package com.example.recording;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActivityChooserView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.CAMERA};
    boolean mPassPermissions = true;
    ScreenRecordService srecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        judgePermissions();
        if(!mPassPermissions) {
            ActivityCompat.requestPermissions(this, this.permissions, 99);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("FFFF", "asdasdadasd");
        srecorder = new ScreenRecordService();
        srecorder.start();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                srecorder.stop();
            }
        }, 5000);

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