package de.tum.socialcomp.android.sensor;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * This class is used to dispatch location changed events 
 * to registered objects implementing OnLocationChange interface.
 * This class is registered to track the location based
 * on GPS data as well as NetworkCarrier data, it determines the best approximation 
 * to the location and only updates the registered interfaces if the new
 * approximation is a better one than the previous.
 * 
 * @author Niklas Klügel
 *
 */

public class LocationChangeListener implements LocationListener {

	public static String lattitude = "";
	public static String longitude = "";
	
	private Location currentBestLocation;
	
	private OnLocationChangeInterface locationChangeClient; 
	
	public void setOnLoctationChangedListener(OnLocationChangeInterface client) {
		this.locationChangeClient = client;
	}
	
	@Override
	public void onLocationChanged(Location loc) {
	    String temp_lattitude = String.valueOf(loc.getLatitude());
	    String temp_longitude = String.valueOf(loc.getLongitude());
	    
	    Log.e(this.getClass().getName(), "location changed long/lat: "+loc.getLongitude()+"/"+loc.getLatitude());
	
	    if(!("").equals(temp_lattitude))lattitude = temp_lattitude;
	    if(!("").equals(temp_longitude))longitude = temp_longitude;
	    
	    if(currentBestLocation != null){
	    	// if this location update has a better quality, use it
	    	if(isBetterLocation(loc, this.currentBestLocation)) {
	    		this.currentBestLocation = loc;
	    		
	    		// update listener
	    		if(this.locationChangeClient != null) {
	    			this.locationChangeClient.locationChanged(this.currentBestLocation);
	    		}
	    	}
	    } else {
	    	// init
	    	this.currentBestLocation = loc;
	    }
	    
	    
	}
	
	@Override
	public void onProviderDisabled(String arg0) {
	
	}
	
	@Override
	public void onProviderEnabled(String arg0) {
	
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.e("LOC LISTENER", "status changed "+provider);	
	}
	
	/*
	 * The following code is taken from 
	 * http://developer.android.com/guide/topics/location/strategies.html 
	 */
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
	
	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;
	
	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }
	
	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
	
	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());
	
	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	
	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}


}