package iu.android.game;

import junit.framework.TestCase;

public class Test_Explosion extends TestCase
{

	private static final int				Loops	= 100;

	private iu.android.game.Explosion	a;
	private iu.desktop.game.Explosion	d;


	@Override
	protected void setUp ( ) throws Exception
	{
		super.setUp ( );
	}


	@Override
	protected void tearDown ( ) throws Exception
	{
		super.tearDown ( );
	}


	public void testGetters ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			int[] r = randomXYRDS ( );
			this.a = iu.android.game.Explosion.getNewExplosion (r[0], r[1], r[2], r[3], r[4]);
			this.d = iu.desktop.game.Explosion.getNewExplosion (r[0], r[1], r[2], r[3], r[4]);
			
			assertEquals (this.a.getDuration ( ), this.d.getDuration ( ));
			assertEquals (this.a.getRadius ( ), this.d.getRadius ( ));
			assertEquals (this.a.getStrength ( ), this.d.getStrength ( ));
			assertEquals (this.a.getTimeLeft ( ), this.d.getTimeLeft ( ));
			assertEquals (this.a.getX ( ), this.d.getX ( ));
			assertEquals (this.a.getY ( ), this.d.getY ( ));
			
			this.a.dispose ( );
			this.d.dispose ( );
		}
	}




	public void testIsAffecting ( )
	{
		fail ("Unit not fully implemented"); // TODO implement when Unit is finished
	}


	public void testDisplace ( )
	{
		fail ("Unit not fully implemented"); // TODO implement when Unit is finished
	}


	public void testUpdate ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			int[] r = randomXYRDS ( );
			this.a = iu.android.game.Explosion.getNewExplosion (r[0], r[1], r[2], r[3], r[4]);
			this.d = iu.desktop.game.Explosion.getNewExplosion (r[0], r[1], r[2], r[3], r[4]);
			
			float aOldTimeLeft = this.a.getTimeLeft ( );
			float dOldTimeLeft = this.d.getTimeLeft ( );
			float dt = Math.random ( );
			
			this.a.update (dt);
			this.d.update (dt);
			
			float aNewTimeLeft = this.a.getTimeLeft ( );
			float dNewTimeLeft = this.d.getTimeLeft ( );
			
			assertEquals (aOldTimeLeft, dOldTimeLeft);
			assertEquals (aNewTimeLeft, dNewTimeLeft);
			
			this.a.dispose ( );
			this.d.dispose ( );
		}
	}


	public void testHasFinished ( )
	{
		for (int i = 0; i < Loops; i++)
		{
			int[] r = randomXYRDS ( );
			this.a = iu.android.game.Explosion.getNewExplosion (r[0], r[1], r[2], r[3], r[4]);
			this.d = iu.desktop.game.Explosion.getNewExplosion (r[0], r[1], r[2], r[3], r[4]);
			
			while ( !this.a.hasFinished ( ) || !this.d.hasFinished ( ) ) {
				float dt = Math.random ( );				
				this.a.update (dt);
				this.d.update (dt);
			}
			
			assertTrue (this.a.getTimeLeft ( ) < 0);
			assertTrue (this.d.getTimeLeft ( ) < 0);
			assertEquals (this.a.getTimeLeft ( ), this.d.getTimeLeft ( ));
			assertEquals (this.a.hasFinished ( ), this.d.hasFinished ( ));
			
			this.a.dispose ( );
			this.d.dispose ( );
		}
	}


	private static int[] randomXYRDS ( )
	{
		final int[] xyrds = new int[5];
		xyrds[0] = (int) (Math.random ( ) * 20 - 5);
		xyrds[1] = (int) (Math.random ( ) * 20 - 5);
		xyrds[2] = (int) (Math.random ( ) * 7);
		xyrds[3] = (int) (Math.random ( ) * 5);
		xyrds[4] = (int) (Math.random ( ) * 50);
		return xyrds;
	}
}

