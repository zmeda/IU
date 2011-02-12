package iu.server.explore.game;

import java.util.HashSet;
import java.util.List;

/**
 * Represents a tile of the map that is made of a 8x8 grid of fields.
 * 
 * <pre>
 *  7XXXXXXXX
 *  6XXXX  XX
 *  5XX     X
 *  4XXXX  XX
 *  3XXXXXXXX
 *  2XXXXXXXX
 *  1XXXXXXXX
 *  0XXXXXXXX
 *   01234567
 * </pre>
 * 
 * Each field of the tile is either explored or unexplored. Internally stored in a long, each bit representing
 * state of one field
 * 
 * This will only work on a platform where Long.SIZE is at least 64
 * 
 */
public class ExploredTile /* extends Tile */
{
	Tile	tile;
	long	fields;


	public ExploredTile (final Tile t)
	{
		this.tile = t;
		this.fields = 0L;
	}


	public ExploredTile (final Tile t, final long f)
	{
		this.tile = t;
		this.fields = f;
	}

	
	public short i () {
		return this.tile.i;
	}
	
	public short j () {
		return this.tile.j;
	}
	

	/**
	 * Explore the field of a tile at positions (k, l)<br>
	 * Example: <code>explore(5, 4);</code> uncovers this field
	 * 
	 * <pre>
	 *  7XXXXXXXX
	 *  6XXXXXXXX
	 *  5XXXXXXXX
	 *  4XXXXX XX
	 *  3XXXXXXXX
	 *  2XXXXXXXX
	 *  1XXXXXXXX
	 *  0XXXXXXXX
	 *   01234567
	 * </pre>
	 * 
	 * @param x
	 *           horizontal index, between 0 and 7
	 * @param y
	 *           vertical index, between 0 and 7
	 */
	public void explore (final short x, final short y)
	{

		if (x < 0 || x > 7 || y < 0 || y > 7)
		{
			return;
		}

		// Explore the field by setting its respective bit
		this.fields |= ExploredTile.fieldMask (x, y);
	}


	/**
	 * Explore fields identified by <code>field</code>
	 * 
	 * @param mask
	 * @param player
	 * @param messages
	 *           may be <code>null</code>
	 */
	public void explore (final long mask, Player player, List<Message> messages)
	{
		if ((this.fields & mask) != mask)
		{
			this.explore (mask);
			messages.add (EventExploration.init (this.i ( ), this.j(), mask, player));

			HashSet<Flag> flagsInField = this.tile.flagsPerField.get (mask);
			if (flagsInField != null)
			{
				for (Flag flag : flagsInField)
				{
					if (messages != null)
					{
						messages.add (EventFlagDiscovered.init (flag, player));
						if (player == flag.owner) {
							messages.add(DataFlagUnits.init(flag, player));
						}
					}
				}
			}
		}
	}


	public void explore (final long mask)
	{
		this.fields |= mask;
	}


	/**
	 * Is entire Tile explored?
	 * 
	 * @return
	 */
	public boolean explored ( )
	{

		// -1L is all ones
		return this.fields == -1L;
	}


	/**
	 * Is entire Tile hidden?
	 * 
	 * @return
	 */
	public boolean hidden ( )
	{

		// 0L is all zeros
		return this.fields == 0L;
	}


	/**
	 * Is field at (k, l) explored?
	 * 
	 * @param k
	 * @param l
	 * @return
	 */
	public boolean explored (final short x, final short y)
	{

		if (x < 0 || x > 7 || y < 0 || y > 7)
		{
			return false;
		}
		// Explore the field by setting its respective bit
		return this.explored (ExploredTile.fieldMask (x, y));
	}


	public boolean explored (final long fieldMask)
	{
		return (this.fields & fieldMask) == fieldMask;
	}


	private static long fieldMask (final short x, final short y)
	{
		int index = (y << 3) + x + 1;
		return Tile.field ((byte)index);
	}


	@Override
	public String toString ( )
	{

		StringBuilder sb = new StringBuilder ( );
		for (short y = 7; y >= 0; y--)
		{
			for (short x = 0; x < 8; x++)
			{
				sb.append (this.explored (x, y) ? ' ' : 'X');
			}
			sb.append ('\n');
		}
		sb.append ('\n');
		return sb.toString ( );
	}


	/**
	 * @return the fields
	 */
	public long getFields ( )
	{
		return this.fields;
	}
	
	
	@Override
	public boolean equals (Object obj)
	{
		if (obj instanceof ExploredTile)
		{
			ExploredTile other = (ExploredTile) obj;
			return this.tile.equals (other.tile) && this.fields == other.fields;
		}
		else {
			return false;
		}
	}
}
