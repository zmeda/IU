package iu.android.unit;

import android.graphics.Rect;

public final class Circle
{
	public float	   x;
	public float	   y;
	public float	   radius;
	public float	   radiusSqr;

	private final Rect	bounds	= new Rect();

	public Circle(final float x, final float y, final float radius)
	{
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.radiusSqr = radius * radius;
	}

	/** Returns true if this point is within the radius of this circle. */
	public boolean contains(final float px, final float py)
	{
		final float dx = this.x - px;
		final float dy = this.y - py;

		// return dx * dx + dy * dy <= this.radius * this.radius;
		return dx * dx + dy * dy <= this.radiusSqr;
	}

	/** Returns true if this circle and the other circle overlap. */
	public boolean isColliding(final Circle c)
	{
		// return this.isColliding (c.x, c.y, c.radius);

		final float dx = c.x - this.x;
		final float dy = c.y - this.y;
		final float distSq = dx * dx + dy * dy;
		final float totalDist = c.radius + this.radius;
		return distSq < totalDist * totalDist;
	}

	public boolean isColliding(final float cx, final float cy, final float cradius)
	{
		final float dx = cx - this.x;
		final float dy = cy - this.y;
		final float distSq = dx * dx + dy * dy;
		final float totalDist = cradius + this.radius;
		return distSq < totalDist * totalDist;
	}

	public Rect getBounds()
	{
		final int rr = (int) (2 * this.radius);

		this.bounds.left = (int) (this.x - this.radius);
		this.bounds.top = (int) (this.y - this.radius);
		this.bounds.right = this.bounds.left + rr;
		this.bounds.bottom = this.bounds.top + rr;

		return this.bounds;
	}

	public boolean isColliding(final Rect rect)
	{

		int numXintersections = 0;
		if ((rect.left - this.x) < this.radius)
		{
			numXintersections++;
		}
		if ((this.x - (rect.right)) < this.radius)
		{
			numXintersections++;
		}

		int numYintersections = 0;
		if ((rect.top - this.y) < this.radius)
		{
			numYintersections++;
		}
		if ((this.y - (rect.bottom)) < this.radius)
		{
			numYintersections++;
		}

		if (numXintersections == 1 && numYintersections == 1)
		{
			return true; // intersecting near corner
		}

		if (numXintersections > 0)
		{
			// must be between the y edges
			if (rect.top <= this.y && this.y <= (rect.bottom))
			{
				return true;
			}
		}

		if (numYintersections > 0)
		{
			// must be between the x edges
			if (rect.left <= this.x && this.x <= (rect.right))
			{
				return true;
			}
		}

		// hopefully that should cover most cases
		return false;
	}

	public void calcIntersectionNormal(final Rect rect, final Vector2d normal)
	{
		normal.x = normal.y = Float.NaN;

		int xdir = 0;
		if (this.x <= rect.left)
		{
			xdir--; // points left
		}
		else if ((rect.right) <= this.x)
		{
			xdir++; // points right
		}

		int ydir = 0;
		if (this.y <= rect.top)
		{
			ydir--; // points up
		}
		else if ((rect.bottom) <= this.y)
		{
			ydir++; // points down
		}

		if (xdir != 0 || ydir != 0)
		{
			normal.x = xdir;
			normal.y = ydir;
			normal.toUnitVector();
		}

	}

	/**
	 * Solves the quadratic (<CODE>ax<SUP>2</SUP> + bx + c = 0</CODE>) using the standard formula. Results are returned by setting the x <STRONG>and</STRONG> y, of root1 and
	 * root2, to be values of the two roots.
	 */
	public static void solveQuadratic(final float a, float b, final float c, final Vector2d root1, final Vector2d root2)
	{
		final float rootbSquaredMinus4ac = (float) Math.sqrt(b * b - 4 * a * c);
		root1.x = root1.y = (-b + rootbSquaredMinus4ac) / (2 * a);
		root2.x = root2.y = (-b - rootbSquaredMinus4ac) / (2 * a);
	}

	/*
	 * 
	 * [] commented out This must have something to do with the 'Cheat for now' on line 180
	 * 
	 * private void calculatePointsOfIntersectionDxZero (final Circle c, final Vector2d v1, final Vector2d v2) { }
	 * 
	 * 
	 * private void calculatePointsOfIntersectionDyZero (final Circle c, final Vector2d v1, final Vector2d v2) { }
	 * 
	 */

	public void calculatePointsOfIntersection(final Circle circ, final Vector2d v1, final Vector2d v2)
	{
		final float dx = this.x - circ.x, dy = this.y - circ.y;
		// cheat for now
		if (dx == 0.0)
		{
			// calculatePointsOfIntersectionDxZero( circ, v1, v2 );
			this.x += Double.MIN_VALUE;
		}

		if (dy == 0.0)
		{
			// calculatePointsOfIntersectionDyZero( circ, v1, v2 );
			this.y += Double.MIN_VALUE;
		}

		final float m = (this.x - circ.x) / (circ.y - this.y);
		final float cl = (circ.x * circ.x - this.x * this.x + circ.y * circ.y - this.y * this.y - circ.radius * circ.radius + this.radius * this.radius) / (2 * (circ.y - this.y));

		// now work out the coefficients of the quadratic (ax^2 + bx +c = 0)
		final float a = 1 + m * m;
		final float b = 2 * (m * cl - this.x - m * this.y);
		final float c = cl * cl - 2 * cl * this.y + this.y * this.y + this.x * this.x - this.radius * this.radius;

		Circle.solveQuadratic(a, b, c, v1, v2);
		v1.y = m * v1.x + cl;
		v2.y = m * v2.x + cl;

	}

	@Override
	public String toString()
	{
		return "Circle S(" + this.x + "," + this.y + ") r=" + this.radius;
	}
}