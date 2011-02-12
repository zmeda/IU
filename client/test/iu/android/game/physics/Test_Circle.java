package iu.android.game.physics;

import junit.framework.TestCase;

import android.graphics.Rect;

/**
 * A test case for Circle. It uses the desktop version which needs some classes from the AWT framework. The
 * JRE is required on the class path for this test to work (to compile, even!).
 * 
 * @author luka
 */
public class Test_Circle extends TestCase
{

	private static final int						Loops	= 100;
	private iu.android.game.physics.Circle[]	a;
	private iu.desktop.game.physics.Circle[]	d;


	@Override
	protected void setUp ( ) throws Exception
	{
		this.a = new iu.android.game.physics.Circle[Loops];
		this.d = new iu.desktop.game.physics.Circle[Loops];

		for (int i = 0; i < Loops; i++)
		{
			final float[] xyr = randomXYR ( );
			this.a[i] = new iu.android.game.physics.Circle (xyr[0], xyr[1], xyr[2]);
			this.d[i] = new iu.desktop.game.physics.Circle (xyr[0], xyr[1], xyr[2]);
		}
	}


	@Override
	protected void tearDown ( ) throws Exception
	{
		super.tearDown ( );
	}


	public void testContains ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			assertEquals (this.a[i].contains (xy[0], xy[1]), this.d[i].contains (xy[0], xy[1]));
		}
	}


	public void testIsCollidingCircle ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xyr = randomXYR ( );

			final iu.android.game.physics.Circle random_a = new iu.android.game.physics.Circle (xyr[0], xyr[1], xyr[2]);
			final iu.desktop.game.physics.Circle random_d = new iu.desktop.game.physics.Circle (xyr[0], xyr[1], xyr[2]);

			assertEquals (this.a[i].isColliding (random_a), this.d[i].isColliding (random_d));
		}
	}


	public void testIsCollidingDoubleDoubleDouble ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xyr = randomXYR ( );

			assertEquals (this.a[i].isColliding (xyr[0], xyr[1], xyr[2]), this.d[i].isColliding (xyr[0], xyr[1], xyr[2]));
		}
	}


	public void testIsCollidingRect ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final float[] xy = randomXY ( );
			final float[] wh = randomWH ( );

			final int x = (int) xy[0];
			final int y = (int) xy[1];
			final int w = (int) wh[0];
			final int h = (int) wh[1];

			final Rect rect_a = new Rect (x, y, x + w, y + h);
			final java.awt.Rectangle rect_d = new java.awt.Rectangle (x, y, w, h);

			assertEquals (this.a[i].isColliding (rect_a), this.d[i].isColliding (rect_d));
		}
	}


	public void testGetBounds ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final Rect rect_a = this.a[i].getBounds ( );
			final java.awt.Rectangle rect_d = this.d[i].getBounds ( );

			assertEquals (rect_a.left, rect_d.x);
			assertEquals (rect_a.top, rect_d.y);
			assertEquals (rect_a.right, rect_d.x + rect_d.width);
			assertEquals (rect_a.bottom, rect_d.y + rect_d.height);
			assertEquals (rect_a.height ( ), rect_d.height);
			assertEquals (rect_a.width ( ), rect_d.width);
		}
	}


	public void testCalcIntersectionNormal ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final iu.android.game.physics.Vector2d vec_a = new iu.android.game.physics.Vector2d ( );
			final iu.desktop.game.physics.Vector2d vec_d = new iu.desktop.game.physics.Vector2d ( );

			final float[] xy = randomXY ( );
			final float[] wh = randomWH ( );

			final int x = (int) xy[0];
			final int y = (int) xy[1];
			final int w = (int) wh[0];
			final int h = (int) wh[1];

			final Rect rect_a = new Rect (x, y, x + w, y + h);
			final java.awt.Rectangle rect_d = new java.awt.Rectangle (x, y, w, h);

			this.a[i].calcIntersectionNormal (rect_a, vec_a);
			this.d[i].calcIntersectionNormal (rect_d, vec_d);

			assertEquals (vec_a.x, vec_d.x);
			assertEquals (vec_a.y, vec_d.y);
		}
	}


	public void testCalculatePointsOfIntersection ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			final iu.android.game.physics.Vector2d vec_a1 = new iu.android.game.physics.Vector2d ( );
			final iu.android.game.physics.Vector2d vec_a2 = new iu.android.game.physics.Vector2d ( );
			final iu.desktop.game.physics.Vector2d vec_d1 = new iu.desktop.game.physics.Vector2d ( );
			final iu.desktop.game.physics.Vector2d vec_d2 = new iu.desktop.game.physics.Vector2d ( );

			final float[] xyr = randomXYR ( );

			final iu.android.game.physics.Circle circ_a = new iu.android.game.physics.Circle (xyr[0], xyr[1], xyr[2]);
			final iu.desktop.game.physics.Circle circ_d = new iu.desktop.game.physics.Circle (xyr[0], xyr[1], xyr[2]);

			this.a[i].calculatePointsOfIntersection (circ_a, vec_a1, vec_a2);
			this.d[i].calculatePointsOfIntersection (circ_d, vec_d1, vec_d2);

			assertEquals (vec_a1.x, vec_d1.x);
			assertEquals (vec_a1.y, vec_d1.y);
			assertEquals (vec_a2.x, vec_d2.x);
			assertEquals (vec_a2.y, vec_d2.y);
		}
	}


	private static float[] randomWH ( )
	{
		final float[] hw = new float[2];
		hw[0] = Math.random ( ) * 2;
		hw[1] = Math.random ( ) * 2;
		return hw;
	}


	private static float[] randomXY ( )
	{
		final float[] xy = new float[2];
		xy[0] = Math.random ( ) * 10 - 5;
		xy[1] = Math.random ( ) * 10 - 5;
		return xy;
	}


	private static float[] randomXYR ( )
	{
		final float[] xyr = new float[3];
		xyr[0] = Math.random ( ) * 10 - 5;
		xyr[1] = Math.random ( ) * 10 - 5;
		xyr[2] = Math.random ( ) * 7;
		return xyr;
	}
}
