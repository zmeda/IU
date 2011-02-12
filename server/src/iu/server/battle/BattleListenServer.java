package iu.server.battle;

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

import iu.server.explore.game.Battle;
import iu.server.explore.game.Command;
import iu.server.explore.game.CommandFinishBattle;
import iu.server.explore.game.Player;
import iu.server.explore.game.Server;

import java.net.*;
import java.util.*;
import java.io.*;

/**
 * This server listens (waits) for new players who want to join the game.-
 */
public class BattleListenServer extends Thread
{
	//
	// This server's character encoding. It must match the one on the client side.-
	//
	protected final static String					ENCODING						= "US-ASCII";

	//
	// The maximum amount of units a player may have in a battle
	//
	protected final static int						MAXIMUM_NUMBER_OF_UNITS_PER_PLAYER = 20;
	
	//
	// Queue size of clients waiting to join a game.-
	//
	private final static int						INITIALIZATION_BACKLOG	= 3;

	//
	// FIXME: Temporary implementation of base positions
	//
	private final static int[][]					BASE_COORDINATES			= { {100, 1600}, {1650, 100} };

	//
	// FIXME: Temporary implementation of players' colors
	//
	private final static int[]						PLAYER_COLORS				= {0xFFFF0000, 0xFF0000FF};

	//
	// The string version of this server
	//
	protected final static String					VERSION						= "1.0b";

	//
	// The connection pool used to handle clients calling ...
	//
	protected List<Socket>							connectionPool				= new LinkedList<Socket> ( );

	//
	// Holds the successfully completed join sessions.
	// (i.e. one JoinBattleServer thread after everything has been negotiated with the client).-
	//
	protected Vector<JoinBattleServer>		   completedJoinSessions	= new Vector<JoinBattleServer> ( );
	
	//
	// The game thread
	//
	private BattleServerGameThread 				serverGameThread			= null;
	
	//
	// The number of players for this battle.
	//
	private int											numberOfPlayers			= -1;

	//
	// A vector of remote client objects
	//
	private Vector<RemoteClient>					remoteClients				= new Vector<RemoteClient> ( );

	//
	// A vector of serverPlayers from the server's point of view
	//
	private Vector<BattleServerPlayer>			serverPlayers				= new Vector<BattleServerPlayer> ( );

	//
	// The TCP listen socket
	//
	protected ServerSocket							TCPListenSocket			= null;

	//
	// The UDP send socket
	//
	protected DatagramSocket						UDPSendSocket				= null;

	//
	// The maximum amount of milliseconds that this server will wait for a client to join.-
	//
	protected int										joinBattleTimeout			= 3000;

	// 
	// A container for player names already in use
	//
	private Vector<String>							allocatedNames				= new Vector<String> ( );

	//
	// The next player's base position on the map
	//
	private int											nextBaseIndex				= 0;

	//
	// The next color a player will use
	//
	private int											nextColorIndex				= 0;

	//
	// The address for this server to bind
	//
	private String 									serverAddress				= null;

	//
	// TCP port on which the server waits for clients to join a game.
	// The client must connect to this port to start a join session.-
	//
	private int											TCPListenPort				= -1;

	//
	// Port on which the server sends UDP packets.-
	//
	private int											UDPSendPort					= -1;

	//
	// Separate port for each client to send data (this is the first one)
	//
	private int											nextTCPSendPort			= -1;

	//
	// Separate port for each client to send data (this is the first one)
	//
	private int 										nextUDPListenPort			= -1;

	//
	// A flag to start and stop the server
	//
	private boolean									running						= false;

	//
	// A flag indicating if the battle has started
	//
	private boolean									hasBattleStarted			= false;

	//
	// A pointer to the explore server
	//
	private Server 									exploreServer				= null;
	
	//
	// A pointer to the battle in progress (as seen by the explore server)
	//
	private Battle 									battle						= null;

	
	
