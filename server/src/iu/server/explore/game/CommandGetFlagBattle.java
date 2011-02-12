package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

import iu.android.network.explore.Protocol;

/**
 * 
 * Answers a {@link Protocol#GET_BATTLE} with a {@link DataFlagBattle}
 * 
 */
public class CommandGetFlagBattle implements Command
{

	private static LinkedList<CommandGetFlagBattle> Pool = new LinkedList<CommandGetFlagBattle> ( );
	
	private FlagBattle battle;
	private Player player;
	
	public static CommandGetFlagBattle init (FlagBattle battle, Player player) {
		CommandGetFlagBattle task;
		if (Pool.isEmpty ( )) {
			task = new CommandGetFlagBattle ( );
		}
		else {
			task = Pool.remove (0);
		}
		
		task.player = player;
		task.battle = battle;
		
		return task;
	}
	
	public void execute (List<Message> messages)
	{
		messages.add (DataFlagBattle.init (this.battle, this.player));
	}

	public void dispose ( ) {
		Pool.add (this);
	}
}
