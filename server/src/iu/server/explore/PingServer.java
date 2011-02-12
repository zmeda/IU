package iu.server.explore;

/*************************************************************************************************************
 * IU 1.0b, a real time strategy game
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Reflect ping packets from a remote client back to the source.
 */
public class PingServer extends Thread
{
	private boolean	running	= true;
	private int	    port	= 1088; // ** PRESET **

	/**
	 * Constructor
	 */
	public PingServer()
	{
	}

	/**
	 * Thread entry point
	 */
	@Override
	public void run()
	{
		DatagramSocket pingSocket = null;
		try
		{
			pingSocket = new DatagramSocket(this.port);
			System.out.println("Ping server up on port " + this.port + ".");
		}
		catch (Exception e)
		{
			System.out.println("Error: Could not start ping server.");
			this.running = false;
		}

		while (this.running)
		{
			try
			{
				byte[] dataBuffer = new byte[1];
				DatagramPacket datagram = new DatagramPacket(dataBuffer, 1);

				pingSocket.receive(datagram); // this blocks until it receives a datagram (which may never
				// happen)



				int fromPort = datagram.getPort();
				datagram.setPort(fromPort + 1); // if it comes from port i, then send back to port i+1

				pingSocket.send(datagram); // transmit the packet back to the sender

				//
				// DEBUG ONLY!
				//
				//byte thisPacketID = dataBuffer[0]; // read ID of incoming packet...
				// System.out.println ("Echoed a ping packet (ID " + thisPacketID + ") to " + datagram.getAddress ( ));
			}
			catch (IOException ioe)
			{
				System.err.println("I/O exception in ping server:\n" + ioe);
			}

		}
	}

	/**
	 * Stops the running thread
	 */
	public void halt()
	{
		this.running = false;
	}

}
