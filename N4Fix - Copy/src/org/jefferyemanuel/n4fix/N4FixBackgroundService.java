package org.jefferyemanuel.n4fix;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by JA on 03/07/13.
 */
public class N4FixBackgroundService extends Service{
	 N4FixServiceReceiver phoneStateReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy(){
		if(Consts.DEVELOPER_MODE)
			Log.d(Consts.TAG,"onDestroy invoked from "+this.getClass().toString());
		super.onDestroy();
		unregisterListeners();	
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if(Consts.DEVELOPER_MODE)
			Log.v(Consts.TAG,"Service is being created.  onCreate invoked");

		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("android.intent.action.PHONE_STATE");
		iFilter.addAction(Intent.ACTION_SCREEN_OFF);
		iFilter.addAction(Intent.ACTION_SCREEN_ON);
		
		phoneStateReceiver = new N4FixServiceReceiver();
		registerReceiver(phoneStateReceiver, iFilter);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (Consts.DEVELOPER_MODE)
			Log.v(Consts.TAG, "N4Fix Service component started");

		return START_STICKY;
	}

	
	private void unregisterListeners() {
		
		if(Consts.DEVELOPER_MODE)
			Log.v(Consts.TAG,"unregistering all Listeners");		

		if (phoneStateReceiver != null)
		{
			unregisterReceiver(phoneStateReceiver);
			phoneStateReceiver.unregisterListeners();
		}
		
		
	}

}
