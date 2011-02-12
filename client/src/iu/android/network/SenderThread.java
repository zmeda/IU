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
import iu.android.network.battle.LocalPlayer;
import iu.android.order.Order;
import iu.android.unit.Unit;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/*
 * Wait for Object on queue. When one arrives, send a datagram.
 */

public class SenderThread extends Thread
{

	boolean	                 bSendPositionUpdates	= true;

	private int	             serverPort;

	private int	             DATA_SIZE;	                // size of datagram content (in bytes)
	private InetAddress	     server;
	private ArrayList<Order>	queue;

	private DatagramSocket	 dataSocket	          = null;
	private boolean	         running	          = true;

	private BattleEngine	         game;

	private byte	         packetID	          = 0;	    // cycles from 0 to 127, then loops.

	private int	             interpacketDelay;

	public SenderThread(ArrayList<Order> queue, int packetDataSize, InetAddress server, int serverPort, DatagramSocket dataSocket, BattleEngine game, int interpacketDelay)
	{
		this.queue = queue;
		this.DATA_SIZE = packetDataSize;
		this.server = server;
		this.serverPort = serverPort;
		this.dataSocket = dataSocket;
		this.game = game;
		this.interpacketDelay = interpacketDelay;

		// try {
		// this.dataSocket = new DatagramSocket(DATAGRAM_PORT);
		// } catch(SocketException except) {
		// System.err.println("Sender: Unable to bind UDP socket: "+except);
		// System.exit(1);
		// }
	}

	@Override
	public void run()
	{
		DatagramPacket datagram;

		LocalPlayer lp = this.game.getUser();

		Debug.Lucas.println("Player " + lp.getID() + " has " + lp.getNumUnits() + " units.");

		while (this.running)
		{

			int pos = 3; // first three bytes encode packet ID, number of orders and number of stateupdates,
			// respectively, so we start reading at byte number 3.
			byte[] data = new byte[this.DATA_SIZE]; // got to be the full monty here, otherwise the udp packet
			// will
			// vary in length (and it can't)
			data[0] = this.packetID;

			// increment the packet ID for the next packet
			if (this.packetID == 127)
			{
				this.packetID = 0;
			}
			else
			{
				this.packetID++;
			}

			boolean cont = true;
			// while there are orders left on the queue..
			int ordercount = 0;
			// if( queue.size() > 0 && Debug.DUNCAN )
			// System.out.println( "There are " + queue.size() + " orders on the queue." );

			while (!this.queue.isEmpty() && cont)
			{
				Order o = this.queue.get(0);
				int datasize = o.getNumBytes();

				if (pos + datasize < this.DATA_SIZE) // if there's room left in the packet..
				{
					this.queue.remove(0);

					o.writeBytes(data, pos);
					pos += datasize; // move the datapointer along
					ordercount++;
				}
				else
				{
					cont = false;
				}
			}

			data[1] = (byte) ordercount;

			int space_left = this.DATA_SIZE - pos;
			int positions_space_left = (space_left / StateUpdate.STATE_UPDATE_SIZE);

			// if ( Debug.DUNCAN )
			// System.out.println("space left = "+space_left+" pos slots available = "+positions_space_left);

			boolean found = false;
			BattlePlayer p = null;
			// int num_players = game.getNumPlayers();

			p = this.game.getUser();

			if (p == null)
			{
				Debug.Lucas.println("ERROR! Failed to look ourselves up in the game player list (game.network.SenderThread).  Sorry.");
			}

			int unitcount = p.getNumUnits();

			int positioncount = 0;

			if (unitcount > positions_space_left) // more unit positions than space left in the packet
			{
				// while there's room, write a random unit's position information`
				while ((this.DATA_SIZE - pos) > StateUpdate.STATE_UPDATE_SIZE)
				{
					int num_units = p.getNumUnits();
					int rand_unit = (int) (java.lang.Math.random() * num_units);
					Unit u = p.getUnit(rand_unit);

					byte pid = p.getID();
					data[pos] = pid;
					pos++;
					u.writeStateInfoBytes(data, pos);

					pos += StateUpdate.STATE_UPDATE_SIZE - 1;
					positioncount++;
				}
			}
			else
			// we can fit the positions of all our units into this packet, with room to spare
			{
				for (int u = 0; u < unitcount; u++)
				{
					Unit unit = p.getUnit(u);
					byte pid = p.getID();
					data[pos] = pid;
					pos++;
					unit.writeStateInfoBytes(data, pos);
					pos += StateUpdate.STATE_UPDATE_SIZE - 1;
					positioncount++;
					// if( Debug.DUNCAN )
					// Debug.duncan.println("PID:"+pid+"/UNITPID:"+unit.getPlayer().getID()+"/GAMEUID:"+game.getUser().getID()+","+unit.getID()+","+unit.getPosition().x+","+unit.getPosition().y);

					// for (int j=-9; j<=0; j++)
					// System.out.print(data[pos+j]+",");
					// System.out.println();

				}
			}

			if (this.bSendPositionUpdates)
			{
				data[2] = (byte) positioncount;
			}
			else
			{
				data[2] = 0;
			}

			// if ( Debug.DUNCAN )
			// Debug.duncan.print(""+packetID+": Orders: "+ordercount+" Positions: "+positioncount);

			/*
			 * ///////////////
			 * 
			 * for (int i=0; i<dataPacketSize; i++) { Debug.duncan.print(""+data[i]+","); }
			 * 
			 * Debug.duncan.println();
			 */
			// //////////////////
			try
			{

				datagram = new DatagramPacket(data, this.DATA_SIZE, this.server, this.serverPort);
				this.dataSocket.send(datagram);
				// System.out.println("Sending datagram: "+server+":"+serverPort+" ["+data.toString()+"]");
				Thread.sleep(this.interpacketDelay * 4); // why *4 ? Well, it's more important to
				// receive than send.
			}
			catch (IOException except)
			{
				System.err.println("Unable to send to " + this.server + " : " + except);
			}
			catch (InterruptedException ie)
			{
				System.out.println("Connection closed.");
			}
		}
	}

	public void stopRunning()
	{
		this.running = false;
	}
}
