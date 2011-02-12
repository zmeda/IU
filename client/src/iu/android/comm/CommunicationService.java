package iu.android.comm;

import iu.android.engine.PlayerRegistry;
import iu.android.explore.ExplorePlayer;
import iu.android.network.NetworkResources;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.os.IBinder;


/**
 * A Service that spawns a new thread in which it sends User's Locations from a LocationProvider to the Server
 * When the service is started, arguments in Bundle define a transformation that will be use from that point.
 * 
 * @author luka
 */
public class CommunicationService extends Service
{
	protected static final String			LOCATION_CHANGED_ACTION	= "android.intent.action.LOCATION_CHANGED";
	protected static final IntentFilter	filter						= new IntentFilter ( );
	{
		CommunicationService.filter.addAction (CommunicationService.LOCATION_CHANGED_ACTION);
	}

	/**
	 * Distance from current Location and last sent Location must be larger that this for the current Location
	 * to be sent to the server
	 */
	private static final float				Distance						= 5.0f;

	/**
	 * Time (millis) that must pass since last location was sent for the current Location to be sent to the
	 * server
	 */
	private static final long				Time							= 1000;
	private static final int				WaitBeforeCamping			= 5000;

	/** Thread that is started when the service is first started */
	protected Worker							worker;

	protected ILocationSender				locationSender				= null;
	protected ILocationTransform			locationTransform;

	protected static GPSCircleSimulator	gpsSimulator				= null;

	protected static long					lastSentTimestamp			= 0L;
	protected static Location				lastSentLocation			= null;
	
	private boolean							started						= false;

	private Timer								timer							= null;
	private GpsUpdater						gpsUpdater					= null;
	private GameWorld							world							= null;


	public static Location getGameLocation ( )
	{
		return CommunicationService.lastSentLocation;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 * 
	 * A worker thread is created and started
	 */
	@Override
	public void onCreate ( )
	{
		//this.locationManager = (LocationManager) this.getSystemService (Context.LOCATION_SERVICE);

		//
		// Criteria for selecting the best provider
		//
		final Criteria criteria = new Criteria ( );
		criteria.setAccuracy (100);
		criteria.setCostAllowed (false);
		criteria.setSpeedRequired (false);
		criteria.setAltitudeRequired (false);
		criteria.setPowerRequirement (Criteria.POWER_LOW);

		this.worker = new Worker ( );

		this.started = false;

		super.onCreate ( );

		//
		// GPSUpdating
		//
		this.gpsUpdater = new GpsUpdater ( );

		this.timer = new Timer ( );

		this.timer.schedule (this.gpsUpdater, 1000, CommunicationService.Time);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(int, android.os.Bundle)
	 * 
	 * Bundle Keys: DeltaLatitude, DeltaLongitude, Height, Width
	 */
	@Override
	public void onStart (final Intent intent, final int startId)
	{
		if (!this.started)
		{

			if (!this.worker.isRunning ( ))
			{
				this.worker.start ( );
			}

			super.onStart (intent, startId);

			this.started = true;
		}

		// Get world
		this.world = BundleUtils.getGameWorld (intent);

		// Init the world to game location transform
		this.locationTransform = new LinearTransform (this.world);
	}

	
	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 * 
	 * Sends a request to the worker thread to stop what it is doing.
	 */
	@Override
	public void onDestroy ( )
	{
		// Post a stop request to the Thread of this service
		if (this.worker != null)
		{
			this.worker.requestStop ( );
		}

		this.timer.cancel ( );

		super.onDestroy ( );
	}


	/**
	 * @return the gpsSimulator
	 */
	public static GPSCircleSimulator getGpsSimulator ( )
	{
		return CommunicationService.gpsSimulator;
	}

	/**
	 * 
	 * 
	 * Used for updating of the GPS location
	 * 
	 */

	protected class GpsUpdater extends TimerTask
	{
		@Override
		public void run ( )
		{
			// If no GPS Simulator yet then init it.
			if (CommunicationService.gpsSimulator == null && CommunicationService.this.world != null)
			{
				// CommunicationService.this.stopSelf ( );
				// CommunicationService.this.initGpsSimulator ( );
				CommunicationService.gpsSimulator = new GPSCircleSimulator (CommunicationService.this.world, 1 / 10.0f);

				// If still not yet then do nothing - next time maybe
				if (CommunicationService.gpsSimulator == null)
				{
					return;
				}
			}

			Location wLocation = CommunicationService.gpsSimulator.getCurrentLocation ( );

			//
			// [] I had to add this otherwise the commander may get drawn outside the game world
			//
			Location gLocation = CommunicationService.this.locationTransform.toGame (wLocation);

			// AND if moved more than CommunicationService.Distance meters since last send 
			// we don't want to swamp the server with meaningless location updates
			if (CommunicationService.lastSentLocation == null || CommunicationService.lastSentLocation.distanceTo (gLocation) > CommunicationService.Distance)
			{

				ExplorePlayer player = PlayerRegistry.getLocalPlayer ( );
				player.setUpCamp(false);

				// Initialize if necessary
				if (CommunicationService.this.locationSender == null && player != null)
				{
					InetAddress address = NetworkResources.getClient ( ).getServerAddress ( );

					CommunicationService.this.locationSender = new GPSLocationSender (address, player);
				}

				// Remember the current location on the reach of everyone
				CommunicationService.lastSentTimestamp = System.currentTimeMillis ( );
				CommunicationService.lastSentLocation = gLocation;

				//
				// Send new location
				//
				SendLocationTask task = new SendLocationTask (CommunicationService.this.locationSender, gLocation);
				CommunicationService.this.worker.push (task);
			}
			else if ((System.currentTimeMillis ( ) - CommunicationService.lastSentTimestamp) > CommunicationService.WaitBeforeCamping)
			{
				PlayerRegistry.getLocalPlayer ( ).setUpCamp (true);
			}
		}
	}

	@Override
	public IBinder onBind (Intent intent)
	{
		return null;
	}
}
