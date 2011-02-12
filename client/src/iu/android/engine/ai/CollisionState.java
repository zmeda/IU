package iu.android.engine.ai;

import iu.android.Debug;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

public class CollisionState extends MotionState
{

	StandardBrain	brain;

	Unit	      neighbourUnit	             = null;

	// Order otherOrder;
	OrderState	  otherOrderState;

	float	      unitRadius;

	float	      scale	                     = 1.1f;

	Vector2d	  myPos	                     = null;
	Vector2d	  myPredictedPos	         = null;
	Vector2d	  otherUnitPosition	         = null;
	Vector2d	  otherUnitPredictedPosition	= null;

	Vector2d	  myPreviousPos	             = null;
	float	      previousSpeed	             = 0;
	float	      previousDirection	         = 0;

	float	      collisionAvoidanceHeading;	               // the calculated heading that the avoids
	// the other unit

	int	          avoidanceDirection;	                       // the direction to turn. -1 == Left,
	// 1 == Right,
	// 0 == collisionAvoidanceHeading not calculated yet or cannot be decided so just stop

	Vector2d	  desiredPosition;
	float	      desiredDirection;

	int	          checkingCounter	         = 0;

	int	          tick	                     = 0;

	float	      debugWinScale	             = 1.0f;

	// [ - was never used] Vector myPreviousPredictedPositions;
	// [ - was never used] Vector otherPreviousPredictedPositions;

	// [ - was never used] Vector myPreviousPositions;
	// [ - was never used] Vector otherPreviousPositions;

	int	          myPreviousPositionsIndex;
	int	          otherPreviousPositionsIndex;

	Vector2d	  relativePosition;

	// [ - was never used] Vector callStack = new Vector ( );

	Vector2d	  startPoint	             = new Vector2d();
	Vector2d	  stopPoint	                 = new Vector2d();

	Vector2d	  previousPos	             = new Vector2d();
	Vector2d	  otherPreviousPos	         = new Vector2d();

	// [ - was never used] private Vector allCollisionUnitsVector; // the units that the state is
	// avoiding so all unit that
	// have predicted collision positions PAST and PRESENT

	// [ - was never used] private Vector neighbourhoodCollisionUnitsVector; // reference to the neighbour present

	// collision vector

	public CollisionState(Unit unit, StandardBrain brain)
	{
		super(unit);
		this.brain = brain;

		// [ - was never used] this.neighbourhoodCollisionUnitsVector = neighbourhood.getCollisionUnitsVector ( );

		this.unitRadius = unit.getBounds().radius;

		// myPos = new Vector2d(); // not needed just get pointer to vector2d on unit
		this.myPos = unit.getPosition();
		this.myPredictedPos = new Vector2d();
		// otherUnitPosition = new Vector2d();
		this.otherUnitPredictedPosition = new Vector2d();

		this.myPreviousPos = new Vector2d();

		this.collisionAvoidanceHeading = 10.0f;

		this.avoidanceDirection = 0;

		// desiredPosition = new Vector2d();
		this.desiredDirection = 10.0f;

		// [ - was never used] this.myPreviousPredictedPositions = new Vector ( );
		// [ - was never used] this.otherPreviousPredictedPositions = new Vector ( );

		// [ - was never used] this.myPreviousPositions = new Vector ( );
		// [ - was never used] this.otherPreviousPositions = new Vector ( );

		this.relativePosition = new Vector2d();

		// moveOrder = null;
	}

	/*
	 * 
	 * public boolean action(){ // ideal latest with Collision Vector;
	 * 
	 * 
	 * 
	 * first time through { add the present neighbourhoodCollisionUnitsVector into allCollisionUnitsVector but NO duplications
	 * 
	 * assess the units to avoid and plan a route though
	 * 
	 * default action stop }
	 * 
	 * after first time through {
	 * 
	 * check for new unit Collisions in neighbourhood { add the present neighbourhoodCollisionUnitsVector into allCollisionUnitsVector but NO duplications
	 * 
	 * check plan is ok
	 * 
	 * if not { assess the units to avoid and plan a route though } }
	 * 
	 * 
	 * check the plan quickly (maybe only every could of time steps)
	 * 
	 * execute the plan { check for finishing conditions
	 * 
	 * 
	 * if predicted position collisions on present course { turn ( till no predicted position collisions ) ( slow down ??? ) } else NO predicted position collisions on present
	 * course { max speed
	 * 
	 * can i turn to desired heading { turn to desired heading } } } } }
	 */

