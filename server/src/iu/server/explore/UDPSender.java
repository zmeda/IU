package iu.server.explore;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPSender
{

	private DatagramSocket	udpSocket;
	private int					udpPort;


	// Creates a new UDP Socket to send datagram packets through the port specified
	public UDPSender (int port)
	{
		this.udpPort = port;

		try
		{
			this.udpSocket = new DatagramSocket ( );
		}
		catch (SocketException ex)
		{
			System.out.println ("Unable to create Datagram socket on port " + this.udpPort);
			ex.printStackTrace ( );
		}
	}


	// Sends the byte array through the datagram socket to the InetAddress specified
	public void sendDatagram (byte[] bytes, InetAddress address)
	{
		DatagramPacket packet = new DatagramPacket (bytes, bytes.length, address, this.udpPort);

		try
		{
			this.udpSocket.send (packet);
		}
		catch (IOException ex)
		{
			System.out.println ("UPD packet send failed (address: '" + address + "', port: '" + this.udpPort + "').");
		}
	}


	public void disconnect ( )
	{
		this.udpSocket.disconnect ( );
	}
}
