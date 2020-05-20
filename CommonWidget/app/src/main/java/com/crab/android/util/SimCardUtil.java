package com.crab.android.util;

import android.content.Context;
import android.telephony.TelephonyManager;

public class SimCardUtil {
    /**
     * check the phone has sim card
     *
     * @param context the android context
     * @return true is the phone insert sim card
     */
    public static boolean hasSim(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final int simState = telephonyManager.getSimState();
        // Note that pulling the SIM card returns UNKNOWN, not ABSENT.
        return simState != TelephonyManager.SIM_STATE_ABSENT
                && simState != TelephonyManager.SIM_STATE_UNKNOWN;
    }
}