	/**
	 * Add this client's request to the connection pool.
	 * 
	 * @param request
	 */
	private void processRequest (Socket request)
	{
		synchronized (this.connectionPool)
		{
			this.connectionPool.add (this.connectionPool.size ( ), request);
			this.connectionPool.notifyAll ( );
		}
	}

	
	/**
	 * Constructor
	 */
	public BattleListenServer (Server exploreServer, Battle battle, String address, int listenPort, int countHumans)
	{
		this.exploreServer 	= exploreServer;
		this.battle			 	= battle;
		this.serverAddress 	= address;
		this.numberOfPlayers = countHumans;
		
		//
		// Debug only!
		//
		if (Debug.LUCAS)
		{
			System.out.println ("*** DEBUG: New Battle server created for " + countHumans + " players.");
		}
		
		//
		// Setup the the rest of the ports, based on the listen port
		//
		this.TCPListenPort 		= listenPort;
		this.UDPSendPort   		= this.TCPListenPort + Server.BattlePortSeed;
		this.nextTCPSendPort 	= this.UDPSendPort 	+ Server.BattlePortSeed;
		this.nextUDPListenPort 	= this.nextTCPSendPort;
	}
	

	/**
	 * Starts this server as a thread and waits for new players to join.-
	 */
	@Override
	public void run ( ) 
	{
		//
		// Create a pool of threads that will attend the client requests
		//
		for (int i = 0 ; i < this.numberOfPlayers ; i++)
		{
			Thread t = new JoinBattleServer (this);
			
			t.setPriority (Thread.MIN_PRIORITY);
			t.start 		  ( );
		}
		
		//
		// Change this thread's priority
		//
		Thread.currentThread ( ).setPriority (Thread.MIN_PRIORITY);

		//
		// This thread is running now
		//
		this.running = true;

		try
		{
			//
			// There's only one of these sockets.
			//
			this.UDPSendSocket = new DatagramSocket (this.UDPSendPort);

			//
			// There's only one of these too. We'll try to keep it as free as possible.
			//
			this.TCPListenSocket = new ServerSocket (this.TCPListenPort, 
														 		  BattleListenServer.INITIALIZATION_BACKLOG, 
																  InetAddress.getByName (this.serverAddress));
		}
		catch (Exception e)
		{
			//
			// Stop the server because of an initialization error
			//
			this.stopListening ( );
			
			System.err.println ("*** BattleListenServer ERROR: The server could not start!");
			e.printStackTrace ( );
		}
			
		//
		// Keep the listening cycle running ...
		//
		while (this.running)
		{
			try
			{
				//
				// Wait for clients willing to join the game ...
				// This method blocks the thread until someone calls ...
				//
				Socket clientRequest = this.TCPListenSocket.accept ( );
				
				//
				// Append this request to the join connection pool
				//
				this.processRequest (clientRequest);
			}
			catch (SocketException se)
			{
				//
				// This is not an error! We close the TCPListenSocket this way.
				//
				System.out.println ("*** INFO: Battle started! No new players may now join in.");
				this.running = false;
			}
			catch (IOException ioe)
			{
				//
				// This is a communication error
				//
				System.err.println  ("*** BattleListenServer ERROR: " + ioe.getMessage ( ));
				ioe.printStackTrace ( );
			}
		}
		
		//
		// Stop the server
		//
		System.out.println ("*** INFO: Battle started, stopped listening for new players ...");
	}


	/**
	 * Stops this server
	 */
	public void stopListening ( )
	{
		//
		// Stop the communications with all the clients
		//
		while (!this.remoteClients.isEmpty ( ))
		{
			if (this.remoteClients.get (0).isRunning ( ))
			{
				this.remoteClients.get (0).die ( );
			}
			this.remoteClients.remove (0);
		}
		
		//
		// Empty the completed join sessions
		//
		this.completedJoinSessions.removeAllElements ( );
		
		//
		// Try to close the opened sockets
		//
		try
		{
			this.UDPSendSocket.close   ( );
			this.TCPListenSocket.close ( );
		}
		catch (IOException e)
		{
			//
			// Could not close the socket ... no problem!
			//
		}
		
		//
		// Stop the game thread
		//
		this.serverGameThread.halt ( );
		
	}
	
	
	/**
	 * Start the battle! 
	 * This method should be called as soon as the last join timeout time is up.
	 */
	public void startBattle ( )
	{
		//
		// Check if the battle has already been started
		//
		if (!this.hasBattleStarted)
		{
			//
			// Create and start the game, as the server sees it
			//
			this.serverGameThread = new BattleServerGameThread (this.remoteClients);
			this.serverGameThread.setPriority (Thread.MIN_PRIORITY);
			this.serverGameThread.start 		  ( );
			
			this.hasBattleStarted = true;
		}
		
		//
		// Close the listening port for new players
		//
		try
		{
			this.TCPListenSocket.close ( );
		}
		catch (IOException se)
		{
			// Port server already closed, no problem!
		}
	}


