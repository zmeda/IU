package iu.server.battle;

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

import iu.android.network.Protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * This thread handles the essential in-game updates to/from a client.
 */
public class BattleServerTCPThread extends Thread
{
	private BattleServerPlayer	player				= null;
	private boolean				running				= false;

	private int						portNumber			= -1;

	private DataOutputStream	socketWriter;
	private DataInputStream		socketReader;

	private Vector<Integer>		deadUnitsReceived	= new Vector<Integer> ( );
	private Vector<String>		deadUnitsToSend	= new Vector<String> ( );

	//
	// A timer to keep track of the last time we received data from the client.
	// It replaces the ping architecture.-
	//
	private long					lastTimestamp		= System.currentTimeMillis ( );
	private long					pingTime				= -1;


	/**
	 * Constructor
	 * 
	 * @param port
	 * @param p
	 */
	public BattleServerTCPThread (int port, BattleServerPlayer p)
	{
		//
		// Get a reference to the player
		//
		this.player = p;
		
		//
		// Save the port on which we will wait for the client
		//
		this.portNumber = port;

		//
		// Low priority because we don't need lots of processing power
		//
		this.setPriority (Thread.MIN_PRIORITY);
		
		//
		// Start the server
		//
		this.start ( );
	}


	/**
	 * Receive and send important data from/to client.-
	 */
	@Override
	public void run ( ) 
	{
		ServerSocket tcpThreadServer = null;
		
		try
		{
			//
			// Create the TCP server socket
			//
			tcpThreadServer = new ServerSocket (this.portNumber);

			//
			// We're up!
			//
			this.running = true;
			
			//
			// Wait for a client to call ... 
			// This should happen after the join session has been completed.-
			//
			Socket serverSocket = tcpThreadServer.accept ( );

			//
			// We've got a connection to the calling client, so go ahead and get streams.-
			//
			this.socketWriter = new DataOutputStream (serverSocket.getOutputStream ( ));
			this.socketReader = new DataInputStream  (serverSocket.getInputStream  ( ));
		}
		catch (IOException ioe)
		{
			this.running = false;
			System.err.println  ("*** ERROR: ServerTCPThread not created!");
			ioe.printStackTrace ( );
		}

		try
		{
			//
			// Keep the thread running as long as we don't receive the halt ( ) method call
			//
			while (this.running)
			{
				/////////////////////////////////////////
				// Receive stuff from the client
				/////////////////////////////////////////
				byte command = this.socketReader.readByte ( );
				
				//
				// Decide what to do, based on the command received ...
				//
				switch (command)
				{
					//
					// The client is informing about units that died
					//
					case (Protocol.NUMBER_OF_UNITS):
						//
						// Receive the number of units that died since the last report
						//
						int numberOfDeadUnits = this.socketReader.readInt ( );
					
						//
						// Receive the IDs of each dead unit
						//
						for (int i = 0 ; i < numberOfDeadUnits; i ++)
						{
							this.deadUnitsReceived.add (new Integer (this.socketReader.readInt ( )));
							
							if (Debug.LUCAS)
							{
								System.out.println ("*** DEBUG: Player " + this.player.getID ( ) + " lost unit " + this.deadUnitsReceived.lastElement ( ));
							}
						}
						
						//
						// Update the last connection time (a.k.a. ping server)
						//
						this.pingTime 		 = System.currentTimeMillis ( ) - this.lastTimestamp;
						this.lastTimestamp = System.currentTimeMillis ( );

						break;
						
					case (Protocol.FINISH_BATTLE):
						//
						// A client is informing that he won the battle.
						// We'll check the ID.
						//
						int id = this.socketReader.readInt ( );
						if (id == this.player.getID ( ))
						{
							//
							// Read out how many units he has left
							//
							this.player.setNumberOfHoverjets (this.socketReader.readShort ( ));
							this.player.setNumberOfTanks     (this.socketReader.readShort ( ));
							this.player.setNumberOfArtillery (this.socketReader.readShort ( ));
							
							//
							// Inform the explore server about this
							//
							this.player.getClient ( ).getBattleListenServer ( ).informAboutWinner (this.player);

							//
							// Debug only!
							//
							System.out.println ("*** DEBUG: Battle finished. Winner is " + this.player.getName ( ));
						}
						else
						{
							BattleServerPlayer enemy = new BattleServerPlayer();
							enemy.setID(id);
							
							enemy.setNumberOfHoverjets (this.socketReader.readShort ( ));
							enemy.setNumberOfTanks     (this.socketReader.readShort ( ));
							enemy.setNumberOfArtillery (this.socketReader.readShort ( ));

							//
							// Inform the explore server about this
							//
							this.player.getClient ( ).getBattleListenServer ( ).informAboutWinner (enemy);

							//
							// Debug only!
							//
							System.out.println ("*** DEBUG: Battle finished. Winner is " + enemy.getName ( ));
						}
						this.running = false;
						break;
					
					//
					// The client is informing us that nothing new happened.-
					//
					case (Protocol.OK):
						//
						// We use this command to keep track of the client.
						// As soon as we don't receive anything for some time, we imply that the client is gone.-
						//
						//
						// Update the last connection time (a.k.a. ping server)
						//
						this.pingTime 	    = System.currentTimeMillis ( ) - this.lastTimestamp;
						this.lastTimestamp = System.currentTimeMillis ( );

						break;

					//
					// A handler for unknown commands.-
					//
					default:
						System.err.println ("*** WARNING: Ignored unknown command <" + command + ">");
				}

				/////////////////////////////////////
				// Send stuff to the client
				/////////////////////////////////////
				
				//
				// Is there anything new to send?
				//
				if (this.deadUnitsToSend.size ( ) > 0)
				{
					//
					// Tell the client, that we are about to send data about a specific number of units
					//
					this.socketWriter.writeByte (Protocol.NUMBER_OF_UNITS);
					this.socketWriter.writeInt  (this.deadUnitsToSend.size ( ));
					
					//
					// Send the player ID and the death unit ID, for each dead unit
					//
					while (!this.deadUnitsToSend.isEmpty ( ))
					{
						String playerID = this.deadUnitsToSend.get (0).split (":") [0];
						String unitID   = this.deadUnitsToSend.get (0).split (":") [1];
						
						//
						// Send the player ID of the unit's owner
						//
						this.socketWriter.writeInt (Integer.valueOf (playerID).intValue ( ));
						
						//
						// Send this unit's ID
						//
						this.socketWriter.writeInt (Integer.valueOf (unitID).intValue ( ));

						//
						// Remove this unit from the queue after sending it
						//
						this.deadUnitsToSend.remove (0);
					}
				}
				else
				{
					//
					// Inform the client that nothing new happened ...
					// This is used as a ping server, so that we know the client is still there.-
					//
					this.socketWriter.writeByte (Protocol.OK);
				}
			}
			
			//
			// Close the opened socket
			//
			if (tcpThreadServer != null)
			{
				tcpThreadServer.close ( );
			}
		}
		catch (Exception e)
		{
			System.err.println  ("*** WARNING: Connection to client " + this.player.getID ( ) + " has been lost!");
			System.err.println  ("*** WARNING: Setting default number of units (3, 2, 1) and winner ...");

			this.player.setNumberOfHoverjets ((short) 3);
			this.player.setNumberOfTanks     ((short) 2);
			this.player.setNumberOfArtillery ((short) 1);
			
			//
			// Inform the explore server about this
			//
			this.player.getClient ( ).getBattleListenServer ( ).informAboutWinner (this.player);
		}
		
		//
		// Debug only!
		//
		if (Debug.LUCAS)
		{
			System.out.println ("*** DEBUG: BattleServerTCPThread for player " + this.player.getID ( ) + " halted on port " + this.portNumber); 
		}
	}


