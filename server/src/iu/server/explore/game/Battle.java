package iu.server.explore.game;

import iu.android.network.explore.Protocol;
import iu.server.battle.BattleListenServer;

import java.util.List;

/**
 * An abstract representation of a Battle from the servers point of view
 * 
 */
public abstract class Battle
{
	private static int				ID	= 0;

	final Integer						id;
	final Location						location;
	protected boolean					started;
	protected boolean					finished;

	protected Player					attacker;
	protected byte						attackerState;
	protected UnitConfiguration	attackerUnits;

	protected Player					defender;
	protected byte						defenderState;
	protected UnitConfiguration	defenderUnits;

	protected Player					winner;
	final Game							game;

	/** How long to wait for everyone to accept */
	protected transient double		timeout;
	protected transient double		timePassed;

	/** An instance of a server that will host this battle */
	private BattleListenServer		server;


	protected Battle (final Game game, final Player attacker, final Player defender, final Location location)
	{
		this.id = ID++;
		this.game = game;
		this.attacker = attacker;
		this.defender = defender;
		this.location = location;
		this.started = false;
		this.finished = false;
	}


	public boolean started ( )
	{
		return this.started;
	}


	/**
	 * Initialize the battle and wait for participants to accept
	 * 
	 * @param timeout
	 *           how long to wait before starting (this is MAX, if all accept, battle may actually start
	 *           sooner)
	 * @param messages
	 */
	public void init (double _timeout, List<Message> messages)
	{
		this.timeout = _timeout;
		this.timePassed = 0;
		this.attackerState = Protocol.BATTLE_ACCEPT_STATE_ACCEPTED;
		this.game.requestBattleStart (this, messages);
	}


	public void start (List<Message> messages)
	{
		this.started = true;
		this.game.startBattle (this, messages);
	}


	public void integrate (double dt, List<Message> messages)
	{
		if (!this.started)
		{
			this.timePassed += dt;
			if (this.timePassed > this.timeout
					|| (this.attackerState != Protocol.BATTLE_ACCEPT_STATE_NONE && this.defenderState != Protocol.BATTLE_ACCEPT_STATE_NONE))
			{
				this.start (messages);
			}
		}
	}


	public boolean finished ( )
	{
		return this.finished;
	}


	public void finish (final Player _winner, List<Message> messages)
	{
		this.finished = true;
		this.winner = _winner;

		this.game.finishBattle (this, messages);
	}


	public Player winner ( )
	{
		return this.winner;
	}


	public void setServer (BattleListenServer server)
	{
		this.server = server;
	}


	public BattleListenServer getServer ( )
	{
		return this.server;
	}


	public Player getAttacker ( )
	{
		return this.attacker;
	}
}
