package iu.server.explore.game;

import java.util.LinkedList;

/**
 * Sent by a battle server when a battle is finished
 */
public class EventBattleFinished extends Message
{

	private static LinkedList<EventBattleFinished>	Pool	= new LinkedList<EventBattleFinished> ( );
	private Battle												battle;


	public static EventBattleFinished init (Battle battle)
	{
		EventBattleFinished message;
		if (Pool.isEmpty ( ))
		{
			message = new EventBattleFinished ( );
		}
		else
		{
			message = Pool.remove (0);
		}

		message.addInterestedPlayer (battle.attacker);
		message.addInterestedPlayer (battle.defender);
		message.battle = battle;

		return message;
	}


	@Override
	protected byte[] toProtocol ( )
	{
		return ProtocolFormater.format (this);
	}


	public void dispose ( )
	{
		super.dispose ( );
		Pool.add (this);
	}
	
	
	
	public Battle getBattle ( )
	{
		return (this.battle);
	}
}
