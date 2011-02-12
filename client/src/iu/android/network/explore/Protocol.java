package iu.android.network.explore;

import java.util.HashMap;

/*************************************************************************************************************
 * IU 1.0b, a java real time strategy game
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 ************************************************************************************************************/

/**
 * This class defines the protocol spoken by all the players and the server while a player is joining an open
 * game on the server.-
 * 
 * FIXME: These commands are human readable because we are constantly debugging. They must be changed to more
 * compact ones, at bit level.
 */
public final class Protocol
{
	public static final int						TCP_PORT								= 3000;
	public static final int						UDP_PORT								= 3000;

	public static final int						MAP_SIZE								= 0;
	public static final int						MAP_SEED								= 0;

	public static final byte					BATTLE_ACCEPT_STATE_NONE		= 0;
	public static final byte					BATTLE_ACCEPT_STATE_ACCEPTED	= 1;
	public static final byte					BATTLE_ACCEPT_STATE_AI			= 2;
	public static final byte					BATTLE_ACCEPT_STATE_REFUSED	= 3;

	/**
	 * DO NOT CHANGE SEQUENCE
	 * 
	 * commands from 1 - 95 are send/received trought TCP port and from 96 to 127 trought UDP port
	 * 
	 * Evets identificators are from 64 to 95
	 */

	public static final byte					LOGIN_DATA							= 1;
	public static final int						OK										= 2;
	public static final int						NOT_ACCEPTED						= 3;
	public static final byte					SIGNIN_DATA							= 4;
	public static final byte					GET_MAP_INFO						= 5;
	public static final int						MAP_INFO								= 6;
	public static final byte					GET_PLAYERS							= 7;
	public static final byte					PLAYER								= 8;
	public static final byte					GET_COMMANDER						= 9;
	public static final byte					COMMANDER							= 10;
	public static final byte					GET_FOG_OF_WAR						= 11;
	public static final byte					FOG_OF_WAR							= 12;
	public static final byte					GET_FLAGS							= 13;
	public static final byte					FLAG									= 14;
	public static final byte					END_INITIALIZATION				= 15;	// end initilization - explore
	// mode starts
	public static final byte					ABORT									= 16;
	public static final byte					SET_UDP_RECEIVE_PORT				= 17;
	public static final byte					GET_UDP_SEND_PORT					= 18;
	public static final byte					GET_TCP_PORT						= 19;
	public static final byte					GET_PLAYER_ID						= 20;
	public static final byte					GET_MAP_SIZE						= 21;
	public static final byte					GET_MAP_SEED						= 22;
	public static final byte					GET_NUMBER_OF_UNITS				= 23;
	public static final byte					SEND_MODE							= 24;
	public static final int						UDP_SEND_PORT						= 25;
	public static final int						PLAYER_ID							= 26;
	public static final int						NUMBER_OF_UNITS					= 27;
	public static final int						SET_PLAYER_NAME					= 28;
	public static final byte					PLAYER_NAME							= 29;
	public static final byte					PLAYER_COLOR						= 30;
	public static final int						SET_PLAYER_COLOR					= 31;
	public static final byte					SET_NUMBER_OF_UNITS				= 32;
	public static final int						GET_UNIT_Y							= 33;
	public static final byte					UNIT_X								= 34;
	public static final int						GET_UNIT_X							= 35;
	public static final byte					START_EXPLORE_MODE				= 36;
	public static final byte					LOGOUT								= 37;

	//
	// commands to control sending of events to client
	// when BUFFER_EVENTS is sent to server, it start buffering of all events
	// into local buffer. When FLUSH_EVENT is sent all buffered events are sent to
	// client
	//
	public static final byte					BUFFER_EVENTS						= 40;	// [cmdId[byte]]
	public static final byte					FLUSH_EVENTS						= 41;	// [cmdId[byte]]

