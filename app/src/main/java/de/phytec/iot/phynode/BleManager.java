package de.phytec.iot.phynode;

/*
    Copyright 2017  PHYTEC Messtechnik GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleManager {
    private final static String TAG = "BleManager";
    private final Activity mActivity;
    private boolean mIsScanning;
    private boolean mIsConnected;
    private ArrayList<byte[]> mBufferData;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<BluetoothGattService> mBluetoothGattServices;
    private ArrayList<ScanFilter> mScanFilters;
    private ScanSettings.Builder mScanSettings;
    private String mBleDeviceAddress = null;
    private BleCallbacks mBleCallbacks = null;

    public enum Status {
        NONE,
        SERVICES_DISCOVERED,
        WROTE_INIT,
        WROTE_BUFFER_COLUMN,
        WROTE_BUFFER,
        WROTE_UPDATE
    };
    private Status mStatus;

    BleManager(final Activity context, final BleCallbacks callbacks) {
        mActivity = context;
        mBleCallbacks = callbacks;
        mScanFilters = new ArrayList<>();
        mIsScanning = false;
        mIsConnected = false;
        mStatus = Status.NONE;

        if (!getBleAdapter()) {
            return;
        }

        if (!mBluetoothAdapter.enable()) {
            PhytecLog.e(TAG, "Could not enable BLE adapter");
            return;
        }

        mScanSettings = new ScanSettings.Builder();
        mScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
    }

    // private

    private boolean getBleAdapter() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager)
                    mActivity.getSystemService(Context.BLUETOOTH_SERVICE);

            if (mBluetoothManager == null) {
                PhytecLog.e(TAG, "getBleAdapter(): Unable to initialize BluetoothManager");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            PhytecLog.e(TAG, "getBleAdapter(): Unable to obtain a BluetoothAdapter");
            return false;
        }

        return true;
    }

    // public

    public boolean isBluetoothEnabled() {
        final BluetoothManager manager = (BluetoothManager)
                mActivity.getSystemService(Context.BLUETOOTH_SERVICE);

        return (manager.getAdapter() != null) &&
                mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isLocationEnabled() {
        final LocationManager manager = (LocationManager)
                mActivity.getSystemService(Context.LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setScanFilters(final ArrayList<String> filterList) {
        if (filterList.isEmpty()) {
            mScanFilters.clear();
            return;
        }

        for (String filter : filterList) {
            ScanFilter.Builder scanFilterEpaper = new ScanFilter.Builder();
            scanFilterEpaper.setDeviceName(filter);
            mScanFilters.add(scanFilterEpaper.build());
        }
    }

    public ArrayList<String> getScanFilters() {
        if (mScanFilters.isEmpty()) {
            return null;
        }

        ArrayList<String> scanFilters = new ArrayList<>();

        for (ScanFilter filter : mScanFilters) {
            scanFilters.add(filter.getDeviceName());
        }

        return scanFilters;
    }

    public void startScan() {
        if (mIsScanning) {
            PhytecLog.i(TAG, "startScan(): Discovery already in progress");
            return;
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (mScanFilters.isEmpty())
            mBluetoothLeScanner.startScan(mScanCallback);
        else
            mBluetoothLeScanner.startScan(mScanFilters, mScanSettings.build(), mScanCallback);

        mIsScanning = true;
        mBleCallbacks.scanStarted();

        PhytecLog.i(TAG, "startScan(): Discovery started");
    }

    public void stopScan() {
        if(!mIsScanning) {
            return;
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.stopScan(mScanCallback);
        mIsScanning = false;
        mBleCallbacks.scanStopped();

        PhytecLog.i(TAG, "stopScan(): Discovery stopped");
    }

    public boolean isScanning() {
        return mIsScanning;
    }

    public boolean connect() {
        if (mIsConnected) {
            PhytecLog.i(TAG, "connect(): Already connected, nothing to do");
            return true;
        }

        if (mBleDeviceAddress == null) {
            PhytecLog.e(TAG, "connect(): mBleDeviceAddress is not specified");
            return false;
        }

        stopScan();

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mBleDeviceAddress);
        mBluetoothGatt = mBluetoothDevice.connectGatt(mActivity, false, mBleGattCallback);

        PhytecLog.i(TAG, "connect(): Connecting to " + mBleDeviceAddress);

        return true;
    }

    public void disconnect() {
        if (!mIsConnected) {
            PhytecLog.i(TAG, "disconnect(): Already disconnected, nothing to do");
            return;
        }

        mBluetoothGatt.disconnect();
        mStatus = Status.NONE;

        PhytecLog.i(TAG, "disconnect(): Disconnecting from " + mBleDeviceAddress);
    }

    public void readCharacteristic(UUID service, UUID characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            PhytecLog.e(TAG, "readCharacteristic(): mBluetoothAdapter or mBluetoothGatt not initialized");
            return;
        }

        BluetoothGattService s = mBluetoothGatt.getService(service);

        if (s == null) {
            PhytecLog.e(TAG, "readCharacteristic(): service not found");
            return;
        }

        BluetoothGattCharacteristic c = s.getCharacteristic(characteristic);

        if (!mBluetoothGatt.readCharacteristic(c))
            PhytecLog.e(TAG, "readCharacteristic(): failed");
    }

    public void writeCharacteristic(UUID service,
                                    UUID characteristic,
                                    byte[] value,
                                    final int writeType) {
        BluetoothGattService s = mBluetoothGatt.getService(service);

        if (mBluetoothAdapter == null) {
            PhytecLog.e(TAG, "writeCharacteristic(): mBluetoothAdapter is null");
            return;
        }

        if (mBluetoothGatt == null) {
            PhytecLog.e(TAG, "writeCharacteristic(): mBluetoothGatt is null");
            return;
        }

        if (s == null) {
            PhytecLog.e(TAG, "writeCharacteristic(): service not found");
            return;
        }

        BluetoothGattCharacteristic c = s.getCharacteristic(characteristic);

        if (c == null) {
            PhytecLog.e(TAG, "writeCharacteristic(): characteristic not found");
            return;
        }

        c.setWriteType(writeType);
        c.setValue(value);

        if (!mBluetoothGatt.writeCharacteristic(c)) {
            PhytecLog.e(TAG, "writeCharacteristic(): initializing write operation failed");
        }
    }

    public void sendEpaperImage(ArrayList<byte[]> data) {
        if (data.isEmpty()) {
            PhytecLog.e(TAG, "sendEpaperImage(): data is empty, nothing to write");
            return;
        }

        if (data.size() != 250) {
            PhytecLog.e(TAG, "sendEpaperImage(): data is of wrong size (should be 250)");
            return;
        }

        mBufferData = new ArrayList<>(data);
        mBleCallbacks.sendingStarted();

        // TODO: This triggers service discovery which triggers epaper callback, though it should
        // only do so if the user sends an image. When reading and writing other characteristics
        // this behaviour should be changed.
        connect();
    }

    public String getBleDeviceAddress() {
        return mBleDeviceAddress;
    }

    public void setBleDeviceAddress(final String address) {
        mBleDeviceAddress = address;
    }

    public List<BluetoothGattService> getBleServices() {
        return mBluetoothGattServices;
    }

    public List<BluetoothGattCharacteristic> getBleCharacteristics(final int location) {
        return mBluetoothGattServices.get(location).getCharacteristics();
    }

    // callbacks

    private BleEpaperCallbacks mBleEpaperCallback = new BleEpaperCallbacks() {
        @Override
        public void onServicesDiscovered() {
            mStatus = Status.SERVICES_DISCOVERED;
            PhytecLog.i(TAG, "EpaperCallback::onServicesDiscovered()");
            final byte[] init = {0};
            writeCharacteristic(BleUuid.Epaper.SERVICE,
                    BleUuid.Epaper.DATA_INIT, init,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }

        @Override
        public void onInitFinished() {
            mStatus = Status.WROTE_INIT;
            PhytecLog.i(TAG, "EpaperCallback::onInitFinished()");

            if (mBufferData.isEmpty()) {
                PhytecLog.w(TAG, "mBufferData is empty!");
                disconnect();
                return;
            }

            onBufferWriteNext();
        }

        @Override
        public void onBufferWriteNext() {
            if (mBufferData.size() <= 0) {
                onBufferWriteFinished();
                return;
            }

            mStatus = Status.WROTE_BUFFER_COLUMN;

            PhytecLog.i(TAG, "EpaperCallback::onBufferWriteNext() "
                    + String.valueOf(mBufferData.size()));

            // On the phyNODE-KW41Z writing too often without waiting for a response results in a
            // squashed looking image on the e-paper display because data is written too fast. Thus
            // we write WITH waiting for a response every so often.
            if (mBufferData.size() % 10 != 0) {
                writeCharacteristic(BleUuid.Epaper.SERVICE,
                        BleUuid.Epaper.DATA_BUFFER,
                        mBufferData.get(mBufferData.size() - 1),
                        BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            } else {
                writeCharacteristic(BleUuid.Epaper.SERVICE,
                        BleUuid.Epaper.DATA_BUFFER,
                        mBufferData.get(mBufferData.size() - 1),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }

            if (mBufferData.size() > 0)
                mBufferData.remove(mBufferData.size() - 1);
        }

        @Override
        public void onBufferWriteFinished() {
            mStatus = Status.WROTE_BUFFER;
            final byte[] update = {0};
            writeCharacteristic(BleUuid.Epaper.SERVICE,
                    BleUuid.Epaper.DATA_UPDATE, update,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }

        @Override
        public void onUpdateFinished() {
            mStatus = Status.WROTE_UPDATE;
            disconnect();
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            mBleCallbacks.deviceFound(result.getDevice().getAddress(),
                    result.getDevice().getName(),
                    result.getRssi());

            PhytecLog.i(TAG, "onScanResult():"
                    + "\n\taddress: " + result.getDevice().getAddress()
                    + "\n\tname: " + result.getDevice().getName()
                    + "\n\tRSSI: " + result.getRssi());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            PhytecLog.e(TAG, "onScanFailed(): error code " + errorCode);
            mIsScanning = false;
        }
    };

    private BluetoothGattCallback mBleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                PhytecLog.i(TAG, "onConnectionStateChange(): CONNECTED");
                mIsConnected = true;
                mBleCallbacks.connected();

                if (mBluetoothGatt != null)
                    mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                PhytecLog.i(TAG, "onConnectionStateChange(): DISCONNECTED");

                // sending obviously stopped if the device has been disconnected
                mIsConnected = false;
                mBleCallbacks.sendingStopped();
                mBleCallbacks.disconnected();

                try {
                    gatt.close();
                } catch (Exception e) {
                    PhytecLog.e(TAG, e.getMessage());
                }
            } else {
                PhytecLog.w(TAG, "onConnectionStateChange(): UNKNOWN");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                PhytecLog.e(TAG, "onServicesDiscovered(): failed with status " + String.valueOf(status));
                return;
            }

            PhytecLog.i(TAG, "onServicesDiscovered(): SUCCESS");

            mBluetoothGattServices = mBluetoothGatt.getServices();
            mBleCallbacks.servicesFound(mBluetoothGattServices);
            mBleCallbacks.sendingStatusChanged();
            mBleEpaperCallback.onServicesDiscovered();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            PhytecLog.i(TAG, "onCharacteristicRead():" +
                    "\n\t" + characteristic.getUuid().toString() +
                    "\n\t" + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            PhytecLog.i(TAG, "onCharacteristicWrite():" +
                    "\n\t" + characteristic.getUuid().toString() +
                    "\n\t" + (status == BluetoothGatt.GATT_SUCCESS ? "SUCCESS" : "FAILURE: " + status));

            if (characteristic.getUuid().equals(BleUuid.Epaper.DATA_INIT)) {
                mBleCallbacks.sendingStatusChanged();
                mBleEpaperCallback.onInitFinished();
            } else if (characteristic.getUuid().equals(BleUuid.Epaper.DATA_BUFFER)) {
                mBleCallbacks.sendingStatusChanged();
                mBleEpaperCallback.onBufferWriteNext();
            } else if (characteristic.getUuid().equals(BleUuid.Epaper.DATA_UPDATE)) {
                mBleCallbacks.sendingStatusChanged();
                mBleCallbacks.sendingStopped();
                mBleEpaperCallback.onUpdateFinished();
            }
        }
    };
}
