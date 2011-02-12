package iu.android.explore;

import iu.android.comm.CommunicationService;
import iu.android.comm.LinearTransform;
import android.location.Location;
import android.os.Message;
import android.util.Log;

/**
 * TODO - rewrite this documentation
 * 
 * This class refreshes the GPS acquired position every 'timeInMillis' milliseconds and calculates the longitude/latitude position into a x/y position on the exploration map.
 * 
 * The latitude/longitude requires a translation into proper x/y coordinates to enable all players equal moving speeds no matter where on the globe they are. Reason: A person in norway would have to move a lot
 * less to shift a certain amount of degrees in longitude than someone on the equator thus giving him a great advantage in exploration.
 * 
 * All that is required to use this class is make an object of type LocationUpdater and provide the parent Activity plus a time between two consecutive refreshes. Also onStart method should call the
 * resumeRunning() function, onStop should call the pauseRunning() function and onDestroy should call the stopUpdating() function.
 * 
 * 
 * This class should also take care of our relative position regarding the exploring field. This way people from all over the world can play on the same territory regardless of where they live. A person when
 * first signing in automatically defines its 'Ground zero' (From now on GZ) later in the game as he takes a walk he would be making the same movements in the IU exploring map as in the real world.
 * 
 * Example: I live in Ljubljana and when I sign up to the IU my coordinates are set to my GZ. This location represents a random coordinate (x0,y0) on the map in explore mode. If I move a kilometer north from my
 * current position I will be at (x0,y0+1000). Another player living in London would Sign up at his favorite coffee place and has its current location set to his GZ. This is randomly set for example 500 meters
 * south of my GZ. This means that if he was to go 500 meters north from his favorite coffee he would in the explore mode move to my GZ and if I was at home (where I first Signed up for an IU account) and was
 * in explore mode we would be in he same position (we would meet and would see each other on the explore mode map).
 */

public class LocationUpdater
{
	private final ExploreMode	activity;
	private final Commander		commander;

	// Follow mode to follow the player around
	private boolean				followMode		= true;

	// Should the view center onto the players location on the next frame
	private boolean				centerToPlayer	= true;

	// Thread to do the updating of our location
	private UpdaterThread		updaterThread;
	
	// A flag to draw the camping only once (spare CPU resources!)
	private boolean				commanderHasMoved = false;


	/*
	 * Activity needed in order to get the LocationManager system service
	 */
	public LocationUpdater (final ExploreMode activity, final Commander commander)
	{
		this.activity = activity;
		this.commander = commander;

		this.updaterThread = new UpdaterThread (1000);
		this.updaterThread.start ( );
	}


	/**
	 * Toggle between follow and still view location
	 */
	public void toggleFollow ( )
	{
		// Toggle follow mode
		this.setFollowMode (!this.followMode);
	}


	/**
	 * Sets the location updater to follow mode
	 * 
	 * @param follow
	 *           new state of the location updater
	 */

	public void setFollowMode (final boolean follow)
	{
		this.followMode = follow;

		// If mode changed
		if (this.followMode)
		{
			this.updateLocation ( );
		}
	}


	/**
	 * Is the location in follow mode
	 * 
	 * @return True if the location updater is in follow mode
	 */
	public boolean isFollowMode ( )
	{
		return this.followMode;
	}


	/**
	 * Tells the location updater to center the MapView onto the current player location
	 */
	public void centerToPlayer ( )
	{
		this.centerToPlayer = true;
	}


	/**
	 * Updates the location according to the current GPS coordinates
	 */
	protected void updateLocation ( )
	{
		//
		// Tell the client we are waiting for GPS for first fix
		//
		while (CommunicationService.getGameLocation ( ) == null)
		{
			try
			{
				Log.d ("IU", "Waiting for GPS device to start ...");
				Thread.sleep (500);
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace ( );
			}
		}

		//
		// Update only if location changed
		//
		Location 	  newMapLocation  = CommunicationService.getGameLocation ( );
		ExplorePlayer commanderPlayer = this.commander.player;

		if (commanderPlayer.lastLocation != newMapLocation)
		{
			Log.d ("IU", "Updating location ...");
			
			//
			// With the help of this flag, we will stop drawing the overlay as soon as the commander camps somewhere.
			//
			this.commanderHasMoved = true;
			
			// Remember the last location
			commanderPlayer.lastLocation = commanderPlayer.mapLocation;
			commanderPlayer.lastPoint = commanderPlayer.mapPoint;
	
			// Set new location
			commanderPlayer.mapLocation = newMapLocation;
			commanderPlayer.mapPoint = LinearTransform.toPointE6 (newMapLocation);
	
			if (this.followMode)
			{
				// If in follow mode than center map to our location
				this.activity.getMapController ( ).setCenter (commanderPlayer.mapPoint);
				this.centerToPlayer = false;
			}
			else if (this.centerToPlayer)
			{
				// If we just want to center to the map location then we animate to it
				this.activity.getMapController ( ).animateTo (commanderPlayer.mapPoint);
				this.centerToPlayer = false;
			}
	
			//
			// Refreshes the map so that the new location is drawn
			//
			Message msg = this.activity.handler.obtainMessage (ExploreMode.REFRESH_MAP_VIEW);
			msg.sendToTarget ( );
		}
		//
		// Is the commander camping?
		//
		else if (this.commanderHasMoved && commanderPlayer.isCamping ( ))
		{
			//
			// The commander is now camping, so clear the has moved flag
			//
			this.commanderHasMoved = false;
			
			//
			// Refreshes the map so that the new location is drawn
			//
			Message msg = this.activity.handler.obtainMessage (ExploreMode.REFRESH_MAP_VIEW);
			msg.sendToTarget ( );
		}
	}


	/**
	 * Pause/Resume location updating
	 * 
	 * @param pause
	 */
	public void pause (final boolean pause)
	{
		if (pause)
		{
			this.updaterThread.pauseUpdating ( );
		}
		else
		{
			this.updaterThread.resumeUpdating ( );
		}
	}


	/**
	 * Stop with location updating
	 */
	public void stopUpdating ( )
	{
		this.updaterThread.stopUpdating ( );
	}

	/**
	 * Returns a reference to the commander for whom we are managing location.-
	 */
	public Commander getCommander ( )
	{
		return (this.commander);
	}
	
	
	/**
	 * 
	 * @author xp
	 * 
	 * A class to continuously check the latest GPS location and update the player properties...
	 * 
	 */
	private class UpdaterThread extends Thread
	{
		private boolean updating;
		private boolean pause;
		private long	 updateInterval;


		public UpdaterThread (final long millis)
		{
			this.updating = true;
			this.pause = false;
			this.updateInterval = millis;
		}


		@Override
		public void run ( )
		{
			while (this.updating)
			{
				if (!this.pause)
				{
					LocationUpdater.this.updateLocation ( );
				}

				try
				{
					Thread.sleep (this.updateInterval);
				}
				catch (InterruptedException ex)
				{
				}
			}
		}


		public void pauseUpdating ( )
		{
			this.pause = true;
		}


		public void resumeUpdating ( )
		{
			this.pause = false;
		}


		public void stopUpdating ( )
		{
			this.updating = false;
		}


		/**
		 * @return the updateInterval
		 */
		public long getUpdateInterval ( )
		{
			return updateInterval;
		}
	}
}
