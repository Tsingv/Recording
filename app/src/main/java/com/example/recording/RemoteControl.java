package com.example.recording;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.icu.text.Collator;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public class RemoteControl extends Service {

    private static final String TAG = "Screenshot";
    private Intent callmeActivity;
    private static final int TIME_OUT = 10000;
    private String hostname;
    private int port;
    private BufferedReader in;
    private PrintWriter out;
    private RCThread rcthread;
    private boolean exitSocket = false;
    private int mWindowWidth;
    private int mWindowHeight;
    private Activity topActivity;

    public RemoteControl() {
    }

    public void start(){
        rcthread = new RCThread();
        rcthread.start();
    }
    public void stop(){
        exitSocket = true;
        rcthread.closeConnection();
        try {
            rcthread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        exitSocket = false;
    }

    public class RCThread extends Thread{

        private Socket socket;

        public RCThread(){
        }

        public void closeConnection(){
            try {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            try {
                socket = new Socket(hostname, port);
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(in != null && out!= null) {
                while (!exitSocket) {
                    out.write("Hello\n");
                    out.flush();
                    String Recvmsg;
                    try {
                        if (!socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && (Recvmsg = in.readLine())!=null) {
                            Log.e("RECV msg", Recvmsg);
                            if(Recvmsg.equals("Screenshot")){
                                Log.e("TOUCH", "IN PROCESS");
//                                topActivity =  scanForActivity(binder.getService().getApplicationContext());
                                topActivity = getCurrentActivity();
                                Looper.prepare();
                                Toast.makeText(getCurrentActivity(), "ready to shot", Toast.LENGTH_SHORT).show();
                                screenshot();
                                Looper.loop();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static Activity getCurrentActivity () {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
                    null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Activity scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    public Bitmap shotme(Activity activity){
        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();

        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        view.setDrawingCacheEnabled(true);

        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0, statusBarHeight, mWindowWidth, mWindowHeight-statusBarHeight);

        view.destroyDrawingCache();

        return bmp;
    }

    private void screenshot(){
        Bitmap mBitmap = shotme(topActivity);
        String mImagePath="/sdcard/";
        String mImageName = System.currentTimeMillis() + ".png";
        if (mBitmap != null) {
            File fileFolder = new File(mImagePath);
            if (!fileFolder.exists()) fileFolder.mkdirs();
            File file = new File(mImagePath, mImageName);
            if (!file.exists()) {
                Log.d(TAG, "file create success ");
                try {
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Log.d(TAG, "file save success ");
                    Toast.makeText(topActivity.getApplicationContext(), "Screenshot is done.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Log.d(TAG, "Image Failed! ");
            Toast.makeText(topActivity.getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        callmeActivity = intent;
        return binder;
    }

    public class Binder extends android.os.Binder {
        public RemoteControl getService(){
            return RemoteControl.this;
        }
        public void setData(String host, int p, int width, int height){
            hostname = host;
            port = p;
            mWindowWidth = width;
            mWindowHeight = height;
        }
    }
    private RemoteControl.Binder binder = new RemoteControl.Binder();
}