package iu.server.explore.game;

import java.util.LinkedList;

public class DataFlagUnits extends Message
{

	private static LinkedList<DataFlagUnits> Pool = new LinkedList<DataFlagUnits> ( );
	
	private Flag flag;
	
	public static DataFlagUnits init (Flag flag, Player player) 
	{
		DataFlagUnits message;
		if (Pool.isEmpty ( )) {
			message = new DataFlagUnits ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayer (player);
		message.flag = flag;
		
		return message;
	}
	
	public Flag getFlag ()
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
