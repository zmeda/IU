package iu.android.order;

import java.util.ArrayList;

/**
 * For now this order tells a unit to fire upon a specific point. Later one it may need to be implemented so
 * as to allow a specific unit to be targeted.
 */

public final class AttackOrder implements Order
{
	private static final ArrayList<AttackOrder>	attackOrderPool	= new ArrayList<AttackOrder> ( );

	private short x;
	private short y;
	private byte											playerID;
	private int												unitID;

	public static final byte							TYPE					= (byte) 2;


	private AttackOrder ( )
	{ // so you cannot make new ones

	}


	private void set (byte playerID, int unitID, short x, short y)
	{
		this.playerID = playerID;
		this.unitID = unitID;
		this.x = x;
		this.y = y;
	}


	public static synchronized AttackOrder getNewAttackOrder (byte playerID, int unitID, short x, short y)
	{
		// OrderManager.orderCount++;
		//
		// Debug.orders ("(" + OrderManager.orderCount + ") AttackOrder [" + playerID + "][" + unitID + "] -> (" + x +
		// "," + y + ")");

		if (AttackOrder.attackOrderPool.size ( ) > 0)
		{
			AttackOrder ao = AttackOrder.attackOrderPool.get (AttackOrder.attackOrderPool.size ( ) - 1);
			AttackOrder.attackOrderPool.remove (AttackOrder.attackOrderPool.size ( ) - 1);
			ao.set (playerID, unitID, x, y);
			return ao;
		}

		AttackOrder ao = new AttackOrder ( );
		ao.set (playerID, unitID, x, y);
		return ao;
	}


	public Order getOrderOfSameType ( )
	{
		return AttackOrder.getNewAttackOrder ((byte) -1, -1, (short) -1, (short) -1);
	}


	public int getUnitID ( )
	{
		return this.unitID;
	}


	public byte getPlayerID ( )
	{
		return this.playerID;
	}


	// public short getX ( )
	// {
	// return this.x;
	// }
	//
	//
	// public short getY ( )
	// {
	// return this.y;
	// }

	/**
	 * Treat dispose like delete in C++, i.e. <STRONG>do not use this object after calling dispose on it</STRONG>.
	 */
	public void dispose ( )
	{
		AttackOrder.attackOrderPool.add (this);
	}


	/** Returns a way of identifiing the order type (move, shoot etc). * */
	public byte getType ( )
	{
		return AttackOrder.TYPE;
	}


	/** How many bytes this order needs * */
	public int getNumBytes ( )
	{
		// TYPE = 1, playerID = 1, unitID = 4, x = 2, y = 2 sum = 1 + 1 + 4 + 2 + 2 = 10
		return 10;
	}


	/**
	 * Write the bytes representing this order into <CODE>bytes</CODE> starting from <CODE>pos</CODE>
	 */
	public void writeBytes (byte[] bytes, int pos)
	{
		bytes[pos] = AttackOrder.TYPE;

		bytes[pos + 1] = this.playerID;

		bytes[pos + 2] = (byte) ((this.unitID & 0xFF000000) >> 24);
		bytes[pos + 3] = (byte) ((this.unitID & 0x00FF0000) >> 16);
		bytes[pos + 4] = (byte) ((this.unitID & 0x0000FF00) >> 8);
		bytes[pos + 5] = (byte) ((this.unitID & 0x000000FF));

		bytes[pos + 6] = (byte) ((this.x & 0xFF00) >> 8);
		bytes[pos + 7] = (byte) (this.x & 0x00FF);

		bytes[pos + 8] = (byte) ((this.y & 0xFF00) >> 8);
		bytes[pos + 9] = (byte) (this.y & 0x00FF);

		// System.out.println( "Order " + x +" "+y );

	}


	/**
	 * Read the bytes representing this order from bytes starting from pos.-
	 */
	public void readBytes (byte[] bytes, int pos)
	{
		if (AttackOrder.TYPE != bytes[pos])
		{
			throw new RuntimeException ("readBytes for attack order reading wrong bytes");
		}

		this.playerID = bytes[pos + 1];

		int byte1 = (bytes[pos + 2] & 0x000000FF) << 24, byte2 = (bytes[pos + 3] & 0x000000FF) << 16, byte3 = (bytes[pos + 4] & 0x000000FF) << 8, byte4 = (bytes[pos + 5] & 0x000000FF);

		// System.out.println( byte1 + " " + byte2 + " " + byte3 + " " + byte4 );

		this.unitID = byte1 | byte2 | byte3 | byte4;

		this.x = (short) (((bytes[pos + 6] & 0x00FF) << 8) | (bytes[pos + 7] & 0x00FF));

		this.y = (short) (((bytes[pos + 8] & 0x00FF) << 8) | (bytes[pos + 9] & 0x00FF));

		// System.out.println( "Order " + x +" "+y );
	}


	/**
	 * A string representation
	 */
	@Override
	public String toString ( )
	{
		return ("Attack order");
	}
}