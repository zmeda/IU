package iu.android.network.explore;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UDPListener extends Thread
{
	private final int	UDP_SOCKET_TIMEOUT	= 5000;

	DatagramSocket		udpSocket;
	boolean				running;


	public UDPListener (int udpPort)
	{
		try
		{
			this.udpSocket = new DatagramSocket (udpPort);

			// This enables us to disconnect the socket the easy way every UDP_SOCKET_TIMEOUT milliseconds
			this.udpSocket.setSoTimeout (this.UDP_SOCKET_TIMEOUT);
			this.running = true;
		}
		catch (SocketException ex)
		{
			System.out.println ("Unable to create an UDP listener socket.");
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

				this.udpSocket.receive (packet);

				// Get data required
				byte[] data = packet.getData ( );
				InetAddress address = packet.getAddress ( );

				// TODO - handle the received data

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

		if (this.udpSocket != null)
		{
			this.udpSocket.disconnect ( );
			this.udpSocket.close ( );
		}
	}
}
