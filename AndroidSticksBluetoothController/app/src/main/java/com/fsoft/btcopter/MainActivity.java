package com.fsoft.btcopter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    BluetoothArduino arduino = new BluetoothArduino();
    enum Mode {LOITER, ALTHOLD, STABILIZE}
    Mode mode = Mode.LOITER;
    TextView myLabel = null;
    ToggleButton autotuneSwitch = null;
    Button buttonMode1 = null;
    Button buttonMode2 = null;
    Button buttonMode3 = null;
    Button buttonConnect = null;
    TextView labelOutput = null;
    StickView stick1 = null;
    StickView stick2 = null;


    Handler handler = new Handler();
    int colorDisable = Color.parseColor("#b6b6b6");
    int colorEnable = Color.parseColor("#68f2a2");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        myLabel = (TextView)findViewById(R.id.status_label);
        labelOutput = (TextView)findViewById(R.id.output_label);
        autotuneSwitch = (ToggleButton)findViewById(R.id.autotune_switch);
        buttonMode1 = (Button) findViewById(R.id.buttonMode1);
        buttonMode2 = (Button) findViewById(R.id.buttonMode2);
        buttonMode3 = (Button) findViewById(R.id.buttonMode3);
        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        stick1 = (StickView) findViewById(R.id.stick1);
        stick2 = (StickView) findViewById(R.id.stick2);

        buttonMode1.setBackgroundColor(colorDisable);
        buttonMode2.setBackgroundColor(colorDisable);
        buttonMode3.setBackgroundColor(colorDisable);
        buttonMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode1();
            }
        });
        buttonMode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode2();
            }
        });
        buttonMode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode3();
            }
        });

        arduino.setOnStatusMessage(new BluetoothArduino.OnStatusMessage() {
            @Override
            public void onStatusMessage(String text) {
                status(text);
            }
        });
        arduino.setOnConnected(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        buttonConnect.setText("Disconnect");
                        mode1();
                        startSendingData();
                    }
                });
            }
        });
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(arduino.isConnected()) {
                    buttonConnect.setText("Connect");
                    status("Not connected");
                    status("---------------------- (0ms)");

                    arduino.close();
                }
                else {
                    arduino.connect();
                }
            }
        });
    }
    protected void status(final String text){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(text.contains("ms)")) {
                    if (labelOutput != null)
                        labelOutput.setText(text);
                }
                else {
                    if (myLabel != null)
                        myLabel.setText(text);
                }
            }
        });
    }

    void mode1(){//loiter
        status("set Loiter mode");
        mode = Mode.LOITER;
        buttonMode1.setBackgroundColor(colorEnable);
        buttonMode2.setBackgroundColor(colorDisable);
        buttonMode3.setBackgroundColor(colorDisable);
    }
    void mode2(){//AltHold
        status("set AltHold mode");
        mode = Mode.ALTHOLD;
        buttonMode1.setBackgroundColor(colorDisable);
        buttonMode2.setBackgroundColor(colorEnable);
        buttonMode3.setBackgroundColor(colorDisable);
    }
    void mode3(){//Stabilize
        status("set Stabilize mode");
        mode = Mode.STABILIZE;
        buttonMode1.setBackgroundColor(colorDisable);
        buttonMode2.setBackgroundColor(colorDisable);
        buttonMode3.setBackgroundColor(colorEnable);
    }
    void startSendingData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    while (arduino != null && arduino.isConnected()) {
                        refresh();
                        Thread.sleep(100);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    status("Error sending: " + e);
                }
            }
        }).start();
    }
    @Override
    protected void onDestroy() {
        try {
            arduino.close();
        }
        catch (Exception e){

        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //do noth
    }

    void refresh(){
        try{
            //values 991-2004

            //1200;1300;1400;1500;1700;1800;1900;2000;|
            float[]  ch = new float[8];
            //--------------sticks
            ch[0] = stick2.positionH;
            ch[1] = stick2.positionV;
            ch[2] = stick1.positionV;
            ch[3] = stick1.positionH;
            //--------------mode
            if(mode == Mode.LOITER) ch[4] = 0f;
            if(mode == Mode.ALTHOLD) ch[4] = 0.5f;
            if(mode == Mode.STABILIZE) ch[4] = 1f;
            //--------------not used
            ch[5]= 0.5f;
            //--------------AutoTune
            ch[6]= autotuneSwitch.isChecked()?1f:0f;
            //--------------NotUsed
            ch[7]= 0.5f;

            float min = 991;
            float max = 2004;
            float interval = max-min;

            for(int i=0;i<8; i++)
                ch[i] = ch[i]*interval+min;

            String result = "";
            for(int i=0;i<8; i++)
                result += ch[i]+";";
            result += "|";

            arduino.send(result);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
