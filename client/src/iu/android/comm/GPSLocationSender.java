package iu.android.comm;

import iu.android.explore.ExplorePlayer;
import iu.android.network.explore.Protocol;
import iu.android.network.explore.UDPSender;
import iu.android.util.Converter;

import java.net.InetAddress;

import android.location.Location;

public class GPSLocationSender implements ILocationSender
{
	ExplorePlayer	player;
	InetAddress		address;
	UDPSender		udpSender;


	public GPSLocationSender (final InetAddress address, final ExplorePlayer player)
	{
		this.player = player;
		this.address = address;

		this.udpSender = new UDPSender ( );
	}


	public void sendLocation (final Location location)
	{
		// { command name [byte] | player id [int] | latitude [float] | longtitude [float] } (13 bytes)

		byte[] bytes = new byte[13];
		bytes[0] = Protocol.LOCATION_UPDATE;

		Converter.intToBytes (this.player.getId ( ), bytes, 1);
		Converter.floatToBytes ((float) location.getLatitude ( ), bytes, 5);
		Converter.floatToBytes ((float) location.getLongitude ( ), bytes, 9);

		this.udpSender.sendDatagram (bytes, this.address);
	}
}
