package iu.android.explore;

import iu.android.comm.GameWorld;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.graphics.Rect;
import android.location.Location;

import com.google.android.maps.GeoPoint;

/**
 * 
 * @author luka
 * 
 *         TODO - size shouldn't be final
 */
public class FogOfWar
{
	// This is constant to use the 64 bits of a single long integer (tile is the size 8x8)
	// DO NOT CHANGE THIS!!!
	public static final int	TILE_SIZE_POW		= 3;

	// Size of a single tile
	public static final int	TILE_SIZE			= 1 << FogOfWar.TILE_SIZE_POW;

	// Mask for isolation of x and y inside of a single tile
	static final int			TILE_FIELD_MASK	= FogOfWar.TILE_SIZE - 1;

	// All zeros (All fields are unexplored)
	static final long			TILE_FOGGY			= 0L;

	// All ones (All fields are explored)
	static final long			TILE_CLEARED		= 0xFFFFFFFFFFFFFFFFL;

	// 2^TILE_COUNT_POW == number of tiles in a row or column of the map
	public final int			MAP_TILE_COUNT_POW;

	// Number of tiles in width on the whole map
	public final int			MAP_TILE_COUNT;

	// Number of fields for the whole map (number of tiles * number of fields per tile)
	public final int			MAP_FILED_COUNT_POW;

	// Number of fields of the whole map in one dimension
	public final int			MAP_FIELD_COUNT;

	// 1D array for a 2D map of tiles the size of 8x8 fields
	private final long[][]	fowMap;

	// Bounding box of non foggy tiles
	int							minx;
	// Bounding box of non foggy tiles
	int							miny;
	// Bounding box of non foggy tiles
	int							maxx;
	// Bounding box of non foggy tiles
	int							maxy;

	// Location of the map that is to have the fog of war applied
	Location						mapLocation;
	GeoPoint						mapPoint;

	float							width;
	float							height;
	int							widthE6;
	int							heightE6;
	float							fieldSize;
	float							fieldSizeInv;
	float							tileSize;
	float							tileSizeInv;

	// An array of flags for every tile on the map (Drawing optimization)
	ArrayList<Flag>			tileFlags[][]		= null;


	//

	public FogOfWar (final GameWorld gameWorld)
	{
		this (gameWorld.minLocation ( ), (float) gameWorld.width ( ), (float) gameWorld.height ( ),
				gameWorld.tileCountPow);
	}


	/**
	 * Creates the fog of war for a map on the location and the size specified
	 * 
	 * @param location
	 *           - Map location on the world map
	 * @param sizeX
	 *           - Width of the map in degrees
	 * @param sizeY
	 *           - Height of the map in degrees
	 */
	@SuppressWarnings("unchecked")
	public FogOfWar (final Location location, final float sizeX, final float sizeY, final int tileCountPower)
	{
		this.mapLocation = location;
		this.width = sizeX;
		this.height = sizeY;

		this.mapPoint = new GeoPoint ((int) (this.mapLocation.getLatitude ( ) * 1E6d), 
														 (int) (this.mapLocation.getLongitude ( ) * 1E6d));
		this.widthE6 = (int) (sizeX * 1E6d);
		this.heightE6 = (int) (sizeY * 1E6d);

		// 2^TILE_COUNT_POW == number of tiles in a row or column of the map
		this.MAP_TILE_COUNT_POW = tileCountPower;

		// Number of tiles in width on the whole map
		this.MAP_TILE_COUNT = 1 << this.MAP_TILE_COUNT_POW;

		// Number of fields for the whole map (number of tiles * number of fields per tile)
		this.MAP_FILED_COUNT_POW = FogOfWar.TILE_SIZE_POW + this.MAP_TILE_COUNT_POW;

		// Number of fields of the whole map in one dimension
		this.MAP_FIELD_COUNT = 1 << this.MAP_FILED_COUNT_POW;

		// Constants for calculation optimization
		this.fieldSize = this.width / this.MAP_FIELD_COUNT;
		this.fieldSizeInv = this.MAP_FIELD_COUNT / this.width;
		this.tileSize = this.width / this.MAP_TILE_COUNT;
		this.tileSizeInv = this.MAP_TILE_COUNT / this.width;

		// 1D array for a 2D map of tiles the size of 8x8 fields
		this.fowMap = new long[this.MAP_TILE_COUNT][this.MAP_TILE_COUNT];

		// All fields are foggy
		for (int i = 0; i < this.MAP_TILE_COUNT; i++)
		{
			for (int j = 0; j < this.MAP_TILE_COUNT; j++)
			{
				this.fowMap[i][j] = FogOfWar.TILE_FOGGY;
			}
		}

		//
		// Initialize the tile flag arrays
		//

		int[] dims = {this.MAP_TILE_COUNT, this.MAP_TILE_COUNT};

		this.tileFlags = (ArrayList<Flag>[][]) Array.newInstance (ArrayList.class, dims);

		for (int i = 0; i < this.MAP_TILE_COUNT; i++)
		{
			this.tileFlags[i] = null;
		}

		this.minx = this.MAP_TILE_COUNT;
		this.miny = this.MAP_TILE_COUNT;
		this.maxx = -1;
		this.maxy = -1;
	}


