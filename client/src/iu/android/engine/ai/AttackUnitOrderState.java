package iu.android.engine.ai;

import iu.android.order.AttackUnitOrder;
import iu.android.order.Order;
import iu.android.unit.Unit;

public class AttackUnitOrderState extends OrderState
{

	StandardBrain		brain;
	AttackUnitOrder	attackUnitOrder	= null;

	AttackState			attackState			= null;

	Unit					enemyUnit			= null;


	public AttackUnitOrderState (StandardBrain brain, AttackState attackState)
	{
		this.brain = brain;
		this.attackUnitOrder = null;
		this.attackState = attackState;
	}


	@Override
	public Order getOrder ( )
	{
		return this.attackUnitOrder;
	}


	@Override
	public void setOrder (Order order)
	{
		this.attackUnitOrder = (AttackUnitOrder) order;
		this.enemyUnit = this.attackUnitOrder.getTargetUnit ( );
		this.attackState.setTargetPosition (this.enemyUnit.getPosition ( ));
	}


	@Override
	public void orderDone ( )
	{
		this.brain.orderDone (this.attackUnitOrder);
	}


	@Override
	public void resetOrder ( )
	{
		this.attackUnitOrder = null;
	}


	@Override
	public boolean actionDone ( )
	{

		// attackState.setTargetPosition( enemyUnit.getPosition() );

		this.attackState.actionDone ( );

		if (this.enemyUnit.isDead ( ))
		{
			return (true);
		}
		return (false);
	}


	@Override
	public boolean gotOrder ( )
	{
		return (this.attackUnitOrder != null);
	}

}
