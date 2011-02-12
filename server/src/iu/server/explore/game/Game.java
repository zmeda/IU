package iu.server.explore.game;

import iu.server.Log;
import iu.server.battle.BattleListenServer;
import iu.server.explore.RemoteClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Represents the state of the entire game
 */
public class Game
{
	private World																		world;
	private HashMap<Integer, Player>												players;
	private final HashMap<Integer, Flag>										flags;

	// Players by name - for Login and Signin
	private HashMap<String, Player>												playersByName;

	private IExploreDao																dao;

	private Server																		server;

	private final LinkedList<Command>											incoming;
	private final LinkedList<Message>											outgoing;

	final ExploringCircle															viewRange;

	/**
	 * Each tile is mapped to a &lt;Player, long&gt; pair, representing the player that sees the tile, and what
	 * he sees in it.
	 */
	private final transient HashMap<Tile, HashMap<Player, FoggyTile>>	visibility;

	private final transient HashMap<Integer, Battle>						battles;


	public Game (final World world, final HashMap<Integer, Player> players, final HashMap<Integer, Flag> flags)
	{
		super ( );
		this.world = world;
		this.players = players;
		this.flags = flags;

		this.visibility = new HashMap<Tile, HashMap<Player, FoggyTile>> ( );
		this.battles = new HashMap<Integer, Battle> ( );

		this.playersByName = new HashMap<String, Player> ( );

		this.incoming = new LinkedList<Command> ( );
		this.outgoing = new LinkedList<Message> ( );

		this.viewRange = new ExploringCircle (ExploringCircle.Sqrt8, this.world);

		// FIXME I don't like having this around
		this.dao = new HsqlExploreDao (this);

		this.initVisibility ( );
	}


	private void initVisibility ( )
	{
		// Add all tiles from the map to the cache
		for (int x = 0; x < this.world.dim; x++)
		{
			for (int y = 0; y < this.world.dim; y++)
			{
				Tile tile = this.world.tileAt (x, y);
				HashMap<Player, FoggyTile> foggyTiles = new HashMap<Player, FoggyTile> ( );

				for (Player player : this.players.values ( ))
				{
					// Filling the playersByName HashMap
					this.playersByName.put (player.getName ( ), player);

					// Player needs a reference back to the game
					player.setGame (this);

					// Does he see some of the tile?
					if (player.isTileVisible (tile))
					{
						FoggyTile foggyTile = new FoggyTile ( );
						foggyTile.fields = player.area.visibleFields (tile);
						foggyTiles.put (player, foggyTile);
					}
				}

				this.visibility.put (tile, foggyTiles);
			}
		}
	}


	/**
	 * Player by his id
	 * 
	 * @param id
	 * @return Player or <code>null</code>
	 */
	public Player getPlayer (final Integer id)
	{
		return this.players.get (id);
	}


	/**
	 * FIXME seems to return null always
	 * 
	 * @param username
	 * @return
	 */
	public Player getPlayerByName (final String username)
	{
		return this.playersByName.get (username);
	}


	/**
	 * All the players in the game
	 * 
	 * @return iterator of all the players in the game
	 */
	public Iterator<Player> getAllPlayers ( )
	{
		return this.players.values ( ).iterator ( );
	}


	/**
	 * Add a player to the game.
	 * 
	 * @param player
	 */

	// TODO - if this will be used for login then it should react accordingly if the player already exists
	public void putPlayer (final Player player)
	{
		// This player already exists
		if (this.players.containsKey (player) || this.playersByName.containsKey (player.getName ( )))
		{
			return;
		}

		this.players.put (player.id, player);
		this.playersByName.put (player.getName ( ), player);

		for (int x = 0; x < this.world.dim; x++)
		{
			for (int y = 0; y < this.world.dim; y++)
			{
				Tile tile = this.world.tileAt (x, y);
				HashMap<Player, FoggyTile> foggyTiles = new HashMap<Player, FoggyTile> ( );

				if (player.isTileVisible (tile))
				{
					FoggyTile foggyTile = new FoggyTile ( );
					foggyTile.fields = player.area.visibleFields (tile);
					foggyTiles.put (player, foggyTile);
				}
			}
		}
	}


	public Flag getFlag (final Integer id)
	{
		return this.flags.get (id);
	}


