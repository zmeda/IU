package iu.server.explore.game;

import java.util.LinkedList;
import java.util.List;

public class CommandLogInPlayer implements Command
{

	private static LinkedList<CommandLogInPlayer> Pool = new LinkedList<CommandLogInPlayer> ( );
	
	private Game game;
	private Player player;
	
	public static CommandLogInPlayer init (Game game, Player player) {
		CommandLogInPlayer task;
		if (Pool.isEmpty ( )) {
			task = new CommandLogInPlayer ( );
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
