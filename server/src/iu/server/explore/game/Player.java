package iu.server.explore.game;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import iu.android.explore.Rank;
import iu.server.Log;
import iu.server.explore.RemoteClient;

/**
 * Represents a player in the servers World
 */
public class Player
{

	public static final double	ExperienceExplore		= 5.0;
	public static final double	ExperiencePerSecond	= 0.1;
	public static final double	ExperienceHover		= 5.0;
	public static final double	ExperienceTank			= 20.0;
	public static final double	ExperienceArtillery	= 60.0;

	public static final int		AisID						= 0;
	public static final int		Enemy						= 0;
	public static final int		Neutral					= 1;
	public static final int		Ally						= 2;

	private Game					game;

	public final Integer			id;

	private String					name;
	public final String			password;
	private boolean				loggedIn;

	private transient boolean	moved						= false;

	// Package visibility for faster access
	Location							loc;
	Tile								tile;
	long								field;
	public final ExploredArea	area;

	/** For purposes of calculation whether the player has moved for Delta */
	private Location				lastMovedFrom;

	private short					hover;
	private short					tank;
	private short					artilery;

	private double					experience;
	private Rank					rank;


	public Player (final Integer id, final String name, final String password, final short hover,
			final short tank, final short artilery, final ExploredArea area)
	{
		super ( );
		this.id = id;
		this.name = name;
		this.password = password;
		this.loggedIn = false;
		this.hover = hover;
		this.tank = tank;
		this.artilery = artilery;
		this.area = area;
		this.rank = Rank.PVT;
	}


	public void move (final Location to, final List<Message> messages)
	{
		Tile prevTile = this.tile;
		long prevField = this.field;

		this.loc = to;
		this.tile = this.loc.tile (this.game.getWorld ( ));
		this.field = this.loc.field (this.game.getWorld ( ));

		Log.Lucas.v ("Player", this + " moved to " + this.loc);

		this.moved = (prevTile == null) || (!this.tile.equals (prevTile)) || (this.field != prevField);

		if (this.moved)
		{

			// Simple exploration one Tile at a time
			// this.area.exploreField (this.tile, this.field, this, messages);

			// All new tiles in view range according size of the circle
			LinkedList<ExploredTile> tilesToExplore = this.game.viewRange.findCircle (this.tile, this.field);
			for (ExploredTile t : tilesToExplore)
			{
				if (!this.area.fieldVisible (t.tile, t.fields))
				{
					this.area.exploreField (t.tile, t.fields, this, messages);
					this.experience += ExperienceExplore;
				}
			}
		}
	}


	public short getHover ( )
	{

		return this.hover;
	}


	public void setHover (final short hover)
	{

		this.hover = hover;
	}


	public short getTank ( )
	{

		return this.tank;
	}


	public void setTank (final short tank)
	{

		this.tank = tank;
	}


	public short getArtillery ( )
	{

		return this.artilery;
	}


	public void setArtillery (final short artilery)
	{

		this.artilery = artilery;
	}


	public void integrate (double dt, List<Message> messages)
	{

		this.experience += (dt * ExperiencePerSecond);

		// Check rank
		if (this.experience > this.rank.experience)
		{
			this.rank = Rank.next (this.rank);
			Log.Lucas.i ("Game", this + " was promoted to " + this.rank);

			messages.add (EventPlayerPromoted.init (this));
		}
	}


	/**
	 * Has the player moved to a different field?
	 * 
	 * @return
	 */
	public boolean moved ( )
	{
		return this.moved;
	}


	public void clearMoved ( )
	{
		this.moved = false;
	}


	/**
	 * Has the player moved for more than
	 * 
	 * @param delta
	 * @return
	 */
	public boolean moved (final double delta)
	{

		final double deltaSQ = delta * delta;
		final double dLat = this.lastMovedFrom.latitude - this.loc.latitude;
		final double dLon = this.lastMovedFrom.longitude - this.loc.longitude;
		boolean m = (deltaSQ >= (dLat * dLat + dLon * dLon));

		if (m)
		{
			this.lastMovedFrom = this.loc;
		}

		return m;
	}


	public boolean isFieldVisible (final Tile _tile, final long _field)
	{

		return this.area.fieldVisible (_tile, _field);
	}


	public boolean isTileVisible (final Tile _tile)
	{

		return this.area.tileVisible (_tile);
	}


	/**
	 * @return the world
	 */
	public World getWorld ( )
	{
		return this.game.getWorld ( );
	}


	/**
	 * @return the name
	 */
	public String getName ( )
	{
		return this.name;
	}


	/**
	 * @param name
	 *           the name to set
	 */
	public void setName (final String name)
	{
		this.name = name;
	}


	/**
	 * @return the loggedIn
	 */
	public boolean isLoggedIn ( )
	{
		return this.loggedIn;
	}


	/**
	 * @param loggedIn
	 *           the loggedIn to set
	 */
	public void setLoggedIn (final boolean loggedIn)
	{
		this.loggedIn = loggedIn;
	}


	/**
	 * Notify the client that this Player belongs to about the passed Event
	 * 
	 * @param message
	 *           The Event to be sent to the client
	 */
	public void notify (final Message message)
	{
		byte[] bytes = message.toProtocol ( );

		if (this.loggedIn)
		{
			Log.Lucas.v ("Message", this + " will be notified: " + message + " [" + Arrays.toString (bytes)	+ "]");
			RemoteClient.getClient (this.name).send (bytes);
		}
		else
		{
			Log.Lucas.v ("Server", "RemoteClient for " + this + " is null");
		}
	}


	/**
	 * Instructs the game engine to execute the command on players behalf.
	 * 
	 * @param cmd
	 */
	public void execute (final Command command)
	{
		this.game.schedule (command);
	}


	/**
	 * Get the diplomacy state between this and other player
	 * 
	 * @param other
	 * @return
	 */
	public int diplomacyStatus (final Player other)
	{
		// Every man for him self - for now
		return Player.Enemy;
	}


	public Location getLocation ( )
	{
		return this.loc;
	}


	@Override
	public String toString ( )
	{
		return this.name + " [" + this.id + "]";
	}


	/**
	 * @return the game
	 */
	public Game getGame ( )
	{
		return this.game;
	}


	/**
	 * @param game
	 *           the game to set
	 */
	public void setGame (final Game game)
	{
		this.game = game;
	}


	public Rank getRank ( )
	{
		return rank;
	}


	public boolean isAI ( )
	{
		return this.id == AisID;
	}
}
