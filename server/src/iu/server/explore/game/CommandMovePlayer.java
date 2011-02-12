package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Move the player and explore any new tiles in his visual range
 */
public class CommandMovePlayer implements Command
{
	private Player											player;
	private Location										location;

	private static LinkedList<CommandMovePlayer>	Pool	= new LinkedList<CommandMovePlayer> ( );


	public static CommandMovePlayer init (Player player, Location location)
	{
		CommandMovePlayer task;
		if (CommandMovePlayer.Pool.isEmpty ( ))
		{
			task = new CommandMovePlayer ( );
		}
		else
		{
			task = CommandMovePlayer.Pool.remove (0);
		}

		task.player = player;
		task.location = location;

		return task;
	}


	public void execute (List<Message> messages)
	{
		this.player.move (this.location, messages);
	}


	public void dispose ( )
	{
		CommandMovePlayer.Pool.add (this);
	}
}
