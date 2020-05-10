package com.excample.exampleconection;

import android.os.Build;
import android.util.Log;

public class ScanHwAndSwInformation {


    public String getHardwareAndSoftwareInfo() {

        String  details =  "VERSION.RELEASE : "+Build.VERSION.RELEASE
                +"\nVERSION.INCREMENTAL : "+Build.VERSION.INCREMENTAL
                +"\nVERSION.SDK.NUMBER : "+Build.VERSION.SDK_INT
                +"\nBOARD : "+Build.BOARD
                +"\nBOOTLOADER : "+Build.BOOTLOADER
                +"\nBRAND : "+Build.BRAND
                +"\nCPU_ABI : "+Build.CPU_ABI
                +"\nCPU_ABI2 : "+Build.CPU_ABI2
                +"\nDISPLAY : "+Build.DISPLAY
                +"\nFINGERPRINT : "+Build.FINGERPRINT
                +"\nHARDWARE : "+Build.HARDWARE
                +"\nHOST : "+Build.HOST
                +"\nID : "+Build.ID
                +"\nMANUFACTURER : "+Build.MANUFACTURER
                +"\nMODEL : "+Build.MODEL
                +"\nPRODUCT : "+Build.PRODUCT
                +"\nSERIAL : "+Build.SERIAL
                +"\nTAGS : "+Build.TAGS
                +"\nTIME : "+Build.TIME
                +"\nTYPE : "+Build.TYPE
                +"\nUNKNOWN : "+Build.UNKNOWN
                +"\nUSER : "+Build.USER;
        Log.d("GetDevInfo", "es "+details);
        return details;
    }

    public String getDevManufacturer(){
        String manufacturer = "";
        manufacturer = Build.MANUFACTURER.toString();
        return  manufacturer;
    }
    public String getDevModel(){
        String model = "";
        model = Build.MODEL.toString();
        return model;
    }


}
