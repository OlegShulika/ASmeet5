package ru.olegshulika.asmeet5;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.solver.widgets.ConstraintHorizontalLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainAct";
    private TextView mC1Text1;  // Cell 1 (1,1) LinearLayout - horizontal
    private TextView mC1Text2;
    private TextView mC1Text3;
    private TextView mC2Text1;  // Cell 2 (1,2) RelativeLayout
    private TextView mC2Text2;
    private TextView mC2Text3;
    private TextView mC2Text4;
    private TextView mC2Text5;
    private TextView mC3Text1;  // Cell 3 (2,1) LinearLayout - vertical
    private TextView mC3Text2;
    private TextView mC3Text3;
    private Button mC4Button1;  // Cell 4 (2,2) ConstantLayout  - circle
    private Button mC4Button2;
    private boolean mC4running=true;
    private int mC4direction=1;     // +1 (CW)  -1(CCW)
    private long mc4delta=0;

    private LocalBroadcastManager mBroadcastManager = LocalBroadcastManager.getInstance(this);
    private CustomBroadcastReceiver mBroadcastReceiver = new CustomBroadcastReceiver(new ViewCallbackImpl());
    private IntentFilter mIntentFilter;

    private Messenger mTimeCell24Messenger = new Messenger(new IncomingTimeHandler());
    private Messenger mTimeServiceMessenger;

    private ServiceConnection mServiceConnection = new ServiceConnection()  {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTimeServiceMessenger = new Messenger(service);
            Message msg = Message.obtain(null, MsgCommand.REGISTER_CLIENT);
            msg.replyTo = mTimeCell24Messenger;
            try {
                mTimeServiceMessenger.send(msg);
            } catch(RemoteException ex) {
                ex.printStackTrace();
            }
            Log.d(TAG, "TimeService connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimeServiceMessenger=null;
            Log.d(TAG, "TimeService disconnected");
        }
    };

    private class IncomingTimeHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Long msec = (Long)msg.obj;
            mC2Text1.setText(getDateTime(msec,"dd/MM/yyyy"));
            mC2Text2.setText(getDateTime(msec,"hh:mm"));
            mC2Text3.setText(getDateTime(msec,".SSS"));
            mC2Text4.setText(getDateTime(msec,"ss"));
            mC2Text5.setText("ms="+msec);

            if (mC4running) {
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mC4Button1.getLayoutParams();
                long degree = (((msec / 1000) % 60) * 6 * mC4direction + 360 + mc4delta) % 360;
                lp.circleAngle = (float) degree;
                mC4Button1.setText(String.valueOf(degree) + (char) 0x00B0);
                mC4Button1.setLayoutParams(lp);
            }
        }
    }

    private static String getDateTime(long milliSeconds, String dateFormat)
    {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
        Log.d(TAG, " onCreate");
    }

    void initViews(){
        mC1Text1 = findViewById(R.id.cell1tv1);     // Cell 1
        mC1Text2 = findViewById(R.id.cell1tv2);
        mC1Text3 = findViewById(R.id.cell1tv3);
        mC2Text1 = findViewById(R.id.cell2tv1);     // Cell 2
        mC2Text2 = findViewById(R.id.cell2tv2);
        mC2Text3 = findViewById(R.id.cell2tv3);
        mC2Text4 = findViewById(R.id.cell2tv4);
        mC2Text5 = findViewById(R.id.cell2tv5);
        mC3Text1 = findViewById(R.id.cell3tv1);     // Cell 3
        mC3Text2 = findViewById(R.id.cell3tv2);
        mC3Text3 = findViewById(R.id.cell3tv3);
        mC4Button1 = findViewById(R.id.cell4btn1);
        mC4Button2 = findViewById(R.id.cell4btn2);

        this.setTitle(getString(R.string.app_name)+"...");
    }

    void initListeners() {
        mC4Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mC4direction *= -1;
            }
        });

        mC4Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mC4running ^= true;
                mC4Button2.setText(mC4running? "STOP":"RUN");
            }
        });
    }

    public void bindTimeService() {
        Log.d(TAG, "binding TimeService...");
        bindService(TimeService.newIntent(MainActivity.this),
                    mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindTimeService() {
        Log.d(TAG, "unbinding TimeService...");
        Message msg = Message.obtain(null, MsgCommand.UNREGISTER_CLIENT);
        msg.replyTo = mTimeCell24Messenger;
        try {
            mTimeServiceMessenger.send(msg);
        } catch(RemoteException ex) {
            ex.printStackTrace();
        }
        unbindService(mServiceConnection);
    }


    @Override
    protected void onStart() {
        Log.d(TAG, " onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, " onResume");
        super.onResume();
        bindTimeService();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, " onPause");
        super.onPause();
        unbindTimeService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, " onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, " onDestroy");
    }

}
