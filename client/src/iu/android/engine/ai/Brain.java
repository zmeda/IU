package iu.android.engine.ai;

import iu.android.unit.Unit;

/** Interface for keeping the AI separate from the unit classes. * */
public interface Brain
{

	/** dt is the timestep length. * */
	public void think(float dt);

	public Unit CollidingWith();

	// public Vector2d getTarget();

	public BrainGraphics getBrainGraphics();

	// protected void orderDone( Order order );

	// public void moveToPosition( Vector2d otherPosition );

	// public boolean nearDestination( Vector2d targetPosition );

}