	/**
	 * Appends a new network client (of this server) to the queue.
	 * 
	 * @param c
	 */
	public void addClient (RemoteClient c)
	{
		this.remoteClients.addElement (c);
	}


	/**
	 * Adds a new battle player to the queue.
	 *   
	 * @param sp
	 */
	public void addPlayer (BattleServerPlayer sp)
	{
		this.serverPlayers.addElement (sp);
	}


	/**
	 * Returns a reference to the vector containing the battle players.
	 * @return
	 */
	public Vector<BattleServerPlayer> getPlayers ( )
	{
		return (this.serverPlayers);
	}


	/**
	 * Returns the number of network clients actually connected.-
	 * 
	 * @return
	 */
	public int getNumberOfClients ( )
	{
		return this.remoteClients.size ( );
	}

	
	/**
	 * 
	 * @param c
	 */
	public void killClient (RemoteClient c)
	{
		this.remoteClients.removeElement (c);
	}


	/**
	 * 
	 * @param name
	 * @return
	 */
	public boolean isNameAllocated (String name)
	{
		return (this.allocatedNames.contains (name));
	}


	/**
	 * 
	 * @param name
	 */
	public void allocateName (String name)
	{
		this.allocatedNames.addElement (name);
	}


	/**
	 * Returns the next free TCP port.-
	 * 
	 * @return
	 */
	public int getNextTCPThreadPort ( )
	{
		int port = this.nextTCPSendPort;
		
		this.nextTCPSendPort += Server.BattlePortSeed;

		return (port);
	}


	/**
	 * Returns the next free UDP port.-
	 * 
	 * @return
	 */
	public int getNextUDPListenPort ( )
	{
		int port = this.nextUDPListenPort;
		
		this.nextUDPListenPort += Server.BattlePortSeed;

		return (port);
	}


	/**
	 * Returns the coordinates of the next free player's base.
	 * 
	 * @return
	 */
	public int [] getNextBaseCoordinates ( )
	{
		int [] ret_value = {BattleListenServer.BASE_COORDINATES[this.nextBaseIndex][0],
							     BattleListenServer.BASE_COORDINATES[this.nextBaseIndex][1]};
		
		this.nextBaseIndex ++;
		
		return ret_value;
	}


	/**
	 * Returns the next free player's color.
	 * 
	 * @return
	 */
	public int getNextColor ( )
	{
		int ret_value = BattleListenServer.PLAYER_COLORS [this.nextColorIndex];
		
		this.nextColorIndex ++;
				
		return ret_value;
	}

	
	/**
	 * Returns a reference to the UDP socket.-
	 * 
	 * @return
	 */
	public DatagramSocket getUDPSendSocket ( )
	{
		return (this.UDPSendSocket);
	}


	/**
	 * Returns the TCP port on which this server is binded.-
	 * 
	 * @return
	 */
	public int getTCPListenPort ( )
	{
		return (this.TCPListenPort);
	}


	/**
	 * Returns a reference to the explore server that spawned this battle.-
	 * 
	 * @return
	 */
	public Server getExploreServer ( )
	{
		return (this.exploreServer);
	}


	/**
	 * Returns a reference to the battle context in the explore server.-
	 * 
	 * @return
	 */
	public Battle getBattle ( )
	{
		return (this.battle);
	}
	
	
	/**
	 * Inform the explore server that we have a winner.-
	 */
	public void informAboutWinner (BattleServerPlayer battleWinner)
	{
		//
		// Get a reference to the explore player, who is the battle winner
		//
		Player winner = this.exploreServer.getGame ( ).getPlayer (battleWinner.getID ( ));
		
		//
		// Set the number of units the winner has after the battle
		//
		winner.setHover 	  (battleWinner.getNumberOfHoverjets ( ));
		winner.setTank 	  (battleWinner.getNumberOfTanks ( ));
		winner.setArtillery (battleWinner.getNumberOfArtillery ( ));
		
		//
		// This is how the Battle Server informs the Explore Server about the end of the battle and the winner
		//
		Command command = CommandFinishBattle.init (this.battle, winner);
	
		this.exploreServer.getGame ( ).schedule (command);
		
		//
		// Stop the communications
		//
		this.stopListening ( );
	}
}
