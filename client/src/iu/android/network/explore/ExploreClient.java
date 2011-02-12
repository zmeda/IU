package iu.android.network.explore;

/*************************************************************************************************************
 * IU 1.0b, a java realtime strategy game
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

import iu.android.Debug;
import iu.android.battle.BattleEngine;
import iu.android.comm.GameWorld;
import iu.android.engine.PlayerRegistry;
import iu.android.explore.Commander;
import iu.android.explore.ExploreMode;
import iu.android.explore.ExplorePlayer;
import iu.android.explore.Flag;
import iu.android.explore.FogOfWar;
import iu.android.explore.Rank;
import iu.android.order.Order;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * This class is the network client each player uses when (s)he wants to play a networked game.-
 * 
 * main: vector inqueue, vector outqueue. [spawn receiver and sender threads] loop: if 'inqueue' contains some
 * data, print it out. if (random) put some random data into the 'outqueue' vector
 */
public class ExploreClient
{
	//
	// This client's character encoding. It must match the one on the server side.-
	//
	protected final static String	ENCODING				= "US-ASCII";

	//	
	// Always stays the same (as long as you only run 1 client per machine)
	//
	// private static int ClientListenPort = 10000;

	//
	// Always stays the same (as long as you only run 1 client per machine)
	//
	// private static int ClientSendPort = 10010;

	private static int				ServerListenPort	= 3000;			// negotiation port for initialization
	private static int				ServerUDPPort		= 0;				// get from server - unique for each client
	private static int				TcpThreadPort		= 0;				// get from server - unique for each client

	private static int				DataPacketSize		= 128;			// size of datagram content (in bytes)
	private static int				PlayerID				= -1;			// get from server - unique for each client.
	private boolean					running				= true;

	private ArrayList<Order>		inqueue;
	private ArrayList<Order>		outqueue;

	private DatagramSocket			sendSocket			= null;
	private DatagramSocket			listenSocket		= null;

	private DataOutputStream		socketWriter;
	private DataInputStream			socketReader;

	// private ExploreClientTCPThread tcpThread;

	private InetAddress				server;
	private ServerSocket				serverSocket;

	// TODO - game needed only if in battle mode
	private BattleEngine						game					= null;

	private Socket						clientSocket		= null;

	private String						playerName			= null;

	private boolean					loggedIn				= false;

	private Sender						sender;

	private ReceiverThread			receiver;

	private ExploreMode				exploreMode;

	//
	// Default modem speed if not changed by client's configuration file
	//
	private static int				ConnectionSpeed	= 14400;
	private static int				PacketsPerSecond	= 14;
	private static int				InterpacketDelay	= 71;

	private static Properties		configuration;


	/**
	 * A helper method to read from the opened client socket.
	 * 
	 * @return A line of data
	 * 
	 * private CharSequence read ( ) { String received = null;
	 * 
	 * try { received = this.socketReader.readLine ( ); } catch (IOException ioe) { // // The socket got closed
	 * (lost connection, server quit, etc.) // Log.w (Debug.TAG, "Couldn't communicate with server in
	 * JoinClient"); } return received; }
	 */

	/**
	 * Constructor
	 */
	public ExploreClient ( )
	{

		//
		// The configuration is kept in a properties file
		//
		ExploreClient.configuration = new Properties ( );

		/*
		 * // FIXME: The path of this configuration file must be a configurable resource! // String propsFile =
		 * System.getProperties ( ).getProperty ("user.dir") + "/iu/desktop/config/client.conf";
		 * 
		 * try { ExploreClient.configuration.load (new FileInputStream (propsFile));
		 * 
		 * ExploreClient.DataPacketSize = Integer.parseInt (ExploreClient.configuration.getProperty
		 * ("DataPacketSize")); ExploreClient.ConnectionSpeed = Integer.parseInt
		 * (ExploreClient.configuration.getProperty ("ConnectionSpeed")); ExploreClient.ServerListenPort =
		 * Integer.parseInt (ExploreClient.configuration.getProperty ("ServerListenPort"));
		 * ExploreClient.ClientListenPort = Integer.parseInt (ExploreClient.configuration.getProperty
		 * ("ClientListenPort")); ExploreClient.ClientSendPort = Integer.parseInt
		 * (ExploreClient.configuration.getProperty ("ClientSendPort"));
		 * 
		 * ExploreClient.PacketsPerSecond = ExploreClient.ConnectionSpeed / (ExploreClient.DataPacketSize * 8);
		 * ExploreClient.InterpacketDelay = 1000 / ExploreClient.PacketsPerSecond; } catch (IOException ioe) {
		 * Log.w (Debug.TAG, "Could not load [" + propsFile + "] configuration file. Using defaults."); }
		 */
	}


