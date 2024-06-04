package com.example.torch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.BatteryManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;

public class MainActivity extends AppCompatActivity {

    private ToggleButton toggleFlashLightOnOff;
    private ToggleButton toggleStrobe;
    private Button buttonSOS;
    private TextView batteryStatus;
    private CameraManager cameraManager;
    private String getCameraID;
    private Handler strobeHandler;
    private boolean isStrobeOn = false;
    private boolean isSOSOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the UI elements
        toggleFlashLightOnOff = findViewById(R.id.toggle_flashlight);
        toggleStrobe = findViewById(R.id.toggle_strobe);
        buttonSOS = findViewById(R.id.button_sos);
        batteryStatus = findViewById(R.id.battery_status);

        // Initialize the CameraManager
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            getCameraID = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Register battery status receiver
        this.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void toggleFlashLight(View view) {
        if (toggleFlashLightOnOff.isChecked()) {
            setFlashLight(true);
        } else {
            setFlashLight(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void toggleStrobe(View view) {
        if (toggleStrobe.isChecked()) {
            isStrobeOn = true;
            strobeHandler = new Handler(Looper.getMainLooper());
            strobeHandler.post(strobeRunnable);
        } else {
            isStrobeOn = false;
            if (strobeHandler != null) {
                strobeHandler.removeCallbacks(strobeRunnable);
            }
            setFlashLight(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendSOS(View view) {
        if (!isSOSOn) {
            isSOSOn = true;
            new Thread(new SOSSender()).start();
        }
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryStatus.setText("Battery Level: " + level + "%");
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setFlashLight(boolean status) {
        try {
            cameraManager.setTorchMode(getCameraID, status);
            String message = status ? "Flashlight is turned ON" : "Flashlight is turned OFF";
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Runnable strobeRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            if (isStrobeOn) {
                try {
                    cameraManager.setTorchMode(getCameraID, true);
                    Thread.sleep(100);
                    cameraManager.setTorchMode(getCameraID, false);
                    Thread.sleep(100);
                } catch (CameraAccessException | InterruptedException e) {
                    e.printStackTrace();
                }
                strobeHandler.post(strobeRunnable);
            }
        }
    };

    private class SOSSender implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            try {
                for (int i = 0; i < 3; i++) {
                    cameraManager.setTorchMode(getCameraID, true);
                    Thread.sleep(200);
                    cameraManager.setTorchMode(getCameraID, false);
                    Thread.sleep(200);
                }
                for (int i = 0; i < 3; i++) {
                    cameraManager.setTorchMode(getCameraID, true);
                    Thread.sleep(600);
                    cameraManager.setTorchMode(getCameraID, false);
                    Thread.sleep(600);
                }
                for (int i = 0; i < 3; i++) {
                    cameraManager.setTorchMode(getCameraID, true);
                    Thread.sleep(200);
                    cameraManager.setTorchMode(getCameraID, false);
                    Thread.sleep(200);
                }
                isSOSOn = false;
            } catch (CameraAccessException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void finish() {
        super.finish();
        try {
            cameraManager.setTorchMode(getCameraID, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
