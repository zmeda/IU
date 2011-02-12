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

import iu.android.battle.BattleEngine;
import iu.android.engine.BattlePlayer;
import iu.android.explore.Player;
import iu.android.order.Order;
import iu.android.unit.Unit;

import java.util.ArrayList;

import android.util.Log;

/**
 * The player who is on this computer.
 */
public class LocalPlayer extends BattlePlayer
{
	private ArrayList<Order>	ordersList	= new ArrayList<Order> ( );

	//private ArrayList<Order>	outqueue		= null;


	/**
	 * Constructor
	 * 
	 * @param exPlayer
	 */
	public LocalPlayer (Player exPlayer)
	{
		super (exPlayer, true, true);
	}


	//
	// Added code to distribute the order across the network (put on outqueue for senderthread)
	//
	public void dispatchOrder (Order order)
	{
		synchronized (this.ordersList)
		{
			this.ordersList.add (order);
		}

		//
		// Are we playing a multiplayer game?
		//
		if ((JoinBattleClient.Game != null) && (JoinBattleClient.Game.isStarted ( )))
		{
			synchronized (JoinBattleClient.OUTqueue)
			{
				JoinBattleClient.OUTqueue.add (order);
				
				//
				// Debug only!
				//
				Log.d ("IU", "Order added to out queue for later broadcast ...");
			}
		}
	}


	@Override
	public void dispatchOrders (BattleEngine game)
	{
		synchronized (this.ordersList)
		{
			int count = this.ordersList.size ( );
			for (int i = 0; i < count; i++)
			{
				Order order = this.ordersList.get (i);
				int id = order.getUnitID ( );
				Unit unit = this.getUnit (id);
				unit.giveOrder (order);
			}
			this.ordersList.clear ( );
		}
	}
}