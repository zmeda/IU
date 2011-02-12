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
import iu.android.battle.BattleEngine;
import iu.android.explore.Commander;
import iu.android.explore.ExplorePlayer;
import iu.android.explore.Flag;
import iu.android.network.Protocol;
import iu.android.network.RemotePlayer;
import iu.android.order.Order;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import android.util.Log;

/**
 * This class is the network client each player uses when (s)he wants to play a networked battle.- 
 */
public class JoinBattleClient
{
	//
	// This client's character encoding. It must match the one on the server side.-
	//
	protected final static String			ENCODING					= "US-ASCII";

	//
	// The size of a datagram (UDP) packet. It must match the size on the server side.-
	//
	public final static int					DATA_PACKET_SIZE		= 128;

	//	
	// Always stays the same (as long as you only run 1 client per machine)
	//
	protected static int						ClientUDPListenPort	= 4505;

	//
	// Always stays the same (as long as you only run 1 client per machine)
	//
	protected static int						ClientUDPSendPort		= 4510;

	//
	// We receive this value from the server.
	// It is unique for each client.
	//
	protected static int						BattleServerUDPPort	= -1;

	//
	// We receive this value from the server.
	// It is unique for each client.
	//
	protected static int						ClientTCPThreadPort	= -1;

	//
	// A reference to the commander player
	//
	protected static Commander				Commander				= null;

	//
	// A reference to the flag in conflict
	//
	protected static Flag					Flag						= null;

	//
	// This user's configuration file
	//
	protected static Properties			Configuration			= null;

	//
	// Default network settings, if not changed by user's configuration file
	//
	public static int							ConnectionSpeed		= 2400;
	public static int							PacketsPerSecond		= JoinBattleClient.ConnectionSpeed / (JoinBattleClient.DATA_PACKET_SIZE * 8);
	public static int							InterpacketDelay		= 1000 / JoinBattleClient.PacketsPerSecond;

	//
	// The input queue in which we save stuff received from the server
	//
	protected static ArrayList<Order>	INqueue					= new ArrayList<Order> ( );

	//
	// The output queue in which we save stuff for the server
	//
	protected static ArrayList<Order>	OUTqueue					= new ArrayList<Order> ( );

	//
	// The send and receive sockets that work over UDP
	//
	protected static DatagramSocket		UDPSendSocket			= null;
	protected static DatagramSocket		UDPListenSocket		= null;

	//
	// A reference to the game engine
	//
	protected static BattleEngine			Game						= null;

	private DataOutputStream				socketWriter			= null;
	private DataInputStream					socketReader			= null;

	private ReceiverBattleThread			receiverThread			= null;
	private SenderBattleThread				senderThread			= null;

	private InetAddress						serverAddress			= null;

	private Socket								clientSocket			= null;

	//
	// This array holds the units' coordinates as received from the server
	//
	private int[][]							unitCoordinates		= null;

	private BattleClientTCPThread			clientTCPThread		= null;

	//
	// Our enemy's player ID
	//
	private int									enemyPlayerID			= -1;


	/**
	 * Clean up all the static references
	 */
	private void cleanUp ( )
	{
		//
		// We receive this value from the server.
		// It is unique for each client.
		//
		JoinBattleClient.BattleServerUDPPort = -1;

		//
		// We receive this value from the server.
		// It is unique for each client.
		//
		JoinBattleClient.ClientTCPThreadPort = -1;

		//
		// A reference to the commander player
		//
		JoinBattleClient.Commander = null;

		//
		// A reference to the flag in conflict
		//
		JoinBattleClient.Flag = null;

		//
		// The input queue in which we save stuff received from the server
		//
		JoinBattleClient.INqueue = new ArrayList<Order> ( );

		//
		// The output queue in which we save stuff for the server
		//
		JoinBattleClient.OUTqueue = new ArrayList<Order> ( );

		//
		// The send and receive sockets that work over UDP
		//
		JoinBattleClient.UDPSendSocket = null;
		JoinBattleClient.UDPListenSocket = null;

		//
		// A reference to the game engine
		//
		JoinBattleClient.Game = null;
	}
	
	
	/**
	 * Constructor
	 */
	public JoinBattleClient ( )
	{
		//
		// Nothing to do!
		//
	}


