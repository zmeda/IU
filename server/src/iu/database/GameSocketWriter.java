/**
 * 
 */
package iu.database;

/**
 * @author xp
 * 
 */
import iu.android.network.Protocol;
import iu.server.explore.game.ExploredArea;
import iu.server.explore.game.ExploredTile;
import iu.server.explore.game.Flag;
import iu.server.explore.game.Game;
import iu.server.explore.game.Player;
import iu.server.explore.game.World;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;


/**
 * 
 * @author luka
 *
 */
public class GameSocketWriter
{
	private Game	game;


	public GameSocketWriter (final Game game)
	{
		this.game = game;
	}


	/**
	 * Writes map info to the dout stream
	 * 
	 * @param dout
	 * @param playerId
	 */
	public void writeMapInfo (final DataOutputStream dout, final Player player)
	{
		World world = player.getWorld ( );

		try
		{
			if (world != null)
			{
				dout.writeFloat (world.latitude);
				dout.writeFloat (world.longitude);
				dout.writeFloat (world.height);
				dout.writeFloat (world.width);
				dout.writeByte (world.dimPow);
			}
			else
			{
				// TODO - handle this on the client
				dout.writeByte (Protocol.NOT_FOUND);
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace ( );
		}
	}


	/**
	 * Writes commander info to the dout stream
	 * 
	 * @param dout
	 * @param playerId
	 */
	public void writeCommanderInfo (final DataOutputStream dout, final Player player)
	{
		try
		{
			if (player != null)
			{
				// FIXME - client should read short not int
				dout.writeShort (player.getHover ( )); // hover
				dout.writeShort (player.getTank ( )); // tank
				dout.writeShort (player.getArtillery ( )); // artillery
			}
			else
			{
				// FIXME - handle this on the client
				dout.writeByte (Protocol.NOT_FOUND);
			}

		}
		catch (IOException ex)
		{
			ex.printStackTrace ( );
		}
	}


	/**
	 * Writes all of the players to the dout stream
	 * 
	 * @param dout
	 */
	public void writePlayers (final DataOutputStream dout, final byte delimiter)
	{
		// FIXME - ONLY the players that the client can currently see or their flags
		Iterator<Player> it = this.game.getAllPlayers ( );

		try
		{
			while (it.hasNext ( ))
			{
				Player p = it.next ( );

				dout.writeByte (delimiter);

				// id
				dout.writeInt (p.id.intValue ( ));

				// name
				String name = p.getName ( );
				dout.writeByte (name.length ( )); // name.length
				dout.writeBytes (name); // name string
				
				// Rank
				byte b = (byte) p.getRank ( ).ordinal ( );
				dout.writeByte (b);
			}

		}
		catch (IOException ex)
		{
			ex.printStackTrace ( );
		}
	}


	/**
	 * Writes the flags standing on an at least partially explored fog of war tile to the dout stream
	 * 
	 * @param dout
	 * @param playerId
	 */
	@SuppressWarnings("deprecation")
	public void writeFlags (final DataOutputStream dout, final Player player, final byte delimiter)
	{
		try
		{
			ExploredArea area = player.area;

			Iterator<Flag> it = this.game.getAllFlags ( );

			while (it.hasNext ( ))
			{
				Flag flag = it.next ( );

				// If flag is visible in the players explored area then write it to the
				if (area.isFlagVisible (flag))
				{
					dout.writeByte (delimiter);
					dout.writeInt (flag.id.intValue ( )); // id
					// FIXME - we should use float instead of double
					dout.writeFloat (flag.location.getLatitude ( )); // lat
					dout.writeFloat (flag.location.getLongitude ( )); // lon
					dout.writeInt (flag.getOwner ( ).id.intValue ( )); // id_owner
					dout.writeShort (flag.getHover ( )); // hovercraft
					dout.writeShort (flag.getTank ( )); // tank
					dout.writeShort (flag.getArtillery ( )); // artillery
				}
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace ( );
		}
	}


	/**
	 * Writes the status of tiles to the dout stream
	 * 
	 * @param dout
	 * @param playerId
	 */
	public void writeFogOfWar (final DataOutputStream dout, final Player player, final byte delimiter)
	{
		try
		{
			Iterator<ExploredTile> tiles = player.area.getExploredTiles ( );

			int num = 0;

			while (tiles.hasNext ( ))
			{
				num++;
				ExploredTile tile = tiles.next ( );

				dout.writeByte (delimiter);
				dout.writeShort (tile.i ( )); // lon
				dout.writeShort (tile.j ( )); // lat
				dout.writeLong (tile.getFields ( )); // tile fields
			}

		}
		catch (IOException ex)
		{
			ex.printStackTrace ( );
		}
	}
}
