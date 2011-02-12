package iu.server.explore.game;

import java.util.Timer;

/**
 * For execution of the {@link GameLoop} in a separate thread in regular intervals
 */
public class GameTimer extends Timer
{
	private Game			game;
	private GameLoop		loop;
	private final long	period;


	public GameTimer (final Game game, final long period)
	{

		this.period = period;
		this.game = game;
		this.loop = new GameLoop (this.game, this.period);
	}


	/**
	 * Start executing the loop in approximately regular periods
	 */
	public void start ( )
	{
		this.schedule (this.loop, 0, this.period);
	}


	public void execute (final Command cmd)
	{
		this.game.schedule (cmd);
	}
}
