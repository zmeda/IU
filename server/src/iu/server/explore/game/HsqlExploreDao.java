/**
 * 
 */
package iu.server.explore.game;

import iu.database.IUDatabase;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class HsqlExploreDao extends IUDatabase implements IExploreDao
{

	protected Game		game;
	protected Worker	worker;


	public HsqlExploreDao (final Game game)
	{
		// Dao for this game
		this.game = game;

		// Open DB connection
		this.connect ( );

		// Start the worker thread for persistence management
		this.worker = new Worker ( );
		this.worker.start ( );
	}


	public void connect ( )
	{
		this.openDbConnection ( );
	}
	
	public void disconnect ( )
	{
		// Tell him to stop
		this.worker.halt ( );

		// Wait until he is done
		while (!this.worker.done)
		{
			// Wait for worker to finish
			try
			{
				Thread.sleep (100);
			}
			catch (InterruptedException e)
			{
				// Keep waiting
			}
		}

		// Close open connection to DB
		this.closeDbConnection ( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see iu.server.explore.game.IExploreDao#exploredArea(iu.server.explore.game.Player)
	 */
	public ExploredArea exploredArea (final Player player)
	{
		ExploredArea exploredArea = null;

		// TODO - load form DB

		return exploredArea;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see iu.server.explore.game.IExploreDao#flagById(int)
	 */
	public Flag flagById (final int id)
	{

		Flag flag = null;

		// try
		// {
		// String sqlQuery = "\n SELECT id_owner, location_lat, location_lon, hovercraft_num, tank_num,
		// artillery_num
		// ";
		// sqlQuery += "\n FROM " + IUDatabase.flagsTableName;
		// sqlQuery += "\n WHERE ";
		// sqlQuery += "\n id_flag = " + id + ";";
		//
		// this.st.execute (sqlQuery);
		// ResultSet rs = this.st.executeQuery (sqlQuery);
		//
		// if (rs.next ( ))
		// {
		// Location loc = new Location (rs.getDouble (2), rs.getDouble (3));
		//
		// // TODO - add the player
		// flag = new Flag (id, this.world, null, loc, rs.getInt (4), rs.getInt (5), rs.getInt (6));
		// }
		//
		// if (rs.next ( ))
		// {
		// throw new SQLException ("Two flags with same id found: id[" + id + "]");
		// }
		//
		// rs.close ( );
		// }
		// catch (SQLException ex)
		// {
		// ex.printStackTrace ( );
		// }

		return flag;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see iu.server.explore.game.IExploreDao#playerById(int)
	 */
	public Player playerById (final int id)
	{
		Player player = null;

		// TODO - load from DB

		return player;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see iu.server.explore.game.IExploreDao#whoSeesTile(iu.server.explore.game.Tile)
	 */
	public List<Player> whoSeesTile (final Tile tile)
	{
		List<Player> players = null;

		// TODO get from DB

		return players;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see iu.server.explore.game.IExploreDao#updateFlag(iu.server.explore.game.Flag)
	 */
	public void updateFlag (final Flag flag)
	{
		StringBuilder sb = new StringBuilder ( );

		sb.append ("UPDATE ");
		sb.append (IUDatabase.flagsTableName);
		sb.append (" SET ");
		sb.append (" id_owner = ");
		sb.append (flag.owner.id.intValue ( ));
		sb.append (", num_hovercraft = ");
		sb.append (flag.getHover ( ));
		sb.append (", num_tank = ");
		sb.append (flag.getTank ( ));
		sb.append (", num_artillery = ");
		sb.append (flag.getArtillery ( ));
		sb.append (" WHERE id_flag = ");
		sb.append (flag.id.intValue ( ));
		sb.append (";");

		this.worker.execute (sb.toString ( ));
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see iu.server.explore.game.IExploreDao#updateFog(iu.server.explore.game.Player)
	 */
	public void updateFog (final Player player)
	{
		// Get tile where the player is currently located
		Tile tile = this.game.getWorld ( ).tileAt (player.loc);

		// Get fields of that tile
		long fields = player.area.visibleFields (tile);

		StringBuilder sb = new StringBuilder ( );
		sb.append ("UPDATE ");
		sb.append (IUDatabase.fogOfWarTableName);
		sb.append (" SET ");
		sb.append (" fields = ");
		sb.append (fields);
		sb.append (" WHERE id_player = ");
		sb.append (player.id.intValue ( ));
		sb.append (" AND id_tile = ");
		sb.append (tile.hashCode ( ));
		sb.append (";");

		this.worker.execute (sb.toString ( ));
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see iu.server.explore.game.IExploreDao#updatePlayer(iu.server.explore.game.Player)
	 */
	public void updatePlayer (final Player player)
	{
		StringBuilder sb = new StringBuilder ( );

		sb.append ("UPDATE ");
		sb.append (IUDatabase.playersTableName);
		sb.append (" SET ");
		sb.append (" num_hovercraft = ");
		sb.append (player.getHover ( ));
		sb.append (", num_tank = ");
		sb.append (player.getTank ( ));
		sb.append (", num_artillery = ");
		sb.append (player.getArtillery ( ));
		sb.append (" WHERE id_player = ");
		sb.append (player.id.intValue ( ));
		sb.append (";");

		this.worker.execute (sb.toString ( ));
	}

	
	@Override
	public Player newPlayer (String username, String password, Game game)
	{
		// FIXME remove from super and put it in here 
		return super.newPlayer (username, password, game);
	}
	
	
	/**
	 * A Thread that executes sql commands
	 */
	private class Worker extends Thread
	{
		final LinkedList<String>	fifo		= new LinkedList<String> ( );
		boolean							running	= true;
		boolean							done		= false;


		public Worker ( )
		{
			// TODO
		}


		@Override
		public void run ( )
		{
			while (this.running)
			{
				while (!this.fifo.isEmpty ( ))
				{
					// Get the update query
					String sql = this.fifo.remove (0);

					// Execute it
					// HsqlExploreDao.this.iuDB.executeHsqlUpdate (sql);
					HsqlExploreDao.this.executeHsqlUpdate (sql);
				}

				try
				{
					Thread.sleep (100);
				}
				catch (InterruptedException e)
				{
					// Is never going to happen ...
				}
			}

			this.done = true;
		}


		public void execute (final String sql)
		{
			this.fifo.add (sql);
		}


		public void halt ( )
		{
			this.running = false;
		}
	}
}
