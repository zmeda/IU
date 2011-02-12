package iu.server.explore;

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

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A class with a bunch of static functions/variables to aid in debugging. Basically there is three of
 * everything, so we don't get our debugging info messed up.
 */

public final class Debug
{
	/** boolean for Lucas, to turn debugging code on and off. * */
	public static final boolean			LUCAS		= true;

	/** Stream for Lucas to print stuff to (instead of System.out). * */
	public static PrintStream				lucas		= new PrintStream (new WindowOutput ("Lucas", 10, Debug.LUCAS), true);

	/** Window for Lucas to draw debugging graphics on. * */
	public final static GraphicOutput	lucasWin	= new GraphicOutput ("Lucas Graphics", 256, 256, Debug.LUCAS);


	/** Redirect Lucas output stream to a file. * */

	public static void redirectLucas (final String file)
	{
		PrintStream stream = Debug.fileStream (file);
		if (stream != null)
		{
			Debug.lucas = stream;
		}
	}


	/** Try to get a PrintStream for the file. * */

	private static PrintStream fileStream (final String file)
	{
		try
		{
			PrintStream stream = new PrintStream (new BufferedOutputStream (new FileOutputStream (file)), true // auto-flush
			);
			return stream;
		}
		catch (Exception e)
		{
			System.err.println (e);
		}
		return null;
	}

	/** An output stream that prints to a window. * */

	public static class WindowOutput extends OutputStream
	{

		private TextArea	textArea	= null;
		private Frame		frame		= null;
		protected boolean	output	= false;


		/** Make a WindowOutput with the specified title and internal byte array buffer size. * */

		WindowOutput (final String name, final int buffer_size, final boolean out)
		{
			this.charBytes = new byte[buffer_size];
			this.frame = new Frame (name);
			this.output = out;

			WindowAdapter adapter = new WindowAdapter ( ) {
				@Override
				public void windowClosing (WindowEvent we)
				{
					WindowOutput.this.output = !WindowOutput.this.output;
				}
			};

			this.frame.addWindowListener (adapter);
		}

		private byte[]	charBytes			= null;
		private int		nCurrentCharByte	= 0;


		/**
		 * Write a byte to the stream. Gets stored in an array then the array is periodically converted to a
		 * java.lang.String and appened to a TextArea component.
		 */

		@Override
		public void write (final int b) throws IOException
		{
			if (!this.output)
			{
				return;// throw new RuntimeException( "using Debug" );
			}

			this.charBytes[this.nCurrentCharByte] = (byte) b;
			this.nCurrentCharByte++;
			if (this.nCurrentCharByte >= this.charBytes.length)
			{
				this.writeBytes ( );
			}
		}


		/** Force the internal buffer(s) to be cleared and written to the window. * */

		@Override
		public void flush ( ) throws IOException
		{
			super.flush ( );
			this.writeBytes ( );
		}


		/** Close the stream. This also disposes of the Window. * */

		@Override
		public void close ( ) throws IOException
		{
			super.close ( );
			this.frame.dispose ( );
		}


		/**
		 * Convert the internal byte array buffer to a String and append it to the TextArea.
		 */

		private void writeBytes ( )
		{
			if (!this.output)
			{
				return;
			}

			if (this.nCurrentCharByte != 0)
			{

				if (this.textArea == null)
				{
					this.textArea = new TextArea ( );
					this.textArea.setEditable (false);
					this.frame.add (this.textArea);
					// this.frame.show();
					this.frame.setVisible (true);
					this.frame.pack ( );
				}

				int nNumBytes = this.nCurrentCharByte;

				String string = new String (this.charBytes, 0, nNumBytes);
				this.textArea.append (string);

				this.nCurrentCharByte -= nNumBytes;
			}

		}

	}

	public static class GraphicOutput extends Canvas
	{

		/**
		 * 
		 */
		private static final long	serialVersionUID	= -409629760733488901L;

		private Frame		frame			= null;

		private Image		offScrImage	= null;
		private Graphics	offScrGr		= null;
		private int			graphicsWidth	= -1, graphicsHeight = -1;
		private boolean	output			= true;


		/** Makes a window for graphical output, with the title and buffer size specified. * */

		public GraphicOutput (final String title, final int graphicsWidth, final int graphicsHeight,
				final boolean output)
		{
			this.frame = new Frame (title);
			this.frame.add (this);
			this.graphicsWidth = graphicsWidth;
			this.graphicsHeight = graphicsHeight;
			this.output = output;
		}


		/** Gets the components preferred size (the size of the buffer). * */

		@Override
		public Dimension getPreferredSize ( )
		{
			return new Dimension (this.graphicsWidth, this.graphicsHeight);
		}


		/**
		 * Get the size of the buffer we are drawing onto. The buffer is stretched to fit the whole of the
		 * window.
		 */
		public Dimension getGraphicsSize ( )
		{
			return new Dimension (this.graphicsWidth, this.graphicsHeight);
		}


		/** Change the windows buffer size. * */

		public void setGraphicsSize (final int width, final int height)
		{
			synchronized (this)
			{
				if (this.offScrGr != null)
				{
					this.offScrGr.dispose ( );
				}

				this.graphicsWidth = width;
				this.graphicsHeight = height;

				this.offScrImage = null;
				this.offScrGr = null;
			}
		}


		/**
		 * Get the graphics context to draw onto. The first time this is called the window will appear.
		 */
		public Graphics g ( )
		{
			if (this.offScrImage == null)
			{

				synchronized (this)
				{
					if (this.output)
					{
						// this.frame.show ( );
						this.frame.setVisible (true);
						this.frame.pack ( );

						this.offScrImage = this.createImage (this.graphicsWidth, this.graphicsHeight);
						this.offScrGr = this.offScrImage.getGraphics ( );
					}
				}

			}

			return this.offScrGr;
		}


		@Override
		public void update (final Graphics g)
		{
			this.paint (g);
		}


		@Override
		public void paint (final Graphics g)
		{
			Image image = this.offScrImage;
			if (image == null)
			{
				return;
			}

			// stretch to fit the window
			g.drawImage (image, 0, 0, this.getSize ( ).width, this.getSize ( ).height, this);
		}

	}


	public static final void assertCompare (boolean bTrue, final String mesg)
	{
		if (!bTrue)
		{
			throw new RuntimeException (mesg);
		}
	}

}
