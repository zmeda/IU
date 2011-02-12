package iu.android.unit;

import java.util.ArrayList;

public class Squad
{
	private ArrayList<Unit>	units	= new ArrayList<Unit>();
	private int	            id	  = -1;

	public Squad(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return this.id;
	}

	public int getNumUnits()
	{
		return this.units.size();
	}

	public Unit getUnit(int i)
	{
		return this.units.get(i);
	}

	public void addUnit(Unit unit)
	{
		this.units.add(unit);
		unit.squad = this;
	}

	public boolean allDead()
	{
		for (int i = this.units.size() - 1; i >= 0; i--)
		{
			Unit unit = this.units.get(i);
			if (!unit.isDead)
			{
				return false;
			}
		}
		return true;
	}

	public int getNumAlive()
	{
		int n = 0;
		for (int i = this.units.size() - 1; i >= 0; i--)
		{
			Unit unit = this.units.get(i);
			if (!unit.isDead)
			{
				n++;
			}
		}
		return n;
	}

	public int getAverageHealth()
	{
		int h = 0;
		for (int i = this.units.size() - 1; i >= 0; i--)
		{
			Unit unit = this.units.get(i);
			// h += Math.max (0, unit.getHealth ( ));
			h += Math.max(0, unit.health < 0 ? 0 : unit.health);

		}

		return h / this.units.size();
	}

	public ArrayList<Unit> getUnits()
	{
		return this.units;
	}

}