package iu.server.explore;

import iu.android.network.explore.Protocol;
import iu.server.explore.game.Player;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class RemoteClientListenerTCP extends Thread
{

	private RemoteClient	client;
	private Socket			tcpListener;
	private boolean		running;


	public RemoteClientListenerTCP (final RemoteClient client, final Socket socket)
	{
		this.running = true;
		this.tcpListener = socket;
		this.client = client;
	}


	@Override
	public void run ( )
	{

		DataInputStream socketReader = null;

		try
		{
			socketReader = new DataInputStream (this.tcpListener.getInputStream ( ));
		}
		catch (IOException e)
		{
			this.running = false;
			e.printStackTrace ( );
		}

		while (this.running)
		{
			try
			{

				if (this.tcpListener.isClosed ( ))
				{
					this.running = false;
					System.out.println ("Connection closed.");

					// TODO : report to client

					continue;
				}

				//
				// FIXME () An EOF exception when starting BServer
				//
				Byte command = socketReader.readByte ( );
				System.out.println ("DEBUG (RemoteClientListener): command received : " + command);

				switch (command)
				{

					case (Protocol.BUFFER_EVENTS):
						{
							this.bufferEvents ( );
							break;
						}

					case (Protocol.FLUSH_EVENTS):
						{
							this.flushEvents ( );
							break;
						}

					case (Protocol.LOGOUT):
						{
							// Logging out the player to free his username for another login

							byte[] arr = new byte[1];
							arr[0] = command;
							this.client.execute (arr);

							this.running = false;
						}
					default:
						{
							int len = Protocol.length (command);
							if (len > 0)
							{
								byte[] arr = new byte[len];
								arr[0] = command;
								socketReader.readFully (arr, 1, Protocol.length (command) - 1);
								this.client.execute (arr);
							}
							break;
						}
				}

			}
			catch (EOFException e)
			{
				/**
				 * If client's side crashed then log out the player
				 */

				Player p = this.client.getPlayer ( );
				p.setLoggedIn (false);

				e.printStackTrace ( );
				this.running = false;
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace ( );
			}

		}
	}


	/**
	 * All events in sender thread will be buffered and will wait for release with flushEvents command
	 */
	private void flushEvents ( )
	{
		System.out.println ("DEBUG: flushEvents()");

	}


	/**
	 * 
	 * 
	 */
	private void bufferEvents ( )
	{
		System.out.println ("DEBUG: bufferEvents()");

	}


	/**
	 * 
	 * 
	 */
	public void stopRunning ( )
	{
		this.running = false;
	}
}
