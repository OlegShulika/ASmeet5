package ru.olegshulika.asmeet5;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    private TextView mC3Text1;  // Cell 1 (2,1) LinearLayout - vertical
    private TextView mC3Text2;
    private TextView mC3Text3;
    private Button mC4Button1;  // Cell 1 (2,2) ConstantLayout  - circle
    private Button mC4Button2;

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

            }
        });

        mC4Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, " onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, " onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, " onPause");
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
