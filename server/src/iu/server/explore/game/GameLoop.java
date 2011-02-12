package iu.server.explore.game;

import java.util.Date;
import java.util.TimerTask;

import iu.server.Log;

public class GameLoop extends TimerTask
{
	private Game						game;
	private final double				dt;


	public GameLoop (Game game, long period)
	{
		this.dt = period / 1000.0d;
		this.game = game;
	}


	protected void setup ( )
	{
		// TODO initial state from DB
	}


	public void run ( )
	{
		long time = System.nanoTime ( );
		
		try
		{
			// Execute commands
			this.game.executeCommands ( );

			// Simulate - produces Messages
			this.game.integrate (this.dt);

			// Send Messages
			this.game.sendMessages ( );
		}
		catch (RuntimeException e)
		{
			Date d = new Date ( );
			System.err.println (d + " - " + e.getClass ( ).getName ( ));
			System.err.println (d + " - " + e.getMessage ( ));
			
			e.printStackTrace ( );
		}
		
		time = System.nanoTime ( ) - time;
		Log.Lucas.v("Game", "GameLoop.run()   " + (time/1000000) + "." + ((time%1000000) / 100) + " ms");
	}


	protected void cleanup ( )
	{
		// TODO
	}
	

	@Override
	public boolean cancel ( )
	{
		this.cleanup ( );
		return super.cancel ( );
	}
}
