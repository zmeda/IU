package iu.server.explore.game;


public class World
{
	public final Integer	id;
	public final String	name;

	// Wolrd location and size (in degrees)
	public final float	latitude;
	public final float  longitude;
	public final float	height;
	public final float	width;

	/** How many tiles in one dimension of a world */
	public final int		dim;
	
	public final int		dimPow;

	/** Size of a tile in degrees */
	public final float	tileSizeInv;
	public final float	fieldSizeInv;

	private final Tile[][]	tiles;


	/**
	 * Creates a new world that is a square <code>pow(2,size)</code> by <code>pow(2,size)</code> large
	 * 
	 * @param lat
	 *           latitude of south edge
	 * @param lon
	 *           longitude of west edge
	 * @param h
	 *           difference between south and north
	 * @param w
	 *           difference between east and west
	 * @param size
	 *           used to calculate number of Tiles in the map
	 *           
	 * NOTE: Height and Width should be equal for this implementation to work correctly
	 */
	public World (Integer id, String name, float lat, float lon, float h, float w, int sizePow)
	{
		super ( );

		this.id = id;
		this.name = name;
		this.latitude = lat;
		this.longitude = lon;
		this.height = h;
		this.width = w;

		this.dimPow = sizePow;
		this.dim = 1 << sizePow;
		this.tiles = new Tile[this.dim][this.dim];

		this.tileSizeInv = this.dim / this.width;
		this.fieldSizeInv = (this.dim * Tile.Dim) / this.width;

		for (short x = 0; x < this.dim; x++)
		{
			for (short y = 0; y < this.dim; y++)
			{
				this.tiles[x][y] = new Tile (x, y);
			}
		}
	}


	/**
	 * Get the tile at specified X and Y
	 * @param x
	 * @param y
	 * @return a Tile or <code>null</code>
	 */
	public Tile tileAt (final int x, final int y)
	{
		// +dim to ensure positive index
		// %dim to ensure index within bounds
		return this.tiles [(x+this.dim) % this.dim] [(y+this.dim) % this.dim];
	}


	public Tile tileAt (final Location location)
	{

		float deltaLon = location.longitude - this.longitude;
		float deltaLat = location.latitude - this.latitude;
		
		int x = (int) (deltaLon * this.tileSizeInv);
		int y = (int) (deltaLat * this.tileSizeInv);

		return this.tileAt (x, y);
	}


	public long fieldAt (final Location location)
	{
		float deltaLat = location.latitude - this.latitude;
		float deltaLon = location.longitude - this.longitude;

		int field_x = ((int) (deltaLon * this.fieldSizeInv)) % 8;
		int field_y = ((int) (deltaLat * this.fieldSizeInv)) % 8;

		return Tile.field ((byte) ((field_y << 3) + field_x + 1));
	}
	

	public Integer getId ( )
	{

		return this.id;
	}


	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );

		sb.append ("Lat:        ");
		sb.append (this.latitude);
		sb.append ("\nLon:        ");
		sb.append (this.longitude);
		sb.append ("\nHeight:     ");
		sb.append (this.height);
		sb.append ("\nWidth:      ");
		sb.append (this.width);
		sb.append ("\nDimensions: ");
		sb.append (this.dim);
		sb.append (" * ");
		sb.append (this.dim);

		for (int y = this.dim-1; y >= 0; y--)
		{
			sb.append ("\n");
			for (int x = 0; x < this.dim; x++)
			{
				sb.append (this.tiles[x][y].toString ( ));
			}
		}
		return sb.toString ( );
	}
}
