package iu.android.order;

import java.util.ArrayList;

public final class GuardOrder implements Order
{
	private static final ArrayList<GuardOrder>	guardOrderPool	= new ArrayList<GuardOrder> ( );

	private short x;
	private short y;
	private byte											playerID;
	private int												unitID;

	public static final byte							TYPE				= (byte) 4;							// $$$$ not


	// sure what
	// number
	// should be
	// MS

	private GuardOrder ( )
	{ // so you cannot make new ones

	}


	private void set (byte playerID, int unitID, short x, short y)
	{
		this.playerID = playerID;
		this.unitID = unitID;
		this.x = x;
		this.y = y;
	}


	public static synchronized GuardOrder getNewGuardOrder (byte playerID, int unitID, short x, short y)
	{
		// OrderManager.orderCount++;
		//
		// Debug.orders ("(" + OrderManager.orderCount + ") GuardOrder [" + playerID + "][" + unitID + "] -> (" + x +
		// "," + y + ")");

		if (GuardOrder.guardOrderPool.size ( ) > 0)
		{
			GuardOrder go = GuardOrder.guardOrderPool.get (GuardOrder.guardOrderPool.size ( ) - 1);
			GuardOrder.guardOrderPool.remove (GuardOrder.guardOrderPool.size ( ) - 1);
			go.set (playerID, unitID, x, y);
			return go;
		}

		GuardOrder go = new GuardOrder ( );
		go.set (playerID, unitID, x, y);
		return go;
	}


	public Order getOrderOfSameType ( )
	{
		return GuardOrder.getNewGuardOrder ((byte) -1, -1, (short) -1, (short) -1);
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
		GuardOrder.guardOrderPool.add (this);
	}


	/** Returns a way of identifiing the order type (guard, shoot etc). * */
	public byte getType ( )
	{
		return GuardOrder.TYPE;
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
		bytes[pos] = GuardOrder.TYPE;

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


	public void readBytes (byte[] bytes, int pos)
	{
		if (GuardOrder.TYPE != bytes[pos])
		{
			throw new RuntimeException ("readBytes for guard order reading wrong bytes");
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
		return ("Guard order");
	}
}