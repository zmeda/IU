/**
 * 
 */
package iu.database;

import iu.server.explore.game.ExploredArea;
import iu.server.explore.game.Game;
import iu.server.explore.game.Player;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 */
public class IUDatabase
{

	public static final String	jdbcDriver			= "org.hsqldb.jdbcDriver";
	public static final String	dbRoot				= "hsqldb/iu";

	public static final String	defaultWorld		= "Ljubljana";

	// Names of the table holding data for IU on the server side
	public static final String	worldsTableName		= "iu_worlds";
	public static final String	playersTableName	= "iu_players";
	public static final String	fogOfWarTableName	= "iu_fow";
	public static final String	tilesTableName		= "iu_tiles";
	public static final String	flagsTableName		= "iu_flags";

	protected Connection			c						= null;
	protected Statement			st						= null;

	//
	// Loads the HSQLDB drivers for the jdbc
	//
	static
	{
		try
		{
			Class.forName (IUDatabase.jdbcDriver);
		}
		catch (Exception e)
		{
			System.out.println ("ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace ( );
		}

	}


	/**
	 * Creates the a new instance to the database specified with the root IUDatabase.dbRoot
	 */
	public IUDatabase ( )
	{
		// The connection is opened and closed when needed
		// this.openDbConnection ( );
	}


	public void openDbConnection ( )
	{
		try
		{
			this.c = DriverManager.getConnection ("jdbc:hsqldb:file:" + IUDatabase.dbRoot, "sa", "");
			this.st = this.c.createStatement ( );

			System.out.println ("New DB connection opened.");
		}
		catch (SQLException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace ( );
		}
	}


	/**
	 * Closes the connection to the database
	 */
	public void closeDbConnection ( )
	{

		try
		{
			if (this.st != null)
			{
				this.st.close ( );
			}
			if (this.c != null)
			{
				this.c.close ( );
			}

			System.out.println ("Closed DB connection.");
		}
		catch (SQLException e)
		{
			// Probably closed already or hasn't even been connected yet
			// e.printStackTrace ( );
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize ( ) throws Throwable
	{

		this.closeDbConnection ( );
		super.finalize ( );
	}


	public void executeHsqlUpdate (final String sqlQuery)
	{
		try
		{
			this.st.execute (sqlQuery);
		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}
	}


	/**
	 * Reads the player info from the database (used for singing in a player)
	 * 
	 * @param name
	 * @return
	 */
	public Player newPlayer (final String username, final String password, final Game game)
	{
		Player player = null;

		try
		{
			//
			// Insert the player into the database
			//
			String sqlInsert = "\n INSERT INTO " + IUDatabase.playersTableName;
			sqlInsert += "\n (id_world, name, password, color, world_ground_zero_lat, world_ground_zero_lon, game_ground_zero_lat, game_ground_zero_lon, num_hovercraft, num_tank, num_artillery) ";
			sqlInsert += "\n VALUES (" + game.getWorld ( ).getId ( ).intValue ( ) + ", '" + username + "', '"
					+ password + "', " + 0xff00ff00 + "," + 0.0f + ", " + 0.0f + ", " + 0.0f + ", " + 0.0f
					+ ", 12, 5, 2);";

			this.st.execute (sqlInsert);

			//
			// Create a Player instance to return
			//
			String sqlQuery = " SELECT id_player, num_hovercraft, num_tank, num_artillery ";
			sqlQuery += " FROM " + IUDatabase.playersTableName;
			sqlQuery += " WHERE name = '" + username + "' and id_world = "
					+ game.getWorld ( ).getId ( ).intValue ( ) + ";";

			ResultSet rs = this.st.executeQuery (sqlQuery);

			if (rs.next ( ))
			{

				Integer playerId = new Integer (rs.getInt (1));
				short numH = rs.getShort (2);
				short numT = rs.getShort (3);
				short numA = rs.getShort (4);

				player = new Player (playerId, username, password, /* game, */numH, numT, numA,
						new ExploredArea ( ));
			}

			if (rs.next ( ))
			{
				throw new IOException ("Two players with name '" + username + "' in world '"
						+ game.getWorld ( ).name + "' found in database.");
			}

			rs.close ( );
		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}
		catch (IOException ex)
		{
			ex.printStackTrace ( );
		}

		return player;
	}
}
