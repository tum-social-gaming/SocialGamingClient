package de.tum.socialcomp.android;

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

public class MapSectionFragment extends Fragment {
	private MapView mapView;
	private IMapController mapController;
	private ScaleBarOverlay scaleBarOverlay;
	private MyLocationNewOverlay myLocationOverlay;
	
	// this hash map will be used for long presses on a user on the map to resolve the facebookID
	private HashMap<OverlayItem, String> overlayItemToFacebookIDMap = new HashMap<OverlayItem, String>();
	private Object hashMapMutex = new Object();
	
	ArrayList<OverlayItem> anotherOverlayItemArray;
	
	
	 OnItemGestureListener<OverlayItem> myOnItemGestureListener
	    = new OnItemGestureListener<OverlayItem>(){

	  @Override
	  public boolean onItemLongPress(int arg0, OverlayItem overlay) {
		synchronized (hashMapMutex) {
			String recipentName = overlay.getTitle();
			String recipentFacebookID = overlayItemToFacebookIDMap.get(overlay);
			
			if(recipentFacebookID != null && !recipentFacebookID.isEmpty()) {
				// show the user ialog, sending the messages to the server backend is done there as well
				Dialog pokeDialog = GameDialogs.createUserPokeDialog(getActivity(), MainActivity.getInstance().getFacebookID(getActivity()), recipentName, recipentFacebookID);
				
				pokeDialog.show();
			}			
		}
	   
	   return true;
	  }

	  @Override
	  public boolean onItemSingleTapUp(int index, OverlayItem item) {
	   Toast.makeText(MapSectionFragment.this.getActivity(), 
	     item.getTitle() + "\n"
	     + item.getPoint().getLatitudeE6() + " : " + item.getPoint().getLongitudeE6(), 
	     Toast.LENGTH_LONG).show();
	   return true;
	  }
	     
	    };
	    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

	    View rootView = inflater.inflate(R.layout.fragment_section_map, container, false);       
	    
        
        HttpClientFactory.setFactoryInstance(new IHttpClientFactory() {
            public HttpClient createHttpClient() {
                final DefaultHttpClient client = new DefaultHttpClient();
                client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "OSMDroid");
                return client;
            }
        });
        
        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);        
        mapController = this.mapView.getController();
        
        
        myLocationOverlay = new MyLocationNewOverlay(rootView.getContext(), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        
        // Zoom and move to User's position
        mapController.setZoom(30);
        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                    mapController.animateTo(myLocationOverlay
                            .getMyLocation());
                }
            });
        
        //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        
        myLocationOverlay.setDrawAccuracyEnabled(true);
        myLocationOverlay.setOptionsMenuEnabled(true);
        
        mapView.getOverlays().add(myLocationOverlay);
        
        
        //ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(rootView);
        //mapView.getOverlays().add(scaleBarOverlay);
        
        

     
       /* 
        Location lastMe = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
        int lat = (int) (lastMe.getLatitude() * 1E6); 
        int lng = (int) (lastMe.getLongitude() * 1E6); 
        GeoPoint gpt = new GeoPoint(lat, lng); 
        */
        
        //OverlayItem me = new OverlayItem("MEEEEE", "MEEEEE", gpt ); 
        //me.setMarker(this.getResources().getDrawable(R.drawable.ic_launcher));
        //anotherOverlayItemArray.add(me);
        
        /*ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay 
         = new ItemizedIconOverlay<OverlayItem>(
           this, anotherOverlayItemArray, myOnItemGestureListener);        
        */
        //mapView.getOverlays().add(anotherItemizedIconOverlay);      
        
        
	    rootView.findViewById(R.id.refresh_map_button)
        .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	// request user data from the database - the server should already know 
            	// our position through constant updates by the Location updates
            	
            	GeoPoint loc = myLocationOverlay.getMyLocation();
            	
            	String facebookID = MainActivity.getInstance().getFacebookID(getActivity());
            	
            	HttpGetter request = new HttpGetter();
            	request.execute(new String[]{"users",facebookID, "getNearbyUsers"});
            	
            	
            	try {
            		String requestResult = request.get();
					
					
					// if we just received an empty json, ignore
					if(requestResult.isEmpty() || !requestResult.equals("{ }")){
						
						JSONObject json = new JSONObject(requestResult);
						JSONArray jsonUsers = json.getJSONArray("users");
						
						ArrayList<OverlayItem> userOverlay = new ArrayList<OverlayItem>();
						
						synchronized(hashMapMutex){
						
							for(int idx = 0; idx < jsonUsers.length(); idx++){
								JSONObject jsonUser = jsonUsers.getJSONObject(idx);
								
								String userName = jsonUser.getString("user");
								Double longitude= jsonUser.getDouble("longitude");
								Double latitude = jsonUser.getDouble("latitude");
								String fbID = jsonUser.getString("facebookID");
	
								OverlayItem user = new OverlayItem(userName, userName, new GeoPoint(latitude, longitude));
								
								userOverlay.add(user);
								
								overlayItemToFacebookIDMap.put(user, fbID);
							}
						
						}
						
						ItemizedIconOverlay<OverlayItem> itemizedIconOverlay 
				         = new ItemizedIconOverlay<OverlayItem>(
				           MapSectionFragment.this.getActivity(), userOverlay,myOnItemGestureListener);   
						
						List<Overlay> overlays = mapView.getOverlays();
						overlays.clear();
						
						overlays.add(myLocationOverlay);
						overlays.add(itemizedIconOverlay);
						
						Log.v("SDF", "added overlays");
						
						
						
					}
					
				} catch (Exception e) { // various Exception can be thrown in the process, for brevity we do a 'catch all' 
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

	
	/*
	public void trashMe(View view) {
		EditText et  = (EditText) findViewById(R.id.edit_message);
		
		String s= et.getText().toString();
		
		TextView tv = (TextView) findViewById(R.id.textView1);
		
		tv.setText(s);
		
	}*/

}
