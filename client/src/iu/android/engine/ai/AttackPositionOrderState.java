package iu.android.engine.ai;

import iu.android.order.AttackPositionOrder;
import iu.android.order.Order;
import iu.android.unit.Vector2d;

public class AttackPositionOrderState extends OrderState
{

	StandardBrain			brain;

	AttackPositionOrder	attackPositionOrder	= null;
	AttackState				attackState				= null;

	Vector2d					destination				= new Vector2d ( );
	int						nNbrHits					= 0;


	public AttackPositionOrderState (StandardBrain brain, AttackState attackState)
	{
		this.brain = brain;
		this.attackPositionOrder = null;
		this.attackState = attackState;
	}


	@Override
	public Order getOrder ( )
	{
		return this.attackPositionOrder;
	}


	@Override
	public void setOrder (Order attackPositionOrder)
	{
		this.attackPositionOrder = (AttackPositionOrder) attackPositionOrder;
		this.destination.x = this.attackPositionOrder.getX ( );
		this.destination.y = this.attackPositionOrder.getY ( );
		this.nNbrHits = 3;
		this.attackState.setTargetPosition (this.destination);
	}


	@Override
	public void orderDone ( )
	{
		this.brain.orderDone (this.attackPositionOrder);
	}


	@Override
	public void resetOrder ( )
	{
		this.attackPositionOrder = null;
		this.destination.zero ( );
	}


	@Override
	public boolean actionDone ( )
	{

		if (this.attackState.actionDone ( ))
		{
			this.nNbrHits--;
		}

		if (this.nNbrHits == 0)
		{
			return true;
		}

		return false;

	}


	@Override
	public boolean gotOrder ( )
	{
		return (this.attackPositionOrder != null);
	}

}
