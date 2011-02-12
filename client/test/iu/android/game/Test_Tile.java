/**
 * 
 */
package iu.android.game;

import junit.framework.TestCase;

/**
 * @author luka
 * 
 */
public class Test_Tile extends TestCase
{

	private static final int		Points[][]	= {{10, 20}, {40, 20}, {100, 300}, {100 + Tile.Size, 300 + Tile.Size}, {-5, -5}};

	private iu.android.game.Tile	a;
	private iu.desktop.game.Tile	d;


	protected void setUp ( ) throws Exception
	{
		super.setUp ( );

		this.a = new iu.android.game.Tile (100, 300, 0, 0);
		this.d = new iu.desktop.game.Tile (100, 300, 0, 0);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown ( ) throws Exception
	{
		super.tearDown ( );
	}


	/**
	 * Test method for {@link iu.android.game.Tile#getIndex1()}.
	 */
	public void testGetIndex1 ( )
	{
		assertEquals (this.a.getIndex1 ( ), this.d.getIndex1 ( ));
	}


	/**
	 * Test method for {@link iu.android.game.Tile#getIndex2()}.
	 */
	public void testGetIndex2 ( )
	{
		assertEquals (this.a.getIndex2 ( ), this.d.getIndex2 ( ));
	}


	/**
	 * Test method for {@link iu.android.game.Tile#getTypeAsString()}.
	 */
	public void testGetTypeAsString ( )
	{
		assertEquals (this.a.getTypeAsString ( ), this.d.getTypeAsString ( ));
	}


	/**
	 * Test method for {@link iu.android.game.Tile#getFrictionCoefficient()}.
	 */
	public void testGetFrictionCoefficient ( )
	{
		assertEquals (this.a.getFrictionCoefficient ( ), this.d.getFrictionCoefficient ( ));
	}


	/**
	 * Test method for {@link iu.android.game.Tile#getType()}.
	 */
	public void testGetType ( )
	{
		assertEquals (this.a.getType ( ), this.d.getType ( ));
	}


	/**
	 * Test method for {@link iu.android.game.Tile#getHeight()}.
	 */
	public void testGetHeight ( )
	{
		assertEquals (this.a.getHeight ( ), this.d.getHeight ( ));
	}


	/**
	 * Test method for {@link iu.android.game.Tile#contains(int, int)}.
	 */
	public void testContains ( )
	{
		boolean aContains, dContains;
		
		for (int[] p : Points) {
			aContains = this.a.contains (p[0], p[1]);
			dContains = this.d.contains (p[0], p[1]);
			assertEquals (aContains, dContains);
		}
	}


	/**
	 * Test method for {@link iu.android.game.Tile#getX()}.
	 */
	public void testGetX ( )
	{
		assertEquals (this.a.getX ( ), this.d.getX ( ));
	}


	/**
	 * Test method for {@link iu.android.game.Tile#getY()}.
	 */
	public void testGetY ( )
	{
		assertEquals (this.a.getY ( ), this.d.getY ( ));
	}

}
