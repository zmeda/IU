package iu.android.network;

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
import iu.android.engine.BattlePlayer;
import iu.android.order.Order;
import iu.android.order.OrderManager;
import iu.android.unit.Unit;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

/*
 * Wait for Datagram. When one arrives, put the contents on the 'queue' vector.
 */
public class ReceiverThread extends Thread
{
	private ArrayList<Order>	queue;
	private byte[]	         data;

	private BattleEngine	         game;

	private DatagramSocket	 dataSocket;
	private boolean	         running	    = true;
	int	                     dataPacketSize;

	byte	                 lastPacketID	= -42;
	int	                     droppedPackets	= 0;

	public ReceiverThread(ArrayList<Order> queue, int dataPacketSize, DatagramSocket dataSocket, BattleEngine game)
	{
		this.queue = queue;
		this.dataPacketSize = dataPacketSize;
		this.data = new byte[dataPacketSize];
		this.dataSocket = dataSocket;
		this.game = game;

		// try {
		// this.dataSocket = new DatagramSocket(datagramPort);
		// } catch(SocketException except) {
		// System.err.println("Receiver: Unable to bind UDP socket: "+except);
		// System.exit(1);
		// }
	}

	@Override
	public void run()
	{
		DatagramPacket datagram;
		int v;
		int received = 0;

		datagram = new DatagramPacket(this.data, this.dataPacketSize);

		while (this.running)
		{
			try
			{
				this.dataSocket.receive(datagram); // blocks until datagram arrives
				received++;

				byte thisPacketID = this.data[0];
				byte maybeLastPacketID = thisPacketID;
				maybeLastPacketID--;
				if (maybeLastPacketID == -1)
				{
					maybeLastPacketID = 127; // handle the fact that it cycles
				}

				if (this.lastPacketID != -42) // if not the first time through..
				{
					if (maybeLastPacketID != this.lastPacketID)
					{
						this.droppedPackets++;
					}
				}

				this.lastPacketID = thisPacketID; // set up for next time round..

				int num_orders = this.data[1];
				int num_stateupdates = this.data[2];

				int pos = 3; // the first 3 bytes are used for packetID, num. orders, num. positions
				for (int i = 0; i < num_orders; i++)
				{
					Order o = OrderManager.getOrderOfType(this.data[pos]);
					o.readBytes(this.data, pos);
					pos += o.getNumBytes();

					this.queue.add(o);
					// System.out.println("Received a datagram!");
				}

				for (int i = 0; i < num_stateupdates; i++)
				{
					byte pid = this.data[pos]; // read the player ID
					pos += 1;

					int byte1 = (this.data[pos] & 0x000000FF) << 24, byte2 = (this.data[pos + 1] & 0x000000FF) << 16, byte3 = (this.data[pos + 2] & 0x000000FF) << 8, byte4 = (this.data[pos + 3] & 0x000000FF);

					// System.out.println( byte1 + " " + byte2 + " " + byte3 + " " + byte4 );

					int unitID = byte1 | byte2 | byte3 | byte4; // getting twisted???
					pos += 4; // MAGIC NUMBER ALERT

					BattlePlayer p = this.game.getPlayerByID(pid);

					if (pid != this.game.getUser().getID()) // if it's not one of our units, update its position
					{
						Unit u = p.getUnit(unitID);
						// if ( Debug.DUNCAN )
						// System.out.println("Updating state: PID="+pid+" ourID="+game.getUser().getID()+"
						// unitID="+u.getID()+"");
						u.readStateBytes(this.data, pos);
					}
					else
					{
						Debug.Lucas.println("Wise Insolence Error: Just refused to update own unit position");
					}

					pos += StateUpdate.STATE_UPDATE_SIZE - 5; // MAGIC NUMBER ALERT
				}

				// System.out.println("R:"+thisPacketID+": Orders: "+num_orders+" Positions: "+num_stateupdates);

				if ((this.droppedPackets > 0) && (received % 100 == 0))
				{
					Debug.Lucas.println("Dropped: " + this.droppedPackets + "  Received: " + received);
				}

				try
				{
					Thread.sleep(50);
				}
				catch (InterruptedException ie)
				{
				}

			}
			catch (IOException except)
			{
				System.err.println("Failed I/O: " + except);
			}
		}
	}

	public void stopRunning()
	{
		this.running = false;
	}
}
