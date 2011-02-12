package iu.server.explore.game;

import iu.android.network.explore.Protocol;
import iu.android.util.Converter;

/**
 * 
 * 
 */
public class ProtocolFormater
{

	//
	// EVENTs
	//

	/**
	 * EVENT_FLAG_OWNER_CHANGED
	 */
	public static byte[] format (final EventPlayerMoved e)
	{
		byte[] arr = new byte[13];

		arr[0] = Protocol.EVENT_LOCATION_UPDATE;

		Player player = e.getPlayer ( );

		// PlayerID
		Converter.intToBytes (player.id, arr, 1);

		Location loc = player.getLocation ( );

		// Location
		Converter.floatToBytes (loc.latitude, arr, 5);
		Converter.floatToBytes (loc.longitude, arr, 9);

		return arr;
	}


	/**
	 * EVENT_FLAG_DISCOVERED
	 */
	public static byte[] format (final EventFlagDiscovered e)
	{
		// [cmdId[byte], flagId[int], lat[float], lon[float], playerId[int]]
		byte[] arr = new byte[17];

		arr[0] = Protocol.EVENT_FLAG_DISCOVERED;

		// add flag id
		Converter.intToBytes (e.getFlag ( ).id, arr, 1);

		// Flag location
		Converter.floatToBytes (e.getFlag ( ).location.latitude, arr, 5);
		Converter.floatToBytes (e.getFlag ( ).location.longitude, arr, 9);

		// add player id
		Converter.intToBytes (e.getFlag ( ).getOwner ( ).id, arr, 13);

		return arr;

	}


	/**
	 * EVENT_PLAYER_LOGGED_OUT
	 */
	public static byte[] format (final EventPlayerLoggedOut e)
	{
		byte[] arr = new byte[5];

		arr[0] = Protocol.EVENT_PLAYER_LOGGED_OUT;

		int id = e.getPlayer ( ).id.intValue ( );

		// add player id
		Converter.intToBytes (id, arr, 1);

		return arr;

	}


	/**
	 * EVENT_FLAG_OWNER_CHANGED
	 */
	public static byte[] format (final EventFlagOwnerChanged e)
	{
		byte[] arr = new byte[9];

		arr[0] = Protocol.EVENT_FLAG_OWNER_CHANGED;

		Flag flag = e.getFlag ( );

		// add flag id
		Converter.intToBytes (flag.id, arr, 1);

		// add player id
		Converter.intToBytes (flag.getOwner ( ).id, arr, 5);

		return arr;

	}


	/**
	 * EVENT_FLAG_UNITS_CHANGED
	 */
	public static byte[] format (final EventFlagUnitsChanged e)
	{
		byte[] arr = new byte[11];

		arr[0] = Protocol.EVENT_FLAG_UNITS_CHANGED;

		Flag flag = e.getFlag ( );

		// add flag id
		Converter.intToBytes (flag.id, arr, 1);

		// add units
		Converter.shortToBytes (flag.getHover ( ), arr, 5);
		Converter.shortToBytes (flag.getTank ( ), arr, 7);
		Converter.shortToBytes (flag.getArtillery ( ), arr, 9);

		return arr;

	}


	public static byte[] format (final EventUnderAttack e)
	{
		byte[] arr = new byte[5];

		// Command
		arr[0] = Protocol.EVENT_FLAG_ATTACKED;

		// Battle id
		Battle b = e.getBattle ( );
		int battleId = b.id;
		Converter.intToBytes (battleId, arr, 1);

		return arr;
	}


	public static byte[] format (final EventBattleStarted e)
	{
		byte[] arr = new byte[9];

		// Command
		arr[0] = Protocol.EVENT_START_BATTLE;

		// BattleID
		Battle battle = e.getBattle ( );
		Converter.intToBytes (battle.id, arr, 1);

		// TCP Port
		Converter.intToBytes (battle.getServer ( ).getTCPListenPort ( ), arr, 5);

		return arr;
	}


	public static byte[] format (final EventFlagStateChanged e)
	{
		byte[] arr = new byte[6];

		// Command
		arr[0] = Protocol.EVENT_FLAG_STATE_CHANGED;

		// Flag ID
		Converter.intToBytes (e.getFlag ( ).id, arr, 1);

		// State
		arr[5] = e.getState ( );

		return arr;
	}


	public static byte[] format (final EventExploration e)
	{
		byte[] arr = new byte[6];

		// Command
		arr[0] = Protocol.EVENT_EXPLORED;

		// Tile.i and Tile.j
		Converter.shortToBytes (e.i, arr, 1);
		Converter.shortToBytes (e.j, arr, 3);

		// Field index 0..63
		arr[5] = e.field;

		return arr;
	}


	//
	// DATA
	//

	public static byte[] format (final DataFlagUnits data)
	{
		byte[] arr = new byte[11];

		arr[0] = Protocol.DATA_FLAG_UNITS;

		Flag flag = data.getFlag ( );
		Converter.intToBytes (flag.id, arr, 1);

		Converter.intToBytes (flag.id, arr, 1);
		Converter.shortToBytes (flag.getHover ( ), arr, 5);
		Converter.shortToBytes (flag.getTank ( ), arr, 7);
		Converter.shortToBytes (flag.getArtillery ( ), arr, 9);

		return arr;
	}


	public static byte[] format (final DataFlagBattle data)
	{
		byte[] arr = new byte[21];

		arr[0] = Protocol.DATA_FLAG_BATTLE;

		FlagBattle battle = data.getBattle ( );
		Converter.intToBytes (battle.attacker.id, arr, 1);
		Converter.intToBytes (battle.getFlag ( ).id, arr, 5);

		// Attacker
		if (battle.attackerUnits != null)
		{
			Converter.shortToBytes (battle.attackerUnits.hover, arr, 9);
			Converter.shortToBytes (battle.attackerUnits.tank, arr, 11);
			Converter.shortToBytes (battle.attackerUnits.artillery, arr, 13);
		}
		else
		{
			for (int i = 9; i < 15; i++)
			{
				arr[i] = 0;
			}
		}

		// Defender
		if (battle.defenderUnits != null)
		{
			Converter.shortToBytes (battle.defenderUnits.hover, arr, 15);
			Converter.shortToBytes (battle.defenderUnits.tank, arr, 17);
			Converter.shortToBytes (battle.defenderUnits.artillery, arr, 19);
		}
		else
		{
			for (int i = 15; i < 21; i++)
			{
				arr[i] = 0;
			}
		}

		return arr;
	}


	public static byte[] format (EventPlayerPromoted e)
	{
		// command | playerID | rank
		byte[] arr = new byte[6];

		// Command
		arr[0] = Protocol.EVENT_PLAYER_PROMOTED;

		// PlayerID
		Converter.intToBytes (e.getPlayer ( ).id, arr, 1);

		// Rank
		arr[5] = (byte) e.getPlayer ( ).getRank ( ).ordinal ( );

		return arr;
	}
	
	
	public static byte [] format (EventBattleFinished e)
	{
		// command | battleID
		byte[] arr = new byte[5];

		// Command
		arr[0] = Protocol.EVENT_BATTLE_FINISHED;

		// BattleID
		Battle battle = e.getBattle ( );
		Converter.intToBytes (battle.id, arr, 1);

		return arr;
	}
}
