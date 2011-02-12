package iu.android.network.battle;

/*************************************************************************************************************
 * IU 1.0b, a java realtime strategy game
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

import iu.android.Debug;
import iu.android.engine.BattlePlayer;
import iu.android.engine.ai.AIPlayer;
import iu.android.network.Protocol;
import iu.android.unit.Unit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

/**
 * Establishes a TCP connection to keep important in-game updates (i.e. unit deaths)
 */
public class BattleClientTCPThread extends Thread
{
	//
	// A constant defining for how long this thread sleeps
	//
	private final static int	SLEEP_TIME		= 500;

	private boolean				running			= false;

	private Socket					socket;

	private DataOutputStream	socketWriter;
	private DataInputStream		socketReader;

	//
	// The local player (ourselves!)
	//
	private LocalPlayer			localPlayer		= null;
	
	//
	// References to all our units that have already died in battle.-
	//
	private ArrayList<Unit>		ourDeadUnits	= new ArrayList<Unit> ( );

	//
	// Unit IDs of dead units that must be sent to the server.-
	// 
	private ArrayList<Integer>	ourDeadUnitIDs	= new ArrayList<Integer> ( );

	
	/**
	 * Checks if any of our units died.
	 * Returns the number of them that have.-
	 */
	private int checkForDeadUnits ( )
	{
		int				 numberOfUpdates = 0;
		ArrayList<Unit> ourUnits 	 	  = this.localPlayer.getUnits ( );
		
		//
		// Check if any of our units died
		//
		for (Unit unit : ourUnits)
		{
			//
			// Did this unit died?
			//
			if (unit.isDead ( ))
			{
				//
				// Have we already registered this dead unit?
				//
				if (!this.ourDeadUnits.contains (unit))
				{
					//
					// Register this unit's death
					//
					this.ourDeadUnits.add 	(unit);
					this.ourDeadUnitIDs.add (new Integer (unit.getID ( )));
					
					//
					// Increment the number of deaths
					//
					numberOfUpdates ++;
				}
			}
		}
		
		//
		// Return the number of new events that happened.-
		//
		return (numberOfUpdates);
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param serverAddress
	 */
	public BattleClientTCPThread (InetAddress serverAddress)
	{
		try
		{
			//
			// Establish a TCP connection to the server
			//
			this.socket = new Socket (serverAddress, JoinBattleClient.ClientTCPThreadPort);
			
			//
			// Extract the streams from the underlying socket
			//
			this.socketWriter = new DataOutputStream (this.socket.getOutputStream ( ));
			this.socketReader = new DataInputStream  (this.socket.getInputStream  ( ));
			
			//
			// We're up!
			//
			this.running = true;
			
			//
			// Save a pointer to the local player for in-class use.
			//
			this.localPlayer = JoinBattleClient.Game.getUser ( );
			
			//
			// Debug only!
			//
			if (Debug.LUCAS)
			{
				Debug.Lucas.println ("*** DEBUG: Created TCP client thread on port " + JoinBattleClient.ClientTCPThreadPort);
			}
		}
		catch (IOException ioe)
		{
			this.running = false;
			Log.e (Debug.TAG, "Failed to connect TCP Thread to server " + serverAddress + ":" + JoinBattleClient.ClientTCPThreadPort);
		}
	}


	@Override
	public void run ( )
	{
		try
		{
			//
			// Keep the thread running as long as we don't receive the halt ( ) method call
			//
			while (this.running)
			{
				/////////////////////////////
				// Send stuff to the server
				/////////////////////////////
				
				//
				// Have any of our units died recently?
				//
				if (this.checkForDeadUnits ( ) > 0)
				{
					//
					// Tell the server, that we are about to send data about a specific number of units
					//
					this.socketWriter.writeByte (Protocol.NUMBER_OF_UNITS);
					this.socketWriter.writeInt  (this.ourDeadUnitIDs.size ( ));
					
					//
					// Send the IDs of the death units
					//
					while (this.ourDeadUnitIDs.size ( ) != 0)
					{
						//
						// Send this unit's ID
						//
						this.socketWriter.writeInt (this.ourDeadUnitIDs.get (0).intValue ( ));

						//
						// Remove this unit's ID from the queue after sending it
						//
						this.ourDeadUnitIDs.remove (0);
					}
				}
				//
				// Check if the battle finished
				//
				else if (JoinBattleClient.Game.hasBeenWon ( ))
				{
					//
					// Did we win?
					//
					if (this.localPlayer.getID ( ) == JoinBattleClient.Game.getWinnerID ( ))
					{
						//
						// Inform the server that the battle has finished
						//
						this.socketWriter.writeByte (Protocol.FINISH_BATTLE);

						//
						// Send the winner's ID
						//
						this.socketWriter.writeInt (this.localPlayer.getID ( ));
						
						//
						// We won, so inform how many units we have left
						//
						this.socketWriter.writeShort (this.localPlayer.getNumberOfHoverJettUnits ( ));
						this.socketWriter.writeShort (this.localPlayer.getNumberOfTankUnits ( ));
						this.socketWriter.writeShort (this.localPlayer.getNumberOfArtilleryUnits ( ));
						
						this.running = false;
						
						//
						// Debug only!
						//
						Log.d ("IU", "Informing the server that we WON the battle");
						
						continue;
					}
					else if (JoinBattleClient.Game.getEnemyUser ( ) instanceof AIPlayer)
					{
						//
						// Inform the server that the battle has finished
						//
						this.socketWriter.writeByte (Protocol.FINISH_BATTLE);
						
						BattlePlayer enemy = JoinBattleClient.Game.getEnemyUser ( );
						
						//
						// Send the winner's ID
						//
						this.socketWriter.writeInt (JoinBattleClient.Game.getEnemyUser ( ).getID ( ));
						
						//
						// We lost, so inform how many units the winner has left
						//
						this.socketWriter.writeShort (enemy.getNumberOfHoverJettUnits ( ));
						this.socketWriter.writeShort (enemy.getNumberOfTankUnits ( ));
						this.socketWriter.writeShort (enemy.getNumberOfArtilleryUnits ( ));
						
						this.running = false;

						//
						// Debug only!
						//
						Log.d ("IU", "Informing the server that we LOST the battle");

						continue;
					}
				}
				else
				{
					//
					// Inform the server that nothing new happened ...
					// This is used as a ping client by the server, so that it knows that we are still connected.-
					//
					this.socketWriter.writeByte (Protocol.OK);
				}

				//////////////////////////////////
				// Receive stuff from the server
				//////////////////////////////////
				byte command = this.socketReader.readByte ( );

				//
				// Decide what to do, based on the command received ...
				//
				switch (command)
				{
					//
					// The server is informing us about units that other players lost
					//
					case (Protocol.NUMBER_OF_UNITS):
						//
						// Receive the number of units that died since the last report
						//
						int numberOfDeadUnits = this.socketReader.readInt ( );
					
						//
						// Receive data for each dead unit
						//
						for (int i = 0 ; i < numberOfDeadUnits; i ++)
						{
							//
							// Receive the unit owner's ID (i.e. player ID)
							//
							int ownerID = this.socketReader.readInt ( );
							
							//
							// Receive the ID for this dead unit
							//
							int deadUnitID = this.socketReader.readInt ( );

							//
							// Update game state
							//
							BattlePlayer enemy 	  = JoinBattleClient.Game.getPlayerByID (ownerID);
							Unit 			 enemyUnit = enemy.getUnit 		 	 				 (deadUnitID);
							
							enemy.killUnit (enemyUnit);
							
							//
							// Debug only!
							//
							if (Debug.LUCAS)
							{
								Log.d ("IU", "BattleClientTCPThread -> Player " + ownerID + " lost unit " + deadUnitID);
							}
						}
						break;
						
					//
					// The server is informing us that nothing new happened.-
					//
					case (Protocol.OK):
						//
						// We use this command to keep track of the connection.
						// As soon as we don't receive anything for some time, we imply that we lost connection with the server.-
						//
						break;
				}

				//
				// Go to sleep for a while not to consume all processor power ...
				//
				try
				{
					Thread.sleep (BattleClientTCPThread.SLEEP_TIME);
				}
				catch (InterruptedException ie)
				{
					//
					// We were interrupted while waiting ... so what?
					//
				}
			}

			//
			// Close this socket  ...
			//
			try
			{
				this.socket.close ( );
			}
			catch (IOException ioe)
			{
				//
				// Socket was already closed ... it is safe to ignore this exception
				//
			}
		}
		catch (IOException ioe)
		{
			//
			// FIXME: (Lucas) Connection to the server was lost, we must inform the user!!
			//
			Log.e ("IU", "BattleClientTCPThread lost connection to the server!");
		}
	}
}
