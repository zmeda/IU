package iu.server.explore.game;


import java.util.Collection;
import java.util.LinkedList;

public class EventPlayerMoved extends Message
{

	private static LinkedList<EventPlayerMoved> Pool = new LinkedList<EventPlayerMoved> ( );
	
	private Player heMoved;
	
	public static EventPlayerMoved init (Player heMoved, Collection<Player> theySeeHim) {
		EventPlayerMoved message;
		if (Pool.isEmpty ( )) {
			message = new EventPlayerMoved ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayers (theySeeHim);
		message.heMoved = heMoved;
		
		return message;
	}
	
	@Override
	protected byte[] toProtocol ( )
	{
		return ProtocolFormater.format(this);
	}
	
	public Player getPlayer ()
	{
		return this.heMoved;
	}
	
	public void dispose ( ) {
		super.dispose ( );
		Pool.add (this);
	}
	
	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );
		sb.append ("Event: ");
		sb.append (this.heMoved);
		sb.append (" has moved to ");
		sb.append (this.heMoved.loc);
		return sb.toString ( );
	}
}
