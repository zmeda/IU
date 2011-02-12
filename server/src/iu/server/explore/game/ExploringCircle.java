package iu.server.explore.game;

import java.util.LinkedList;

/**
 * Helper class to find tiles in a circle around any tile other tile
 */
public class ExploringCircle
{

	/**
	 * A bit less than sqrt(8) which gives a nice circle:
	 * 
	 * <pre>
	 *  
	 *  ooo
	 * ooooo
	 * ooxoo
	 * ooooo
	 *  ooo
	 * </pre>
	 */
	public static final double	Sqrt8	= 2.8d;

	static class Pair
	{
		int	x, y;


		Pair (int x, int y)
		{
			this.x = x;
			this.y = y;
		}


		@Override
		public boolean equals (Object obj)
		{
			if (obj instanceof Pair)
			{
				Pair other = (Pair) obj;
				return (this.x == other.x) && (this.y == other.y);
			}
			else
			{
				return false;
			}
		}


		@Override
		public String toString ( )
		{
			return "x=" + this.x + "  y=" + this.y;
		}
	}

	private int						r;
	private LinkedList<Pair>	circle;
	private World					world;


	/**
	 * Creates a new utility with the circle of specified radius. <br>
	 * Examples:
	 * <ul>
	 * <li> Radius = 0
	 * 
	 * <pre>
	 * O
	 * </pre>
	 * 
	 * <li> Radius = 1
	 * 
	 * <pre>
	 *  o 
	 * oOo
	 *  o
	 * </pre>
	 * 
	 * <li> Radius = 2.8 ( <code>&lt; sqrt(8)</code> )
	 * 
	 * <pre>
	 *  ooo 
	 * ooooo
	 * ooOoo
	 * ooooo
	 *  ooo
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * @param maxDistSq
	 */
	public ExploringCircle (double radius, World world)
	{

		this.world = world;
		double maxDistSq = radius * radius;
		this.circle = new LinkedList<Pair> ( );

		this.r = (int) radius;
		for (int i = -this.r; i <= this.r; i++)
		{
			for (int j = -this.r; j <= this.r; j++)
			{
				if (distSq (i, j) <= maxDistSq)
				{
					this.circle.add (new Pair (i, j));
				}
			}
		}
	}


	public static ExploredTile findNeighbour (World world, Tile tile, long field, int dx, int dy)
	{
		Tile neighbourTile;
		Pair fieldXY = fieldXY (field);
		fieldXY.x += dx;
		fieldXY.y += dy;

		short i = tile.i;
		short j = tile.j;

		if (fieldXY.x > 7)
		{
			i++;
			fieldXY.x -= 8;
		}
		else if (fieldXY.x < 0)
		{
			i--;
			fieldXY.x += 8;
		}

		if (fieldXY.y > 7)
		{
			j++;
			fieldXY.y -= 8;
		}
		else if (fieldXY.y < 0)
		{
			j--;
			fieldXY.y += 8;
		}

		if (tile.i != i || tile.j != j)
		{
			neighbourTile = world.tileAt (i, j);
		}
		else
		{
			neighbourTile = tile;
		}

		if (neighbourTile == null)
		{
			return null;
		}
		else
		{
			ExploredTile e = new ExploredTile (neighbourTile);
			e.explore ((short) fieldXY.x, (short) fieldXY.y);
			return e;
		}
	}


	public LinkedList<ExploredTile> findCircle (Tile tile, long field)
	{
		LinkedList<ExploredTile> neighbours = new LinkedList<ExploredTile> ( );

		for (Pair p : this.circle)
		{
			ExploredTile e = findNeighbour (this.world, tile, field, p.x, p.y);
			if (e != null)
			{
				neighbours.add (e);
			}
		}

		return neighbours;
	}


	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );

		for (int i = -this.r; i <= this.r; i++)
		{
			for (int j = -this.r; j <= this.r; j++)
			{
				Pair p = new Pair (i, j);
				if (this.circle.contains (p))
				{
					sb.append ("X");
				}
				else
				{
					sb.append (" ");
				}
			}
			sb.append ("\n");
		}

		return sb.toString ( );
	}


	private static int distSq (int dx, int dy)
	{
		return (dx * dx) + (dy * dy);
	}


	static final Pair fieldXY (long field)
	{
		if (field == 0)
			return null;
		
		int index = Tile.fieldIndex (field) - 1;
		int x = index % 8;
		int y = index / 8;
		
		return new Pair (x, y);
	}
}
