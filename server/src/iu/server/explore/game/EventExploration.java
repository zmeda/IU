package iu.server.explore.game;

import java.util.LinkedList;


public class EventExploration extends Message
{

	private static LinkedList<EventExploration> Pool = new LinkedList<EventExploration> ( );
	
	short i, j;
	byte field;
	
	public static EventExploration init (short i, short j, long field, Player player) {
		EventExploration message;
		if (Pool.isEmpty ( )) {
			message = new EventExploration ( );
		}
		else {
			message = Pool.remove (0);
		}
		
		message.addInterestedPlayer (player);
		message.i = i;
		message.j = j;
		message.field = Tile.fieldIndex (field);
		
		return message;
	}
	
	@Override
	protected byte[] toProtocol ( )
	{
		return ProtocolFormater.format (this);
	}
	
	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ();
		sb.append ("Tile[");
		sb.append (this.i);
		sb.append (", ");
		sb.append (this.j);
		sb.append ("] - Field=");
		sb.append (this.field);
		return sb.toString ( );
	}
	
	public void dispose ( ) {
		super.dispose ( );
		Pool.add (this);
	}
}
