package iu.android.engine.ai;

import iu.android.order.Order;

public class StopOrderState extends OrderState
{

	StandardBrain	brain;
	StopState		stopState;


	public StopOrderState (final StandardBrain brain, final StopState stopState)
	{
		this.brain = brain;
		this.stopState = stopState;
	}


	@Override
	public Order getOrder ( )
	{
		return null;
	}


	@Override
	public void setOrder (final Order moveOrder)
	{
		// this.moveOrder = (MoveOrder)moveOrder;
	}


	@Override
	public void orderDone ( )
	{
		// brain.orderDone( (Order)moveOrder );
		// this.moveOrder = null;
	}


	@Override
	public void resetOrder ( )
	{
		// Nothing for now
	}


	@Override
	public boolean actionDone ( )
	{
		this.stopState.actionDone ( );

		return false;
	}


	@Override
	public boolean gotOrder ( )
	{
		return true;
	}

}