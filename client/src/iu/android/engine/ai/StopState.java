package iu.android.engine.ai;

import iu.android.Debug;
import iu.android.unit.GeneralPhysics;
import iu.android.unit.Unit;
import iu.android.unit.Weapon;

public class StopState extends StationaryState
{

	StandardBrain	brain;

	Unit	      nearestEnemyUnit	  = null;

	// boolean underAttack = false;

	int	          tick	              = 0;
	float	      tickSinceLastAttack	= 0.0f;

	public StopState(Unit unit, StandardBrain brain)
	{
		super(unit);

		this.brain = brain;
	}

	@Override
	public boolean actionDone()
	{
		Unit thisUnit = this.unit;

		thisUnit.setMoving(false);
		thisUnit.setTargetSpeed(0.0f);
		// if( unit.getID() == 2 )
		// {
		// System.out.println("StopState/action unit = "+unit.getID()+" "+unit.getPosition().toString() );
		// }

		// if (this.brain.isHealthChanged ( ))
		if (this.brain.healthChanged)
		{
			// this.brain.resetHealthChanged ( );
			this.brain.healthChanged = false;

			this.nearestEnemyUnit = thisUnit.findNearestEnemy();

			// underAttack = true;

			// tick = 6;
			// tickSinceLastAttack = 0.0;
		}

		// System.out.println("StopState/action unit = "+unit.getID()+" "+unit.getPosition().toString()+" losing
		// health" );

		// if( underAttack == true )
		// {

		if (this.nearestEnemyUnit != null)
		{
			if (this.nearestEnemyUnit.isDead() == true)
			{
				this.nearestEnemyUnit = null;
				// underAttack = false;
			}
			else
			{
				float direction = this.calcHeading(thisUnit.getPosition(), this.nearestEnemyUnit.getPosition());

				if (thisUnit.turnToHeading(direction))
				{

					Weapon[] weapons = thisUnit.getWeapons();

					if (weapons.length > 0)
					{
						// if ( Debug.MATT )
						Debug.assertCompare(weapons.length != 0, "weapons.length = " + weapons.length);

						if (GeneralPhysics.distanceSqr(thisUnit.getPosition(), this.nearestEnemyUnit.getPosition()) < (weapons[0].getRange() * weapons[0].getRange()))
						{

							if (weapons[0].isReady())
							{
								weapons[0].fireOnPosition((int) this.nearestEnemyUnit.getPosition().x, (int) this.nearestEnemyUnit.getPosition().y);
								// nNbrHits--;
								// System.out.println( "Fire !!! Boom Bang" );
							}

						}
						else
						{
							// System.out.println( "Help !!! under attack but too far away to shoot back without
							// moving" );
							this.nearestEnemyUnit = null;
							// underAttack = false;
						}

					}
				}
			}

		}
		else
		{
			// System.out.println("StopState/action unit = "+unit.getID()+" "+unit.getPosition().toString()+" did
			// not
			// find the nearest emeny" );
		}

		// }

		return false;
	}

}
