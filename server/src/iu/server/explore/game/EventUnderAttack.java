package iu.server.explore.game;

import java.util.LinkedList;

public class EventUnderAttack extends Message
{

	private static LinkedList<EventUnderAttack> Pool = new LinkedList<EventUnderAttack> ( );
	
	private Battle battle;
	
	public static EventUnderAttack init (Battle battle) {
		EventUnderAttack message;
		if (Pool.isEmpty ( )) {
			message = new EventUnderAttack ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayer (battle.defender);
		message.battle = battle;
		
		return message;
	}
	
	public Battle getBattle ( )
	{
		return this.battle;
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
		sb.append ("Under attack by ");
		sb.append (this.battle.attacker);
		sb.append (" at ");
		sb.append (this.battle.location);
		return sb.toString ( );
	}
}
