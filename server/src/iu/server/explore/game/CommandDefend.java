package iu.server.explore.game;

import iu.android.network.explore.Protocol;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Instruction for the engine to finish a battle. Players are notified about an {@link EventFlagOwnerChanged}
 * 
 */
public class CommandDefend implements Command
{

	private static LinkedList<CommandDefend> Pool = new LinkedList<CommandDefend> ( );
	
	private Battle battle;
	
	public static CommandDefend init (Battle battle) {
		CommandDefend task;
		if (Pool.isEmpty ( )) {
			task = new CommandDefend ( );
		}
		else {
			task = Pool.remove (0);
		}
		
		task.battle = battle;
		
		return task;
	}
	
	public void execute (List<Message> messages)
	{
		this.battle.defenderState = Protocol.BATTLE_ACCEPT_STATE_ACCEPTED;
	}

	public void dispose ( ) {
		Pool.add (this);
	}
}
