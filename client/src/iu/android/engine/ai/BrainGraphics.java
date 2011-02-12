package iu.android.engine.ai;

import iu.android.unit.Vector2d;

public class BrainGraphics
{

	// Brain brain;

	public boolean	display	                    = true;
	public float	scale	                    = 1.1f;
	public boolean	predictedPosColor	        = false;
	public float	angleToTarget	            = -1;
	public float	viewingRadius	            = -1;
	public boolean	inFront	                    = false;
	public float	mattsAngularVelocity	    = 0;

	public Vector2d	myPredictedPositionCheck	= new Vector2d(100, 100);
	public Vector2d	otherPredictedPositionCheck	= new Vector2d(120, 120);

	BrainGraphics()
	{
		// this.brain = brain;
	}

}