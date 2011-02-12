package iu.android.util;

public final class Converter
{
	/**
	 * set 4 bytes encoded from i
	 * 
	 * @param i integer to convert
	 * @param b byte array to hold the result
	 * @param offset where in the byte array to start
	 */
	public static void intToBytes (final int i, final byte[] b, final int offset)
	{
		b[offset] = (byte) ((i & 0xFF000000) >> 24);
		b[offset + 1] = (byte) ((i & 0x00FF0000) >> 16);
		b[offset + 2] = (byte) ((i & 0x0000FF00) >> 8);
		b[offset + 3] = (byte) ((i & 0x000000FF));
	}


	/**
	 * decode an integer from byte array
	 * 
	 * @param b source bytes
	 * @param offset where in the array to start
	 * @return
	 */
	public static int bytesToInt (final byte[] b, final int offset)
	{
		final int byte1 = (b[offset] & 0x000000FF) << 24; 
		final int byte2 = (b[offset + 1] & 0x000000FF) << 16;
		final int byte3 = (b[offset + 2] & 0x000000FF) << 8; 
		final int byte4 = (b[offset + 3] & 0x000000FF);

		return byte1 | byte2 | byte3 | byte4;
	}


	/**
	 * set 2 bytes from short
	 * 
	 * @param n short to convert
	 * @param b byte array to hold the result
	 * @param offset where in the array to start
	 */
	public static void shortToBytes (final short n, final byte[] b, final int offset)
	{
		b[offset] = (byte) ((n >> 8) & 0x00FF);
		b[offset + 1] = (byte) (n & 0x00FF);
	}


	/**
	 * decode a short from byte array
	 * 
	 * @param b source bytes
	 * @param offset where in the array to start
	 * @return
	 */
	public static short bytesToShort (final byte[] b, final int offset)
	{
		return (short) (((b[offset] & 0x00FF) << 8) | (b[offset + 1] & 0x00FF));
	}


	/**
	 * set 4 bytes encoded from f
	 * 
	 * @param d float to convert
	 * @param b byte array to hold the result
	 * @param offset where in the byte array to start
	 */
	public static void floatToBytes (final float f, final byte[] b, final int offset)
	{
		final int i = Float.floatToIntBits (f);
		intToBytes (i, b, offset);
	}


	/**
	 * decode a float from byte array
	 * 
	 * @param b source bytes
	 * @param offset where in the array to start
	 * @return
	 */
	public static float bytesToFloat (final byte[] b, final int offset)
	{
		final int i = bytesToInt (b, offset);
		return Float.intBitsToFloat (i);
	}
}
