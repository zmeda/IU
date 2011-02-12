package iu.android.engine;

import iu.android.battle.BattleEngine;
import iu.android.battle.BattleThread;
import iu.android.explore.Player;
import iu.android.unit.Artillery;
import iu.android.unit.HoverJet;
import iu.android.unit.Squad;
import iu.android.unit.Tank;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;
import iu.android.unit.Weapon;

import java.util.ArrayList;

public abstract class BattlePlayer
{
	private Player					explorePlayer;

	private ArrayList<Unit>		units					= new ArrayList<Unit> ( );
	protected ArrayList<Unit>	deadUnits			= new ArrayList<Unit> ( );
	private ArrayList<Squad>	squads				= new ArrayList<Squad> ( );
	private boolean				isUser				= false;
	private boolean				isParticipating	= false;



	/**
	 * Constructor
	 * 
	 * @param explorePlayer
	 * @param isUser
	 * @param isParticipating
	 */
	public BattlePlayer (Player explorePlayer, boolean isUser, boolean isParticipating)
	{
		// this.id = explorePlayer.get;
		// this.name = name;
		this.explorePlayer = explorePlayer;
		this.isUser = isUser;
		this.isParticipating = isParticipating;
	}


	public byte getID ( )
	{
		//
		// FIXME: The Player ID should be changed to int in the future!
		//
		return ((byte) this.explorePlayer.getId ( ));
	}


	public String getName ( )
	{
		return this.explorePlayer.getName ( );
	}


	// public void setColor(int color)
	// {
	// this.color = color;
	// }

	/**
	 * Returns an argb encoded color, left most byte is alpha, next red, next green, next blue. i.e. 0xFF<FONT
	 * COLOR=red>FF</FONT><FONT COLOR=green>FF</FONT><FONT COLOR=blue>FF</FONT>
	 */
	public int getColor ( )
	{
		return this.explorePlayer.getColor ( );
	}


	public boolean isUser ( )
	{
		return this.isUser;
	}


	public boolean isParticipating ( )
	{
		return this.isParticipating;
	}


	public void addUnit (Unit unit)
	{
		unit.setID (this.units.size ( ));
		this.units.add (unit);
	}


	public int getNumUnits ( )
	{
		return this.units.size ( );
	}
	
	public int[] getUnitsArray ()
	{
		int hNum=0, tNum=0, aNum=0;
		
		ArrayList<Unit> units = this.getUnits ( );
		int unitCount = units.size ( );
		
		for (int j = 0; j < unitCount; j++)
		{
			Unit unit = units.get (j);

			if (!unit.isDead ( ))
			{
				if (unit instanceof HoverJet)
				{
					hNum++;
				}
				else if (unit instanceof Tank)
				{
					tNum++;
				}
				else if (unit instanceof Artillery)
				{
					aNum++;
				}
			}
		}
		
		return new int[]{hNum, tNum, aNum};
	}


	public int getNumUnitsAlive ( )
	{
		return this.units.size ( ) - this.deadUnits.size ( );
	}


	public Unit getUnit (int i)
	{
		return this.units.get (i);
	}


	public void getRecentlyDeadUnits (ArrayList<Unit> dead_units)
	{
		ArrayList<Unit> thisUnits = this.units;
		ArrayList<Unit> thisDeadUnits = this.deadUnits;

		int unitCount = thisUnits.size ( );
		for (int i = 0; i < unitCount; i++)
		{
			Unit unit = thisUnits.get (i);
			if (unit.getHealth ( ) <= 0 && !thisDeadUnits.contains (unit))
			{
				unit.kill ( );
				dead_units.add (unit);
				thisDeadUnits.add (unit);
			}
		}
	}


	/**
	 * One of our units died ... kill the unit
	 * 
	 * @param unit
	 */
	public void killUnit (Unit unit)
	{
		unit.setHealth (0);
	}


	public void integrateUnitBrains (float dt)
	{

		ArrayList<Unit> unitsVec = this.units;

		int numUnits = unitsVec.size ( );

		for (int i = 0; i < numUnits; i++)
		{
			Unit unit = unitsVec.get (i);
			if (!unit.isDead ( ))
			{
				unit.integrateBrain (dt);
			}
		}
	}


	public void integrateUnits (float dt)
	{

		ArrayList<Unit> unitsVec = this.units;

		int numUnits = unitsVec.size ( );

		for (int i = 0; i < numUnits; i++)
		{
			Unit unit = unitsVec.get (i);
			if (!unit.isDead ( ))
			{
				unit.integrate (dt);
			}
		}
	}


	public int getNumSquads ( )
	{
		return this.squads.size ( );
	}


	public Squad getSquad (int i)
	{
		return this.squads.get (i);
	}


	public void addSquad (Squad squad)
	{
		this.squads.add (squad);
	}


	public final void registerWeapons (BattleThread gameThread)
	{

		ArrayList<Unit> unitsVec = this.units;

		int numUnits = unitsVec.size ( );

		for (int i = 0; i < numUnits; i++)
		{
			Unit unit = unitsVec.get (i);
			Weapon[] weapons = unit.getWeapons ( );
			for (int j = 0; j < weapons.length; j++)
			{
				weapons[j].register (gameThread);
			}
		}
	}


	public ArrayList<Unit> getUnits ( )
	{
		return this.units;
	}


	/**
	 * Returns a position on the screen where the units are at the moment
	 * 
	 * @param coord
	 */
	public void calculateAverageUnitPosition (int coord[])
	{
		ArrayList<Unit> allUnits = this.units;

		int unitCount = allUnits.size ( );

		float x = 0;
		float y = 0;

		// Sum all x and y coordinates
		for (int i = 0; i < unitCount; i++)
		{
			Vector2d pos = allUnits.get (i).getPosition ( );
			x += pos.x;
			y += pos.y;
		}

		// Get average
		if (unitCount > 0)
		{
			x /= unitCount;
			y /= unitCount;
		}

		// Return
		coord[0] = (int) x;
		coord[1] = (int) y;
	}


	public int getNumberOfHoverJettUnits ( )
	{
		int hoverNum = 0;

		ArrayList<Unit> units = this.units;

		int unitCount = units.size ( );

		for (int i = 0; i < unitCount; i++)
		{
			Unit unit = units.get (i);

			if (!unit.isDead ( ) && unit instanceof HoverJet)
			{
				hoverNum++;
			}
		}

		return hoverNum;
	}


	public int getNumberOfTankUnits ( )
	{
		int tankNum = 0;

		ArrayList<Unit> units = this.units;

		int unitCount = units.size ( );

		for (int i = 0; i < unitCount; i++)
		{
			Unit unit = units.get (i);

			if (!unit.isDead ( ) && unit instanceof Tank)
			{
				tankNum++;
			}
		}

		return tankNum;
	}


	public int getNumberOfArtilleryUnits ( )
	{
		int artNum = 0;

		ArrayList<Unit> units = this.units;

		int unitCount = units.size ( );

		for (int i = 0; i < unitCount; i++)
		{
			Unit unit = units.get (i);

			if (!unit.isDead ( ) && unit instanceof Artillery)
			{
				artNum++;
			}
		}

		return artNum;
	}


	public abstract void dispatchOrders (BattleEngine game);

}