	/**
	 * Creates an iterator that goes through all of the flags
	 * 
	 * @return iterator of all of the flags in the world
	 */
	@Deprecated
	public Iterator<Flag> getAllFlags ( )
	{
		return this.flags.values ( ).iterator ( );
	}


	/**
	 * @return the players
	 */
	public Collection<Player> getPlayers ( )
	{
		return this.players.values ( );
	}


	public Battle getBattle (final Integer id)
	{
		return this.battles.get (id);
	}


	public void requestBattleStart (final Battle battle, final List<Message> messages)
	{
		this.battles.put (battle.id, battle);

		// Notify others that I want to attack
		messages.add (EventUnderAttack.init (battle));
	}


	public void startBattle (final Battle battle, final List<Message> messages)
	{
		BattleListenServer battleServer = this.server.createBattleServer (battle);
		battle.setServer (battleServer);
		battleServer.start ( );

		// Notify participants
		messages.add (EventBattleStarted.init (battle));
	}


	public void finishBattle (final Battle battle, final List<Message> messages)
	{
		// this.battles.remove (battle.id);
		this.server.destroyBattleServer (battle.getServer ( ));

		// Notify anyone who sees this battle
		EventBattleFinished message = EventBattleFinished.init (battle);
		messages.add (message);
	}


	/**
	 * Simulation of the game world.
	 * 
	 * @param dt
	 *           in seconds
	 * 
	 * @param messages
	 *           Events that happen during integrate will be stored here
	 * 
	 * @return A list of messages produced as a result of the simulation
	 */
	public void integrate (final double dt)
	{
		// Process flags, so they produce resources
		for (Flag flag : this.flags.values ( ))
		{
			flag.integrate (dt, this.outgoing);
		}

		// Process players to see if others need to be notified about movements
		for (Player me : this.players.values ( ))
		{
			me.integrate(dt, this.outgoing);
			this.updateVisibility (me);
			this.informOthersAboutMove (me, this.outgoing);
			// this.lookForEnemies (me, this.outgoing);
		}

		// Process battles to see if any have finished
		// FIXME - Better to use Iterator<Battle> it = this.battles.values().iterator(); to loop through all of
		// the battles
		Iterator<Integer> battleKeysIterator = this.battles.keySet ( ).iterator ( );
		while (battleKeysIterator.hasNext ( ))
		{
			Battle battle = this.battles.get (battleKeysIterator.next ( ));
			battle.integrate (dt, this.outgoing);

			if (battle.finished)
			{
				// remove finished battles
				battleKeysIterator.remove ( );
			}
		}
	}


	/**
	 * Players who see at least one field in this tile
	 * 
	 * @param tile
	 * @return a Set of players
	 */
	public Set<Player> whoSees (final Tile tile)
	{
		return this.visibility.get (tile).keySet ( );
	}


	/**
	 * Players who see the exact field in the Tile
	 * 
	 * @param tile
	 * @param field
	 * @return a List of players
	 */
	public LinkedList<Player> whoSees (final Tile tile, final long field)
	{
		LinkedList<Player> theySeeField = new LinkedList<Player> ( );
		Set<Player> theySeeTile = this.visibility.get (tile).keySet ( );

		for (Player him : theySeeTile)
		{
			// He sees the tile, but does he see the field?
			if (him.isFieldVisible (tile, field))
			{
				theySeeField.add (him);
			}
		}

		return theySeeField;
	}


	/**
	 * Update what is visible to the player according to his new location
	 * 
	 * @param me
	 *           the Player exploring
	 */
	private void updateVisibility (final Player me)
	{
		// This only happens during initialisation, before the player had moved for the first time
		if (me.tile == null)
		{
			return;
		}

		// All players who see at least some of the tile
		HashMap<Player, FoggyTile> whoSeesTile = this.visibility.get (me.tile);

		// 
		FoggyTile foggyTile = whoSeesTile.get (me);

		// Player does not see the tile at all ...
		if (foggyTile == null)
		{
			foggyTile = new FoggyTile ( );
			// ... well, now he does.
			whoSeesTile.put (me, foggyTile);
		}

		// Is the players current field already visible to him?
		if ((foggyTile.fields & me.field) != me.field)
		{
			foggyTile.fields |= me.field;
		}
	}


