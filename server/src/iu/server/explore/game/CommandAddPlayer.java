package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Add a player to the game.
 * Currently not used
 */
public class CommandAddPlayer implements Command
{

	private static LinkedList<CommandAddPlayer> Pool = new LinkedList<CommandAddPlayer> ( );
	
	private Game game;
	private Player player;
	
	public static CommandAddPlayer init (Game game, Player player) {
		CommandAddPlayer task;
		if (Pool.isEmpty ( )) {
			task = new CommandAddPlayer ( );
		}
		else {
			task = Pool.remove (0);
		}
		
		task.game = game;
		task.player = player;
		
		return task;
	}
	
	public void execute (List<Message> messages)
	{
		this.game.putPlayer (this.player);
	}

	public void dispose ( ) {
		Pool.add (this);
	}
}
