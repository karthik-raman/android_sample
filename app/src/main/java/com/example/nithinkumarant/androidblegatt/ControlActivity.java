package com.example.nithinkumarant.androidblegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    private final static String TAG = ControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;
    private SeekBar FanCon;
    private TextView textFan;

    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeService mBluetoothLeService;

    TextView textViewState;
    private ExpandableListView mGattServicesList;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private int i=0;
    private int j=0;
    private int FanSpeed;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("GATT_DISCONNECTED");
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
               // displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }
        }
    };
    private Button Notifybutton;

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    private void updateConnectionState(final String st) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewState.setText(st);
            }
        });
    }

    public void displayData(String data) {

        if (data != null) {
            textViewState.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "Unknown Service";
        String unknownCharaString = "Unknown Characteristic";
        String IPVS_Weat="IPVS_Weather Service";
        String IPVS_Fan="IPVS_Fan Service";
        String temp="Temperature Characterstics";
        String hum="Humidity Characterstics";
        String Ton="Fan_Turn_On/Off Characterstics";
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            if(uuid.compareTo("00000002-0000-0000-fdfd-fdfdfdfdfdfd") == 0)
            {
                currentServiceData.put(
                        LIST_NAME, lookup(uuid, IPVS_Weat));
            }
            else if(uuid.compareTo("00000001-0000-0000-fdfd-fdfdfdfdfdfd") == 0)
            {
                FanCon.setVisibility(View.VISIBLE);
                textFan.setVisibility(View.VISIBLE);

                currentServiceData.put(
                        LIST_NAME, lookup(uuid, IPVS_Fan));
            }
            else
            {
                currentServiceData.put(
                        LIST_NAME, lookup(uuid, unknownServiceString));
            }
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                if(uuid.compareTo("00002a1c-0000-1000-8000-00805f9b34fb") == 0) {
                    currentCharaData.put(
                            LIST_NAME, lookup(uuid, temp));
                }
                else if(uuid.compareTo("00002a6f-0000-1000-8000-00805f9b34fb") == 0) {
                    currentCharaData.put(
                            LIST_NAME, lookup(uuid, hum));
                }
                else if(uuid.compareTo("10000001-0000-0000-fdfd-fdfdfdfdfdfd") == 0) {
                    currentCharaData.put(
                            LIST_NAME, lookup(uuid, Ton));
                }
                else {
                    currentCharaData.put(
                            LIST_NAME, lookup(uuid, unknownCharaString));
                }
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }


    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        BluetoothGattCharacteristic characteristic = null;
                        characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null)
                            {
                                // Temperature reading
                                if (characteristic.getUuid().compareTo(UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb")) == 0) {

                                    float f = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
                                    String F_str;
                                    //Toast.makeText(ControlActivity.this, "Temperature Value is :"+f, Toast.LENGTH_SHORT).show();
                                    F_str= Float.toString(f);
                                    displayData("Temperature Value: "+F_str+"°C");
                                   // mBluetoothGatt.setCharacteristicNotification(characteristic,true);

                                    List<BluetoothGattDescriptor> desc = characteristic.getDescriptors();
                                    BluetoothGattDescriptor descriptor = desc.get(0);
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                                }
                                //Humidity reading
                                if(characteristic.getUuid().compareTo(UUID.fromString("00002a6f-0000-1000-8000-00805f9b34fb")) == 0) {
                                    int val = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                                    val=val/100;
                                    String val_str=Integer.toString(val);
                                    displayData("Humidity value: "+val_str+"%");

                                }
                                // Turn on and off the Fan
                                if(characteristic.getUuid().compareTo(UUID.fromString("10000001-0000-0000-fdfd-fdfdfdfdfdfd")) == 0) {

                                    if(i==0) {
                                        characteristic.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                                        displayData("Fan is not running");
                                        mBluetoothLeService.writeCharacteristic(characteristic);
                                        i++;
                                    }
                                    else
                                    {
                                        characteristic.setValue(FanSpeed, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                                        displayData("Fan is running");
                                        mBluetoothLeService.writeCharacteristic(characteristic);
                                        i=0;
                                    }
                                }

                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);

                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        TextView textViewDeviceName = (TextView)findViewById(R.id.textDeviceName);
        TextView textViewDeviceAddr = (TextView)findViewById(R.id.textDeviceAddress);
        textViewState = (TextView)findViewById(R.id.textState);
        textFan = (TextView)findViewById(R.id.textViewFan);
        FanCon =(SeekBar)findViewById(R.id.seekBarFan);
        FanCon.setMax(65535);

        FanCon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                FanSpeed = progress;
                FanCon.setProgress(FanSpeed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        textViewDeviceName.setText(mDeviceName);
        //textViewDeviceAddr.setText(mDeviceAddress);


        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private static HashMap<String, String> attributes = new HashMap();

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);

        return name == null ? defaultName : name;
    }
}
