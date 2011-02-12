package iu.android.map;

import iu.android.engine.Explosion;
import iu.android.unit.Building;
import iu.android.unit.Circle;
import iu.android.unit.Projectile;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

import java.util.ArrayList;

import android.graphics.Rect;


/**
 * This class represents the battle field background
 * 
 * @author luka
 *
 */
public class Map
{
	private static final int		UNIT_INC							= 100;
	Unit[]								allUnitsOnX						= new Unit[Map.UNIT_INC];
	Unit[]								allUnitsOnY						= new Unit[Map.UNIT_INC];
	int									allUnitsCount					= 0;

	private ArrayList<Building>	buildings						= new ArrayList<Building> ( );
	private ArrayList<Explosion>	explosions						= new ArrayList<Explosion> ( );
	private ArrayList<Projectile>	projectiles						= new ArrayList<Projectile> ( );

	Tile[][]								tiles								= null;
	int									mapSize;
	float									waterLevel						= -0.98f;
	float									slope								= -0.91f;

	int									collisionCountX				= 1;
	int									collisionCountY				= 1;
	boolean								lastUsedCollisionX			= true;
	int									framesSinceSwitchCollision	= 0;


	/**
	 * Creates a new map 
	 * 
	 * @param seed
	 * @param map_size_pow
	 */
	private void makeMap (final long seed, final int map_size_pow)
	{
		this.mapSize = 1 << map_size_pow;

		this.tiles = new Tile[this.mapSize][this.mapSize];

		MapGenerator generator = new MapGenerator ( );
		float[][] height_map = generator.createHeightMap(seed, this.mapSize);
		int[][] types = generator.createTileTypes (height_map, this.waterLevel, this.slope);

		Tile[][] thisTiles = this.tiles;

		for (int i = 0; i < this.mapSize; i++)
		{
			Tile[] thisTilesI = thisTiles[i];

			for (int j = 0; j < this.mapSize; j++)
			{
				thisTilesI[j] = new Tile (i << Tile.Size_Power, j << Tile.Size_Power, types[i][j], height_map[i][j] < 0.1 ? 0 : 1);
			}
		}
	}
	

	/**
	 * Constructor
	 */
	public Map (final long seed, final int map_size_pow)
	{
		this.makeMap (seed, map_size_pow);
	}




	public int pixelCount ( )
	{
		return this.mapSize << Tile.Size_Power;
	}


	/** Get the Tile in the map at this (cartesian) coordinate. * */
	public Tile getTileAt (final int x, final int y)
	{
		int i = x >> Tile.Size_Power;
		int j = y >> Tile.Size_Power;

				
		return this.tiles[i][j];
	}


	public Tile getTile (final int i, final int j)
	{
		return this.tiles[i][j];
	}


	public Unit getUnitAt (final int x, final int y)
	{
		Unit[] thisUnits = this.allUnitsOnX;
		int unitCount = this.allUnitsCount;

		for (int i = 0; i < unitCount; i++)
		{
			final Unit unit = thisUnits[i];
			final Circle circle = unit.getBounds ( );

			if (circle.contains (x, y))
			{
				return unit;
			}
		}
		return null;
	}


	public void addBuilding (final Building building)
	{
		this.buildings.add (building);
	}


	public int getNumBuildings ( )
	{
		return this.buildings.size ( );
	}


	public Building getBuilding (final int i)
	{
		return this.buildings.get (i);
	}

	private Vector2d	normal	= new Vector2d ( );


	public void checkBuildingsCollisions ( )
	{
		final Vector2d nor = this.normal;
		final float norX = nor.x;
		final float norY = nor.y;

		Unit[] allUnits = this.allUnitsOnX;
		int areaUnitsCount = this.allUnitsCount;

		for (int k = 0; k < areaUnitsCount; k++)
		{
			// Unit unit = area.getUnit (k);
			Unit unit = allUnits[k];

			int numBuildings = this.getNumBuildings ( );

			for (int m = 0; m < numBuildings; m++)
			{
				Building building = this.getBuilding (m);
				if (building.intersects (unit.getBounds ( )))
				{
					// System.out.println( "hit building" );
					building.calcIntersectionNormal (unit.getBounds ( ), nor);
					if (!Double.isNaN (norX) && !Double.isNaN (norY))
					{
						Vector2d vel = unit.getVelocity ( );
						float dotProduct = vel.getDotProduct (nor);
						if (dotProduct < 0.0)
						{
							vel.multiply (0.0f);
							unit.applyForce (200 * norX, 200 * norY);
						}
						vel.multiply (0.05f);
						// game.physics.Vector2d vel = unit.getVelocity();
						// float dotProduct = vel.getDotProduct( normal );
						// if ( dotProduct > 0.5 )
						// vel.multiply( 0.05 );
					}

				}
			}
		}
	}


