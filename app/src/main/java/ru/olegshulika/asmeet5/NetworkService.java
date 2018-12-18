package ru.olegshulika.asmeet5;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

@interface Command {
    int INVALID = -1;
    int STOP = 0;
    int START = 1;
}

public class NetworkService extends Service {
    private static final String TAG = "_NetworkService";
    private static final int MODE = Service.START_NOT_STICKY;
    private static final String KEY_COMMAND = "service.cmd";
    private static final int SERVICE_WORKTIME_LIMIT = 1000;   // max work time in sec
    private boolean serviceStarted = false;

    private boolean isServiceStarted() {
        return serviceStarted;
    }

    private void setServiceStarted(boolean serviceStarted) {
        this.serviceStarted = serviceStarted;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.d(TAG, " onStartCommand "+flags+" "+startId);
        int cmd = getCommand(intent);
        if (cmd==Command.INVALID)
            return MODE;
        // start service
        if (cmd==Command.START && !isServiceStarted()) {
            Log.d(TAG, " started..." + startId);
            setServiceStarted(true);
            startSrv();
            return MODE;
        }
        // stop service
        if (cmd==Command.STOP && isServiceStarted()) {
            Log.d(TAG, " stopped...");
            stopSelf(startId);
            setServiceStarted(false);
            return MODE;
        }

        return MODE;
    }

    private void startSrv() {              // Service workout thread
        new Thread(new Runnable() {         // Start service
            @Override
            public void run() {
                setServiceStarted(true);
                int workTime=SERVICE_WORKTIME_LIMIT;
                while (--workTime>0 && isServiceStarted()){
                    try {
                        Thread.sleep(1000);             // wait 1 sec
                    } catch (Exception ex){
                        Log.d(TAG,ex.getMessage());
                    }
                }
                stopSelf();
                setServiceStarted(false);
            }
        }).start();
    }

    public static final Intent newIntent(Context context, int serviceCommand){
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(KEY_COMMAND, serviceCommand);
        return intent;
    }

    public static final Intent newIntent(Context context){
        return new Intent(context, NetworkService.class);
    }


    public int getCommand (Intent intent) {
        return intent.getIntExtra(KEY_COMMAND, Command.INVALID);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, " onCreate");
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, " onBind");
        return null;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, " onDestroy");
        stopSelf();
        setServiceStarted(false);
        super.onDestroy();
    }
}
