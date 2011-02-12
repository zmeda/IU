package iu.android.order;

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

/**
 * 
 * @author Lucas
 */
public final class OrderManager
{

	public static int			orderCount	= 0;

	private static Order[]	orders		= {MoveOrder.getNewMoveOrder ((byte) -1, -1, (short) -1, (short) -1),
			TurnOrder.getNewTurnOrder ((byte) -1, -1, (short) -1, (short) -1, true),
			AttackPositionOrder.getNewAttackPositionOrder ((byte) -1, -1, (short) -1, (short) -1),
			AttackUnitOrder.getNewAttackUnitOrder ((byte) -1, -1, (byte) -1, -1), GuardOrder.getNewGuardOrder ((byte) -1, -1, (short) -1, (short) -1)};


	/**
	 * Use this method to "decode" orders that arrived from the network.-
	 */
	public static Order getOrderOfType (byte id)
	{
		return OrderManager.orders[id].getOrderOfSameType ( );
	}
}