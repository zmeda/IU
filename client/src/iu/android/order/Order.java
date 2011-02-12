package iu.android.order;

public interface Order
{

	public int getUnitID ( );


	public byte getPlayerID ( );


	/**
	 * Treat dispose like delete in C++, i.e. <STRONG>do not use this object after calling dispose on it</STRONG>.
	 */
	public void dispose ( );


	/** Returns a way of identifying the order type (move, shoot etc). * */
	public byte getType ( );


	/**
	 * Use is similar to java.lang.Object "clone", but the data in the order is not copied. Useful for the
	 * prototype pattern.
	 */
	public Order getOrderOfSameType ( );


	/** How many bytes this order needs * */
	public int getNumBytes ( );


	/**
	 * Write the bytes representing this order into <CODE>bytes</CODE> starting from <CODE>pos</CODE>
	 */
	public void writeBytes (byte[] bytes, int pos);


	public void readBytes (byte[] bytes, int pos);

}