	public void initTile (final int tx, final int ty, final long tile)
	{
		this.fowMap[ty][tx] = tile;

		this.minx = (this.minx < tx ? this.minx : tx);
		this.miny = (this.miny < ty ? this.miny : ty);

		this.maxx = (this.maxx > tx ? this.maxx : tx);
		this.maxy = (this.maxy > ty ? this.maxy : ty);
	}


	/**
	 * Clear a field
	 * 
	 * @param tx
	 *           tile.x
	 * @param ty
	 *           tile.y
	 * 
	 * @param index
	 *           significance of the field bit in the long mask <code>(0..64)</code>
	 */
	public void clearField (final short tx, final short ty, final byte index)
	{
		long field;
		if (index == 0)
		{
			// this should not happen
			field = 0L;
		}
		else
		{
			field = 1L << (index - 1);
		}

		this.fowMap[ty][tx] |= field;

		this.minx = (this.minx < tx ? this.minx : tx);
		this.miny = (this.miny < ty ? this.miny : ty);

		this.maxx = (this.maxx > tx ? this.maxx : tx);
		this.maxy = (this.maxy > ty ? this.maxy : ty);
	}


	/**
	 * Returns true if the field on the coordinates specified has been explored
	 * 
	 * @param x
	 *           coordinate of the field
	 * @param y
	 *           coordinate of the field
	 * @return status of the field (explored / foggy)
	 */
	public boolean isCleared (final int x, final int y)
	{
		try
		{
			int fogX = (y >> FogOfWar.TILE_SIZE_POW) % this.MAP_TILE_COUNT;
			int fogY = (x >> FogOfWar.TILE_SIZE_POW) % this.MAP_TILE_COUNT;
			long fog = this.fowMap[fogX][fogY];
			
			return (fog & (1L << (((y & FogOfWar.TILE_FIELD_MASK) << FogOfWar.TILE_SIZE_POW) + (x & FogOfWar.TILE_FIELD_MASK)))) != 0;
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			//
			// The coordinates received as parameter are out of our fog of war map.
			// We will assume that everything is under the FOW even if it is outside it.
			//
			return (false);
		}
	}


	/**
	 * Returns true if the field on the coordinates specified has been explored
	 * 
	 * @param tx
	 *           coordinate of the tile
	 * @param ty
	 *           coordinate of the tile
	 * @param x
	 *           coordinate of the field in the tile specified with tx and ty
	 * @param y
	 *           coordinate of the field in the tile specified with tx and ty
	 * @return status of the field (explored / foggy)
	 */
	public boolean isCleared (final int tx, final int ty, final int px, final int py)
	{
		return ((this.fowMap[ty % this.MAP_TILE_COUNT][tx % this.MAP_TILE_COUNT]) & (1L << (((py & FogOfWar.TILE_FIELD_MASK) << FogOfWar.TILE_SIZE_POW) + (px & FogOfWar.TILE_FIELD_MASK)))) != 0;
	}


