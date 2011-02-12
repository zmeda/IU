package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

/**
 * Change players properties
 */
public class CommandSetPlayerProperties implements Command
{
	private Player													player;
	private short													numHovercraft;
	private short													numTank;
	private short													numArtillery;

	private static LinkedList<CommandSetPlayerProperties>	Pool	= new LinkedList<CommandSetPlayerProperties> ( );


	public static CommandSetPlayerProperties init (final Player player, final short numHovercraft, final short numTank, final short numArtillery)
	{
		CommandSetPlayerProperties task;
		if (CommandSetPlayerProperties.Pool.isEmpty ( ))
		{
			task = new CommandSetPlayerProperties ( );
		}
		else
		{
			task = CommandSetPlayerProperties.Pool.remove (0);
		}

		task.player = player;
		task.numHovercraft = numHovercraft;
		task.numTank = numTank;
		task.numArtillery = numArtillery;

		return task;
	}


	public void execute (final List<Message> messages)
	{
		this.player.setHover (this.numHovercraft);
		this.player.setTank (this.numTank);
		this.player.setArtillery (this.numArtillery);
		
		// TODO Update database
	}


	public void dispose ( )
	{
		CommandSetPlayerProperties.Pool.add (this);
	}
}
