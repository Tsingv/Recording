package com.example.recording;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenRecordService extends Service {

    private static final String TAG = "MANMAN";

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Surface mSurface;
    private MediaCodec mMediaCodec;
    private MediaMuxer mMuxer;
    private String mVideoPath = "/sdcard/";
    private boolean mMuxerStarted = false;
    private int mVideoTrackIndex = -1;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private AtomicBoolean mIsQuit = new AtomicBoolean(false);
    private boolean isRecordOn;
    private int mResultCode;
    private Intent mData;
    private int mWindowWidth;
    private int mWindowHeight;
    private int mScreenDensity;
    private MediaRecorder mediaRecorder;
    private boolean mDone = true;
    private boolean mRunning = false;

    public void start(){
        if(mDone){
            mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mData);
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(
                    mVideoPath + System.currentTimeMillis() + ".mp4");
            mediaRecorder.setVideoSize(mWindowWidth, mWindowHeight);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
            mediaRecorder.setVideoFrameRate(30);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("record_screen",
                    mWindowWidth, mWindowHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mediaRecorder.getSurface(), null, null);
            mediaRecorder.start();
            mRunning = true;
            mDone = false;
        }
    }

    public void stop(){
        if(mRunning) {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }
            if (mMediaProjection != null){
                mMediaProjection.stop();
                mMediaProjection = null;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        createNotificationChannel();
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 通知渠道的id
        String id = "my_channel_01";
        // 用户可以看到的通知渠道的名字.
        CharSequence name = getString(R.string.app_name);
//         用户可以看到的通知渠道的描述
        String description = "描述描述";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        mChannel = new NotificationChannel(id, name, importance);

//         配置通知渠道的属性
        mChannel.setDescription(description);
//         设置通知出现时的闪灯（如果 android 设备支持的话）
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
//         设置通知出现时的震动（如果 android 设备支持的话）
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//         最后在notificationmanager中创建该通知渠道 //
        mNotificationManager.createNotificationChannel(mChannel);

        // 为该通知设置一个id
        int notifyID = 1456789876;
        // 通知渠道的id
        String CHANNEL_ID = "my_channel_01";
        // Create a notification and set the notification channel.
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.vip_dialog_title_text)) .setContentText(name + "正在录制屏幕内容...")
                .setChannelId(CHANNEL_ID)
                .build();
        startForeground(1456789876,notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    public class Binder extends android.os.Binder {
        public ScreenRecordService getService(){
            return ScreenRecordService.this;
        }
        public void setData(int code, Intent data, MediaProjectionManager manager, int width, int height, int dense){
            mResultCode = code;
            mData = data;
            mMediaProjectionManager = manager;
            mWindowWidth = width;
            mWindowHeight = height;
            mScreenDensity = dense;
        }
    }
    private Binder binder = new Binder();
}