	/**
	 * Returns true if server address is valid and a server was found at that address (handshaking required)
	 * 
	 * @param serverIP
	 * @return
	 */
	public boolean joinToServer (final String serverIP)
	{
		//
		// Connect to the server
		//
		try
		{
			this.server = InetAddress.getByName (serverIP);
			this.clientSocket = new Socket (this.server, ExploreClient.ServerListenPort);
			this.socketWriter = new DataOutputStream (this.clientSocket.getOutputStream ( ));
			this.socketReader = new DataInputStream (this.clientSocket.getInputStream ( ));
		}
		catch (UnknownHostException uhe)
		{
			Log.e (Debug.TAG, "Server address " + serverIP + " could not be resolved.");
			return (false);
		}
		catch (IOException ioe)
		{
			Log.e (Debug.TAG, "Server at " + serverIP + " is not responding.");
			Log.e (Debug.TAG, ioe.toString ( ));
			return (false);
		}

		//
		// Start the ping client to measure network latency
		//
		// this.pingThread = this.startPingThread ( );

		//
		// Success!
		//
		return (true);
	}


	/**
	 * Waits the order from the server to start the game.-
	 * 
	 * @throws IOException
	 */
	public void waitForGameStart ( ) throws IOException
	{

		boolean notStarted = true;

		while (notStarted)
		{
			//
			// The next line blocks execution until data is received from the server
			//
			// CharSequence command = this.socketReader.readLine();
			byte command = this.socketReader.readByte ( );

			// if (CharSequences.equals(command, CharSequences.forAsciiBytes(Protocol.START_GAME)))
			if (command == Protocol.START_EXPLORE_MODE)
			{
				notStarted = false;
				this.game.startGame ( );
				Debug.Lucas.println ("----- S T A R T E D ------");
			}
		}
	}


	public String getRank ( )
	{
		return PlayerRegistry.getPlayer (this.playerName).getRank ( ).longName;
	}


