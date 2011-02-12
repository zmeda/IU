package iu.server.explore.game;

import java.util.LinkedList;

public class EventPlayerLoggedOut extends Message
{

	private static LinkedList<EventPlayerLoggedOut>	Pool	= new LinkedList<EventPlayerLoggedOut> ( );

	private Player												heLoggedOut;


	public static EventPlayerLoggedOut init (final Player heLoggedOut)
	{
		EventPlayerLoggedOut message;
		if (EventPlayerLoggedOut.Pool.isEmpty ( ))
		{
			message = new EventPlayerLoggedOut ( );
		}
		else
		{
			message = EventPlayerLoggedOut.Pool.remove (0);
		}

		message.addInterestedPlayers (heLoggedOut.getGame ( ).getPlayers ( ));
		message.heLoggedOut = heLoggedOut;

		return message;
	}


	@Override
	protected byte[] toProtocol ( )
	{
		return ProtocolFormater.format (this);
	}


	public Player getPlayer ( )
	{
		return this.heLoggedOut;
	}


	@Override
	public void dispose ( )
	{
		super.dispose ( );
		EventPlayerLoggedOut.Pool.add (this);
	}


	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );
		sb.append ("Event: ");
		sb.append (this.heLoggedOut);
		sb.append (" has logged out ");
		sb.append (this.heLoggedOut.loc);
		return sb.toString ( );
	}
}
