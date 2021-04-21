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
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;

public class ScreenRecordService extends Service {
    private static final String TAG = "Man in here";
    private MediaRecorder mMediaRecorder = null;
    private boolean Running = false;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;


    private void initMediaRecorder(){
        this.mMediaRecorder = new MediaRecorder();
    }

    private void configMediaRecorder(){
        File videoFile = new File("/sdcard/video.mp4");
        Log.e(TAG, "文件路径="+videoFile.getAbsolutePath());
        if (videoFile.exists()){
            videoFile.delete();
        }
        this.mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);//设置音频输入源  也可以使用 MediaRecorder.AudioSource.MIC
        this.mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//设置视频输入源
        this.mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//音频输出格式
        this.mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//设置音频的编码格式
        this.mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置图像编码格式

//        mMediaRecorder.setVideoFrameRate(30);//要录制的视频帧率 帧率越高视频越流畅 如果设置设备不支持的帧率会报错  按照注释说设备会支持自动帧率所以一般情况下不需要设置
        this.mMediaRecorder.setVideoSize(1080,2220);//设置录制视频的分辨率  如果设置设备不支持的分辨率会报错
//        this.mMediaRecorder.setVideoEncodingBitRate(8*1920*1080);//设置比特率,比特率是每一帧所含的字节流数量,比特率越大每帧字节越大,画面就越清晰,算法一般是 5 * 选择分辨率宽 * 选择分辨率高,一般可以调整5-10,比特率过大也会报错
//        this.mMediaRecorder.setOrientationHint(90);//设置视频的摄像头角度 只会改变录制的视频文件的角度(对预览图像角度没有效果)

//        TextureView mTextureView = new TextureView(this);
//        Surface surface = new Surface(mTextureView.getSurfaceTexture());
//        this.mMediaRecorder.setPreviewDisplay(surface);//设置拍摄预览
        this.mMediaRecorder.setOutputFile(videoFile.getAbsolutePath());//MP4文件保存路径

    }

    private void startRecorder(){
        configMediaRecorder();
        try{
            this.mMediaRecorder.prepare();
            this.mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecorder(){
        this.mMediaRecorder.stop();
        this.mMediaRecorder.reset();
    }

    private void pauseRecorder(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mMediaRecorder.pause();//暂停
        }
    }

    private void resumeRecorder(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mMediaRecorder.resume();//恢复
        }
    }

    private void destroyRecorder(){
        if(this.mMediaRecorder != null) {
            try{
                this.mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                this.mMediaRecorder = null;
                this.mMediaRecorder = new MediaRecorder();
            } catch (RuntimeException e) {
                e.printStackTrace();
                this.mMediaRecorder = null;
                this.mMediaRecorder = new MediaRecorder();
            } catch (Exception e) {
                e.printStackTrace();
                this.mMediaRecorder = null;
                this.mMediaRecorder = new MediaRecorder();
            }
            this.mMediaRecorder.reset();
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
        }
    }

    public ScreenRecordService() {
    }

    public void start(){
        this.initMediaRecorder();
        this.startRecorder();
        this.Running = true;
    }

    public void stop(){
        this.destroyRecorder();
        this.Running = false;
    }

    public void pause(){
        if(Running){
            this.pauseRecorder();
            this.Running = false;
        }
    }

    public void resume(){
        if(!Running){
            this.resumeRecorder();
            this.Running = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
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
        int notifyID = 1;
        // 通知渠道的id
        String CHANNEL_ID = "my_channel_01";
        // Create a notification and set the notification channel.
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.vip_dialog_title_text)) .setContentText(name + "正在录制屏幕内容...")
                .setChannelId(CHANNEL_ID)
                .build();
        startForeground(1,notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}