	/**
	 * Sets the game context on this client.-
	 * 
	 * @param game
	 */
	// public void setGame (final BattleEngine game)
	// {
	//
	// if (game == null)
	// {
	// Log.w (Debug.TAG, "Argument is null in JoinClient.setGame ( )");
	// }
	// else
	// {
	// this.game = game;
	// }
	// }
	/**
	 * 
	 * @param player
	 */
	// public void setUser (final LocalPlayer player)
	// {
	//
	// if (player == null)
	// {
	// Log.w (Debug.TAG, "Argument is null in JoinClient.setUser ( )");
	// }
	// else
	// {
	// player.setOutqueue (this.outqueue);
	// }
	// }
	/**
	 * 
	 * @return
	 */
	// public long getMapSeed ( )
	// {
	//
	// long seed = 0L;
	//
	// try
	// {
	// this.socketWriter.write (Protocol.GET_MAP_SEED);
	// // this.socketWriter.write(Protocol.DELIMITER);
	//
	// // String response = this.socketReader.readLine();
	// byte response = this.socketReader.readByte ( );
	//
	// // if (response.startsWith(String.valueOf(Protocol.SET_MAP_SEED)))
	// if (response != Protocol.MAP_SEED)
	// {
	// throw new RuntimeException ("Expecting Protocol.MAP_SEED");
	// }
	// // String seedPart = (response.split("="))[1];
	//
	// // try
	// // {
	// // seed = Long.parseLong(seedPart);
	// seed = this.socketReader.readLong ( );
	// // }
	// // catch (NumberFormatException nfe)
	// // {
	// // Log.w(Debug.TAG, "Couldn't PARSE the obtained seed in JoinClient.getMapSeed ( )");
	// // }
	// // }
	// // else
	// // {
	// // Log.w(Debug.TAG, "Couldn't get a random map seed in JoinClient.getMapSeed ( )");
	// // }
	// }
	// catch (IOException ioe)
	// {
	// Log.e (Debug.TAG, "Init socket not set up in JoinClient.getMapSeed ( )");
	// }
	//
	// //
	// // Return
	// //
	// return (seed);
	// }
	/**
	 * 
	 * @param unitx
	 */
	// public int[] getUnitsX (/* int[] unitx */)
	// {
	//
	// try
	// {
	// this.socketWriter.writeByte (Protocol.GET_UNIT_X);
	// // this.socketWriter.writeByte(Protocol.DELIMITER);
	//
	// // String response = this.socketReader.readLine();
	// byte response = this.socketReader.readByte ( );
	//
	// if (response != Protocol.UNIT_X)
	// {
	// throw new RuntimeException ("Expecting Protocol.UNIT_X");
	// }
	//
	// Debug.Lucas.println ("unitx=" + response);
	//
	// short unitCount = this.socketReader.readShort ( );
	// int[] unitx = new int[unitCount];
	//
	// for (int i = 0; i < unitCount; i++)
	// {
	// unitx[i] = this.socketReader.readInt ( );
	// }
	//
	// return unitx;
	// // int i = 0;
	// // StringTokenizer st = new StringTokenizer(response, ",");
	// // while (st.hasMoreTokens())
	// // {
	// // String x = st.nextToken();
	// // int xval = Integer.parseInt(x);
	// // unitx[i] = xval;
	// // i++;
	// // }
	// }
	// catch (IOException ioe)
	// {
	// Log.e (Debug.TAG, "Init socket not set up in JoinClient.getUnitsX ( )");
	// }
	// catch (NumberFormatException nfe)
	// {
	// Log.w (Debug.TAG, "Error parsing response in JoinClient.getUnitsX ( )");
	// }
	//
	// return null;
	// }
	/**
	 * 
	 * @param unity
	 */
	// public int[] getUnitsY (/* int[] unitx */)
	// {
	//
	// try
	// {
	// this.socketWriter.writeByte (Protocol.GET_UNIT_Y);
	// // this.socketWriter.writeByte(Protocol.DELIMITER);
	//
	// // String response = this.socketReader.readLine();
	// byte response = this.socketReader.readByte ( );
	//
	// Debug.Lucas.println ("unity=" + response);
	//
	// short unitCount = this.socketReader.readShort ( );
	// int[] unitx = new int[unitCount];
	//
	// for (int i = 0; i < unitCount; i++)
	// {
	// unitx[i] = this.socketReader.readInt ( );
	// }
	//
	// return unitx;
	// // int i = 0;
	// // StringTokenizer st = new StringTokenizer(response, ",");
	// // while (st.hasMoreTokens())
	// // {
	// // String x = st.nextToken();
	// // int xval = Integer.parseInt(x);
	// // unitx[i] = xval;
	// // i++;
	// // }
	// }
	// catch (IOException ioe)
	// {
	// Log.e (Debug.TAG, "Init socket not set up in JoinClient.getUnitsX ( )");
	// }
	// catch (NumberFormatException nfe)
	// {
	// Log.w (Debug.TAG, "Error parsing response in JoinClient.getUnitsX ( )");
	// }
	//
	// return null;
	// }
	/**
	 * 
	 * @return
	 */
	// public int getNumUnits ( )
	// {
	//
	// int numUnits = -1;
	// try
	// {
	// this.socketWriter.writeByte (Protocol.GET_NUMBER_OF_UNITS);
	// // this.socketWriter.write(Protocol.DELIMITER);
	//
	// // String response = this.socketReader.readLine();
	// byte response = this.socketReader.readByte ( );
	//
	// // if (response.startsWith(String.valueOf(Protocol.SET_NUMBER_OF_UNITS)))
	// if (response == Protocol.SET_NUMBER_OF_UNITS)
	// {
	// // String numPart = (response.split("="))[1];
	//
	// try
	// {
	// // numunits = Integer.parseInt(numPart);
	// numUnits = this.socketReader.readShort ( );
	// }
	// catch (NumberFormatException nfe)
	// {
	// Log.w (Debug.TAG, "Couldn't PARSE the obtained number in JoinClient.getNumUnits ( )");
	// }
	// }
	// else
	// {
	// Log.w (Debug.TAG, "Couldn't get a sensible response in JoinClient.getNumUnits ( )");
	// }
	// }
	// catch (IOException ioe)
	// {
	// Log.e (Debug.TAG, "Init socket not set up in JoinClient.getNumUnits ( )");
	// }
	// return numUnits;
	// }
	// public int getClientID ( )
	// {
	//
	// int id = -1;
	// try
	// {
	// this.socketWriter.writeByte (Protocol.GET_PLAYER_ID);
	// // this.socketWriter.write(Protocol.DELIMITER);
	//
	// // String response = this.socketReader.readLine();
	//
	// if (this.socketReader.readByte ( ) != Protocol.PLAYER_ID)
	// {
	// throw new RuntimeException ("Expecting Protocol.PLAYER_ID");
	// }
	//
	// // try
	// // {
	// // id = Integer.parseInt(response);
	// id = this.socketReader.readInt ( );
	// // }
	// // catch (NumberFormatException nfe)
	// // {
	// // Debug.Lucas.println("Warning: client.getClientID() couldn't PARSE the obtained number.");
	// // }
	// }
	// catch (IOException ioe)
	// {
	// Debug.Lucas.println ("JoinClient.getClientID(): init socket not set up");
	// }
	// ExploreClient.PlayerID = id;
	// return id;
	// }
	// public boolean getMapSize ( ) throws IOException
	// {
	//
	// this.socketWriter.writeByte (Protocol.GET_MAP_SIZE);
	//
	// if (this.socketReader.readByte ( ) != Protocol.MAP_SIZE)
	// {
	// throw new RuntimeException ("Expecting Protocol.MAP_SIZE");
	// }
	//
	// // return (response.equals("YES"));
	// return this.socketReader.readInt ( ) == 64;
	// }
	/**
	 * Check if this player's color has not yet been taken.-
	 * 
	 * @param color
	 *           The RGB color representation.-
	 * @return
	 * @throws IOException
	 */
	// public boolean checkColor (final int color) throws IOException
	// {
	//
	// // String col = "" + color;
	//
	// this.socketWriter.writeByte (Protocol.SET_PLAYER_COLOR);
	// // this.socketWriter.write(color);
	// this.socketWriter.writeInt (color);
	// // this.socketWriter.write(Protocol.DELIMITER);
	//
	// // String response = this.socketReader.readLine();
	// byte response = this.socketReader.readByte ( );
	//
	// if (this.socketReader.readByte ( ) != Protocol.PLAYER_COLOR)
	// {
	// throw new RuntimeException ("Expecting Protocol.PLAYER_COLOR");
	// }
	//
	// int c = this.socketReader.readInt ( );
	//
	// // return (response.equals("OK"));
	// return c == color;
	// }
	/**
	 * Check if this player's name has not yet been taken.-
	 * 
	 * @param playerName
	 * @return
	 * @throws IOException
	 */
	// public boolean checkName (final String playerName) throws IOException
	// {
	//
	// this.socketWriter.writeByte (Protocol.SET_PLAYER_NAME);
	// this.socketWriter.writeByte ((byte) playerName.length ( ));
	// this.socketWriter.writeChars (playerName);
	//
	// if (this.socketReader.readByte ( ) != Protocol.PLAYER_NAME)
	// {
	// throw new RuntimeException ("Expecting Protocol.PLAYER_NAME");
	// }
	//
	// // get name
	// int len = this.socketReader.readByte ( );
	// byte[] nameBuffer = new byte[len];
	// this.socketReader.readFully (nameBuffer);
	//
	// return playerName.equals (new String (nameBuffer));
	// }
	public void endInitialization ( )
	{

		try
		{
			this.socketWriter.writeByte (Protocol.END_INITIALIZATION);

			this.switchToGameNetworking ( );
			// clientSocket will from now on serve for command send and event listening
			// this.clientSocket.close ( );
		}
		catch (IOException ioe)
		{
			Log.w (Debug.TAG, "Could not close init socket in JoinClient.closeInitialisationStream ( )");
		}

	}


