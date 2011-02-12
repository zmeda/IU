package iu.android.engine.ai;

import iu.android.battle.BattleEngine;
import iu.android.engine.BattlePlayer;
import iu.android.explore.Player;
import iu.android.order.AttackUnitOrder;
import iu.android.order.MoveOrder;
import iu.android.unit.Squad;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;
import iu.android.unit.Weapon;

import java.util.ArrayList;
import java.util.Hashtable;

/** The player who <EM>is</EM> this computer. * */

public class AIPlayer extends BattlePlayer
{

	// private int currentUnit = 0;

	private long						timeStarted				= 0;
	// private boolean bAttacking = false;

	int									currentSquad			= 0;

	private Hashtable<Unit, Unit>	unitsToAttack			= new Hashtable<Unit, Unit> ( );

	private ArrayList<Unit>			attackedUnits			= new ArrayList<Unit> ( );

	private long						initialAttackDelay	= 30000;								// 30000 ms = 30
																													// seconds


	public AIPlayer (final Player exPlayer)
	{
		super (exPlayer, false, true);
		this.timeStarted = System.currentTimeMillis ( );
	}


	protected AIPlayer (final Player exPlayer, final boolean isParticipating)
	{
		super (exPlayer, false, isParticipating);
		this.timeStarted = System.currentTimeMillis ( );
	}


	// NOT USED
	// private void simpleDispatchOrders(BattleEngine game)
	// {
	// long timeElapsedSinceStart = System.currentTimeMillis() - this.timeStarted;
	// if (!this.bAttacking && timeElapsedSinceStart > this.initialAttackDelay)
	// {
	// this.bAttacking = true;
	// // FIXME messageDisplay BattleEngine.messageDisplay.systemMsg ("Enemy activity detected.");
	// }
	//
	// if (this.currentUnit >= this.getNumUnits())
	// {
	// this.currentUnit = 0;
	// }
	//
	// Unit unit = null;
	// int lookedAt = 0;
	//
	// do
	// {
	//
	// if (lookedAt >= this.getNumUnits())
	// {
	// return;
	// }
	//
	// if (this.currentUnit >= this.getNumUnits())
	// {
	// this.currentUnit = 0;
	// }
	// unit = this.getUnit(this.currentUnit);
	// lookedAt++;
	// this.currentUnit++;
	// } while (unit.isDead());
	//
	// Unit closestEnemy = null;
	// float closestDistSq = Float.MAX_VALUE;
	//
	// int numPlayers = game.getNumPlayers();
	// for (int i = 0; i < numPlayers; i++)
	// {
	// Player player = game.getPlayer(i);
	//
	// if (player != this && player.isParticipating())
	// {
	// int numUnits = player.getNumUnits();
	//
	// for (int j = 0; j < numUnits; j++)
	// {
	// Unit enemy = player.getUnit(j);
	// if (enemy.isDead())
	// {
	// continue;
	// }
	// Vector2d enemyPos = enemy.getPosition();
	//
	// Vector2d pos = unit.getPosition();
	// float distSq = pos.distanceSq(enemyPos);
	// if (distSq < closestDistSq)
	// {
	// closestDistSq = distSq;
	// closestEnemy = enemy;
	// }
	// }
	// }
	// }
	//
	// if (closestEnemy != null && this.bAttacking)
	// {
	// AttackUnitOrder attackOrder = AttackUnitOrder.getNewAttackUnitOrder(this.getID(), unit.getID(),
	// closestEnemy);
	// unit.giveOrder(attackOrder);
	// }
	//
	// }

	private Unit getUnitToAttack (final Unit unit)
	{
		Object obj = this.unitsToAttack.get (unit);
		if (obj == null)
		{
			return null;
		}
		return (Unit) obj;
	}


	private void setUnitToAttack (final Unit unit, final Unit attackedUnit)
	{
		this.unitsToAttack.put (unit, attackedUnit);
	}


	private void getUnitsToAttack (final Squad squad, final ArrayList<Unit> attacked_units)
	{
		int squadNumUnits = squad.getNumUnits ( );
		for (int i = 0; i < squadNumUnits; i++)
		{
			Unit unit = this.getUnitToAttack (squad.getUnit (i));
			if (unit != null)
			{
				attacked_units.add (unit);
			}
		}
	}


	// TODO - rewrite (each unit already has closest enemy)
	private Unit closestEnemy (final BattleEngine game, final Unit unit)
	{
		Unit closestEnemy = null;
		float closestDistSq = Float.MAX_VALUE;

		int numPlayers = game.getNumPlayers ( );
		for (int i = 0; i < numPlayers; i++)
		{
			BattlePlayer player = game.getPlayer (i);

			if (player != this && player.isParticipating ( ))
			{
				int playerNumUnits = player.getNumUnits ( );
				for (int j = 0; j < playerNumUnits; j++)
				{
					Unit enemy = player.getUnit (j);
					if (enemy.isDead ( ))
					{
						continue;
					}
					Vector2d enemyPos = enemy.getPosition ( );

					Vector2d pos = unit.getPosition ( );
					float distSq = pos.distanceSq (enemyPos);
					if (distSq < closestDistSq)
					{
						closestDistSq = distSq;
						closestEnemy = enemy;
					}
				}
			}
		}

		return closestEnemy;
	}


