package iu.server.explore.game;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import iu.database.GameFactory;
import iu.server.Log;
import iu.server.battle.BattleListenServer;
import iu.server.explore.RemoteClientSenderUDP;
import iu.server.explore.UDPListener;

public class Server extends Thread
{

	/** Rate of execution of game logic */
	private static final int						TicksPerSecond		= 10;

	/** Ports for battles start here */
	public static final int							BattleStartPort	= 4000;
	public static final int							BattlePortSeed		= 10;
	public static final int							BattleEndPort		= BattleStartPort + BattlePortSeed;

	/** Server listens on this port for requests to join the game */
	public static final int							ExplorePort			= 3000;

	/** Server sends UDP packets on this port */
	public static final int							ExploreSendPort	= 3001;

	private static final int						Backlog				= 10;

	/** Bytes */
	public static final int							DatagramSize		= 128;

	private static final LinkedList<Integer>	AvailablePorts;

	static
	{
		AvailablePorts = new LinkedList<Integer> ( );

		for (int i = BattleStartPort; i < BattleEndPort; i++)
		{
			AvailablePorts.add (i);
		}
	}


	/**
	 * Get the next available port
	 * 
	 * @return next available port
	 * 
	 * @throws NullPointerException
	 *            when no ports are available
	 */
	private static int nextPort ( )
	{
		if (!AvailablePorts.isEmpty ( ))
		{
			Integer port = AvailablePorts.removeFirst ( );
			return port.intValue ( );
		}
		else
		{
			throw new NullPointerException ("No ports available");
		}
	}


	/**
	 * Releases the port so it can be reused by {@link Server#nextPort()}
	 * 
	 * @param port
	 *           port number to be released
	 * 
	 * @throws IllegalArgumentException
	 *            <ul>
	 *            <li>when port is not between {@value Server#BattleStartPort} and
	 *            {@value Server#BattleEndPort}</li>
	 *            <li>or when port is not already available</li>
	 *            </ul>
	 */
	private static void releasePort (int port)
	{
		if (port < BattleStartPort || port > BattleEndPort)
		{
			throw new IllegalArgumentException ("port [" + port + "] must be between " + BattleStartPort
					+ " and " + BattleEndPort + ".");
		}
		else if (AvailablePorts.contains (port))
		{
			throw new IllegalArgumentException ("port  [" + port + "] is already available");
		}
		else
		{
			AvailablePorts.addLast (new Integer (port));
		}
	}


	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main (String[] args) throws IOException
	{
		String address;
		try
		{
			address = args[0];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			address = "localhost";
		}

		Log.Lucas.i ("Server", "Listening on " + address);

		GameFactory gf = GameFactory.getInstance ( );
		final Game game = gf.createGame ("Ljubljana");

		final Server server = new Server (address, game);
		server.begin ( );
	}

	//
	// End Static stuff
	//
	//
	// Begin Instance stuff
	//

	/** For execution of game loops in regular intervals */
	private final GameTimer								timer;

	/** The game running on this server */
	private final Game									game;

	/** Address that this Server will listen on */
	private final String									host;
	private final InetAddress							address;

	/** several threads for handling join requests */
	private final LinkedList<JoinSocketHandler>	joinHandlers;

	/** UDP traffic from clients goes through here */
	private final UDPListener							udpListener;
	private final RemoteClientSenderUDP				udpSender;

	private boolean										running;

	/** The TCP listen socket */
	private ServerSocket									exploreJoinSocket;

	/** The UDP send socket */
	private DatagramSocket								exploreSendSocket;

	final LinkedList<Socket>							requestBuffer;


	public Server (String host, Game game) throws IOException
	{
		this.host = host;
		this.address = InetAddress.getByName (this.host);
		this.game = game;
		this.game.setServer (this);
		this.timer = new GameTimer (game, 1000 / TicksPerSecond);
		this.requestBuffer = new LinkedList<Socket> ( );
		this.joinHandlers = new LinkedList<JoinSocketHandler> ( );
		this.udpListener = new UDPListener (ExplorePort);

		DatagramSocket senderSocket = new DatagramSocket ( );
		this.udpSender = new RemoteClientSenderUDP (senderSocket);

		for (int i = 1; i < 5; i++)
		{
			this.joinHandlers.add (new JoinSocketHandler (this));
		}
	}


