package iu.android.order;

import java.util.ArrayList;

public final class MoveOrder implements Order
{
	private static final ArrayList<MoveOrder>	moveOrderPool	= new ArrayList<MoveOrder> ( );

	private short x;
	private short y;
	private byte										playerID;
	private int											unitID;

	// TODO [] we can move all these TYPE constants to Order.java
	public static final byte						TYPE				= (byte) 0;


	private MoveOrder ( )
	{ // so you cannot make new ones

	}


	private void set (byte playerID, int unitID, short x, short y)
	{
		this.playerID = playerID;
		this.unitID = unitID;
		this.x = x;
		this.y = y;
	}


	public static synchronized MoveOrder getNewMoveOrder (byte playerID, int unitID, short x, short y)
	{
		// OrderManager.orderCount++;
		//
		// Debug.orders ("(" + OrderManager.orderCount + ") Move order [" + playerID + "][" + unitID + "] -> (" + x +
		// "," + y + ")");

		if (MoveOrder.moveOrderPool.size ( ) > 0)
		{
			MoveOrder mo = MoveOrder.moveOrderPool.get (MoveOrder.moveOrderPool.size ( ) - 1);
			MoveOrder.moveOrderPool.remove (MoveOrder.moveOrderPool.size ( ) - 1);
			mo.set (playerID, unitID, x, y);
			return mo;
		}

		MoveOrder mo = new MoveOrder ( );
		mo.set (playerID, unitID, x, y);
		return mo;
	}


	public Order getOrderOfSameType ( )
	{
		return MoveOrder.getNewMoveOrder ((byte) -1, -1, (short) -1, (short) -1);
	}


	public int getUnitID ( )
	{
		return this.unitID;
	}


	public byte getPlayerID ( )
	{
		return this.playerID;
	}


	public short getX ( )
	{
		return this.x;
	}


	public short getY ( )
	{
		return this.y;
	}


	/**
	 * Treat dispose like delete in C++, i.e. <STRONG>do not use this object after calling dispose on it</STRONG>.
	 */
	public void dispose ( )
	{
		MoveOrder.moveOrderPool.add (this);
	}


	/** Returns a way of identifiing the order type (move, shoot etc). * */
	public byte getType ( )
	{
		return MoveOrder.TYPE;
	}


	/** How many bytes this order needs * */
	public int getNumBytes ( )
	{
		// TYPE = 1, playerID = 1, unitID = 4, x = 2, y = 2
		// sum = 1 + 1 + 4 + 2 + 2 = 10
		return 10;
	}


	/**
	 * Write the bytes representing this order into <CODE>bytes</CODE> starting from <CODE>pos</CODE>
	 */
	public void writeBytes (byte[] bytes, int pos)
	{
		bytes[pos] = MoveOrder.TYPE;

		bytes[pos + 1] = this.playerID;

		bytes[pos + 2] = (byte) ((this.unitID & 0xFF000000) >> 24);
		bytes[pos + 3] = (byte) ((this.unitID & 0x00FF0000) >> 16);
		bytes[pos + 4] = (byte) ((this.unitID & 0x0000FF00) >> 8);
		bytes[pos + 5] = (byte) ((this.unitID & 0x000000FF));

		bytes[pos + 6] = (byte) ((this.x & 0xFF00) >> 8);
		bytes[pos + 7] = (byte) (this.x & 0x00FF);

		bytes[pos + 8] = (byte) ((this.y & 0xFF00) >> 8);
		bytes[pos + 9] = (byte) (this.y & 0x00FF);
	}


	public void readBytes (byte[] bytes, int pos)
	{
		if (MoveOrder.TYPE != bytes[pos])
		{
			throw new RuntimeException ("readBytes for move order reading wrong bytes");
		}

		this.playerID = bytes[pos + 1];

		int byte1 = (bytes[pos + 2] & 0x000000FF) << 24, byte2 = (bytes[pos + 3] & 0x000000FF) << 16, byte3 = (bytes[pos + 4] & 0x000000FF) << 8, byte4 = (bytes[pos + 5] & 0x000000FF);

		this.unitID = byte1 | byte2 | byte3 | byte4;

		this.x = (short) (((bytes[pos + 6] & 0x00FF) << 8) | (bytes[pos + 7] & 0x00FF));

		this.y = (short) (((bytes[pos + 8] & 0x00FF) << 8) | (bytes[pos + 9] & 0x00FF));
	}


	/**
	 * A string representation
	 */
	@Override
	public String toString ( )
	{
		return ("Move order");
	}
}