	/**
	 * Stop this server
	 */
	public void halt ( )
	{
		this.running = false;
	}


	/**
	 * Called by server game thread. Returns null if there are none left.
	 */
	public String getNextUnitDeath ( )
	{

		if (this.deadUnitsReceived.size ( ) == 0)
		{
			return null;
		}
		else
		{
			//
			// Build the return value, using our player ID as prefix
			//
			String ret_value = String.valueOf (this.player.getID ( )) + ":" + String.valueOf (this.deadUnitsReceived.firstElement ( ));
			
			//
			// Remove this data, because it has been just informed
			//
			this.deadUnitsReceived.removeElementAt (0);

			//
			// Return
			//
			return ret_value;
		}
	}


	/**
	 * Returns TRUE if there are dead units yet to be informed.-
	 * 
	 * @return
	 */
	public boolean hasMoreUnitDeaths ( )
	{
		return (!this.deadUnitsReceived.isEmpty ( ));
	}


	/**
	 * Sends information about other players' dead units to this player.-
	 * 
	 * @param s
	 */
	public void sendDeadUnitData (String s)
	{
		this.deadUnitsToSend.addElement (s);
	}


	/**
	 * Returns the last time we received anything from the client.
	 * Used to keep the client connection alive (a.k.a. ping server).-
	 * 
	 * @return
	 */
	public long getPingTime ( )
	{
		return (this.pingTime);
	}
}
