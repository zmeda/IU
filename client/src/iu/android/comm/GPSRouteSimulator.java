package iu.android.comm;

import android.location.Location;
import android.location.LocationManager;

/**
 * 
 * 
 * This class was implemented for testing purposes
 */

public class GPSRouteSimulator
{
	double minLat;
	double minLon;
	double width;
	double height;

	// Start and end of rouute
	Location	locStart	= new Location (LocationManager.GPS_PROVIDER);
	Location	locEnd	= new Location (LocationManager.GPS_PROVIDER);

	long startRouteTime;
	long endRouteTime;
	long pauseStart;

	double	metersPerSecond;


	public GPSRouteSimulator (GameWorld world, double metersPerSecond)
	{
		float tileSize = (float) world.width / (1 << world.tileCountPow);

		this.height = 2.0d * tileSize;
		this.width = 2.0d * tileSize;

		this.minLat = (world.minLocation.getLatitude ( ) + this.height / 2.0d) - tileSize;
		this.minLon = (world.minLocation.getLongitude ( ) + this.width / 2.0d) - tileSize;

		this.metersPerSecond = metersPerSecond;

		// Set end location so we can initialize first route
		this.locEnd.setLatitude (this.minLat + Math.random ( ) * this.height);
		this.locEnd.setLongitude (this.minLon + Math.random ( ) * this.width);

		this.endRouteTime = System.currentTimeMillis ( );

		this.initNewRoute ( );
	}


	public void initNewRoute ( )
	{
		// We continue from where we started
		this.locStart.setLatitude (this.locEnd.getLatitude ( ));
		this.locStart.setLongitude (this.locEnd.getLongitude ( ));
	}


	public Location getCurrentLocation ( )
	{
		long time = System.currentTimeMillis ( );

		if (this.endRouteTime > time)
		{
			this.initNewRoute ( );
		}

		Location loc = new Location (LocationManager.GPS_PROVIDER);

		return loc;
	}


	public void pause ( )
	{
		this.pauseStart = System.currentTimeMillis ( );
	}


	public void resume ( )
	{
		long pauseDuration = System.currentTimeMillis ( ) - this.pauseStart;

		this.startRouteTime += pauseDuration;
		this.endRouteTime += pauseDuration;
	}
}
