package iu.android.comm;

import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.location.LocationManager;

public class LinearTransform implements ILocationTransform
{
	//
	// BattleEngine World translation data
	//

	// Explore distance
	// public static double exploreDistance;

	public static double	exploreDistance	= 0.0001;

	//
	// Where the theatre is located on Earth
	// Both Location and Point object refer to the same geographic location but are here for convenience
	//

	/** location of and game on Earth */
	private final double	minLatitude;
	/** location of and game on Earth */
	private final double	minLongitude;
	/** size of game on Earth */
	private final double	width;
	/** size of game on Earth */
	private final double	height;


	/**
	 * A `Zero` transformation, every location is transformed back to itself
	 */
	public LinearTransform ( )
	{
		this (0, 0, 360, 180);
	}


	public LinearTransform (final GameWorld map)
	{
		this (map.minLocation ( ).getLatitude ( ), map.minLocation ( ).getLongitude ( ), map.height ( ), map.width ( ));
	}


	/**
	 * Creates the linear wrapping transformation from the real world GPS coordinates to the BattleEngine World. The transformation is `Wrapping` in a sense that map edges are connected East to West, and North to South.
	 */
	public LinearTransform (final double minLatitude, final double minLongitude, final double width, final double height)
	{
		this.minLatitude = minLatitude;
		this.minLongitude = minLongitude;
		this.width = width;
		this.height = height;
	}


	public Location toGame (final Location onEarth)
	{
		Location translated = new Location (LocationManager.GPS_PROVIDER);

		translated.setLatitude (LinearTransform.remainder (onEarth.getLatitude ( ) - this.minLatitude, this.height) + this.minLatitude);
		translated.setLongitude (LinearTransform.remainder (onEarth.getLongitude ( ) - this.minLongitude, this.width) + this.minLongitude);
		return translated;

	}


	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );
		sb.append ("Transform(");
		sb.append ("dLat=");
		sb.append (this.minLatitude);
		sb.append (", dLon=");
		sb.append (this.minLongitude);
		sb.append (", h=");
		sb.append (this.height);
		sb.append (", w=");
		sb.append (this.width);
		sb.append (")");

		return sb.toString ( );
	}


	//
	// Translators Location (double)<--> Point (int E6)
	//

	public static GeoPoint toPointE6 (final Location loc)
	{
		return new GeoPoint ((int) (loc.getLatitude ( ) * 1000000.0d), (int) (loc.getLongitude ( ) * 1000000.0d));
	}


	public static Location toLocation (final GeoPoint p)
	{
		Location loc = new Location (LocationManager.GPS_PROVIDER);

		loc.setLongitude (p.getLongitudeE6 ( ) * 0.000001d);
		loc.setLatitude (p.getLatitudeE6 ( ) * 0.000001d);

		return loc;
	}


	// Floating point remainder (Used to wrap the map)
	// sort of modulo division for floating points
	private static double remainder (final double a1, final double a2)
	{
		return (a1 - Math.floor (a1 / a2) * a2);
	}


	// If same coordinates
	public static boolean pointEquals (final GeoPoint p1, final GeoPoint p2)
	{
		if (p1 == p2)
		{
			return true;
		}
		else if (p1 == null || p2 == null)
		{
			return false;
		}
		else
		{
			return (p1.getLongitudeE6 ( ) == p2.getLongitudeE6 ( ) && p1.getLatitudeE6 ( ) == p2.getLatitudeE6 ( ));
		}
	}


	/**
	 * If both are null or both have same latitude and longitude than return true
	 * 
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public static boolean locationEquals (final Location loc1, final Location loc2)
	{

		if (loc1 == loc2)
		{
			return true;
		}
		else if (loc1 == null || loc2 == null)
		{
			return false;
		}
		else
		{
			return (loc1.getLongitude ( ) == loc2.getLongitude ( ) && loc1.getLatitude ( ) == loc2.getLatitude ( ));
		}
	}
}
