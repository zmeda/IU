package iu.android.engine.ai;

import iu.android.Debug;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

public abstract class StationaryState extends State
{

	Unit	unit;

	public StationaryState(Unit unit)
	{
		this.unit = unit;
	}

	@Override
	public abstract boolean actionDone();

	protected boolean nearDestination(Vector2d targetPosition)
	{
		return (this.nearDestination((int) targetPosition.x, (int) targetPosition.y));
	}

	protected boolean nearDestination(int targetX, int targetY)
	{
		Vector2d pos = this.unit.getPosition();
		float dx = targetX - pos.x, dy = targetY - pos.y;
		// System.out.println( "target.x="+targetX+" pos.x="+pos.x+" target.y="+targetY+" pos.y="+pos.y );
		// System.out.println( "dx*dx="+dx*dx+" dy*dy="+dy*dy+" 11*11="+11*11 );
		return (dx * dx + dy * dy < 11 * 11);
	}

	protected float calcHeading(Vector2d presentPosition, Vector2d otherPosition)
	{

		if (Debug.JOSHUA)
		{
			Debug.assertCompare(presentPosition != null, "presentPosition = " + presentPosition + "  should not be equal to null");
			Debug.assertCompare(otherPosition != null, "otherPosition = " + otherPosition + "  should not be equal to null");
		}

		if (presentPosition == null)
		{
			System.out.println("presentPosition == null");
			return 0.0f;
		}
		if (otherPosition == null)
		{
			System.out.println("otherPosition == null");
			return 0.0f;
		}

		float dx = otherPosition.x - presentPosition.x;
		float dy = otherPosition.y - presentPosition.y;
		float heading = (float) Math.atan2(dx, dy);// expensive

		return heading;
	}
}