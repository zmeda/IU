package iu.android.unit;

public final class GeneralPhysics
{

	public static float distanceSqr(Vector2d v, Vector2d u)
	{
		if (v == null || u == null)
		{
			return Float.NaN;
		}

		float dx = u.x - v.x;
		float dy = u.y - v.y;
		return (dx * dx + dy * dy);
	}

	public static float distanceSqr(int x1, int y1, final int x2, final int y2)
	{
		x1 -= x2;
		y1 -= y2;
		return (x1 * x1 + y1 * y1);
	}

	public static float distancePointLine(Vector2d point, Vector2d i, Vector2d j)
	{
		float x3 = point.x, y3 = point.y;

		float x1 = i.x, y1 = i.y;
		float x2 = j.x, y2 = j.y;

		float uNum = (x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1);

		float dx = x2 - x1, dy = y2 - y1;
		float uDenom = dx * dx + dy * dy;

		float u = uNum / uDenom;

		float xi = x1 + u * dx;
		float yi = y1 + u * dy;

		dx = xi - x3;
		dy = yi - y3;

		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	public static float distancePointVector(Vector2d point, Vector2d position, float direction)
	{
		Vector2d anotherPosition = new Vector2d(position.x + (float) Math.sin(direction), position.y + (float) Math.cos(direction));
		return GeneralPhysics.distancePointLine(point, position, anotherPosition);
	}

	public static void getPointLineLineIntersection(Vector2d a, Vector2d b, Vector2d c, Vector2d d, Vector2d result)
	{
		float tt = GeneralPhysics.determinant(a.x, a.y, b.x, b.y);
		float tb = GeneralPhysics.determinant(c.x, c.y, d.x, d.y);
		float bottom = GeneralPhysics.determinant(a.x - b.x, a.y - b.y, c.x - d.x, c.y - d.y);

		float xTop = GeneralPhysics.determinant(tt, a.x - b.x, tb, c.x - d.x);
		float yTop = GeneralPhysics.determinant(tt, a.y - b.y, tb, c.y - d.y);

		result.x = xTop / bottom;
		result.y = yTop / bottom;
	}

	public static float determinant(final float a, final float b, final float c, final float d)
	{
		return ((a * d) - (b * c));
	}

	public static boolean isGreaterDistanceToIntersection(Vector2d positionA, float directionA, Vector2d positionB, float directionB)
	{
		//System.out.println("newing here");
		Vector2d futurePositionA = new Vector2d();
		Vector2d futurePositionB = new Vector2d();

		GeneralPhysics.getFuturePosition(positionA, directionA, futurePositionA);
		GeneralPhysics.getFuturePosition(positionB, directionB, futurePositionB);

		Vector2d intersection = new Vector2d();
		GeneralPhysics.getPointLineLineIntersection(positionA, futurePositionA, positionB, futurePositionB, intersection);

		float distA2IntersectionSqr = GeneralPhysics.distanceSqr(positionA, intersection);
		float distB2IntersectionSqr = GeneralPhysics.distanceSqr(positionB, intersection);

		if (distA2IntersectionSqr < distB2IntersectionSqr)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static void getFuturePosition(Vector2d position, float direction, Vector2d futurePosition)
	{
		futurePosition.x = (position.x + (float) Math.sin(direction));
		futurePosition.y = (position.y + (float) Math.cos(direction));
	}

}