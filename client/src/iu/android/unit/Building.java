package iu.android.unit;

import android.graphics.Rect;

public class Building
{
	Rect	bounds	= null;

	Rect[]	walls	= null;

	String	name	= null;

	public Building(Rect[] walls, String name)
	{
		this.walls = walls;
		this.name = name;
		this.bounds = this.calcBounds();
	}

	private Rect calcBounds()
	{
		int left = Integer.MAX_VALUE;
		int top = Integer.MAX_VALUE;
		int right = -(Integer.MAX_VALUE - 1);
		int bottom = -(Integer.MAX_VALUE - 1);

		for (int i = 0; i < this.walls.length; i++)
		{
			Rect rect = this.walls[i];
			left = Math.min(left, rect.left);
			top = Math.min(top, rect.top);
			right = Math.max(right, rect.right);
			bottom = Math.max(bottom, rect.bottom);
		}

		return new Rect(left, top, right, bottom);
	}

	// public final Rect getBounds ( )
	// {
	// return this.bounds;
	// }

	// public final Rect[] getWalls ( )
	// {
	// return this.walls;
	// }

	public boolean intersects(Circle circle)
	{
		Rect circlebounds = circle.getBounds();

		if (!Rect.intersects(circlebounds, this.bounds))
		{
			return false;
		}

		for (int i = 0; i < this.walls.length; i++)
		{
			Rect rect = this.walls[i];
			if (Rect.intersects(circlebounds, rect))
			{
				// if ( circle.isColliding( rect ) )
				return true;
			}
		}

		return false;
	}

	/**
	 * Sets <CODE>normal</CODE> to a vector pointing in the correct direction to push the circle <EM>away</EM> from the building (if the circle is intersecting the building.
	 */
	public void calcIntersectionNormal(Circle circle, Vector2d normal)
	{
		Rect circlebounds = circle.getBounds();

		normal.x = Float.NaN;
		normal.y = Float.NaN;

		if (!Rect.intersects(circlebounds, this.bounds))
		{
			return;
		}

		int numIntersections = 0;
		float x = 0, y = 0;

		for (int i = 0; i < this.walls.length; i++)
		{
			Rect rect = this.walls[i];
			if (Rect.intersects(circlebounds, rect) && circle.isColliding(rect))
			{
				circle.calcIntersectionNormal(rect, normal);
				numIntersections++;
				x += normal.x;
				y += normal.y;
				// return;
			}
		}

		if (numIntersections != 0)
		{
			normal.x = x / numIntersections;
			normal.y = y / numIntersections;
			normal.toUnitVector();
		}
	}

	public boolean contains(int x, int y)
	{
		if (!this.bounds.contains(x, y))
		{
			return false;
		}

		for (int i = 0; i < this.walls.length; i++)
		{
			if (this.walls[i].contains(x, y))
			{
				return true;
			}
		}

		return false;
	}

}