	// public void clearDeadObjects()
	// {
	// for (int i = 0; i < this.areas.length; i++)
	// {
	// for (int j = 0; j < this.areas[i].length; j++)
	// {
	// Area area = this.areas[i][j];
	// area.clearDeadObjects();
	// }
	// }
	// }

	// public void addPlayerUnits(Player player)
	// {
	// int numUnits = player.getNumUnits();
	// for (int i = 0; i < numUnits; i++)
	// {
	// Unit unit = player.getUnit(i);
	// if (!unit.isDead())
	// {
	// Vector2d pos = unit.getPosition();
	// Area area = this.getAreaAt((int) pos.x, (int) pos.y);
	// // area.addUnit (unit);
	// area.units.addElement(unit);
	// unit.clearNeighbourhood();
	// }
	// }
	// }

	public void addProjectile (final Projectile projectile)
	{
		this.projectiles.add (projectile);
	}


	public void integrateProjectiles (final float dt)
	{
		ArrayList<Projectile> projectilesVec = this.projectiles;

		for (int i = projectilesVec.size ( ) - 1; i >= 0; i--)
		{
			Projectile projectile = projectilesVec.get (i);
			projectile.update (dt, this);
			if (projectile.getTimeLeft ( ) <= 0.0)
			{
				projectilesVec.remove (i);
			}
		}
	}


	// public void paintProjectiles(Canvas canvas)
	// {
	// ArrayList<Projectile> areaProjectiles = this.projectiles;
	// ArrayList<Sprite> rendererSprites = this.;
	//
	// // for (int i = area.getNumProjectiles ( ) - 1; i >= 0; i--)
	// for (int i = areaProjectiles.size() - 1; i >= 0; i--)
	// {
	// // Projectile projectile = area.getProjectile (i);
	// Projectile projectile = areaProjectiles.elementAt(i);
	//
	// ProjectileSprite projectileSprite = ProjectileSprite.newProjectileSprite();
	// projectileSprite.init(projectile);
	// projectileSprite.paint(canvas);
	// rendererSprites.addElement(projectileSprite);
	// }
	// }

	public void addExplosion (final Explosion explosion)
	{
		//
		// Add the explosion so that they are all sorted in the vector
		//

		ArrayList<Explosion> thisExplosions = this.explosions;
		int explosionCount = thisExplosions.size ( );

		// Find the first explosion that is more right than the explosion we are inserting
		float posX = explosion.getX ( );
		int idx = 0;

		while (idx < explosionCount && thisExplosions.get (idx).getX ( ) < posX)
		{
			idx++;
		}
		this.explosions.add (idx, explosion);
		// this.explosions.insertElementAt(explosion, idx);
	}


	public void checkAndUpdateExplosions (final float dt)
	{
		ArrayList<Explosion> explosionVec = this.explosions;
		int explosionCount = explosionVec.size ( );

		if (explosionCount == 0)
		{
			return;
		}

		Explosion explosion;

		// End the explosions
		for (int i = explosionCount - 1; i >= 0; i--)
		{
			explosion = explosionVec.get (i);
			if (explosion.hasFinished ( ))
			{
				explosionVec.remove (i);
			}
			else
			{
				explosion.update (dt);
			}
		}

		explosionCount = explosionVec.size ( );

		Unit[] allUnits = this.allUnitsOnX;
		int unitCount = this.allUnitsCount;

		// Index of unit where to start looking for units affected by an explosion
		int startIdx = 0;

		boolean unitLeftOf, unitRightOf;
		float expX, expRad, expLeft, expRight;
		Circle unitBounds;
		Unit unit;

		// For each explosion check the units that are close enough
		for (int i = 0; i < explosionCount; i++)
		{
			explosion = explosionVec.get (i);

			// Left and right side of explosion
			expX = explosion.getX ( );
			expRad = explosion.getRadius ( );
			expLeft = expX - expRad;
			expRight = expX + expRad;

			unitLeftOf = true;

			// Move index left because the last explosion checked could have been smaller than this one
			while (unitLeftOf && startIdx > 0)
			{
				unit = allUnits[startIdx - 1];
				unitBounds = unit.getBounds ( );

				// Is unit left of explosion
				unitLeftOf = (unitBounds.x + unitBounds.radius) < expLeft;

				startIdx -= (unitLeftOf ? 1 : 0);
			}

			unitRightOf = false;

			// While unit in sorted array not right of explosion look for explosion effects
			for (int j = startIdx; !unitRightOf && j < unitCount; j++)
			{
				unit = allUnits[j];
				unitBounds = unit.getBounds ( );

				// Is unit left of explosion
				unitLeftOf = (unitBounds.x + unitBounds.radius) < expLeft;

				// Is unit right of explosion
				unitRightOf = !unitLeftOf && (expRight < unitBounds.x - unitBounds.radius);

				// If not left or right then test for explosion effect
				if (!(unitLeftOf || unitRightOf) && explosion.isAffecting (unit))
				{
					explosion.displace (unit);
				}

				// If unit left of explosion then no other explosion can affect it
				if (unitLeftOf)
				{
					startIdx++;
				}
			}
		}
	}


