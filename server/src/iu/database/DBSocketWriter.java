package iu.database;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * Used to read game status and flush it directly to the DataOutputStream (for socket)
 */
public class DBSocketWriter extends IUDatabase
{

	/**
	 * Writes map info to the dout stream
	 * 
	 * @param dout
	 * @param playerId
	 */
	public void writeMapInfo (final DataOutputStream dout, final int playerId)
	{
		try
		{
			String sqlQuery = " SELECT min_lat, min_lon, height, width, fow_size ";
			sqlQuery += " FROM " + IUDatabase.worldsTableName + " W ";
			sqlQuery += " JOIN " + IUDatabase.playersTableName + " P ON P.id_world = W.id_world ";
			sqlQuery += " WHERE P.id_player = " + playerId + ";";

			ResultSet rs = this.st.executeQuery (sqlQuery);

			if (rs.next ( ))
			{
				// Map location on the globe
				dout.writeFloat (rs.getFloat (1));
				dout.writeFloat (rs.getFloat (2));
				dout.writeFloat (rs.getFloat (3));
				dout.writeFloat (rs.getFloat (4));
				dout.writeByte (rs.getByte (5));

				System.out.println ("Fow size : " + rs.getByte (5) + " POW ");
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
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
	public void writeCommanderInfo (final DataOutputStream dout, final int playerId)
	{
		try
		{
			String sqlQuery = " SELECT num_hovercraft, num_tank, num_artillery ";
			sqlQuery += " FROM " + IUDatabase.playersTableName;
			sqlQuery += " WHERE id_player = '" + playerId + "' AND id_world = 1;";

			this.st.execute (sqlQuery);

			ResultSet rs = this.st.executeQuery (sqlQuery);

			if (rs.next ( ))
			{
				dout.writeShort (rs.getShort (1));
				dout.writeShort (rs.getShort (2));
				dout.writeShort (rs.getShort (3));
			}

		}
		catch (SQLException ex)
		{
			ex.printStackTrace ( );
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
		try
		{
			String sqlQuery = " SELECT id_player, name ";
			sqlQuery += " FROM iu_players ";
			sqlQuery += " WHERE id_world = 1;";

			this.st.execute (sqlQuery);

			ResultSet rs = this.st.executeQuery (sqlQuery);

			while (rs.next ( ))
			{
				dout.writeByte (delimiter);

				dout.writeInt (rs.getInt (1)); // id

				String name = rs.getString (2);
				dout.writeByte (name.length ( )); // name.length
				dout.writeBytes (name); // name

				// dout.writeInt (rs.getInt (3)); // color
				// dout.writeFloat (rs.getFloat (4)); // lat
				// dout.writeFloat (rs.getFloat (5)); // lon
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
	}


	/**
	 * Writes the flags standing on an at least partially explored fog of war tile to the dout stream
	 * 
	 * @param dout
	 * @param playerId
	 */
	public void writeFlags (final DataOutputStream dout, final int playerId, final byte delimiter)
	{
		try
		{
			String sqlQuery = "\n SELECT F.id_flag, F.location_lat, F.location_lon, F.id_owner, ";
			sqlQuery += "\n F.num_hovercraft, F.num_tank, F.num_artillery, FOW.fields, F.field_mask ";
			sqlQuery += "\n FROM ";
			sqlQuery += "\n " + IUDatabase.tilesTableName + " T ";
			sqlQuery += "\n JOIN " + IUDatabase.fogOfWarTableName + " FOW ON FOW.id_tile = T.id_tile ";
			sqlQuery += "\n JOIN " + IUDatabase.flagsTableName + " F ON F.id_tile = T.id_tile ";
			sqlQuery += "\n WHERE ";
			sqlQuery += "\n FOW.id_player = " + playerId + " AND ";
			sqlQuery += "\n T.id_world = 1;";

			this.st.execute (sqlQuery);
			ResultSet rs = this.st.executeQuery (sqlQuery);

			int count = 0;
			while (rs.next ( ))
			{
				long tile = rs.getLong (8);
				long fieldMask = rs.getLong (9);

				if ((tile & fieldMask) == fieldMask)
				{
					dout.writeByte (delimiter);
					dout.writeInt (rs.getInt (1)); // id
					dout.writeFloat (rs.getFloat (2)); // lat
					dout.writeFloat (rs.getFloat (3)); // lon
					dout.writeInt (rs.getInt (4)); // id_owner
					dout.writeShort (rs.getShort (5)); // hovercraft
					dout.writeShort (rs.getShort (6)); // tank
					dout.writeShort (rs.getShort (7)); // artillery
					count++;
				}
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
	}


	/**
	 * Writes the status of tiles to the dout stream
	 * 
	 * @param dout
	 * @param playerId
	 */
	public void writeFogOfWar (final DataOutputStream dout, final int playerId, final byte delimiter)
	{
		try
		{
			String sqlQuery = "\n SELECT T.lat_idx, T.lon_idx, FOW.fields ";
			sqlQuery += "\n FROM ";
			sqlQuery += "\n " + IUDatabase.tilesTableName + " T ";
			sqlQuery += "\n JOIN " + IUDatabase.fogOfWarTableName + " FOW ON FOW.id_tile = T.id_tile ";
			sqlQuery += "\n WHERE FOW.id_player = " + playerId + ";";

			ResultSet rs = this.st.executeQuery (sqlQuery);

			System.out.println ("Sending Fog of war !!!!!!!!!!!!!!!! for player " + playerId);

			while (rs.next ( ))
			{
				dout.writeByte (delimiter);
				dout.writeShort (rs.getShort (2)); // lon
				dout.writeShort (rs.getShort (1)); // lat
				dout.writeLong (rs.getLong (3)); // tile fields

				System.out.println ("Fog (" + rs.getShort (2) + "," + rs.getShort (1) + "," + Long.toHexString (rs.getLong (3)) + ")");
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
	}

}
