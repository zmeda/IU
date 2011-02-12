package iu.android.explore;

import iu.android.Debug;
import iu.android.R;
import iu.android.battle.BattleActivity;
import iu.android.comm.BundleUtils;
import iu.android.comm.CommunicationService;
import iu.android.comm.GPSCircleSimulator;
import iu.android.comm.GameWorld;
import iu.android.comm.IntentFactory;
import iu.android.comm.LinearTransform;
import iu.android.engine.FlagRegistry;
import iu.android.engine.PlayerRegistry;
import iu.android.explore.event.EventBattleStarted;
import iu.android.gui.Briefing;
import iu.android.network.NetworkResources;
import iu.android.network.explore.ExploreClient;
import iu.android.network.explore.FlagBattle;
import iu.android.network.explore.Protocol;
import iu.android.network.explore.ReceiverThread;
import iu.android.network.explore.Sender;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * 
 * @author luka
 * 
 */
public class ExploreMode extends MapActivity
{
	//
	// Menu items IDs
	//
	private static final int	START_WALKING_ID			= Menu.FIRST;
	private static final int	WALK_BACKWARDS_ID			= Menu.FIRST + 2;
	private static final int	LOG_OUT_ID					= Menu.FIRST + 3;

	protected static final int	DEFEND_FLAG					= 1;
	protected static final int	DEFEND_FLAG_AI				= 2;
	public static final int		SHOW_ATTACK_ALERT			= 3;
	public static final int		FLAG_UNDER_ATTACK			= 4;
	protected static final int	COMMUNICATION_PROBLEM	= 5;
	protected static final int	START_BATTLE				= 0;
	protected static final int	ERROR							= 7;
	protected static final int	ATTACK_FLAG					= 8;
	private static final int	INFO_MESSAGE				= 9;
	public static final int 	REFRESH_MAP_VIEW			= 10;

	public boolean					tutorialMode				= false;

	// Client socket connection...
	private ExploreClient		client;

	private MapView				exploreView;
	
	private MapController		exploreController;

	GameWorld						gameWorld;

	// Class to translate GPS location to BattleEngine World location
	LinearTransform				coordinateTranslation;

	// For location updating
	LocationUpdater				locationUpdater;

	// Handles the state of exploration
	FogOfWar							fogOfWar;

	//
	// Map overlay used to draw over the displayed map
	//
	private GeoLocationOverlay	geoLocationOverlay;

	// Minimum and maximum allowed zoom
	private int						minZoom						= 12;
	private int						maxZoom						= 19;


	// // This intent is recieved if we have the explore mode in the background and one of
	// // out flags is attacked and we chose to defend it
	// public static final String DEFEND_FLAG = "android.intent.action.LOCATION_CHANGED";
	// protected static final IntentFilter filter = new IntentFilter (ExploreMode.DEFEND_FLAG);
	//
	// DefendFlagReceiver locationReceiver = new DefendFlagReceiver ( );
	// this.registerReceiver (this.locationReceiver, CommunicationService.filter);

	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		
		this.getWindow ( ).setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature (Window.FEATURE_NO_TITLE);
		this.setContentView (R.layout.explore_activity);

		this.exploreView = (MapView) findViewById (R.id.explore_view);
		this.exploreController = this.exploreView.getController ( );

		//
		// Initialize images for Commanders
		//
		Rank.initDrawables (this);
		ExplorePlayer.initTent (this);

		this.tutorialMode = this.getIntent ( ).getExtras ( ).getBoolean ("tutorialMode");
		
		//
		// Get the client connection to the server
		//
		while (NetworkResources.getClient ( ) == null)
		{
			try
			{
				Log.d ("IU", "Waiting for network client to connect ...");
				Thread.sleep (500);
			}
			catch (InterruptedException e)
			{
				//
				// Nothing wrong if interrupted here
				///
			}
		}
		this.client = NetworkResources.getClient ( );
		this.client.setExploreMode (this);
		
		//
		// Map information from server
		//
		Commander commander = this.loadDataFromServer ( );

		//
		// Uses a GPS location manager to periodically update our location
		//
		this.locationUpdater = new LocationUpdater (this, commander);

