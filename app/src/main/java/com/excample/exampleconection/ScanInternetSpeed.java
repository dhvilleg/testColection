package com.excample.exampleconection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;

public class ScanInternetSpeed {

    ConnectivityManager connMgr;
    Context context;
    NetworkInfo networkInfo;
    WifiManager wifiManager;
    public ScanInternetSpeed(Context context) {
        this.context = context;
        this.connMgr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        this.networkInfo = connMgr.getActiveNetworkInfo();
        this.wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        //NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public Pair<Integer, Integer> checkMovileInternetSpeed(){
        int downSpeed = 0;
        int upSpeed = 0;

        NetworkCapabilities nc = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
        downSpeed = nc.getLinkDownstreamBandwidthKbps();
        upSpeed = nc.getLinkUpstreamBandwidthKbps();

        return new Pair <Integer, Integer> (downSpeed, upSpeed);

    }
    public int checkWifiInternetSpeed(){

        int linkSpeed = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(linkSpeed, 5);
        return level;

    }

    public String getNetworkConectivityType(){
        String networkConectivityType ="";
        if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("RED","wifi");
            networkConectivityType = "WIFI";
        } else if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            Log.d("RED","Mobile");
            networkConectivityType = "MOBILE";
        }
        return networkConectivityType;
    }
}
