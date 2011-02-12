package iu.server.explore.game;

import java.util.LinkedList;


/**
 * 
 * @author luka
 *
 */
public class EventPlayerPromoted extends Message
{

	private static LinkedList<EventPlayerPromoted>	Pool	= new LinkedList<EventPlayerPromoted> ( );

	private Player												player;


	public static EventPlayerPromoted init (Player player)
	{
		EventPlayerPromoted message;
		if (Pool.isEmpty ( ))
		{
			message = new EventPlayerPromoted ( );
		}
		else
		{
			message = Pool.remove (0);
		}

		Game g = player.getGame ( );
		message.addInterestedPlayers (g.getPlayers ( ));
		message.player = player;

		return message;
	}


	@Override
	protected byte[] toProtocol ( )
	{
		return ProtocolFormater.format (this);
	}


	public Player getPlayer ( )
	{
		return this.player;
	}


	public void dispose ( )
	{
		super.dispose ( );
		Pool.add (this);
	}


	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );
		sb.append ("Event: ");
		sb.append (this.player);
		sb.append (" was promoted to ");
		sb.append (this.player.getRank ( ));
		return sb.toString ( );
	}
}
