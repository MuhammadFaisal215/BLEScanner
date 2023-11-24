package com.blescanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BLEDetectionModule extends ReactContextBaseJavaModule {
    private static final String TAG = "BLEDetectionModule";
    private static final int SCAN_PERIOD = 2000; // Scan for 2 seconds
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings settings;
    private ScanFilter filter;
    private boolean scanning = false;

    public BLEDetectionModule(ReactApplicationContext reactContext) {
        super(reactContext);

        BluetoothManager bluetoothManager = (BluetoothManager) reactContext.getSystemService(ReactApplicationContext.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            Log.e(TAG, "kk is null");

            bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        } else {
            Log.e(TAG, "BluetoothManager is null");
        }
    }

    @Override
    public String getName() {
        return "BLEDetectionModule";
    }

    @ReactMethod
    public void startScanning(Callback successCallback) {
        Log.e(TAG, "Function k under");
        successCallback.invoke("in side scanning");
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.e(TAG, "Current activity is null");
            return;
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not enabled");
            return;
        }
        if (!scanning) {
            Log.e(TAG, "inside scanning");
            scanning = true;
            if (!requestPermissions()) {
                return;
            }
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();
            filter = new ScanFilter.Builder()
//                    .setDeviceName("MBeacon")
                    .build();
            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(filter);
            bluetoothLeScanner.startScan(scanCallback);
            // Stop scanning after a SCAN_PERIOD
            new android.os.Handler().postDelayed(() -> {
                scanning = false;
                Log.e(TAG, "stop scan");
                bluetoothLeScanner.stopScan(scanCallback);
            }, SCAN_PERIOD);
        }
    }

    private boolean requestPermissions() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.e(TAG, "Current activity is null");
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] permissions = {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
            ActivityCompat.requestPermissions(currentActivity, permissions, 1);
            return  bluetoothScanPermission(currentActivity) && bluetoothConnectPermission(currentActivity) &&
                    courseLocationPermission(currentActivity) && fineAccessLocationPermission(currentActivity)
                    && bluetoothPermission(currentActivity) && backgroundLocationPermission(currentActivity);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            };
            ActivityCompat.requestPermissions(currentActivity, permissions, 1);
            return bluetoothPermission(currentActivity) && bluetoothAdminPermission(currentActivity) && fineAccessLocationPermission(currentActivity);
        } else {
            Log.e(TAG, "not any os found");
            return false;
        }
    }

    private boolean bluetoothScanPermission(Activity currentActivity) {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Log.e(TAG, "BLUETOOTH_SCAN permissions not granted");
        return false;
    }

    private boolean bluetoothConnectPermission(Activity currentActivity) {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Log.e(TAG, "BLUETOOTH_CONNECT permissions not granted");
        return false;
    }

    private boolean bluetoothPermission(Activity currentActivity) {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
//            ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.BLUETOOTH}, 1);
            Log.e(TAG, "BLUETOOTH permissions not granted");
            return false;
        }
    }

    private boolean bluetoothAdminPermission(Activity currentActivity) {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Log.e(TAG, "BLUETOOTH_ADMIN permissions not granted");
        return false;
    }

    private boolean backgroundLocationPermission(Activity currentActivity) {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
//            ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
            Log.e(TAG, "ACCESS_BACKGROUND_LOCATION permissions not granted");
            return false;
        }
    }

    private boolean fineAccessLocationPermission(Activity currentActivity) {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Log.e(TAG, "ACCESS_FINE_LOCATION permissions not granted");
        return false;
    }

    private boolean courseLocationPermission(Activity currentActivity) {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Log.e(TAG, "ACCESS_COARSE_LOCATION permissions not granted");
        return false;
    }

    @ReactMethod
    public void stopScanning() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled() && scanning) {
            scanning = false;
            if (!requestPermissions()) {
                return;
            }
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            sendEvent("BeaconData", parseScanResult(result));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                sendEvent("BeaconData", parseScanResult(result));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with error code: " + errorCode);
        }
    };

    private void sendEvent(String eventName, Map<String, Object> params) {
//        Log.e(TAG, "Inside send event");
//        Log.e(TAG, params.toString());
//        getReactApplicationContext()
//                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                .emit(eventName, params);
    }

    private Map<String, Object> parseScanResult(ScanResult result) {
        Map<String, Object> beaconData = new HashMap<>();

        if (Objects.equals(result.getDevice().getName(), "MBeacon") || true) {
            Log.e(TAG, "Parse scan result");
//            Log.e(TAG, result.getDevice().getName());
            Log.e(TAG, result.getDevice().getAddress());
            beaconData.put("deviceName", result.getDevice().getName());
            beaconData.put("deviceAddress", result.getDevice().getAddress());
        }

        // Add other relevant information from the ScanResult
        return beaconData;
    }
}