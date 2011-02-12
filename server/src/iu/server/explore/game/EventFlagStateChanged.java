package iu.server.explore.game;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An event indicating that a flag state has changed. Everyone who sees the flag is informed
 */
public class EventFlagStateChanged extends Message
{

	private static LinkedList<EventFlagStateChanged> Pool = new LinkedList<EventFlagStateChanged> ( );
	
	private Flag flag;
	private byte state;
	
	public static EventFlagStateChanged init (Flag flag, byte state, Collection<Player> theySeeIt) {
		EventFlagStateChanged message;
		if (Pool.isEmpty ( )) {
			message = new EventFlagStateChanged ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayers (theySeeIt);
		message.flag = flag;
		message.state = state;
		
		return message;
	}
	
	public Flag getFlag() 
	{
		return this.flag;
	}
	
	public byte getState () {
		return this.state;
	}
	
	
	@Override
	protected byte[] toProtocol ( )
	{
		return ProtocolFormater.format (this);
	}
	
	public void dispose ( ) {
		super.dispose ( );
		Pool.add (this);
	}
}
