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

import iu.android.battle.BattleEngine;
import iu.android.engine.BattlePlayer;
import iu.android.explore.ExplorePlayer;
import iu.android.order.Order;
import iu.android.unit.Unit;

import java.util.ArrayList;

import android.util.Log;

/**
 * A player who is not on this computer.
 * 
 * The packets come across the network to a client thread, that then puts the orders on an in queue.
 */
public class RemotePlayer extends BattlePlayer
{
	//
	// The number of units, for each known type
	//
	private short			numberOfHoverjets = -1;
	private short			numberOfTanks		= -1;
	private short			numberOfArtillery = -1;
	
	private ArrayList<Order>	inQueue;
	private ArrayList<Unit>	 	killedUnits	= new ArrayList<Unit>();

	
	/**
	 * Constructor
	 * 
	 * @param exPlayer
	 */
	public RemotePlayer(ExplorePlayer exPlayer)
	{
		super(exPlayer, false, true);
	}

	/**
	 * Read orders off the in-queue, and distribute them to *our* units
	 * this method is called by the game, not the receiving net thread.-
	 */
	@Override
	public void dispatchOrders (BattleEngine game)
	{
		//
		// Make sure the queue is there ...
		//
		if (this.inQueue != null)
		{
			ArrayList<Order> delete = new ArrayList<Order> ( );

			int inCount = this.inQueue.size ( );

			for (int i = 0; i < inCount; i++)
			{
				Order order = this.inQueue.get (i);
				
				if (order.getPlayerID ( ) == this.getID ( ))
				{
					int id = order.getUnitID ( );
					Unit unit = this.getUnit (id);
					unit.giveOrder (order);
					
					delete.add (order);
				}
				
				//
				// Debug only!
				//
				//Log.d ("IU", "Read order <" + order.toString ( ) + "> from player " + order.getPlayerID ( ));
			}

			int delCount = delete.size ( );
			for (int i = 0; i < delCount; i++) // remove all orders to OUR units from the inqueue
			{
				this.inQueue.remove (delete.get (i));
			}

			//
			// Empty the delete vector to get rid of references to completed orders
			// (so they can be garbage-collected)
			//
			delete.clear ( );
		}
	}

	
	/**
	 * Saves a reference to this player's in-queue.-
	 * 
	 * @param queue
	 */
	public void setInqueue (ArrayList<Order> queue)
	{
		this.inQueue = queue;
	}

	
	/**
	 * 
	 */
	@Override
	public void getRecentlyDeadUnits (ArrayList<Unit> dead_units)
	{
		ArrayList<Unit> thisKilledUnits = this.killedUnits;
		ArrayList<Unit> thisDeadUnits   = this.deadUnits;
		int lastIndexOfKilledUnits 	  = thisKilledUnits.size ( ) - 1;
		
		for (int i = lastIndexOfKilledUnits; i > -1; i --)
		{
			Unit unit = thisKilledUnits.get (i);
			unit.setHealth (0);
			unit.kill ( );
			
			dead_units.add 	(unit);
			thisDeadUnits.add (unit);
			thisKilledUnits.remove (i);
		}
	}

	
	/**
	 * Kill this unit!
	 */
	@Override
	public void killUnit (Unit unit)
	{
		unit.kill ( );
		this.killedUnits.add (unit);
		
		//
		// Debug only!
		//
		Log.d ("IU", "Remote player " + this.getID ( ) + " lost unit " + unit.getID ( ));
	}

	
	/**
	 * 
	 * @return
	 */
	public short getNumberOfHoverjets ( )
	{
		return this.numberOfHoverjets;
	}

	
	/**
	 * 
	 * @param numberOfHoverjets
	 */
	public void setNumberOfHoverjets (short numberOfHoverjets)
	{
		this.numberOfHoverjets = numberOfHoverjets;
	}

	
	/**
	 * 
	 * @return
	 */
	public short getNumberOfTanks ( )
	{
		return this.numberOfTanks;
	}

	
	/**
	 * 
	 * @param numberOfTanks
	 */
	public void setNumberOfTanks (short numberOfTanks)
	{
		this.numberOfTanks = numberOfTanks;
	}

	
	/**
	 * 
	 * @return
	 */
	public short getNumberOfArtillery ( )
	{
		return this.numberOfArtillery;
	}

	
	/**
	 * 
	 * @param numberOfArtillery
	 */
	public void setNumberOfArtillery (short numberOfArtillery)
	{
		this.numberOfArtillery = numberOfArtillery;
	}

}