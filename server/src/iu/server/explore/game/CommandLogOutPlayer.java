package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Logout the player
 * 
 */
public class CommandLogOutPlayer implements Command
{

	private static LinkedList<CommandLogOutPlayer>	Pool	= new LinkedList<CommandLogOutPlayer> ( );

	private Game												game;
	private Player												player;


	public static CommandLogOutPlayer init (final Game game, final Player player)
	{
		CommandLogOutPlayer task;
		if (CommandLogOutPlayer.Pool.isEmpty ( ))
		{
			task = new CommandLogOutPlayer ( );
		}
		else
		{
			task = CommandLogOutPlayer.Pool.remove (0);
		}

		task.game = game;
		task.player = player;

		return task;
	}


	public void execute (final List<Message> messages)
	{
		this.game.logOutPlayer (this.player, messages);
	}


	public void dispose ( )
	{
		CommandLogOutPlayer.Pool.add (this);
	}
}