	public static final byte					GET_FLAG_UNITS						= 42;	// [cmdId[byte], flagId[int]]
	public static final byte					SET_FLAG_UNITS						= 43;	// [flagId[byte], flagId[int],
	// hovercraft[short],
	// tank[short],
	// artillery[short]]
	public static final byte					DATA_FLAG_UNITS					= 44;	// [cmdId[byte], flagId[int],
	// hovercraft[short],
	// tank[short],
	// artillery[short]]

	public static final byte					GET_PLAYER_PROPERTIES			= 45;	// [cmdId[byte], playerId[int]]

	/** [1] | player[4] | hover[2] | tank[2] | artillery[2] */
	public static final byte					SET_PLAYER_PROPERTIES			= 46;

	public static final byte					DATA_PLAYER_PROPERTIES			= 47;	// [cmdId[byte], playerId[int],
	// nameLength[int], name... ,
	// hovercraft[short],
	// tank[short],
	// artillery[short]]

	public static final byte					ATTACK_FLAG							= 48;	// [cmdId[byte], flagId[int],
	// hovercraft[short],
	// tank[short],
	// artillery[short]]
	public static final byte					ATTACK_COMMANDER					= 49;	// [cmdId[byte], playerId[int],
	// hovercraft[short],
	// tank[short],
	// artillery[short]]
	public static final byte					DEFFEND_FLAG						= 50;	// [cmdId[byte], battleId[int],
	// hovercraft[short],
	// tank[short],
	// artillery[short]]
	public static final byte					FINISH_BATTLE						= 51;	// [cmdId[byte], battleId[int],
	// playerId[int]]

	public static final byte					GET_BATTLE							= 52;	// [cmdId[byte], battleId[int]]
	public static final byte					DATA_FLAG_BATTLE					= 53;	// [cmdId[byte],
	// attackerId[int], flagId[int]
	// ]

	public static final byte					EVENT_FLAG_UNITS_CHANGED		= 64;	// [cmdId[byte], flagId[int],
	// hovercraft[short],
	// tank[short],
	// artillery[short]]
	public static final byte					EVENT_FLAG_STATE_CHANGED		= 65;	// [cmdId[byte], flagId[int],
	// state[byte]]
	public static final byte					EVENT_FLAG_OWNER_CHANGED		= 66;	// [cmdId[byte], flagId[int],
	// playerId[int]]
	public static final byte					EVENT_FLAG_DISCOVERED			= 67;	// [cmdId[byte], flagId[int],
	// lat[float], lon[float],
	// playerId[int]]
	public static final byte					EVENT_EXPLORED						= 68;	// [cmdId[byte], tile.x[short],
	// tile.y[short], field[byte]

	public static final byte					EVENT_START_BATTLE				= 69;	// [cmdId[byte], battleId,
	// tcpPort [int]]
	public static final byte					EVENT_FLAG_ATTACKED				= 70;	// [cmdId[byte], battleId[int],
	// hovercraft[short],
	// tank[short],
	// artillery[short]]

	public static final byte					EVENT_LOCATION_UPDATE			= 71;	// [cmdId[byte], playerId[int],
	// lat[float], lon[float]]
	public static final byte					EVENT_PLAYER_LOGGED_OUT			= 72;	// [cmdId[byte], playerId[int]
	public static final byte					EVENT_PLAYER_PROMOTED			= 73;	// [cmdId[byte], playerId[int],
	// rank[byte]

	public static final byte					EVENT_BATTLE_FINISHED			= 74;	// [cmdId[byte], battleId[int],

	//
	// commands sent/received throught UDP port
	//
	public static final byte					EVENT_LOCATIONS_UPDATE			= 96;	// locations of other visible
	// users - not used in this
	// release
	// For now over TCP
	// public static final byte EVENT_LOCATION_UPDATE = 97; // one location of other visible player
	public static final byte					LOCATION_UPDATE					= 98;	// command for sending clients
	// location to server

