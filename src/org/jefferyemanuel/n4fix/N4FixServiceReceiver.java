package org.jefferyemanuel.n4fix;

import org.jefferyemanuel.n4fix.Consts.CALL_TYPE;
import org.jefferyemanuel.n4fix.ShakeEventListener.OnShakeListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * A {@link BroadcastReceiver} that also listens for sensor shake events and reacts by holding a full cpu wakelock for 
 * an elapsed time frame. Specifically, filters for phone state change events and when even is OFFHOOK invokes another 
 * broadcastReceiver (ScreenEventsReceiver) to listen when the screen is off/on.  This saves battery as we only
 * listen for screen off/on while OFFHOOK. */
public class N4FixServiceReceiver extends BroadcastReceiver implements
		OnShakeListener {

	private SensorManager mSensorManager;
	private ShakeEventListener mSensorEventListener;
	private ScreenEventsReceiver mScreenEventsReceiver; //listens for screen off/on events
	private Context context;

	

	private CALL_TYPE mCallType;
	
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void onReceive(Context context, Intent intent) {

		String newPhoneState = intent.hasExtra(TelephonyManager.EXTRA_STATE) ? intent
				.getStringExtra(TelephonyManager.EXTRA_STATE) : null;

		if (Consts.DEVELOPER_MODE)
			Log.v(Consts.TAG, "Device Telephony State Is:" + newPhoneState);

		if (TelephonyManager.EXTRA_STATE_IDLE.equals(newPhoneState)) {
			if (mCallType == CALL_TYPE.CALL_STARTED) {
				mCallType = CALL_TYPE.CALL_STOPPED;
				unregisterListeners();
			}

		}

		if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(newPhoneState)) {

			if (mCallType != CALL_TYPE.CALL_STARTED) {

				mCallType = CALL_TYPE.CALL_STARTED;
				//set the context
				setContext(context);
				/*
				 * now that phone is OFFHOOK lets start listening for shake
				 * events
				 */
				startSensor();

				IntentFilter iFilter = new IntentFilter();
				iFilter.addAction(Intent.ACTION_SCREEN_OFF);
				iFilter.addAction(Intent.ACTION_SCREEN_ON);

				/*
				 * now that phone is OFFHOOK lets begin listening for screen
				 * off/on events and gain a partial wakelock accordingly
				 */
				mScreenEventsReceiver = new ScreenEventsReceiver();
				context.registerReceiver(mScreenEventsReceiver, iFilter);
			}

		}
	}

	/*
	 * registers for shake sensor events
	 * */
	
	public void startSensor() {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		final int defaultForce = Integer.parseInt(context
				.getString(R.string.default_force));

		/*
		 * retrieve the users preference which we retrieve here instead of storing as member as 
		 * this can change dynamically while service is running
		*/
		final int forceToApply = preferences.getInt(
				context.getString(R.string.cookie_force), defaultForce);

		mSensorEventListener = new ShakeEventListener();
		ShakeEventListener.setMIN_FORCE(forceToApply);

		mSensorEventListener.setOnShakeListener(this);

		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
	
		//REGISTER TO LISTEN FOR SENSOR EVENTS
		mSensorManager.registerListener(mSensorEventListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

	}

	@Override
	public void onShake() {
		// TODO Auto-generated method stub
		wakeUp(Consts.WAKELOCK_ELAPSEDTIME);

		if (Consts.DEVELOPER_MODE) {
			Log.v(Consts.TAG, "phone is being shaken");
		}

	}

	/*
	 * Used to Acquire a full CPU wake lock
	 * */
	private void wakeUp(int elapsedTime) {
		PowerManager pm = (PowerManager) getContext().getSystemService(
				Context.POWER_SERVICE);

		boolean isScreenOn = pm.isScreenOn();

		if (!isScreenOn) {
			WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.ON_AFTER_RELEASE, Consts.TAG + " Lock");

			wl.acquire(elapsedTime);

			WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					Consts.TAG + " " + "CpuLock");

			wl_cpu.acquire(elapsedTime);
		}
	}

	/* A clean up method which we make public so that background component service can call incase onDestroy is called.
	 * this ensures all receivers released and saves battery*/
	public void unregisterListeners() {

		if (mSensorEventListener != null) {
			if (Consts.DEVELOPER_MODE)
				Log.v(Consts.TAG, "unregistering sensor Listener");
			mSensorManager.unregisterListener(mSensorEventListener);
		}

		if (mScreenEventsReceiver != null){
		
			if (Consts.DEVELOPER_MODE)
				Log.v(Consts.TAG, "unregistering screenEvent receiver");
			
			mScreenEventsReceiver.cleanUpCpuLocks();		
			getContext().unregisterReceiver(mScreenEventsReceiver);
	
		
		}
		
	}
}