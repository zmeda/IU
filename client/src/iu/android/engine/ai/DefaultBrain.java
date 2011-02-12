package iu.android.engine.ai;

import iu.android.order.AttackPositionOrder;
import iu.android.order.AttackUnitOrder;
import iu.android.order.GuardOrder;
import iu.android.order.MoveOrder;
import iu.android.order.Order;
import iu.android.order.TurnOrder;
import iu.android.unit.Unit;

import java.util.ArrayList;

/** A dummy brain * */

public class DefaultBrain extends StandardBrain
{

	// Action States
	private CollisionState	         collisionState	             = null;

	private MoveState	             moveState	                 = null;
	private AttackState	             attackState	             = null;
	private StopState	             stopState	                 = null;
	private TurnState	             turnState	                 = null;

	// Order States
	private MoveOrderState	         moveOrderState	             = null;
	private TurnOrderState	         turnOrderState	             = null;
	private AttackPositionOrderState	attackPositionOrderState	= null;
	private AttackUnitOrderState	 attackUnitOrderState	     = null;
	private StopOrderState	         stopOrderState	             = null;
	private GuardOrderState	         guardOrderState	         = null;

	private ArrayList<OrderState>	 orderStateStack	         = new ArrayList<OrderState>();

	OrderState	                     currentOrderState	         = null;

	private int	                     nbrOrderOnUnit	             = 0;

	public DefaultBrain(Unit unit)
	{
		super(unit);

		this.collisionState = new CollisionState(unit, this);
		this.moveState = new MoveState(unit, this.collisionState);
		this.attackState = new AttackState(unit, this.moveState);
		this.stopState = new StopState(unit, this);
		this.turnState = new TurnState(unit);

		this.stopOrderState = new StopOrderState(this, this.stopState);
		this.turnOrderState = new TurnOrderState(this, this.turnState);
		this.moveOrderState = new MoveOrderState(this, this.moveState);
		this.attackPositionOrderState = new AttackPositionOrderState(this, this.attackState);
		this.attackUnitOrderState = new AttackUnitOrderState(this, this.attackState);
		this.guardOrderState = new GuardOrderState(unit, this, this.moveState, this.attackState);

		this.currentOrderState = this.stopOrderState;

	}

	public void think(float dt)
	{

		this.lastDt = dt;

		this.checkHealth();

		this.getOrders();

		if (this.currentOrderState.actionDone() == true)
		{
			this.orderDone(this.currentOrderState.getOrder());
			this.currentOrderState.resetOrder();
			this.setNextCurrentOrder();
		}
	}

	private void getOrders()
	{

		int newNbrOrdersOnUnit = this.unit.getNumOrders();

		if (newNbrOrdersOnUnit < this.nbrOrderOnUnit)
		{
			// System.out.println( "DefaultBrain/getOrders unit.getNumOrders() < nbrOrderOnUnit " );
		}
		if (newNbrOrdersOnUnit > 0)
		{
			// System.out.println( "DefaultBrain/getOrders newNbrOrdersOnUnit ="+newNbrOrdersOnUnit );
		}

		if (newNbrOrdersOnUnit != this.nbrOrderOnUnit)
		{
			int counter = this.nbrOrderOnUnit;
			while (counter < newNbrOrdersOnUnit)
			{

				Order order = this.unit.getOrder(counter);

				if (order.getType() == MoveOrder.TYPE)
				{

					int numCleared = this.clearAllOrderStates();

					newNbrOrdersOnUnit -= numCleared;
					counter -= numCleared;

					this.moveOrderState.setOrder(order);

				}
				else if (order.getType() == AttackPositionOrder.TYPE)
				{
					// if ( attackPositionOrderState.gotOrder() == true ){
					// orderDone( attackPositionOrderState.getOrder() );
					// attackPositionOrderState.resetOrder();
					// --newNbrOrdersOnUnit;
					// --counter;
					// }

					int numCleared = this.clearAllOrderStates();

					newNbrOrdersOnUnit -= numCleared;
					counter -= numCleared;

					this.attackPositionOrderState.setOrder(order);

				}
				else if (order.getType() == AttackUnitOrder.TYPE)
				{
					// if ( attackUnitOrderState.gotOrder() == true ){
					// orderDone( attackUnitOrderState.getOrder() );
					// attackUnitOrderState.resetOrder();
					// --newNbrOrdersOnUnit;
					// --counter;
					// }

					int numCleared = this.clearAllOrderStates();

					newNbrOrdersOnUnit -= numCleared;
					counter -= numCleared;

					this.attackUnitOrderState.setOrder(order);

				}

				else if (order.getType() == GuardOrder.TYPE)
				{

					// if ( guardOrderState.gotOrder() == true ){
					// orderDone( guardOrderState.getOrder() );
					// guardOrderState.resetOrder();
					// --newNbrOrdersOnUnit;
					// --counter;
					// }

					int numCleared = this.clearAllOrderStates();

					newNbrOrdersOnUnit -= numCleared;
					counter -= numCleared;

					this.guardOrderState.setOrder(order);

				}

				else if (order.getType() == TurnOrder.TYPE)
				{

					// if ( turnOrderState.gotOrder() == true ){
					// orderDone( turnOrderState.getOrder() );
					// turnOrderState.resetOrder();
					// --newNbrOrdersOnUnit;
					// --counter;
					// }

					this.turnOrderState.setOrder(order);

					if (this.turnOrderState.isDelayed() == false)
					{
						int numCleared = this.clearAllOrderStatesBar(order);

						newNbrOrdersOnUnit -= numCleared;
						counter -= numCleared;
					}

				}

				else
				{
					// unknownOrder
					this.orderDone(order);
					--newNbrOrdersOnUnit;
					--counter;
				}

				++counter;

			}

			this.nbrOrderOnUnit = newNbrOrdersOnUnit;

			if (this.unit.getNumOrders() > 2)
			{
				// System.out.println("unit.getNumOrders() > 2 ");
			}

			this.setNextCurrentOrder();

		}
	}

