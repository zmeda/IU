package iu.server.explore.game;

import java.util.LinkedList;


public class EventFlagUnitsChanged extends Message
{

	private static LinkedList<EventFlagUnitsChanged> Pool = new LinkedList<EventFlagUnitsChanged> ( );
	
	private Flag flag;
	
	public static EventFlagUnitsChanged init (Flag flag) {
		EventFlagUnitsChanged message;
		if (Pool.isEmpty ( )) {
			message = new EventFlagUnitsChanged ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayer (flag.owner);
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
