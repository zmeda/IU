package iu.android.network.explore;

import iu.android.network.NetworkResources;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPSender
{
	DatagramSocket	udpSocket;


	// Creates a new UDP Socket to send datagram packets through the port specified
	public UDPSender ( )
	{
		try
		{
			this.udpSocket = new DatagramSocket ( );
		}
		catch (SocketException ex)
		{
			System.out.println ("Unable to create Datagram socket on port " + NetworkResources.UDP_SEND_PORT);
			ex.printStackTrace ( );
		}
	}


	// Sends the byte array through the datagram socket to the InetAddress specified
	public void sendDatagram (byte[] bytes, InetAddress address)
	{
		DatagramPacket packet = new DatagramPacket (bytes, bytes.length, address, NetworkResources.UDP_SEND_PORT);

		try
		{
			this.udpSocket.send (packet);
		}
		catch (IOException ex)
		{
			System.out.println ("UPD packet send failed (address: '" + address + "', port: '" + NetworkResources.UDP_SEND_PORT + "').");
		}
	}


	public void disconnect ( )
	{
		if (this.udpSocket != null)
		{
			this.udpSocket.disconnect ( );
			this.udpSocket.close ( );
		}
	}
}
