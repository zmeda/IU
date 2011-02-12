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

import iu.android.battle.BattleThread;
import iu.android.network.StateUpdate;
import iu.android.order.Order;

import java.util.*;

/**
 * This thread handles the distribution of incoming data from clients.
 * Currently, it collects orders, positions and dead units from clients 
 * and broadcasts them to all clients, but the one that originated the order/position.
 */
public class BattleServerGameThread extends Thread
{
	private boolean					running	= true;
	private Vector<RemoteClient>	clients;


	/**
	 * Constructor
	 * 
	 * @param clients
	 */
	public BattleServerGameThread (Vector<RemoteClient> clients)
	{
		this.clients = clients;
	}


	@Override
	public void run ( )
	{
		while (this.running)
		{
			//
			// Loop through all the clients we know, checking their in-queues
			//
			for (int i = 0; i < this.clients.size ( ); i++)
			{
				RemoteClient  			 rc 			 = this.clients.elementAt (i);
				Vector<Order> 			 ordersQueue = rc.getInQueue 			  ( );
				Vector<StateUpdate>	 statesQueue = rc.getPositionsInQueue ( );
				BattleServerTCPThread tcpThread 	 = rc.getTCPThread 		  ( );
				
				//
				// Take care of the orders given by each player to his/her units ...
				//
				while (ordersQueue.size ( ) > 0)
				{
					Order order = ordersQueue.elementAt (0);

					//
					// Send the this order to all the clients ...
					//
					for (int j = 0; j < this.clients.size ( ); j ++)
					{
						//
						// ... but not to the one who actually informed it!
						//
						if (j != i)
						{
							//
							// Put the order we got from the informer's in-queue on the rc2's out-queue
							//
							RemoteClient rc2 = this.clients.elementAt (j);
							rc2.addToOutQueue (order); 
						}
					}
					ordersQueue.removeElementAt (0);
				}

				//
				// Take care of current unit states updates
				//
				while (statesQueue.size ( ) > 0)
				{
					StateUpdate state = statesQueue.elementAt (0);
					
					//
					// Send the new state to all the clients ...
					//
					for (int j = 0; j < this.clients.size ( ); j++) // for all the clients that we know..
					{
						//
						// ... but not to the one who actually informed it!
						//
						if (j != i)
						{
							//
							// Put that state we got from the informer's in-queue on the rc2's out-queue
							//
							RemoteClient rc2 = this.clients.elementAt (j);
							rc2.addToPositionsOutQueue (state); 
						}
					}
					//
					// Pop out the data just broadcasted
					//
					statesQueue.removeElementAt (0);
				}

				//
				// Unit deaths
				//
				if (tcpThread != null)
				{
					//
					// Broadcast information as long as there is something to inform
					//
					while (tcpThread.hasMoreUnitDeaths ( ))
					{
						String data = tcpThread.getNextUnitDeath ( );
						
						//
						// Send this information to all the clients ...
						//
						for (int j = 0; j < this.clients.size ( ); j ++)
						{
							//
							// ... but not to the owner of the dead units!
							//
							if (j != i)
							{
								RemoteClient rc2 = this.clients.elementAt (j);
								rc2.getTCPThread ( ).sendDeadUnitData (data);
							}
						}
					}
				}

				try
				{
					//
					// This down time matches the logic tics at the client.-
					//
					Thread.sleep (BattleThread.DELAY_PER_FRAME);
				}
				catch (InterruptedException ie)
				{
					//
					// Interrupted while waiting ... no problem!
					//
				}
			}
		}
	}


	/**
	 * Stops this battle thread.-
	 */
	public void halt ( )
	{
		this.running = false;
	}
}
