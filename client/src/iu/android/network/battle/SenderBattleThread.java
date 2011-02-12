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
import iu.android.network.StateUpdate;
import iu.android.order.Order;
import iu.android.unit.Unit;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.util.Log;

/**
 * Sends in-battle updates from the queue out to the network (over UDP).-
 */
public class SenderBattleThread extends Thread
{

	boolean					sendPositionUpdates	= true;
	private InetAddress	server;
	private boolean		running					= true;

	//
	// This ID cycles from 0 to 127, then loops.
	//
	private byte			packetID					= 0;


	/**
	 * Constructor
	 */
	public SenderBattleThread (InetAddress server)
	{
		this.server = server;

		try
		{
			JoinBattleClient.UDPSendSocket = new DatagramSocket();
		}
		catch (SocketException e)
		{
			Log.e ("IU", "Could not create Sender datagram socket!");
		}
	}


	/**
	 * Keep this thread running as long as there is anything to send.-
	 */
	@Override
	public void run ( )
	{
		//
		// The buffer we fill with the data we want to send in the UDP datagram
		//
		byte[] dataBuffer = new byte[JoinBattleClient.DATA_PACKET_SIZE];

		//
		// The datagram itself
		//
		DatagramPacket datagram = new DatagramPacket (dataBuffer, JoinBattleClient.DATA_PACKET_SIZE, this.server, JoinBattleClient.BattleServerUDPPort);

		//
		// Debug only!
		//
		if (Debug.LUCAS)
		{
			LocalPlayer lp = JoinBattleClient.Game.getUser ( );
			Log.d ("IU", "Local player " + lp.getID ( ) + " has " + lp.getNumUnits ( ) + " units.");
		}

		//
		// Keep it running ...
		//
		while (this.running)
		{
			//
			// Start building a datagram ...
			// The first 3 bytes of a packet contain the packetID, the number of orders and number of state
			// updates.
			// Data regarding orders starts from the 4th byte on.
			//

			//
			// This packet ID
			//
			dataBuffer[0] = this.packetID;

			//
			// Increment the packet ID for the next round (IDs move from 0 to 127)
			//
			this.packetID = (byte) ((this.packetID + 1) % 128);

			//
			// First, we will package the orders
			//
			byte orderCount = 0;

			//
			// Orders data start from the 4th byte on
			//
			int position = 3;

			//
			// Is there enough room in this package left for the data?
			//
			boolean roomLeftInPackage = true;

			//
			// A reference to this player's out-queue
			//
			synchronized (JoinBattleClient.OUTqueue)
			{
				//
				// Is there any order to send?
				//
				if (JoinBattleClient.OUTqueue.isEmpty ( ))
				{
					//
					// No orders to send
					//
					dataBuffer[1] = 0;
				}
				else
				{
					// 
					// Package orders left on the queue ...
					//
					while (!JoinBattleClient.OUTqueue.isEmpty ( ) && roomLeftInPackage)
					{
						Order order = JoinBattleClient.OUTqueue.get (0);
						int datasize = order.getNumBytes ( );

						roomLeftInPackage = (position + datasize < JoinBattleClient.DATA_PACKET_SIZE);

						if (roomLeftInPackage)
						{
							order.writeBytes (dataBuffer, position);
							position += datasize;
							orderCount ++;

							//
							// Pop out the order just packed
							//
							JoinBattleClient.OUTqueue.remove (0);
						}
					}
					//
					// Encode the number of orders packaged in this datagram
					//
					dataBuffer[1] = orderCount;
				}
			}

			//
			// Debug only!
			//
			//if (Debug.LUCAS)
			//{
			//	Log.d ("IU", "Sender UDP encoded " + String.valueOf (dataBuffer[1]) + " orders.");
			//}
			
			//
			// Second, we will package the positions (states) of our units
			//
			if (this.sendPositionUpdates)
			{
				// int stateUpdateSize = new StateUpdate ( ).getNumBytes ( );

				int space_left = JoinBattleClient.DATA_PACKET_SIZE - position;
				int positions_space_left = (space_left / StateUpdate.STATE_UPDATE_SIZE);
				// roomLeftInPackage = (position + datasize < JoinBattleClient.DATA_PACKET_SIZE);

				//
				// A reference to the player
				//
				BattlePlayer battlePlayer = JoinBattleClient.Game.getUser ( );
				if (battlePlayer == null)
				{
					Log.e (Debug.TAG, "*** ERROR: Failed to look ourselves up in the game player list. Sorry!");
				}

				//
				// The total number of units this player has and his ID.-
				//
				int numberOfUnits = battlePlayer.getNumUnits ( );
				byte playerID = battlePlayer.getID ( );

				//
				// The number of updates we were able to encode, so far ...
				//
				int stateUpdatesCount = 0;

				//
				// Do we have enough place to encode all our units?
				// 
				if (numberOfUnits > positions_space_left)
				{
					//
					// The space we have used in this packet
					//
					int spaceUsed = JoinBattleClient.DATA_PACKET_SIZE - position;

					//
					// While there's room, write a random unit's position information
					//
					while (spaceUsed > StateUpdate.STATE_UPDATE_SIZE)
					{
						//
						// Pick a unit randomly
						//
						int randomUnitID = (int) (java.lang.Math.random ( ) * numberOfUnits);
						Unit unit = battlePlayer.getUnit (randomUnitID);

						//
						// Save this player's ID in the packet
						//
						dataBuffer[position] = playerID;

						//
						// Push the position forward
						//
						position++;

						//
						// Encode this unit's state
						//
						unit.writeStateInfoBytes (dataBuffer, position);

						position += StateUpdate.STATE_UPDATE_SIZE - 1;

						//
						// State update encoding is finished, increment its counter
						stateUpdatesCount++;

						//
						// Update the space we have used
						//
						spaceUsed = JoinBattleClient.DATA_PACKET_SIZE - position;
					}
				}
				else
				{
					//
					// We can fit the states of all our units into this packet, with room to spare ...
					//
					for (int u = 0; u < numberOfUnits; u++)
					{
						Unit unit = battlePlayer.getUnit (u);
						dataBuffer[position] = playerID;
						position++;
						unit.writeStateInfoBytes (dataBuffer, position);
						position += StateUpdate.STATE_UPDATE_SIZE - 1;
						stateUpdatesCount++;
					}
				}

				//
				// Encode the amount of state updates we packaged
				//
				dataBuffer[2] = (byte) stateUpdatesCount;
			}
			else
			{
				//
				// Do not send state updates
				//
				dataBuffer[2] = 0;
			}

			//
			// Debug only!
			//
			//if (Debug.LUCAS)
			//{
			//	Log.d ("IU", "Sender UDP encoded " + String.valueOf (dataBuffer[2]) + " positions.");
			//}

			try
			{
				//
				// Save the data to the datagram before sending it
				//
				datagram.setData (dataBuffer);

				//
				// Send it!
				//
				JoinBattleClient.UDPSendSocket.send (datagram);

				//
				// Don't overload the connection ...
				//
				Thread.sleep (JoinBattleClient.InterpacketDelay);
			}
			catch (IOException except)
			{
				//
				// FIXME: (Lucas) We must inform the user about this error!
				//
				Log.e (Debug.TAG, "*** ERROR: Communication error to " + this.server + " while in-battle (UDP sender thread)");
			}
			catch (InterruptedException ie)
			{
				//
				// Interrupted while waiting ... no problem!
				//
			}
		}
	}


	/**
	 * Stops this thread.-
	 */
	public void stopRunning ( )
	{
		this.running = false;

		if (JoinBattleClient.UDPSendSocket != null)
		{
			JoinBattleClient.UDPSendSocket.close ( );
		}
		//
		// Debug only!
		//
		if (Debug.LUCAS)
		{
			Debug.Lucas.println ("*** DEBUG: Battle UDP Sender thread stopped.");
		}
	}
}
