package iu.android.comm;

import android.location.Location;

/**
 * 
 * Objects that are meant to sent Location updates to a server, 
 * should implement this interface
 */
public interface ILocationSender
{
	public void sendLocation(Location location);
}
