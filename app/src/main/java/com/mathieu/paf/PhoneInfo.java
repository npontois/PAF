package com.mathieu.paf;
import android.telephony.SignalStrength;

import com.mathieu.paf.TeleListener;
/**
 * Created by di on 16-Jun-15.
 */
public class PhoneInfo {
    private TeleListener teleListener;
    private SignalStrength signalStrength;

    public TeleListener getTeleListener(){
        return teleListener;
    }

    /*public void initialize(){
        teleListener.setImei();
    }*/
    /*public void update(){
        teleListener.setNetworkType();
        teleListener.setPlmn();
        teleListener.setOperateur();
        teleListener.setRoaming();
        teleListener.onSignalStrengthsChanged(signalStrength);
        //teleListener.onCallStateChanged(); call State changed à modifier
    }*/
}
