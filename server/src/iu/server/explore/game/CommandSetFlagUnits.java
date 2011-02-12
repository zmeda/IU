package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

/**
 * Change unit configuration a flag
 */
public class CommandSetFlagUnits implements Command
{
	private Flag												flag;
	private short												numHovercraft;
	private short												numTank;
	private short												numArtillery;

	private static LinkedList<CommandSetFlagUnits>	Pool	= new LinkedList<CommandSetFlagUnits> ( );


	public static CommandSetFlagUnits init (Flag flag, short numHovercraft, short numTank, short numArtillery)
	{
		CommandSetFlagUnits task;
		if (CommandSetFlagUnits.Pool.isEmpty ( ))
		{
			task = new CommandSetFlagUnits ( );
		}
		else
		{
			task = CommandSetFlagUnits.Pool.remove (0);
		}

		task.flag = flag;
		task.numHovercraft = numHovercraft;
		task.numTank = numTank;
		task.numArtillery = numArtillery;

		return task;
	}


	public void execute (List<Message> messages)
	{
		this.flag.setHover (this.numHovercraft);
		this.flag.setTank (this.numTank);
		this.flag.setArtillery (this.numArtillery);
		
		// TODO Update Database
		
		EventFlagUnitsChanged message = EventFlagUnitsChanged.init (this.flag);
		messages.add (message);
	}


	public void dispose ( )
	{
		CommandSetFlagUnits.Pool.add (this);
	}
}
