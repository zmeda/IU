package iu.server.explore.game;

import java.util.List;

/**
 * Classes should implement this interface if they want to manipulate game state externally.
 * {@link Game} accepts arbitrary commands through {@link Game#schedule(Command)}
 * 
 */
public interface Command
{
	
	/**
	 * Execute the command. Anything interested that may happen during command execution
	 * will be stored to the passed List
	 * 
	 * @param messages Here messages that happend during execution are stored
	 */
	public void execute (List<Message> messages);
	
	/**
	 * Nicely dispose of the object to take some load of the GC.
	 * e.g. put back to a Pool for later reuse
	 */
	public void dispose ( );
}
