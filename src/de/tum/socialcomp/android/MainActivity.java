package de.tum.socialcomp.android;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethod.SessionCallback;

// facebook imports
import com.facebook.*;
import com.facebook.Session.StatusCallback;
import com.facebook.model.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.tum.socialcomp.android.sensor.LocationChangeListener;
import de.tum.socialcomp.android.sensor.OnLocationChangeInterface;
import de.tum.socialcomp.android.sensor.OnShakeListener;
import de.tum.socialcomp.android.sensor.ShakeListener;
import de.tum.socialcomp.android.ui.AppSectionsPagerAdapter;
import de.tum.socialcomp.android.webservices.util.HttpPoster;

/**
 * This is class represents the main activity of the game,
 * it manages the UI but also the game logic including
 * setting up services such as the Facebook login data
 * and the Google Cloud Messaging. 
 * 
 * To maneuver through the code, it is best to start with the
 * 	void onCreate(Bundle savedInstanceState) - method.
 * 
 * 
 * 
 * @author Niklas Klügel
 *
 */

@SuppressLint({ "NewApi", "ValidFragment" })
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	// Google Cloud Messaging / Play Service specifics
	private static final String EXTRA_MESSAGE = "message";
	private static final String PROPERTY_GCM_REG_ID = "GCMDeviceID";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	// For SharedPreferences and GM & FacebookData
	private static final String PROPERTY_FACEBOOK_ID = "FacebookID";
	private static final String PROPERTY_FACEBOOK_NAME = "FacebookName";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	/**
	 * Tag used on log messages.
	 */
	private static final String GCM_TAG = "GCM";
	private static final String FB_TAG = "Facebook";

	/**
	 * Shared attributes
	 */
	private GoogleCloudMessaging gcm;
	private SharedPreferences prefs;
	private Context context;
	

	private ShakeListener shakeListener;
	private LocationManager locationManager;
	private LocationChangeListener locationChangeListener;

	private ViewPager viewPager;
	private AppSectionsPagerAdapter appSectionsPagerAdapter;

	private static MainActivity instance = null;
	
	
	
	
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// basic setup for application context
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		MainActivity.instance = this;
		context = getApplicationContext();

		// This logs the Keyhash used by Facebook for App development
		// (convenience)
		this.logKeyhash();		
		
		/**********************************
		 *** The important parts of the initialization start here
		 ***
		 **********************************/

		/*****
		 * 
		 * initializing the UserInterface ...
		 * 
		 *****/
		this.initLogView();
		this.initPagination();

		
		
		/*****
		 * 
		 * initializing the sensors ...
		 * 
		 *****/
		shakeListener = new ShakeListener(this);
		this.initLocationServices();

		
		
		/*****
		 * 
		 * initializing the webservices ...
		 * 
		 * Note: the initFacebookSessionAndLoginOnCallback method will not only
		 * open a Facebook session but it will also log in to our webservice
		 * once this session is (Asynchronously) established!
		 * 
		 *****/
		this.initGoogleCloudMessaging();
		this.initFacebookSessionAndLoginOnCallback();

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();

		shakeListener.resume();
	}

	@Override
	public void onPause() {
		shakeListener.pause();
		super.onPause();
	}

	/**
	 * Initializes the tabs and adapter to show the tabs so the user can navigate between
	 * the three Fragments: MainSectionFragment, MapSectionFragment and GameSectionFragment
	 * 
	 */
	
	private void initPagination() {
		appSectionsPagerAdapter = new AppSectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		// Specify that the Home/Up button should not be enabled, since there is
		// no hierarchical
		// parent.
		actionBar.setHomeButtonEnabled(false);

		// Specify that we will be displaying tabs in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(appSectionsPagerAdapter);
		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between different app sections, select
						// the corresponding tab.
						// We can also use ActionBar.Tab#select() to do this if
						// we have a reference to the
						// Tab.
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < appSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter.
			// Also specify this Activity object, which implements the
			// TabListener interface, as the
			// listener for when this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(appSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	/**
	 * This is simply used to show the key hash that is used by
	 * Facebook to identify the developer's devices.
	 * Otherwise the application *WILL NOT* log in since the 
	 * Facebook application is not verified to run on this device.
	 * 
	 * This is a convenience function; the keyhash can also be acquired
	 * as shown in the Facebook introductory tutorial:
	 * https://developers.facebook.com/docs/android/getting-started/
	 * 
	 */
	private void logKeyhash() {
		// log the key hash so it can be pasted to the facebook developer
		// console
		try {

			PackageInfo info = getPackageManager().getPackageInfo(
					"de.tum.socialcomp.android", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KEYHASH",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	public static MainActivity getInstance() {
		return instance;
	}
	
	// this is used to show a timestamp
	private long startTime = 0L;
	public void initLogView() {
		startTime = System.currentTimeMillis();

	}
	
	/**
	 * Shows a simple log message on the MainSectionFragment.
	 * This is used to show that the application logged in.
	 * 
	 * @param logMessage
	 */
	public void showLogMessage(final String logMessage) {
		MainActivity.instance.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				TextView welcome = (TextView) MainActivity.instance
						.findViewById(R.id.welcome);
				welcome.setText(welcome.getText()
						+ "\n"
						+ (System.currentTimeMillis() - MainActivity.instance.startTime)
						/ 1000 + ": " + logMessage);
			}
		});
	}

	
	/**
	 * Initializes the location based services; when the location has changed
	 * a heartbeat signal is sent to the webservice updating the user's location
	 * in the database.
	 * 
	 */
	private void initLocationServices() {

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationChangeListener = new LocationChangeListener();

		// When the location has changed, then send an update to the webservice
		locationChangeListener
				.setOnLoctationChangedListener(new OnLocationChangeInterface() {
					@Override
					public void locationChanged(Location loc) {
						String facebookID;
						facebookID = MainActivity.this
								.getFacebookID(getBaseContext());

						if (!facebookID.isEmpty()) {
							new HttpPoster().execute(new String[] {
									"positions", facebookID,
									loc.getLongitude() + "",
									loc.getLatitude() + "", "update" });
						}

					}
				});

		// Use both, gps and network, on the cost of battery drain. But this way
		// we are likely to get some localization information in most cases.
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
			
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,
					Configuration.MinimumDistanceForLocationUpdates,
					locationChangeListener);
		} else {
			Log.e(this.getClass().getName(), "Pos: GPS not available!");
		}
		
		if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
			
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 1,
					Configuration.MinimumDistanceForLocationUpdates,
					locationChangeListener);
		} else {
			Log.e(this.getClass().getName(),"Pos: Net. Provider not available!");
		}

	}

	/**
	 * This requests a new device ID for Google Cloud Messaging and stores
	 * it in the SharedPreferences, otherwise, if already requested it takes
	 * the stored one. Using this device ID the webservice can send Push
	 * Messages to this device.
	 * 
	 * This *REQUIRES* the Google Play Services APK to be installed.
	 * 
	 * The code for this initialization procedure is directly taken from:
	 * http://developer.android.com/reference/com/google/android/gms/gcm/GoogleCloudMessaging.html
	 * 
	 */
	private void initGoogleCloudMessaging() {
		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			String regId = getGoogleCloudMessagingRegistrationID(context);

			if (regId.isEmpty()) {
				registerGoogleCloudMessagingInBackground();
			}
		} else {
			Log.e(GCM_TAG, "No valid Google Play Services APK found.");
		}
	}

	/**
	 * This method logs into Facebook. When the application is attempting to 
	 * log in for the first time, it will open a dialog whether the user accepts
	 * the application accessing Facebook user data.
	 * 
	 * Once a a Facebook session has been established the application will log 
	 * into our webservice and send:
	 * - the facebook authentication token (this will be used by the webservice to access the users facebook data)
	 * - the Google Cloud Messaging ID (this is used to contact the device from the webservice) 
	 * - the user's location
	 * 
	 * Since establishing the Facebook session is an asynchronous task (because of the involved network communication),
	 * a callback object is used to trigger the processing once this is operation is successful.
	 * Here the log in to our webservice is finally performed.
	 * 
	 * The Facebook session login is directly based on the examples:
	 * https://developers.facebook.com/docs/getting-started/facebook-sdk-for-android-using-android-studio/3.0/
	 * 
	 */
	private void initFacebookSessionAndLoginOnCallback() {
		Log.i(this.getClass().getName(), "Trying to log in to Facebook...");
				
		// start Facebook Login		
				
		final StatusCallback callback =  new Session.StatusCallback() {

			// callback when session changes state
			@Override
			public void call(Session session, SessionState state,					
					Exception exception) {
				
				Log.e("FACEBOOK", " session exception => " + exception
						+ " session state " + state);

				if (session.isOpened()) {
					// send all credentials to the server

					// get last known location
					Location lastLocation = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

					if (lastLocation != null) {
						// if the phone has never been located the last location
						// can be 0, use a default location
						new HttpPoster().execute(new String[] { "users",
								session.getAccessToken(),
								getGoogleCloudMessagingRegistrationID(context),
								"" + lastLocation.getLongitude(),
								"" + lastLocation.getLatitude(), "login" });
					} else {
						new HttpPoster().execute(new String[] { "users",
								session.getAccessToken(),
								getGoogleCloudMessagingRegistrationID(context),
								"" + Configuration.DefaultLongitude,
								"" + Configuration.DefaultLatitude, "login" });
					}
					showLogMessage("Sent login data to GameServer");

					/**
					 * This gives an example of how to access the facebook graph directly from the Facebook
					 * Android SDK; in this case we simply request the user's facebook name and display it
					 * as log message on the MainActivityFragment.
					 */
					Request.executeMeRequestAsync(session,
							new Request.GraphUserCallback() {

								// callback after Graph API response with user
								// object
								@Override
								public void onCompleted(GraphUser user,
										Response response) {
									if (user != null) {
										// check whether we have already saved
										// the facebook credentials
										if (getFacebookID(context).isEmpty()) {
											storeFacebookIDAndName(
													user.getId(),
													user.getName());
										}

										showLogMessage("Facebook> Hello "
												+ user.getName() + "!\nFB-id: "
												+ user.getId());

									}
								}

							});
				}
			}
		};
		
		final Session.OpenRequest request = new Session.OpenRequest(this);
		
		// request special permissions for the app, in our case we want to get
		// the user's friends (who are also using the application)
		
		request.setPermissions(Arrays.asList("user_friends"));
		
		request.setCallback(callback);
		
		Session session = Session.getActiveSession();
		
		if(session == null || session.isClosed()) {
			session = new Session(this);			
		}
		
		Session.setActiveSession(session);
		
		session.openForRead(request);
	}


	
	
	/**************************************************
	 * The next methods implement most of the client's game logic
	 **************************************************
	 **************************************************/
	
	
	/**
	 * Since the game messages are sent via the Google Cloud Messaging and therefore
	 * received at the client in another thread, we have to switch into the UI
	 * thread in order to display any dialogs or perform 
	 * other UI state modifying functions. 
	 * 
	 * In this way this method works as a dispatch for the received JSON objects onto
	 * the game logic that is handled in the UI thread (since it alters the UI).
	 * 
	 * @param gameMessage
	 */
	public void receivedGameMessage(final JSONObject gameMessage) {
		// Important: performing the processing in the UI thread is
		// necessary for the instantiation of ALL AsyncTasks (such as the http
		// request)

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				processGameMessageOnUIThread(gameMessage);

			}
		});
	}

	/**
	 * Main game logic - it is a simple message dispatch showing dialogs
	 * for the respective messages received.
	 * 
	 * @param gameMessage
	 */
	public void processGameMessageOnUIThread(final JSONObject gameMessage) {
		try {
			String subtype = gameMessage.getString("subtype");

			if (subtype.equals("request")) {

				// on request allow the user to join or abort the game,
				// this dialog will also take care of sending the respective
				// messages to
				// the game server
				showDialog(GameDialogs.createGameRequestDialog(gameMessage,
						this));

			} else if (subtype.equals("aborted")) {

				// if one user aborted, show who gave up
				showDialog(GameDialogs.createUserAbortDialog(gameMessage, this));

			} else if (subtype.equals("established")) {
				final String gameID = gameMessage.getString("gameID");

				// if all users accepted, start the game
				final Dialog gameEstablishedDialog = GameDialogs
						.createGameStartDialog(gameMessage, this);
				OnShakeListener onShakeListener = new OnShakeListener() {

					@Override
					public void onShake() {

						// close the dialog and send a message to the server
						gameEstablishedDialog.dismiss();

						new HttpPoster().execute(new String[] {
								"games",
								gameID,
								MainActivity.this
										.getFacebookID(MainActivity.this
												.getBaseContext()),
								"interaction" });

						// the listener will be automatically removed

					}
				};

				this.shakeListener.addOnShakeListenerOneShot(onShakeListener);

				showDialog(gameEstablishedDialog);

			} else if (subtype.equals("won")) {
				// the user won
				showDialog(GameDialogs.createUserWonDialog(gameMessage, this));

			} else if (subtype.equals("lost")) {
				// the user lost
				showDialog(GameDialogs.createUserLostDialog(gameMessage, this));

			} else if (subtype.equals("draw")) {
				// the game was a draw
				showDialog(GameDialogs.createUserDrawDialog(gameMessage, this));

			} else if (subtype.equals("poke")) {
				// the user was poked
				showDialog(GameDialogs.createUserWasPokedDialog(gameMessage,
						this));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	

	/**************************************************
	 * The following methods are simply helper methods
	 * necessary to store/retrieve preferences or 
	 * to support the registration of the Google Cloud Messaging
	 * services or to alter the UI.
	 **************************************************
	 **************************************************/
	
	void showDialog(final Dialog dia) {
		dia.show();
	}
	

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}


	private SharedPreferences getSharedPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private void registerGoogleCloudMessagingInBackground() {
		Log.i("GCM", "Registering..");
		new AsyncTask() {
			@Override
			protected String doInBackground(Object... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					String regId = gcm.register(Configuration.GoogleCloudMessagingSenderID);
					msg = ">>Device registered, registration ID=" + regId;
					storeGoogleCloudMessagingDeviceID(context, regId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

		}.execute(null, null, null);
	}

	private void storeGoogleCloudMessagingDeviceID(Context context, String regId) {
		final SharedPreferences prefs = getSharedPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(GCM_TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_GCM_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private void storeFacebookIDAndName(String facebookID, String name) {
		final SharedPreferences prefs = getSharedPreferences(context);
		int appVersion = getAppVersion(context);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_FACEBOOK_ID, facebookID);
		editor.putString(PROPERTY_FACEBOOK_NAME, name);
		editor.commit();

	}

	public String getFacebookID(Context context) {
		String ret = "";

		final SharedPreferences prefs = getSharedPreferences(context);
		String facebookID = prefs.getString(PROPERTY_FACEBOOK_ID, "");
		if (facebookID.isEmpty()) {
			Log.i(FB_TAG, "Facebook ID not found.");
			return "";
		} else {
			ret = facebookID;
		}

		return ret;
	}

	public String getFacebookName(Context context) {
		String ret = "";

		final SharedPreferences prefs = getSharedPreferences(context);
		String facebookName = prefs.getString(PROPERTY_FACEBOOK_NAME, "");
		if (facebookName.isEmpty()) {
			Log.i(FB_TAG, "Facebook ID not found.");
			return "";
		} else {
			ret = facebookName;
		}

		return ret;
	}

	private String getGoogleCloudMessagingRegistrationID(Context context) {
		final SharedPreferences prefs = getSharedPreferences(context);
		String registrationId = prefs.getString(PROPERTY_GCM_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(GCM_TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(GCM_TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Session.getActiveSession() != null && resultCode == RESULT_OK) {
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		} else {
			Log.e(FB_TAG, "Failed to open session!");
		}				
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(GCM_TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}
}