	private void carryOnAttacking (final BattleEngine game, final Squad squad)
	{
		int squadNumUnits = squad.getNumUnits ( );
		for (int i = 0; i < squadNumUnits; i++)
		{
			Unit unit = squad.getUnit (i);
			if (unit.isDead ( ))
			{
				continue;
			}

			Unit attacked = this.getUnitToAttack (unit);
			if (attacked.isDead ( ))
			{
				attacked = this.closestEnemy (game, unit);
				if (attacked != null)
				{
					this.setUnitToAttack (unit, attacked);
					AttackUnitOrder attackOrder = AttackUnitOrder.getNewAttackUnitOrder (this.getID ( ), unit
							.getID ( ), attacked);
					unit.giveOrder (attackOrder);
				}
			}
			else
			{// if ( Math.random() < 0.9 ) {
				Unit enemy = unit.findNearestEnemy ( );
				if (enemy != null)
				{
					// setUnitToAttack( unit, attacked );
					AttackUnitOrder attackOrder = AttackUnitOrder.getNewAttackUnitOrder (this.getID ( ), unit
							.getID ( ), enemy);
					unit.giveOrder (attackOrder);
				}
				else
				{
					AttackUnitOrder attackOrder = AttackUnitOrder.getNewAttackUnitOrder (this.getID ( ), unit
							.getID ( ), attacked);
					unit.giveOrder (attackOrder);
				}
			}
			/*
			 * else { AttackUnitOrder attackOrder = AttackUnitOrder.getNewAttackUnitOrder( getID(), unit.getID(),
			 * attacked ); unit.giveOrder( attackOrder ); }
			 */

		}
	}


	private void startAttacking (final BattleEngine game, final Squad squad)
	{
		Unit unit0 = squad.getUnit (0);

		if (unit0.getUnitType ( ) == Unit.LIGHT_UNIT)
		{
			// hover jets
			long timeElapsedSinceStart = System.currentTimeMillis ( ) - this.timeStarted;

			if (timeElapsedSinceStart < (1.5 * this.initialAttackDelay))
			{

				int unitCount = squad.getNumUnits ( );
				for (int i = 0; i < unitCount; i++)
				{
					Unit unit = squad.getUnit (i);

					MoveOrder moveOrder = MoveOrder.getNewMoveOrder (this.getID ( ), unit.getID ( ), (short) 300,
							(short) 1500);

					unit.giveOrder (moveOrder);
				}

				return;
			}
		}

		if (unit0.getUnitType ( ) == Unit.HEAVY_UNIT)
		{
			// others
			long timeElapsedSinceStart = System.currentTimeMillis ( ) - this.timeStarted;

			if (timeElapsedSinceStart < this.initialAttackDelay)
			{

				Weapon[] weapons = unit0.getWeapons ( );

				if (weapons[0].getMinimumRange ( ) == 0
						|| timeElapsedSinceStart < ((3 * this.initialAttackDelay) >> 2))
				{

					int squadNumUnits = squad.getNumUnits ( );
					for (int i = 0; i < squadNumUnits; i++)
					{
						Unit unit = squad.getUnit (i);

						MoveOrder moveOrder = MoveOrder.getNewMoveOrder (this.getID ( ), unit.getID ( ),
								(short) 1500, (short) 1000);
						unit.giveOrder (moveOrder);
					}

					return;
				}
			}
		}

		ArrayList<Unit> attackedUnitVec = this.attackedUnits;

		while (attackedUnitVec.size ( ) < squad.getNumUnits ( ))
		{
			int numPlayers = game.getNumPlayers ( );
			for (int i = 0; i < numPlayers; i++)
			{
				BattlePlayer player = game.getPlayer (i);

				if (player != this && player.isParticipating ( ))
				{
					int rand = (int) (Math.random ( ) * player.getNumUnits ( ));
					Unit enemy = player.getUnit (rand);

					// Weapon[] weapons = enemy.getWeapons();

					attackedUnitVec.add (enemy);
				}
			}
		}

		int numUnits = squad.getNumUnits ( );
		for (int i = 0; i < numUnits; i++)
		{
			Unit unit = squad.getUnit (i);

			Unit attacked = attackedUnitVec.get (i);

			this.setUnitToAttack (unit, attacked);
			AttackUnitOrder attackOrder = AttackUnitOrder.getNewAttackUnitOrder (this.getID ( ), unit.getID ( ),
					attacked);
			unit.giveOrder (attackOrder);

		}
	}


	@Override
	public void dispatchOrders (final BattleEngine game)
	{
		ArrayList<Unit> attackedUnitsVec = this.attackedUnits;

		if (this.currentSquad >= this.getNumSquads ( ))
		{
			this.currentSquad = 0;
			return;
		}

		Squad squad = this.getSquad (this.currentSquad);

		attackedUnitsVec.clear ( );

		this.getUnitsToAttack (squad, attackedUnitsVec);

		if (attackedUnitsVec.size ( ) == 0)
		{
			// work out the initial attack
			this.startAttacking (game, squad);
		}
		else
		{
			// carry on with the attack
			this.carryOnAttacking (game, squad);
		}

		this.currentSquad++;
	}

}