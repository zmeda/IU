package iu.android.engine.ai;

import iu.android.Debug;
import iu.android.unit.GeneralPhysics;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;
import iu.android.unit.Weapon;

public class AttackState extends StationaryState
{

	MoveState	moveState;

	Vector2d	targetPosition	= null;

	boolean	  inRange	       = false;

	public AttackState(Unit unit, MoveState moveState)
	{
		super(unit);
		this.moveState = moveState;
	}

	void setTargetPosition(Vector2d targetPosition)
	{
		this.targetPosition = targetPosition;

		// this.moveState.setDestination (targetPosition);
		this.moveState.destination = targetPosition;

		Weapon[] weapons = this.unit.getWeapons();
		if (Debug.BORIS)
		{
			Debug.assertCompare(weapons.length != 0, "weapons.length = " + weapons.length);
		}
		if (weapons.length != 0)
		{

			if (GeneralPhysics.distanceSqr(this.unit.getPosition(), targetPosition) > (weapons[0].getRange() * weapons[0].getRange()))
			{
				this.inRange = false;
			}
			else
			{
				this.inRange = true;
			}
		}

	}

	// public boolean isInRange ( )
	// {
	// return this.inRange;
	// }

	@Override
	public boolean actionDone()
	{
		Weapon[] weapons = this.unit.getWeapons();

		if (Debug.BORIS)
		{
			Debug.assertCompare(weapons.length != 0, "weapons.length = " + weapons.length);

		}
		if (weapons.length != 0)
		{

			if (GeneralPhysics.distanceSqr(this.unit.getPosition(), this.targetPosition) > (weapons[0].getRange() * weapons[0].getRange()))
			{
				// System.out.println( "AttackUnitOrderState/action nearDestination == true " );
				this.moveState.actionDone();
				// moveToPosition( targetPosition );
				this.inRange = false;
			}
			/*
			 * else if( GeneralPhysics.distanceSqr( unit.getPosition(), targetPosition ) < ( weapons[0].getMinimumRange() * weapons[0].getMinimumRange() ) ){ // System.out.println(
			 * "AttackUnitOrderState/action nearDestination == true " ); unit.setMoving( true ); unit.setTargetSpeed( -unit.getMaxSpeed() ); float direction = calcHeading(
			 * unit.getPosition(), targetPosition ); unit.turnToHeading( direction ); }
			 */else
			{
				Unit thisUnit = this.unit;

				this.inRange = true;

				thisUnit.setMoving(false);
				thisUnit.setTargetSpeed(0.0f);
				// System.out.println( "AttackUnitOrderState/action nearDestination == true " );

				float direction = this.calcHeading(thisUnit.getPosition(), this.targetPosition);

				if (thisUnit.turnToHeading(direction))
				{

					if (weapons[0].isReady())
					{
						weapons[0].fireOnPosition((int) this.targetPosition.x, (int) this.targetPosition.y);
						return true;

						// nNbrHits--;
						// System.out.println( "Fire !!! Boom Bang" );
					}

				}

				// System.out.println( "AttackUnitOrderState/action nearDestination == false " );
			}
		}

		return false;

	}
}