	/**
	 * Returns true if server address is valid, and the join session completes successfully.-
	 * 
	 * @param serverIP
	 * @return
	 */
	public boolean joinToServer (String serverIP, int serverPort)
	{
		//
		// Be sure we already have a game context
		//
		if (JoinBattleClient.Game != null)
		{
			try
			{
				//
				// Connect to the server
				//
				this.serverAddress 	= InetAddress.getByName (serverIP);
				
				this.clientSocket 	= new Socket (this.serverAddress, serverPort);
				
				this.socketWriter 	= new DataOutputStream (this.clientSocket.getOutputStream ( ));
				this.socketReader 	= new DataInputStream  (this.clientSocket.getInputStream  ( ));

				//
				// Start the join session.-
				//
				//
				// Inform the server on which UDP port we receive data.
				//
				this.socketWriter.writeByte (Protocol.SET_UDP_RECEIVE_PORT);
				this.socketWriter.writeInt  (JoinBattleClient.ClientUDPListenPort);
					
				//
				// Ask the server the UDP port it is receiving data ...
				//
				this.socketWriter.writeByte (Protocol.GET_UDP_SEND_PORT);
				
				//
				// ... and save the answer.
				//
				if (this.socketReader.readByte ( ) == Protocol.SET_UDP_SEND_PORT)
				{
					JoinBattleClient.BattleServerUDPPort = this.socketReader.readInt ( );
				}
				else
				{
					//
					// Unknown answer received
					//
					Log.w (Debug.TAG, "Unknown answer received in JoinBattleClient");
				}

				//
				// Ask the server which TCP port it has reserved for us ...
				//
				this.socketWriter.writeByte (Protocol.GET_TCP_PORT);
				
				//
				// ... and save the answer.
				//
				if (this.socketReader.readByte ( ) ==  Protocol.SET_TCP_PORT)
				{
					JoinBattleClient.ClientTCPThreadPort = this.socketReader.readInt ( ); 
				}
				else
				{
					//
					// Unknown answer received
					//
					Log.w (Debug.TAG, "Unknown answer received in JoinBattleClient");
				}
					
				//
				// Send our name ...
				//
				this.socketWriter.writeByte  (Protocol.SET_PLAYER_NAME);
				this.socketWriter.writeByte  (JoinBattleClient.Commander.getPlayer ( ).getName ( ).length ( ));
				this.socketWriter.writeBytes (JoinBattleClient.Commander.getPlayer ( ).getName ( ));
				
				//
				// ... and see what the server thinks about it.
				//
				if (this.socketReader.readByte ( ) == Protocol.NOT_ACCEPTED)
				{
					//
					// The name has not been accepted by the server
					//
					Log.w (Debug.TAG, "The name " + JoinBattleClient.Commander.getPlayer ( ).getName ( ) + " has been rejected by the server.");
				}
				
				//
				// Send our ID
				//
				this.socketWriter.writeByte (Protocol.SET_PLAYER_ID);
				this.socketWriter.writeInt  (JoinBattleClient.Commander.getPlayer ( ).getId ( ));

				//
				// Send the number of units we wish to use in this battle ...
				//
				this.socketWriter.writeByte (Protocol.SET_NUMBER_OF_UNITS);

				this.socketWriter.writeShort (JoinBattleClient.Commander.getHovercraftCount ( ));
				this.socketWriter.writeShort (JoinBattleClient.Commander.getTankCount ( ));
				this.socketWriter.writeShort (JoinBattleClient.Commander.getArtilleryCount ( ));

				//
				// ... and see what the server thinks about it.
				//
				if (this.socketReader.readByte ( ) == Protocol.NOT_ACCEPTED)
				{
					//
					// The number if units has not been accepted by the server
					//
					Log.w (Debug.TAG, "Our units have been rejected by the server.");
				}
				else
				{
					this.unitCoordinates = new int [JoinBattleClient.Commander.getTotalNumberOfUnits ( )] [2];
				}

				//
				// Ask the server for our units' X coordinates ... 
				//
				this.socketWriter.writeByte (Protocol.GET_UNIT_X);
				
				//
				// ... start receiving the X coordinate for each unit.
				//
				for (int i = 0; i < JoinBattleClient.Commander.getTotalNumberOfUnits ( ); i ++)
				{
					this.unitCoordinates[i][0] = this.socketReader.readInt ( );
					
					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Log.i (Debug.TAG, "X coordinate received: " + this.unitCoordinates[i][0]);
					}
				}

				//
				// Ask the server for our units' Y coordinates ... 
				//
				this.socketWriter.writeByte (Protocol.GET_UNIT_Y);
				
				//
				// ... and receive the Y coordinate for each unit.
				//
				for (int i = 0; i < JoinBattleClient.Commander.getTotalNumberOfUnits ( ); i ++)
				{
					this.unitCoordinates[i][1] = this.socketReader.readInt ( );

					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Log.i (Debug.TAG, "Y coordinate received: " + this.unitCoordinates[i][1]);
					}
				}

				//
				// Send the ID of the flag we wish to attack.
				//
				this.socketWriter.writeByte (Protocol.FLAG_ID);
				this.socketWriter.writeInt  (JoinBattleClient.Flag.getFlagId ( ));
				
				//
				// Finish the join session
				//
				this.socketWriter.writeByte (Protocol.SEND_MODE);

				//
				// Debug only!
				// 
				if (Debug.LUCAS)
				{
					Log.i (Debug.TAG, "*** DEBUG: Switched to SEND mode ...");
				}
				
				//
				// Join session finished successfully!
				//
				return (true);
			
			}
			catch (UnknownHostException uhe)
			{
				Log.e  (Debug.TAG, "Server address " + serverIP + " could not be resolved.");
				return (false);
			}
			catch (IOException ioe)
			{
				Log.e (Debug.TAG, "Server at " + serverIP + " is not responding. Join failed.");
				return (false);
			}
		}
		else
		{
			Log.e (Debug.TAG, "Client has no game context. JoinBattleClient.setGame ( ) has not yet been called!");
			return (false);
		}
	}


	/**
	 * Waits the order from the server to start the game.
	 * Receives information about other players' units while waiting.-
	 * 
	 * @throws IOException
	 */
	public void receiveOpponentData ( ) throws IOException
	{
		boolean 		  	notStarted 			= true;
		ExplorePlayer 	enemyPlayer 		= null;
		RemotePlayer 	remoteEnemyPlayer = null;
		int   			numberOfUnits		= 0;
		short 			hoverjetUnits		= -1; 	
		short 			tankUnits			= -1;
		short 			artilleryUnits		= -1;
		int [] 			unitCoordinatesX  = null;
		int [] 			unitCoordinatesY  = null;
		
		//
		// Wait here until the server instructs us to start fighting.-
		//
		while (notStarted)
		{
			//
			// The next line blocks execution until a command is received from the server
			//
			final byte command = this.socketReader.readByte ( );

			//
			// Decide what to do, based on the command just received
			//
			switch (command)
			{
				//
				// The server is informing us that the battle started
				//
				case (Protocol.START_GAME):
					//
					// Change the flag value to stop waiting
					//
					notStarted = false;
					
					//
					// Start the battle 
					//
					JoinBattleClient.Game.startGame ( );
					
					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Debug.Lucas.println ("----  F I G H T  ----");	
					}
					break;
					
				//
				// Enemy player's ID
				//
				case (Protocol.SET_PLAYER_ID):
					//
					// Save the enemy's ID
					// 
					this.enemyPlayerID = this.socketReader.readInt ( );

					//
					// Create a new enemy player
					//
					enemyPlayer = new ExplorePlayer (this.enemyPlayerID);
					
					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Debug.Lucas.println ("*** DEBUG: Enemy player's ID is " + enemyPlayer.getId ( ));
					}
					break;

				//
				// Receive enemy player's name
				//
				case (Protocol.SET_PLAYER_NAME):
					//
					// Get the string length and create a buffer to hold it
					//
					int stringLength = this.socketReader.readByte ( );
					byte [] buffer	  = new byte [stringLength];

					//
					// Read the player's name and save it
					//
					this.socketReader.readFully (buffer);
					String playerName = new String (buffer);
					
					enemyPlayer.setName (playerName);
					
					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Debug.Lucas.println ("*** DEBUG: Enemy player's name is " + enemyPlayer.getName ( ));
					}
					break;

				//
				// Receive enemy player's color
				//
				case (Protocol.SET_PLAYER_COLOR):
					//
					// Enemy players are always RED colored. We just ignore this data.-
					//
					enemyPlayer.setColor (-65536);
					this.socketReader.readInt ( );
					
					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Debug.Lucas.println ("*** DEBUG: Enemy player's color is " + enemyPlayer.getColor ( ));
					}
					break;

				//
				// Receive the number of units our enemy wishes to use.-
				//
				case (Protocol.SET_NUMBER_OF_UNITS):
					//
					// Parse the number units of each type
					//
					hoverjetUnits 	= this.socketReader.readShort ( );
					tankUnits 		= this.socketReader.readShort ( );
					artilleryUnits = this.socketReader.readShort ( );
					
					//
					// The total number of enemy units
					//
					numberOfUnits  = hoverjetUnits + tankUnits + artilleryUnits; 

					//
					// Create a remote battle player based on the data collected so far ...
					//
					remoteEnemyPlayer = new RemotePlayer (enemyPlayer); 

					//
					// Set the number of units this enemy player has
					//
					remoteEnemyPlayer.setNumberOfHoverjets (hoverjetUnits);
					remoteEnemyPlayer.setNumberOfTanks 		(tankUnits);
					remoteEnemyPlayer.setNumberOfArtillery (artilleryUnits);

					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Debug.Lucas.println ("*** DEBUG: Enemy player has these units");
						Debug.Lucas.println (hoverjetUnits + " H, " + tankUnits + " T, " + artilleryUnits + " A\n");
					}
					break;

				//
				// Receive the X coordinates for the units the enemy has.-
				//
				case (Protocol.SET_UNIT_X):
					//
					// Create an array for the units' coordinates
					//
					unitCoordinatesX = new int [numberOfUnits];
				
					//
					// Receive the X coordinate for each unit
					//
					for (int i = 0; i < numberOfUnits; i ++)
					{
						unitCoordinatesX[i] = this.socketReader.readInt ( );
						
						//
						// Debug only!
						//
						if (Debug.LUCAS)
						{
							Debug.Lucas.println ("*** DEBUG: Received X coordinate " + unitCoordinatesX[i] + " ...");
						}
					}
					break;

				//
				// Receive the Y coordinates for the units the enemy has.-
				//
				case (Protocol.SET_UNIT_Y):
					//
					// Create an array for the units' coordinates
					//
					unitCoordinatesY = new int [numberOfUnits];
				
					//
					// Receive the X coordinate for each unit
					//
					for (int i = 0; i < numberOfUnits; i ++)
					{
						unitCoordinatesY[i] = this.socketReader.readInt ( );
						
						//
						// Debug only!
						//
						if (Debug.LUCAS)
						{
							Debug.Lucas.println ("*** DEBUG: Received Y coordinate " + unitCoordinatesY[i] + " ...");
						}
					}
					break;
				
				//
				// A handler for unknown commands
				//
				default:
					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Debug.Lucas.print ("*** WARNING: Unknown command received in JoinBattleClient");
					}
					break;
			}
		}
		
		//
		// Do we have enough information to create the remote player's army?
		//
		if ((unitCoordinatesY != null) && (unitCoordinatesY.length > 0))
		{
			//
			// Create an army for this enemy player
			//
			JoinBattleClient.Game.addPlayerArmy (remoteEnemyPlayer, hoverjetUnits, tankUnits, artilleryUnits);

			//
			// Add the enemy to the game engine
			//
			JoinBattleClient.Game.addPlayer (remoteEnemyPlayer);

			//
			// Set the enemy units' positions based on the data we received from the server.-
			//
			JoinBattleClient.Game.setupPlayerUnitPositions (remoteEnemyPlayer, unitCoordinatesX, unitCoordinatesY);	
				
			//
			// Set the orders queue
			//
			remoteEnemyPlayer.setInqueue (JoinBattleClient.INqueue);
		}
	}


	/**
	 * Sets the game context for this network client to work properly.-
	 * 
	 * @param game
	 */
	public void setGame (BattleEngine game)
	{
		if (game == null)
		{
			Log.w (Debug.TAG, "Argument is null in JoinBattleClient.setGame ( )");
		}
		else
		{
			JoinBattleClient.Game = game;
		}
	}


	/**
	 * Saves a reference to the commander unit of this player.-
	 */
	public void setCommander (Commander commander)
	{
		JoinBattleClient.Commander = commander;
	}


	/** 
	 * Saves a reference to the flag in conflict.-
	 * 
	 * @param flag
	 */
	public void setFlag (Flag flag)
	{
		JoinBattleClient.Flag = flag;
	}


	/**
	 * Returns the array containing the units' coordinates received from the server.
	 */
	public int[][] getUnitCoordinates ( )
	{
		return (this.unitCoordinates);
	}


	/**
	 * Closes the join session even before finishing it.-
	 */
	public void closeInitializationStream ( )
	{
		try
		{
			this.socketWriter.writeByte (Protocol.CLOSE_STREAM);

			this.clientSocket.close ( );
		}
		catch (IOException ioe)
		{
			Log.w (Debug.TAG, "Could not close init socket in JoinBattleClient.closeInitialisationStream ( )");
		}
	}


	/**
	 * Starts the thread for receiving data from the server over TCP.-
	 */
	public void startTCPThread ( )
	{
		//
		// Create a new TCP thread for this client
		//
		this.clientTCPThread = new BattleClientTCPThread (this.serverAddress);

		//
		// Change its priority so it will not use the whole processor
		//
		this.clientTCPThread.setPriority (Thread.MIN_PRIORITY);
		
		//
		// Start it!
		//
		this.clientTCPThread.start	( );
	}


	/**
	 * Starts the two threads (sender and receiver) to communicate with the server over UDP.-
	 */
	public void startUDPThreads ( )
	{
		// 
		// Start the UDP receiver thread.-
		//
		this.receiverThread = new ReceiverBattleThread ( );

		//
		// Change its priority so it will not use the whole processor
		//
		this.receiverThread.setPriority (Thread.MIN_PRIORITY);

		//
		// Start it!
		//
		this.receiverThread.start ( );
		
		// 
		// Start the UDP sender thread.-
		//
		this.senderThread = new SenderBattleThread (this.serverAddress);

		//
		// Change its priority so it will not use the whole processor
		//
		this.senderThread.setPriority (Thread.MIN_PRIORITY);

		//
		// Start it!
		//
		this.senderThread.start ( );
	}


	/**
	 * Stops the threads that read and write to sockets
	 */
	public void stopRunning ( )
	{
		// 
		// forces to stop threads - closes sockets
		//
		if (this.receiverThread != null)
		{
			this.receiverThread.stopRunning ( );
		}

		if (this.senderThread != null)
		{
			this.senderThread.stopRunning ( );
		}
		
		/*if (this.clientTCPThread != null)
		{
			this.clientTCPThread.halt ( );
		}*/
		
		//
		// Clean all the static members
		//
		this.cleanUp ( );
	}
	

	/**
	 * Returns this client's (explorePlayer's) ID
	 * 
	 * @return
	 */
	public int getID ( )
	{
		return (JoinBattleClient.Commander.getPlayer ( ).getId ( ));
	}

	
	/**
	 * Returns our enemy's player ID in this battle.-
	 * 
	 * @return
	 */
	public int getEnemyPlayerID ( )
	{
		return this.enemyPlayerID;
	}
}
