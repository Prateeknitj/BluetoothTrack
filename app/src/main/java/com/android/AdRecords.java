package com.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by prateek on 26-Apr-18.
 */

public class AdRecords {
    public static final int TYPE_FLAGS = 0x1;
    public static final int TYPE_UUID16_INC = 0x2;
    public static final int TYPE_UUID16 = 0x3;
    public static final int TYPE__UUID32_INC = 0x4;
    public static final int TYPE_UUID32 = 0x5;
    public static final int TYPE_UUID128_INC = 0x6;
    public static final int TYPE_UUID128 = 0x7;
    public static final int TYPE_NAME_SHORT = 0x8;
    public static final int TYPE_NAME = 0x9;
    public static final int TYPE_TRANSMITPOWER = 0xA;
    public static final int TYPE_CONNINTERVAL = 0x12;
    public static final int TYPE_SERVICEDATA = 0x16;

    public static List<AdRecords> parseScanRecord(byte[] scanRecord) {
        List<AdRecords> records = new ArrayList<>();
        int index = 0;

        while (index < scanRecord.length) {
            int length = scanRecord[index++];
            if(length == 0) break;

            int type = scanRecord[index];
            if(type == 0) break;

            byte[] data = Arrays.copyOfRange(scanRecord,index+1,index+length);

           // records.add(new AdRecords(length,type,data));

            index += length;
        }

        return records;
    }

    public static String getName(AdRecords nameRecord) {
        return new String(nameRecord.mData);
    }

    public static int getServiceDataUuid(AdRecords serviceData) {
        if (serviceData.mType != TYPE_SERVICEDATA) return -1;

        byte[] raw = serviceData.mData;
        //Find UUID data in byte array
        int uuid = (raw[1] & 0xFF) << 8;
        uuid += (raw[0] & 0xFF);

        return uuid;
    }

    public static byte[] getServiceData(AdRecords serviceData) {
        if (serviceData.mType != TYPE_SERVICEDATA) return null;

        byte[] raw = serviceData.mData;
        //Chop out the uuid
        return Arrays.copyOfRange(raw, 2, raw.length);
    }

    /* Model Object Definition */

    private int mLength;
    private int mType;
    private byte[] mData;

    public AdRecords(int length, int type, byte[] data) {
        mLength = length;
        mType = type;
        mData = data;
    }

    public int getLength() {
        return mLength;
    }

    public int getType() {
        return mType;
    }

    @Override
    public String toString() {
        switch (mType) {
            case TYPE_FLAGS:
                return "Flags";
            case TYPE_NAME_SHORT:
            case TYPE_NAME:
                return "Name";
            case TYPE_UUID16:
            case TYPE_UUID16_INC:
                return "UUIDs";
            case TYPE_TRANSMITPOWER:
                return "Transmit Power";
            case TYPE_CONNINTERVAL:
                return "Connect Interval";
            case TYPE_SERVICEDATA:
                return "Service Data";
            default:
                return "Unknown Structure: "+mType;
        }
    }

}
