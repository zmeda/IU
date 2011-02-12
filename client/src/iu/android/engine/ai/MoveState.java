package iu.android.engine.ai;

import iu.android.Debug;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

public class MoveState extends StationaryState
{

	boolean	       stopAtPosition	= true;

	CollisionState	collisionState;

	Vector2d	   destination;

	boolean	       doCollisionAvoidance;
	boolean	       doingCollisionAvoidance;

	public MoveState(Unit unit, CollisionState collisionState)
	{
		super(unit);
		this.collisionState = collisionState;
		this.doCollisionAvoidance = false;
		this.doingCollisionAvoidance = false;
	}

	@Override
	public boolean actionDone()
	{

		Vector2d dest = this.destination;

		Debug.assertCompare(dest != null, "destination != null");

		Unit thisUnit = this.unit;
		Vector2d unitPosition = thisUnit.getPosition();

		if (this.nearDestination(dest))
		{
			if (this.stopAtPosition == true)
			{
				thisUnit.setMoving(false);
				thisUnit.setTargetSpeed(0.0f);
			}

			return true;

		}
		else
		{

			float heading = this.calcHeading(unitPosition, dest);

			thisUnit.setMoving(true);
			thisUnit.setTargetSpeed(thisUnit.getMaxSpeed());

			thisUnit.turnToHeading(heading);
			return false;

		}
	}

	// protected void setStopAtPosition (boolean stopAtPosition)
	// {
	// this.stopAtPosition = stopAtPosition;
	// }
	//
	//
	// public void setDestination (Vector2d destination)
	// {
	// this.destination = destination;
	// }
	//
	//
	// protected void setDoCollisionAvoidance (boolean doCollisionAvoidance)
	// {
	// this.doCollisionAvoidance = doCollisionAvoidance;
	// }
	//
	//
	// protected boolean isDoingCollisionAvoidance ( )
	// {
	// return this.doCollisionAvoidance;
	// }

}