	protected int clearAllOrderStates()
	{
		int counter = 0;

		if (this.moveOrderState.gotOrder() == true)
		{
			this.orderDone(this.moveOrderState.getOrder());
			this.moveOrderState.resetOrder();
			++counter;
		}

		if (this.attackPositionOrderState.gotOrder() == true)
		{
			this.orderDone(this.attackPositionOrderState.getOrder());
			this.attackPositionOrderState.resetOrder();
			++counter;
		}

		if (this.attackUnitOrderState.gotOrder() == true)
		{
			this.orderDone(this.attackUnitOrderState.getOrder());
			this.attackUnitOrderState.resetOrder();
			++counter;
		}

		if (this.guardOrderState.gotOrder() == true)
		{
			this.orderDone(this.guardOrderState.getOrder());
			this.guardOrderState.resetOrder();
			++counter;
		}

		if (this.turnOrderState.gotOrder() == true)
		{
			this.orderDone(this.turnOrderState.getOrder());
			this.turnOrderState.resetOrder();
			++counter;
		}

		return counter;

	}

	protected int clearAllOrderStatesBar(Order order)
	{
		int counter = 0;

		if ((this.moveOrderState.gotOrder() == true) && (this.moveOrderState.getOrder() != order))
		{
			this.orderDone(this.moveOrderState.getOrder());
			this.moveOrderState.resetOrder();
			++counter;
		}

		if ((this.attackPositionOrderState.gotOrder() == true) && (this.attackPositionOrderState.getOrder() != order))
		{
			this.orderDone(this.attackPositionOrderState.getOrder());
			this.attackPositionOrderState.resetOrder();
			++counter;
		}

		if ((this.attackUnitOrderState.gotOrder() == true) && (this.attackUnitOrderState.getOrder() != order))
		{
			this.orderDone(this.attackUnitOrderState.getOrder());
			this.attackUnitOrderState.resetOrder();
			++counter;
		}

		if ((this.guardOrderState.gotOrder() == true) && (this.guardOrderState.getOrder() != order))
		{
			this.orderDone(this.guardOrderState.getOrder());
			this.guardOrderState.resetOrder();
			++counter;
		}

		if ((this.turnOrderState.gotOrder() == true) && (this.turnOrderState.getOrder() != order))
		{
			this.orderDone(this.turnOrderState.getOrder());
			this.turnOrderState.resetOrder();
			++counter;
		}

		return counter;

	}

	// public OrderState getCurrentState ( )
	// {
	// return this.currentOrderState;
	// }

	protected void setNextCurrentOrder()
	{
		if (this.orderStateStack.size() > 0)
		{
			this.currentOrderState = this.orderStateStack.get(0);
			this.orderStateStack.remove(0);
			if (this.currentOrderState.gotOrder() == false)
			{
				this.setNextCurrentOrder();
			}

		}
		else if (this.guardOrderState.gotOrder())
		{
			this.currentOrderState = this.guardOrderState;
		}
		else if (this.attackUnitOrderState.gotOrder())
		{
			this.currentOrderState = this.attackUnitOrderState;
		}
		else if (this.attackPositionOrderState.gotOrder())
		{
			this.currentOrderState = this.attackPositionOrderState;
		}
		else if (this.moveOrderState.gotOrder())
		{
			this.currentOrderState = this.moveOrderState;
		}
		else if (this.turnOrderState.gotOrder())
		{
			this.currentOrderState = this.turnOrderState;
		}

		else
		{
			this.currentOrderState = this.stopOrderState;
		}

	}

	@Override
	protected void orderDone(Order order)
	{
		if (order != null)
		{
			this.unit.orderDone(order);
			--this.nbrOrderOnUnit;
		}
	}

	protected void stackCurrentOrder(OrderState newOrder)
	{
		this.orderStateStack.add(this.currentOrderState);
		this.currentOrderState = newOrder;
	}

}