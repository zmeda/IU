package iu.android.engine.ai;

import iu.android.order.Order;
import iu.android.order.TurnOrder;
import iu.android.unit.Vector2d;

public class TurnOrderState extends OrderState
{

	StandardBrain	brain;

	Order				order			= null;
	TurnOrder		turnOrder	= null;
	TurnState		turnState	= null;

	Vector2d			target		= new Vector2d ( );


	public TurnOrderState (StandardBrain brain, TurnState turnState)
	{
		this.brain = brain;
		this.turnOrder = null;
		this.turnState = turnState;
	}


	@Override
	public Order getOrder ( )
	{
		return this.turnOrder;
	}


	@Override
	public void setOrder (Order turnOrder)
	{
		this.order = turnOrder;
		this.turnOrder = (TurnOrder) turnOrder;
		this.target.x = this.turnOrder.getX ( );
		this.target.y = this.turnOrder.getY ( );
		//this.turnState.setTarget (this.target);
		this.turnState.target = this.target;
	}


	@Override
	public void orderDone ( )
	{
		this.brain.orderDone (this.turnOrder);
		// destination.x = -1;
		// destination.y = -1;
		// this.turnOrder = null;
	}


	@Override
	public void resetOrder ( )
	{
		this.order = null;
		this.turnOrder = null;
		this.target.zero ( );
	}


	@Override
	public boolean actionDone ( )
	{

		return this.turnState.actionDone ( );

	}


	@Override
	public boolean gotOrder ( )
	{
		return (this.turnOrder != null);
	}


	public boolean isDelayed ( )
	{
		return this.turnOrder.isDelayed ( );
	}

}
