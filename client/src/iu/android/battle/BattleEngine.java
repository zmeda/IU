package iu.android.battle;

import iu.android.army.ArmyRegistry;
import iu.android.army.DefaultArmy;
import iu.android.army.PluggableArmy;
import iu.android.engine.BattlePlayer;
import iu.android.engine.ai.AIPlayer;
import iu.android.explore.Player;
import iu.android.map.Map;
import iu.android.network.battle.LocalPlayer;
import iu.android.order.AttackUnitOrder;
import iu.android.unit.Squad;
import iu.android.unit.Unit;
import iu.android.util.Random;

import java.util.HashMap;
import java.util.Vector;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * The battle engine exports the functionality in the game engine (physics, sprites, ...)
 * to the Battle Activity.-
 */
public class BattleEngine
{
	// Used to get the player by name
	private HashMap<String, BattlePlayer>	playersMap	= new HashMap<String, BattlePlayer> ( );
	private Vector<BattlePlayer>				playersById	= new Vector<BattlePlayer> ( );
	private Vector<BattlePlayer>				playersVec	= new Vector<BattlePlayer> ( );

	private Map										map			= null;
	private LocalPlayer							user			= null;

	private boolean								bHasBeenWon	= false;
	private int										nWinnerID	= -1;

	private boolean								bIsStarted	= false;

	private Handler								battleActivityMessageHandler;


	/**
	 * Constructor
	 * 
	 * @param activity
	 * @param gameGUI
	 */
	public BattleEngine (final Handler messageHandler)
	{
		this.battleActivityMessageHandler = messageHandler;
		AttackUnitOrder.setGame (this);
	}


	public void setMap (final Map map)
	{
		this.map = map;
	}


	public Map getMap ( )
	{
		return this.map;
	}


	public void setUser (final LocalPlayer user)
	{
		this.user = user;
	}


	public LocalPlayer getUser ( )
	{
		return this.user;
	}


	/**
	 * Returns the player against which this battle is being fought.-
	 * 
	 * @return
	 */
	public BattlePlayer getEnemyUser ( )
	{
		if (this.playersVec.size ( ) != 2)
		{
			Log.e ("IU - BattleEngine", "Player vector contains " + Integer.toString (this.playersVec.size ( )) + " elements. It should contain 2!");
			return null;
		}
		else
		{
			if (this.playersVec.elementAt (0) == this.user)
			{
				return this.playersVec.elementAt (1);
			}
			else
			{
				return this.playersVec.elementAt (0);
			}
		}

	}


	public void addPlayer (final BattlePlayer player)
	{
		this.playersMap.put (player.getName ( ), player);

		this.playersVec.add (player);

		if (player.getID ( ) >= this.playersById.size ( ))
		{
			this.playersById.setSize (player.getID ( ) + 1);
		}

		this.playersById.set (player.getID ( ), player);
	}


	public int getNumPlayers ( )
	{
		return this.playersVec.size ( );
	}


	public BattlePlayer getPlayer (final String playerName)
	{
		return this.playersMap.get (playerName);
	}


	public BattlePlayer getPlayer (final int idx)
	{
		return this.playersVec.get (idx);
	}


	public BattlePlayer getPlayerByID (final int id)
	{
		return this.playersById.get (id);
	}


	public void clearGame ( )
	{
		this.playersMap.clear ( );
		// this.map = null;
		// this.user = null;
	}


	/**
	 * To be rewritten for different game types.
	 * Called by the BattleThread.-
	 */
	public void checkForWinners ( ) 
	{
		if (!this.bHasBeenWon)
		{
			int playerCount 						= this.playersVec.size ( );
			int numberOfPlayersWithUnitsLeft = 0;
			BattlePlayer 			 lastPlayer = null;

			//
			// Count the number of players that are still playing
			//
			for (int i = 0; i < playerCount; i ++)
			{
				BattlePlayer p = this.playersVec.get (i);

				if (p != null && p.isParticipating ( ) && p.getNumUnitsAlive ( ) > 0)
				{
					numberOfPlayersWithUnitsLeft ++;
					lastPlayer = p;
				}
			}

			//
			// If only one player still has units left ...
			//
			if (numberOfPlayersWithUnitsLeft == 1 && lastPlayer != null)
			{
				this.nWinnerID = lastPlayer.getID ( );

				if (!this.bHasBeenWon)
				{
					Log.i ("IU", " WINNER: " + lastPlayer.getName ( ));
					Log.i ("IU", "   G A M E   O V E R");
				}

				this.bHasBeenWon = true;
			}
		}
	}




