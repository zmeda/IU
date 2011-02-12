package iu.android.unit;

/** An object we will assume never moves. Buildings etc. */
public class FixedBody
{
	final Vector2d	position	= new Vector2d();
	final Vector2d	size	 = new Vector2d();

	// FIXME [] width - remove or give meaning
	// FIXME [] height - remove or give meaning
	public FixedBody(final float x, final float y, final float width, final float height)
	{
		this.position.x = x;
		this.position.y = y;
	}

	// public final Vector2d getPosition ( )
	// {
	// return this.position;
	// }

	// public final Vector2d getSize ( )
	// {
	// // FIXME [] always returns (0,0)
	// return this.size;
	// }
}