package iu.android.explore;

import com.google.android.maps.GeoPoint;
import android.location.Location;
import android.location.LocationManager;

public class CoordinateTranslation
{
	//
	// BattleEngine World translation data
	//

	// Latitude and longitude degrees per BattleEngine World width / height
	static double		mapWidth;
	static double		mapHeight;

	// Position of the map in real world coordinates
	static Location	mapLocation			= null;
	static Location	mapLocation2		= null;
	static GeoPoint	mapPoint				= null;
	static GeoPoint	mapPoint2			= null;

	//
	// Player specific translation data
	//

	// GPS coordinates for Ground Zero of this player (absolute world GPS location)
	Location				zeroWorldLocation	= null;
	GeoPoint				zeroWorldPoint		= null;

	// BattleEngine World position for Ground Zero of this player (Location relative to the mapLocation)
	Location				zeroGameLocation	= null;
	GeoPoint				zeroGamePoint		= null;

	// Explore distance
	static double		exploreDistance;


	/**
	 * Sets up the size of the map used for BattleEngine World in micro-degrees and the position of that map on Google
	 * Maps
	 * 
	 * @param mapDegreeWidth
	 *           - Width in micro-degrees
	 * @param mapDegreeHeight
	 *           - Height in micro-degrees
	 */
	public static void setupMapCoordinates (Location mapLocation, double mapDegreeWidth, double mapDegreeHeight)
	{
		//
		// Select the map to use from the Google Maps
		//

		CoordinateTranslation.mapWidth = mapDegreeWidth;
		CoordinateTranslation.mapHeight = mapDegreeHeight;

		// Bottom left corner of the map
		CoordinateTranslation.mapLocation = mapLocation;

		// Top Right corner of the map
		CoordinateTranslation.mapLocation2 = new Location (LocationManager.GPS_PROVIDER);
		CoordinateTranslation.mapLocation2.setLongitude (mapLocation.getLongitude ( ) + mapDegreeWidth);
		CoordinateTranslation.mapLocation2.setLatitude (mapLocation.getLatitude ( ) + mapDegreeHeight);

		CoordinateTranslation.mapPoint = new GeoPoint ((int) (mapLocation.getLatitude ( ) * 1E6d),
				(int) (mapLocation.getLongitude ( ) * 1E6d));
		CoordinateTranslation.mapPoint2 = new GeoPoint ((int) (CoordinateTranslation.mapLocation2
				.getLatitude ( ) * 1E6d), (int) (CoordinateTranslation.mapLocation2.getLongitude ( ) * 1E6d));

		CoordinateTranslation.exploreDistance = mapDegreeWidth * 1E6d / 128.0d;
	}


	/**
	 * Creates the linear transformation from the real world GPS coordinates to the position in the BattleEngine World
	 * 
	 * @param initialLocation
	 *           - Ground Zero location
	 * @param microW
	 *           - width of the BattleEngine World in micro degrees
	 * @param microH
	 *           - height of the BattleEngine World in micro degrees
	 */

	public CoordinateTranslation (Location zero)
	{
		//
		// Store the ground zero location in the real world GPS coordinates
		//
		this.zeroWorldLocation = zero;
		this.zeroWorldPoint = new GeoPoint ((int) (zero.getLatitude ( ) * 1E6d),
				(int) (zero.getLongitude ( ) * 1E6d));

		//
		// Random Ground Zero location in the Map used for game world explore mode
		//
		double zeroLon = Math.random ( ) * CoordinateTranslation.mapWidth;
		double zeroLat = Math.random ( ) * CoordinateTranslation.mapHeight;

		this.zeroGameLocation = new Location (LocationManager.GPS_PROVIDER);
		this.zeroGameLocation.setLongitude (zeroLon);
		this.zeroGameLocation.setLatitude (zeroLat);

		this.zeroGamePoint = new GeoPoint ((int) (zeroLat * 1E6d), (int) (zeroLon * 1E6d));
	}


	/**
	 * Returns the location translated to the coordinates on the map used for the game world.
	 * 
	 * @param worldLocation
	 */
	public Location getMapLocation (Location worldLocation)
	{
		Location mapLoc = new Location (LocationManager.GPS_PROVIDER);

		// Get difference of this location to Ground Zero location transform to MicroDegrees
		// and modulate the coordinates to BattleEngine Map size
		double mapx = CoordinateTranslation.remainder (worldLocation.getLongitude ( )
				- this.zeroWorldLocation.getLongitude ( ) + this.zeroGameLocation.getLongitude ( ),
				CoordinateTranslation.mapWidth)
				+ CoordinateTranslation.mapLocation.getLongitude ( );
		double mapy = CoordinateTranslation.remainder (worldLocation.getLatitude ( )
				- this.zeroWorldLocation.getLatitude ( ) + this.zeroGameLocation.getLatitude ( ),
				CoordinateTranslation.mapHeight)
				+ CoordinateTranslation.mapLocation.getLatitude ( );

		mapLoc.setLongitude (mapx);
		mapLoc.setLatitude (mapy);

		return mapLoc;
	}


	//
	// Translators Location (double)<--> Point (int E6)
	//

	public static GeoPoint toPointE6 (Location loc)
	{
		return new GeoPoint ((int) (loc.getLatitude ( ) * 1000000.0d), (int) (loc.getLongitude ( ) * 1000000.0d));
	}


	public static Location toLocation (GeoPoint p)
	{
		Location loc = new Location (LocationManager.GPS_PROVIDER);

		loc.setLongitude (p.getLongitudeE6 ( ) * 0.000001d);
		loc.setLatitude (p.getLatitudeE6 ( ) * 0.000001d);

		return loc;
	}


	// Distance between location and flag
	public static double distance (Location loc, Flag flag)
	{
		return loc.distanceTo (flag.location);
	}


	// Floating point remainder (Used to wrap the map)
	public static double remainder (double a1, double a2)
	{
		return (a1 - Math.floor (a1 / a2) * a2);
	}


	// If same coordinates
	public static boolean pointEquals (GeoPoint p1, GeoPoint p2)
	{
		return (p1.getLongitudeE6 ( ) == p2.getLongitudeE6 ( ) && p1.getLatitudeE6 ( ) == p2.getLatitudeE6 ( ));
	}


	// If same location
	public static boolean locationEquals (Location loc1, Location loc2)
	{
		return (loc1.getLongitude ( ) == loc2.getLongitude ( ) && loc1.getLatitude ( ) == loc2.getLatitude ( ));
	}
}
