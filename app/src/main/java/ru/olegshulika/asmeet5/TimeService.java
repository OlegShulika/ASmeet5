package ru.olegshulika.asmeet5;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

@interface Command {
    int INVALID = -1;
    int STOP = 0;
    int START = 1;
}

public class TimeService extends Service {
    private static final int MODE = Service.START_NOT_STICKY;
    private static final String TAG = "_TimeService";
    private static final String KEY_COMMAND = "timeservice.cmd";
    private static final String KEY_DATA = "timeservice.data";
    private static final int SERVICE_WORKTIME_LIMIT_MS = 1000000;   // max work time in msec
    private static final int SERVICE_TIMESTEP_MS = 100;             // service 1 step time in msec
    private IBinder mBinder = new LocalBinder();
    private Handler handlerMsg = null;
    private boolean serviceStarted = false;

    private boolean isServiceStarted() {
        return serviceStarted;
    }

    private void setServiceStarted(boolean serviceStarted) {
        this.serviceStarted = serviceStarted;
    }

    public class LocalBinder extends Binder {
        TimeService getService() {
            return TimeService.this;
        }
        void setHandler(Handler handlerM) {
            handlerMsg = handlerM;
        }
    }

    private TimeService() {
    }
    @Override

    public void onCreate() {
        Log.d(TAG, " onCreate");
        super.onCreate();
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
            startSrv(startId);
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

    private void startSrv(final int startId) {              // Service workout thread
        new Thread(new Runnable() {         // Start service
            @Override
            public void run() {
                int workTime=SERVICE_WORKTIME_LIMIT_MS;
                while (workTime>0 && isServiceStarted()){
                    workTime -= SERVICE_TIMESTEP_MS;
                    try {
                        Thread.sleep(SERVICE_TIMESTEP_MS);             // wait 1 sec
                        String strMsg = "msg"+System.currentTimeMillis();
                        Log.d(TAG,"==>"+strMsg);
                        if (handlerMsg!=null) {
                            Message msg = handlerMsg.obtainMessage(0, strMsg);
                            handlerMsg.sendMessage(msg);
                        }
                    } catch (Exception ex){
                        Log.d(TAG,ex.getMessage());
                    }
                }
                stopSelf(startId);
                setServiceStarted(false);
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static final Intent newIntent(Context context, int serviceCommand){
        Intent intent = new Intent(context, TimeService.class);
        intent.putExtra(KEY_COMMAND, serviceCommand);
        return intent;
    }

    public static final Intent newIntent(Context context){
        return new Intent(context, TimeService.class);
    }

    public int getCommand (Intent intent) {
        return intent.getIntExtra(KEY_COMMAND, Command.INVALID);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, " onDestroy");
        stopSelf();
        setServiceStarted(false);
        super.onDestroy();
    }

}
