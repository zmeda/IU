package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

import iu.android.network.explore.Protocol;

/**
 * 
 * Answers a {@link Protocol#GET_FLAG_UNITS} command with {@link DataFlagUnits}
 */
public class CommandGetFlagUnits implements Command
{

	private Flag												flag;
	private Player												player;

	private static LinkedList<CommandGetFlagUnits>	Pool	= new LinkedList<CommandGetFlagUnits> ( );


	public static CommandGetFlagUnits init (final Player player, final Flag flag)
	{
		CommandGetFlagUnits task;
		if (CommandGetFlagUnits.Pool.isEmpty ( ))
		{
			task = new CommandGetFlagUnits ( );
		}
		else
		{
			task = CommandGetFlagUnits.Pool.remove (0);
		}

		task.player = player;
		task.flag = flag;

		return task;
	}


	public void execute (final List<Message> messages)
	{
		DataFlagUnits data = DataFlagUnits.init (this.flag, this.player);
		messages.add (data);
	}


	public void dispose ( )
	{
		CommandGetFlagUnits.Pool.add (this);
	}
}
