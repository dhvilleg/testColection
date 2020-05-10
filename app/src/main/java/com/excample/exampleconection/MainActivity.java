package com.excample.exampleconection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.excample.exampleconection.test.HttpDownloadTest;
import com.excample.exampleconection.test.HttpUploadTest;
import com.excample.exampleconection.test.PingTest;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class MainActivity extends AppCompatActivity {

    //Definicion de variables globales
    private final int REQUEST_PERMISSION_PHONE_STATE=1;
    public String phonestate;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Context mainContext;
    private LocationAddress locationAddress;
    private Button buttonColectInfo;
    private Button buttonUploadInfo;
    private ScanDbHelper scanDbHelper;
    private ScanMetadata scanMetadata;
    private Cursor cursorQuery;
    private String pingTimeMilis = "null" ;
    private String uploadSpeed = "null" ;
    private String downloadSpeed = "null" ;
    static int position = 0;
    static int lastPosition = 0;
    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    HashSet<String> tempBlackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String permissionACLocation =  Manifest.permission.ACCESS_COARSE_LOCATION;
        String permissionAFLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String permissionInternet = Manifest.permission.INTERNET;
        String permissionAccessWifi = Manifest.permission.ACCESS_WIFI_STATE;
        Log.d("Pre-permiso","la variable permiso tiene: "+permissionACLocation);
        Log.d("Pre-permiso","la variable permiso tiene: "+permissionAFLocation);
        Log.d("Pre-permiso","la variable permiso tiene: "+permissionInternet);
        Log.d("Pre-permiso","la variable permiso tiene: "+permissionAccessWifi);
        this.showPhoneStatePermission(permissionACLocation);
        this.showPhoneStatePermission(permissionAFLocation);
        this.showPhoneStatePermission(permissionInternet);
        this.showPhoneStatePermission(permissionAccessWifi);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        //definicion de objetos de otras clases
        mainContext = getApplicationContext();
        locationAddress = new LocationAddress();
        //Definicion de objetos de otras clases
        final ScanCellularActivity scanCellularActivity = new ScanCellularActivity(mainContext);
        final SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        buttonUploadInfo = this.findViewById(R.id.buttonUploadInfo);
        buttonColectInfo = (Button) this.findViewById(R.id.buttonColectInfo);
        buttonUploadInfo.setEnabled(false);
        buttonUploadInfo.setText("Refresque para colectar");
        buttonColectInfo.setEnabled(false);
        buttonColectInfo.setText("Refresque para Subir");
        final TextView pingTextView = (TextView) this.findViewById(R.id.pingTextView);
        final TextView downloadTextView = (TextView) this.findViewById(R.id.downloadTextView);
        final TextView uploadTextView = (TextView) this.findViewById(R.id.uploadTextView);
        final LinearLayout chartDownload = (LinearLayout) this.findViewById(R.id.chartDownload);
        final LinearLayout chartPing = (LinearLayout) this.findViewById(R.id.chartPing);
        final LinearLayout chartUpload = (LinearLayout) this.findViewById(R.id.chartUpload);



        final Button[] btn = {this.findViewById(R.id.button)};
        TelephonyManager telMng = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telMng.listen(new PhoneStateListener() {
            @Override
            public void onServiceStateChanged (ServiceState serviceState) {

                super.onServiceStateChanged(serviceState);
                switch(serviceState.getState()) {
                    case ServiceState.STATE_EMERGENCY_ONLY:
                        phonestate ="STATE_EMERGENCY_ONLY";
                        break;
                    case ServiceState.STATE_IN_SERVICE:
                        phonestate ="STATE_IN_SERVICE";
                        break;
                    case ServiceState.STATE_OUT_OF_SERVICE:
                        phonestate ="STATE_OUT_OF_SERVICE";
                        break;
                    case ServiceState.STATE_POWER_OFF:
                        phonestate ="STATE_POWER_OFF";
                        break;
                    default:
                        phonestate = "Unknown";
                        break;
                }
                Log.d("estado de conexion","El estado es :"+ phonestate);
            }
        }, PhoneStateListener.LISTEN_SERVICE_STATE);

        btn[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Captura de datos Celular

                ScanHwAndSwInformation scanHwAndSwInformation = new ScanHwAndSwInformation();
                ArrayList<String> phoneInformation = scanCellularActivity.getOperatorInfo();
                ScanInternetSpeed scanInternetSpeed = new ScanInternetSpeed(mainContext);
                final String timestamp = s.format(new Date());
                final String countryISO = phoneInformation.get(0);
                final String phoneOperatorId =  phoneInformation.get(2);
                final String devManufacturer = scanHwAndSwInformation.getDevManufacturer();
                final String devModel = scanHwAndSwInformation.getDevModel();
                final String phoneNetStandard = scanCellularActivity.getPhoneSignalType();
                final String phoneNetTechnology = scanCellularActivity.getPhoneNetworType();
                Log.d("BotonRefrescar","boton presionado valor "+phonestate);
                final String isConected = scanCellularActivity.getDevIsConected();

                final double latitude;
                final double longitude;
                if (phonestate == "STATE_IN_SERVICE" && isConected == "NoConectado"){
                    buttonColectInfo.setText("Colectar solo red movil");
                    buttonColectInfo.setEnabled(true);
                    buttonUploadInfo.setText("No se puede subir en red Mobil");
                    buttonUploadInfo.setEnabled(false);

                    Pair<Double,Double> latLonLocation = locationAddress.getLatLongFromLocation(mainContext);
                    latitude = latLonLocation.first;
                    longitude = latLonLocation.second;
                    final String simOperatorId = phoneInformation.get(3);
                    final String operatorMcc = scanCellularActivity.getDevMccId();
                    final String operatorMnc = scanCellularActivity.getDevMncId();
                    final String internetConNetwork = "null";

                    buttonColectInfo.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if(phoneNetTechnology == "HSPA+" || phoneNetTechnology == "HSPA" || phoneNetTechnology == "UMTS"){
                                ArrayList<Integer> strengthInfo = scanCellularActivity.getDevStrengthSignal();
                                ArrayList<Integer> cellIdentity = scanCellularActivity.getDevCellIdentity();
                                int phoneSignalStrength =  strengthInfo.get(0);
                                int phoneAsuStrength =  strengthInfo.get(1);
                                int phoneSignalLevel =  strengthInfo.get(2);
                                int cellWcdmaLac = cellIdentity.get(0);
                                int cellWcdmaUcid = cellIdentity.get(1);
                                int cellWcdmaPsc = cellIdentity.get(2);
                                int cellWcdmaCid = cellIdentity.get(3);
                                int cellWcdmaRnc = cellIdentity.get(4);
                                int cellWcdmaUarfcn = cellIdentity.get(5);
                                String signalQuality = scanCellularActivity.getSignalQuality(strengthInfo.get(0));
                                int fieldIsRegistered = 0;
                                String phoneRsrpStrength="null";
                                String phoneRssnrStrength="null";
                                String phoneTimingAdvance="null";
                                String phoneCqiStrength="null";
                                String phoneRsrqStrength="null";
                                String cellLtePci="null";
                                String cellLteCid="null";
                                String cellLteTac="null";
                                String cellLteeNode="null";
                                String cellLteEarfcn="null";
                                String cellBslat="null";
                                String cellBslon="null";
                                String cellSid="null";
                                String cellNid="null";
                                String cellBid="null";
                                String cellGsmArcfn="null";
                                String cellGsmLac="null";
                                String cellGsmCid="null";

                                ScanMetadata scanMetadata = new ScanMetadata(""+timestamp, ""+countryISO, ""+phoneOperatorId, ""+simOperatorId, ""+operatorMcc, ""+operatorMnc, ""+devManufacturer, ""+devModel, ""+isConected, ""+phoneNetStandard, ""+phoneNetTechnology, ""+internetConNetwork, ""+latitude, ""+longitude, ""+pingTimeMilis, ""+downloadSpeed, ""+uploadSpeed, ""+phoneSignalStrength, ""+phoneAsuStrength, ""+phoneSignalLevel, ""+signalQuality, 0, ""+phoneRsrpStrength, ""+phoneRssnrStrength, ""+phoneTimingAdvance, ""+phoneCqiStrength, ""+phoneRsrqStrength, ""+cellLtePci, ""+cellLteCid, ""+cellLteTac, ""+cellLteeNode, ""+cellLteEarfcn, ""+cellBslat, ""+cellBslon, ""+cellSid, "null", ""+cellBid, ""+cellWcdmaLac, ""+cellWcdmaUcid, ""+cellWcdmaUarfcn, ""+cellWcdmaPsc, ""+cellWcdmaCid, ""+cellWcdmaRnc, ""+cellGsmArcfn, ""+cellGsmLac, ""+cellGsmCid);
                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                scanDbHelper.saveSqlScan(scanMetadata);
                            }
                            //Seccion Lte
                            if(phoneNetTechnology == "LTE"){
                                ArrayList<Integer> strengthInfo = scanCellularActivity.getDevStrengthSignal();
                                ArrayList<Integer> cellIdentity = scanCellularActivity.getDevCellIdentity();
                                int phoneSignalStrength = strengthInfo.get(0);
                                int phoneAsuStrength = strengthInfo.get(1);
                                int phoneSignalLevel = strengthInfo.get(2);
                                int phoneRsrpStrength = strengthInfo.get(3);
                                int phoneRsrqStrength = strengthInfo.get(4);
                                double phoneRssnrStrength = 5.4;
                                int phoneTimingAdvance = strengthInfo.get(6);
                                int phoneCqiStrength = strengthInfo.get(7);
                                int cellLtePci = cellIdentity.get(0);
                                int cellLteTac = cellIdentity.get(1);
                                int cellLteeNodeB = cellIdentity.get(2);
                                int cellLteCid = cellIdentity.get(3);
                                int cellLteEarfcn = cellIdentity.get(4);
                                String signalQuality = scanCellularActivity.getSignalQuality(strengthInfo.get(0));
                                int fieldIsRegistered = 0;
                                String cellBslat = "null";
                                String cellBslon = "null";
                                String cellSid = "null";
                                String cellNid = "null";
                                String cellBid = "null";
                                String cellWcdmaLac = "null";
                                String cellWcdmaUcid = "null";
                                String cellWcdmaUarfcn = "null";
                                String cellWcdmaPsc = "null";
                                String cellWcdmaCid = "null";
                                String cellWcdmaRnc = "null";
                                String cellGsmArcfn = "null";
                                String cellGsmLac = "null";
                                String cellGsmCid = "null";

                                ScanMetadata scanMetadata = new ScanMetadata(""+timestamp, ""+countryISO, ""+phoneOperatorId, ""+simOperatorId, ""+operatorMcc, ""+operatorMnc, ""+devManufacturer, ""+devModel, ""+isConected, ""+phoneNetStandard, ""+phoneNetTechnology, ""+internetConNetwork, ""+latitude, ""+longitude, ""+pingTimeMilis, ""+downloadSpeed, ""+uploadSpeed, ""+phoneSignalStrength, ""+phoneAsuStrength, ""+phoneSignalLevel, ""+signalQuality, 0, ""+phoneRsrpStrength, ""+phoneRssnrStrength, ""+phoneTimingAdvance, ""+phoneCqiStrength, ""+phoneRsrqStrength, ""+cellLtePci, ""+cellLteCid, ""+cellLteTac, ""+cellLteeNodeB, ""+cellLteEarfcn, ""+cellBslat, ""+cellBslon, ""+cellSid, ""+cellNid, ""+cellBid, ""+cellWcdmaLac, ""+cellWcdmaUcid, ""+cellWcdmaUarfcn, ""+cellWcdmaPsc, ""+cellWcdmaCid, ""+cellWcdmaRnc, ""+cellGsmArcfn, ""+cellGsmLac, ""+cellGsmCid);
                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                scanDbHelper.saveSqlScan(scanMetadata);
                            }
                            //Seccion GSM
                            if(phoneNetTechnology == "GSM" || phoneNetTechnology == "GPRS" || phoneNetTechnology == "EDGE"){
                                ArrayList<Integer> strengthInfo = scanCellularActivity.getDevStrengthSignal();
                                ArrayList<Integer> cellIdentity = scanCellularActivity.getDevCellIdentity();
                                int phoneSignalStrength =  strengthInfo.get(0);
                                int phoneAsuStrength =  strengthInfo.get(1);
                                int phoneSignalLevel =  strengthInfo.get(2);
                                int cellGsmLac  = cellIdentity.get(0);
                                int cellGsmCid  = cellIdentity.get(1);
                                int cellGsmArcfn = cellIdentity.get(2);
                                String signalQuality = scanCellularActivity.getSignalQuality(strengthInfo.get(0));
                                int fieldIsRegistered = 0;
                                String phoneRsrpStrength = "null";
                                String phoneRssnrStrength = "null";
                                String phoneTimingAdvance = "null";
                                String phoneCqiStrength = "null";
                                String phoneRsrqStrength = "null";
                                String cellLtePci = "null";
                                String cellLteCid = "null";
                                String cellLteTac = "null";
                                String cellLteeNodeB = "null";
                                String cellLteEarfcn = "null";
                                String cellBslat = "null";
                                String cellBslon = "null";
                                String cellSid = "null";
                                String cellNid = "null";
                                String cellBid = "null";
                                String cellWcdmaLac = "null";
                                String cellWcdmaUcid = "null";
                                String cellWcdmaUarfcn = "null";
                                String cellWcdmaPsc = "null";
                                String cellWcdmaCid = "null";
                                String cellWcdmaRnc = "null";

                                ScanMetadata scanMetadata = new ScanMetadata(""+timestamp, ""+countryISO, ""+phoneOperatorId, ""+simOperatorId, ""+operatorMcc, ""+operatorMnc, ""+devManufacturer, ""+devModel, ""+isConected, ""+phoneNetStandard, ""+phoneNetTechnology, ""+internetConNetwork, ""+latitude, ""+longitude, ""+pingTimeMilis, ""+downloadSpeed, ""+uploadSpeed, ""+phoneSignalStrength, ""+phoneAsuStrength, ""+phoneSignalLevel, ""+signalQuality, 0, ""+phoneRsrpStrength, ""+phoneRssnrStrength, ""+phoneTimingAdvance, ""+phoneCqiStrength, ""+phoneRsrqStrength, ""+cellLtePci, ""+cellLteCid, ""+cellLteTac, ""+cellLteeNodeB, ""+cellLteEarfcn, ""+cellBslat, ""+cellBslon, ""+cellSid, ""+cellNid, ""+cellBid, ""+cellWcdmaLac, ""+cellWcdmaUcid, ""+cellWcdmaUarfcn, ""+cellWcdmaPsc, ""+cellWcdmaCid, ""+cellWcdmaRnc, ""+cellGsmArcfn, ""+cellGsmLac, ""+cellGsmCid);
                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                scanDbHelper.saveSqlScan(scanMetadata);

                            }
                            //Seccion CDMA
                            if(phoneNetTechnology == "CDMA" || phoneNetTechnology == "EVDO_0" || phoneNetTechnology == "EVDO_A" || phoneNetTechnology == "EVDO_B"){
                                ArrayList<Integer> strengthInfo = scanCellularActivity.getDevStrengthSignal();
                                ArrayList<Integer> cellIdentity = scanCellularActivity.getDevCellIdentity();
                                int phoneSignalStrength =  strengthInfo.get(0);
                                int phoneAsuStrength =  strengthInfo.get(0);
                                int phoneSignalLevel =  strengthInfo.get(0);
                                int cellBslat = cellIdentity.get(0);
                                int cellBslon = cellIdentity.get(1);
                                int cellSid = cellIdentity.get(2);
                                int cellNid = cellIdentity.get(3);
                                int cellBid = cellIdentity.get(4);
                                String signalQuality = scanCellularActivity.getSignalQuality(strengthInfo.get(0));
                                int fieldIsRegistered = 0;
                                String phoneRsrpStrength = "null";
                                String phoneRssnrStrength = "null";
                                String phoneTimingAdvance = "null";
                                String phoneCqiStrength = "null";
                                String phoneRsrqStrength = "null";
                                String cellLtePci = "null";
                                String cellLteCid = "null";
                                String cellLteTac = "null";
                                String cellLteeNodeB = "null";
                                String cellLteEarfcn = "null";
                                String cellWcdmaLac = "null";
                                String cellWcdmaUcid = "null";
                                String cellWcdmaUarfcn = "null";
                                String cellWcdmaPsc = "null";
                                String cellWcdmaCid = "null";
                                String cellWcdmaRnc = "null";
                                String cellGsmArcfn = "null";
                                String cellGsmLac = "null";
                                String cellGsmCid = "null";
                                ScanMetadata scanMetadata = new ScanMetadata(""+timestamp, ""+countryISO, ""+phoneOperatorId, ""+simOperatorId, ""+operatorMcc, ""+operatorMnc, ""+devManufacturer, ""+devModel, ""+isConected, ""+phoneNetStandard, ""+phoneNetTechnology, ""+internetConNetwork, ""+latitude, ""+longitude, ""+pingTimeMilis, ""+downloadSpeed, ""+uploadSpeed, ""+phoneSignalStrength, ""+phoneAsuStrength, ""+phoneSignalLevel, ""+signalQuality, 0, ""+phoneRsrpStrength, ""+phoneRssnrStrength, ""+phoneTimingAdvance, ""+phoneCqiStrength, ""+phoneRsrqStrength, ""+cellLtePci, ""+cellLteCid, ""+cellLteTac, ""+cellLteeNodeB, ""+cellLteEarfcn, ""+cellBslat, ""+cellBslon, ""+cellSid, ""+cellNid, ""+cellBid, ""+cellWcdmaLac, ""+cellWcdmaUcid, ""+cellWcdmaUarfcn, ""+cellWcdmaPsc, ""+cellWcdmaCid, ""+cellWcdmaRnc, ""+cellGsmArcfn, ""+cellGsmLac, ""+cellGsmCid);
                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                scanDbHelper.saveSqlScan(scanMetadata);

                            }
                        }
                    });
                }
                else if (phonestate == "STATE_OUT_OF_SERVICE" && isConected == "NoConectado"){
                    buttonUploadInfo.setText("No se puede subir info");
                    buttonUploadInfo.setEnabled(false);
                    buttonColectInfo.setText("Sin Servicio");
                    buttonColectInfo.setEnabled(false);
                }
                else if (phonestate == "STATE_OUT_OF_SERVICE" && isConected == "Conectado"){
                    buttonUploadInfo.setText("Sin Servicio");
                    buttonUploadInfo.setEnabled(false);
                    final String internetConNetwork = scanInternetSpeed.getNetworkConectivityType();
                    if(internetConNetwork == "WIFI"){
                        buttonUploadInfo.setText("Subir Informacion");
                        buttonUploadInfo.setEnabled(true);
                        buttonUploadInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {

                                Log.d("URL_JSON","Variable network es:  "+internetConNetwork);
                                Log.d("URL_JSON","Boton buttonUploadInfo presionado ");
                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                //seccion Total
                                cursorQuery = scanDbHelper.getScanInfoByIsRegistered(0);
                                ArrayList<String> jsonInputString  =  scanDbHelper.getScanInfoInJson(cursorQuery);
                                if(jsonInputString.size() != 0){
                                    for(int i = 0 ; i < jsonInputString.size(); i++){
                                        Log.d("URL_JSON","LTEvalor de i es "+i);
                                        HttpJsonPost jsonPost = new HttpJsonPost();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            Log.d("URL_JSON","Crea y envia jsonInputString "+jsonInputString.get(i));
                                            String response = jsonPost.postJsonToServer("http://ec2-3-135-61-30.us-east-2.compute.amazonaws.com:80/add", jsonInputString.get(i));
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            Log.d("Finall URL Post","mensaje es "+response);
                                        }
                                    }
                                }else{
                                    buttonUploadInfo.setText("No hay datos que insertar");
                                    buttonUploadInfo.setEnabled(false);
                                }
                            }
                        });

                    }
                    else{
                        buttonUploadInfo.setText("No se puede subir subir informacion en red Movil");
                        buttonUploadInfo.setEnabled(false);
                    }
                }
                else if (phonestate == "STATE_POWER_OFF" && isConected == "NoConectado"){
                    buttonUploadInfo.setText("No se puede subir info");
                    buttonUploadInfo.setEnabled(false);
                    buttonColectInfo.setText("Modo Avion");
                    buttonColectInfo.setEnabled(false);
                }
                else if (phonestate == "STATE_POWER_OFF" && isConected == "Conectado"){
                    buttonUploadInfo.setText("Modo Avion");
                    buttonUploadInfo.setEnabled(false);
                    final String internetConNetwork = scanInternetSpeed.getNetworkConectivityType();
                    if(internetConNetwork == "WIFI"){
                        buttonUploadInfo.setText("Subir Informacion");
                        buttonUploadInfo.setEnabled(true);
                        buttonUploadInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {

                                Log.d("URL_JSON","Variable network es:  "+internetConNetwork);
                                Log.d("URL_JSON","Boton buttonUploadInfo presionado ");
                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                //seccion Total
                                cursorQuery = scanDbHelper.getScanInfoByIsRegistered(0);
                                ArrayList<String> jsonInputString  =  scanDbHelper.getScanInfoInJson(cursorQuery);
                                if(jsonInputString.size() != 0){
                                    for(int i = 0 ; i < jsonInputString.size(); i++){
                                        Log.d("URL_JSON","LTEvalor de i es "+i);
                                        HttpJsonPost jsonPost = new HttpJsonPost();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            Log.d("URL_JSON","Crea y envia jsonInputString "+jsonInputString.get(i));
                                            String response = jsonPost.postJsonToServer("http://ec2-3-135-61-30.us-east-2.compute.amazonaws.com:80/add", jsonInputString.get(i));
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            Log.d("Finall URL Post","mensaje es "+response);
                                        }
                                    }
                                }else{
                                    buttonUploadInfo.setText("No hay datos que insertar");
                                    buttonUploadInfo.setEnabled(false);
                                }
                            }
                        });

                    }
                    else{
                        buttonUploadInfo.setText("No se puede subir subir informacion en red Movil");
                        buttonUploadInfo.setEnabled(false);
                    }
                }
                else if (phonestate == "STATE_IN_SERVICE" && isConected == "Conectado"){
                    buttonUploadInfo.setText("preparado para subir");
                    buttonUploadInfo.setEnabled(true);
                    buttonColectInfo.setText("Colectar & medir");
                    buttonColectInfo.setEnabled(true);
                    Pair<Double,Double> latLonLocation = locationAddress.getLatLongFromLocation(mainContext);
                    latitude = latLonLocation.first;
                    longitude = latLonLocation.second;
                    final DecimalFormat dec = new DecimalFormat("#.##");
                    final String simOperatorId = phoneInformation.get(3);
                    final String operatorMcc = scanCellularActivity.getDevMccId();
                    final String operatorMnc = scanCellularActivity.getDevMncId();
                    final String internetConNetwork = scanInternetSpeed.getNetworkConectivityType();

                    tempBlackList = new HashSet<>();

                    getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
                    getSpeedTestHostsHandler.start();

                    if(internetConNetwork == "WIFI"){
                        buttonUploadInfo.setText("Subir Informacion");
                        buttonUploadInfo.setEnabled(true);
                        buttonUploadInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {

                                Log.d("URL_JSON","Variable network es:  "+internetConNetwork);
                                Log.d("URL_JSON","Boton buttonUploadInfo presionado ");
                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                //seccion Total
                                cursorQuery = scanDbHelper.getScanInfoByIsRegistered(0);
                                ArrayList<String> jsonInputString  =  scanDbHelper.getScanInfoInJson(cursorQuery);
                                if(jsonInputString.size() != 0){
                                    for(int i = 0 ; i < jsonInputString.size(); i++){
                                        Log.d("URL_JSON","LTEvalor de i es "+i);
                                        HttpJsonPost jsonPost = new HttpJsonPost();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            Log.d("URL_JSON","Crea y envia jsonInputString "+jsonInputString.get(i));
                                            String response = jsonPost.postJsonToServer("http://ec2-3-135-61-30.us-east-2.compute.amazonaws.com:80/add", jsonInputString.get(i));
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            Log.d("Finall URL Post","mensaje es "+response);
                                        }
                                    }
                                }else{
                                    buttonUploadInfo.setText("No hay datos que insertar");
                                }
                            }
                        });

                    }
                    else{
                        buttonUploadInfo.setText("No se puede subir subir informacion en red Movil");
                        buttonUploadInfo.setEnabled(false);
                    }


                    buttonColectInfo.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            buttonColectInfo.setEnabled(false);

                            //Restart test icin eger baglanti koparsa
                            if (getSpeedTestHostsHandler == null) {
                                getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
                                getSpeedTestHostsHandler.start();
                            }

                            new Thread(new Runnable() {
                                RotateAnimation rotate;



                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            buttonColectInfo.setText("Consiguiendo mejor servidor basado en ping...");
                                        }
                                    });

                                    //Get egcodes.speedtest hosts
                                    int timeCount = 600; //1min
                                    while (!getSpeedTestHostsHandler.isFinished()) {
                                        timeCount--;
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                        }
                                        if (timeCount <= 0) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(mainContext, "No Connection...", Toast.LENGTH_LONG).show();
                                                    buttonColectInfo.setEnabled(true);
                                                    buttonColectInfo.setTextSize(16);
                                                    buttonColectInfo.setText("Comenzar de nuevo");
                                                    Log.d("SpeedVars","El valor de ping es"+pingTimeMilis+" el valor de download es "+downloadSpeed+" el valor de upload es "+uploadSpeed);
                                                }
                                            });
                                            getSpeedTestHostsHandler = null;
                                            return;
                                        }
                                    }

                                    //Find closest server
                                    HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey();
                                    HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue();
                                    double selfLat = getSpeedTestHostsHandler.getSelfLat();
                                    double selfLon = getSpeedTestHostsHandler.getSelfLon();
                                    double tmp = 19349458;
                                    double dist = 0.0;
                                    int findServerIndex = 0;
                                    for (int index : mapKey.keySet()) {
                                        if (tempBlackList.contains(mapValue.get(index).get(5))) {
                                            continue;
                                        }

                                        Location source = new Location("Source");
                                        source.setLatitude(selfLat);
                                        source.setLongitude(selfLon);

                                        List<String> ls = mapValue.get(index);
                                        Location dest = new Location("Dest");
                                        dest.setLatitude(Double.parseDouble(ls.get(0)));
                                        dest.setLongitude(Double.parseDouble(ls.get(1)));

                                        double distance = source.distanceTo(dest);
                                        if (tmp > distance) {
                                            tmp = distance;
                                            dist = distance;
                                            findServerIndex = index;
                                        }
                                    }
                                    String uploadAddr = mapKey.get(findServerIndex);
                                    final List<String> info = mapValue.get(findServerIndex);
                                    final double distance = dist;

                                    if (info == null) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                buttonColectInfo.setTextSize(12);
                                                buttonColectInfo.setText("Problemas con el servidor de localizacion, intente de nuevo.");
                                            }
                                        });
                                        return;
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            buttonColectInfo.setTextSize(13);
                                            buttonColectInfo.setText(String.format("Host Location: %s [Distance: %s km]", info.get(2), new DecimalFormat("#.##").format(distance / 1000)));
                                        }
                                    });

                                    //Init Ping graphic

                                    XYSeriesRenderer pingRenderer = new XYSeriesRenderer();
                                    XYSeriesRenderer.FillOutsideLine pingFill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ALL);
                                    pingFill.setColor(Color.parseColor("#4d5a6a"));
                                    pingRenderer.addFillOutsideLine(pingFill);
                                    pingRenderer.setDisplayChartValues(false);
                                    pingRenderer.setShowLegendItem(false);
                                    pingRenderer.setColor(Color.parseColor("#4d5a6a"));
                                    pingRenderer.setLineWidth(5);
                                    final XYMultipleSeriesRenderer multiPingRenderer = new XYMultipleSeriesRenderer();
                                    multiPingRenderer.setXLabels(0);
                                    multiPingRenderer.setYLabels(0);
                                    multiPingRenderer.setZoomEnabled(false);
                                    multiPingRenderer.setXAxisColor(Color.parseColor("#647488"));
                                    multiPingRenderer.setYAxisColor(Color.parseColor("#2F3C4C"));
                                    multiPingRenderer.setPanEnabled(true, true);
                                    multiPingRenderer.setZoomButtonsVisible(false);
                                    multiPingRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
                                    multiPingRenderer.addSeriesRenderer(pingRenderer);

                                    //Init Download graphic

                                    XYSeriesRenderer downloadRenderer = new XYSeriesRenderer();
                                    XYSeriesRenderer.FillOutsideLine downloadFill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ALL);
                                    downloadFill.setColor(Color.parseColor("#4d5a6a"));
                                    downloadRenderer.addFillOutsideLine(downloadFill);
                                    downloadRenderer.setDisplayChartValues(false);
                                    downloadRenderer.setColor(Color.parseColor("#4d5a6a"));
                                    downloadRenderer.setShowLegendItem(false);
                                    downloadRenderer.setLineWidth(5);
                                    final XYMultipleSeriesRenderer multiDownloadRenderer = new XYMultipleSeriesRenderer();
                                    multiDownloadRenderer.setXLabels(0);
                                    multiDownloadRenderer.setYLabels(0);
                                    multiDownloadRenderer.setZoomEnabled(false);
                                    multiDownloadRenderer.setXAxisColor(Color.parseColor("#647488"));
                                    multiDownloadRenderer.setYAxisColor(Color.parseColor("#2F3C4C"));
                                    multiDownloadRenderer.setPanEnabled(false, false);
                                    multiDownloadRenderer.setZoomButtonsVisible(false);
                                    multiDownloadRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
                                    multiDownloadRenderer.addSeriesRenderer(downloadRenderer);

                                    //Init Upload graphic

                                    XYSeriesRenderer uploadRenderer = new XYSeriesRenderer();
                                    XYSeriesRenderer.FillOutsideLine uploadFill = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ALL);
                                    uploadFill.setColor(Color.parseColor("#4d5a6a"));
                                    uploadRenderer.addFillOutsideLine(uploadFill);
                                    uploadRenderer.setDisplayChartValues(false);
                                    uploadRenderer.setColor(Color.parseColor("#4d5a6a"));
                                    uploadRenderer.setShowLegendItem(false);
                                    uploadRenderer.setLineWidth(5);
                                    final XYMultipleSeriesRenderer multiUploadRenderer = new XYMultipleSeriesRenderer();
                                    multiUploadRenderer.setXLabels(0);
                                    multiUploadRenderer.setYLabels(0);
                                    multiUploadRenderer.setZoomEnabled(false);
                                    multiUploadRenderer.setXAxisColor(Color.parseColor("#647488"));
                                    multiUploadRenderer.setYAxisColor(Color.parseColor("#2F3C4C"));
                                    multiUploadRenderer.setPanEnabled(false, false);
                                    multiUploadRenderer.setZoomButtonsVisible(false);
                                    multiUploadRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
                                    multiUploadRenderer.addSeriesRenderer(uploadRenderer);

                                    //Reset value, graphics
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            pingTextView.setText("0 ms");
                                            chartPing.removeAllViews();
                                            downloadTextView.setText("0 Mbps");
                                            chartDownload.removeAllViews();
                                            uploadTextView.setText("0 Mbps");
                                            chartUpload.removeAllViews();
                                        }
                                    });
                                    final List<Double> pingRateList = new ArrayList<>();
                                    final List<Double> downloadRateList = new ArrayList<>();
                                    final List<Double> uploadRateList = new ArrayList<>();
                                    Boolean pingTestStarted = false;
                                    Boolean pingTestFinished = false;
                                    Boolean downloadTestStarted = false;
                                    Boolean downloadTestFinished = false;
                                    Boolean uploadTestStarted = false;
                                    Boolean uploadTestFinished = false;

                                    //Init Test
                                    final PingTest pingTest = new PingTest(info.get(6).replace(":8080", ""), 6);
                                    final HttpDownloadTest downloadTest = new HttpDownloadTest(uploadAddr.replace(uploadAddr.split("/")[uploadAddr.split("/").length - 1], ""));
                                    final HttpUploadTest uploadTest = new HttpUploadTest(uploadAddr);


                                    //Tests
                                    while (true) {
                                        if (!pingTestStarted) {
                                            pingTest.start();
                                            pingTestStarted = true;
                                        }
                                        if (pingTestFinished && !downloadTestStarted) {
                                            downloadTest.start();
                                            downloadTestStarted = true;
                                        }
                                        if (downloadTestFinished && !uploadTestStarted) {
                                            uploadTest.start();
                                            uploadTestStarted = true;
                                        }


                                        //Ping Test
                                        if (pingTestFinished) {
                                            //Failure
                                            if (pingTest.getAvgRtt() == 0) {
                                                System.out.println("Ping error...");
                                                pingTimeMilis = "null";
                                            } else {
                                                //Success
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pingTextView.setText(dec.format(pingTest.getAvgRtt()) + " ms");
                                                        pingTimeMilis=""+dec.format(pingTest.getAvgRtt());
                                                    }
                                                });
                                            }
                                        } else {
                                            pingRateList.add(pingTest.getInstantRtt());

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pingTextView.setText(dec.format(pingTest.getInstantRtt()) + " ms");
                                                }
                                            });

                                            //Update chart
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // Creating an  XYSeries for Income
                                                    XYSeries pingSeries = new XYSeries("");
                                                    pingSeries.setTitle("");

                                                    int count = 0;
                                                    List<Double> tmpLs = new ArrayList<>(pingRateList);
                                                    for (Double val : tmpLs) {
                                                        pingSeries.add(count++, val);
                                                    }

                                                    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                                    dataset.addSeries(pingSeries);

                                                    GraphicalView chartView = ChartFactory.getLineChartView(getApplicationContext(), dataset, multiPingRenderer);
                                                    chartPing.addView(chartView, 0);

                                                }
                                            });
                                        }


                                        //Download Test
                                        if (pingTestFinished) {
                                            if (downloadTestFinished) {
                                                //Failure
                                                if (downloadTest.getFinalDownloadRate() == 0) {
                                                    System.out.println("Download error...");
                                                    downloadSpeed = "null";
                                                } else {
                                                    //Success
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            downloadTextView.setText(dec.format(downloadTest.getFinalDownloadRate()) + " Mbps");
                                                            downloadSpeed = ""+dec.format(downloadTest.getFinalDownloadRate());
                                                        }
                                                    });
                                                }
                                            } else {
                                                //Calc position
                                                double downloadRate = downloadTest.getInstantDownloadRate();
                                                downloadRateList.add(downloadRate);
                                                position = getPositionByRate(downloadRate);

                                                runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        //rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                                        //rotate.setInterpolator(new LinearInterpolator());
                                                        //rotate.setDuration(100);
                                                        //barImageView.startAnimation(rotate);
                                                        downloadTextView.setText(dec.format(downloadTest.getInstantDownloadRate()) + " Mbps");

                                                    }

                                                });
                                                lastPosition = position;

                                                //Update chart
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // Creating an  XYSeries for Income
                                                        XYSeries downloadSeries = new XYSeries("");
                                                        downloadSeries.setTitle("");

                                                        List<Double> tmpLs = new ArrayList<>(downloadRateList);
                                                        int count = 0;
                                                        for (Double val : tmpLs) {
                                                            downloadSeries.add(count++, val);
                                                        }

                                                        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                                        dataset.addSeries(downloadSeries);

                                                        GraphicalView chartView = ChartFactory.getLineChartView(mainContext, dataset, multiDownloadRenderer);
                                                        chartDownload.addView(chartView, 0);
                                                    }
                                                });

                                            }
                                        }


                                        //Upload Test
                                        if (downloadTestFinished) {
                                            if (uploadTestFinished) {
                                                //Failure
                                                if (uploadTest.getFinalUploadRate() == 0) {
                                                    System.out.println("Upload error...");
                                                    uploadSpeed = "null";
                                                } else {
                                                    //Success
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //Log.d("UploadSpeed","entra a hilo valor de variable "+uploadSpeed);
                                                            uploadTextView.setText(dec.format(uploadTest.getFinalUploadRate()) + " Mbps");
                                                            //uploadSpeed = Double.parseDouble(dec.format(uploadTest.getFinalUploadRate()));

                                                        }
                                                    });
                                                }
                                            } else {
                                                //Calc position
                                                double uploadRate = uploadTest.getInstantUploadRate();
                                                uploadRateList.add(uploadRate);
                                                position = getPositionByRate(uploadRate);

                                                runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        //rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                                        //rotate.setInterpolator(new LinearInterpolator());
                                                        //rotate.setDuration(100);
                                                        //barImageView.startAnimation(rotate);
                                                        uploadTextView.setText(dec.format(uploadTest.getInstantUploadRate()) + " Mbps");
                                                        uploadSpeed = ""+dec.format(uploadTest.getFinalUploadRate());
                                                    }

                                                });
                                                lastPosition = position;

                                                //Update chart
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // Creating an  XYSeries for Income
                                                        XYSeries uploadSeries = new XYSeries("");
                                                        uploadSeries.setTitle("");

                                                        int count = 0;
                                                        List<Double> tmpLs = new ArrayList<>(uploadRateList);
                                                        for (Double val : tmpLs) {
                                                            if (count == 0) {
                                                                val = 0.0;
                                                            }
                                                            uploadSeries.add(count++, val);
                                                        }

                                                        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                                        dataset.addSeries(uploadSeries);

                                                        GraphicalView chartView = ChartFactory.getLineChartView(mainContext, dataset, multiUploadRenderer);
                                                        chartUpload.addView(chartView, 0);
                                                    }
                                                });

                                            }
                                        }

                                        //Test bitti
                                        if (pingTestFinished && downloadTestFinished && uploadTest.isFinished()) {
                                            break;
                                        }

                                        if (pingTest.isFinished()) {
                                            pingTestFinished = true;
                                        }
                                        if (downloadTest.isFinished()) {
                                            downloadTestFinished = true;
                                        }
                                        if (uploadTest.isFinished()) {
                                            uploadTestFinished = true;
                                        }

                                        if (pingTestStarted && !pingTestFinished) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                            }
                                        } else {
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {
                                            }
                                        }
                                    }

                                    //Thread bitiminde button yeniden aktif ediliyor
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            buttonColectInfo.setEnabled(true);
                                            buttonColectInfo.setTextSize(16);
                                            buttonColectInfo.setText("Comenzar de Nuevo");

                                            Log.d("SpeedVars","El valor de ping es"+pingTimeMilis+" el valor de download es "+downloadSpeed+" el valor de upload es "+uploadSpeed+" red "+phoneNetTechnology);
                                            //Seccion HSPA
                                            if(phoneNetTechnology == "HSPA+" || phoneNetTechnology == "HSPA" || phoneNetTechnology == "UMTS"){
                                                ArrayList<Integer> strengthInfo = scanCellularActivity.getDevStrengthSignal();
                                                ArrayList<Integer> cellIdentity = scanCellularActivity.getDevCellIdentity();
                                                int phoneSignalStrength =  strengthInfo.get(0);
                                                int phoneAsuStrength =  strengthInfo.get(1);
                                                int phoneSignalLevel =  strengthInfo.get(2);
                                                int cellWcdmaLac = cellIdentity.get(0);
                                                int cellWcdmaUcid = cellIdentity.get(1);
                                                int cellWcdmaPsc = cellIdentity.get(2);
                                                int cellWcdmaCid = cellIdentity.get(3);
                                                int cellWcdmaRnc = cellIdentity.get(4);
                                                int cellWcdmaUarfcn = cellIdentity.get(5);
                                                String signalQuality = scanCellularActivity.getSignalQuality(strengthInfo.get(0));
                                                int fieldIsRegistered = 0;
                                                String phoneRsrpStrength="null";
                                                String phoneRssnrStrength="null";
                                                String phoneTimingAdvance="null";
                                                String phoneCqiStrength="null";
                                                String phoneRsrqStrength="null";
                                                String cellLtePci="null";
                                                String cellLteCid="null";
                                                String cellLteTac="null";
                                                String cellLteeNode="null";
                                                String cellLteEarfcn="null";
                                                String cellBslat="null";
                                                String cellBslon="null";
                                                String cellSid="null";
                                                String cellNid="null";
                                                String cellBid="null";
                                                String cellGsmArcfn="null";
                                                String cellGsmLac="null";
                                                String cellGsmCid="null";

                                                ScanMetadata scanMetadata = new ScanMetadata(""+timestamp, ""+countryISO, ""+phoneOperatorId, ""+simOperatorId, ""+operatorMcc, ""+operatorMnc, ""+devManufacturer, ""+devModel, ""+isConected, ""+phoneNetStandard, ""+phoneNetTechnology, ""+internetConNetwork, ""+latitude, ""+longitude, ""+pingTimeMilis, ""+downloadSpeed, ""+uploadSpeed, ""+phoneSignalStrength, ""+phoneAsuStrength, ""+phoneSignalLevel, ""+signalQuality, 0, ""+phoneRsrpStrength, ""+phoneRssnrStrength, ""+phoneTimingAdvance, ""+phoneCqiStrength, ""+phoneRsrqStrength, ""+cellLtePci, ""+cellLteCid, ""+cellLteTac, ""+cellLteeNode, ""+cellLteEarfcn, ""+cellBslat, ""+cellBslon, ""+cellSid, ""+cellNid, ""+cellBid, ""+cellWcdmaLac, ""+cellWcdmaUcid, ""+cellWcdmaUarfcn, ""+cellWcdmaPsc, ""+cellWcdmaCid, ""+cellWcdmaRnc, ""+cellGsmArcfn, ""+cellGsmLac, ""+cellGsmCid);
                                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                                scanDbHelper.saveSqlScan(scanMetadata);
                                            }
                                            //Seccion Lte
                                            if(phoneNetTechnology == "LTE"){
                                                ArrayList<Integer> strengthInfo = scanCellularActivity.getDevStrengthSignal();
                                                ArrayList<Integer> cellIdentity = scanCellularActivity.getDevCellIdentity();
                                                int phoneSignalStrength = strengthInfo.get(0);
                                                int phoneAsuStrength = strengthInfo.get(1);
                                                int phoneSignalLevel = strengthInfo.get(2);
                                                int phoneRsrpStrength = strengthInfo.get(3);
                                                int phoneRsrqStrength = strengthInfo.get(4);
                                                double phoneRssnrStrength = 5.4;
                                                int phoneTimingAdvance = strengthInfo.get(6);
                                                int phoneCqiStrength = strengthInfo.get(7);
                                                int cellLtePci = cellIdentity.get(0);
                                                int cellLteTac = cellIdentity.get(1);
                                                int cellLteeNodeB = cellIdentity.get(2);
                                                int cellLteCid = cellIdentity.get(3);
                                                int cellLteEarfcn = cellIdentity.get(4);
                                                String signalQuality = scanCellularActivity.getSignalQuality(strengthInfo.get(0));
                                                int fieldIsRegistered = 0;
                                                String cellBslat = "null";
                                                String cellBslon = "null";
                                                String cellSid = "null";
                                                String cellNid = "null";
                                                String cellBid = "null";
                                                String cellWcdmaLac = "null";
                                                String cellWcdmaUcid = "null";
                                                String cellWcdmaUarfcn = "null";
                                                String cellWcdmaPsc = "null";
                                                String cellWcdmaCid = "null";
                                                String cellWcdmaRnc = "null";
                                                String cellGsmArcfn = "null";
                                                String cellGsmLac = "null";
                                                String cellGsmCid = "null";

                                                ScanMetadata scanMetadata = new ScanMetadata(""+timestamp, ""+countryISO, ""+phoneOperatorId, ""+simOperatorId, ""+operatorMcc, ""+operatorMnc, ""+devManufacturer, ""+devModel, ""+isConected, ""+phoneNetStandard, ""+phoneNetTechnology, ""+internetConNetwork, ""+latitude, ""+longitude, ""+pingTimeMilis, ""+downloadSpeed, ""+uploadSpeed, ""+phoneSignalStrength, ""+phoneAsuStrength, ""+phoneSignalLevel, ""+signalQuality, 0, ""+phoneRsrpStrength, ""+phoneRssnrStrength, ""+phoneTimingAdvance, ""+phoneCqiStrength, ""+phoneRsrqStrength, ""+cellLtePci, ""+cellLteCid, ""+cellLteTac, ""+cellLteeNodeB, ""+cellLteEarfcn, ""+cellBslat, ""+cellBslon, ""+cellSid, ""+cellNid, ""+cellBid, ""+cellWcdmaLac, ""+cellWcdmaUcid, ""+cellWcdmaUarfcn, ""+cellWcdmaPsc, ""+cellWcdmaCid, ""+cellWcdmaRnc, ""+cellGsmArcfn, ""+cellGsmLac, ""+cellGsmCid);
                                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                                scanDbHelper.saveSqlScan(scanMetadata);
                                            }
                                            //Seccion GSM
                                            if(phoneNetTechnology == "GSM" || phoneNetTechnology == "GPRS" || phoneNetTechnology == "EDGE"){
                                                ArrayList<Integer> strengthInfo = scanCellularActivity.getDevStrengthSignal();
                                                ArrayList<Integer> cellIdentity = scanCellularActivity.getDevCellIdentity();
                                                int phoneSignalStrength =  strengthInfo.get(0);
                                                int phoneAsuStrength =  strengthInfo.get(1);
                                                int phoneSignalLevel =  strengthInfo.get(2);
                                                int cellGsmLac  = cellIdentity.get(0);
                                                int cellGsmCid  = cellIdentity.get(1);
                                                int cellGsmArcfn = cellIdentity.get(2);
                                                String signalQuality = scanCellularActivity.getSignalQuality(strengthInfo.get(0));
                                                int fieldIsRegistered = 0;
                                                String phoneRsrpStrength = "null";
                                                String phoneRssnrStrength = "null";
                                                String phoneTimingAdvance = "null";
                                                String phoneCqiStrength = "null";
                                                String phoneRsrqStrength = "null";
                                                String cellLtePci = "null";
                                                String cellLteCid = "null";
                                                String cellLteTac = "null";
                                                String cellLteeNodeB = "null";
                                                String cellLteEarfcn = "null";
                                                String cellBslat = "null";
                                                String cellBslon = "null";
                                                String cellSid = "null";
                                                String cellNid = "null";
                                                String cellBid = "null";
                                                String cellWcdmaLac = "null";
                                                String cellWcdmaUcid = "null";
                                                String cellWcdmaUarfcn = "null";
                                                String cellWcdmaPsc = "null";
                                                String cellWcdmaCid = "null";
                                                String cellWcdmaRnc = "null";

                                                ScanMetadata scanMetadata = new ScanMetadata(""+timestamp, ""+countryISO, ""+phoneOperatorId, ""+simOperatorId, ""+operatorMcc, ""+operatorMnc, ""+devManufacturer, ""+devModel, ""+isConected, ""+phoneNetStandard, ""+phoneNetTechnology, ""+internetConNetwork, ""+latitude, ""+longitude, ""+pingTimeMilis, ""+downloadSpeed, ""+uploadSpeed, ""+phoneSignalStrength, ""+phoneAsuStrength, ""+phoneSignalLevel, ""+signalQuality, 0, ""+phoneRsrpStrength, ""+phoneRssnrStrength, ""+phoneTimingAdvance, ""+phoneCqiStrength, ""+phoneRsrqStrength, ""+cellLtePci, ""+cellLteCid, ""+cellLteTac, ""+cellLteeNodeB, ""+cellLteEarfcn, ""+cellBslat, ""+cellBslon, ""+cellSid, ""+cellNid, ""+cellBid, ""+cellWcdmaLac, ""+cellWcdmaUcid, ""+cellWcdmaUarfcn, ""+cellWcdmaPsc, ""+cellWcdmaCid, ""+cellWcdmaRnc, ""+cellGsmArcfn, ""+cellGsmLac, ""+cellGsmCid);
                                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                                scanDbHelper.saveSqlScan(scanMetadata);

                                            }
                                            //Seccion CDMA
                                            if(phoneNetTechnology == "CDMA" || phoneNetTechnology == "EVDO_0" || phoneNetTechnology == "EVDO_A" || phoneNetTechnology == "EVDO_B"){
                                                ArrayList<Integer> strengthInfo = scanCellularActivity.getDevStrengthSignal();
                                                ArrayList<Integer> cellIdentity = scanCellularActivity.getDevCellIdentity();
                                                int phoneSignalStrength =  strengthInfo.get(0);
                                                int phoneAsuStrength =  strengthInfo.get(0);
                                                int phoneSignalLevel =  strengthInfo.get(0);
                                                int cellBslat = cellIdentity.get(0);
                                                int cellBslon = cellIdentity.get(1);
                                                int cellSid = cellIdentity.get(2);
                                                int cellNid = cellIdentity.get(3);
                                                int cellBid = cellIdentity.get(4);
                                                String signalQuality = scanCellularActivity.getSignalQuality(strengthInfo.get(0));
                                                int fieldIsRegistered = 0;
                                                String phoneRsrpStrength = "null";
                                                String phoneRssnrStrength = "null";
                                                String phoneTimingAdvance = "null";
                                                String phoneCqiStrength = "null";
                                                String phoneRsrqStrength = "null";
                                                String cellLtePci = "null";
                                                String cellLteCid = "null";
                                                String cellLteTac = "null";
                                                String cellLteeNodeB = "null";
                                                String cellLteEarfcn = "null";
                                                String cellWcdmaLac = "null";
                                                String cellWcdmaUcid = "null";
                                                String cellWcdmaUarfcn = "null";
                                                String cellWcdmaPsc = "null";
                                                String cellWcdmaCid = "null";
                                                String cellWcdmaRnc = "null";
                                                String cellGsmArcfn = "null";
                                                String cellGsmLac = "null";
                                                String cellGsmCid = "null";
                                                ScanMetadata scanMetadata = new ScanMetadata(""+timestamp, ""+countryISO, ""+phoneOperatorId, ""+simOperatorId, ""+operatorMcc, ""+operatorMnc, ""+devManufacturer, ""+devModel, ""+isConected, ""+phoneNetStandard, ""+phoneNetTechnology, ""+internetConNetwork, ""+latitude, ""+longitude, ""+pingTimeMilis, ""+downloadSpeed, ""+uploadSpeed, ""+phoneSignalStrength, ""+phoneAsuStrength, ""+phoneSignalLevel, ""+signalQuality, 0, ""+phoneRsrpStrength, ""+phoneRssnrStrength, ""+phoneTimingAdvance, ""+phoneCqiStrength, ""+phoneRsrqStrength, ""+cellLtePci, ""+cellLteCid, ""+cellLteTac, ""+cellLteeNodeB, ""+cellLteEarfcn, ""+cellBslat, ""+cellBslon, ""+cellSid, ""+cellNid, ""+cellBid, ""+cellWcdmaLac, ""+cellWcdmaUcid, ""+cellWcdmaUarfcn, ""+cellWcdmaPsc, ""+cellWcdmaCid, ""+cellWcdmaRnc, ""+cellGsmArcfn, ""+cellGsmLac, ""+cellGsmCid);
                                                scanDbHelper = new ScanDbHelper(getApplicationContext());
                                                scanDbHelper.saveSqlScan(scanMetadata);

                                            }
                                        }
                                    });


                                }
                            }).start();
                        }
                    });


                }
            }
        });

    }

    public int getPositionByRate(double rate) {
        if (rate <= 1) {
            return (int) (rate * 30);

        } else if (rate <= 10) {
            return (int) (rate * 6) + 30;

        } else if (rate <= 30) {
            return (int) ((rate - 10) * 3) + 90;

        } else if (rate <= 50) {
            return (int) ((rate - 30) * 1.5) + 150;

        } else if (rate <= 100) {
            return (int) ((rate - 50) * 1.2) + 180;
        }

        return 0;
    }

    private void showPhoneStatePermission(String permission) {
        Log.d("Entra showPhoneState","la variable permiso tiene: "+permission);
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, permission);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
                Log.d("Entra showExplanation","la variable permiso tiene: "+permission);
                showExplanation("Permission Needed", "Rationale", permission, REQUEST_PERMISSION_PHONE_STATE);
            } else {
                Log.d("Entra requestpermision","la variable permiso tiene: "+permission);
                requestPermission(permission, REQUEST_PERMISSION_PHONE_STATE);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_PHONE_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showExplanation(String title,String message,final String permission,final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        Log.d("Entra requestPermission","la variable permiso tiene: "+permissionName);
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

}
