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
import iu.android.order.Order;
import iu.android.order.OrderManager;

import java.net.*;
import java.io.*;


/**
 * Receives UDP packets from a remote client and put the objects on the client's in-queue.-
 */
public class RemoteClientListener extends Thread
{
	//
	// The size of a datagram packet. It must match the size on the client side.-
	//
	private static final int	DATA_PACKET_SIZE	= 128;

	//
	// The amount of time (1000 ms = 1 second) to wait before deciding that the client has gone offline.-
	//
	private static final int	TIMEOUT				= 30000;

	//
	// We will keep this thread waiting for client data
	//
	private boolean				running				= true;

	//
	// The remote user, connected at the other end of the cable
	//
	private RemoteClient			client				= null;


	/**
	 * Constructor
	 */
	public RemoteClientListener (RemoteClient c)
	{
		this.client = c;
	}


	/**
	 * Receive UDP packets from the remote client.-
	 */
	@Override
	public void run ( )
	{
		int 	receivedPackets = 0;
		int 	droppedPackets  = 0;
		byte 	lastPacketID 	 = 127;

		//
		// Keep this thread running ...
		//
		while (this.running)
		{
			try
			{
				//
				// The buffer we fill with the data received in the UDP datagram
				//
				byte [] 			dataBuffer 	= new byte [RemoteClientListener.DATA_PACKET_SIZE];
				DatagramPacket datagram 	= new DatagramPacket (dataBuffer, dataBuffer.length);

				//
				// We will wait for the client for some time, afterwards we imply that the connection was lost
				//
				this.client.getServerListenSocket ( ).setSoTimeout (RemoteClientListener.TIMEOUT);
				
				//
				// Wait for a datagram to arrive
				//
				this.client.getServerListenSocket ( ).receive (datagram); 
				
				//
				// Start parsing the datagram just received.
				// The first 3 bytes of a packet contain the packetID, the number of orders and number of state updates.
				// Data regarding orders starts from the 4th byte on.
				//

				//
				// Read out the packet ID (IDs move from 0 to 127)
				//
				byte packetID 			  	  = dataBuffer[0];
				byte expectedLastPacketID = (byte) ((packetID - 1) % 128);

				//
				// Check if we lost a package
				//
				if (expectedLastPacketID != lastPacketID) 
				{
					droppedPackets ++;
				}
				
				//
				// Remember this packet ID for the next round
				//
				lastPacketID = packetID;

				//
				// A counter to keep track of the number of packets successfully received
				//
				receivedPackets ++;

				//
				// Parse out the number of orders packaged in this datagram
				//
				int numberOfOrders = dataBuffer[1];

				//
				// Parse each of the orders, starting off from the 4th position in the packet
				//
				int position = 3;

				for (int i = 0; i < numberOfOrders; i ++)
				{
					//
					// Create a new order object based on the ID received
					//
					Order order = OrderManager.getOrderOfType (dataBuffer [position]);

					//
					// Decode the order
					//
					order.readBytes (dataBuffer, position);
					
					//
					// Move the position inside the package forward
					//
					position += order.getNumBytes ( );
					
					//
					// Append the order to the user's in-queue
					//
					this.client.addToInQueue (order);
				}

				//
				// Debug only!
				//
				if (Debug.LUCAS)
				{
					if (numberOfOrders > 0)
					{
						System.out.println ("*** DEBUG: RemoteClientListener UDP read " + String.valueOf (numberOfOrders) + " orders.");
					}
				}

				//
				// Parse out the number of positions (unit states) packaged in this datagram
				//
				int numberOfPositions = dataBuffer[2];

				for (int i = 0; i < numberOfPositions; i ++)
				{
					//
					// Debug only!
					//
					//if (Debug.LUCAS)
					//{
					//	System.out.println ("*** DEBUG: Reading state " + i + " of " + numberOfPositions + ". Data = [ "+dataBuffer[position]+"]");
					//}

					//
					// Create a new state and read the data received
					//
					StateUpdate stateUpdate = new StateUpdate ( );
					
					stateUpdate.readBytes (dataBuffer, position);

					//
					// Move the position inside the package forward
					//
					position += StateUpdate.STATE_UPDATE_SIZE;

					//
					// Append the order to the user's in-queue
					//
					this.client.addToPositionsInQueue (stateUpdate);
				}
				
				//
				// Debug only!
				//
				if (Debug.LUCAS)
				{
					if (numberOfPositions > 0)
					{
						System.out.println ("*** DEBUG: RemoteClientListener UDP read " + String.valueOf (numberOfPositions) + " positions.");
					}
				}
			}
			catch (InterruptedIOException iioe)
			{
				//
				// More than timeout seconds passed with no answer ...
				//
				System.err.println ("*** WARNING: The client " + this.client.getPlayerID ( ) + " is not responding and its threads have been halted.");
				this.client.die ( );
			}
			catch (IOException ioe)
			{
				//
				// Connection error
				//
				System.err.println ("*** ERROR: Connection failed with client " + this.client.getPlayerID ( ) + ".\n" + ioe);
			}
		}
		
		//
		// Close the opened socket
		//
		this.client.getServerListenSocket ( ).close ( );

		//
		// Debug only!
		//
		if (Debug.LUCAS)
		{
			System.out.println ("*** DEBUG: RemoteClientListener for client " + this.client.getPlayerID ( ) + " has been halted. Releasing connection.");
		}
	}


	/**
	 * Stop this thread.-
	 */
	public void halt ( )
	{
		this.running = false;
		
		//
		// Close the opened socket
		//
		this.client.getServerListenSocket ( ).close ( );
	}
}
