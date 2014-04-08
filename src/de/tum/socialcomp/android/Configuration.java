package de.tum.socialcomp.android;

/**
 * Simple class that holds all configuration specific information for the game.
 * 
 * @author Niklas Klügel
 *
 */

public class Configuration {
	// URL of the Game Server
	public static String ServerURL = "http://131.159.24.59:9000";
	
	// This is the minimum distance the user should have moved to trigger a location update 
	public static float MinimumDistanceForLocationUpdates = 5f; 
	
	public static double DefaultLongitude = 11.0;
	public static double DefaultLatitude = 48.0;

}
