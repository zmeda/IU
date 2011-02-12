package iu.server.battle;

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

import iu.android.network.StateUpdate;
import iu.android.order.Order;
import iu.server.battle.RemoteClientListener;
import iu.server.battle.RemoteClientSender;

import java.net.*;
import java.util.*;

/**
 * Represents a remote client, from the server's point of view.
 * It handles all UDP communications with the user.-
 */
public class RemoteClient
{
	//
	// We'll keep our state synchronized with this flag
	//
	private boolean					running;
	
	//
	// The socket that the server is using to receive UDP requests from the client (one per client)
	//
	private DatagramSocket			serverListenSocket;
	
	//
	// The UDP socket that the server is using to broadcast data to all clients (there's only one of these)
	//
	private DatagramSocket			serverUDPSendSocket;
	
	//
	// The address of the remote client
	//
	private InetAddress				clientAddress;
	
	//
	// The port to send data to on the client's machine
	//
	private int							remoteUDPListenPort;			

	private Vector<Order>			inQueue;
	private Vector<Order>			outQueue;
	private Vector<StateUpdate>	positionsInQueue;
	private Vector<StateUpdate>	positionsOutQueue;

	//
	// Player's ID (of the remote client)
	//
	private int							playerID;	

	private BattleServerTCPThread	tcpThread;
	private RemoteClientSender		remoteClientSender;
	private RemoteClientListener	remoteClientListener;
	private BattleListenServer		battleListenServer = null;
	


	/**
	 * Constructor
	 */
	public RemoteClient (BattleListenServer battleListenServer, DatagramSocket serverListenSocket, DatagramSocket serverSendSocket, InetAddress clientAddress, int clientPort)
	{
		this.battleListenServer 	= battleListenServer;
		this.serverListenSocket 	= serverListenSocket;
		this.serverUDPSendSocket 	= serverSendSocket;
		this.clientAddress 			= clientAddress;
		this.remoteUDPListenPort 	= clientPort;

		this.inQueue  = new Vector<Order> ( );
		this.outQueue = new Vector<Order> ( );
		
		this.positionsInQueue  = new Vector<StateUpdate> ( );
		this.positionsOutQueue = new Vector<StateUpdate> ( );
		
		this.running = true;
	}


	public void setClientAddress (InetAddress c)
	{
		this.clientAddress = c;
	}


	public InetAddress getClientAddress ( )
	{
		return this.clientAddress;
	}


	public void setServerListenSocket (DatagramSocket d)
	{
		this.serverListenSocket = d;
	}


	public DatagramSocket getServerListenSocket ( )
	{
		return (this.serverListenSocket);
	}


	public DatagramSocket getServerSendSocket ( )
	{
		return this.serverUDPSendSocket;
	}

	public int getRemotePort ( )
	{
		return this.remoteUDPListenPort;
	}


	public void setPlayerID (int p)
	{
		this.playerID = p;
	}


	public int getPlayerID ( )
	{
		return this.playerID;
	}


	public Vector<Order> getInQueue ( )
	{
		return (this.inQueue);
	}


	public Vector<Order> getOutQueue ( )
	{
		return this.outQueue;
	}


	public Vector<StateUpdate> getPositionsInQueue ( )
	{
		return this.positionsInQueue;
	}


	public Vector<StateUpdate> getPositionsOutQueue ( )
	{
		return this.positionsOutQueue;
	}


	public void addToOutQueue (Order o)
	{
		this.outQueue.addElement (o);
	}


	public void addToInQueue (Order o)
	{
		this.inQueue.addElement (o);
	}


	public void addToPositionsOutQueue (StateUpdate state)
	{
		this.positionsOutQueue.addElement (state);
	}


	public void addToPositionsInQueue (StateUpdate state)
	{
		this.positionsInQueue.addElement (state);
	}


	public boolean isRunning ( )
	{
		return this.running;
	}


	/**
	 * Saves a reference to the remote UDP client sender.-
	 * 
	 * @param rcs
	 */
	public void setRemoteClientSender (RemoteClientSender rcs)
	{
		this.remoteClientSender = rcs;
	}


	/**
	 * Saves a reference to the remote UDP client listener.-
	 * 
	 * @param rcs
	 */
	public void setRemoteClientListener (RemoteClientListener rcl)
	{
		this.remoteClientListener = rcl;
	}


	public void setRemoteTCPThread (BattleServerTCPThread tcp)
	{
		this.tcpThread = tcp;
	}


	public BattleServerTCPThread getTCPThread ( )
	{
		return this.tcpThread;
	}


	public void die ( )
	{
		//
		// Stop all the running network services for this client
		//
		if (this.remoteClientListener != null)
		{
			this.remoteClientListener.halt ( );
		}
		
		if (this.remoteClientSender != null)
		{
			this.remoteClientSender.halt ( );	
		}
		
		if (this.tcpThread != null)
		{
			this.tcpThread.halt ( );
		}
		
		this.running = false;
	}


	/**
	 * Returns a reference to the battle server in control of this client.-
	 * 
	 * @return
	 */
	public BattleListenServer getBattleListenServer ( )
	{
		return (this.battleListenServer);
	}
}