	/**
	 * UDP packages
	 * 
	 * EVENT_LOCATIONS_UPDATE package
	 * ------------------------------------------------------------------------------- size = 128 bytes {
	 * command name [byte] | number of locations [byte] | location_1 [|..] } location_X = { player id [int] |
	 * latitude [float] | longtitude [float] }
	 * 
	 * ==> maximum 10 loactions are sent -> 122 bytes used
	 * 
	 * ///////////////////////////////////////////////// // 0 - command // 1 - number of locations (1-10) //
	 * 2-121 - locations - each 12bytes // 122-127 - not used /////////////////////////////////////////////////
	 * 
	 * 
	 * EVENT_LOCATION_UPDATE --------------------------------------------------------------------------------
	 * size = 13 { command name [byte] | player id [int] | latitude [float] | longtitude [flaot] }
	 * 
	 * LOCATION_UPDATE ------------------------------------------------------------------------------- size =
	 * 13 bytes { command name [byte] | player id [int] | latitude [float] | longtitude [float] }
	 * 
	 */

	private static HashMap<Byte, Integer>	commandLengths;
	static
	{
		Protocol.commandLengths = new HashMap<Byte, Integer> ( );

		Protocol.commandLengths.put (Protocol.BUFFER_EVENTS, 1);
		Protocol.commandLengths.put (Protocol.FLUSH_EVENTS, 1);
		Protocol.commandLengths.put (Protocol.GET_FLAG_UNITS, 5);
		Protocol.commandLengths.put (Protocol.SET_FLAG_UNITS, 11);
		Protocol.commandLengths.put (Protocol.DATA_FLAG_UNITS, 11);
		Protocol.commandLengths.put (Protocol.GET_PLAYER_PROPERTIES, 5);
		Protocol.commandLengths.put (Protocol.SET_PLAYER_PROPERTIES, 11); // lenght depends on name length
		Protocol.commandLengths.put (Protocol.DATA_PLAYER_PROPERTIES, -1);// lenght depends on name length
		Protocol.commandLengths.put (Protocol.ATTACK_FLAG, 11);
		Protocol.commandLengths.put (Protocol.ATTACK_COMMANDER, 11);
		Protocol.commandLengths.put (Protocol.DEFFEND_FLAG, 11);
		Protocol.commandLengths.put (Protocol.EVENT_FLAG_UNITS_CHANGED, 11);
		Protocol.commandLengths.put (Protocol.EVENT_FLAG_STATE_CHANGED, 6);
		Protocol.commandLengths.put (Protocol.EVENT_FLAG_OWNER_CHANGED, 1); // FIXME A je res samo 1?
		Protocol.commandLengths.put (Protocol.EVENT_FLAG_DISCOVERED, 17);
		Protocol.commandLengths.put (Protocol.EVENT_EXPLORED, 6);
		Protocol.commandLengths.put (Protocol.EVENT_LOCATION_UPDATE, 13);
		Protocol.commandLengths.put (Protocol.EVENT_START_BATTLE, 9);
		Protocol.commandLengths.put (Protocol.EVENT_FLAG_ATTACKED, 5);
		Protocol.commandLengths.put (Protocol.GET_BATTLE, 5);
		Protocol.commandLengths.put (Protocol.DATA_FLAG_BATTLE, 21);
		Protocol.commandLengths.put (Protocol.EVENT_BATTLE_FINISHED, 5);

		// UPD
		Protocol.commandLengths.put (Protocol.EVENT_LOCATIONS_UPDATE, 128);
		// commandLengths.put(Protocol.EVENT_LOCATION_UPDATE, 128);
		Protocol.commandLengths.put (Protocol.LOCATION_UPDATE, 128);

	}


	public static int length (final byte command)
	{
		Integer i = Protocol.commandLengths.get (command);
		if (i == null)
		{
			return 0;
		}
		else
		{
			return i.intValue ( );
		}
	}

	// Flag States
	public static final byte	StateUnderAttack	= 0;
	public static final byte	StateIdle			= 1;

}