	/**
	 * Returns indexes for all the tiles contained in the clipped area.-
	 * 
	 * @param indexRect
	 * @param clipArea
	 */
	public void getClippedTiles (Rect indexRect, final Rect clipArea)
	{
		indexRect.left = clipArea.left / (Tile.Size);
		indexRect.top = clipArea.top / (Tile.Size);

		indexRect.right = clipArea.right / (Tile.Size);
		indexRect.bottom = clipArea.bottom / (Tile.Size);
	}


	public ArrayList<Explosion> getExplosions ( )
	{
		return this.explosions;
	}


	//
	//
	// COLLISION DETECTION
	//
	//

	/**
	 * Optimized collision handling (No areas used just a sorted array of all units).
	 */
	public void checkCollisions ( )
	{
		//
		// Decide which sorted array to use X or Y
		//
		boolean useX = (this.collisionCountX < this.collisionCountY);

		// Criteria (lastX = 500 checks and lastY = 100 checks then wait 500/100 = 5 frames * K before switching to X, K=20)
		int switchCriteriaFactor;

		if (useX)
		{
			switchCriteriaFactor = 20 * this.collisionCountY
					/ (this.collisionCountX > 0 ? this.collisionCountX : 100);
		}
		else
		{
			switchCriteriaFactor = 20 * this.collisionCountX
					/ (this.collisionCountY > 0 ? this.collisionCountY : 100);
		}

		// If using same coordinate for a long time then try switching to other one
		useX ^= (useX == this.lastUsedCollisionX && this.framesSinceSwitchCollision >= switchCriteriaFactor);

		//
		// Do collision detection
		//
		int numOfChecks;

		if (useX)
		{
			numOfChecks = this.checkCollisionsUsingX ( );

			this.collisionCountX = numOfChecks;
		}
		else
		{
			numOfChecks = this.checkCollisionsUsingY ( );

			this.collisionCountY = numOfChecks;
		}

		// If using same collision again then increment count otherwise set to zero
		if (this.lastUsedCollisionX == useX)
		{
			this.framesSinceSwitchCollision++;
		}
		else
		{
			this.framesSinceSwitchCollision = 1;
		}

		this.lastUsedCollisionX = useX;
	}


	/**
	 * Checks for collision using the sorted array of units according to position.x parameter
	 */

	public int checkCollisionsUsingX ( )
	{
		int checkCount = 0;

		// Sorts all of the units in the allUnitsOnX according to their X position
		this.sortCollisionUnitsOnX ( );

		Unit[] units = this.allUnitsOnX;
		int unitCount = this.allUnitsCount;

		boolean closeEnough;

		for (int i = 0; i < unitCount; i++)
		{
			final Unit uniti = units[i];

			final Circle boundsi = uniti.getBounds ( );

			final float unitJmaxPosX = uniti.getPosition ( ).x + boundsi.radius;

			closeEnough = true;

			for (int j = i + 1; closeEnough && j < unitCount; j++)
			{
				final Unit unitj = units[j];
				final Circle boundsj = unitj.getBounds ( );

				// If possible collision according to the unit distance on X coordinate
				closeEnough = (unitj.getPosition ( ).x - boundsj.radius < unitJmaxPosX);

				if (closeEnough && boundsi.isColliding (boundsj))
				{
					// collisions code here
					uniti.doCollision (unitj);
					uniti.collide (unitj);
					unitj.collide (uniti);
				}

				// Counting exact checks made for collision detection
				checkCount += (closeEnough ? 1 : 0);
			}
		}

		return checkCount;
	}


