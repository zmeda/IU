package iu.android.engine.ai;

import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

public class TurnState extends StationaryState
{

	Vector2d	target;
	float	 direction	= -10f;

	public TurnState(Unit unit)
	{
		super(unit);
	}

	// public Vector2d getTarget ( )
	// {
	// return this.target;
	// }
	//
	//
	// public void setTarget (Vector2d target)
	// {
	// this.target = target;
	// }

	@Override
	public boolean actionDone()
	{
		this.direction = this.calcHeading(this.unit.getPosition(), this.target);
		return this.unit.turnToHeading(this.direction);
	}

}
