package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Announce an attack.
 *
 */
public class CommandAttack implements Command
{
	/** How long the server will wait until starting a battle */
	public static final double Timeout = 30.0d;
	
	private static LinkedList<CommandAttack>	Pool	= new LinkedList<CommandAttack> ( );

	private Battle battle;

	public static CommandAttack init (final Battle battle)
	{
		CommandAttack task;
		if (CommandAttack.Pool.isEmpty ( ))
		{
			task = new CommandAttack ( );
		}
		else
		{
			task = CommandAttack.Pool.remove (0);
		}

		task.battle = battle;
		
		return task;
	}


	public void execute (final List<Message> messages)
	{
		// Start Battle
		this.battle.init (Timeout, messages);
	}


	public void dispose ( )
	{
		CommandAttack.Pool.add (this);
	}
}
