package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Instruction for the engine to finish a battle. Players are notified about an {@link EventFlagOwnerChanged}
 * 
 */
public class CommandFinishBattle implements Command
{

	private static LinkedList<CommandFinishBattle>	Pool	= new LinkedList<CommandFinishBattle> ( );

	private Battle												battle;
	private Player												winner;


	public static CommandFinishBattle init (Battle battle, Player winner)
	{
		CommandFinishBattle task;
		if (Pool.isEmpty ( ))
		{
			task = new CommandFinishBattle ( );
		}
		else
		{
			task = Pool.remove (0);
		}

		task.battle = battle;
		task.winner = winner;

		return task;
	}


	public void execute (List<Message> messages)
	{
		// Finish Battle
		this.battle.finish (this.winner, messages);
	}


	public void dispose ( )
	{
		Pool.add (this);
	}
}