	/**
	 * Start the Server and listen on the address specified in constructor
	 * <ul>
	 * <li>Opens a TCP socket to listen for join requests on {@value Server#ExplorePort}
	 * <li>Opens a UDP socket for sending on {@value Server#ExploreSendPort}
	 * <li>Starts a new thread to handle join requests
	 * <li>Starts a new thread for executing game logic in regular intervals
	 * </ul>
	 * 
	 * @throws IOException
	 *            when not able to open network connections
	 */
	public void begin ( ) throws IOException
	{
		Log.Lucas.v ("Server", "Starting Server");
		this.exploreJoinSocket = new ServerSocket (ExplorePort, Backlog, this.address);
		this.exploreSendSocket = new DatagramSocket (ExploreSendPort, this.address);

		for (JoinSocketHandler thread : this.joinHandlers)
		{
			thread.start ( );
		}

		this.timer.start ( );
		this.udpListener.start ( );
		this.running = true;
		this.start ( );
	}


	/**
	 * Stop the server. Closes network connections, stops threads.
	 * 
	 * @throws IOException
	 */
	public void end ( ) throws IOException
	{
		this.udpListener.disconnect ( );
		for (JoinSocketHandler thread : this.joinHandlers)
			thread.halt ( );

		this.timer.cancel ( );

		this.running = false;
		this.exploreJoinSocket.close ( );
		this.exploreSendSocket.close ( );
	}


	@Override
	public void run ( )
	{
		while (this.running)
		{
			Socket s;
			try
			{
				// Wait for incoming connections ...
				s = this.exploreJoinSocket.accept ( );

				synchronized (this.requestBuffer)
				{
					this.requestBuffer.add (s);
					this.requestBuffer.notify ( );
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace ( );
			}
		}
	}


	/**
	 * Tries to create a BattleServer on the address, automatically acquiring the port number. Method may fail
	 * (returning <code>null</code>) if no ports are available
	 * 
	 * @param address
	 * @return a new {@link BattleListenServer} or <code>null</code>
	 */
	public BattleListenServer createBattleServer (Battle battle)
	{
		try
		{
			int port = nextPort ( );
			int countHumans = 0;

			boolean attackerHuman = !battle.attacker.isAI ( ) && battle.attacker.isLoggedIn ( );
			boolean defenderHuman = !battle.defender.isAI ( ) && battle.defender.isLoggedIn ( );

			if (attackerHuman)
				countHumans++;
			if (defenderHuman)
				countHumans++;

			System.out.println ("Attacker AI=" + !attackerHuman);
			System.out.println ("Defender AI=" + !defenderHuman);
			System.out.println ("CountHumans=" + countHumans);

			BattleListenServer battleServer = new BattleListenServer (this, battle, this.host, port, countHumans);
			Log.Lucas.i ("Server", "New " + battleServer.getClass ( ).getName ( ) + " on " + port);
			return battleServer;
		}
		catch (Exception e)
		{
			e.printStackTrace ( );
			return null;
		}
	}


	/**
	 * Destroy the battle server and release ports it was using
	 * 
	 * @param server
	 */
	public void destroyBattleServer (BattleListenServer server)
	{
		// the port number that I gave you before in createBattleServer
		int port = server.getTCPListenPort ( );
		server.stopListening ( );

		try
		{
			releasePort (port);
		}
		catch (Exception e)
		{
			e.printStackTrace ( );
		}
	}


	/**
	 * The game running on this server
	 * 
	 * @return
	 */
	public Game getGame ( )
	{
		return this.game;
	}


	public ServerSocket getExploreJoinSocket ( )
	{
		return this.exploreJoinSocket;
	}


	public RemoteClientSenderUDP getUdpSender ( )
	{
		return udpSender;
	}
}
