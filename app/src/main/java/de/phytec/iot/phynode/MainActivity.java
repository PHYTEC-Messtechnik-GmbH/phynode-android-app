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

import android.Manifest;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BleCallbacks {

    private final String TAG = "MainActivity";
    private final static int PERMISSIONS_REQUEST_COARSE_LOCATION = 0;
    private final static int REQUEST_CODE_LOCATION_SETTINGS = 1;
    private final String mKeyDeviceListAddress = "DeviceListAddress";
    private final String mKeyDeviceListName = "DeviceListName";
    private final String mKeyDeviceListRssi = "DeviceListRssi";
    private final String mKeyDeviceListFavourite = "DeviceListFavourite";

    private DeviceList mDeviceList = null;
    private DeviceListAdapter mDeviceListAdapter = null;
    private BleManager mBleManager = null;

    private ListView mListView;
    private ProgressBar mProgressScan;
    private Button mButtonScan;
    private LinearLayout mLayoutPlaceholder;
    private TextView mTextPlaceholder;
    private ImageView mImagePlaceholder;
    private Button mButtonChangeSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhytecLog.i(TAG, "onCreate()");

        if (savedInstanceState != null) {
            try {
                ArrayList<Boolean> bal = new ArrayList<>();
                for (boolean object : savedInstanceState.getBooleanArray(mKeyDeviceListFavourite)) {
                    bal.add(object);
                }
                mDeviceList.setLists(savedInstanceState.getStringArrayList(mKeyDeviceListAddress),
                        savedInstanceState.getStringArrayList(mKeyDeviceListName),
                        savedInstanceState.getIntegerArrayList(mKeyDeviceListRssi), bal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mDeviceList = new DeviceList();
        }

        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            PhytecLog.e(TAG, "Bluetooth Low Energy is not supported on this device");
            Toast.makeText(this, "Bluetooth Low Energy is not supported on this device",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        mBleManager = new BleManager(this, this);
        String[] arrayFilters = getResources().getStringArray(R.array.device_name_filters);
        ArrayList<String> listFilters = new ArrayList<>();
        Collections.addAll(listFilters, arrayFilters);
        mBleManager.setScanFilters(listFilters);
        mDeviceListAdapter = new DeviceListAdapter(this, R.layout.list_item_device, mDeviceList);

        // prompt for permission (location service)
        final int permissionLocation = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            PhytecLog.i(TAG, "Coarse location permission not granted");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_COARSE_LOCATION);
        }

        mListView = (ListView) findViewById(R.id.list_view_main);
        mProgressScan = (ProgressBar) findViewById(R.id.progress_scan);
        mLayoutPlaceholder = (LinearLayout) findViewById(R.id.layout_placeholder);
        mImagePlaceholder = (ImageView) findViewById(R.id.icon_placeholder);
        mTextPlaceholder = (TextView) findViewById(R.id.text_placeholder);
        mButtonChangeSettings = (Button) findViewById(R.id.button_change_settings);
        mButtonScan = (Button) findViewById(R.id.button_scan);

        mListView.setVisibility(View.GONE);
        mListView.setAdapter(mDeviceListAdapter);

        mProgressScan.setVisibility(View.INVISIBLE);

        mButtonChangeSettings.setVisibility(View.GONE);
        mButtonChangeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        REQUEST_CODE_LOCATION_SETTINGS);
            }
        });

        mButtonScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mBleManager.isScanning()) {
                    mBleManager.stopScan();
                } else {
                    mBleManager.startScan();
                }
            }
        });

        // load preferences
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        Set<String> addressSet = sharedPreferences.getStringSet(getString(R.string.saved_device_addresses), null);
        Set<String> nameSet = sharedPreferences.getStringSet(getString(R.string.saved_device_names), null);
        Set<String> rssiSet = sharedPreferences.getStringSet(getString(R.string.saved_device_rssis), null);
        Set<String> favouriteSet = sharedPreferences.getStringSet(getString(R.string.saved_device_favourites), null);

        if (addressSet == null ||
                nameSet == null ||
                rssiSet == null ||
                favouriteSet == null) {
            PhytecLog.w(TAG, "Cannot load Shared Preferences: retrieved sets are null!");
            return;
        }

        Set<Integer> rssiSetAsInteger = new HashSet<>();
        for (String str : rssiSet)
            rssiSetAsInteger.add(Integer.valueOf(str));
        PhytecLog.i(TAG, rssiSetAsInteger.toString());

        Set<Boolean> favouriteSetAsBoolean = new HashSet<>();
        for (String str : favouriteSet)
            favouriteSetAsBoolean.add(Boolean.valueOf(str));
        PhytecLog.i(TAG, favouriteSetAsBoolean.toString());

        PhytecLog.i(TAG, addressSet.toString() + " " + nameSet.toString() + " " + rssiSet.toString());

        mDeviceList.setLists(new ArrayList<>(addressSet), new ArrayList<>(nameSet),
                new ArrayList<>(rssiSetAsInteger), new ArrayList<>(favouriteSetAsBoolean));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_about:
                final Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PhytecLog.i(TAG, "onResume()");

        onLocationSettingsChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PhytecLog.i(TAG, "onPause()");
        PhytecLog.i(TAG, mDeviceList.toString());

        mBleManager.stopScan();

        if (mDeviceList == null) {
            PhytecLog.i(TAG, "mDeviceList is null");
            return;
        }

        DeviceList tempDeviceList = new DeviceList(mDeviceList);
        tempDeviceList.removeAllNonFavourites();
        PhytecLog.i(TAG, "removed all non-favourites: " + tempDeviceList.toString() + " (" +
                mDeviceList.toString() + ")");

        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!tempDeviceList.isNull()) {
            editor.putStringSet(getString(R.string.saved_device_addresses), new HashSet<>(tempDeviceList.getAddressList()));
            editor.putStringSet(getString(R.string.saved_device_names), new HashSet<>(tempDeviceList.getNameList()));
            editor.putStringSet(getString(R.string.saved_device_rssis), new HashSet<>(tempDeviceList.getRssiListAsStrings()));
            editor.putStringSet(getString(R.string.saved_device_favourites), new HashSet<>(tempDeviceList.getFavouriteListAsStrings()));
        }
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_COARSE_LOCATION: {
                if (!(grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Privileges for coarse location have to be granted!",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            onLocationSettingsChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(mKeyDeviceListAddress, mDeviceList.getAddressList());
        outState.putStringArrayList(mKeyDeviceListName, mDeviceList.getNameList());
        outState.putIntegerArrayList(mKeyDeviceListRssi, mDeviceList.getRssiList());
        boolean[] ba = new boolean[mDeviceList.getFavouriteList().size()];
        int i = 0;
        for (Boolean object : mDeviceList.getFavouriteList()) {
            ba[i] = object;
            i++;
        }
        outState.putBooleanArray(mKeyDeviceListFavourite, ba);

        super.onSaveInstanceState(outState);
    }

    public void onDeviceSelected(int position) {
        mBleManager.stopScan();

        PhytecLog.i(TAG, "onDeviceSelected(): " + mDeviceList.getAddressList().get(position));

        final Intent intent = new Intent(this, PeripheralActivity.class);
        intent.putExtra(PeripheralActivity.EXTRA_DEVICE_ADDRESS,
                mDeviceList.getAddressList().get(position));
        intent.putExtra(PeripheralActivity.EXTRA_DEVICE_NAME,
                mDeviceList.getNameList().get(position));
        startActivity(intent);
    }

    // private

    private void onLocationSettingsChanged() {
        mListView.setVisibility(View.GONE);
        mLayoutPlaceholder.setVisibility(View.VISIBLE);

        if (!mBleManager.isLocationEnabled()) {
            mButtonScan.setEnabled(false);
            mImagePlaceholder.setImageResource(R.drawable.ic_location_off);
            mTextPlaceholder.setText(R.string.location_service_disabled);
            mButtonChangeSettings.setVisibility(View.VISIBLE);
        } else {
            mButtonScan.setEnabled(true);
            mImagePlaceholder.setImageResource(R.drawable.ic_developer_board);
            mTextPlaceholder.setText(R.string.no_devices_found);
            mButtonChangeSettings.setVisibility(View.GONE);

            if (mDeviceList.size() > 0) {
                mListView.setVisibility(View.VISIBLE);
                mLayoutPlaceholder.setVisibility(View.GONE);
            }
        }
    }

    // BleCallbacks

    @Override
    public void deviceFound(String address, String name, int rssi) {
        mLayoutPlaceholder.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mDeviceList.add(address, name, rssi);
        mDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void scanStarted() {
        mButtonScan.setText(R.string.scan_stop);
        mDeviceList.removeAllNonFavourites();

        // if mDeviceList has at least one favourite then display it immediately, otherwise show the
        // placeholder and hide mListView
        if (!mDeviceList.getFavouriteList().contains(true)) {
            mLayoutPlaceholder.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }

        mProgressScan.setVisibility(View.VISIBLE);
    }

    @Override
    public void scanStopped() {
        mButtonScan.setText(R.string.scan_start);
        mProgressScan.setVisibility(View.INVISIBLE);
    }

    @Override
    public void servicesFound(final List<BluetoothGattService> services) {}

    @Override
    public void sendingStarted() {}

    @Override
    public void sendingStatusChanged() {}

    @Override
    public void sendingStopped() {}

    @Override
    public void connected() {}

    @Override
    public void disconnected() {}
}
