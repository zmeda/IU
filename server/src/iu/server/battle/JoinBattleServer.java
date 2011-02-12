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
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * This thread is started for every new client wanting to join the game. It handles the join procedure for
 * her/him. The thread dies if the client wasn't able to send all the requested data in JOIN_TIMEOUT seconds.
 */
public class JoinBattleServer extends Thread
{
	private boolean									inReceiveMode				= true;

	private BattleListenServer						battleListenServer		= null;
	private BattleServerTCPThread					serverTCPThread;
	private DataOutputStream						socketWriter;
	private DataInputStream							socketReader;
	private int											clientID						= -1;
	private int											UDPListenPort				= -1;
	private int											UDPSendPort					= -1;
	private int											TCPConnectionPort			= -1;
	private int[]										playerBaseCoordinates	= null;

	private Vector<BattleServerPlayer>			playersToSend				= new Vector<BattleServerPlayer> ( );

	private DatagramSocket							listenSocket;

	private BattleServerPlayer						serverPlayer				= new BattleServerPlayer ( );

	private short										hoverjetUnits				= -1;

	private short										tankUnits					= -1;

	private short										artilleryUnits				= -1;


	/**
	 * Constructor
	 */
	public JoinBattleServer (BattleListenServer battleListenServer)
	{
		this.inReceiveMode		= true;
		this.battleListenServer = battleListenServer;

		this.playerBaseCoordinates = this.battleListenServer.getNextBaseCoordinates ( );

		this.UDPListenPort 	  = this.battleListenServer.getNextUDPListenPort ( );
		this.TCPConnectionPort = this.battleListenServer.getNextTCPThreadPort ( );

		this.serverTCPThread = new BattleServerTCPThread (this.TCPConnectionPort, this.serverPlayer);
	}


