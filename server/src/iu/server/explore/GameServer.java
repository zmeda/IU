package iu.server.explore;

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

import java.util.Vector;

/**
 * Implementation of the game from the server's point of view. This class contains all the game state variables needed by the server.
 */
public class GameServer
{
	//
	// The initial number of units each player receives.-
	//
	protected final static int	INITIAL_NUMBER_OF_UNITS	= 20;

	//
	// A flag that indicates whether the game has started or not.-
	//
	private static boolean	   started	                = false;

	//
	// A flag that indicates whether the game has finished or not.-
	//
	private static boolean	   finished	                = false;

	//
	// A vector containing all the players in this game.-
	//
	private static Vector<ServerPlayer> players         = new Vector<ServerPlayer>();

	/**
	 * Constructor
	 */
	private GameServer()
	{
		// Nothing to do!
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isJoinable()
	{
		return (!GameServer.started && !GameServer.finished);
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isRunning()
	{
		return (GameServer.started && !GameServer.finished);
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isFinished()
	{
		return GameServer.finished;
	}

	/**
	 * Signal that a game has started (no more people can join)
	 */
	public static void startGame()
	{
		GameServer.started = true;
	}

	/**
	 * Signal that the game has stopped and is finished.
	 */
	public static void stopGame()
	{
		GameServer.finished = true;
	}

	/**
	 * Join a player to the current game.-
	 * 
	 * @param player
	 * @return
	 */
	public static boolean addPlayer(ServerPlayer player)
	{
		if (!GameServer.started && !GameServer.finished)
		{
			GameServer.players.addElement(player);
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns a vector of players currently playing.-
	 * 
	 * @return
	 */
	public static Vector<ServerPlayer> getPlayers()
	{
		return GameServer.players;
	}

}
