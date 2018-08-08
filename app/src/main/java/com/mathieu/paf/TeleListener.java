package com.mathieu.paf;

import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.util.List;

/**
 * Created by di on 16-Jun-15.
 */
public class TeleListener extends PhoneStateListener {

    //les attributs
    private String callState, imei, plmn, operateur, networkType;
    private boolean roaming;
    private int signalStrength;
    //les RSSI, CID et LAC des voisins
    private int[][] neighborCells;

    //les getters
    public String getCallState(){
        return callState;
    }
    /*
    public String getImei(){
        return imei;
    }
    public String getPlmn(){
        return plmn;
    }
    public String getOperateur(){
        return operateur;
    }*/

    public String getNetworkVoix(){
        return networkType;
    }
    public String getRoaming(){
        if(roaming)
            return "true";
        else return "false";
    }

    public int getSignalStrength(){
        return signalStrength;
    }
    public int[][] getNeighborCells(){
        return neighborCells;
    }

    //les setters

    //ne pas utiliser en dehors
    public void setCallState(int state){
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                // CALL_STATE_IDLE;
                callState="Idle";
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                // CALL_STATE_OFFHOOK;
               callState="Offhook";
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                // CALL_STATE_RINGING
                callState="Ringing";
                break;
            default:
                break;
        }
    }
    /*
    //à faire toutes les demi secondes
    public void setPlmn(){
        plmn = telephonyManager.getNetworkOperator();
    }

    //à faire toutes les demi secondes
    public void setOperateur(){
        operateur= telephonyManager.getNetworkOperatorName();
    }
    */

    //à faire toutes les demi secondes
    public void setNetworkVoix(int netType){
        switch (netType) {
            //On peut rajouter d'autres types de réseau dans le case
            case TelephonyManager.NETWORK_TYPE_EDGE:
                networkType = "Edge";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                networkType ="LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                networkType ="UMTS";
                break;
            default:
                networkType = "N/A";
                break;
        }
    }

    /*
    //à faire toutes les demi secondes
    public void setRoaming(){
        roaming = telephonyManager.isNetworkRoaming();
    }*/

    //ne pas utiliser en dehors
    public void setSignalStrength(int strength){
        signalStrength = strength;
    }

    //les méthodes de listener

    //Changement de RSSI
    public void onSignalStrengthChanged(SignalStrength strength){
        super.onSignalStrengthsChanged(strength);
        int strength1 = strength.getGsmSignalStrength();
        strength1 = (2*strength1) - 113; // -> dBm
        setSignalStrength(strength1);
    }

    //Changements dans les cellules
    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo){
        //super.onCellInfoChanged(cellInfo); --> impossible sur cette API
    }

    //all about neighbor cells
    public void setNeighborCells(List<NeighboringCellInfo> neighboringCellInfos){
        int n = neighboringCellInfos.size() -1;
        for(int k=0; k<n; k++){
               //Créer une matrice
            NeighboringCellInfo neighbor = neighboringCellInfos.get(k);
            neighborCells[k][1] = neighbor.getRssi();
            neighborCells[k][2] = neighbor.getCid();
            neighborCells[k][3] = neighbor.getLac();
        }
    }
    /**/
}
