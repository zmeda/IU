package iu.server.explore.game;

import iu.android.network.explore.Protocol;

import java.util.Collection;
import java.util.List;

public class FlagBattle extends Battle
{
	/** How long the AI will wait until it will accept the battle */
	private static final double TimeForAiToWait = 5.0;
	private Flag	flag;

	public FlagBattle (final Player attacker, final Flag target)
	{
		super (attacker.getGame ( ), attacker, target.owner, target.location);
		this.flag = target;
	}


	@Override
	public void finish (final Player _winner, List<Message> messages)
	{
		if (_winner == this.attacker)
		{
			this.flag.setOwner (_winner);
			
			// Notify everyone who is sees this flag
			World world = this.game.getWorld ( );
			Tile t = this.location.tile (world);
			long f = this.location.field (world);
			Collection<Player> theySeeIt = this.game.whoSees (t, f);
			messages.add (EventFlagOwnerChanged.init (this.flag, theySeeIt));
			messages.add (EventFlagStateChanged.init (this.flag, Protocol.StateIdle, theySeeIt));
		}
		
		super.finish (_winner, messages);
	}
	
	
	@Override
	public void start (List<Message> messages)
	{
		// First start as any other battle
		super.start (messages);
		
		// Notify everyone who is sees this flag
		World world = this.game.getWorld ( );
		Tile t = this.location.tile (world);
		long f = this.location.field (world);
		Collection<Player> theySeeIt = this.game.whoSees (t, f);
		messages.add (EventFlagStateChanged.init (this.flag, Protocol.StateUnderAttack, theySeeIt));
	}
	
	
	@Override
	public void integrate (double dt, List<Message> messages)
	{
		// FlagBattles are automatically accepted by the 'Brain' for itself and on the  
		if (!this.started && (this.flag.isOwnerAI ( ) || !this.flag.owner.isLoggedIn ( ))) {
			if (this.timePassed >= TimeForAiToWait) {
				this.defenderState = Protocol.BATTLE_ACCEPT_STATE_ACCEPTED;
			}
		}
		
		super.integrate (dt, messages);
	}
	
	
	public Flag getFlag ( ) {
		return this.flag;
	}
}
