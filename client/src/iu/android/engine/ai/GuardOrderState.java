package iu.android.engine.ai;

import iu.android.order.GuardOrder;
import iu.android.order.Order;
import iu.android.unit.GeneralPhysics;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

public class GuardOrderState extends OrderState
{

	Unit	      unit;
	StandardBrain	brain;

	Unit	      nearestEnemyUnit	= null;

	MoveState	  moveState	       = null;
	AttackState	  attackState	   = null;

	GuardOrder	  guardOrder;
	Vector2d	  destination;

	boolean	      moveToPosition	= true;

	// boolean underAttack = false;

	// int tick = 0;
	// float tickSinceLastAttack = 0.0;

	public GuardOrderState(Unit unit, StandardBrain brain, MoveState moveState, AttackState attackState)
	{

		this.unit = unit;

		this.brain = brain;
		this.moveState = moveState;
		this.attackState = attackState;

		this.guardOrder = null;
		this.destination = new Vector2d();
	}

	@Override
	public Order getOrder()
	{
		return this.guardOrder;
	}

	@Override
	public void setOrder(Order guardOrder)
	{
		this.guardOrder = (GuardOrder) guardOrder;
		this.destination.x = this.guardOrder.getX();
		this.destination.y = this.guardOrder.getY();

		// this.moveState.setDestination (this.destination);
		this.moveState.destination = this.destination;

		this.moveToPosition = true;
	}

	@Override
	public void orderDone()
	{
		this.brain.orderDone(this.guardOrder);
	}

	@Override
	public void resetOrder()
	{
		this.guardOrder = null;
		this.destination.zero();
	}

	@Override
	public boolean actionDone()
	{

		if (this.moveToPosition == true)
		{
			// System.out.println( "GuardOrderState/moveToPosition == true" );
			if (this.moveState.actionDone())
			{
				this.moveToPosition = false;
			}
		}
		else
		{

			this.nearestEnemyUnit = this.unit.findNearestEnemy();
			// System.out.println( "GuardOrderState/findNearestEnemy nearestEnemyUnit = "+nearestEnemyUnit );

			if (this.nearestEnemyUnit != null)
			{
				if (this.nearestEnemyUnit.isDead() == true)
				{
					// System.out.println( "GuardOrderState/nearestEnemyUnit.isDead() == true" );
					this.nearestEnemyUnit = null;
				}
				else
				{

					if (GeneralPhysics.distanceSqr(this.nearestEnemyUnit.getPosition(), this.destination) > 300 * 300) // 300
					// range
					// of
					// siegeCannon
					{
						// System.out.println( "GuardOrderState/distance > 300" );
						this.nearestEnemyUnit = null;
						this.moveToPosition = true;

						// this.moveState.setDestination (this.destination);
						this.moveState.destination = this.destination;
					}
					else
					{
						// System.out.println( "GuardOrderState/attack unit" );
						this.attackState.setTargetPosition(this.nearestEnemyUnit.getPosition());
						this.attackState.actionDone();
					}

				}

			}
			else
			{
				this.moveToPosition = true;

				// this.moveState.setDestination (this.destination);
				this.moveState.destination = this.destination;
			}

		}

		return false;
	}

	@Override
	public boolean gotOrder()
	{
		return (this.guardOrder != null);
	}

}
