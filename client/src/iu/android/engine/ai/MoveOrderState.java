package iu.android.engine.ai;

import iu.android.order.MoveOrder;
import iu.android.order.Order;
import iu.android.unit.Vector2d;

public class MoveOrderState extends OrderState {

	StandardBrain	brain;
	MoveOrder	  moveOrder;
	MoveState	  moveState;

	Vector2d	  destination	= new Vector2d();

	public MoveOrderState(StandardBrain brain, MoveState moveState) {
		this.brain = brain;
		this.moveOrder = null;
		this.moveState = moveState;
	}

	@Override
	public Order getOrder() {
		return this.moveOrder;
	}

	@Override
	public void setOrder(Order moveOrder) {
		Vector2d dest = this.destination;

		this.moveOrder = (MoveOrder) moveOrder;
		dest.x = this.moveOrder.getX();
		dest.y = this.moveOrder.getY();

		// this.moveState.setDestination (this.destination);
		this.moveState.destination = dest;

		// this.moveState.setStopAtPosition (true);
		this.moveState.stopAtPosition = true;
	}

	@Override
	public void orderDone() {
		this.brain.orderDone(this.moveOrder);
	}

	@Override
	public void resetOrder() {
		this.moveOrder = null;
	}

	@Override
	public boolean actionDone() {
		return this.moveState.actionDone();
	}

	@Override
	public boolean gotOrder() {
		return (this.moveOrder != null);
	}

}
