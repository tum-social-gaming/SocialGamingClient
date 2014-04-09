package de.tum.socialcomp.android.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.http.HttpClientFactory;
import org.osmdroid.http.IHttpClientFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import de.tum.socialcomp.android.GameDialogs;
import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.R.id;
import de.tum.socialcomp.android.R.layout;
import de.tum.socialcomp.android.webservices.util.HttpGetter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * This Fragment shows the Map View for the game, it sports a refresh button
 * that requests all users that are nearby from the webservice and shows them on
 * the map.
 * 
 * The map used OSMDroid instead of Google Maps, so we dont have problems with
 * any Google specific Map Service restrictions such as request quota.
 * 
 * Most of the code is directly based on the examples given at:
 * https://code.google
 * .com/p/osmdroid/source/browse/#svn/trunk/OpenStreetMapViewer
 * 
 * There are two additional functions: - When another user is tapped on the map
 * the name is shown. - When performed a tap & hold on the other user he/she can
 * be poked (a dialog will appear).
 * 
 * 
 * @author Niklas Klügel
 * 
 */

public class MapSectionFragment extends Fragment {
	private MapView mapView;
	private IMapController mapController;
	private MyLocationNewOverlay myLocationOverlay;

	// The overlay items are simply the user positions requested from the
	// webservice's database

	// This hash map will be used for long presses on a user on the map to
	// resolve the facebookID
	private HashMap<OverlayItem, String> overlayItemToFacebookIDMap = new HashMap<OverlayItem, String>();
	private Object hashMapMutex = new Object();

	// The gesture listener will be used to react upon tap&hold gestures to show
	// additional info or
	// trigger the dialog for the overlay items.
	OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() {

		// A tap and hold gesture will open a dialog to "poke" the tapped user
		@Override
		public boolean onItemLongPress(int arg0, OverlayItem overlay) {
			synchronized (hashMapMutex) {
				String recipentName = overlay.getTitle();
				String recipentFacebookID = overlayItemToFacebookIDMap
						.get(overlay);

				if (recipentFacebookID != null && !recipentFacebookID.isEmpty()) {
					// show the user ialog, sending the messages to the server
					// backend is done there as well
					Dialog pokeDialog = GameDialogs.createUserPokeDialog(
							getActivity(), MainActivity.getInstance()
									.getFacebookID(getActivity()),
							recipentName, recipentFacebookID);

					pokeDialog.show();
				}
			}

			return true;
		}
		
		// A simple tap gesture will show the user's name
		@Override
		public boolean onItemSingleTapUp(int index, OverlayItem item) {
			Toast.makeText(
					MapSectionFragment.this.getActivity(),
					item.getTitle() + "\n" + item.getPoint().getLatitudeE6()
							+ " : " + item.getPoint().getLongitudeE6(),
					Toast.LENGTH_LONG).show();
			return true;
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_section_map,
				container, false);

		HttpClientFactory.setFactoryInstance(new IHttpClientFactory() {
			// we need to set the HTTP user agent to something 
			// that is not anonymous in order to receive map tiles
			// from the tile server (images of the map).
			//
			// This was implemented at the side of the tile server to 
			// circumvent automated requests.
			
			public HttpClient createHttpClient() {
				final DefaultHttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
						"OSMDroid");
				return client;
			}
		});

		// set up the map
		mapView = (MapView) rootView.findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		mapController = this.mapView.getController();

		// show the users position in an additional map overlay
		myLocationOverlay = new MyLocationNewOverlay(rootView.getContext(),
				mapView);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableFollowLocation();

		// Zoom and move to User's position
		mapController.setZoom(30);
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapController.animateTo(myLocationOverlay.getMyLocation());
			}
		});

		myLocationOverlay.setDrawAccuracyEnabled(true);
		myLocationOverlay.setOptionsMenuEnabled(true);

		mapView.getOverlays().add(myLocationOverlay);

		// This sets up the action triggered by the refresh button in the view
		// we will request users from the webservice as a HTTP GET request and
		// receive a JSON containing all users nearby the user's position.
		// this data will be added to an overlay of the map visualization.
		rootView.findViewById(R.id.refresh_map_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// request user data from the database - the server
						// should already know
						// our position through constant updates by the Location
						// updates

						String facebookID = MainActivity.getInstance()
								.getFacebookID(getActivity());

						HttpGetter request = new HttpGetter();
						request.execute(new String[] { "users", facebookID,
								"getNearbyUsers" });

						try {
							String requestResult = request.get();

							// if we just received an empty json, ignore
							if (requestResult.isEmpty()
									|| !requestResult.equals("{ }")) {

								JSONObject json = new JSONObject(requestResult);
								JSONArray jsonUsers = json
										.getJSONArray("users");

								ArrayList<OverlayItem> userOverlay = new ArrayList<OverlayItem>();

								synchronized (hashMapMutex) {

									for (int idx = 0; idx < jsonUsers.length(); idx++) {
										JSONObject jsonUser = jsonUsers
												.getJSONObject(idx);

										String userName = jsonUser
												.getString("user");
										Double longitude = jsonUser
												.getDouble("longitude");
										Double latitude = jsonUser
												.getDouble("latitude");
										String fbID = jsonUser
												.getString("facebookID");

										OverlayItem user = new OverlayItem(
												userName, userName,
												new GeoPoint(latitude,
														longitude));

										userOverlay.add(user);

										overlayItemToFacebookIDMap.put(user,
												fbID);
									}

								}

								ItemizedIconOverlay<OverlayItem> itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(
										MapSectionFragment.this.getActivity(),
										userOverlay, myOnItemGestureListener);

								List<Overlay> overlays = mapView.getOverlays();
								overlays.clear();

								overlays.add(myLocationOverlay);
								overlays.add(itemizedIconOverlay);

								Log.v(this.getClass().getName(),
										"added overlays");

							}

						} catch (Exception e) { // various Exceptions can be
												// thrown in the process, for
												// brevity we do a 'catch all'
							Log.e("Map", e.getMessage());
						}

					}
				});

		return rootView;
	}

	@Override
	public void onPause() {
		myLocationOverlay.disableFollowLocation();
		super.onPause();
	}

	@Override
	public void onResume() {
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableFollowLocation();
		super.onResume();
	}

}
