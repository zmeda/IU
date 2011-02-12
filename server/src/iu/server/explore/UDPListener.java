package iu.server.explore;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UDPListener extends Thread
{
	private final int	UDP_SOCKET_TIMEOUT	= 5000;

	DatagramSocket		socket;
	boolean				running;


	public UDPListener (int udpPort)
	{
		try
		{
			this.socket = new DatagramSocket (udpPort);

			// This enables us to disconnect the socket the easy way every UDP_SOCKET_TIMEOUT milliseconds
			this.socket.setSoTimeout (this.UDP_SOCKET_TIMEOUT);
			this.running = true;
		}
		catch (SocketException ex)
		{
			System.out.println ("Unable to create an UDP listener socket.");
			ex.printStackTrace ( );
		}
	}


	@Override
	public void run ( )
	{
		while (this.running)
		{
			try
			{
				byte[] bytes = new byte[128];

				// Wait for an incoming UDP packet
				DatagramPacket packet = new DatagramPacket (bytes, bytes.length);

				this.socket.receive (packet);

				// Get data required
				byte[] data = packet.getData ( );
				InetAddress address = packet.getAddress ( );

				// Tell a remote client to process this data
				RemoteClient client = RemoteClient.getClient (address);

				if (client != null)
				{
					client.execute (data);
				}
			}
			catch (SocketTimeoutException stex)
			{
				// Timeout for listening
				// System.out.println ("Receive UPD packet time-out");
			}
			catch (IOException ioex)
			{
				System.out.println ("UDPListener: IOException");
			}
		}
	}


	/**
	 * 
	 */
	public void disconnect ( )
	{
		this.running = false;

		if (this.socket != null)
		{
			this.socket.disconnect ( );
			this.socket.close ( );
		}
	}
}
