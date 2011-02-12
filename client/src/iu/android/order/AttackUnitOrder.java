package iu.android.order;

import iu.android.battle.BattleEngine;
import iu.android.unit.Unit;
import iu.android.util.Converter;

import java.util.ArrayList;

/**
 * For now this order tells a unit to fire upon a specific point. Later one it may need to be implemented so
 * as to allow a specific unit to be targeted.
 */

public final class AttackUnitOrder implements Order
{
	private static final ArrayList<AttackUnitOrder>	attackUnitOrderPool	= new ArrayList<AttackUnitOrder> ( );

	private byte												playerID;
	private int													unitID;

	private byte												attackPlayerID;
	private int													attackUnitID;

	public static final byte								TYPE						= (byte) 3;

	private static BattleEngine										game						= null;


	public static void setGame (BattleEngine g)
	{
		AttackUnitOrder.game = g;
	}


	private AttackUnitOrder ( )
	{ 
		// so you cannot make new ones
	}


	private void set (byte playerID, int unitID, byte attackPlayerID, int attackUnitID)
	{
		this.playerID = playerID;
		this.unitID = unitID;
		this.attackPlayerID = attackPlayerID;
		this.attackUnitID = attackUnitID;
	}


	public static synchronized AttackUnitOrder getNewAttackUnitOrder (byte playerID, int unitID, Unit target)
	{
		return AttackUnitOrder.getNewAttackUnitOrder (playerID, unitID, target.getPlayer ( ).getID ( ), target.getID ( ));
	}


	public static synchronized AttackUnitOrder getNewAttackUnitOrder (byte playerID, int unitID, byte attackPlayerID, int attackUnitID)
	{
		// OrderManager.orderCount++;
		//
		// Debug.orders ("(" + OrderManager.orderCount + ") AttackUnitOrder [" + playerID + "][" + unitID + "] -> [" +
		// attackPlayerID + "," + attackUnitID
		// + "]");

		if (AttackUnitOrder.attackUnitOrderPool.size ( ) > 0)
		{
			AttackUnitOrder ao = AttackUnitOrder.attackUnitOrderPool.get (AttackUnitOrder.attackUnitOrderPool.size ( ) - 1);
			AttackUnitOrder.attackUnitOrderPool.remove (AttackUnitOrder.attackUnitOrderPool.size ( ) - 1);
			ao.set (playerID, unitID, attackPlayerID, attackUnitID);
			return ao;
		}

		AttackUnitOrder ao = new AttackUnitOrder ( );
		ao.set (playerID, unitID, attackPlayerID, attackUnitID);
		return ao;
	}


	public Order getOrderOfSameType ( )
	{
		AttackUnitOrder order = new AttackUnitOrder ( );
		order.set ((byte) -1, -1, (byte) -1, -1);
		return order;
	}


	public byte getPlayerID ( )
	{
		return this.playerID;
	}


	public int getUnitID ( )
	{
		return this.unitID;
	}


	public byte getAttackPlayerID ( )
	{
		return this.attackPlayerID;
	}


	public int getAttackUnitID ( )
	{
		return this.attackUnitID;
	}


	public Unit getTargetUnit ( )
	{
		return AttackUnitOrder.game.getPlayerByID (this.attackPlayerID).getUnit (this.attackUnitID);
	}


	/**
	 * Treat dispose like delete in C++, i.e. <STRONG>do not use this object after calling dispose on it</STRONG>.
	 */
	public void dispose ( )
	{
		AttackUnitOrder.attackUnitOrderPool.add (this);
	}


	/** Returns a way of identifiing the order type (move, shoot etc). * */
	public byte getType ( )
	{
		return AttackUnitOrder.TYPE;
	}


	/** How many bytes this order needs * */
	public int getNumBytes ( )
	{
		// TYPE = 1, playerID = 1, unitID = 4, attackPlayerID = 1, attackUnitID = 4
		// sum = 1 + 1 + 4 + 1 + 4 = 15
		return 11;
	}


	/**
	 * Write the bytes representing this order into <CODE>bytes</CODE> starting from <CODE>pos</CODE>
	 */
	public void writeBytes (byte[] bytes, int pos)
	{
		bytes[pos] = AttackUnitOrder.TYPE;
		pos++;

		bytes[pos] = this.playerID;
		pos++;

		Converter.intToBytes (this.unitID, bytes, pos);
		pos += 4;

		bytes[pos] = this.attackPlayerID;
		pos++;

		Converter.intToBytes (this.attackUnitID, bytes, pos);
		pos += 4;

		// System.out.println( "Order " + x +" "+y );

	}


	public void readBytes (byte[] bytes, int pos)
	{
		if (AttackUnitOrder.TYPE != bytes[pos])
		{
			throw new RuntimeException ("readBytes for move order reading wrong bytes");
		}

		pos++;

		this.playerID = bytes[pos];
		pos++;

		this.unitID = Converter.bytesToInt (bytes, pos);
		pos += 4;

		this.attackPlayerID = bytes[pos];
		pos++;

		this.attackUnitID = Converter.bytesToInt (bytes, pos);
		pos += 4;

		// System.out.println( "Order " + x +" "+y );
	}


	/**
	 * A string representation
	 */
	@Override
	public String toString ( )
	{
		return ("AttackUnit order");
	}
}