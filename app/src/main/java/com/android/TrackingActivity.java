package com.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.londatiga.android.bluetooth.R;

import static android.content.ContentValues.TAG;


/**
 * A login screen that offers login via email/password.
 */
public class TrackingActivity extends Activity implements BluetoothAdapter.LeScanCallback{

    public static final String tag = "TrackingActivity";
    private static final UUID THERM_SERVICE = UUID.fromString("00001809-0000-1000-8000-0080f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String,TemperatureBeacon> mBeacons;
    private BeaconAdapter mAdapter;
    String add;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        // Set up the login form.
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        add = getIntent().getStringExtra("Device");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enablebtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enablebtIntent);
            finish();
            return;
        }

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        StartScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    private Runnable mStopRunnable = () -> {StopScan();};
    private Runnable mStartRunnable = () -> {StartScan();};

    public void StartScan() {

        mBluetoothAdapter.startLeScan(new UUID[] {THERM_SERVICE}, this);
        mHandler.postDelayed(mStopRunnable,5000);
    }

    public void StopScan() {
        mBluetoothAdapter.stopLeScan(this);
        mHandler.postDelayed(mStartRunnable,2500);
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        Log.i(TAG,"New LE device : " + bluetoothDevice.getName() + "@" + i);

        List<AdRecords> records = AdRecords.parseScanRecord(bytes);

        TemperatureBeacon beacon = new TemperatureBeacon(records,bluetoothDevice.getAddress(),i);
        mHandler.sendMessage(Message.obtain(null,0,beacon));
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            TemperatureBeacon beacon = (TemperatureBeacon) msg.obj;
            mBeacons.put(beacon.getAddress(),beacon);
            mAdapter.setNotifyOnChange(false);
            mAdapter.addAll(mBeacons.values());
            mAdapter.notifyDataSetChanged();
        }
    };

    public static class BeaconAdapter extends ArrayAdapter<TemperatureBeacon> {

        public BeaconAdapter(Context context) {
            super(context,0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_tracking,parent,false);
            }

            TemperatureBeacon beacon = getItem(position);

            final int textColor = getTemperatureColor(beacon.getCurrentTemp());

            TextView nameView = (TextView) convertView.findViewById(R.id.text_name);
            nameView.setText(beacon.getName());
            nameView.setTextColor(textColor);

            TextView tempView = (TextView) convertView.findViewById(R.id.text_temperature);
            nameView.setText(String.format("%.1f\u00B0C", beacon.getCurrentTemp()));
            nameView.setTextColor(textColor);

            TextView addressView = (TextView) convertView.findViewById(R.id.text_address);
            nameView.setText(beacon.getAddress());
            nameView.setTextColor(textColor);

            TextView rssiView = (TextView) convertView.findViewById(R.id.text_rssi);
            nameView.setText(String.format("%ddBm", beacon.getSignal()));
            nameView.setTextColor(textColor);

            return convertView;
        }

        public int getTemperatureColor(float temperature) {
            float clipped = Math.max(0f,Math.min(40f,temperature));

            float scaled = ((40f - clipped)/ 40f) * 255f;
            int blue = Math.round(scaled);
            int red = 255 - blue;

            return Color.rgb(red,0,blue);
        }
    }
}

