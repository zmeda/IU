package iu.server.explore.game;

import java.util.LinkedList;

public class EventEnemyNear extends Message
{

	private static LinkedList<EventEnemyNear> Pool = new LinkedList<EventEnemyNear> ( );
	
	private Player enemy;
	
	public static EventEnemyNear init (Player me, Player enemy) {
		EventEnemyNear message;
		if (Pool.isEmpty ( )) {
			message = new EventEnemyNear ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayer (me);
		message.enemy = enemy;
		
		return message;
	}
		
	@Override
	protected byte[] toProtocol ( )
	{
		// TODO Protocolize
		return null;
	}
	
	public void dispose ( ) {
		super.dispose ( );
		Pool.add (this);
	}
	
	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );
		sb.append ("Enemy near: ");
		sb.append (this.enemy);
		return sb.toString ( );
	}
}
