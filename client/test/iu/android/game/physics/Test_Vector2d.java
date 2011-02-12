package iu.android.game.physics;

import junit.framework.TestCase;

public class Test_Vector2d extends TestCase
{
	private static final int							Loops	= 100;
	private iu.android.game.physics.Vector2d[]	vectors_a;
	private iu.android.game.physics.Vector2d[]	copies_a;
	private iu.desktop.game.physics.Vector2d[]	vectors_d;
	private iu.desktop.game.physics.Vector2d[]	copies_d;


	@Override
	protected void setUp ( ) throws Exception
	{
		super.setUp ( );

		this.vectors_a = new iu.android.game.physics.Vector2d[Loops];
		this.copies_a = new iu.android.game.physics.Vector2d[Loops];
		this.vectors_d = new iu.desktop.game.physics.Vector2d[Loops];
		this.copies_d = new iu.desktop.game.physics.Vector2d[Loops];

		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			this.vectors_a[i] = new iu.android.game.physics.Vector2d (xy[0], xy[1]);
			this.copies_a[i] = new iu.android.game.physics.Vector2d (this.vectors_a[i]);
			this.vectors_d[i] = new iu.desktop.game.physics.Vector2d (xy[0], xy[1]);
			this.copies_d[i] = new iu.desktop.game.physics.Vector2d (this.vectors_d[i]);
		}
	}


	@Override
	protected void tearDown ( ) throws Exception
	{
		super.tearDown ( );
	}


	public void testZero ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			this.vectors_a[i].zero ( );
			this.copies_a[i].zero ( );
			this.vectors_d[i].zero ( );
			this.copies_d[i].zero ( );

			assertEquals (this.vectors_a[i].x, this.vectors_d[i].x);
			assertEquals (this.vectors_a[i].y, this.vectors_d[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_a[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_a[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_d[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_d[i].y);
		}
	}


	public void testAddVector2d ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			final iu.android.game.physics.Vector2d random_a = new iu.android.game.physics.Vector2d (xy[0], xy[1]);
			final iu.desktop.game.physics.Vector2d random_d = new iu.desktop.game.physics.Vector2d (xy[0], xy[1]);

			this.vectors_a[i].add (random_a);
			this.copies_a[i].add (random_a);
			this.vectors_d[i].add (random_d);
			this.copies_d[i].add (random_d);

			assertEquals (this.vectors_a[i].x, this.vectors_d[i].x);
			assertEquals (this.vectors_a[i].y, this.vectors_d[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_a[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_a[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_d[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_d[i].y);
		}
	}


	public void testAddDoubleVector2d ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			final float scalar = Math.random ( );
			final iu.android.game.physics.Vector2d random_a = new iu.android.game.physics.Vector2d (xy[0], xy[1]);
			final iu.desktop.game.physics.Vector2d random_d = new iu.desktop.game.physics.Vector2d (xy[0], xy[1]);

			this.vectors_a[i].add (scalar, random_a);
			this.copies_a[i].add (scalar, random_a);
			this.vectors_d[i].add (scalar, random_d);
			this.copies_d[i].add (scalar, random_d);

			assertEquals (this.vectors_a[i].x, this.vectors_d[i].x);
			assertEquals (this.vectors_a[i].y, this.vectors_d[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_a[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_a[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_d[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_d[i].y);
		}
	}


	public void testSubtractVector2d ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			final iu.android.game.physics.Vector2d random_a = new iu.android.game.physics.Vector2d (xy[0], xy[1]);
			final iu.desktop.game.physics.Vector2d random_d = new iu.desktop.game.physics.Vector2d (xy[0], xy[1]);

			this.vectors_a[i].subtract (random_a);
			this.copies_a[i].subtract (random_a);
			this.vectors_d[i].subtract (random_d);
			this.copies_d[i].subtract (random_d);

			assertEquals (this.vectors_a[i].x, this.vectors_d[i].x);
			assertEquals (this.vectors_a[i].y, this.vectors_d[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_a[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_a[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_d[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_d[i].y);
		}
	}


	public void testSubtractDoubleVector2d ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			final float scalar = Math.random ( );
			final iu.android.game.physics.Vector2d random_a = new iu.android.game.physics.Vector2d (xy[0], xy[1]);
			final iu.desktop.game.physics.Vector2d random_d = new iu.desktop.game.physics.Vector2d (xy[0], xy[1]);

			this.vectors_a[i].subtract (scalar, random_a);
			this.copies_a[i].subtract (scalar, random_a);
			this.vectors_d[i].subtract (scalar, random_d);
			this.copies_d[i].subtract (scalar, random_d);

			assertEquals (this.vectors_a[i].x, this.vectors_d[i].x);
			assertEquals (this.vectors_a[i].y, this.vectors_d[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_a[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_a[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_d[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_d[i].y);
		}
	}


	public void testMultiply ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float scalar = Math.random ( );

			this.vectors_a[i].multiply (scalar);
			this.copies_a[i].multiply (scalar);
			this.vectors_d[i].multiply (scalar);
			this.copies_d[i].multiply (scalar);

			assertEquals (this.vectors_a[i].x, this.vectors_d[i].x);
			assertEquals (this.vectors_a[i].y, this.vectors_d[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_a[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_a[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_d[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_d[i].y);
		}
	}


	public void testPerpendicular ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			this.vectors_a[i].perpendicular ( );
			this.copies_a[i].perpendicular ( );
			this.vectors_d[i].perpendicular ( );
			this.copies_d[i].perpendicular ( );

			assertEquals (this.vectors_a[i].x, this.vectors_d[i].x);
			assertEquals (this.vectors_a[i].y, this.vectors_d[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_a[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_a[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_d[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_d[i].y);
		}
	}


	public void testGetDotProduct ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			final iu.android.game.physics.Vector2d random_a = new iu.android.game.physics.Vector2d (xy[0], xy[1]);
			final iu.desktop.game.physics.Vector2d random_d = new iu.desktop.game.physics.Vector2d (xy[0], xy[1]);

			assertEquals (this.vectors_a[i].getDotProduct (random_a), this.vectors_d[i].getDotProduct (random_d));
			assertEquals (this.vectors_a[i].getDotProduct (random_a), this.copies_a[i].getDotProduct (random_a));
			assertEquals (this.vectors_a[i].getDotProduct (random_a), this.copies_d[i].getDotProduct (random_d));
		}
	}


	public void testGetPerpDotProduct ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			final iu.android.game.physics.Vector2d random_a = new iu.android.game.physics.Vector2d (xy[0], xy[1]);
			final iu.desktop.game.physics.Vector2d random_d = new iu.desktop.game.physics.Vector2d (xy[0], xy[1]);

			assertEquals (this.vectors_a[i].getPerpDotProduct (random_a), this.vectors_d[i].getPerpDotProduct (random_d));
			assertEquals (this.vectors_a[i].getPerpDotProduct (random_a), this.copies_a[i].getPerpDotProduct (random_a));
			assertEquals (this.vectors_a[i].getPerpDotProduct (random_a), this.copies_d[i].getPerpDotProduct (random_d));

			this.vectors_a[i].perpendicular ( );
			this.copies_a[i].perpendicular ( );
			this.vectors_d[i].perpendicular ( );
			this.copies_d[i].perpendicular ( );
			assertEquals (this.vectors_a[i].getDotProduct (random_a), this.vectors_d[i].getDotProduct (random_d));
			assertEquals (this.vectors_a[i].getDotProduct (random_a), this.copies_a[i].getDotProduct (random_a));
			assertEquals (this.vectors_a[i].getDotProduct (random_a), this.copies_d[i].getDotProduct (random_d));
		}
	}


	public void testDistanceSq ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			final iu.android.game.physics.Vector2d random_a = new iu.android.game.physics.Vector2d (xy[0], xy[1]);
			final iu.desktop.game.physics.Vector2d random_d = new iu.desktop.game.physics.Vector2d (xy[0], xy[1]);

			assertEquals (this.vectors_a[i].distanceSq (random_a), this.vectors_d[i].distanceSq (random_d));
			assertEquals (this.vectors_a[i].distanceSq (random_a), this.copies_a[i].distanceSq (random_a));
			assertEquals (this.vectors_a[i].distanceSq (random_a), this.copies_d[i].distanceSq (random_d));
		}
	}


	public void testGetMagnitude ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			assertEquals (this.vectors_a[i].getMagnitude ( ), this.vectors_d[i].getMagnitude ( ));
			assertEquals (this.vectors_a[i].getMagnitude ( ), this.copies_a[i].getMagnitude ( ));
			assertEquals (this.vectors_a[i].getMagnitude ( ), this.copies_d[i].getMagnitude ( ));
		}
	}


	public void testToUnitVector ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			this.vectors_a[i].toUnitVector ( );
			this.copies_a[i].toUnitVector ( );
			this.vectors_d[i].toUnitVector ( );
			this.copies_d[i].toUnitVector ( );

			assertEquals (this.vectors_a[i].x, this.vectors_d[i].x);
			assertEquals (this.vectors_a[i].y, this.vectors_d[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_a[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_a[i].y);
			assertEquals (this.vectors_a[i].x, this.copies_d[i].x);
			assertEquals (this.vectors_a[i].y, this.copies_d[i].y);
		}
	}


	public void testIsZeroVector ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			assertTrue ((this.vectors_a[i].x == 0 && this.vectors_a[i].y == 0) == (this.vectors_a[i].isZeroVector ( )));
		}
	}


	private static float[] randomXY ( )
	{
		final float[] xy = new float[2];
		xy[0] = Math.random ( ) * 1000 - 500;
		xy[1] = Math.random ( ) * 1000 - 500;
		return xy;
	}
}
