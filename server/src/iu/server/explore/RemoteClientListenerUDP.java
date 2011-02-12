package iu.server.explore;

/*************************************************************************************************************
 * IU 1.0b, a java real time strategy game
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

import iu.android.network.explore.Protocol;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Receive packets from a remote client and put objects on the client's in-queue
 */

public class RemoteClientListenerUDP extends Thread
{
	private boolean			running		= true;

	private DatagramSocket	udpListener;

	private static int		DATA_SIZE	= 128;	// ** PRESET **

	//private static int		TIMEOUT		= 30000; // number of milliseconds (1000 ms = 1 second) to wait before


	// deciding that the client has died.

	public RemoteClientListenerUDP (final DatagramSocket socket)
	{
		this.udpListener = socket;
	}


	@Override
	public void run ( )
	{
		/*
		 * DEBUG
		 * 
		 * 
		 * int receivedPackets = 0;
		 *
		 *	int droppedPackets = 0;
		 *	byte lastPacketID = -42;
		 *	byte nextPacketID = 0;*/

		while (this.running)
		{

			try
			{
				//
				// set datagram with buffer of size 128 bytes
				// if message received is longer than 128 bytes it is truncated
				// if it is shorter than there is no problem
				//
				byte[] dataBuffer = new byte[RemoteClientListenerUDP.DATA_SIZE];
				DatagramPacket datagram = new DatagramPacket (dataBuffer, dataBuffer.length);

				// set the timeout - 6min
				// on every 5min client should send its location trought UDP
				// if client does not respond SocketTimeoutException will be rised
				// and exception will be handled
				this.udpListener.setSoTimeout (360000);
				this.udpListener.receive (datagram); // this blocks until it receives a
				// datagram

				// i am not sure if this is needed or not
				dataBuffer = datagram.getData ( );

				//
				// first byte is allways command
				//
				byte command = dataBuffer[0];

				switch (command)
				{
					case Protocol.LOCATION_UPDATE:
						{
							// do something
							break;
						}
					default:
						{
							System.out.println ("RemoteClientListenerUDP: UNKNOWN COMMAND '" + command);
						}

				}

			}
			catch (InterruptedIOException iioe)
			{

			}
			catch (IOException ioe)
			{
				System.err.println ("Failed I/O: " + ioe);
			}

		}
	}


	public void halt ( )
	{
		this.running = false;
	}
}
