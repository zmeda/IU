package iu.server.explore.game;

import java.net.Socket;

import iu.server.Log;

public class JoinSocketHandler extends Thread
{
	/** A reference to the server whose join requests are handled by this Thread */
	private final Server	server;
	private boolean		running;


	public JoinSocketHandler (Server server)
	{
		this.server = server;
		// this.socketPool = new LinkedList<ServerSocket> ( );

	}


	@Override
	public void run ( )
	{
		while (this.running)
		{

			Socket socket;

			// Wait for an incoming connection ...
			synchronized (this.server.requestBuffer)
			{
				while (this.server.requestBuffer.isEmpty ( ))
				{
					try
					{
						this.server.requestBuffer.wait ( );
					}
					catch (InterruptedException e)
					{
						// OK
					}
				}

				socket = this.server.requestBuffer.removeFirst ( );
			}

			// ... and handle it in a new Thread
			new JoinGameThread (this.server, socket).start ( );
			Log.Lucas.d ("Server", "starting new JoinGameThread");
		}
	}


	@Override
	public synchronized void start ( )
	{
		this.running = true;
		super.start ( );
	}


	public void halt ( )
	{
		this.running = false;
	}
}