	/**
	 * This thread's entry point ...
	 */
	@Override
	public void run ( )
	{
		//
		// Wait forever and process client requests from the connection pool ...
		//
		while (true)
		{
			Socket connection;

			synchronized (this.battleListenServer.connectionPool)
			{
				//
				// Wait for new requests to arrive in the connectionPool ...
				//
				while (this.battleListenServer.connectionPool.isEmpty ( ))
				{
					try
					{
						this.battleListenServer.connectionPool.wait ( );
					}
					catch (InterruptedException e)
					{
						// We were interrupted while waiting ... so what?
					}
				}
				//
				// We will start processing this request, so we pop it out the connectionPool ...
				//
				connection = this.battleListenServer.connectionPool.remove (0);
			}

			try
			{
				//
				// We've got a socket to the joining client, so go ahead and get streams
				//
				this.socketWriter = new DataOutputStream (connection.getOutputStream ( ));
				this.socketReader = new DataInputStream  (connection.getInputStream ( ));

				//
				// Keep this join session in receive mode as long as the client has stuff to send.-
				//
				while (this.inReceiveMode)
				{
					//
					// Read a command
					//
					final byte command = this.socketReader.readByte ( );

					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						System.out.println ("Command received: " + command);
					}

					//
					// Decide what to do, based on the command just received
					//
					switch (command)
					{
						//
						// Client closed this connection
						//
						case (Protocol.CLOSE_STREAM):
							this.inReceiveMode = false;
							System.err.println ("*** WARNING: JoinBattleServer closed by " + connection.getInetAddress ( ).getHostAddress ( ));
							break;

						//
						// Client aborted this connection
						//
						case (Protocol.ABORT):
							this.inReceiveMode = false;
							System.err.println ("*** WARNING: JoinBattleServer aborted by " + connection.getInetAddress ( ).getHostAddress ( ));
							break;

						//
						// Client finishing this join session
						//
						case (Protocol.SEND_MODE):
							// 
							// Stop receiving data
							//
							this.inReceiveMode = false;

							//
							// Debug only!
							// 
							if (Debug.LUCAS)
							{
								System.out.println ("*** DEBUG: JoinBattleServer for " + connection.getInetAddress ( ).getHostAddress ( ) + " switched to SEND mode ...");
							}
							break;

						//
						// The port the client RECEIVES on, is the one that we SEND data to
						//
						case (Protocol.SET_UDP_RECEIVE_PORT):
							this.UDPSendPort = this.socketReader.readInt ( );
							break;

						//
						// Inform the client on which UDP we are RECEIVEing data
						//
						case (Protocol.GET_UDP_SEND_PORT):
							//
							// The port that the client SENDS udp packets to is the port that we are LISTENING on.
							//
							this.socketWriter.writeByte (Protocol.SET_UDP_SEND_PORT);
							this.socketWriter.writeInt  (this.UDPListenPort);
							break;

						//
						// Inform the client to switch to a new TCP port for the battle
						//
						case (Protocol.GET_TCP_PORT):
							this.socketWriter.writeByte (Protocol.SET_TCP_PORT);
							this.socketWriter.writeInt  (this.TCPConnectionPort);
							break;

						//
						// Receive this player's name
						//
						case (Protocol.SET_PLAYER_NAME):
							//
							// Get the string length and create a buffer to hold it
							//
							int stringLength = this.socketReader.readByte ( );
							byte[] buffer = new byte[stringLength];

							//
							// Read the player's name
							//
							this.socketReader.readFully (buffer);
							String playerName = new String (buffer);

							//
							// Check if this name is not in use
							//
							if (!this.battleListenServer.isNameAllocated (playerName))
							{
								this.battleListenServer.allocateName 	(playerName);
								this.serverPlayer.setName 			(playerName);
								this.socketWriter.write				(Protocol.OK);
							}
							else
							{
								this.socketWriter.write (Protocol.NOT_ACCEPTED);
							}

							//
							// Debug only!
							//
							if (Debug.LUCAS)
							{
								System.out.println ("*** DEBUG: Player name is " + this.serverPlayer.getName ( ));
							}
							break;

						//
						// Player ID
						//
						case (Protocol.SET_PLAYER_ID):
							this.clientID = this.socketReader.readInt ( );

							//
							// Debug only!
							//
							if (Debug.LUCAS)
							{
								System.out.println ("*** DEBUG: Received player ID " + this.clientID);
							}
							break;

						//
						// Receive the number of units this explorePlayer wishes to use.
						// Should not be more than MAXIMUM_NUMBER_OF_UNITS_PER_PLAYER all together.
						//
						case (Protocol.SET_NUMBER_OF_UNITS):
							//
							// Parse the number units of each type
							//
							this.hoverjetUnits 	= this.socketReader.readShort ( );
							this.tankUnits 		= this.socketReader.readShort ( );
							this.artilleryUnits 	= this.socketReader.readShort ( );

							//
							// Check that the number of units is ok
							//
							int totalNumberOfUnits = this.hoverjetUnits + this.tankUnits + this.artilleryUnits;

							if (totalNumberOfUnits > BattleListenServer.MAXIMUM_NUMBER_OF_UNITS_PER_PLAYER)
							{
								//
								// Reject this number of units because it is too big.
								//
								this.socketWriter.writeByte (Protocol.NOT_ACCEPTED);
							}
							else
							{
								//
								// Tell the client the number of units is OK
								//
								this.socketWriter.writeByte (Protocol.OK);

								//
								// Set the number of units this player has
								//
								this.serverPlayer.setNumberOfUnits (this.hoverjetUnits, this.tankUnits, this.artilleryUnits);

								//
								// Set up the units' coordinates based on this player's base position
								//
								this.serverPlayer.setupUnits (this.playerBaseCoordinates[0], this.playerBaseCoordinates[1]);
							}

							//
							// Debug only!
							//
							if (Debug.LUCAS)
							{
								System.out.print ("*** DEBUG: Received number of units ");
								System.out.print (this.hoverjetUnits + " H, ");
								System.out.print (this.tankUnits + " T, ");
								System.out.print (this.artilleryUnits + " A\n");
							}
							break;

						//
						// Send the unit X coordinates. It should never happen before actually
						// receiving the number of units this player has.
						//
						case (Protocol.GET_UNIT_X):
							//
							// Check that number of units for this player is valid
							//
							if (this.serverPlayer.getNumberOfUnits ( ) > 0)
							{
								//
								// Send the X coordinate for each unit
								//
								for (int i = 0; i < this.serverPlayer.getNumberOfUnits ( ); i++)
								{
									if (Debug.LUCAS)
									{
										System.out.println ("*** DEBUG: Sending " + this.serverPlayer.getUnitX (i) + " ...");
									}
									this.socketWriter.writeInt (this.serverPlayer.getUnitX (i));
								}
							}
							else
							{
								throw new IllegalArgumentException ("Did not yet receive a valid number of units!");
							}
							break;

						//
						// Send the unit Y coordinates. It should never happen before actually
						// receiving the number of units this player has.
						//
						case (Protocol.GET_UNIT_Y):
							//
							// Check that number of units for this player is valid
							//
							if (this.serverPlayer.getNumberOfUnits ( ) > 0)
							{
								//
								// Send the Y coordinate for each unit
								//
								for (int i = 0; i < this.serverPlayer.getNumberOfUnits ( ); i++)
								{
									this.socketWriter.writeInt (this.serverPlayer.getUnitY (i));
								}
							}
							else
							{
								throw new IllegalArgumentException ("Did not yet receive a valid number of units!");
							}
							break;

						//
						// Here we receive the FLAG_ID the player is willing to attack.
						// This player is the one who starts the battle, so his units
						// will have a little less energy than the defending ones.
						//
						case (Protocol.FLAG_ID):

							//
							// Read the flag id
							//
							this.socketReader.readInt ( );

							//
							// Look for the owner -> Implemented in Explore Server
							// Inform the owner -> Implemented in Explore Server
							// Wait for the flag owner to join the battle -> already implemented on client side!
							// If owner didn't join in N seconds, start defense with AI-driven units -> already implemented on client side!
							//
							//
							break;

						//
						// A holder for any unknown commands
						//
						default:
							System.err.println ("*** WARNING: Ignored unknown command <" + command + ">");
					}
				}
				
				//
				// Assign a color to this player
				//
				this.getPlayer ( ).setColor (this.battleListenServer.getNextColor ( ));

				//
				// Player has just joined, go on with data transfer ...
				//
				//
				// This part handles remote unit data transmission
				//
				for (JoinBattleServer joinCompleted : this.battleListenServer.completedJoinSessions)
				{
					BattleServerPlayer joinedPlayer = joinCompleted.getPlayer ( );

					//
					// 1. Send this serverPlayer's data to everyone who is already in the game.
					//
					joinCompleted.addPlayerToSend (this.getPlayer ( ));

					//
					// 2. Send data, about people who have already joined, to this serverPlayer.
					//
					this.addPlayerToSend (joinedPlayer);
				}

				//
				// Append this player's completed join session to the rest.-
				//				
				this.battleListenServer.completedJoinSessions.addElement (this);

				//
				// Debug only!
				//
				if (Debug.LUCAS)
				{
					System.out.println ("*** DEBUG: A client has joined!");
				}

				//
				// Create a new UDP socket to receive not so important data from this client.-
				//
				try
				{
					this.listenSocket = new DatagramSocket (this.UDPListenPort);

					//
					// Create a new network client object (from the server's point of view)
					//
					RemoteClient client = new RemoteClient (this.battleListenServer,
																		 this.listenSocket, 
																		 this.battleListenServer.getUDPSendSocket ( ), 
																		 connection.getInetAddress ( ), 
																		 this.UDPSendPort);

					this.serverPlayer.setRemoteClient (client);
					this.serverPlayer.setID 			 (this.clientID);
					client.setPlayerID 					 (this.clientID);
					this.battleListenServer.addClient (client);
					this.battleListenServer.addPlayer (this.serverPlayer);
					client.setRemoteTCPThread 			 (this.serverTCPThread);

					//
					// Wait here until for some time, receiving data for new players ...
					//
					while (this.battleListenServer.joinBattleTimeout > 0)
					{
						try
						{
							//
							// Go to sleep a little bit, not to use the whole processor ...
							//
							Thread.sleep (500);
							this.battleListenServer.joinBattleTimeout -= 500;

							//
							// Broadcast unit information to all the players, but the last joined
							//
							while (this.playersToSend.size ( ) > 0)
							{
								//
								// The last player to join the battle
								//
								BattleServerPlayer newPlayer = this.playersToSend.firstElement ( );

								//
								// Send this player's ID
								//
								this.socketWriter.writeByte (Protocol.SET_PLAYER_ID);
								this.socketWriter.writeInt  (newPlayer.getID ( ));

								//
								// Send this player's name
								//
								this.socketWriter.writeByte  (Protocol.SET_PLAYER_NAME);
								this.socketWriter.writeByte  (newPlayer.getName ( ).length ( ));
								this.socketWriter.writeBytes (newPlayer.getName ( ));
								
								//
								// Send this player's color
								//
								this.socketWriter.writeByte (Protocol.SET_PLAYER_COLOR);
								this.socketWriter.writeInt  (newPlayer.getColor ( ));

								//
								// Debug only!
								//
								if (Debug.LUCAS)
								{
									System.out.println ("*** DEBUG: Player data sent -> ID=" + newPlayer.getID ( ) + " Name=" + newPlayer.getName ( ) + " Color=" + newPlayer.getColor ( ));
								}

								//
								// Send this player's number of units
								//
								this.socketWriter.writeByte  (Protocol.SET_NUMBER_OF_UNITS);
								this.socketWriter.writeShort (newPlayer.getNumberOfHoverjets ( ));
								this.socketWriter.writeShort (newPlayer.getNumberOfTanks ( ));
								this.socketWriter.writeShort (newPlayer.getNumberOfArtillery ( ));
								
								//
								// Send the X coordinates ...
								//
								this.socketWriter.writeByte (Protocol.SET_UNIT_X);
								
								//
								// ... for each of the player's unit
								//
								for (int i = 0; i < newPlayer.getNumberOfUnits ( ); i ++)
								{
									this.socketWriter.writeInt (newPlayer.getUnitX (i));
								}
								
								//
								// Send the Y coordinates ...
								//
								this.socketWriter.writeByte (Protocol.SET_UNIT_Y);
								
								//
								// ... for each of the player's unit
								//
								for (int i = 0; i < newPlayer.getNumberOfUnits ( ); i ++)
								{
									this.socketWriter.writeInt (newPlayer.getUnitY (i));
								}

								//
								// Remove this player from the container, because his data has just been broadcasted.-
								//
								this.playersToSend.removeElementAt (0);
							}
						}
						catch (InterruptedException ie)
						{
							//
							// We've been interrupted while waiting ... so what?
							//
						}
					}

					//
					// Time is up ... battle starts!
					//
					this.battleListenServer.startBattle ( );
					
					//
					// Create the UDP related threads
					//
					RemoteClientListener rcl = new RemoteClientListener (client);
					RemoteClientSender   rcs = new RemoteClientSender (client);
					
					client.setRemoteClientListener (rcl);
					client.setRemoteClientSender   (rcs);

					//
					// Are we fighting only against AI-driven units?
					//
					if (this.battleListenServer.completedJoinSessions.size ( ) > 1)
					{
						//
						// No, we are fighting in multiplayer environment, so we must ...
						// ... start the threads that deal with in-battle updates (UDP connections).-
						//
						rcl.setPriority (Thread.MIN_PRIORITY);
						rcl.start ( );

						rcs.setPriority (Thread.MIN_PRIORITY);
						rcs.start ( );
						
						//
						// Debug only!
						//
						if (Debug.LUCAS)
						{
							System.out.println ("*** DEBUG: Start multiplayer battle!");
						}
					}
					else
					{
						//
						// Debug only!
						//
						if (Debug.LUCAS)
						{
							System.out.println ("*** DEBUG: Start single player battle!");
						}
					}

					try
					{
						//
						// Wait for a second, so any data not yet sent will arrive to the clients
						//
						Thread.sleep (1000);
					}
					catch (InterruptedException ie)
					{
						//
						// We've been interrupted while waiting ... so what?
						//
					}

					//
					// Notify the client that the game has started
					//
					this.socketWriter.writeByte (Protocol.START_GAME);
				}
				catch (BindException be)
				{
					System.err.println ("*** ERROR: Could not bind socket for client " + this.clientID + " to UDP port " + this.UDPListenPort);
				}
			}
			catch (IOException ioe)
			{
				//
				// The client connection has been lost ... there's nothing we can do ...
				//
				System.err.println ("*** WARNING: Connection with client [" + this.getPlayer ( ).getName ( ) + "] has been lost!");
				ioe.printStackTrace ( );
			}
			catch (IllegalArgumentException iae)
			{
				//
				// The user wishes to use more than MAXIMUM_NUMBER_OF_UNITS_PER_PLAYER
				//
				System.err.println ("*** ERROR: The user " + this.getPlayer ( ).getName ( ) + " wishes to use more than " + 
										  BattleListenServer.MAXIMUM_NUMBER_OF_UNITS_PER_PLAYER + " units!");
			}
			finally
			{
				//
				// We have to clean and close the client socket when we don't need it anymore ...
				//
				try
				{
					connection.close ( );
					
					if (Debug.LUCAS)
					{
						System.out.println ("*** DEBUG: Join session closed for client " + this.getPlayer ( ).getName ( ));
					}
					
				}
				catch (IOException e)
				{
					//
					// If I can't close the socket, then the client already did it.
					//
				}
			}
		}
	}


	/**
	 * Sends the START_GAME signal to all the players.
	 *
	public void signalStart ( )
	{
		this.gameStarted = true;
	}*/


	/**
	 * 
	 * @return
	 */
	public BattleServerPlayer getPlayer ( )
	{
		return this.serverPlayer;
	}


	/**
	 * 
	 * @param player
	 */
	public void addPlayerToSend (BattleServerPlayer player)
	{
		this.playersToSend.addElement (player);
	}
}
