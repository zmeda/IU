package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

public class CommandGetPlayerProperties implements Command
{

	private Game														game;
	private Player														player;

	private static LinkedList<CommandGetPlayerProperties>	Pool	= new LinkedList<CommandGetPlayerProperties> ( );


	public static CommandGetPlayerProperties init (final Game game, final Player player)
	{
		CommandGetPlayerProperties task;
		if (CommandGetPlayerProperties.Pool.isEmpty ( ))
		{
			task = new CommandGetPlayerProperties ( );
		}
		else
		{
			task = CommandGetPlayerProperties.Pool.remove (0);
		}

		task.game = game;
		task.player = player;

		return task;
	}


	public void execute (final List<Message> messages)
	{
		// FIXME - what do I do here ??? or better yet when do I do it?
	}


	public void dispose ( )
	{
		CommandGetPlayerProperties.Pool.add (this);
	}
}
