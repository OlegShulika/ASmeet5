package ru.olegshulika.asmeet5;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

@interface MsgCommand {
    int REGISTER_CLIENT = 0;
    int UNREGISTER_CLIENT = 1;
    int SET_VALUE = 2;
}

public class TimeService extends Service {
    private static final String TAG = "_TimeService";
    private List<Messenger> mClients = new ArrayList<Messenger>();
    private Messenger mMessenger = new Messenger (new IncomingHandler());
    private static final int SERVICE_WORKTIME_LIMIT = 1000000;   // max work time in msec
    private static final int SERVICE_WORKTIME_STEP = 100;   // work step in msec

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MsgCommand.REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    if (!isServiceStarted()) {
                        startSrv();
                        setServiceStarted(true);
                    }
                    break;
                case MsgCommand.UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    if (mClients.isEmpty()) {
                        stopSelf();
                        setServiceStarted(false);
                    }
                    break;
                case MsgCommand.SET_VALUE:
                    break;
                default:
                    break;
            }
        }
    }
    private boolean serviceStarted = false;
    private boolean isServiceStarted() {
        return serviceStarted;
    }
    private void setServiceStarted(boolean serviceStarted) {
        this.serviceStarted = serviceStarted;
    }

    public static final Intent newIntent(Context context){
        return new Intent(context, TimeService.class);
    }

    private void startSrv() {              // Service workout thread
        new Thread(new Runnable() {         // Start service
            @Override
            public void run() {
                Log.d(TAG,"StartSrv()");
                int workTime=SERVICE_WORKTIME_LIMIT;
                while (workTime>0 && isServiceStarted()){
                    workTime-=SERVICE_WORKTIME_STEP;
                    try {
                        Thread.sleep(SERVICE_WORKTIME_STEP);             // wait some time
                        long currentTimeMs = System.currentTimeMillis();
                        //Log.d(TAG, "==>"+currentTimeMs);
                        for(Messenger clientMsgr : mClients){
                            Message msg = Message.obtain(null,0, (Long)currentTimeMs);
                            clientMsgr.send(msg);
                        }
                    }
                    catch (Exception ex){
                        Log.d(TAG,ex.getMessage());
                    }
                }
                stopSelf();
                setServiceStarted(false);
            }
        }).start();
    }


    @Override
    public void onCreate() {
        Log.d(TAG, " onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, " onBind");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, " onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, " onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, " onDestroy");
        stopSelf();
        super.onDestroy();
    }

}
