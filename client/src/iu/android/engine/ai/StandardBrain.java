package iu.android.engine.ai;

import iu.android.order.Order;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

/** A dummy brain * */

public abstract class StandardBrain implements Brain
{

	protected Unit	        unit;
	protected BrainGraphics	brainGraphics;
	// protected float desiredHeading = 0;
	protected Vector2d	    target;
	protected float	        targetDirection	= 0.0f;
	protected float	        targetSpeed	    = 0.0f;
	protected float	        lastDt	        = 0.0f;

	private CollisionState	collisionState	= null;

	private int	            prevHealth	    = 100;
	boolean	                healthChanged	= false;

	public StandardBrain(Unit unit)
	{
		this.unit = unit;
		this.brainGraphics = new BrainGraphics();

		this.target = new Vector2d(unit.getPosition());
		// Orientation trial = new Orientation( 0 );

		this.collisionState = new CollisionState(unit, this);

	}

	protected abstract void orderDone(Order order);

	// protected Vector2d getTarget ( )
	// {
	// return this.target;
	// }

	public BrainGraphics getBrainGraphics()
	{
		return this.brainGraphics;
	}

	// protected CollisionState GetCollisionState ( )
	// {
	// return this.collisionState;
	// }

	// protected boolean headingEqualsTargetHeading(){
	// return( desiredHeading == unit.getDirection() );
	// }

	/*
	 * protected boolean nearDestination(){ Vector2d pos = unit.getPosition(); float dx = target.x - pos.x, dy = target.y - pos.y; // System.out.println( "target.x="+target.x+"
	 * pos.x="+pos.x+" target.y="+target.y+" pos.y="+pos.y ); // System.out.println( "dx*dx="+dx*dx+" dy*dy="+dy*dy+" 11*11="+11*11 ); return ( dx*dx + dy*dy < 11*11 ); }
	 * 
	 * protected boolean nearDestination( Vector2d targetPosition ){ Vector2d pos = unit.getPosition(); float dx = targetPosition.x - pos.x, dy = targetPosition.y - pos.y; //
	 * System.out.println( "target.x="+targetPosition.x+" pos.x="+pos.x+" target.y="+targetPosition.y+" pos.y="+pos.y ); // System.out.println( "dx*dx="+dx*dx+" dy*dy="+dy*dy+"
	 * 11*11="+11*11 ); return ( dx*dx + dy*dy < 11*11 ); }
	 * 
	 * protected boolean nearDestination( int targetX, int targetY ){ Vector2d pos = unit.getPosition(); float dx = targetX - pos.x, dy = targetY - pos.y; // System.out.println(
	 * "target.x="+targetX+" pos.x="+pos.x+" target.y="+targetY+" pos.y="+pos.y ); // System.out.println( "dx*dx="+dx*dx+" dy*dy="+dy*dy+" 11*11="+11*11 ); return ( dx*dx + dy*dy <
	 * 11*11 ); }
	 */

	// protected void setDestination (float x, float y)
	// {
	// this.target.x = x;
	// this.target.y = y;
	// // calcDesiredHeading();
	// }
	/*
	 * protected void calcDesiredHeading(){ // if ( !headingEqualsTargetHeading() ) { Vector2d pos = unit.getPosition(); float dx = target.x - pos.x, dy = target.y - pos.y;
	 * desiredHeading = Math.atan2( dx, dy );// expensive // } }
	 */
	protected float calcHeading(Vector2d presentPosition, Vector2d otherPosition)
	{

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

	// protected void moveToDestination ( )
	// {
	// this.moveToPosition (this.target);
	// /*
	// * Vector2d pos = unit.getPosition();
	// *
	// * float dx = target.x - pos.x, dy = target.y - pos.y;
	// *
	// * if ( dx*dx + dy*dy > 11*11 ){
	// *
	// * calcDesiredHeading();
	// *
	// * if ( unit.turnToHeading( this.desiredHeading ) ){ unit.setMoving( true ); if ( unit.getCurrentSpeed() <
	// * 10 ){ unit.setTargetSpeed( unit.getCurrentSpeed() + 0.1 ); } } } else { unit.setMoving( false );
	// * unit.setTargetSpeed( 0.0 ); }
	// */
	// }

	// protected void moveToPredictedPosition ( )
	// {
	// // moveToPosition( brainPredictedPosition );
	// }

	protected void moveToPosition(Vector2d otherPosition)
	{
		Unit thisUnit = this.unit;

		Vector2d unitPosition = thisUnit.getPosition();

		float dx = otherPosition.x - unitPosition.x;
		float dy = otherPosition.y - unitPosition.y;

		if (dx * dx + dy * dy > 11 * 11)
		{

			float heading = this.calcHeading(unitPosition, otherPosition);
			// this.desiredHeading = heading;

			thisUnit.setMoving(true);
			thisUnit.setTargetSpeed(thisUnit.getMaxSpeed());
			// System.out.println( "StandardBrain/moveToPosition/speed = "+unit.getMaxSpeed() );

			if (thisUnit.turnToHeading(heading))
			{
				// if ( unit.getCurrentSpeed() < 10 ){
				// unit.setTargetSpeed( unit.getCurrentSpeed() + 0.1 );
				// }
			}
		}
		else
		{
			thisUnit.setMoving(false);
			thisUnit.setTargetSpeed(0.0f);
			// System.out.println( "StandardBrain/moveToPosition/speed = 0" );

			/*
			 * if ( brainTO != null && brainTO.isDelayed() ){
			 * 
			 * float ddx = brainTO.getX() - unitPosition.x; float ddy = brainTO.getY() - unitPosition.y; float angle = Math.atan2( ddx, ddy ); //$$$$ expensive
			 * 
			 * unit.turnToHeading( angle );
			 * 
			 * brainTO = null; }
			 */
			// unit.setTargetSpeed( 0.0 );
		}
	}

	// public boolean isInCollisionState ( )
	// {
	// // String currentString = currentOrderState.orderStateAsString();
	// // return ( currentString == "Collision State" );
	// return false;
	// }

	public final Unit CollidingWith()
	{
		// String currentString = currentOrderState.orderStateAsString();
		// if( currentString == "Collision State" )
		// {
		// Unit collisionUnit = this.collisionState.getCollisionUnit ( );
		Unit collisionUnit = this.collisionState.neighbourUnit;

		return collisionUnit;
		// }
		// return null;

	}

	protected void checkHealth()
	{

		int presentHealth = this.unit.getHealth();

		if (presentHealth != this.prevHealth)
		{
			// System.out.println( "StandardBrain/checkHealth/ presentHealth = "+presentHealth+"
			// prevHealth="+prevHealth );

			this.healthChanged = true;

			this.prevHealth = presentHealth;
		}

	}

	// protected boolean isHealthChanged ( )
	// {
	// return this.healthChanged;
	// }

	// protected void resetHealthChanged ( )
	// {
	// this.healthChanged = false;
	// }

}