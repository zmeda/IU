package iu.server.explore.game;

import java.util.Collection;
import java.util.LinkedList;

public class EventFlagOwnerChanged extends Message
{

	private static LinkedList<EventFlagOwnerChanged> Pool = new LinkedList<EventFlagOwnerChanged> ( );
	
	private Flag flag;
	
	public static EventFlagOwnerChanged init (Flag flag, Collection<Player> theySeeIt) {
		EventFlagOwnerChanged message;
		if (Pool.isEmpty ( )) {
			message = new EventFlagOwnerChanged ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayers (theySeeIt);
		message.flag = flag;
		
		return message;
	}
	
	public Flag getFlag() 
	{
		return this.flag;
	}
	
	
	@Override
	protected byte[] toProtocol ( )
	{
		return ProtocolFormater.format(this);
	}
	
	public void dispose ( ) {
		super.dispose ( );
		Pool.add (this);
	}
}
