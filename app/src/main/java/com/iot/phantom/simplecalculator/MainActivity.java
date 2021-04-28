package com.iot.phantom.simplecalculator;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;


import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SerialInputOutputManager.Listener, View.OnClickListener {
    public class Constants {
        public static final int NUM_BUTTONS = 19;
        public static final int NUM_TEXTVIEWS = 3;
    }

    private View decorView;
    Button[] buttons = new Button[Constants.NUM_BUTTONS];
    TextView[] textViews = new TextView[Constants.NUM_TEXTVIEWS];
    String buffer, result;
    Boolean showResult = false;

    UsbSerialPort port;
    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    private void initUSBPort(){
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        if (availableDrivers.isEmpty()) {
            Log.d("UART", "UART is not available");

        }else {
            Log.d("UART", "UART is available");

            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {

                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
                manager.requestPermission(driver.getDevice(), usbPermissionIntent);

                manager.requestPermission(driver.getDevice(), PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));

                return;
            } else {

                port = driver.getPorts().get(0);
                try {
                    Log.d("UART", "openned succesful");
                    port.open(connection);
                    port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    //port.write("ABC#".getBytes(), 1000);

                    SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, this);
                    Executors.newSingleThreadExecutor().submit(usbIoManager);

                } catch (Exception e) {
                    Log.d("UART", "There is error");
                }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0) {
                    hideSystemBar();
                }
            }
        });

        initUSBPort();

        buffer = "";

        for (int i = 0; i < Constants.NUM_BUTTONS; i++) {
            String buttonID = "button" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
            buttons[i].setOnClickListener(this);
        }
        for (int i = 0; i < Constants.NUM_TEXTVIEWS; i++) {
            String buttonID = "textView" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            textViews[i] = findViewById(resID);
        }
    }

    @Override
    public void onClick(View view) {
        if (showResult == true) {
            result = "";
        }
        switch (view.getId()) {
            case R.id.button0:
                buffer += "7";
                break;
            case R.id.button1:
                buffer += "8";
                break;
            case R.id.button2:
                buffer += "9";
                break;
            case R.id.button3:
                buffer = buffer.substring(0, buffer.length() - 1);
                break;
            case R.id.button4:
                buffer = "";
                break;
            case R.id.button5:
                buffer += "4";
                break;
            case R.id.button6:
                buffer += "5";
                break;
            case R.id.button7:
                buffer += "6";
                break;
            case R.id.button8:
                buffer += "\u002b";
                break;
            case R.id.button9:
                buffer += "\u2212";
                break;
            case R.id.button10:
                buffer += "1";
                break;
            case R.id.button11:
                buffer += "2";
                break;
            case R.id.button12:
                buffer += "3";
                break;
            case R.id.button13:
                buffer += "\u00D7";
                break;
            case R.id.button14:
                buffer += "\u00F7";
                break;
            case R.id.button15:
                buffer += "0";
                break;
            case R.id.button16:
                buffer += ".";
                break;
            case R.id.button17:
                buffer += "Ans";
                break;
            case R.id.button18:
                result = "Hello";
                showResult = true;
                break;
        }
        textViews[0].setText(buffer);
        textViews[1].setText(result);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(hideSystemBar());
        }
    }

    private int hideSystemBar() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    @Override
    public void onNewData(byte[] data) {

    }

    @Override
    public void onRunError(Exception e) {

    }
}
