package org.jefferyemanuel.n4fix;

/**
 * Created by JA on 03/07/13.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class myStartupIntentReceiver extends BroadcastReceiver {

    String TAG=this.getClass().toString();
    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){

            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            Boolean shouldStartBackgroundService = sp.getBoolean(context.getString(R.string.cookie_on_off), false);
            
            Log.v(TAG,"should start service:"+(shouldStartBackgroundService?"yes":"no"));
            
            if (shouldStartBackgroundService) {
                Intent svc = new Intent(context, N4FixBackgroundService.class);
                context.startService(svc);
            }
        }
    }
}