package com.excample.exampleconection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import java.util.ArrayList;

//import SQLiteOpenHelper;

public class ScanDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ScanArcotelDemoV3.db";

    public ScanDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("SqlLite","Entra a crear la base de datos "+DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("SqlLite","entra a crear la tabla"+ScanContract.ScanEntry.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ScanContract.ScanEntry.TABLE_NAME + "("
                + ScanContract.ScanEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ScanContract.ScanEntry.TIMESTAMP + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.COUNTRYISO + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONEOPERATORID + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.SIMOPERATORID + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.OPERATORMCC + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.OPERATORMNC + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.DEVMANUFACTURER + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.DEVMODEL + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.ISCONECTED + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONENETSTANDARD + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONENETTECHNOLOGY + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.INTERNETCONNETWORK + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.LATITUDE + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.LONGITUDE + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PINGTIMEMILIS + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.DOWNLOADSPEED + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.UPLOADSPEED + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONESIGNALSTRENGTH + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONEASUSTRENGTH + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONESIGNALLEVEL + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.SIGNALQUALITY + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.FIELDISREGISTERED + " INTEGER NOT NULL,"
                + ScanContract.ScanEntry.PHONERSRPSTRENGTH + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONERSSNRSTRENGTH + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONETIMINGADVANCE + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONECQISTRENGTH + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.PHONERSRQSTRENGTH + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLLTEPCI + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLLTECID + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLLTETAC + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLLTEENODEB + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLLTEEARFCN + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLBSLAT + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLBSLON + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLSID + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLNID + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLBID + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLWCDMALAC + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLWCDMAUCID + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLWCDMAUARFCN + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLWCDMAPSC + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLWCDMACID + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLWCDMARNC + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLGSMARCFN + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLGSMLAC + " TEXT NOT NULL,"
                + ScanContract.ScanEntry.CELLGSMCID + " TEXT NOT NULL,"

                + "UNIQUE (" + ScanContract.ScanEntry._ID + "))");
        Log.d("SqlLite","entra a crear la tabla"+ScanContract.ScanEntry.TABLE_NAME);
    }

    //Sección de inserción en las tablas
    public void saveSqlScan(ScanMetadata scanMetadata) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Log.d("SqlLite","entra a insert SqlLite");
        sqLiteDatabase.insert(ScanContract.ScanEntry.TABLE_NAME,null,scanMetadata.toContentValues());
    }

    //Sección de consultas de tablas
    public void querySqlScan(){

    }
    public Cursor getScanInfoById(String scanId) {
        Cursor c = getReadableDatabase().query(
                ScanContract.ScanEntry.TABLE_NAME,
                null,
                ScanContract.ScanEntry._ID + " LIKE ?",new String[]{scanId},
                null,
                null,
                null);
        return c;
    }

    //Sección Update para Json
    public void updateIsRegisteredById(String scanId) {
        ContentValues cv = new ContentValues();
        cv.put(ScanContract.ScanEntry.FIELDISREGISTERED,"1");
        int c = getWritableDatabase().update(ScanContract.ScanEntry.TABLE_NAME,
                cv,ScanContract.ScanEntry._ID + " LIKE ?",new String[]{scanId});
        Log.d("updateIsRegisteredByID","el valor de la consulta es "+c);
    }


    //Seccion consultas para Json
    public Cursor getScanInfoByIsRegistered(int isRegistered) {
        Log.d("getScanInfoByIsRegred","Entra al metodo");
        Cursor c = getReadableDatabase().query(
                ScanContract.ScanEntry.TABLE_NAME,
                null,
                ScanContract.ScanEntry.FIELDISREGISTERED + " LIKE ?",new String[]{String.valueOf(isRegistered)},
                null,
                null,
                null);
        return c;
    }

    //Sección crar formato para JSON
    public ArrayList<String> getScanInfoInJson(Cursor getScanInfoByIsRegistered){
        ArrayList<String> jsonQueryFormat = new ArrayList<String>();
        int contador = 0;
        Log.d("getScanInfoInJson","Entra al metodo");
        while(getScanInfoByIsRegistered.moveToNext()){
            Log.d("getScanInfoInJson","Entra al while");
            String scan_id = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry._ID));
            String timestamp = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.TIMESTAMP));
            String countryISO = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.COUNTRYISO));
            String phoneOperatorId = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONEOPERATORID));
            String simOperatorId = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.SIMOPERATORID));
            String operatorMcc = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.OPERATORMCC));
            String operatorMnc = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.OPERATORMNC));
            String devManufacturer = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.DEVMANUFACTURER));
            String devModel = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.DEVMODEL));
            String isConected = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.ISCONECTED));
            String phoneNetStandard = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONENETSTANDARD));
            String phoneNetTechnology = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONENETTECHNOLOGY));
            String internetConNetwork = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.INTERNETCONNETWORK));
            String latitude = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.LATITUDE));
            String longitude = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.LONGITUDE));
            String pingTimeMilis = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PINGTIMEMILIS));
            String downloadSpeed = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.DOWNLOADSPEED));
            String uploadSpeed = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.UPLOADSPEED));
            String phoneSignalStrength = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONESIGNALSTRENGTH));
            String phoneAsuStrength = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONEASUSTRENGTH));
            String phoneSignalLevel = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONESIGNALLEVEL));
            String signalQuality = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.SIGNALQUALITY));
            String fieldIsRegistered = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.FIELDISREGISTERED));
            String phoneRsrpStrength = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONERSRPSTRENGTH));
            String phoneRssnrStrength = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONERSSNRSTRENGTH));
            String phoneTimingAdvance = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONETIMINGADVANCE));
            String phoneCqiStrength = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONECQISTRENGTH));
            String phoneRsrqStrength = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.PHONERSRQSTRENGTH));
            String cellLtePci = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLLTEPCI));
            String cellLteCid = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLLTECID));
            String cellLteTac = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLLTETAC));
            String cellLteeNodeB = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLLTEENODEB));
            String cellLteEarfcn = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLLTEEARFCN));
            String cellBslat = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLBSLAT));
            String cellBslon = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLBSLON));
            String cellSid = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLSID));
            String cellNid = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLNID));
            String cellBid = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLBID));
            String cellWcdmaLac = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLWCDMALAC));
            String cellWcdmaUcid = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLWCDMAUCID));
            String cellWcdmaUarfcn = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLWCDMAUARFCN));
            String cellWcdmaPsc = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLWCDMAPSC));
            String cellWcdmaCid = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLWCDMACID));
            String cellWcdmaRnc = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLWCDMARNC));
            String cellGsmArcfn = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLGSMARCFN));
            String cellGsmLac = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLGSMLAC));
            String cellGsmCid = getScanInfoByIsRegistered.getString(getScanInfoByIsRegistered.getColumnIndex(ScanContract.ScanEntry.CELLGSMCID));


            if (Integer.parseInt(fieldIsRegistered) == 0 ){
                Log.d("getScanInfoInJson","Entra al if");
                Log.d("getScanInfoInJson","antes contador es "+contador);
                jsonQueryFormat.add("{\"timestamp\":\""+timestamp+"\"," +
                        "\"countryISO\":\""+countryISO+"\"," +
                        "\"phoneOperatorId\":\""+phoneOperatorId+"\"," +
                        "\"simOperatorId\":\""+simOperatorId+"\"," +
                        "\"operatorMcc\":\""+operatorMcc+"\"," +
                        "\"operatorMnc\":\""+operatorMnc+"\"," +
                        "\"devManufacturer\":\""+devManufacturer+"\"," +
                        "\"devModel\":\""+devModel+"\"," +
                        "\"isConected\":\""+isConected+"\"," +
                        "\"phoneNetStandard\":\""+phoneNetStandard+"\"," +
                        "\"phoneNetTechnology\":\""+phoneNetTechnology+"\"," +
                        "\"internetConNetwork\":\""+internetConNetwork+"\"," +
                        "\"latitude\":\""+latitude+"\"," +
                        "\"longitude\":\""+longitude+"\"," +
                        "\"pingTimeMilis\":\""+pingTimeMilis+"\"," +
                        "\"downloadSpeed\":\""+downloadSpeed+"\"," +
                        "\"uploadSpeed\":\""+uploadSpeed+"\"," +
                        "\"phoneSignalStrength\":\""+phoneSignalStrength+"\"," +
                        "\"phoneAsuStrength\":\""+phoneAsuStrength+"\"," +
                        "\"phoneSignalLevel\":\""+phoneSignalLevel+"\"," +
                        "\"signalQuality\":\""+signalQuality+"\"," +
                        "\"fieldIsRegistered\":\""+fieldIsRegistered+"\"," +
                        "\"phoneRsrpStrength\":\""+phoneRsrpStrength+"\"," +
                        "\"phoneRssnrStrength\":\""+phoneRssnrStrength+"\"," +
                        "\"phoneTimingAdvance\":\""+phoneTimingAdvance+"\"," +
                        "\"phoneCqiStrength\":\""+phoneCqiStrength+"\"," +
                        "\"phoneRsrqStrength\":\""+phoneRsrqStrength+"\"," +
                        "\"cellLtePci\":\""+cellLtePci+"\"," +
                        "\"cellLteCid\":\""+cellLteCid+"\"," +
                        "\"cellLteTac\":\""+cellLteTac+"\"," +
                        "\"cellLteeNodeB\":\""+cellLteeNodeB+"\"," +
                        "\"cellLteEarfcn\":\""+cellLteEarfcn+"\"," +
                        "\"cellBslat\":\""+cellBslat+"\"," +
                        "\"cellBslon\":\""+cellBslon+"\"," +
                        "\"cellSid\":\""+cellSid+"\"," +
                        "\"cellNid\":\""+cellNid+"\"," +
                        "\"cellBid\":\""+cellBid+"\"," +
                        "\"cellWcdmaLac\":\""+cellWcdmaLac+"\"," +
                        "\"cellWcdmaUcid\":\""+cellWcdmaUcid+"\"," +
                        "\"cellWcdmaUarfcn\":\""+cellWcdmaUarfcn+"\"," +
                        "\"cellWcdmaPsc\":\""+cellWcdmaPsc+"\"," +
                        "\"cellWcdmaCid\":\""+cellWcdmaCid+"\"," +
                        "\"cellWcdmaRnc\":\""+cellWcdmaRnc+"\"," +
                        "\"cellGsmArcfn\":\""+cellGsmArcfn+"\"," +
                        "\"cellGsmLac\":\""+cellGsmLac+"\"," +
                        "\"cellGsmCid\":\""+cellGsmCid+"\"}");

                updateIsRegisteredById(scan_id);
                Log.d("getScanInfoInJson","despues contador es "+contador);
                Log.d("getScanInfoInJson","jsonQueryFormat es "+jsonQueryFormat.get(contador));
                contador = contador+1;
            }
        }
        return jsonQueryFormat;
    }


    //Sección de consultas para traer toda la información de las tablas por tecnología
    public Cursor getAllScanInfo() {
        return getReadableDatabase()
                .query(
                        ScanContract.ScanEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    //sección para consultas de actualización de mapas
    public ArrayList<String> getMapQuery(Cursor queryCursor){
        Log.d("SqlLite","Entra a getMapQuery");

        ArrayList<String> queryMapFormat = new ArrayList<String>();
        int contador = 0;

        while(queryCursor.moveToNext()){
            String operatorName = queryCursor.getString(queryCursor.getColumnIndex(ScanContract.ScanEntry.SIMOPERATORID));
            String phoneNetworType = queryCursor.getString(queryCursor.getColumnIndex(ScanContract.ScanEntry.PHONENETTECHNOLOGY));
            String phoneSignalStrength = queryCursor.getString(queryCursor.getColumnIndex(ScanContract.ScanEntry.PHONESIGNALSTRENGTH));
            String signalQuality = queryCursor.getString(queryCursor.getColumnIndex(ScanContract.ScanEntry.SIGNALQUALITY));
            String latitude = queryCursor.getString(queryCursor.getColumnIndex(ScanContract.ScanEntry.LATITUDE));
            String longitude = queryCursor.getString(queryCursor.getColumnIndex(ScanContract.ScanEntry.LONGITUDE));
            queryMapFormat.add(operatorName+";"
                    +phoneNetworType+";"
                    +phoneSignalStrength+";"
                    +latitude+";"
                    +longitude+";"
                    +signalQuality);

            Log.d("getMapQuery","queryMapFormat es "+queryMapFormat.get(contador)+" contador es "+contador);
            contador = contador+1;
        }
        return queryMapFormat;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("SqlLite","Entra a OnUpgrade");
        db.execSQL("DROP TABLE IF EXISTS "+ScanContract.ScanEntry.TABLE_NAME);
        onCreate(db);
    }
}
