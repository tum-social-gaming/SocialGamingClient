package de.tum.socialcomp.android.sensor;

import android.location.Location;

/**
 * Simple interface used to register to location-changed-events
 * from the LocationChangeListener class.
 * 
 * @author Niklas Klügel
 *
 */
public interface OnLocationChangeInterface {
	public void locationChanged(Location loc);
}
