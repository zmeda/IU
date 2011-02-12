/**
 * 
 */
package iu.android.comm;

import android.location.Location;

/**
 *
 */
public interface ILocationTransform {
	public Location toGame (Location onEarth);
	// public Location toEarth (Location inGame);
}
