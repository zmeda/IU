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

import iu.server.explore.game.Command;
import iu.server.explore.game.Game;
import iu.server.explore.game.Player;
import iu.server.explore.game.ProtocolParser;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents a remote client, from the server's point of view
 */
public class RemoteClient
{

	private static HashMap<String, RemoteClient>			clients				= new HashMap<String, RemoteClient> ( );
	private static HashMap<InetAddress, RemoteClient>	clientsByAddress	= new HashMap<InetAddress, RemoteClient> ( );

	// address of the client
	InetAddress														clientAddress;																	// the

	private boolean												alive					= true;
	// private ListenServer server;

	//
	// client threads that handles send and recive actions
	//
	private RemoteClientSenderTCP								tcpSender;
	private RemoteClientListenerTCP							tcpListener;
	private RemoteClientSenderUDP								udpSender;
	private RemoteClientListenerUDP							udpListener;

	private String													username;

	private boolean												logged;

	private Player													player;

	private Game													game;


	/*
	 * private RemoteClientListener rcl; private RemoteClientSender rcs;
	 */

	public RemoteClient (final Game game, final InetAddress clientAddress, final String username)
	{
		this.game = game;
		this.clientAddress = clientAddress;
		this.username = username;

		RemoteClient.clients.put (this.username, this);
		RemoteClient.clientsByAddress.put (this.clientAddress, this);
	}


	/**
	 * Sends bytes to client
	 * 
	 * @param arr
	 */
	public void send (final byte[] arr)
	{
		System.out.println ("DEBUG: send to client " + this.username + " byte[] = " + Arrays.toString (arr));

		while (this.tcpSender == null)
		{
			try
			{
				// Wait for the Join thread to finish
				Thread.sleep (100);
			}
			catch (Exception e)
			{
				// Interrupted while waiting ... NP
			}
		}

		// check command that will be send
		// and choose appropriate protocol

		if (arr[0] < 96)
		{
			// send through tcp port

			// check if it is event
			if (arr[0] > 63)
			{
				this.tcpSender.sendEvent (arr);
			}
			else
			{
				this.tcpSender.sendData (arr);
			}
		}
		else
		{
			// commands from 96 are reserved for UPD
			this.udpSender.send (arr);
		}
	}


	public void execute (final byte[] arr)
	{
		Command command = ProtocolParser.parse (arr, this.game, this.player);
		this.player.execute (command);
	}


	public boolean isAlive ( )
	{
		return this.alive;
	}


	public void logout ( )
	{
		this.alive = false;
	}


	public Player login (final Game game, final String password)
	{
		Player player = game.logInPlayer (this.username, password);

		this.player = player;

		if (player != null)
		{
			this.logged = true;
		}

		return player;
	}


	public RemoteClientListenerTCP getTcpListener ( )
	{
		return this.tcpListener;
	}


	public void setTcpListener (final RemoteClientListenerTCP tcpListener)
	{
		this.tcpListener = tcpListener;
	}


	public RemoteClientSenderTCP getTcpSender ( )
	{
		return this.tcpSender;
	}


	public void setTcpSender (final RemoteClientSenderTCP tcpSender)
	{
		this.tcpSender = tcpSender;
	}


	public RemoteClientListenerUDP getUdpListener ( )
	{
		return this.udpListener;
	}


	public void setUdpListener (final RemoteClientListenerUDP udpListener)
	{
		this.udpListener = udpListener;
	}


	public RemoteClientSenderUDP getUdpSender ( )
	{
		return this.udpSender;
	}


	public void setUdpSender (final RemoteClientSenderUDP udpSender)
	{
		this.udpSender = udpSender;
	}


	//
	// Static functions to get the clients
	//

	public static RemoteClient getClient (final String username)
	{
		return RemoteClient.clients.get (username);
	}


	public static RemoteClient getClient (final InetAddress address)
	{
		return RemoteClient.clientsByAddress.get (address);
	}


	public boolean isLogged ( )
	{
		return this.logged;
	}


	@Override
	public String toString ( )
	{
		return "RemoteClient for [" + this.player + "]";
	}


	/**
	 * @return the player
	 */
	public Player getPlayer ( )
	{
		return this.player;
	}
}
