package org.jefferyemanuel.n4fix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * A {@link BroadcastReceiver} that reacts to screen off/on.  After receiving this event the wake locak is partially held
 * as this is needed on many mobile phones to continue sensor activity when the phone screen dims.  without holding a partial wake
 * lock, many phones would not report sensor events. Reference: http://nosemaj.org/android-persistent-sensors*/
public class ScreenEventsReceiver extends BroadcastReceiver {
	private WakeLock wakeLock;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			if (Consts.DEVELOPER_MODE)
				Log.v(Consts.TAG, "screen is off");

			if (Consts.DEVELOPER_MODE)
				Log.v(Consts.TAG, "Acqiuring partial wakeLock:");

			PowerManager mgr = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"MyWakeLock");
			wakeLock.acquire();
		}

		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			if (Consts.DEVELOPER_MODE)
				Log.v(Consts.TAG, "screen is on");

			if (wakeLock != null && wakeLock.isHeld()) {

				if (Consts.DEVELOPER_MODE)
					Log.v(Consts.TAG, "Releasing partial wakeLock:");

				wakeLock.release();
			}
		}
	}

	public void cleanUpCpuLocks() {

		if (wakeLock != null && wakeLock.isHeld()) {

			if (Consts.DEVELOPER_MODE)
				Log.v(Consts.TAG,
						this.getClass().toString()
								+ " is being unregistered, thus Releasing partial wakeLock:");

			wakeLock.release();
		}

	}

}
