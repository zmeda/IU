package iu.server.explore.game;

import java.util.LinkedList;

public class EventFlagDiscovered extends Message
{

	private static LinkedList<EventFlagDiscovered> Pool = new LinkedList<EventFlagDiscovered> ( );
	
	private Flag flag;
	private Player heFoundIt;
	
	public static EventFlagDiscovered init (Flag flag, Player heFoundIt) {
		EventFlagDiscovered message;
		if (Pool.isEmpty ( )) {
			message = new EventFlagDiscovered ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayer (heFoundIt);
		message.flag = flag;
		message.heFoundIt = heFoundIt;
		
		return message;
	}
	
	public Flag getFlag ()
	{
		return this.flag;
	}
	
	public Player getOwner ()
	{
		return this.flag.getOwner();
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
	
	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );
		sb.append ("Flag Discovered: ");
		sb.append (this.flag);
		sb.append (" by ");
		sb.append (this.heFoundIt);
		return sb.toString ( );
	}
}
