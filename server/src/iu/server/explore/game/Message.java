package iu.server.explore.game;

import java.util.Collection;
import java.util.Vector;

/**
 * Subclasses represent Events that happen as a result of the GameLoop. {@link GameLoop} produces arbitrary
 * messages in response to user actions in its {@link GameLoop#run()} method.
 * 
 * Subclasses should do their notification code in overridden {@link Message#notifyPlayer(Player)}
 * 
 */
public abstract class Message
{
	private Vector<Player>	interestedPlayers;


	public Message ( )
	{
		this.interestedPlayers = new Vector<Player> ( );
	}


	public final void send ( )
	{
		for (Player p : this.interestedPlayers)
		{
			p.notify (this);
		}
	}


	public final void addInterestedPlayer (Player player)
	{
		this.interestedPlayers.add (player);
	}


	public final void addInterestedPlayers (Collection<Player> players)
	{
		this.interestedPlayers.addAll (players);
	}


	public final void removeInterestedPlayer (Player player)
	{
		this.interestedPlayers.remove (player);
	}


	/**
	 * This method is called from within {@link Player#notify(Message)}, for each interested player, when
	 * {@link Message#send()} is called. Subclasses should override this method to do their own notifications.
	 */
	protected abstract byte[] toProtocol ( );


	/**
	 * All overriding classes should call {@link Message#dispose()}
	 */
	protected void dispose ( )
	{
		this.interestedPlayers.clear ( );
	}
}