	/**
	 * Returns true if the tile if foggy (completely unexplored)
	 * 
	 * @param tx
	 *           tile coordinate
	 * @param ty
	 *           tile coordinate
	 * @return true if the tile if foggy (completely unexplored)
	 */
	public boolean isTileFoggy (final int tx, final int ty)
	{
		return this.fowMap[ty % this.MAP_TILE_COUNT][tx % this.MAP_TILE_COUNT] == FogOfWar.TILE_FOGGY;
	}


	/**
	 * Returns true if the tile if cleared (completely explored)
	 * 
	 * @param tx
	 *           tile coordinate
	 * @param ty
	 *           tile coordinate
	 * @return true if the tile if cleared (completely explored)
	 */
	public boolean isTileCleared (final int tx, final int ty)
	{
		return this.fowMap[ty % this.MAP_TILE_COUNT][tx % this.MAP_TILE_COUNT] == FogOfWar.TILE_CLEARED;
	}


	/**
	 * Returns true if the tile is only partially explored/cleared
	 * 
	 * @param tx
	 *           tile coordinate
	 * @param ty
	 *           tile coordinate
	 * @return true if the tile is only partially explored/cleared
	 */
	public boolean isTilePartial (final int tx, final int ty)
	{
		long tileState = this.fowMap[ty % this.MAP_TILE_COUNT][tx % this.MAP_TILE_COUNT];

		return (tileState != FogOfWar.TILE_CLEARED) && (tileState != FogOfWar.TILE_FOGGY);
	}


	//
	//
	//
	// Location based functions
	//
	//
	//

	/**
	 * Adds a Flag to the FOW map
	 */
	@SuppressWarnings("unchecked")
	public void addFlag (final Flag flag)
	{
		// Get location in map
		double y = flag.location.getLatitude ( ) - this.mapLocation.getLatitude ( );
		double x = flag.location.getLongitude ( ) - this.mapLocation.getLongitude ( );

		int fj = (int) (y * this.fieldSizeInv);
		int fi = (int) (x * this.fieldSizeInv);

		int tj = fj >> FogOfWar.TILE_SIZE_POW;
		int ti = fi >> FogOfWar.TILE_SIZE_POW;

		// Check flag containers for flags on fog of war
		if (this.tileFlags[tj] == null)
		{
			// Create row of containers
			this.tileFlags[tj] = (ArrayList<Flag>[]) Array.newInstance (ArrayList.class, this.tileFlags.length);

			// And the container
			this.tileFlags[tj][ti] = new ArrayList<Flag> ( );
		}
		else if (this.tileFlags[tj][ti] == null)
		{
			// Create new flag container
			this.tileFlags[tj][ti] = new ArrayList<Flag> ( );
		}

		this.tileFlags[tj][ti].add (flag);

		fj = (int) (y * this.fieldSizeInv);
		fi = (int) (x * this.fieldSizeInv);

		flag.setFieldIdx (fi, fj);
	}


	/**
	 * Returns an ArrayList of flags on the tile with the specified indexes
	 * 
	 * @param i
	 *           index of the tile
	 * @param j
	 *           index of the tile
	 * @return an ArrayList of flags on the tile with the specified indexes
	 */
	public ArrayList<Flag> getTileFlags (final int i, final int j)
	{
		if (this.tileFlags[j] == null)
		{
			return null;
		}

		return this.tileFlags[j][i];
	}


	public boolean isRowExplored (final int tj)
	{
		return (this.tileFlags[tj] != null);
	}


	/**
	 * Returns the I and J indexes in the 'int coord[2]'
	 * 
	 * @param loc
	 *           - Location on the map
	 * @param coord
	 *           - the I and J indexes are stored in the array the size of 2
	 */
	public void getTileIndexes (final Location loc, final int[] coord)
	{
		double y = loc.getLatitude ( ) - this.mapLocation.getLatitude ( );
		double x = loc.getLongitude ( ) - this.mapLocation.getLongitude ( );

		coord[0] = (int) (x / this.width * this.MAP_TILE_COUNT);
		coord[1] = (int) (y / this.height * this.MAP_TILE_COUNT);
	}