	public void switchToGameNetworking ( )
	{
		this.sender = new Sender (this.clientSocket);
		this.receiver = new ReceiverThread (this.clientSocket);
		this.receiver.start ( );
	}


	/**
	 * Returns sender for explore mode
	 * 
	 * @return
	 */
	public Sender getSender ( )
	{
		return this.sender;
	}


	public ReceiverThread getReceiverThread ( )
	{
		return this.receiver;
	}


	public void switchToReceive ( ) throws IOException
	{

		//
		// Tell the server that we finished sending data
		//
		this.socketWriter.writeByte (Protocol.SEND_MODE);
		// this.socketWriter.write(Protocol.DELIMITER);
	}


	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean sendLoginData (final String username, final String password)
	{
		try
		{
			this.socketWriter.writeByte (Protocol.LOGIN_DATA);
			this.socketWriter.writeByte (username.length ( ));
			this.socketWriter.writeBytes (username);
			this.socketWriter.writeByte (password.length ( ));
			this.socketWriter.writeBytes (password);

			byte answer = this.socketReader.readByte ( );

			if (answer == Protocol.OK)
			{
				this.playerName = username;
				PlayerRegistry.setLocalPlayer (username);
				this.loggedIn = true;

				return true;
			}
			else if (answer == Protocol.NOT_ACCEPTED)
			{
				return false;
			}
			else
			{
				throw new IOException ("Error logging in player '" + username + "'");
			}

		}
		catch (IOException ioe)
		{
			ioe.printStackTrace ( );
		}

		return false;
	}


	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean sendSignInData (final String username, final String password)
	{

		try
		{
			this.socketWriter.writeByte (Protocol.SIGNIN_DATA);
			this.socketWriter.writeByte (username.length ( ));
			this.socketWriter.writeBytes (username);
			this.socketWriter.writeByte (password.length ( ));
			this.socketWriter.writeBytes (password);

			byte answer = this.socketReader.readByte ( );

			if (answer == Protocol.OK)
			{
				this.playerName = username;
				PlayerRegistry.setLocalPlayer (username);
				this.loggedIn = true;

				return true;
			}
			else if (answer == Protocol.NOT_ACCEPTED)
			{
				return false;
			}
			else
			{
				throw new IOException ("Error logging in player '" + username + "'");
			}

		}
		catch (IOException ioe)
		{
			ioe.printStackTrace ( );
		}

		return false;
	}


