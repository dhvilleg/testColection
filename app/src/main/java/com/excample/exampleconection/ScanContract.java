package com.excample.exampleconection;

import android.content.ContentValues;
import android.provider.BaseColumns;

public class ScanContract {

    public static abstract class ScanEntry implements BaseColumns {
        public static final String TABLE_NAME ="cellularScan";
        public static final String TIMESTAMP = "timestamp";
        public static final String COUNTRYISO = "countryISO";
        public static final String PHONEOPERATORID = "phoneOperatorId";
        public static final String SIMOPERATORID = "simOperatorId";
        public static final String OPERATORMCC = "operatorMcc";
        public static final String OPERATORMNC = "operatorMnc";
        public static final String DEVMANUFACTURER = "devManufacturer";
        public static final String DEVMODEL = "devModel";
        public static final String ISCONECTED = "isConected";
        public static final String PHONENETSTANDARD = "phoneNetStandard";
        public static final String PHONENETTECHNOLOGY = "phoneNetTechnology";
        public static final String INTERNETCONNETWORK = "internetConNetwork";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String PINGTIMEMILIS = "pingTimeMilis";
        public static final String DOWNLOADSPEED = "downloadSpeed";
        public static final String UPLOADSPEED = "uploadSpeed";
        public static final String PHONESIGNALSTRENGTH = "phoneSignalStrength";
        public static final String PHONEASUSTRENGTH = "phoneAsuStrength";
        public static final String PHONESIGNALLEVEL = "phoneSignalLevel";
        public static final String SIGNALQUALITY = "signalQuality";
        public static final String FIELDISREGISTERED = "fieldIsRegistered";
        public static final String PHONERSRPSTRENGTH = "phoneRsrpStrength";
        public static final String PHONERSSNRSTRENGTH = "phoneRssnrStrength";
        public static final String PHONETIMINGADVANCE = "phoneTimingAdvance";
        public static final String PHONECQISTRENGTH = "phoneCqiStrength";
        public static final String PHONERSRQSTRENGTH = "phoneRsrqStrength";
        public static final String CELLLTEPCI = "cellLtePci";
        public static final String CELLLTECID = "cellLteCid";
        public static final String CELLLTETAC = "cellLteTac";
        public static final String CELLLTEENODEB = "cellLteeNodeB";
        public static final String CELLLTEEARFCN = "cellLteEarfcn";
        public static final String CELLBSLAT = "cellBslat";
        public static final String CELLBSLON = "cellBslon";
        public static final String CELLSID = "cellSid";
        public static final String CELLNID = "cellNid";
        public static final String CELLBID = "cellBid";
        public static final String CELLWCDMALAC = "cellWcdmaLac";
        public static final String CELLWCDMAUCID = "cellWcdmaUcid";
        public static final String CELLWCDMAUARFCN = "cellWcdmaUarfcn";
        public static final String CELLWCDMAPSC = "cellWcdmaPsc";
        public static final String CELLWCDMACID = "cellWcdmaCid";
        public static final String CELLWCDMARNC = "cellWcdmaRnc";
        public static final String CELLGSMARCFN = "cellGsmArcfn";
        public static final String CELLGSMLAC = "cellGsmLac";
        public static final String CELLGSMCID = "cellGsmCid";


    }

}