	/**
	 * Adds a human player's army
	 * 
	 * @param map
	 */
	public void addPlayerArmy (final BattlePlayer player, final int hNum, final int tNum, final int aNum)
	{
		PluggableArmy playerArmy = new DefaultArmy (hNum, tNum, aNum);

		ArmyRegistry.addArmy (playerArmy);

		playerArmy.createArmy (player, this.map);
	}


	/**
	 * Adds a computer controlled army.-
	 * 
	 * @param exPlayer
	 * @param hNum
	 * @param tNum
	 * @param aNum
	 * @returns A reference to the AIPlayer created
	 */
	public AIPlayer addAiArmy (final Player exPlayer, final int hNum, final int tNum, final int aNum)
	{
		// Make army and attach AI
		PluggableArmy compArmy = new DefaultArmy (hNum, tNum, aNum);

		ArmyRegistry.addArmy (compArmy);

		// AIPlayer ai_player = compArmy.newAI((byte) armyId);
		AIPlayer aiPlayer = compArmy.newAI (exPlayer);

		// ai_player.setColor(rgb);

		compArmy.createArmy (aiPlayer, this.map);

		this.addPlayer (aiPlayer);

		//
		// Return the AIPlayer just created
		//
		return (aiPlayer);
	}


	/**
	 * Sets the initial positions of the player specified
	 * 
	 * @param player
	 * @param coord
	 *           an array of coordinates, the second dimension is 2, indicating X or Y.-
	 */
	public void setupPlayerUnitPositions (final BattlePlayer player, int[][] coord)
	{
		int unitCount = player.getNumUnits ( );

		for (int unitIdx = 0; unitIdx < unitCount; unitIdx++)
		{
			Unit unit = player.getUnit (unitIdx);

			unit.setPosition (coord[unitIdx][0], coord[unitIdx][1]);
		}
	}


	/**
	 * Sets the initial positions of the player specified
	 * 
	 * @param player
	 * @param coordX
	 *           an array of X coordinates
	 * @param coordY
	 *           an array of Y coordinates
	 */
	public void setupPlayerUnitPositions (final BattlePlayer player, int[] coordX, int[] coordY)
	{
		int unitCount = player.getNumUnits ( );

		for (int unitIdx = 0; unitIdx < unitCount; unitIdx++)
		{
			Unit unit = player.getUnit (unitIdx);

			unit.setPosition (coordX[unitIdx], coordY[unitIdx]);
		}
	}


	/**
	 * Sets random positions for the AI-player's units, based on the map area selected with X and Y.-
	 * 
	 * @param aiPlayer
	 * @param x
	 *           The X coordinate area of the map (from 0 to 2)
	 * @param y
	 *           The Y coordinate area of the map (from 0 to 2)
	 */
	public void setupRandomAIPlayerUnitPositions (final AIPlayer aiPlayer, int x, int y)
	{
		// Bounds of the area where to put the units
		int minx = this.map.pixelCount ( ) * x / 3;
		int miny = this.map.pixelCount ( ) * y / 3;

		int maxx = this.map.pixelCount ( ) * (x + 1) / 3 - 1;
		int maxy = this.map.pixelCount ( ) * (y + 1) / 3 - 1;

		int width = maxx - minx;
		int height = maxy - miny;

		int squadCount = aiPlayer.getNumSquads ( );

		for (int squadIdx = 0; squadIdx < squadCount; squadIdx++)
		{
			Squad squad = aiPlayer.getSquad (squadIdx);

			int unitNum = squad.getNumUnits ( );

			if (unitNum <= 0)
			{
				continue;
			}

			// All units in the squad are the same size
			float r = squad.getUnit (0).getBounds ( ).radius;

			// How long a line of units in formation is going to be ( sqrt(num)*2 )
			int numSquared = 2 * (int) Math.ceil (Math.sqrt (unitNum));

			// Num of pixels needed to place them
			int squadDim = (int) (numSquared * 3 * r);

			// Get the squad position so it won't go over the edge of the map
			int squad_x = minx + (int) ((width - squadDim) * Random.randomFloat ( ));
			int squad_y = miny + (int) ((height - squadDim) * Random.randomFloat ( ));

			int unit_x = squad_x;
			int unit_y = squad_y;

			int unitIdx = 0;

			while (unitIdx < unitNum)
			{
				Unit unit = squad.getUnit (unitIdx);

				unit.setPosition (unit_x, unit_y);

				unitIdx++;

				if (unitIdx % numSquared == 0)
				{
					// next row
					unit_y += 3 * r;
					// Begining of row
					unit_x = squad_x;
				}
				else
				{
					// Next unit in row
					unit_x += 3 * r;
				}
			}
		}
	}


