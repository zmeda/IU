package iu.server.explore.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of Tiles visible to one player
 */
public class ExploredArea
{
	private HashMap<Integer, ExploredTile>	visible;		// hashCode -> ExploredTitle
	private HashSet<Flag>						cached_flags;


	public ExploredArea ( )
	{
		this.visible = new HashMap<Integer, ExploredTile> ( );
		this.cached_flags = new HashSet<Flag> ( );
	}


	/**
	 * Register this tile as explored
	 * 
	 * @param tile
	 * @return
	 */
	public ExploredTile exploreTile (final Tile tile)
	{

		Integer hash = new Integer (tile.hashCode ( ));
		ExploredTile exploredTile = this.visible.get (hash);

		if (exploredTile == null)
		{
			exploredTile = new ExploredTile (tile);
			this.visible.put (hash, exploredTile);
		}

		return exploredTile;
	}

	
	/**
	 * Explore fields identified by <code>field</code>
	 * Sending notifications of messages to player
	 * 
	 * @param tile
	 * @param field
	 * @param player
	 * @param messages
	 */
	public void exploreField (final Tile tile, final long field, Player player, List<Message> messages)
	{
		ExploredTile exploredTile = this.exploreTile (tile);
		exploredTile.explore (field, player, messages);
	}

	
	/**
	 * Same as above, but no messages are generated
	 * 
	 * @param tile
	 * @param field
	 */
	public void exploreField (final Tile tile, final long field)
	{
		ExploredTile exploredTile = this.exploreTile (tile);
		exploredTile.explore (field);
	}
	

	/**
	 * Is some of the tile visible to user?
	 * 
	 * @param tile
	 * @return
	 */
	public boolean tileVisible (final Tile tile)
	{

		return this.visible.containsKey (new Integer (tile.hashCode ( )));
	}


	/**
	 * Is the field visible to user
	 * 
	 * @param tile
	 * @param field
	 * @return
	 */
	public boolean fieldVisible (final Tile tile, final long field)
	{

		ExploredTile exploredTile = this.visible.get (new Integer (tile.hashCode ( )));
		if (exploredTile != null)
		{
			return exploredTile.explored (field);
		}
		return false;
	}


	/**
	 * Is the flag visible?
	 * 
	 * @param flag
	 * @return
	 */
	public boolean isFlagVisible (final Flag flag)
	{
		if (this.cached_flags.contains (flag))
		{
			return true;
		}

		ExploredTile exploredTile = this.visible.get (new Integer (flag.tile ( ).hashCode ( )));

		if (exploredTile != null)
		{
			if (exploredTile.explored (flag.field ( )))
			{
				this.cached_flags.add (flag);
				return true;
			}
		}

		return false;
	}


	/**
	 * Is the player visible?
	 * 
	 * @param player
	 * @return
	 */
	public boolean isPlayerVisible (final Player player)
	{

		ExploredTile exploredTile = this.visible.get (new Integer (player.tile.hashCode ( )));
		if (exploredTile != null)
		{
			if (exploredTile.explored (player.field))
			{
				return true;
			}
		}

		return false;
	}


	public long visibleFields (final Tile tile)
	{

		ExploredTile exploredTile = this.visible.get (new Integer (tile.hashCode ( )));
		return exploredTile.fields;
	}


	public Iterator<ExploredTile> getExploredTiles ( )
	{
		return this.visible.values ( ).iterator ( );
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ( )
	{
		String str = "Tiles: \n";

		Iterator<ExploredTile> it = this.visible.values ( ).iterator ( );

		while (it.hasNext ( ))
		{
			ExploredTile et = it.next ( );

			str += "(" + et.tile.i + "," + et.tile.j + ")\n" + et.toString ( );
		}

		return str;
	}

}