	/**
	 * Returns the I and J indexes in the 'int coord[2]'
	 * 
	 * @param p
	 *           - Point on the map
	 * @param coord
	 *           - the I and J indexes are stored in the array the size of 2
	 */
	public void getTileIndexes (final GeoPoint p, final int[] coord)
	{
		int x = p.getLongitudeE6 ( ) - this.mapPoint.getLongitudeE6 ( );
		int y = p.getLatitudeE6 ( ) - this.mapPoint.getLatitudeE6 ( );

		coord[0] = (x * this.MAP_TILE_COUNT / this.widthE6);
		coord[1] = (y * this.MAP_TILE_COUNT / this.heightE6);
	}


	/**
	 * Explores the field on which the specified location is on the map
	 * 
	 * @param loc
	 *           Location on the map
	 */
	public boolean isExplored (final Location loc)
	{
		double y = loc.getLatitude ( ) - this.mapLocation.getLatitude ( );
		double x = loc.getLongitude ( ) - this.mapLocation.getLongitude ( );

		int j = (int) (y / this.height * this.MAP_FIELD_COUNT);
		int i = (int) (x / this.width * this.MAP_FIELD_COUNT);

		return this.isCleared (i, j);
	}


	/**
	 * Explores the field on which the specified point is on the map
	 * 
	 * @param p
	 *           Point on the map
	 */
	public boolean isExplored (final GeoPoint p)
	{
		int y = p.getLatitudeE6 ( ) - this.mapPoint.getLatitudeE6 ( );
		int x = p.getLongitudeE6 ( ) - this.mapPoint.getLongitudeE6 ( );

		int j = (y * this.MAP_FIELD_COUNT / this.heightE6);
		int i = (x * this.MAP_FIELD_COUNT / this.widthE6);

		return this.isCleared (i, j);
	}


	/**
	 * Returns the bounding index rectangle of the tiles that have already been partially explored
	 * 
	 * @param p1
	 *           Bottom / left Point on the map
	 * @param p2
	 *           Top / Right Point on the map
	 * @param indexBounds
	 *           A bounding box of indexes of tiles that are present between p1 and p2
	 */

	public void getScreenBoundsTiles (final GeoPoint p1, final GeoPoint p2, final Rect indexBounds)
	{
		int x, y;

		//
		// Bottom / Left
		//
		x = (p1.getLongitudeE6 ( ) - this.mapPoint.getLongitudeE6 ( )) * this.MAP_TILE_COUNT / this.widthE6;
		y = (p1.getLatitudeE6 ( ) - this.mapPoint.getLatitudeE6 ( )) * this.MAP_TILE_COUNT / this.heightE6;

		// Add one top row for correct drawing of the fog
		y = (y > 0 ? y - 1 : 0);

		x = (x < this.minx ? this.minx : (x > this.maxx ? this.maxx : x));
		y = (y < this.miny ? this.miny : (y > this.maxy ? this.maxy : y));

		indexBounds.left = (x > 0 ? x : 0);
		indexBounds.bottom = (y > 0 ? y : 0);

		//
		// Top / Right
		//
		x = (p2.getLongitudeE6 ( ) - this.mapPoint.getLongitudeE6 ( )) * this.MAP_TILE_COUNT / this.widthE6;
		y = (p2.getLatitudeE6 ( ) - this.mapPoint.getLatitudeE6 ( )) * this.MAP_TILE_COUNT / this.heightE6;

		// Add one bottom row for correct drawing of the fog
		y = (y < this.MAP_TILE_COUNT - 1 ? y + 1 : this.MAP_TILE_COUNT - 1);

		x = (x < this.minx ? this.minx : (x > this.maxx ? this.maxx : x));
		y = (y < this.miny ? this.miny : (y > this.maxy ? this.maxy : y));

		indexBounds.right = (x > 0 ? x : 0);
		indexBounds.top = (y > 0 ? y : 0);
	}
}
