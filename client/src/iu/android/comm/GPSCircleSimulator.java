package iu.android.comm;

import android.location.Location;
import android.location.LocationManager;

/**
 * 
 * 
 * This class was implemented for testing purposes
 */

public class GPSCircleSimulator
{
	private float lat;
	private float lon;
	private float radius;
	private float angleSpeed;
	private float angle;
	private float direction;
	private long		previousTime;
	private boolean	pause;


	public GPSCircleSimulator (final GameWorld world, final float rpm)
	{
		// Two tiles radius
		this.radius = 2.0f * (float) (world.width / (1 << world.tileCountPow));

		// Center of game world
		this.lat = (float) (world.minLocation.getLatitude ( ) + world.height / 2.0d);
		this.lon = (float) (world.minLocation.getLongitude ( ) + world.width / 2.0d);

		// Sped of the angle change
		this.angleSpeed = (float) (rpm / 60000.0d * 2 * Math.PI);

		// Little random
		// this.radius *= (float)(0.4d * Math.random ( ) +0.4d);
		// this.lat += (float)Math.random () * this.radius;
		// this.lon += (float) Math.random () * this.radius;

		this.previousTime = System.currentTimeMillis ( );
		this.angle = 0;
		this.direction = 1.0f;
		this.pause = true;
	}


	public GPSCircleSimulator (final float gameLat, final float gameLon, final float radius, final float rpm)
	{
		// this.lat = gameLat;
		// this.lon = gameLon;
		// this.radius = radius;
		// this.angleSpeed = (float) (rpm / 60000.0d * 2 * Math.PI);

		// Two tiles radius
		this.radius = radius;

		// Center of game world
		this.lat = gameLat;
		this.lon = gameLon;

		// Sped of the angle change
		this.angleSpeed = (float) (rpm / 60000.0d * 2 * Math.PI);

		this.previousTime = System.currentTimeMillis ( );
		this.angle = 0;
		this.direction = 1.0f;
		this.pause = true;

	}


	/**
	 * @return The current simulated GPS location
	 */
	public Location getCurrentLocation ( )
	{
		if (!this.pause)
		{
			long time = System.currentTimeMillis ( );
			this.angle += (time - this.previousTime) * (this.direction * this.angleSpeed);
			this.previousTime = time;
		}

		float dLat = (float) (Math.sin (3.0f * this.angle) * this.radius);
		float dLon = (float) (Math.cos (7.0f * this.angle) * this.radius);

		// dLat += (float) (Math.sin (alfa * 6.0f) * this.radius * 0.4f);
		// dLon += (float) (Math.cos (alfa * 6.0f) * this.radius * 0.4f);

		Location loc = new Location (LocationManager.GPS_PROVIDER);
		loc.setLatitude (this.lat + dLat);
		loc.setLongitude (this.lon + dLon);

		return loc;
	}


	/**
	 * Move backwards
	 */
	public void reverseDirection ( )
	{
		this.direction *= -1;
	}


	/**
	 * Pause the movement
	 */
	public void togglePause ( )
	{
		this.pause = !this.pause;

		if (!this.pause)
		{
			this.previousTime = System.currentTimeMillis ( );
		}
	}


	/**
	 * @return the pause
	 */
	public boolean isPause ( )
	{
		return this.pause;
	}
}
