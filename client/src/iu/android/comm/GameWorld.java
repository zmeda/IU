package iu.android.comm;

import com.google.android.maps.GeoPoint;
import android.location.Location;


/**
 * A piece of Earth's sphere
 */
public class GameWorld
{
	// public static final GameWorld Globe = new GameWorld(-90,-180,180,360);
	// public static final GameWorld Afrika = new GameWorld(12.196959, 37.209637, 0.02, 0.02);
	// public static final GameWorld SaudiArabia = new GameWorld(16.0, 46.0, 0.02, 0.02);

	public final double		width;
	public final double		height;

	public final Location	minLocation;
	public final GeoPoint	minPoint;
	public final Location	maxLocation;
	public final GeoPoint	maxPoint;

	public final int			tileCountPow;


	public GameWorld (double minLatitude, double minLongitude, double height, double width, int tileCountPow)
	{
		super ( );
		this.width = width;
		this.height = height;

		// Prepare locations
		this.minLocation = new Location ("gps");
		this.minLocation.setLatitude (minLatitude);
		this.minLocation.setLongitude (minLongitude);

		this.maxLocation = new Location ("gps");
		this.maxLocation.setLatitude (minLatitude + height);
		this.maxLocation.setLongitude (minLongitude + width);

		this.minPoint = new GeoPoint ((int) (minLatitude * 1E6d), (int) (minLongitude * 1E6d));
		this.maxPoint = new GeoPoint ((int) ((minLatitude + height) * 1E6d), (int) ((minLongitude + width) * 1E6d));

		this.tileCountPow = tileCountPow;
	}


	public Location minLocation ( )
	{
		return this.minLocation;
	}


	public Location maxLocation ( )
	{
		return this.maxLocation;
	}


	public GeoPoint minPoint ( )
	{
		return this.minPoint;
	}


	public GeoPoint maxPoint ( )
	{
		return this.maxPoint;
	}


	public double height ( )
	{
		return this.height;
	}


	public double width ( )
	{
		return this.width;
	}
}
