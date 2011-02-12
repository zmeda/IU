package iu.server.explore.game;

import java.util.LinkedList;

/**
 * Event informing that a battle has started. All participants are informed.
 */
public class EventBattleStarted extends Message
{

	private static LinkedList<EventBattleStarted> Pool = new LinkedList<EventBattleStarted> ( );
	
	private Battle battle;
	
	public static EventBattleStarted init (Battle battle) {
		EventBattleStarted message;
		if (Pool.isEmpty ( )) {
			message = new EventBattleStarted ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayer (battle.attacker);
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
}
