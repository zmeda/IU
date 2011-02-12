package iu.android;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import android.util.Log;

/**
 * A class with a bunch of static functions/variables to aid in debugging. Basically there is three of
 * everything, so we don't get our debugging info messed up.
 * 
 * @author Lucas
 */
public final class Debug
{
	//
	// The TAG identifying our application
	//
	public static final String		TAG	= "IU";

	//
	// Flag for Boris, to turn debugging code on and off.
	//
	public static final boolean	BORIS	= false;

	//
	// Flag for Lucas, to turn debugging code on and off.
	//
	public static final boolean	LUCAS	= true;

	//
	// Flag for Joshua, to turn debugging code on and off.
	//
	public static final boolean	JOSHUA = false;

	/** Stream for Lucas to print stuff to (instead of System.out). * */
	public static PrintStream		Lucas	= new PrintStream (new LogOutput ("Lucas", 256, Debug.LUCAS), true);

	/** Stream for Boris to print stuff to (instead of System.out). * */
	public static PrintStream		Boris	= new PrintStream (new LogOutput ("Boris", 256, Debug.BORIS), true);

	/** Stream for Joshua to print stuff to (instead of System.out). * */
	public static PrintStream		Joshua = new PrintStream (new LogOutput ("Joshua", 256, Debug.JOSHUA), true);

	
	/** Window for Matt to draw debugging graphics on. * */
	// public final static GraphicOutput mattWin = new GraphicOutput ("Matt Graphics", 256, 256, Debug.MATT);
	/** Window for John to draw debugging graphics on. * */
	// public final static GraphicOutput johnWin = new GraphicOutput ("John Graphics", 256, 256, Debug.JOHN);
	/** Window for Duncan to draw debugging graphics on. * */
	// public final static GraphicOutput duncanWin = new GraphicOutput ("Duncan Graphics", 256, 256,
	// Debug.LUCAS);

	
	/**
	 * An output stream that prints to the emulator's log system.-
	 */
	public static class LogOutput extends OutputStream
	{
		private String		tag;
		private boolean	output;
		private byte[]		charBytes			= null;
		private int			nCurrentCharByte	= 0;


		//
		// Make a WindowOutput with the specified title and internal byte array buffer size.
		//
		LogOutput (String name, int buffer_size, boolean out)
		{
			this.tag = Debug.TAG;
			this.charBytes = new byte[buffer_size];
			this.output = out;
		}


		//
		// Write a byte to the stream. Gets stored in an array then the array is periodically converted to a
		// String and appended to the emulator logger.-
		//
		@Override
		public void write (int b) throws IOException
		{
			if (!this.output)
			{
				return;
			}

			this.charBytes[this.nCurrentCharByte] = (byte) b;
			this.nCurrentCharByte++;
			if (this.nCurrentCharByte >= this.charBytes.length)
			{
				this.writeBytes ( );
			}
		}


		//
		// Force the internal buffer(s) to be cleared and written to the window.
		// 
		@Override
		public void flush ( ) throws IOException
		{
			super.flush ( );
			this.writeBytes ( );
		}


		//
		// Close the stream.
		//
		@Override
		public void close ( ) throws IOException
		{
			super.close ( );
		}


		//
		// Convert the internal byte array buffer to a String and append it to the TextArea.
		//
		private void writeBytes ( )
		{
			if (!this.output)
			{
				return;
			}

			if (this.nCurrentCharByte != 0)
			{
				int nNumBytes = this.nCurrentCharByte;

				String string = new String (this.charBytes, 0, nNumBytes);
				Log.i (this.tag, string);

				this.nCurrentCharByte -= nNumBytes;
			}
		}
	}


	/**
	 * An implementation of the standard C assert
	 * 
	 * @param bTrue
	 * @param mesg
	 */
	public static final void assertCompare (boolean bTrue, String mesg)
	{
		if (!bTrue)
		{
			throw new RuntimeException (mesg);
		}
	}
}
