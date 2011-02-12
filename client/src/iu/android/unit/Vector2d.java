package iu.android.unit;

/** A class for a 2D vector. */
public final class Vector2d
{

	public float	x;
	public float	y;

	/**
	 * Creates a zero vector.
	 */
	public Vector2d()
	{
		this(0, 0);
	}

	/**
	 * Creates a vector with specified components.
	 * 
	 * @param x
	 * @param y
	 */
	public Vector2d(final float x, final float y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param v
	 */
	public Vector2d(final Vector2d v)
	{
		this(v.x, v.y);
	}

	public void zero()
	{
		this.x = 0;
		this.y = 0;
	}

	// public void setEqualTo (final Vector2d v)
	// {
	// this.x = v.x;
	// this.y = v.y;
	// }

	public void add(final Vector2d v)
	{
		this.x += v.x;
		this.y += v.y;
	}

	/** Add a multiple of v to this vector. */
	public void add(final float scalar, final Vector2d v)
	{
		this.x += (scalar * v.x);
		this.y += (scalar * v.y);
	}

	public void subtract(final Vector2d v)
	{
		this.x -= v.x;
		this.y -= v.y;
	}

	/** Subtract a multiple of v from this vector. */
	// public void subtract (final float scalar, final Vector2d v)
	// {
	// this.x -= (scalar * v.x);
	// this.y -= (scalar * v.y);
	// }
	public void multiply(final float scalar)
	{
		this.x *= scalar;
		this.y *= scalar;
	}

	/**
	 * Set this vector to be perpendicular to what it was. i.e. set x = -y and y = x
	 */
	// public void perpendicular ( )
	// {
	// final float old_x = this.x;
	// this.x = -this.y;
	// this.y = old_x;
	// }
	/** Return the scalar dot product of this vector and v. */
	public float getDotProduct(final Vector2d v)
	{
		final float product = (this.x * v.x) + (this.y * v.y);
		return product;
	}

	/**
	 * Return the perpendicular dot product. The dot product with the perpendicular of this vector and v.
	 */
	public float getPerpDotProduct(final Vector2d v)
	{
		return ((-this.y) * v.x) + (this.x * v.y);
	}

	public float distanceSq(final Vector2d v)
	{
		final float dx = this.x - v.x, dy = this.y - v.y;
		return dx * dx + dy * dy;
	}

	/** Return the length of this vector. * */
	public float getMagnitude()
	{
		// return Math.sqrt (this.getDotProduct (this));
		return (float) Math.sqrt(this.x * this.x + this.y * this.y);
	}

	/**
	 * Sets this vector to have length one. Useful because this allows the easy calculation of the <EM>sine</EM> and <EM>cosine</EM> of an angle from the sides of a triangle,
	 * without needing to calculate the angle (with atan2) itself.
	 */
	public void toUnitVector()
	{
		// this.multiply (1.0 / this.getMagnitude ( ));
		final float f = 1.0f / this.getMagnitude();
		this.x *= f;
		this.y *= f;
	}

	public boolean isZeroVector()
	{
		return ((this.x == 0.0 || this.x == -0) && (this.y == 0.0 || this.y == -0));
	}

	@Override
	public String toString()
	{
		return "[ x = " + this.x + ", y = " + this.y + " ]";
	}

	@Override
	protected void finalize()
	{
		//System.out.println(this.getClass().getName() + " was garbage collected");
	}

}