	/**
	 * Chacks for collision using the sorted array of units according to position.y parameter
	 */

	public int checkCollisionsUsingY ( )
	{
		int checkCount = 0;

		// Sorts all of the units in the allUnitsOnX according to their X position
		this.sortCollisionUnitsOnY ( );

		Unit[] units = this.allUnitsOnY;
		int unitCount = this.allUnitsCount;

		boolean closeEnough;

		for (int i = 0; i < unitCount; i++)
		{
			final Unit uniti = units[i];

			final Circle boundsi = uniti.getBounds ( );

			final float unitJmaxPosY = uniti.getPosition ( ).y + boundsi.radius;

			closeEnough = true;

			for (int j = i + 1; closeEnough && j < unitCount; j++)
			{
				final Unit unitj = units[j];
				final Circle boundsj = unitj.getBounds ( );

				// If possible collision according to the unit distance on X coordinate
				closeEnough = (unitj.getPosition ( ).y - boundsj.radius < unitJmaxPosY);

				if (closeEnough && boundsi.isColliding (boundsj))
				{
					// collisions code here
					uniti.doCollision (unitj);
					uniti.collide (unitj);
					unitj.collide (uniti);
				}

				// Counting exact checks made for collision detection
				checkCount += (closeEnough ? 1 : 0);
			}
		}

		return checkCount;
	}


	/**
	 * Sorts the units by their position.x values - this in used for collision detection
	 */
	public void sortCollisionUnitsOnX ( )
	{
		Unit[] allUnits = this.allUnitsOnX;
		int unitCount1 = this.allUnitsCount - 1;

		int i = 0;
		int j = 1;
		while (i < unitCount1)
		{
			if (allUnits[i].getPosition ( ).x <= allUnits[i + 1].getPosition ( ).x)
			{
				i = j++;
			}
			else
			{
				Unit tmp = allUnits[i];
				allUnits[i] = allUnits[i + 1];
				allUnits[i + 1] = tmp;

				// Set indexes of units position in array
				allUnits[i].sortedIndexX = i;
				allUnits[i + 1].sortedIndexX = i + 1;

				i = (i == 0 ? j++ : i - 1);
			}
		}
	}


	/**
	 * Sorts the units by their position.y values - this used for collision detection
	 */
	public void sortCollisionUnitsOnY ( )
	{
		Unit[] allUnits = this.allUnitsOnY;
		int unitCount1 = this.allUnitsCount - 1;

		int i = 0;
		int j = 1;
		while (i < unitCount1)
		{
			if (allUnits[i].getPosition ( ).y <= allUnits[i + 1].getPosition ( ).y)
			{
				i = j++;
			}
			else
			{
				Unit tmp = allUnits[i];
				allUnits[i] = allUnits[i + 1];
				allUnits[i + 1] = tmp;

				// Set indexes of units position in array
				allUnits[i].sortedIndexY = i;
				allUnits[i + 1].sortedIndexY = i + 1;

				i = (i == 0 ? j++ : i - 1);
			}
		}
	}


	/**
	 * @return Array of units ordered ascending by their x positions
	 */

	public Unit[] getAllUnitsOnX ( )
	{
		return this.allUnitsOnX;
	}


	/**
	 * @return Array of units ordered ascending by their y positions
	 */

	public Unit[] getAllUnitsOnY ( )
	{
		return this.allUnitsOnY;
	}


	/**
	 * @return Number of units in play
	 */

	public int getAllUnitsCount ( )
	{
		return this.allUnitsCount;
	}


	/**
	 * Adds units of a new army
	 * 
	 * @param units
	 */

