package iu.android.order;

import java.util.ArrayList;

public final class TurnOrder implements Order
{
	private static final ArrayList<TurnOrder>	turnOrderPool	= new ArrayList<TurnOrder> ( );

	private short x;
	private short y;
	private byte										playerID;
	private int											unitID;
	private boolean									delayed;

	public static final byte						TYPE				= (byte) 1;


	private TurnOrder ( )
	{ // so you cannot make new ones

	}


	private void set (byte playerID, int unitID, short x, short y, boolean delayed)
	{
		this.playerID = playerID;
		this.unitID = unitID;
		this.x = x;
		this.y = y;
		this.delayed = delayed;
	}


	public static synchronized TurnOrder getNewTurnOrder (byte playerID, int unitID, short x, short y, boolean delayed)
	{
		// OrderManager.orderCount++;
		//
		// Debug.orders ("(" + OrderManager.orderCount + ") Move order [" + playerID + "][" + unitID + "] -> (" + x +
		// "," + y + ") delay: " + delayed);

		if (TurnOrder.turnOrderPool.size ( ) > 0)
		{
			TurnOrder mo = TurnOrder.turnOrderPool.get (TurnOrder.turnOrderPool.size ( ) - 1);
			TurnOrder.turnOrderPool.remove (TurnOrder.turnOrderPool.size ( ) - 1);
			mo.set (playerID, unitID, x, y, delayed);
			return mo;
		}

		TurnOrder mo = new TurnOrder ( );
		mo.set (playerID, unitID, x, y, delayed);
		return mo;
	}


	public boolean isDelayed ( )
	{
		return this.delayed;
	}


	public Order getOrderOfSameType ( )
	{
		return TurnOrder.getNewTurnOrder ((byte) -1, -1, (short) -1, (short) -1, this.delayed);
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
		TurnOrder.turnOrderPool.add (this);
	}


	/** Returns a way of identifiing the order type (turn, shoot etc). * */
	public byte getType ( )
	{
		return TurnOrder.TYPE;
	}


	/** How many bytes this order needs * */
	public int getNumBytes ( )
	{
		// TYPE = 1, playerID = 1, unitID = 4, x = 2, y = 2, delayed = 1 sum = 1 + 1 + 4 + 2 + 2 + 1 = 11
		return 11;
	}


	/**
	 * Write the bytes representing this order into <CODE>bytes</CODE> starting from <CODE>pos</CODE>
	 */
	public void writeBytes (byte[] bytes, int pos)
	{
		bytes[pos] = TurnOrder.TYPE;

		bytes[pos + 1] = this.playerID;

		bytes[pos + 2] = (byte) ((this.unitID & 0xFF000000) >> 24);
		bytes[pos + 3] = (byte) ((this.unitID & 0x00FF0000) >> 16);
		bytes[pos + 4] = (byte) ((this.unitID & 0x0000FF00) >> 8);
		bytes[pos + 5] = (byte) ((this.unitID & 0x000000FF));

		bytes[pos + 6] = (byte) ((this.x & 0xFF00) >> 8);
		bytes[pos + 7] = (byte) (this.x & 0x00FF);

		bytes[pos + 8] = (byte) ((this.y & 0xFF00) >> 8);
		bytes[pos + 9] = (byte) (this.y & 0x00FF);

		bytes[pos + 10] = this.delayed ? (byte) 1 : (byte) 0;

	}


	public void readBytes (byte[] bytes, int pos)
	{
		if (TurnOrder.TYPE != bytes[pos])
		{
			throw new RuntimeException ("readBytes for turn order reading wrong bytes");
		}

		this.playerID = bytes[pos + 1];

		int byte1 = (bytes[pos + 2] & 0x000000FF) << 24, byte2 = (bytes[pos + 3] & 0x000000FF) << 16, byte3 = (bytes[pos + 4] & 0x000000FF) << 8, byte4 = (bytes[pos + 5] & 0x000000FF);

		// System.out.println( byte1 + " " + byte2 + " " + byte3 + " " + byte4 );

		this.unitID = byte1 | byte2 | byte3 | byte4;

		this.x = (short) (((bytes[pos + 6] & 0x00FF) << 8) | (bytes[pos + 7] & 0x00FF));

		this.y = (short) (((bytes[pos + 8] & 0x00FF) << 8) | (bytes[pos + 8] & 0x00FF));

		this.delayed = (bytes[pos + 10] == 1);
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