	/**
	 * 
	 * @return
	 */
	public Commander loadCommander ( )
	{

		try
		{
			this.socketWriter.writeByte (Protocol.GET_COMMANDER);

			if (this.socketReader.readByte ( ) != Protocol.COMMANDER)
			{
				throw new IOException ("Unexpected command received in ExploreClient.");
			}

			Commander comm = new Commander (this.playerName, this.socketReader.readShort ( ), this.socketReader.readShort ( ), this.socketReader.readShort ( ));
			
			return comm; 
		}
		catch (IOException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace ( );
		}

		return null;
	}


	/**
	 * 
	 * @param gameWorld
	 * @return
	 */
	public FogOfWar loadFogOfWar (final GameWorld gameWorld)
	{

		FogOfWar fow = new FogOfWar (gameWorld);

		try
		{
			this.socketWriter.writeByte (Protocol.GET_FOG_OF_WAR);

			byte comm = 0;
			while ((comm = this.socketReader.readByte ( )) == Protocol.FOG_OF_WAR)
			{
				fow.initTile (this.socketReader.readShort ( ), this.socketReader.readShort ( ), this.socketReader.readLong ( ));
			}

			if (comm != Protocol.OK)
			{
				throw new IOException ("Expecting Protocol.OK at end of FOW string.");
			}

		}
		catch (IOException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace ( );
		}

		return fow;
	}


