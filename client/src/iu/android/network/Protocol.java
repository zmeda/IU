package iu.android.network;

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
	//
	// Data accepted and understood
	//
	public static final byte	OK							= 0;

	//
	// Data not accepted
	//
	public static final byte	NOT_ACCEPTED			= 1;

	//
	// Data requested was not found
	//
	public static final byte	NOT_FOUND				= 1;

	//
	// Close the open stream
	//
	public static final byte	CLOSE_STREAM			= 2;

	//
	// Start the game
	//
	public static final byte	START_GAME				= 3;

	//
	// Abort joining the game
	//
	public static final byte	ABORT						= 4;

	//
	// End the current join procedure and switch the server to send mode
	//
	public static final byte	SEND_MODE				= 5;

	//
	// The client asks the server on which UDP port the server is listening
	//
	public static final byte	GET_UDP_SEND_PORT		= 6;
	public static final byte	UDP_SEND_PORT			= 7;
	public static final byte	SET_UDP_SEND_PORT		= 8;

	//
	// The client informs on which UDP port it will listen for server data
	//
	public static final byte	GET_UDP_RECEIVE_PORT	= 9;
	public static final byte	UDP_RECEIVE_PORT		= 10;
	public static final byte	SET_UDP_RECEIVE_PORT	= 11;

	//
	// The client asks for the TCP port opened on the server
	//
	public static final byte	GET_TCP_PORT			= 12;
	public static final byte	TCP_PORT					= 13;
	public static final byte	SET_TCP_PORT			= 14;

	//
	// The color this player will use during the game
	//
	public static final byte	GET_PLAYER_COLOR		= 15;
	public static final byte	PLAYER_COLOR			= 16;
	public static final byte	SET_PLAYER_COLOR		= 17;

	//
	// The name this player will use during the game
	//
	public static final byte	GET_PLAYER_NAME		= 18;
	public static final byte	PLAYER_NAME				= 19;
	public static final byte	SET_PLAYER_NAME		= 20;

	//
	// The client asks for the unique ID this player will have inside the game
	//
	public static final byte	GET_PLAYER_ID			= 21;
	public static final byte	PLAYER_ID				= 22;
	public static final byte	SET_PLAYER_ID			= 23;

	//
	// The client asks for the number of units she/he receives
	//
	public static final byte	GET_NUMBER_OF_UNITS	= 24;
	public static final byte	NUMBER_OF_UNITS		= 25;
	public static final byte	SET_NUMBER_OF_UNITS	= 26;

	//
	// The client asks for the X coordinate of the units (all of them)
	//
	public static final byte	GET_UNIT_X				= 27;
	public static final byte	UNIT_X					= 28;
	public static final byte	SET_UNIT_X				= 29;

	//
	// The client asks for the Y coordinate of the units (all of them)
	//
	public static final byte	GET_UNIT_Y				= 30;
	public static final byte	UNIT_Y					= 31;
	public static final byte	SET_UNIT_Y				= 32;

	//
	// The client asks for the size of the map in which we will play
	//
	public static final byte	GET_MAP_SIZE			= 33;
	public static final byte	MAP_SIZE					= 34;
	public static final byte	SET_MAP_SIZE			= 35;

	//
	// The client asks for the seed of the map in which we will play
	//
	public static final byte	GET_MAP_SEED			= 36;
	public static final byte	MAP_SEED					= 37;
	public static final byte	SET_MAP_SEED			= 38;

	//
	// Used to send and receive information about a specific unit on the battle field
	//
	public static final byte	UNIT_ID					= 39;

	//
	// The attacker player should send this command and the flag ID itself
	//
	public static final byte	FLAG_ID					= 50;

	public static final byte	FINISH_BATTLE			= 51;

	
	//
	//
	// EXPLORE MODE
	//
	//

	public static final byte	GPS_COORDINATES		= 100;

	public static final byte	LOGIN_DATA				= 101;
	public static final byte	SIGNIN_DATA				= 102;

	public static final byte	GET_PLAYERS				= 103;
	public static final byte	PLAYER					= 104;

	public static final byte	GET_COMMANDER			= 105;
	public static final byte	COMMANDER				= 106;

	public static final byte	GET_MAP_INFO			= 107;
	public static final byte	MAP_INFO					= 108;

	public static final byte	GET_FLAGS				= 109;
	public static final byte	FLAG						= 110;

	public static final byte	GET_FOG_OF_WAR			= 111;
	public static final byte	FOG_OF_WAR				= 112;

	public static final byte	DELIMITER				= 127;
}