	@Override
	public boolean actionDone()
	{
		/*
		 * // first time through if( avoidanceDirection == 0 ) { Debug.assertCompare( neighbourhood.getCollisionUnitsVectorSize() > 0, "neighbourhood.getCollisionUnitsVectorSize()
		 * should be >0 but = "+neighbourhood.getCollisionUnitsVectorSize()+" !!!");
		 * 
		 * //add the present neighbourhoodCollisionUnitsVector into allCollisionUnitsVector but NO duplications for( int i = 0; i < neighbourhoodCollisionUnitsVector.size(); i++ ) {
		 * allCollisionUnitsVector.addElement( neighbourhoodCollisionUnitsVector.elementAt( i ) ); }
		 * 
		 * 
		 * //assess the units to avoid and plan a route though calcAvoidanceDirection( 2 ); // 2 * atan2
		 * 
		 * 
		 * //default action stop unit.setMoving( false ); unit.setTargetSpeed( 0.0 ); tick = 0; resetGraphics(); } else //after first time through {
		 * 
		 * CheckForNewCollisions(); // check the plan quickly (maybe only every could of time steps) // do in previous Check
		 * 
		 * execute the plan { check for finishing conditions
		 * 
		 * 
		 * if predicted position collisions on present course { turn ( till no predicted position collisions ) ( slow down ??? ) } else NO predicted position collisions on present
		 * course { max speed
		 * 
		 * can i turn to desired heading { turn to desired heading } } } }
		 */
		return true;
	}

	/*
	 * public boolean action(){ // new
	 * 
	 * if ( Debug.MATT ) { Debug.assertCompare( desiredPosition != null, "desiredPosition = "+desiredPosition+" should not be null" ); Debug.assertCompare( unit != null, "unit =
	 * "+unit+" should not be null" ); Debug.assertCompare( neighbourUnit != null, "neighbourUnit = "+neighbourUnit+" should not be null" ); }
	 * 
	 * tick++;
	 * 
	 * Vector callStack = startCallStack( tick );
	 * 
	 * updateHistory();
	 * 
	 * if( avoidanceDirection == 0 ) { // System.out.println( "Phase 1 speed = "+unit.getCurrentSpeed()+" direction = "+unit.getDirection() );
	 * 
	 * calcAvoidanceDirection( 2 ); // 2 * atan2 unit.setMoving( false ); unit.setTargetSpeed( 0.0 ); tick = 0; resetGraphics(); } else {
	 * 
	 * return avoidCollision( callStack ); } return true; }
	 * 
	 * 
	 */

	/*
	 * private void CheckForNewCollisions() { if( neighbourhood.getCollisionUnitsVectorSize() <=0 ) { check for new unit Collisions in neighbourhood { // add the present
	 * neighbourhoodCollisionUnitsVector into allCollisionUnitsVector but NO duplications
	 * 
	 * for( int i = 0; i < neighbourhoodCollisionUnitsVector.size(); i++ ) { if( allCollisionUnitsVector.contains( neighbourhoodCollisionUnitsVector.elementAt( i ) ) == false ) {
	 * allCollisionUnitsVector.addElement( neighbourhoodCollisionUnitsVector.elementAt( i ) ); } }
	 * 
	 * 
	 * 
	 * check plan is ok
	 * 
	 * if not { assess the units to avoid and plan a route though } } } }
	 * 
	 * 
	 * 
	 * Debug.assertCompare( neighbourhood.getCollisionUnitsVectorSize() > 0, "neighbourhood.getCollisionUnitsVectorSize() should be >0 but =
	 * "+neighbourhood.getCollisionUnitsVectorSize()+" !!!");
	 * 
	 * numUnitsToAvoid = neighbourhood.getCollisionUnitsVectorSize();
	 * 
	 * //add the present neighbourhoodCollisionUnitsVector into allCollisionUnitsVector but NO duplications for( int i = 0; i < neighbourhoodCollisionUnitsVector.size(); i++ ) {
	 * allCollisionUnitsVector.addElement( neighbourhoodCollisionUnitsVector.elementAt( i ) ); }
	 * 
	 * 
	 * 
	 */

	public Vector2d calcPos(Vector2d position)
	{
		Vector2d newPos = new Vector2d(position);
		this.debugWinScale = 1.5f;

		newPos.subtract(this.relativePosition);

		newPos.multiply(this.debugWinScale);

		newPos.x += 127;
		newPos.y += 127;

		return newPos;
	}

	public float huns(float value)
	{
		int intValue = (int) (value * 100);
		// float newValue = (intValue / 100.0);
		float newValue = (intValue * 0.01f);
		return newValue;
	}

	public void setDesiredPosition(Vector2d position)
	{
		this.desiredPosition = position;
		if (Debug.JOSHUA)
		{
			Debug.assertCompare(this.desiredPosition != null, "desiredPosition = " + this.desiredPosition + "  should not be null");
		}
	}

	public void SetUnitToAvoid(Unit otherUnit)
	{
		if (Debug.JOSHUA)
		{
			Debug.assertCompare(otherUnit != null, "otherUnit = " + otherUnit + "  should not be null");
		}
		this.neighbourUnit = otherUnit;
		this.otherUnitPosition = otherUnit.getPosition();
		if (Debug.JOSHUA)
		{
			Debug.assertCompare(this.otherUnitPosition != null, "otherUnitPosition = " + this.otherUnitPosition + "  should not be null");
		}

	}

	// public final Unit getCollisionUnit ( )
	// {
	// return this.neighbourUnit;
	// }

	// public final void setOtherOrderState (OrderState otherOrderState)
	// {
	// this.otherOrderState = otherOrderState;
	// }

}
