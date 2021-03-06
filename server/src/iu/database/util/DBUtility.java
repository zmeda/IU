package iu.database.util;

import iu.database.IUDatabase;
import iu.server.explore.game.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtility extends IUDatabase
{
	protected World	world	= null;


	/**
	 * Creates a random database with a world 10 players and 50000 flags
	 */
	public void createRandomDatabase (final World w, final int openedArea, final boolean truncateDatabase)
	{
		// float centerLat = 46.0f + 3.0f / 60.0f;
		// float centerLon = 14.0f + 31.0f / 60.0f;
		// float size = 0.2f;
		//
		// this.world = new World (new Integer (1), "Ljubljana", centerLat + size / 2, centerLon - size / 2,
		// size,
		// size, fowTileCountPow);

		this.world = w;

		String sqlQuery;

		try
		{
			float lon, lat;

			//
			// Drop old data if necessary
			//

			if (truncateDatabase)
			{
				try
				{
					this.st.execute (" DROP TABLE " + IUDatabase.flagsTableName + "; ");
				}
				catch (SQLException e)
				{
					System.out.println ("Cant drop " + IUDatabase.flagsTableName);
				}

				try
				{
					this.st.execute (" DROP TABLE " + IUDatabase.fogOfWarTableName + "; ");
				}
				catch (SQLException e)
				{
					System.out.println ("Cant drop " + IUDatabase.fogOfWarTableName);
				}

				try
				{
					this.st.execute (" DROP TABLE " + IUDatabase.tilesTableName + "; ");
				}
				catch (SQLException e)
				{
					System.out.println ("Cant drop " + IUDatabase.tilesTableName);
				}

				try
				{
					this.st.execute (" DROP TABLE " + IUDatabase.playersTableName + "; ");
				}
				catch (SQLException e)
				{
					System.out.println ("Cant drop " + IUDatabase.playersTableName);
				}

				try
				{
					this.st.execute (" DROP TABLE " + IUDatabase.worldsTableName + "; ");
				}
				catch (SQLException e)
				{
					System.out.println ("Cant drop " + IUDatabase.worldsTableName);
				}

				//
				// Make sequences and tables
				//

				// Worlds table
				sqlQuery = "\n CREATE TABLE " + IUDatabase.worldsTableName;
				sqlQuery += "\n (id_world INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1) NOT NULL PRIMARY KEY, ";
				sqlQuery += "\n name VARCHAR NOT NULL, min_lat FLOAT, min_lon FLOAT, height FLOAT, width FLOAT, fow_size INTEGER); ";

				this.st.execute (sqlQuery);

				// Tiles for each world
				sqlQuery = "\n CREATE TABLE " + IUDatabase.tilesTableName;
				sqlQuery += "\n (id_tile INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0, INCREMENT BY 1) NOT NULL PRIMARY KEY, ";
				sqlQuery += "\n id_world INTEGER NOT NULL, lat_idx SMALLINT, lon_idx SMALLINT, ";
				sqlQuery += "\n FOREIGN KEY (id_world) REFERENCES " + IUDatabase.worldsTableName + "(id_world));";

				this.st.execute (sqlQuery);

				// Players table
				sqlQuery = "\n CREATE TABLE " + IUDatabase.playersTableName;
				sqlQuery += "\n (id_player INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0, INCREMENT BY 1) NOT NULL PRIMARY KEY, ";
				sqlQuery += "\n id_world INTEGER NOT NULL, name VARCHAR NOT NULL, password VARCHAR NOT NULL, color INTEGER NOT NULL, world_ground_zero_lat FLOAT, world_ground_zero_lon FLOAT, game_ground_zero_lat FLOAT, game_ground_zero_lon FLOAT, num_hovercraft INTEGER, num_tank INTEGER, num_artillery INTEGER, ";
				sqlQuery += "\n FOREIGN KEY (id_world) REFERENCES " + IUDatabase.worldsTableName + "(id_world)); ";

				this.st.execute (sqlQuery);

				// Fog table
				sqlQuery = "\n CREATE TABLE " + IUDatabase.fogOfWarTableName;
				sqlQuery += "\n (id_player INTEGER NOT NULL, id_tile INTEGER NOT NULL, fields BIGINT NOT NULL, ";
				sqlQuery += "\n FOREIGN KEY (id_player) REFERENCES " + IUDatabase.playersTableName + "(id_player),";
				sqlQuery += "\n FOREIGN KEY (id_tile) REFERENCES " + IUDatabase.tilesTableName + "(id_tile));";

				this.st.execute (sqlQuery);

				// Flags table
				sqlQuery = "\n CREATE TABLE " + IUDatabase.flagsTableName;
				sqlQuery += "\n (id_flag INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0, INCREMENT BY 1) NOT NULL PRIMARY KEY, ";
				sqlQuery += "\n id_world INTEGER NOT NULL, id_owner INTEGER NOT NULL, name VARCHAR, location_lat FLOAT NOT NULL, location_lon FLOAT NOT NULL, num_hovercraft INTEGER NOT NULL, num_tank INTEGER NOT NULL, num_artillery INTEGER NOT NULL, id_tile INTEGER NOT NULL, field_mask BIGINT NOT NULL, ";
				sqlQuery += "\n FOREIGN KEY (id_world) REFERENCES " + IUDatabase.worldsTableName + "(id_world), ";
				sqlQuery += "\n FOREIGN KEY (id_owner) REFERENCES " + IUDatabase.playersTableName + "(id_player), ";
				sqlQuery += "\n FOREIGN KEY (id_tile) REFERENCES " + IUDatabase.tilesTableName + "(id_tile));";

				this.st.execute (sqlQuery);
			}
			//
			// Insert data into the DB
			//

			//
			// World
			//

			System.out.println ("Inserting world " + this.world.name);

			this.insertWorld (this.world.name, 
							  this.world.latitude, this.world.longitude, this.world.height, this.world.width,	this.world.dimPow);

			//
			// Players
			//

			String[] playerNames = {"Guerilla", "google1", "google2", "google3"};

			int[] colors = {0xff0000ff, 0xff00ff00, 0xff00ffff, 0xffff0000};

			System.out.println ("Inserting players");

			for (int id = 0; id < playerNames.length; id++)
			{
				System.out.println ("\t" + playerNames[id].toLowerCase ( ));
				// game ground zero
				float latG = (float) (this.world.latitude + Math.random ( ) * this.world.height);
				float lonG = (float) (this.world.longitude + Math.random ( ) * this.world.width);

				// world ground zero
				lat = (float) (this.world.latitude + (float) Math.random ( ) * 180.0d - 90.0d);
				lon = (float) (this.world.longitude + (float) Math.random ( ) * 360.0d - 180.0d);

				this.insertPlayer (1, playerNames[id].toLowerCase ( ), playerNames[id].toLowerCase ( ), colors[id], lat, lon, latG, lonG);

				// // Add some tiles as the previous state
				// long tile = 0x00183c7e7e3c1f00L;
				// // long tile = 0x00387c7c7c380000L;
				// // long tile = 0xffffffffffffffffL;
				//
				// for (int i = this.world.dim / 2 - openedArea; i < this.world.dim / 2 + openedArea; i++)
				// {
				// for (int j = this.world.dim / 2 - openedArea; j < this.world.dim / 2 + openedArea; j++)
				// {
				// this.insertFogTile (id, this.world.getId ( ).intValue ( ), i, j, tile);
				// }
				// }
			}

			//
			// Flags
			//

			System.out.println ("Inserting flags...");
			// Place flags for each tile
			float tileSize = this.world.height / this.world.dim;

			for (int i = 0; i < this.world.dim; i++)
			{
				for (int j = 0; j < this.world.dim; j++)
				{
					// Make 2-5 flags for each tile
					int flagNum = (int) (0.0d + Math.random ( ) * 2.5);

					for (int n = 0; n < flagNum; n++)
					{
						lat = this.world.latitude + i * tileSize + (float) Math.random ( ) * tileSize;
						lon = this.world.longitude + j * tileSize + (float) Math.random ( ) * tileSize;

						/*
						int numh = 6;// + (int) (Math.random ( ) * 10);
						int numt = 3;// + (int) (Math.random ( ) * 4);
						int numa = 2;// + (int) (Math.random ( ) * 2);*/

						//
						// DEBUG - Minimum number of units for the enemy
						//
						int numh = 1;
						int numt = 0;
						int numa = 0;

						this.insertFlag (1, 0, "", lat, lon, numh, numt, numa);
					}
				}
				System.out.println (" \tfor tile row " + i);
			}

			//
			// FIXME - DEBUG only - remove after
			//

			System.out.println ("Inserting player flags");
			for (int i = 1; i < playerNames.length; i++)
			{
				lat = this.world.latitude + this.world.height / 2.0f + (2 * openedArea) * this.world.height / this.world.dim
						* (float) (Math.random ( ) - 0.5d);
				lon = this.world.longitude + this.world.width / 2.0f + this.world.width / this.world.dim * (float) (Math.random ( ) - 0.5d);
				int numh = 6;// + (int) (Math.random ( ) * 10);
				int numt = 3;// + (int) (Math.random ( ) * 4);
				int numa = 2;// + (int) (Math.random ( ) * 2);

				this.insertFlag (1, i, playerNames[i] + " home", lat, lon, numh, numt, numa);
			}

			System.out.println ("done");

			this.st.execute ("SHUTDOWN");
		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}

	}


	/**
	 * Just prints out some stuff from the database
	 */
	public void printDB ( )
	{

		ResultSet rs = null;

		String sqlQuery;

		//
		// Print fow
		//
		try
		{
			System.out.println ("FOW\n---");
			
			sqlQuery = "\n SELECT T.lat_idx, T.lon_idx, FOW.fields ";
			sqlQuery += "\n FROM ";
			sqlQuery += "\n " + IUDatabase.tilesTableName + " T ";
			sqlQuery += "\n JOIN " + IUDatabase.fogOfWarTableName + " FOW ON FOW.id_tile = T.id_tile ";
			//sqlQuery += "\n WHERE FOW.id_player = " + 0 + ";";
			
			rs = this.st.executeQuery (sqlQuery);
			
			while (rs.next ( ))
			{
				System.out.println ("Fog (" + rs.getShort (2) + "," + rs.getShort (1) + ")");
			}
			
			rs.close ( );

			
			System.out.println ("ARMIES\n------");
			
			sqlQuery = "\n SELECT name, id_flag, num_hovercraft, num_tank, num_artillery";
			sqlQuery += "\n FROM ";
			sqlQuery += "\n " + IUDatabase.flagsTableName;
			sqlQuery += "\n WHERE id_owner != 0;";

			rs = this.st.executeQuery (sqlQuery);

			while (rs.next ( ))
			{
				System.out.println ("Flag " + rs.getString (1) + " [" + rs.getInt (2) + "](" + rs.getShort (3) + ", " + rs.getShort (4) + ", "
						+ rs.getShort (5) + ")");
			}

			rs.close ( );

		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close ( );
				}
				if (this.st != null)
				{
					this.st.execute ("SHUTDOWN");

					this.st.close ( );
				}
				if (this.c != null)
				{
					this.c.close ( );
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace ( );
			}
		}
	}


	/**
	 * Adds a world to the database
	 * 
	 * @param worldId -
	 *           id
	 * @param name -
	 *           name of the world
	 * @param lat -
	 *           minimum latitude of this world
	 * @param lon -
	 *           minimum longitude of this world
	 * @param height -
	 *           height of this world in degrees (latitude span)
	 * @param width -
	 *           width of this world in degrees (latitude span)
	 * @param fowSize -
	 *           number of Fog of war fields that extend from left to right of top to bottom of the specified
	 *           map
	 * @throws SQLException
	 */
	public void insertWorld (final String name, final float lat, final float lon, final float height, final float width, final int fowSize)
			throws SQLException
	{

		String sqlQuery = "\n INSERT INTO " + IUDatabase.worldsTableName;
		sqlQuery += "\n (name, min_lat, min_lon, height, width, fow_size) ";
		sqlQuery += "\n VALUES ('" + name + "', " + lat + ", " + lon + ", " + height + ", " + width + ", " + fowSize + "); ";

		this.st.execute (sqlQuery);

		int tileCount = 1 << fowSize;

		// This is in the database for sure
		int worldId = 1;

		for (int i = 0; i < tileCount; i++)
		{
			for (int j = 0; j < tileCount; j++)
			{
				sqlQuery = "\n INSERT INTO " + IUDatabase.tilesTableName;
				sqlQuery += "\n (id_world, lat_idx, lon_idx) ";
				sqlQuery += "\n VALUES (" + worldId + ", " + i + ", " + j + "); ";

				this.st.execute (sqlQuery);
			}

		}
	}


	/**
	 * Adds a flag to the database belonging to the player specified
	 * 
	 * @param flagId
	 * @param worldId
	 * @param ownerId
	 * @param flagName
	 * @param lat
	 * @param lon
	 * @param numh
	 * @param numt
	 * @param numa
	 * @throws SQLException
	 */
	public void insertFlag (final int worldId, final int ownerId, final String flagName, final float lat, final float lon, final int numh,
			final int numt, final int numa) throws SQLException
	{

		// Indexes of the fog of war fields
		int fLat = (int) Math.floor ((lat - this.world.latitude) * (this.world.dim * 8) / this.world.height);
		int fLon = (int) Math.floor ((lon - this.world.longitude) * (this.world.dim * 8) / this.world.width);

		long fieldMask = 1L << ((8 * (fLat % 8)) + (fLon % 8));

		// Indexes of the fog of war tiles
		int tLat = fLat >> 3;
		int tLon = fLon >> 3;

		String sqlQuery = "\n INSERT INTO " + IUDatabase.flagsTableName;
		sqlQuery += "\n (id_world, id_owner, name, location_lat, location_lon, num_hovercraft, num_tank, num_artillery, id_tile, field_mask)";
		sqlQuery += "\n SELECT " + worldId + ", " + ownerId + ", '" + flagName + "', " + lat + ", " + lon + ", " + numh + ", " + numt + ", " + numa
				+ ", TILE.id_tile, " + fieldMask;
		sqlQuery += "\n FROM " + IUDatabase.tilesTableName + " TILE ";
		sqlQuery += "\n WHERE lat_idx = " + tLat + " AND lon_idx = " + tLon + " AND id_world = 1;";

		this.st.execute (sqlQuery);
	}


	/**
	 * Adds a partially explored tile to the fog of war
	 * 
	 * @param playerId
	 * @param i -
	 *           longitude index
	 * @param j -
	 *           latitude index
	 * @param fields -
	 *           tile (each bit represents a field in the fog of war)
	 * @throws SQLException
	 */
	public void insertFogTile (final int playerId, final int worldId, final int i, final int j, final long fields) throws SQLException
	{
		String sqlQuery = "\n INSERT INTO " + IUDatabase.fogOfWarTableName;
		sqlQuery += "\n (id_player, id_tile, fields)";
		sqlQuery += "\n SELECT " + playerId + ", TILE.id_tile, " + fields + " ";
		sqlQuery += "\n FROM " + IUDatabase.tilesTableName + " TILE ";
		sqlQuery += "\n WHERE TILE.lat_idx = " + j + " AND TILE.lon_idx = " + i + " AND id_world = " + worldId + ";";

		this.st.execute (sqlQuery);
	}


	/**
	 * TODO - ground zero should be set
	 * 
	 * Adds a new player to the game in the world specified
	 * 
	 * @param playerId
	 * @param worldId
	 * @param name
	 * @param password
	 * @param color
	 * @param wLat -
	 *           World ground zero latitude
	 * @param wLon -
	 *           World ground zero longitude
	 * @param gLat -
	 *           Game ground zero latitude
	 * @param gLon -
	 *           Game ground zero longitude
	 * @throws SQLException
	 */

	public void insertPlayer (final int worldId, final String name, final String password, final int color, final float wLat, final float wLon,
			final float gLat, final float gLon) throws SQLException
	{
		// FIXME - initial amount of units ??? (12,5,2)
		String sqlQuery = "\n INSERT INTO " + IUDatabase.playersTableName;
		sqlQuery += "\n (id_world, name, password, color, world_ground_zero_lat, world_ground_zero_lon, game_ground_zero_lat, game_ground_zero_lon, num_hovercraft, num_tank, num_artillery) ";
		sqlQuery += "\n VALUES (" + worldId + ", '" + name + "', '" + password + "', " + color + "," + wLat + ", " + wLon + ", " + gLat + ", " + gLon
				+ ", 4, 2, 1);";

		this.st.execute (sqlQuery);
	}


	/**
	 * Gets the world bounds for the current game from the DB
	 * 
	 * @param worldName -
	 */
	public void setWorld (final String worldName)
	{

		this.world = null;

		try
		{

			String sqlQuery = " SELECT id_world, min_lat, min_lon, height, width, fow_size ";
			sqlQuery += " FROM " + IUDatabase.worldsTableName;
			sqlQuery += " WHERE name ='" + worldName + "';";

			this.st.execute (sqlQuery);
			ResultSet rs = this.st.executeQuery (sqlQuery);

			if (rs.next ( ))
			{
				this.world = new World (new Integer (rs.getInt (1)), worldName, rs.getFloat (2), rs.getFloat (3), rs.getFloat (4), rs.getFloat (5), rs
						.getInt (6));
			}
			rs.close ( );
		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
		}
	}
}
