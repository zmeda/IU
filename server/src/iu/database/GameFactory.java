/**
 * 
 */
package iu.database;

import iu.server.explore.game.ExploredArea;
import iu.server.explore.game.Flag;
import iu.server.explore.game.Game;
import iu.server.explore.game.Location;
import iu.server.explore.game.Player;
import iu.server.explore.game.Tile;
import iu.server.explore.game.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author xp
 * 
 */
public class GameFactory extends IUDatabase
{

	/**
	 * Returns a Game object wich holds all of the data relevant to the game happening in the world named with
	 * the parameter
	 * 
	 * @param worldName
	 *           name of the world
	 * @return
	 */

	// Singleton class used to create a game for the world specified
	private static GameFactory		gameFactory;									// = new GameFactory ( );

	// All of the games currently running
	private HashMap<String, Game>	games	= new HashMap<String, Game> ( );


	public static GameFactory getInstance ( )
	{
		if (GameFactory.gameFactory == null)
		{
			GameFactory.gameFactory = new GameFactory ( );
		}

		return GameFactory.gameFactory;
	}


	/**
	 * Creates a new Game object holding all of the game specific information needed to run the game
	 * 
	 * @param worldName
	 *           Name of the game world in which the new game will run
	 * @return new Game object for the world specified or an already existing game if it exists. If the world
	 *         specified by the parameter doesn't exist in the Database then a null is returned.
	 */
	public Game createGame (final String worldName)
	{
		// If game already exists for this world then just return
		Game game = this.games.get (worldName);

		// Else create game
		if (game == null)
		{
			this.openDbConnection ( );

			// Get world
			World world = this.getWorld (worldName);

			// If world exists then load game status
			if (world != null)
			{
				HashMap<Integer, Player> players = this.getPlayers (/* game */world);

				HashMap<Integer, Flag> flags = this.getFlags (world, players);

				game = new Game (world, players, flags);

				this.games.put (worldName, game);
			}

			this.closeDbConnection ( );
		}

		return game;
	}


	/**
	 * Gets the world instance by name
	 * 
	 * @param worldName
	 * @return
	 */

	public World getWorld (final String worldName)
	{
		World world = null;

		try
		{
			String sqlQuery = "\n SELECT id_world, min_lat, min_lon, height, width, fow_size ";
			sqlQuery += "\n FROM " + IUDatabase.worldsTableName;
			sqlQuery += "\n WHERE name = '" + worldName + "';";

			ResultSet rs = this.st.executeQuery (sqlQuery);

			if (rs.next ( ))
			{
				Integer id = new Integer (rs.getInt (1));
				float lat = rs.getFloat (2);
				float lon = rs.getFloat (3);
				float height = rs.getFloat (4);
				float width = rs.getFloat (5);
				int fowSizePow = rs.getInt (6);

				world = new World (id, worldName, lat, lon, height, width, fowSizePow);
			}
			else
			{
				throw new SQLException ("ERROR: No world with the name '" + worldName + "' found in database.");
			}

			// If two worlds with same name found
			if (rs.next ( ))
			{
				throw new SQLException ("ERROR: More than one world with the name '" + worldName
						+ "' found in database.");
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}

		return world;
	}


	/**
	 * Get explored area for player
	 * 
	 * @param playerId
	 * @return
	 */
	public ExploredArea getExploredArea (final World world, final Integer playerId)
	{
		ExploredArea exploredArea = null;

		try
		{
			//
			// Get visible tiles
			//
			exploredArea = new ExploredArea ( );

			String sqlQuery = "\n SELECT T.lon_idx, T.lat_idx, FOW.fields ";
			sqlQuery += "\n FROM " + IUDatabase.tilesTableName + " T ";
			sqlQuery += "\n JOIN " + IUDatabase.fogOfWarTableName + " FOW ON FOW.id_tile = T.id_tile ";
			sqlQuery += "\n WHERE FOW.id_player = " + playerId.intValue ( ) + ";";

			ResultSet rs = this.st.executeQuery (sqlQuery);

			int num = 0;
			while (rs.next ( ))
			{
				num++;
				short i = rs.getShort (1);
				short j = rs.getShort (2);
				long fields = rs.getLong (3);

				Tile tile = world.tileAt (i, j);
				exploredArea.exploreField (tile, fields);
			}
			rs.close ( );

			//
			// ExploredArea to be returned
			//

		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}

		return exploredArea;
	}


	/**
	 * Get players data
	 * 
	 * @param world
	 * @return
	 */
	public HashMap<Integer, Player> getPlayers (/* final Game game */final World world)
	{
		HashMap<Integer, Player> players = new HashMap<Integer, Player> ( );

		try
		{
			String sqlQuery = " SELECT id_player, name, password, num_hovercraft, num_tank, num_artillery ";
			sqlQuery += " FROM " + IUDatabase.playersTableName + ";";

			ResultSet rs = this.st.executeQuery (sqlQuery);
			int num = 0;
			while (rs.next ( ))
			{
				num++;
				Integer playerId = new Integer (rs.getInt (1));
				String name = rs.getString (2);
				String password = rs.getString (3);
				short numH = rs.getShort (4);
				short numT = rs.getShort (5);
				short numA = rs.getShort (6);

				ExploredArea exploredArea = this.getExploredArea (/* game.getWorld ( ), */world, playerId);

				Player player = new Player (playerId, name, password, /* game, */numH, numT, numA, exploredArea);

				players.put (playerId, player);
			}

			rs.close ( );
		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}

		return players;
	}


	/**
	 * Returns the flags in a hashmap with their id's as the key
	 * 
	 * @param world
	 *           id of the world
	 * @param players
	 *           hashmap of all players in the world
	 * @return
	 */
	public HashMap<Integer, Flag> getFlags (final World world, final HashMap<Integer, Player> players)
	{
		HashMap<Integer, Flag> flags = new HashMap<Integer, Flag> ( );

		try
		{
			String sqlQuery = "\n SELECT id_flag, id_owner, location_lat, location_lon, num_hovercraft, num_tank, num_artillery ";
			sqlQuery += "\n FROM ";
			sqlQuery += "\n " + IUDatabase.flagsTableName;
			sqlQuery += "\n WHERE id_world = " + world.getId ( ) + ";";

			ResultSet rs = this.st.executeQuery (sqlQuery);

			while (rs.next ( ))
			{
				// Location (float latitude, float longitude)
				Location loc = new Location (rs.getFloat (3), rs.getFloat (4));

				// Get player by id
				Player player = players.get (new Integer (rs.getInt (2)));

				int id = rs.getInt (1);

				// Flag (int id, World world, Player owner, Location location, int hNum, int tNum, int aNum)
				Flag flag = new Flag (id, world, player, loc, rs.getShort (5), rs.getShort (6), rs.getShort (7));

				flags.put (new Integer (id), flag);
			}

		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}

		return flags;
	}
}
