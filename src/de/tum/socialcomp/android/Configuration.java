package de.tum.socialcomp.android;

/**
 * Simple class that holds all configuration specific information for the game.
 * 
 * @author Niklas Klügel
 *
 */

public class Configuration {
	// URL of the Game Server
	public static final String ServerURL = "http://131.159.24.59:9000";
	
	// This is the minimum distance the user should have moved to trigger a location update 
	public static final float MinimumDistanceForLocationUpdates = 5f; 
	
	public static final double DefaultLongitude = 11.0;
	public static final double DefaultLatitude = 48.0;
	
	/**
	 * Google Cloud Messaging Sender ID:
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	
	public static final String GoogleCloudMessagingSenderID = "1085161367964";

}