	/**
	 * 
	 * @return
	 */
	public boolean loadPlayers ( )
	{
		ExplorePlayer ep = null;

		try
		{
			this.socketWriter.writeByte (Protocol.GET_PLAYERS);

			byte comm = 0;
			while ((comm = this.socketReader.readByte ( )) == Protocol.PLAYER)
			{
				// id
				int id = this.socketReader.readInt ( );

				// name
				byte nameLen = this.socketReader.readByte ( );
				byte[] byteArr = new byte[nameLen];
				this.socketReader.readFully (byteArr);
				String name = new String (byteArr);

				// Rank
				byte rank = this.socketReader.readByte ( );

				// The player is automatically added into the PlayerRegistry
				ep = new ExplorePlayer (id, name, Rank.values ( )[rank]);
				
				Log.d ("IU", "Added player " + ep.getName ( ) + " - " + ep.getRank ( ));
			}

			if (comm != Protocol.OK)
			{
				throw new IOException ("Expecting Protocol.OK at end of player string.");
			}
		}
		catch (IOException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace ( );
		}

		return true;
	}


	/**
	 * 
	 * @return
	 */
	public GameWorld loadMapInfo ( )
	{

		try
		{
			this.socketWriter.writeByte (Protocol.GET_MAP_INFO);

			if (this.socketReader.readByte ( ) != Protocol.MAP_INFO)
			{
				return null;
			}

			float lat = this.socketReader.readFloat ( );
			float lon = this.socketReader.readFloat ( );
			float h = this.socketReader.readFloat ( );
			float w = this.socketReader.readFloat ( );
			int fowSizePow = this.socketReader.readByte ( );

			// TODO - This is only close - Meters per degree on the equator
			float mPerDegree = 40E6f / 360.0f;

			// Set the range of the commander
			Commander.setViewDistance (mPerDegree * 2.5f * h / (1 << (fowSizePow + 3)));

			return new GameWorld (lat, lon, w, h, fowSizePow);

		}
		catch (IOException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace ( );
		}

		return null;
	}


	/**
	 * 
	 * @return
	 */
	public boolean loadFlags ( )
	{

		try
		{
			this.socketWriter.writeByte (Protocol.GET_FLAGS);

			byte comm = 0;
			while ((comm = this.socketReader.readByte ( )) == Protocol.FLAG)
			{
				// flag id
				int flagId = this.socketReader.readInt ( );
				float locLat = this.socketReader.readFloat ( ); // lat
				float locLong = this.socketReader.readFloat ( ); // lon
				Location loc = new Location (LocationManager.GPS_PROVIDER);
				loc.setLatitude (locLat);
				loc.setLongitude (locLong);

				int playerId = this.socketReader.readInt ( ); // player id

				ExplorePlayer p = PlayerRegistry.getPlayer (playerId);

				int numh = this.socketReader.readShort ( );
				int numt = this.socketReader.readShort ( );
				int numa = this.socketReader.readShort ( );

				new Flag (loc, p, flagId/* , Flag.TYPE_FLAG_UNKNOWN */, numh, numt, numa);
			}

			if (comm != Protocol.OK)
			{
				throw new IOException ("Protocol.OK expected at end of flag string");
			}
		}
		catch (IOException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace ( );
		}

		return true;
	}


	/**
	 * 
	 */
	public void logout ( )
	{
		if (this.loggedIn)
		{
			this.receiver.stopRunning ( );
		}

		try
		{
			if (this.sender != null)
			{
				this.sender.notifyLogout ( );
			}

			this.clientSocket.close ( );
		}
		catch (IOException e)
		{
			//
		}

	}


	public InetAddress getServerAddress ( )
	{
		return this.server;
	}


	/**
	 * 
	 * @return
	 */
	public String getPlayerName ( )
	{
		return this.playerName;
	}


	/**
	 * Returns this client's (player's) ID
	 * 
	 * @return
	 */
	public static int getID ( )
	{

		return ExploreClient.PlayerID;
	}


	public DataInputStream getSocketReader ( )
	{
		return this.socketReader;
	}


	public void setExploreMode (final ExploreMode mode)
	{
		this.exploreMode = mode;
	}


	public ExploreMode getExploreMode ( )
	{
		return this.exploreMode;
	}
}