	/**
	 * Sets player's units positions, based on the (x, y) point received.- 
	 */
	public void setupPlayerUnitPositions (final BattlePlayer player, int x, int y)
	{
		// Bounds of the area where to put the units
		int minx = this.map.pixelCount ( ) * x / 3;
		int miny = this.map.pixelCount ( ) * y / 3;

		int maxx = this.map.pixelCount ( ) * (x + 1) / 3 - 1;
		int maxy = this.map.pixelCount ( ) * (y + 1) / 3 - 1;

		int width = maxx - minx;
		int height = maxy - miny;

		int squadCount = player.getNumSquads ( );

		for (int squadIdx = 0; squadIdx < squadCount; squadIdx++)
		{
			Squad squad = player.getSquad (squadIdx);

			int unitNum = squad.getNumUnits ( );

			if (unitNum <= 0)
			{
				continue;
			}

			// All units in the squad are the same size
			float r = squad.getUnit (0).getBounds ( ).radius;

			// How long a line of units in formation is going to be ( sqrt(num)*2 )
			int numSquared = 2 * (int) Math.ceil (Math.sqrt (unitNum));

			// Num of pixels needed to place them
			int squadDim = (int) (numSquared * 3 * r);

			// Get the squad position so it won't go over the edge of the map
			int squad_x = minx + (int) ((width - squadDim) * Random.randomFloat ( ));
			int squad_y = miny + (int) ((height - squadDim) * Random.randomFloat ( ));

			int unit_x = squad_x;
			int unit_y = squad_y;

			int unitIdx = 0;

			while (unitIdx < unitNum)
			{
				Unit unit = squad.getUnit (unitIdx);

				unit.setPosition (unit_x, unit_y);

				unitIdx++;

				if (unitIdx % numSquared == 0)
				{
					// next row
					unit_y += 3 * r;
					// Begining of row
					unit_x = squad_x;
				}
				else
				{
					// Next unit in row
					unit_x += 3 * r;
				}
			}
		}
	}


	/**
	 * called by whatever needs to know (game canvas)
	 * 
	 * @return
	 */
	public boolean hasBeenWon ( )
	{
		return this.bHasBeenWon;
	}


	/**
	 * called by whatever needs to know (game canvas)
	 * 
	 * @return
	 */
	public int getWinnerID ( )
	{
		return this.nWinnerID;
	}


	public void startGame ( )
	{
		this.bIsStarted = true;
	}


	public boolean isStarted ( )
	{
		return this.bIsStarted;
	}

	
	/**
	 * Informing the battle activity that the battle has finished.-
	 *  
	 * TODO - This should be sent through the communication to the server
	 */
	public void endGame ( )
	{
		Message msg = this.battleActivityMessageHandler.obtainMessage (BattleActivity.SHOW_BATTLE_RESULT, 
																							this.getPlayerByID (this.nWinnerID));
		this.battleActivityMessageHandler.sendMessage (msg);
	}


	/**
	 * @return the battleActivityMessageHandler
	 */
	public Handler getBattleActivityMessageHandler ( )
	{
		return this.battleActivityMessageHandler;
	}
}
