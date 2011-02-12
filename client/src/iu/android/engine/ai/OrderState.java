package iu.android.engine.ai;

import iu.android.order.Order;

public abstract class OrderState extends State
{

	public abstract Order getOrder ( );


	public abstract void setOrder (Order order);


	public abstract void orderDone ( );


	public abstract void resetOrder ( );


	public abstract boolean gotOrder ( );

}