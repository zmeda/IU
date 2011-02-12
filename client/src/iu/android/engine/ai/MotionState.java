package iu.android.engine.ai;

import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

public abstract class MotionState extends StationaryState
{

	boolean	stopAtPosition	= true;

	public MotionState(Unit unit)
	{
		super(unit);
	}

	// protected void setStopAtPosition (boolean stopAtPosition)
	// {
	// this.stopAtPosition = stopAtPosition;
	// }

	protected void moveToPosition(Vector2d otherPosition)
	{
		Unit thisUnit = this.unit;

		Vector2d unitPosition = thisUnit.getPosition();

		if (this.stopAtPosition == true)
		{
			if (this.nearDestination(otherPosition))
			{

				float heading = this.calcHeading(unitPosition, otherPosition);

				thisUnit.setMoving(true);
				thisUnit.setTargetSpeed(thisUnit.getMaxSpeed());

				thisUnit.turnToHeading(heading);
			}
			else
			{
				thisUnit.setMoving(false);
				thisUnit.setTargetSpeed(0.0f);
			}

		}
		else
		{
			float heading = this.calcHeading(unitPosition, otherPosition);

			thisUnit.setMoving(true);
			thisUnit.setTargetSpeed(thisUnit.getMaxSpeed());

			thisUnit.turnToHeading(heading);
		}

	}

}