package iu.server.explore.game;

import iu.android.network.explore.Protocol;
import iu.database.GameSocketWriter;
import iu.server.Log;
import iu.server.explore.RemoteClient;
import iu.server.explore.RemoteClientListenerTCP;
import iu.server.explore.RemoteClientSenderTCP;
import iu.server.explore.RemoteClientSenderUDP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;


/**
 * 
 * @author luka
 *
 */
public class JoinGameThread extends Thread
{

	private Socket					connection;
	private Server					server;

	private Player					player;
	private GameSocketWriter	dbReader;
	private boolean				running;
	private RemoteClient			client;


	public JoinGameThread (Server server, Socket socket)
	{
		this.server = server;
		this.connection = socket;

		// Create a writer to write game data to the socket stream
		this.dbReader = new GameSocketWriter (this.server.getGame ( ));
	}


	@SuppressWarnings("deprecation")
	@Override
	public void run ( )
	{

		DataOutputStream socketWriter = null;
		DataInputStream socketReader = null;

		this.running = false;

		//
		// We've got a socket to the joining client, so go ahead and get streams
		//
		try
		{
			socketWriter = new DataOutputStream (this.connection.getOutputStream ( ));
			socketReader = new DataInputStream (this.connection.getInputStream ( ));
			this.running = true;

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace ( );
		}

		try
		{
			//
			// Keep this join session in receive mode as long as the client has stuff to send.-
			// FIXME: Add a timeout connected with JOIN_TIMEOUT
			//
			while (this.running)
			{
				// String command = socketReader.readLine();
				final byte command = socketReader.readByte ( );
				int len;
				byte[] byteArr;

				System.out.println ("Command received: '" + command + "'");

				switch (command)
				{
					// For login
					case (Protocol.LOGIN_DATA):

						// Username
						len = socketReader.readByte ( );
						byteArr = new byte[len];
						socketReader.readFully (byteArr);
						String username = new String (byteArr);

						// Password
						len = socketReader.readByte ( );
						byteArr = new byte[len];
						socketReader.readFully (byteArr);
						String password = new String (byteArr);

						System.out.println ("Checking ('" + username + "','" + password + "')");

						//
						// create server client
						//
						this.client = new RemoteClient (this.server.getGame ( ), this.connection.getInetAddress ( ), username);

						this.player = this.client.login (this.server.getGame ( ), password);

						if (this.player != null)
						{
							socketWriter.writeByte (Protocol.OK);

							//
							// this is ugly, but im trying to fit in
							//

						}
						else
						{
							socketWriter.writeByte (Protocol.NOT_ACCEPTED);
						}

						//
						// FIXME - For DEMO only
						// Explore the field around all of the players flags (simulated move of player to that flag)
						//

						Iterator<Flag> flags = this.player.getGame ( ).getAllFlags ( );

						int c = 0;
						while (flags.hasNext ( ))
						{
							c++;
							Flag flag = flags.next ( );

							if (flag.getOwner ( ) == this.player)
							{
								CommandMovePlayer commMove = CommandMovePlayer.init (this.player, flag.location);

								this.player.execute (commMove);
							}
						}

					break;

					// For sign in
					case (Protocol.SIGNIN_DATA):

						// Username
						len = socketReader.readByte ( );
						byteArr = new byte[len];
						socketReader.readFully (byteArr);
						username = new String (byteArr);

						// Password
						len = socketReader.readByte ( );
						byteArr = new byte[len];
						socketReader.readFully (byteArr);
						password = new String (byteArr);

						this.player = this.server.getGame ( ).signInPlayer (username, password);

						if (this.player != null)
						{
							socketWriter.writeByte (Protocol.OK);
						}
						else
						{
							socketWriter.writeByte (Protocol.NOT_ACCEPTED);
						}

					break;

					// Load world info for the map
					case (Protocol.GET_MAP_INFO):

						socketWriter.writeByte (Protocol.MAP_INFO);

						this.dbReader.writeMapInfo (socketWriter, this.player);

					break;

					// Returns all of the players to the client
					case (Protocol.GET_PLAYERS):

						this.dbReader.writePlayers (socketWriter, Protocol.PLAYER);

						socketWriter.writeByte (Protocol.OK);

					break;

					// Returns commander info to the client
					case (Protocol.GET_COMMANDER):

						socketWriter.writeByte (Protocol.COMMANDER);

						this.dbReader.writeCommanderInfo (socketWriter, this.player);

					break;

					// Returns the fog of war of the player connecting
					case (Protocol.GET_FOG_OF_WAR):

						this.dbReader.writeFogOfWar (socketWriter, this.player, Protocol.FOG_OF_WAR);

						socketWriter.writeByte (Protocol.OK);

					break;

					// Returns all of the flags the player can see
					case (Protocol.GET_FLAGS):

						this.dbReader.writeFlags (socketWriter, this.player, Protocol.FLAG);

						socketWriter.writeByte (Protocol.OK);

					break;

					// case (Protocol.END_INITIALIZATION):
					//	
					// System.out.println ("Server: Protocol.CLOSE_STREAM");
					//	
					// this.stopRunning ( );
					// connection.close ( );
					// System.out.println ("JoinServer closed by " + connection.getInetAddress ( ).getHostAddress (
					// ));
					//	
					// break;

					case (Protocol.ABORT):

						this.stopRunning ( );
						this.connection.close ( );
						System.out.println ("JoinServer remotely aborted by " + this.connection.getInetAddress ( ).getHostAddress ( ));

					break;

					case (Protocol.GET_PLAYER_ID):

						socketWriter.writeByte (Protocol.PLAYER_ID);
						socketWriter.writeInt (this.player.id);

					break;

					case (Protocol.GET_MAP_SIZE):

						// Game setup queries
						socketWriter.write (Protocol.NOT_ACCEPTED);
						socketWriter.writeByte (Protocol.MAP_SIZE);
						// TODO - map size ???
						socketWriter.writeByte (64);

					break;

					case (Protocol.GET_MAP_SEED):

						socketWriter.writeByte (Protocol.MAP_SEED);
						socketWriter.writeInt (3);

					break;

					case (Protocol.GET_NUMBER_OF_UNITS):

						socketWriter.writeByte (Protocol.NUMBER_OF_UNITS);
						socketWriter.writeInt (20);
						// socketWriter.writeInt (GameServer.INITIAL_NUMBER_OF_UNITS);

					break;

					//
					// Join session ends with this command
					//
					case (Protocol.END_INITIALIZATION):

						Log.Lucas.d ("Network", "Initializing client threads " + this.client);
						this.initClientThreads ( );
						this.stopRunning ( );

					break;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace ( );
		}

	}


	private void initClientThreads ( )
	{

		//
		// create send and receive threads for TCP
		//
		RemoteClientListenerTCP tcpListener = new RemoteClientListenerTCP (this.client, this.connection);
		RemoteClientSenderTCP tcpSender = new RemoteClientSenderTCP (this.client, this.connection);
		RemoteClientSenderUDP udpSender = this.server.getUdpSender ( );

		//
		// UDP connections will be established when client connect trought UDP port
		//

		tcpListener.start ( );
		tcpSender.start ( );

		this.client.setTcpListener (tcpListener);
		this.client.setTcpSender (tcpSender);
		this.client.setUdpSender (udpSender);
	}


	public void stopRunning ( )
	{
		this.running = false;
	}
}
