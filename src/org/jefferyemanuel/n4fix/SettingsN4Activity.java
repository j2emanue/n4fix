package org.jefferyemanuel.n4fix;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.GooglePlusUtil;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusOneButton;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI. In our case,
 * we create a checkbox to enable to disable the service and add a custom
 * preferenceDialog to adjust the strength of user shake. The background
 * component (service) that is spawned is used to create a broadcastReciever to
 * listen for phone calls and turn on sensor tracking accordingly.
 * 
 * Lastly, we interface with Google services connection callbacks inorder to
 * show a Google +1 button -- Attach the google-play-services_lib project as a
 * library in the android SDK sameples folder.
 * 
 * beta apk is here:
 * https://play.google.com/store/apps/details?id=org.jefferyemanuel.n4fix
 */
public class SettingsN4Activity extends PreferenceActivity implements
		ConnectionCallbacks, OnConnectionFailedListener {

	private ProgressDialog mConnectionProgressDialog;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	private PlusOneButton mPlusOneButton;

	@Override
	protected void onStart() {
		super.onStart();
		if (!mPlusClient.isConnecting())
			mPlusClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mPlusClient.disconnect();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// Refresh the state of the +1 button each time the activity receives focus.
		mPlusOneButton.initialize(mPlusClient,
				getString(R.string.social_recommendation_url),
				Consts.PLUS_ONE_REQUEST_CODE);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		int errorCode = GooglePlusUtil.checkGooglePlusApp(this);
		if (errorCode != GooglePlusUtil.SUCCESS) {
			if(errorCode!= GooglePlusUtil.APP_MISSING)/*dont bother to prompt if end user does not have the app
			as could be storage reasons*/
			GooglePlusUtil.getErrorDialog(errorCode, this, 0).show();
		} else {
			mPlusClient = new PlusClient.Builder(this, this, this)
					.setVisibleActivities(
							"http://schemas.google.com/AddActivity",
							"http://schemas.google.com/BuyActivity").build();

			mConnectionProgressDialog = new ProgressDialog(this);
			mConnectionProgressDialog.setMessage("Signing in...");
			mConnectionProgressDialog.setOwnerActivity(this);

		}
		mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
		promptGooglePlusSignIn(); //force inital sign in
	}

	/**
	 * set up our preference screen and load advertisement
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
		setAdvertisment();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_n4, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.share_menuitem:
			pushSMS();
			break;
		case R.id.signin_menuitem:
			promptGooglePlusSignIn();
			break;

		case R.id.signout_menuitem:
			if (mPlusClient.isConnected()) {
				mPlusClient.clearDefaultAccount();
				mPlusClient.disconnect();
				mPlusClient.connect();
			}
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	/* allows the user to send a recommendation via sms */

	private void pushSMS() {
		String msg = "https://play.google.com/store/apps/details?id=org.jefferyemanuel.n4fix";

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, msg);
		intent.putExtra(Intent.EXTRA_SUBJECT,
				getString(R.string.recommendations_sms_social));
		startActivity(Intent.createChooser(intent,
				getString(R.string.title_social_recommendations)));

	}

	private void promptGooglePlusSignIn() {

		if (!mPlusClient.isConnected()) {
			if (mConnectionResult == null) {
				// Progress bar to be displayed if the connection failure is not resolved.
				mConnectionProgressDialog.show();
			} else {
				try {
					mConnectionResult.startResolutionForResult(
							SettingsN4Activity.this,
							Consts.REQUEST_CODE_RESOLVE_ERR);
				} catch (SendIntentException e) {
					// Try connecting again.
					mConnectionResult = null;
					mPlusClient.connect();
				}
			}

		}
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown. In this case it will always be single-pane as app is designed for
	 * mobile phones only.
	 */
	private void setupSimplePreferencesScreen() {

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Add 'on off switch' preference, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		getPreferenceScreen().addPreference(fakeHeader);

		// Add 'force to apply' preference, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_data_confidence);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_force_applied);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.

		bindPreferenceSummaryToValue(findPreference(getString(R.string.cookie_force)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.cookie_on_off)));
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			Context context = preference.getContext();

			if (preference instanceof SeekBarPreference) {

				if (preference.getKey().equalsIgnoreCase(
						context.getString(R.string.cookie_force))) {

					int percentage = ((SeekBarPreference) preference)
							.getProgress();
					//		int percentage = (Integer)value;
					preference.setSummary(percentage + " %");

				}

			}

			else if (preference instanceof CheckBoxPreference) {

				Boolean shouldStartBackgroundService = (Boolean) value;

				if (shouldStartBackgroundService) {

					final Intent svc = new Intent(preference.getContext(),
							N4FixBackgroundService.class);
					context.startService(svc);
				}
				//else service should be shut down
				else {

					if (Consts.DEVELOPER_MODE)
						Log.d(Consts.TAG,
								"Attempting to stop N4Fix background Service");

					final Intent svc = new Intent(preference.getContext(),
							N4FixBackgroundService.class);
					context.stopService(svc);
				}

			}

			else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		if (preference instanceof SeekBarPreference) {

			int defaultProgress = PreferenceManager
					.getDefaultSharedPreferences(preference.getContext())
					.getInt(preference.getKey(),
							Integer.parseInt(preference.getContext().getString(
									R.string.default_force)));

			((SeekBarPreference) preference).setProgress(defaultProgress);

			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getInt(
							preference.getKey(),
							Integer.parseInt(preference.getContext().getString(
									R.string.default_force))));
		}

		else if (preference instanceof CheckBoxPreference) {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getBoolean(
							preference.getKey(), false));

		}

		else
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getString(
							preference.getKey(), ""));
	}

	/* creates a custom toast message, gives our app flavor */
	public static void createToast(String msg, Context c) {

		LayoutInflater inflater = (LayoutInflater) c
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.toast_layout, null);

		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(msg);

		Toast toast = new Toast(c);
		toast.setGravity(Gravity.BOTTOM, 0, 40);

		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	/* load ads into already inflated linear layouts */
	public void setAdvertisment() {
		// set up our advertisment
		String admob_publisherID = Consts.admob_publisherID;

		int[] idArray = { R.id.adone, R.id.adtwo, R.id.adthree /*
																 * ,
																 * R.id.adfour,
																 * R.id.adfive,
																 * R.id.adsix
																 */}; //add your ads from the xml here and thats it, all done
		int numberOfAds = idArray.length;

		// Create the adView and layout arrays
		AdRequest AD = new AdRequest();
		AdView[] adViews = new AdView[numberOfAds];
		LinearLayout[] adlayouts = new LinearLayout[numberOfAds];

		for (int i = 0; i < numberOfAds; i++) {
			adViews[i] = new AdView(this, AdSize.BANNER, admob_publisherID);
			adlayouts[i] = (LinearLayout) findViewById(idArray[i]);
			adlayouts[i].addView(adViews[i]);
			adViews[i].loadAd(AD);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {

		if (Consts.DEVELOPER_MODE)
			Log.d(Consts.TAG, "onActivityResult: responseCode-->"
					+ responseCode);
		/*
		 * checks if there was an connection error that is resolvable and tries
		 * to connect again
		 */
		if (requestCode == Consts.REQUEST_CODE_RESOLVE_ERR
				&& responseCode == RESULT_OK) {
			mConnectionResult = null;
			mPlusClient.connect();
		}
	}
	
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

		if (Consts.DEVELOPER_MODE)
			Log.d(Consts.TAG, "onConnectionFailed: connection Result: "
					+ result.isSuccess());

		if (mConnectionProgressDialog.isShowing()) {
			// The user clicked the sign-in button already. Start to resolve
			// connection errors. Wait until onConnected() to dismiss the
			// connection dialog.

			mConnectionProgressDialog.dismiss();

			if (result.hasResolution()) {
				try {
					result.startResolutionForResult(this,
							Consts.REQUEST_CODE_RESOLVE_ERR);
				} catch (SendIntentException e) {
					mPlusClient.connect();
				}
			}
		}

		// Save the intent so that we can start an activity when the user clicks
		// the sign-in button.
		mConnectionResult = result;

	}

	

	/* listen for a google services connection and react when connected */
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

		mConnectionProgressDialog.dismiss();

		if (Consts.DEVELOPER_MODE)
			Log.d(Consts.TAG, "onConnected: Connected to Google services as:"
					+ mPlusClient.getAccountName());
		String accountName = mPlusClient.getAccountName();
		createToast(accountName + " is connected to Google+.", this);//show a custom toast on success

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		if (Consts.DEVELOPER_MODE)
			Log.d(Consts.TAG, "disconnected from google services");
	}

}
