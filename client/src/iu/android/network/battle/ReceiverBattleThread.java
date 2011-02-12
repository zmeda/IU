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
import iu.android.order.OrderManager;
import iu.android.unit.Unit;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.util.Log;

/**
 * Wait for a UDP datagram. When one arrives, put the contents on the input queue.
 */
public class ReceiverBattleThread extends Thread
{
	//
	// We'll keep listing as long as this is TRUE
	//
	private boolean running;


	/**
	 * Constructor
	 */
	public ReceiverBattleThread ( )
	{
		try
		{
			JoinBattleClient.UDPListenSocket = new DatagramSocket (JoinBattleClient.ClientUDPListenPort);

			// 
			// Debug only!
			//
			Log.d ("IU", "ReceiverBattleThread constructed on port " + JoinBattleClient.ClientUDPListenPort);
		}
		catch (SocketException e)
		{
			Log.e ("IU", "Could not create ReceiverBattleThread on port " + String.valueOf (JoinBattleClient.ClientUDPListenPort));
		}
	}

	
	/**
	 * Start listening for incoming UDP packets (datagrams)
	 */
	@Override
	public void run ( )
	{
		//
		// We'll save received data from packets here ...
		//
		byte [] packetData = new byte [JoinBattleClient.DATA_PACKET_SIZE];
		
		//
		// A new datagram packet to hold the data received
		//
		DatagramPacket datagramPacket = new DatagramPacket (packetData, packetData.length);

		//
		// Keep listening ...
		//
		this.running = true;
		
		while (this.running)
		{
			try
			{
				//
				// Debug only!
				//
				//Log.d ("IU", "ReceiverBattleThread is waiting for a datagram to arrive ...");
				
				//
				// Waits for a datagram to arrive
				//
				JoinBattleClient.UDPListenSocket.receive (datagramPacket);

				int num_orders 		= packetData[1];
				int num_stateupdates = packetData[2];

				//
				// Debug only!
				//
				//Log.d ("IU", "ReceiverBattleThread UDP received " + String.valueOf (num_orders) + " orders and " + String.valueOf (num_stateupdates) + " state updates");
				
				//
				// The first 3 bytes are used for packetID, num. orders, num. positions
				//
				int pos = 3; 
				for (int i = 0; i < num_orders; i++)
				{
					Order order = OrderManager.getOrderOfType (packetData[pos]);

					order.readBytes (packetData, pos);
					pos += order.getNumBytes ( );

					JoinBattleClient.INqueue.add (order);
				}

				
				//
				// (Lucas) Maybe this old YARTS code just works?
				//
				for (int i = 0; i < num_stateupdates; i++)
				{
					byte pid = packetData[pos]; // read the player ID
					pos += 1;

					int byte1 = (packetData[pos] 		& 0x000000FF) << 24; 
					int byte2 = (packetData[pos + 1] & 0x000000FF) << 16;
					int byte3 = (packetData[pos + 2] & 0x000000FF) << 8;
					int byte4 = (packetData[pos + 3] & 0x000000FF);

					// System.out.println( byte1 + " " + byte2 + " " + byte3 + " " + byte4 );

					int unitID = byte1 | byte2 | byte3 | byte4; // getting twisted???
					pos += 4; // MAGIC NUMBER ALERT

					BattlePlayer p = JoinBattleClient.Game.getPlayerByID(pid);

					if (pid != JoinBattleClient.Game.getUser().getID()) // if it's not one of our units, update its position
					{
						Unit u = p.getUnit(unitID);
						u.readStateBytes(packetData, pos);
					}

					pos += StateUpdate.STATE_UPDATE_SIZE - 5; // MAGIC NUMBER ALERT
					
					// 
					// Debug only!
					//
					//if (Debug.LUCAS)
					//{
					//	Log.d ("IU", "*** DEBUG: Battle UDP receiver decoded <Unit: " + unitID + ", Player: " + pid + ">");
					//}
				}
			}
			catch (IOException except)
			{
				//
				// FIXME: (Lucas) We must inform the user about this communication error!
				//
				Log.e (Debug.TAG, "*** ERROR: Communication error while in-battle (UDP receiver thread)");
			}
		}
	}

	
	/**
	 * Stops this thread
	 */
	public void stopRunning ( )
	{
		this.running = false;
		
		JoinBattleClient.UDPListenSocket.close ( );
		
		//
		// Debug only!
		//
		if (Debug.LUCAS)
		{
			Debug.Lucas.println ("*** DEBUG: Battle UDP Receiver thread stopped.");
		}
	}
}
