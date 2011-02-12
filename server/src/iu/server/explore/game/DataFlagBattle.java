package iu.server.explore.game;

import java.util.LinkedList;

public class DataFlagBattle extends Message
{

	private static LinkedList<DataFlagBattle> Pool = new LinkedList<DataFlagBattle> ( );
	
	private FlagBattle battle;
	
	public static DataFlagBattle init (FlagBattle battle, Player player) 
	{
		DataFlagBattle message;
		if (Pool.isEmpty ( )) {
			message = new DataFlagBattle ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayer (player);
		message.battle = battle;
		
		return message;
	}
	
	public FlagBattle getBattle ()
	{
		return this.battle;
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