	public void addUnits (final ArrayList<Unit> units)
	{
		Unit[] newUnitArray = units.toArray (new Unit[0]);
		int newUnitCount = units.size ( );

		int oldUnitCount = this.allUnitsCount;

		// Copy old units into new longer array if needed
		if (oldUnitCount + newUnitCount > this.allUnitsOnX.length)
		{
			int len = oldUnitCount + newUnitCount;

			len = (len / Map.UNIT_INC + 1) * Map.UNIT_INC;

			Unit[] oldX = this.allUnitsOnX;
			Unit[] oldY = this.allUnitsOnY;

			this.allUnitsOnX = new Unit[len];
			this.allUnitsOnY = new Unit[len];

			System.arraycopy (oldX, 0, this.allUnitsOnX, 0, oldUnitCount);
			System.arraycopy (oldY, 0, this.allUnitsOnY, 0, oldUnitCount);
		}

		System.arraycopy (newUnitArray, 0, this.allUnitsOnX, oldUnitCount, newUnitCount);
		System.arraycopy (newUnitArray, 0, this.allUnitsOnY, oldUnitCount, newUnitCount);

		this.allUnitsCount = oldUnitCount + newUnitCount;
	}


	public void addUnits2 (final ArrayList<Unit> units)
	{
		if (this.allUnitsCount == 0)
		{
			this.allUnitsOnX = new Unit[this.allUnitsCount];
			this.allUnitsOnY = new Unit[this.allUnitsCount];
		}

		Unit[] newUnitArray = units.toArray (new Unit[0]);
		int newUnitCount = units.size ( );

		Unit[] oldX = this.allUnitsOnX;
		Unit[] oldY = this.allUnitsOnY;
		int oldUnitCount = this.allUnitsCount;

		this.allUnitsOnX = new Unit[oldUnitCount + newUnitCount];
		this.allUnitsOnY = new Unit[oldUnitCount + newUnitCount];

		// Copy old to new
		System.arraycopy (oldX, 0, this.allUnitsOnX, 0, oldUnitCount);
		// Add new
		System.arraycopy (newUnitArray, 0, this.allUnitsOnX, oldUnitCount, newUnitCount);

		// Copy old to new
		System.arraycopy (oldY, 0, this.allUnitsOnY, 0, oldUnitCount);
		// Add new
		System.arraycopy (newUnitArray, 0, this.allUnitsOnY, oldUnitCount, newUnitCount);

		for (int i = 0; i < newUnitCount; i++)
		{
			this.allUnitsOnX[i].sortedIndexX = i;
			this.allUnitsOnY[i].sortedIndexY = i;
		}

		this.allUnitsCount = oldUnitCount + newUnitCount;
	}


	/**
	 * Removes dead units from collision detection
	 * 
	 * @param deadUnitVec -
	 *           Array of dead units
	 */

	public void removeUnitsFromCollision (final ArrayList<Unit> deadUnitVec)
	{
		if (deadUnitVec.size ( ) == 0)
		{
			return;
		}

		int idx;
		Unit[] allUnitsOnXLocal = this.allUnitsOnX;
		Unit[] allUnitsOnYLocal = this.allUnitsOnY;
		int thisAllUnitsCount = this.getAllUnitsCount ( );
		Unit unit;

		//
		// Move units in array to the left to fill empty spaces that were made by dead units
		//

		// For X sorted array
		idx = 0;
		for (int i = 0; i < thisAllUnitsCount; i++)
		{
			unit = allUnitsOnXLocal[i];

			if (!deadUnitVec.contains (unit))
			{
				unit.sortedIndexX = idx;
				allUnitsOnXLocal[idx++] = unit;
			}
			else
			{
				unit.sortedIndexX = -1;
				unit.sortedIndexY = -1;
			}
		}

		// For Y sorted array
		idx = 0;
		for (int i = 0; i < thisAllUnitsCount; i++)
		{
			unit = allUnitsOnYLocal[i];

			if (!deadUnitVec.contains (unit))
			{
				unit.sortedIndexY = idx;
				allUnitsOnYLocal[idx++] = unit;
			}
		}

		// Set trailing elements of array to null
		for (int i = idx; i < thisAllUnitsCount; i++)
		{
			allUnitsOnXLocal[i] = null;
			allUnitsOnYLocal[i] = null;
		}

		// New number of units
		this.allUnitsCount -= deadUnitVec.size ( );
	}


	public ArrayList<Building> getBuildings ( )
	{
		return this.buildings;
	}


	public ArrayList<Projectile> getProjectiles ( )
	{
		return this.projectiles;
	}


	public int getMapSize ( )
	{
		return this.mapSize;
	}


	public Tile[][] getTiles ( )
	{
		return this.tiles;
	}
}
