package iu.android.util;

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

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 * 
 * @author Lucas
 */
public abstract class IOStreamManager
{

	protected abstract OutputStream getOutStream (String fileName) throws IOException;


	public final OutputStream getOutputStream (String fileName) throws IOException
	{
		return this.getOutStream (fileName);
	}

	protected InputStream getInStream (String fileName) throws IOException
	{
		URL url = this.getClass ( ).getResource ("/" + fileName);
		if (url == null)
		{
			throw new IOException ("Failed to load: " + fileName);
		}
		URLConnection connection = url.openConnection ( );
		return connection.getInputStream ( );
	}

	public final InputStream getInputStream (String fileName) throws IOException
	{
		return this.getInStream (fileName);
	}
}