package iu.android.battle;

/*************************************************************************************************************
 * IU 1.0b, a java realtime strategy game
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 ************************************************************************************************************/

import iu.android.control.InputDevice;
import iu.android.engine.BattlePlayer;
import iu.android.engine.Explosion;
import iu.android.graph.Renderer;
import iu.android.map.Map;
import iu.android.unit.Circle;
import iu.android.unit.Projectile;
import iu.android.unit.Unit;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.view.SurfaceHolder;


/**
 * This thread runs the battle logics 15 times a second.-
 * 
 * @author Lucas
 * 
 */
public class BattleThread extends Thread
{
	//
	// The amount of frames per second (or logics tics) of this game engine
	//
	public final static int		FPS						= 15;

	//
	// The delay (in milliseconds) between frames (or tics) in one second
	//
	public final static int		DELAY_PER_FRAME		= 1000 / BattleThread.FPS;

	private volatile boolean	running					= false;

	private Renderer				renderer					= null;
	private InputDevice			inputDevice				= null;
	private Map						map						= null;
	private BattleEngine			gameEngine				= null;
	private SurfaceHolder		battleFieldSurface	= null;
	private ArrayList<Unit>		deadUnits				= new ArrayList<Unit> ( );


	/**
	 * Register the weapons for all players.-
	 */
	private void registerWeapons ( )
	{
		BattleEngine thisGame = this.gameEngine;

		int numPlayers = thisGame.getNumPlayers ( );

		for (int i = 0; i < numPlayers; i++)
		{
			BattlePlayer player = thisGame.getPlayer (i);

			if (player != null)
			{
				player.registerWeapons (this);
			}
		}
	}
	


	/**
	 * One cycle in this battle logic.-
	 * 
	 * @param dt
	 */
	private void integrate (final float dt)
	{
		//
		// Local variables to all used pointer for speed up
		//
		Map thisMap 						= this.map;
		BattleEngine thisGame 			= this.gameEngine;
		ArrayList<Unit> thisDeadUnits	= this.deadUnits;
		int playerCount 					= thisGame.getNumPlayers ( );
		
		//
		// Check if someone is left without units
		//
		thisGame.checkForWinners ( );

		if (thisGame.hasBeenWon ( ))
		{
			try
			{
				BattleThread.sleep (2000);
			}
			catch (InterruptedException e)
			{
				//
				// We were interrupted while waiting ... no problem!
				//
			}
			finally
			{
				this.quit ( );
				this.gameEngine.endGame ( );
			}
		}

		//
		// Dead units
		//
		thisDeadUnits.clear ( );

		//
		// Get dead units
		//
		int num_players = thisGame.getNumPlayers ( );
		for (int i = 0; i < num_players; i++)
		{
			BattlePlayer player = thisGame.getPlayer (i);
			//
			// Check if we received a valid reference
			//
			if (player != null)
			{
				player.getRecentlyDeadUnits (thisDeadUnits);
			}
		}

		//
		// Read keyboard or touch input events
		//
		this.inputDevice.doInput ( );

		//
		// AI Brains
		//
		playerCount = thisGame.getNumPlayers ( );
		for (int i = 0; i < playerCount; i++)
		{
			BattlePlayer player = thisGame.getPlayer (i);
			//
			// Check if we received a valid reference
			//
			if (player != null && player.getNumUnitsAlive ( ) > 0)
			{
				player.integrateUnitBrains (dt);
			}
		}
		
		//
		// Collision detection
		//
		thisMap.removeUnitsFromCollision (thisDeadUnits);
		thisMap.checkCollisions 			( );

		//
		// Move units around
		//
		playerCount = thisGame.getNumPlayers ( );
		for (int i = 0; i < playerCount; i++)
		{
			BattlePlayer player = thisGame.getPlayer (i);
			//
			// Check if we received a valid reference
			//
			if (player != null)
			{
				player.integrateUnits (dt);

				player.dispatchOrders (thisGame);
			}
		}

		//
		// Move projectiles
		//
		thisMap.integrateProjectiles (dt);
		
		//
		// Explosion update
		//
		thisMap.checkAndUpdateExplosions (dt);

		//
		// Make explosions
		//
		int deadUnitsNum = thisDeadUnits.size ( );
		for (int i = 0; i < deadUnitsNum; i++)
		{
			Unit unit = thisDeadUnits.get (i);
			Circle bounds = unit.getBounds ( );
			Explosion explosion = Explosion.getNewExplosion ((int) bounds.x, (int) bounds.y, (int) (2 * bounds.radius), 1.0f, 2);
			thisMap.addExplosion (explosion);
		}
	}
	

	
	/**
	 * Constructor
	 * 
	 * @param game
	 */
	public BattleThread (final BattleEngine game)
	{
		this.gameEngine = game;
	}


	/**
	 * Sets the input device this thread will read events from (keyboard, touch, mouse, ...).-
	 * 
	 * @param inputDevice
	 */
	public void setInputDevice (final InputDevice inputDevice)
	{
		this.inputDevice = inputDevice;
	}


	/**
	 * Sets the canvas this battle thread will draw on.-
	 * 
	 * @param renderer 
	 */
	public void setRenderer (final Renderer renderer)
	{
		this.renderer = renderer;
		this.battleFieldSurface = renderer.getSurfaceHolder ( );
	}


	public void addProjectile (final Projectile projectile)
	{
		this.map.addProjectile (projectile);
	}


	
	@Override
	public void run ( )
	{
		//
		// Delta time for each game loop
		//
		final float dt = 0.15f;
		
		//
		// Register all the weapons in the engine
		//
		this.registerWeapons ( );

		//
		// The main loop for the battle engine
		//
		while (this.running)
		{
			long startTime = System.currentTimeMillis ( );

			//
			// Game engine integration
			//
			this.integrate (dt);

			//
			// Check if we still have time to draw a frame in the animation
			//
			long spentTime = System.currentTimeMillis ( ) - startTime;
			
			if (spentTime < BattleThread.DELAY_PER_FRAME)
			{
				//
				// We still have time ... let's draw a frame
				//
				Canvas canvas = null;
 
				try
				{
					canvas = this.battleFieldSurface.lockCanvas (null);
					
					synchronized (this.battleFieldSurface)
					{
						this.renderer.requestRender (canvas);
					}
				}
				finally
				{
					//
					// Do this in a finally block so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					//
					if (canvas != null)
					{
						this.battleFieldSurface.unlockCanvasAndPost (canvas);
					}
				}
			}
			
			//
			// Check if we still have some time left to release the processor ...
			//
			spentTime = System.currentTimeMillis ( ) - startTime;
			
			try
			{
				BattleThread.sleep (BattleThread.DELAY_PER_FRAME - spentTime);
			}
			catch (InterruptedException e)
			{
				//
				// We were interrupted while waiting ... no problem!
				//
			}
			catch (IllegalArgumentException e)
			{
				//
				// We sent the processor to sleep with a negative amount of milliseconds.
				// This means that we are not being able to calculate and draw everything
				// in one loop (i.e. we are running out of time!).
				//
			}
		}
	}


	
	/**
	 * Finish this battle by stopping the thread.-
	 */
	public void quit ( )
	{
		this.running = false;
		this.gameEngine.clearGame ( );
	}
	
	
	/**
	 * Changes the status of the running flag for this thread.-
	 */
	public void setRunningFlag (boolean flag)
	{
		//
		// Set a valid pointer to the map
		//
		this.map = this.gameEngine.getMap ( );
		
		this.running = flag;
	}
}
