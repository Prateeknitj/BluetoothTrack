package com.android;

import android.os.ParcelUuid;

import java.util.List;

/**
 * Created by prateek on 26-Apr-18.
 */

public class TemperatureBeacon {
    public static final ParcelUuid THERM_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");
    /* Short-form UUID that defines the Health Thermometer service */
    private static final int UUID_SERVICE_THERMOMETER = 0x1809;

    private String mName;
    private float mCurrentTemp;
    //Device metadata
    private int mSignal;
    private String mAddress;

    /* Builder for pre-Lollipop */
    public TemperatureBeacon(List<AdRecords> records, String deviceAddress, int rssi) {
        mSignal = rssi;
        mAddress = deviceAddress;

        for(AdRecords packet : records) {
            //Find the device name record
            if (packet.getType() == AdRecords.TYPE_NAME) {
                mName = AdRecords.getName(packet);
            }
            //Find the service data record that contains our service's UUID
            if (packet.getType() == AdRecords.TYPE_SERVICEDATA
                    && AdRecords.getServiceDataUuid(packet) == UUID_SERVICE_THERMOMETER) {
                byte[] data = AdRecords.getServiceData(packet);
                mCurrentTemp = (data[0] & 0xFF);
                if((data[1] & 0x80) != 0) {
                    mCurrentTemp += 0.5f;
                }
            }
        }
    }

    private float parseTemp(byte[] serviceData) {
        /*
         * Temperature data is two bytes, and precision is 0.5degC.
         * LSB contains temperature whole number
         * MSB contains a bit flag noting if fractional part exists
         */
        float temp = (serviceData[0] & 0xFF);
        if ((serviceData[1] & 0x80) != 0) {
            temp += 0.5f;
        }

        return temp;
    }

    public String getName() {
        return mName;
    }

    public int getSignal() {
        return mSignal;
    }

    public float getCurrentTemp() {
        return mCurrentTemp;
    }

    public String getAddress() {
        return mAddress;
    }

    @Override
    public String toString() {
        return String.format("%s (%ddBm): %.1fC", mName, mSignal, mCurrentTemp);
    }
}