	/**
	 * 
	 * Inform everyone who sees me about my movements
	 * 
	 * @param me
	 * @param messages
	 */
	private void informOthersAboutMove (final Player me, final List<Message> messages)
	{
		// Have I moved?
		if (me.moved ( ))
		{
			Set<Player> theySeeMyTile = this.visibility.get (me.tile).keySet ( );
			LinkedList<Player> theyActuallySeeMe = new LinkedList<Player> ( );

			Log.Lucas.v ("Game", "I've moved: " + me + ", they see my tile: " + theySeeMyTile);

			for (Player him : theySeeMyTile)
			{
				// He sees my tile, but does he see my field?
				if (me != him && him.isFieldVisible (me.tile, me.field))
				{
					theyActuallySeeMe.add (him);
				}
			}
			messages.add (EventPlayerMoved.init (me, theyActuallySeeMe));
		}
		me.clearMoved ( );
	}


	/**
	 * Look for enemies around me
	 * 
	 * @param me
	 * @param message
	 *
	private void lookForEnemies (final Player me, final List<Message> messages)
	{

		if (me.tile == null)
		{
			return;
		}

		// TODO If I get a set of players in the same tile it should get a little faster
		Set<Player> theySeeMyTile = this.visibility.get (me.tile).keySet ( );
		for (Player him : theySeeMyTile)
		{
			// He sees my tile, is he in the same field as I am
			if (me != him && me.field == him.field && me.tile.equals (him.tile))
			{
				switch (me.diplomacyStatus (him))
				{
					case Player.Enemy:
						messages.add (EventEnemyNear.init (me, him));
					break;

					case Player.Neutral:
						// TODO
					case Player.Ally:
						// TODO
				}
			}
		}
	}*/

	/**
	 * Just a wrapper around long primitive. Represents a Tile of 64 fields which are either explored or not
	 */
	final class FoggyTile
	{
		public long	fields	= 0L;
	}


	/**
	 * @return the world
	 */
	public World getWorld ( )
	{
		return this.world;
	}


	/**
	 * Schedule a command for later execution
	 * 
	 * @param command
	 */
	public void schedule (final Command command)
	{
		this.incoming.addLast (command);
	}


	/**
	 * Execute all scheduled commands
	 * 
	 * @see Game#schedule(Command)
	 */
	void executeCommands ( )
	{
		while (!this.incoming.isEmpty ( ))
		{
			Command cmd = this.incoming.removeFirst ( );
			cmd.execute (this.outgoing);
			cmd.dispose ( );
		}
	}


	void sendMessages ( )
	{
		while (!this.outgoing.isEmpty ( ))
		{
			Message e = this.outgoing.removeFirst ( );
			e.send ( );
			e.dispose ( );
		}
	}


	public Player logInPlayer (final String name, final String password)
	{
		Player ret = null;

		// Get player by name
		Player player = this.playersByName.get (name);

		// If player not logged in yet and the password is correct then log him in and return the player
		// instance
		if (player != null && !player.isLoggedIn ( ) && player.password.equals (password))
		{
			player.setLoggedIn (true);

			Log.Lucas.v ("Game", "Player (" + player.getName ( ) + ") has just logged in.");

			ret = player;
		}

		return ret;
	}


	public void logOutPlayer (final Player player, final List<Message> messages)
	{
		if (player != null && player.isLoggedIn ( ))
		{
			player.setLoggedIn (false);

			messages.add (EventPlayerLoggedOut.init (player));

			RemoteClient rc = RemoteClient.getClient (player.getName ( ));

			if (rc != null)
			{
				rc.logout ( );
			}
			else
			{
				Log.E ("Server", "Logging out but RemoteClient doesn't exist for this player.");
			}
		}
	}


	public Player signInPlayer (final String name, final String password)
	{
		Player player = null;

		// If player with this name doesn't exist yet then create one else return null
		if (this.playersByName.get (name) == null)
		{
			// player = this.iuDB.insertNewPlayer (name, password, this.world);
			player = this.dao.newPlayer (name, password, this);
			player.setLoggedIn (true);

			// Add the new player to the containers
			this.players.put (player.id, player);
			this.playersByName.put (player.getName ( ), player);
		}

		return player;
	}


	public void finish ( )
	{
		this.dao.disconnect ( );
	}


	public void setServer (final Server server)
	{
		this.server = server;
	}


	public Server getServer ( )
	{
		return this.server;
	}
}