		//
		// Geo location overlay displays important locations in our vicinity
		//
		this.geoLocationOverlay = new GeoLocationOverlay (this);

		//this.overlayController.add (this.geoLocationOverlay, true);
		this.exploreView.getOverlays ( ).add (this.geoLocationOverlay);

		//
		// Show the map view at the right zoom and with the right layers
		//
		if (!this.exploreView.isSatellite ( ))
		{
			this.exploreView.setSatellite (true);
		}
		this.exploreController.setZoom (18);

		//
		// If the briefing mode is enabled ...
		//
		this.switchToTutorial ( );

		//
		// Redraw the map
		//
		this.exploreView.invalidate ( );
	}
	

	@Override
	public boolean onCreateOptionsMenu (final Menu menu)
	{
		super.onCreateOptionsMenu (menu);

		menu.add (0, ExploreMode.START_WALKING_ID, 	0, R.string.start_walking_text);
		menu.add (0, ExploreMode.WALK_BACKWARDS_ID, 	0, R.string.walk_backwards_text);
		menu.add (0, ExploreMode.LOG_OUT_ID, 			0, R.string.log_out_text);

		// We can open the menu.
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		GPSCircleSimulator gpsSim = CommunicationService.getGpsSimulator ( );

		switch (item.getItemId ( ))
		{
			case ExploreMode.START_WALKING_ID:

				if (gpsSim != null)
				{
					gpsSim.togglePause ( );

					if (gpsSim.isPause ( ))
					{	
						item.setTitle (R.string.start_walking_text);
					}
					else
					{
						item.setTitle (R.string.stop_walking_text);
					}
				}
				return true;

			case ExploreMode.WALK_BACKWARDS_ID:
				if (gpsSim != null)
				{
					gpsSim.reverseDirection ( );
				}
				return true;
				
			case ExploreMode.LOG_OUT_ID:
				this.finish ( );
				return true;
				
		}
		return super.onOptionsItemSelected (item);
	}


	/**
	 * Just resume the location updating
	 */
	@Override
	public void onResume ( )
	{
		super.onResume ( );
		
		if (this.locationUpdater != null)
		{
			this.locationUpdater.pause (false);
		}
	}


	/**
	 * Just pause the location updating
	 */	
	@Override
	protected void onStop ( )
	{
		super.onPause ( );

		if (this.locationUpdater != null)
		{
			this.locationUpdater.pause (true);
		}
	}


	@Override
	public void onDestroy ( )
	{
		if (this.client != null)
		{
			this.client.logout ( );
		}

		// Stop the communication service if finish called
		if (this.isFinishing ( ))
		{
			this.stopCommunicationService ( );
		}

		if (this.locationUpdater != null)
		{
			this.locationUpdater.stopUpdating ( );
		}

		super.onStop ( );
	}


	public void startCommunicationService ( )
	{
		Intent startServiceIntent = IntentFactory.createSendLocationIntent (this);

		// extras.putString (CommunicationService.REASON, reason);
		// extras.putString (IntentFactory.Type, IntentFactory.Type_Location);
		BundleUtils.putGameWorld (this.gameWorld, startServiceIntent);

		this.startService (startServiceIntent);
	}


	public void stopCommunicationService ( )
	{
		Intent intent = new Intent ( );
		intent.setClass (this, CommunicationService.class);
		this.stopService (intent);
	}


	@Override
	public boolean onKeyDown (final int keyCode, final KeyEvent event)
	{
		int z;

		switch (keyCode)
		{
			case KeyEvent.KEYCODE_I:
				// Zoom not closer than maxZoom
				z = this.exploreView.getZoomLevel ( );

				if (z < this.maxZoom)
				{
					z++;
					this.exploreController.setZoom (z);
				}

				return true;

			case KeyEvent.KEYCODE_O:
				// Zoom not farther than minZoom
				z = this.exploreView.getZoomLevel ( );

				if (z > this.minZoom)
				{
					z--;
					this.exploreController.setZoom (z);
				}

				return true;

			case KeyEvent.KEYCODE_T:
				// Switch to satellite view
				// this.exploreView.toggleSatellite ( );
				this.exploreView.setSatellite (true);
				return true;

			case KeyEvent.KEYCODE_R:
				// Switch to satellite view
				// this.exploreView.toggleTraffic ( );
				this.exploreView.setTraffic (true);
				return true;

			case KeyEvent.KEYCODE_F:
				// Switch between follow mode
				this.locationUpdater.toggleFollow ( );
				return true;

			case KeyEvent.KEYCODE_C:
				this.locationUpdater.centerToPlayer ( );
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
				this.switchToFlag ( );
				return true;

			case KeyEvent.KEYCODE_ENDCALL:
				this.finish ( );
				return true;

			case KeyEvent.KEYCODE_D:
				this.geoLocationOverlay.toggleDebugMode ( );
				return true;
		}

		return false;
	}


	/**
	 * When client clicks on flag which owner is not him attack alert is shown. If attack is clicked
	 * BattlePreperation screen is shown
	 */
	private void attackFlagAlert (final Flag flag)
	{
		//
		// Show the dialog to the user
		// 
		String notifyMessage = "You are near " + flag.getOwner ( ).getName ( ) + "'s base.\n" + "There are "
				+ this.locationUpdater.getCommander ( ).getHovercraftCount ( ) + " hoverjets and "
				+ this.locationUpdater.getCommander ( ).getTankCount ( ) + " tanks in your convoy.\n"
				+ "The artillery squad counts " + this.locationUpdater.getCommander ( ).getArtilleryCount ( )
				+ " units.";

		AlertDialog questionDialog = new AlertDialog.Builder (this).setIcon (0).setTitle ("Attack?")
				.setMessage (notifyMessage).setPositiveButton ("Go!", new DialogInterface.OnClickListener ( ) {
					public void onClick (DialogInterface dialog, int whichButton)
					{
						Message msg = new Message ( );
						msg.what = ExploreMode.ATTACK_FLAG;
						msg.obj = flag;
						ExploreMode.this.handler.sendMessage (msg);
					}
				}).setNegativeButton ("Run away", null).create ( );

		questionDialog.show ( );
	}


	/**
	 * This method is called when EVENT_UNDER_ATTACK is received
	 */
	private void flagUnderAttack (final int battleId)
	{
		// get data flag battle

		Thread t = new Thread (new Runnable ( ) {
			public void run ( )
			{

				FlagBattle dfb = null;

				Sender sender = NetworkResources.getClient ( ).getSender ( );
				ReceiverThread receiver = NetworkResources.getClient ( ).getReceiverThread ( );
				try
				{
					sender.getDataFlagBattle (battleId);
					// wait for response
					Debug.Joshua.print ("Wait for data flag battle");
					dfb = receiver.waitForDataFlagBattle ( );
					Debug.Joshua.print ("data flag battle received");
				}
				catch (IOException e)
				{
					// communication problem
					ExploreMode.this.handler.sendEmptyMessage (ExploreMode.COMMUNICATION_PROBLEM);
				}

				dfb.battleId = battleId;

				Message msg = new Message ( );
				msg.what = ExploreMode.SHOW_ATTACK_ALERT;
				msg.obj = dfb;
				ExploreMode.this.handler.sendMessage (msg);

			}
		});

		t.start ( );
	}


	/**
	 * Alert msg when data DATA_BATTLE_FLAG is received from server
	 * 
	 * @param dfb
	 */
	public void showAttackAlert (final FlagBattle dfb)
	{
		// set defender units
		// for now it will be all units on flag
		Flag flag = FlagRegistry.getFlag (dfb.idFlag);
		dfb.defHover = (short) flag.hovercraftCount;
		dfb.defTank = (short) flag.tankCount;
		dfb.defArtillery = (short) flag.hovercraftCount;

		/*
		 * DialogInterface.OnClickListener deffendFlag = new DialogInterface.OnClickListener ( ) {
		 * 
		 * public void onClick (DialogInterface arg0, int arg1) { Message deffendFlag = new Message ( );
		 * deffendFlag.what = ExploreMode.DEFEND_FLAG; deffendFlag.obj = dfb;
		 * ExploreMode.this.handler.sendMessage (deffendFlag);
		 * 
		 * }
		 * 
		 * };
		 * 
		 * DialogInterface.OnClickListener deffendFlagAI = new DialogInterface.OnClickListener ( ) {
		 * 
		 * public void onClick (DialogInterface arg0, int arg1) { Message deffendFlag = new Message ( );
		 * deffendFlag.what = ExploreMode.DEFEND_FLAG_AI; deffendFlag.obj = dfb;
		 * ExploreMode.this.handler.sendMessage (deffendFlag);
		 * 
		 * }
		 * 
		 * };
		 */
		String notifyMessage = "You're being attacked!\n" + "An army under "
				+ PlayerRegistry.getPlayer (dfb.idAttacker).getName ( )
				+ "'s orders is closing in on one of your bases.\n"
				+ "Our spy just returned with fresh information about the enemy:\n" + "Hovercrafts : "
				+ dfb.attHover + "\n  Tanks : " + dfb.attTank + "\n  Artillery : " + dfb.attArtillery;

		// this.showAlert ("Under attack!", notifyMessage, "Command defense", deffendFlag, "Send an army",
		// deffendFlagAI, false, null);

		AlertDialog questionDialog = new AlertDialog.Builder (this).setIcon (0).setTitle (
				"You're under attack!").setMessage (notifyMessage).setPositiveButton ("Command defense",
				new DialogInterface.OnClickListener ( ) {
					public void onClick (DialogInterface dialog, int whichButton)
					{
						Message deffendFlag = new Message ( );
						deffendFlag.what = ExploreMode.DEFEND_FLAG;
						deffendFlag.obj = dfb;
						ExploreMode.this.handler.sendMessage (deffendFlag);
					}
				}).setNegativeButton ("Send an army", new DialogInterface.OnClickListener ( ) {
			public void onClick (DialogInterface dialog, int whichButton)
			{
				Message deffendFlag = new Message ( );
				deffendFlag.what = ExploreMode.DEFEND_FLAG_AI;
				deffendFlag.obj = dfb;
				ExploreMode.this.handler.sendMessage (deffendFlag);
			}
		}).create ( );
		questionDialog.show ( );
	}

	//
	// handler for handling events from other threads
	//
	public Handler	handler	= new Handler ( ) {
										@Override
										public void handleMessage (Message msg)
										{
											switch (msg.what)
											{

												case DEFEND_FLAG:
													Debug.Joshua.print ("DEFEND_FLAG");
													ExploreMode.this.defendFlag (Protocol.BATTLE_ACCEPT_STATE_ACCEPTED, (FlagBattle) msg.obj);
													break;

												case DEFEND_FLAG_AI:
													Debug.Joshua.print ("DEFEND_FLAG_AI");
													ExploreMode.this.defendFlag (Protocol.BATTLE_ACCEPT_STATE_AI, (FlagBattle) msg.obj);
													break;

												case SHOW_ATTACK_ALERT:
													Debug.Joshua.print ("SHOW ATTACK ALERT");
													ExploreMode.this.showAttackAlert ((FlagBattle) msg.obj);
													break;

												case FLAG_UNDER_ATTACK:
													Debug.Joshua.print ("FLAG UNDER ATTACK msg");
													ExploreMode.this.flagUnderAttack (msg.arg1);
													break;

												case START_BATTLE:
													Debug.Joshua.print ("START_BATTLE");
													ExploreMode.this.switchToBattleModeDefend ((FlagBattle) msg.obj);
													break;

												case ATTACK_FLAG:
													Debug.Joshua.print ("ATTACK_FLAG");
													ExploreMode.this.switchToBattlePreparation ((Flag) msg.obj);
													break;

												case COMMUNICATION_PROBLEM:
													ExploreMode.this.showError ("Communication error!");
													break;

												case ERROR:
													Debug.Joshua.print ("ERROR: " + msg.obj.toString ( ));
													ExploreMode.this.showError (msg.obj.toString ( ));
													break;

												case INFO_MESSAGE:
													// ExploreMode.this.showAlert ("Info", (String) msg.obj, "ok",
													// false);

													AlertDialog infoDialog = new AlertDialog.Builder (
															ExploreMode.this.exploreView.getContext ( )).setIcon (0)
															.setTitle ("Info").setPositiveButton ("ok", null).setMessage (
																	(String) msg.obj).create ( );
													infoDialog.show ( );

													break;
												
												case REFRESH_MAP_VIEW:
													Log.d ("IU", "Refreshing map");
													ExploreMode.this.exploreView.invalidate ( );
													break;
											}
										}
									};


	/**
	 * Defend flag
	 * 
	 * @param accept
	 *           - can be ACCEPT, AI, REFUSE
	 * @param dfb
	 *           - Container of all important data that is needed to start battle
	 */
	private void defendFlag (final byte accept, final FlagBattle dfb)
	{
		Debug.Joshua.print ("Defend Flag: accepted: " + accept);
		Thread t = new Thread (new Runnable ( ) {
			public void run ( )
			{

				// send deffend flag msg
				ReceiverThread receiver = NetworkResources.getClient ( ).getReceiverThread ( );
				Sender sender = NetworkResources.getClient ( ).getSender ( );

				EventBattleStarted ebs = null;

				Flag flag = FlagRegistry.getFlag (dfb.idFlag);

				try
				{
					sender.deffendFlag (dfb.battleId, accept, (short) flag.hovercraftCount,
							(short) flag.tankCount, (short) flag.artilleryCount);

					Debug.Joshua.print ("Wait for Event Battle Started");
					ebs = receiver.waitForEventBattleStarted ( );
					Debug.Joshua.print ("Received Event Battle Started - port=" + ebs.tcpPort);
				}
				catch (IOException e)
				{
					ExploreMode.this.handler.sendEmptyMessage (ExploreMode.COMMUNICATION_PROBLEM);
				}

				if (ebs == null)
				{
					Message message = new Message ( );
					message.what = ExploreMode.ERROR;
					String msg = "No Battle Started Received";
					message.obj = msg;
					ExploreMode.this.handler.sendMessage (message);
				}
				else
				{
					dfb.communicationPort = ebs.tcpPort;
					Message msg = new Message ( );
					msg.what = ExploreMode.START_BATTLE;
					msg.obj = dfb;

					ExploreMode.this.handler.sendMessage (msg);

				}
				;

			}
		});
		t.start ( );

	}


	/**
	 * Shows error alert with message msg
	 * 
	 * @param msg
	 */
	protected void showError (final String msg)
	{
		// this.showAlert ("Error", msg, "ok", false);
		AlertDialog errorDialog = new AlertDialog.Builder (this.getBaseContext ( )).setIcon (0).setTitle (
				"Error").setPositiveButton ("ok", null).setMessage (msg).create ( );
		errorDialog.show ( );
	}


	public LocationUpdater getLocationUpdater ( )
	{
		return this.locationUpdater;
	}


	private Commander loadDataFromServer ( )
	{
		// Gets game map location info from server
		this.gameWorld = this.client.loadMapInfo ( );

		this.coordinateTranslation = new LinearTransform (this.gameWorld);

		this.startCommunicationService ( );

		// Gets players that we need to know of
		// TODO - get ONLY the players needed
		this.client.loadPlayers ( );

		// Get the player info
		Commander commander = this.client.loadCommander ( );

		// Get the fog of war status
		this.fogOfWar = this.client.loadFogOfWar (this.gameWorld);
		FlagRegistry.setFogOfWar (this.fogOfWar);

		// TODO - Get ONLY the flags that we need
		this.client.loadFlags ( );

		// TODO - Done with initialization - start 'send/receive UDP packet' threads
		// this.client.start ( );

		this.client.endInitialization ( );

		return commander;
	}


	//
	//
	//
	// For handling Sub-activities
	//
	//
	//

	private void switchToTutorial ( )
	{
		//
		// If tutorial mode is on
		//
		if (this.tutorialMode)
		{
			Intent i = new Intent (this, Briefing.class);

			i.putExtra ("tutorial_context", Briefing.ExploreModeBriefing);

			this.startActivityForResult (i, Briefing.TUTORIAL_REQUEST_CODE);
		}
	}


	private void switchToBattleModeAttack (final int tcpPort, final Flag flag)
	{
		Commander commander = this.locationUpdater.getCommander ( );

		if (flag != null)
		{
			Intent intent = new Intent (this, BattleActivity.class);
			Bundle extras = new Bundle ( );

			extras.putBoolean ("practice", false);
			BundleUtils.putFlag (flag, extras);
			BundleUtils.putCommander (commander, extras);
			BundleUtils.putTCPPort (tcpPort, extras);

			intent.putExtras (extras);

			if (!CommunicationService.getGpsSimulator ( ).isPause ( ))
			{
				CommunicationService.getGpsSimulator ( ).togglePause ( );
			}

			this.startActivityForResult (intent, BattleActivity.BATTLE_MODE_REQUEST_CODE);
		}
	}


	private void switchToBattleModeDefend (final FlagBattle flagBattle)
	{
		
		// will try to use same interface as before
		// because of that i will create commander and flag with same ids
		Intent intent = new Intent (this, BattleActivity.class);
		Bundle extras = new Bundle ( );

		extras.putBoolean ("practice", false);
		BundleUtils.putFlag (FlagRegistry.getFlag (flagBattle.idFlag), extras);
		BundleUtils.putCommander (this.locationUpdater.getCommander ( ), extras);
		BundleUtils.putTCPPort (flagBattle.communicationPort, extras);
		intent.putExtras (extras);

		Debug.Joshua.print ("Battle Mode Deffend - before starting activity");
		if (!CommunicationService.getGpsSimulator ( ).isPause ( ))
		{
			CommunicationService.getGpsSimulator ( ).togglePause ( );
		}

		this.startActivityForResult (intent, BattleActivity.BATTLE_MODE_REQUEST_CODE);
	}


	/**
	 * Checks whether player is owner or not. Regard to this it switches to battle preparation or commander
	 * editor
	 * 
	 */
	private void switchToFlag ( )
	{
		Flag flag = this.locationUpdater.getCommander ( ).nearestFlag;

		if (flag == null)
		{
			return;
		}

		if (flag.owner.getId ( ) == this.locationUpdater.getCommander ( ).getPlayer ( ).getId ( ))
		{
			this.switchToCommanderEditor (flag);
		}
		else
		{
			this.attackFlagAlert (flag);
		}
	}


	/**
	 * Switch to commander editor - when flag owner is current player
	 * 
	 * @param flag
	 */
	private void switchToCommanderEditor (final Flag flag)
	{
		Commander commander = this.locationUpdater.getCommander ( );

		if (flag != null)
		{
			Intent intent = new Intent (this, CommanderEditor.class);
			Bundle bundle = new Bundle ( );

			BundleUtils.putFlag (flag, bundle);
			BundleUtils.putCommander (commander, bundle);

			intent.putExtras (bundle);

			this.startActivityForResult (intent, CommanderEditor.COMMANDER_EDITOR_REQUEST_CODE);
		}
	}


	/**
	 * 
	 * @param flag
	 */
	private void switchToBattlePreparation (final Flag flag)
	{
		Commander commander = this.locationUpdater.getCommander ( );

		if (flag != null)
		{
			Intent intent = new Intent (this, BattlePreparation.class);
			Bundle bundle = new Bundle ( );

			BundleUtils.putFlag (flag, bundle);
			BundleUtils.putCommander (commander, bundle);

			intent.putExtras (bundle);

			intent.putExtra ("tutorial_mode", this.tutorialMode);

			this.startActivityForResult (intent, BattlePreparation.BATTLE_PREP_REQUEST_CODE);
		}
	}


	@Override
	protected void onActivityResult (final int requestCode, final int resultCode, final Intent intent)
	{
		Bundle extras = null;
		
		if (intent != null)
		{
			extras = intent.getExtras ( );
		}

		Log.d ("IU", "ExploreMode.onActivityResult -> " + String.valueOf (requestCode) + " " + String.valueOf (resultCode));

		//
		// If battle mode ended then update the unit count
		//
		if (requestCode == BattleActivity.BATTLE_MODE_REQUEST_CODE)
		{
			if (extras == null)
			{
				// battle has been canceled
				this.showError ("Operation failed. \nYou lost your army.");
				this.locationUpdater.getCommander ( ).setHovercraftCount (0);
				this.locationUpdater.getCommander ( ).setTankCount (0);
				this.locationUpdater.getCommander ( ).setArtilleryCount (0);

				return;

			}

			String msg = "";

			boolean userWon = extras.getBoolean ("user_won");
			int[] userUnits = extras.getIntArray ("user_units");

			Flag flag = BundleUtils.getFlag (extras);
			Commander commander = BundleUtils.getCommander (extras);

			if (userWon)
			{
				msg = "Battle has been won!\n";
			}
			else
			{
				msg = "Battle has been lost!\n";
			}
			//			
			// int hLost = commander.getHovercraftCount() - userUnits[0];
			// int tLost = commander.getTankCount() - userUnits[1];
			// int aLost = commander.getArtilleryCount() - userUnits[2];
			// msg += "\nUnits lost\nHovercraft : "+hLost+"\nTank : "+tLost+"\nArtillery : "+aLost;
			//			

			Message m = new Message ( );
			m.what = ExploreMode.INFO_MESSAGE;
			m.obj = msg;
			this.handler.sendMessage (m);
			
			//
			// update commander units and flag units
			//
			if (flag.getOwner ( ) == commander.getPlayer ( ))
			{
				// defending flag
				flag.setNumHovercraft (userUnits[0]);
				flag.setNumTanks (userUnits[1]);
				flag.setNumArtillery (userUnits[2]);
			}
			else
			{
				commander.setHovercraftCount (userUnits[0]);
				commander.setTankCount (userUnits[1]);
				commander.setArtilleryCount (userUnits[2]);
			}

			// if user won and flag not his - change flag owner
			if (flag.getOwner ( ) == commander.getPlayer ( ) && !userWon)
			{
				// will be notified by server
			}

			if (flag.getOwner ( ) != commander.getPlayer ( ) && userWon)
			{
				flag.setOwner (commander.getPlayer ( ));
				flag.setNumHovercraft (1);
				flag.setNumTanks (1);
				flag.setNumArtillery (1);
			}

		}
		else if (requestCode == CommanderEditor.COMMANDER_EDITOR_REQUEST_CODE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Flag flag = BundleUtils.getFlag (extras);
				
				try
				{
					this.client.getSender ( ).setFlagUnits (flag.getFlagId ( ), 
																		 (short) flag.getNumHovercraft ( ),
																		 (short) flag.getNumTanks ( ), 
																		 (short) flag.getNumArtillery ( ));
					
					this.client.getSender ( ).setPlayerProperties (this.locationUpdater.getCommander ( ).player);
				}
				catch (IOException e)
				{
					// An error sending data through TCP
					// FIXME what to do here, logout?
				}
			}
		}
		else if (requestCode == BattlePreparation.BATTLE_PREP_REQUEST_CODE)
		{
			if (resultCode == Activity.RESULT_OK && extras != null)
			{
				// do attack
				Flag flag = FlagRegistry.getFlag (BundleUtils.getFlag (extras).getFlagId ( ));
				int tcpPort = BundleUtils.getTCPPort (extras);
				this.switchToBattleModeAttack (tcpPort, flag);
			}
			else
			{
				// do nothing
				Debug.Joshua.print ("FROM BATTLE PREP MODE - DO NOTHING");
			}
		}
		else if (requestCode == Briefing.TUTORIAL_REQUEST_CODE)
		{
			if (resultCode == Activity.RESULT_CANCELED)
			{
				//
				// Debug only!
				//
				if (Debug.LUCAS)
				{
					// Log.d (Debug.TAG, "Tutorial canceled!");
				}
			}
		}

		// TODO : send changes to server
		super.onActivityResult (requestCode, resultCode, intent);
	}


	@Override
	protected boolean isRouteDisplayed ( )
	{
		return false;
	}


	/**
	 * @return The map controller under the map view used by this activity.-
	 */
	public MapController getMapController ( )
	{
		return (this.exploreController);
	}
}