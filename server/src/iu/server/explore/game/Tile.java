package iu.server.explore.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class Tile
{

	/**
	 * How many Fields there are in a Tile in one dimension (value = 8)
	 * 
	 * This produces 8x8 = 64 fields in one Tile. This can be stored in a single long integer, so don't change
	 * it.
	 */
	public static final int											Dim	= 1 << 3;

	/** Horizontal position of this tile in a map */
	public final short												i;
	/** Vertical position of this tile in a map */
	public final short												j;

	public transient final HashMap<Long, HashSet<Flag>>	flagsPerField;


	public Tile (short k, short l)
	{
		super ( );
		this.i = k;
		this.j = l;

		this.flagsPerField = new HashMap<Long, HashSet<Flag>> ( );
	}


	/**
	 * Add a flag to the field. This is required for the getFlag() methods to work. These methods provide a
	 * quick access to all flags in a tile or in one field, without calculating the tile and field from the
	 * flags location.
	 * 
	 * @param flag
	 */
	public void addFlag (Flag flag)
	{
		if (!this.equals (flag.tile ( )))
		{
			String msg = "Flag: " + flag + " does not belong to tile " + this;
			// XXX throw new IllegalArgumentException (msg);
			System.err.println (msg);
			return;
		}

		HashSet<Flag> flagsInField = this.flagsPerField.get (flag.field ( ));
		if (flagsInField == null)
		{
			flagsInField = new HashSet<Flag> ( );
			this.flagsPerField.put (flag.field ( ), flagsInField);
		}
		flagsInField.add (flag);
	}


	/**
	 * A vector of all flags in this Tile. The vector is not linked to internal collections of the Tile so
	 * modifying the vector (adding new Flags, removing Flags, etc.) will not alter the Tile. Flags in the
	 * vector however are the same instances and modification of their properties will be reflected to the tile
	 * 
	 * @return a Vector of all flags in the field, which may be empty, but never <code>null</code>.
	 */
	public Vector<Flag> getFlags ( )
	{
		Vector<Flag> v = new Vector<Flag> ( );

		for (HashSet<Flag> flagsInTile : this.flagsPerField.values ( ))
		{
			v.addAll (flagsInTile);
		}

		return v;
	}


	/**
	 * All flags that belong to the field with specified mask
	 * 
	 * @param field the mask of the field
	 * @return A set of flags or <code>null</code>.
	 */
	public HashSet<Flag> getFlags (long field)
	{
		return this.flagsPerField.get (field);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ( )
	{
		int a = this.i;
		return (a << 16) + this.j;
	}


	@Override
	public boolean equals (final Object obj)
	{
		if (obj instanceof Tile)
		{
			Tile other = (Tile) obj;
			return (this.i == other.i) && (this.j == other.j);
		}
		return false;
	}


	@Override
	public String toString ( )
	{
		return "[" + this.i + ", " + this.j + "]";
	}

	private static final double	ln2	= Math.log (2);


	public static byte fieldIndex (long f)
	{
		if (f == 0)
			return 0;
		else if (f == (1L << 63))
			return 64;
		else
			return (byte) (Math.log (f) / ln2 + 1);
	}


	public static long field (byte index)
	{
		if (index == 0) {
			return 0L;
		}
		return 1L << (index - 1);
	}
}
