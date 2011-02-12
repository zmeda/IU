package iu.android.comm;

import android.location.Location;

/**
 * 
 * @author luka
 *
 * A buffer between the currently running CommunicationService and its clients. 
 */
public class Communication
{
	/** The one and only instance */
	private static Communication singleton;
	
	private Location location;
	
	public static synchronized Communication instance() {
		if (singleton == null) {
			singleton = new Communication ( );
		}
		return singleton;
	}
	
	private Communication ( ) {
		super ( );
	}

	
	public Location getLocation ( )
	{
		return this.location;
	}

	public void setLocation (Location location)
	{
		this.location = location;
	}	
}
