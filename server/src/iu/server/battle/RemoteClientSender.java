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

import iu.android.network.StateUpdate;
import iu.android.network.battle.JoinBattleClient;
import iu.android.order.Order;
import iu.server.explore.game.Command;
import iu.server.explore.game.CommandFinishBattle;

import java.net.*;
import java.util.*;
import java.io.*;


/**
 * Periodically send UDP packets to the remote client, with objects taken from the outqueue.-
 */
public class RemoteClientSender extends Thread
{
	//
	// We will keep this thread waiting for client data
	//
	private boolean				running					= true;

	//
	// The remote user, connected at the other end of the cable
	//
	private RemoteClient			client					= null;


	/**
	 * Constructor
	 * 
	 * @param c
	 */
	public RemoteClientSender (RemoteClient c)
	{
		this.client = c;
	}


	/**
	 * Send UDP packets to the remote client.-
	 */
	@Override
	public void run ( )
	{
		int  sentPackets = 0;
		byte nextPacketID = 0;

		//
		// Keep it running ...
		//
		while (this.running)
		{
			try
			{
				//
				// Don't overload the connection ...
				//
				Thread.sleep (JoinBattleClient.InterpacketDelay);
			}
			catch (InterruptedException except)
			{
				//
				// Interrupted while waiting ... no problem!
				//
			}

			try
			{
				byte[] dataBuffer = new byte[JoinBattleClient.DATA_PACKET_SIZE];

				// 
				// The first 3 bytes are taken (Packet ID, num. orders, num. positions)
				//
				int position = 3;
				
				//
				// Get a reference to this client's out-queue
				//
				Vector<Order> orderQueue = this.client.getOutQueue ( );
				
				//
				// Check if there is any order to forward
				//
				if (!orderQueue.isEmpty ( ))
				{
					dataBuffer[0] = nextPacketID; // set ID of outgoing packet..

					boolean cont = true;
					// while there are orders left on the queue..
					byte orderCount = 0;
					
					while (!orderQueue.isEmpty ( ) && cont)
					{
						Order order = orderQueue.firstElement ( );
						int datasize = order.getNumBytes ( );

						//
						// if there's room left in the packet ...
						//
						if (position + datasize < JoinBattleClient.DATA_PACKET_SIZE) 
						{
							orderQueue.removeElementAt (0);

							order.writeBytes (dataBuffer, position);
							position += datasize; // move the datapointer along
							orderCount ++;
						}
						else
						{
							cont = false;
						}
					}
					
					dataBuffer[1] = orderCount;
				}
				else
				{
					//
					// No orders to send
					//
					dataBuffer[1] = 0;
				}

				//
				// Debug only!
				//
				if (Debug.LUCAS)
				{
					if (dataBuffer[1] > 0)
					{
						System.out.println ("*** DEBUG: RemoteClientSender just sent " + String.valueOf (dataBuffer[1]) + 
												  " ORDERS to client " + this.client.getPlayerID ( ) + " on " + this.client.getClientAddress ( ) + 
												  ":" + this.client.getRemotePort ( ));
					}
				}
				
				//
				// Check if there is any state unit update to send
				//
				if (!this.client.getPositionsOutQueue ( ).isEmpty ( ))
				{
					Vector<StateUpdate> stateQueue = this.client.getPositionsOutQueue ( );

					int positioncount = 0;
					// while there's room, write a unit's position information`
					while (!orderQueue.isEmpty ( ) && (JoinBattleClient.DATA_PACKET_SIZE - position > StateUpdate.STATE_UPDATE_SIZE))
					{
						// take positions off the positions outqueue
						StateUpdate su = stateQueue.firstElement ( );
						int datasize = StateUpdate.STATE_UPDATE_SIZE;

						if (position + datasize < JoinBattleClient.DATA_PACKET_SIZE) // if there's room..
						{
							stateQueue.removeElementAt (0);
							su.writeBytes (dataBuffer, position);
							position += datasize; // move the datapointer along
							positioncount++;
						}
						
						//
						// Save the number of state sent
						//
						dataBuffer[2] = (byte) positioncount;
					}
				}

				//
				// Debug only!
				//
				if (Debug.LUCAS)
				{
					if (dataBuffer[2] > 0)
					{
						System.out.println ("*** DEBUG: RemoteClientSender just sent " + String.valueOf (dataBuffer[2]) + 
												  " POSITION updates to client " + this.client.getPlayerID ( ) + " on " + 
												  this.client.getClientAddress ( ) + ":" + this.client.getRemotePort ( ));
					}
				}

				//
				// Build a packet to send the data just built
				//
				DatagramPacket datagram = new DatagramPacket (dataBuffer, 
																			 dataBuffer.length, 
																			 this.client.getClientAddress ( ), 
																			 this.client.getRemotePort ( ));
				//
				// Send it!
				//
				this.client.getServerSendSocket ( ).send (datagram);
				
				sentPackets ++;

				// 
				// Debug only!
				//
				if ((sentPackets % 100) == 0)
				{
					System.out.println ("*** DEBUG: Sent " + sentPackets + " packets to player " + this.client.getPlayerID ( ));
				}

				// iterate packet IDs
				if (nextPacketID == 127)
				{
					nextPacketID = 0;
				}
				else
				{
					nextPacketID++;
				}
			}
			catch (IOException e)
			{
				//
				// FIXME: (Lucas) Is this the correct command for Explore Server??
				//
				Command command = CommandFinishBattle.init (this.client.getBattleListenServer ( ).getBattle ( ), 
																		  this.client.getBattleListenServer ( ).getBattle ( ).getAttacker ( ));
				
				this.client.getBattleListenServer ( ).getExploreServer ( ).getGame ( ).schedule (command);

				System.err.println ("*** ERROR: Connection lost with player " + this.client.getPlayerID ( ) + ".\n" + e);
			}
		}
		
		//
		// Close the opened socket
		//
		this.client.getServerSendSocket ( ).close ( );
	}

	
	/**
	 * Stops this thread.-
	 */
	public void halt ( )
	{
		this.running = false;
	}
}
