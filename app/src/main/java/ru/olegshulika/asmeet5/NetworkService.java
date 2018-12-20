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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.List;

@interface Command {
    int INVALID = -1;
    int STOP = 0;
    int START = 1;
    int ADD_URL = 2;
}

public class NetworkService extends Service {
    public static final String  KEY_BROADCAST = "ru.olegshulika.asmeet5.LOCAL_BROADCAST";
    public static final String  KEY_URL = "LOCAL_BROADCAST_URL";
    public static final String  KEY_LATENCY = "LOCAL_BROADCAST_LATENCY";

    private static final String TAG = "_NetworkService";
    private static final int MODE = Service.START_NOT_STICKY;
    private static final String KEY_COMMAND = "service.cmd";
    private static final String KEY_ADDURL = "add.url.cmd";
    private List<String> mUrlList = new ArrayList<String>();
    private static final int SERVICE_WORKCOUNT_LIMIT = 900;     // 900 sec (15 min)
    private static final int SERVICE_SLEEP_TIME = 2000;         // wait 2 sec

    private boolean serviceStarted = false;
    private boolean isServiceStarted() {
        return serviceStarted;
    }
    private void setServiceStarted(boolean serviceStarted) {
        this.serviceStarted = serviceStarted;
    }

    private LocalBroadcastManager localBroadcastManager;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.d(TAG, " onStartCommand "+flags+" "+startId);
        switch (getCommand(intent)) {
            case Command.START:
                if (isServiceStarted())
                    return MODE;
                Log.d(TAG, " starting..." + startId);
                setServiceStarted(true);
                startSrv();
                break;
            case Command.STOP:
                if (!isServiceStarted())
                    return MODE;
                Log.d(TAG, " stopping...");
                stopSelf(startId);
                setServiceStarted(false);
                mUrlList.clear();
                break;
            case Command.ADD_URL:
                mUrlList.add(getURL(intent));
                break;
            case Command.INVALID:
                Log.d(TAG, " Invalid command");
                break;
            default:
                break;
        }
        return MODE;
    }

    private void startSrv() {              // Service workout thread
        new Thread(new Runnable() {         // Start service
            @Override
            public void run() {
                setServiceStarted(true);
                int workTime=SERVICE_WORKCOUNT_LIMIT;
                while (--workTime>0 && isServiceStarted()){
                    try {
                        Thread.sleep(SERVICE_SLEEP_TIME);
                        for(String url : mUrlList) {
                            long latency = PingHost(url);
                            Log.d(TAG, url + " lat=" + latency);
                            sendLocalBroadcast(url, latency);
                        }
                    } catch (Exception ex){
                        Log.d(TAG,ex.getMessage());
                    }
                }
                stopSelf();
                setServiceStarted(false);
            }
        }).start();
    }

    private void sendLocalBroadcast(String url, long latency) {
        Intent broadcastIntent = new Intent(KEY_BROADCAST);
        broadcastIntent.putExtra(KEY_URL, url);
        broadcastIntent.putExtra(KEY_LATENCY, latency);
        localBroadcastManager.sendBroadcast(broadcastIntent);
        Log.d(TAG,"sendLocalBroadcast "+url);
    }

    public static final Intent newIntent(Context context, int serviceCommand){
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(KEY_COMMAND, serviceCommand);
        return intent;
    }

    public static final Intent newIntent(Context context, String URL){
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(KEY_COMMAND, Command.ADD_URL);
        intent.putExtra(KEY_ADDURL, URL);
        return intent;
    }


    public static final Intent newIntent(Context context){
        return new Intent(context, NetworkService.class);
    }


    public int getCommand (Intent intent) {
        return intent.getIntExtra(KEY_COMMAND, Command.INVALID);
    }

    public String getURL (Intent intent) {
        return intent.getStringExtra(KEY_ADDURL);
    }


    @Override
    public void onCreate() {
        Log.d(TAG, " onCreate");
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
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

    private long PingHost (String host) {
        int timeout = 5000;
        long beforeTime = System.currentTimeMillis();
        if (isConnectionReachable(host)==false)
            return -1;
        long afterTime = System.currentTimeMillis();
        return afterTime - beforeTime;
    }

    private static boolean isConnectionReachable(String hostname)
    {
        try {
            URL url = new URL(hostname);

            HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
            Object objData = urlConnect.getContent();

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return false;
        }

        return true;
    }
}
