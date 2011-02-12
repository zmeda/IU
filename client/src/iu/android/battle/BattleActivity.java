package iu.android.battle;

import java.io.IOException;

import iu.android.Debug;
import iu.android.IUStart;
import iu.android.R;
import iu.android.comm.BundleUtils;
import iu.android.control.AccelerometerListener;
import iu.android.engine.BattlePlayer;
import iu.android.engine.PlayerRegistry;
import iu.android.engine.ai.AIPlayer;
import iu.android.explore.Commander;
import iu.android.explore.ExplorePlayer;
import iu.android.explore.Flag;
import iu.android.explore.Player;
import iu.android.graph.GameView;
import iu.android.map.Map;
import iu.android.network.battle.JoinBattleClient;
import iu.android.network.battle.LocalPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A battle should always be started with a BattleIntent
 */
public class BattleActivity extends Activity
{
	public static final int			BATTLE_MODE_REQUEST_CODE	= 111;

	public static final int			START_BATTLE					= 0;

	protected static final int		FINISH_BATTLE					= 1;

	public static final int			SHOW_MESSAGE					= 2;

	protected static final int		HIDE_MESSAGE					= 3;

	public static final int			SHOW_BATTLE_RESULT			= 4;

	private BattleEngine				gameEngine						= null;
	private BattleThread				gameThread						= null;
	private JoinBattleClient		battleClient					= null;
	private GameView					gameView							= null;
	private TextView					gameViewText					= null;

	private SensorManager			sensorManager					= null;
	private AccelerometerListener	sensorListener					= null;

	private int[]						player1Units					= null;
	private int[]						player2Units					= null;

	private boolean					finished							= false;
	private boolean					practice							= false;



	/**
	 * Changes battle behavior based on the message received.-
	 */
	private Handler messageHandler = new Handler ( )
	{
		@Override
		public void handleMessage (Message msg)
		{
			switch (msg.what)
			{
				case BattleActivity.START_BATTLE:
				{
					BattleActivity.this.startBattle ( );
					break;
				}
	
				case BattleActivity.FINISH_BATTLE:
				{
					BattleActivity.this.hideMessage ( );
					BattleActivity.this.finish ( );
					break;
				}
	
				case BattleActivity.SHOW_MESSAGE:
				{
					BattleActivity.this.showMessage ((String) msg.obj);
					break;
				}
			
				case BattleActivity.HIDE_MESSAGE:
				{
					BattleActivity.this.hideMessage ( );
					break;
				}
				
				case BattleActivity.SHOW_BATTLE_RESULT:
				{
					BattleActivity.this.showBattleResult ((BattlePlayer) msg.obj);
					break;
				}
				
				//
				// All other message are sent to the parent class
				//
				default:
					super.handleMessage (msg);
			}
		}
	};

	
	/**
	 * Sets up a practice or multiplayer battle and starts the battle engine.
	 * Leaves the camera on the upper left corner of the battle field.-
	 */
	private void startBattle ( )
	{
		//
		// Hide progress bar if any
		//
		this.hideMessage ( );

		this.showMessage ("Prepare for battle ...");
		
		//
		// Is this a multiplayer battle?
		//
		if (this.practice)
		{
			//
			// It is a practice battle against AI ...
			//
			this.setupBattleAgainstAI ( );
			
			this.gameView.init (this.gameEngine);
			
			this.gameThread.setRenderer (this.gameView);
			this.gameThread.setInputDevice (this.gameView);

			//
			// Hide any messages being displayed
			//
			this.hideMessage ( );
			
			//
			// Start the main loop for the battle
			//
			this.gameThread.setRunningFlag (true);
			this.gameThread.start ( );			
		}
		//
		// Multiplayer battle ...
		//
		else if (this.setupMultiplayerBattle ( ))
		{
			this.gameView.init (this.gameEngine);
			
			this.gameThread.setRenderer (this.gameView);
			this.gameThread.setInputDevice (this.gameView);

			//
			// Hide any messages being displayed
			//
			this.hideMessage ( );
			
			//
			// Start the main loop for the battle
			//
			this.gameThread.setRunningFlag (true);
			this.gameThread.start ( );
		}
		else
		{
			//
			// There was a problem while starting the battle 
			//
			this.finished = false;
			this.finishBattle ( );
		}
	}

	
	/**
	 * Stops the battle mode (used for onPause, onStop and onDestroy)
	 */
	private void finishBattle ( )
	{
		if (!this.finished)
		{
			if (this.gameThread != null)
			{
				this.gameThread.quit ( );
				this.gameThread = null;
			}

			if (this.battleClient != null)
			{
				this.battleClient.stopRunning ( );
			}
			
			this.finished = true;
			
			this.finish ( );
		}
	}
	
	
	/**
	 * Displays a message to the user.-
	 * 
	 * @param msg
	 */
	private void showMessage (String msg)
	{
		this.gameViewText.setVisibility (View.VISIBLE);
		this.gameViewText.setText (msg);
	}


	/**
	 * Hides any message being displayed to the user.-
	 */
	private void hideMessage ( )
	{
		this.gameViewText.setVisibility (View.INVISIBLE);
		this.gameViewText.setText (null);
	}
	

	/**
	 * Creates a new battle to be played against AI-controlled units.-
	 */
	private void setupBattleAgainstAI ( )
	{
		//
		// Show a progress dialog because the battle setup may take some time ...
		//
		//Message msg = this.battleActivityMessageHandler.obtainMessage (BattleActivity.SHOW_PROGRESS_DIALOG, new String ("Prepare for battle ..."));

		//
		// Step 1 - Build a small (8x8 tiles) battle field
		//
		Map map = new Map ((int) (Math.random ( ) * 100), 3);

		this.gameEngine.setMap (map);

		//
		// Step 2 - Create the human player, set his color and army units
		//
		Player humanPlayer = new Player ("Player 1");

		humanPlayer.setColor (0xFF0000FF);

		int[] humanPlayerUnits = this.player1Units;

		//
		// Step 3 - Create the AI player, set his color and army units
		//
		Player aiPlayer = new Player ("Android");

		aiPlayer.setColor (0xFFFF0000);

		int[] aiPlayerUnits = this.player2Units;

		//
		// Step 4 - Add the human player to the battleEngine engine
		//
		LocalPlayer humanLocalPlayer = new LocalPlayer (humanPlayer);

		this.gameEngine.addPlayer (humanLocalPlayer);
		this.gameEngine.setUser (humanLocalPlayer);

		//
		// Step 5 - Add both armies to the battleEngine engine
		//
		this.gameEngine.addPlayerArmy (humanLocalPlayer, humanPlayerUnits[0], humanPlayerUnits[1], humanPlayerUnits[2]);

		AIPlayer aiPlayerArmy = this.gameEngine.addAiArmy (aiPlayer, aiPlayerUnits[0], aiPlayerUnits[1], aiPlayerUnits[2]);

		//
		// Step 6 - Generate a random position for both armies on the battle field
		//
		this.gameEngine.setupPlayerUnitPositions (humanLocalPlayer, (int) (Math.random ( ) * 3), (int) (Math.random ( ) * 3));
		this.gameEngine.setupRandomAIPlayerUnitPositions (aiPlayerArmy, (int) (Math.random ( ) * 3), (int) (Math.random ( ) * 3));
	}
	

	/**
	 * Connects to the server to join a battle against some other player.-
	 */
	private boolean setupMultiplayerBattle ( )
	{
		//
		// Get the TCP listen port of the battle server
		//
		int battleServerPort = BundleUtils.getTCPPort (this.getIntent ( ).getExtras ( ));

		//
		// Get references to the commander and the flag in conflict
		//
		Commander commander = BundleUtils.getCommander (this.getIntent ( ).getExtras ( ));
		Flag flag = BundleUtils.getFlag (this.getIntent ( ).getExtras ( ));

		//
		// Create a new network client for this battle
		//
		this.battleClient = new JoinBattleClient ( );

		//
		// Set the battleEngine context to this client
		//
		this.battleClient.setGame (this.gameEngine);

		//
		// Set the commander behind the troops
		//
		this.battleClient.setCommander (commander);

		//
		// And the flag in conflict
		//
		this.battleClient.setFlag (flag);

		//
		// Try joining to the battle server ...
		//
		if (this.battleClient.joinToServer (IUStart.ServerIP, battleServerPort))
		{
			if (Debug.LUCAS)
			{
				Log.i (Debug.TAG, "Join to battle session completed!");
			}

			//
			// Step 1 - Build a small (8x8 tiles) battle field, using the flag ID as
			// random map seed
			//
			Map map = new Map (flag.getFlagId ( ), 3);

			this.gameEngine.setMap (map);

			//
			// Step 2 - Set up our army based on the received unit positions
			//
			ExplorePlayer myExplorePlayer = PlayerRegistry.getPlayer (commander.getPlayer ( ).getName ( ));
			LocalPlayer myLocalPlayer = new LocalPlayer (myExplorePlayer);

			this.gameEngine.addPlayerArmy (myLocalPlayer, commander.getHovercraftCount ( ),
												 	 commander.getTankCount ( ), commander.getArtilleryCount ( ));

			//
			// Add ourselves to the battleEngine engine
			//
			this.gameEngine.addPlayer (myLocalPlayer);
			this.gameEngine.setUser (myLocalPlayer);

			//
			// Step 3 - Set the units' positions based on the data we received
			// from the server.-
			//
			int[][] unitCoordinates = this.battleClient.getUnitCoordinates ( );

			this.gameEngine.setupPlayerUnitPositions (myLocalPlayer, unitCoordinates);

			//
			// Step 4 - Start the client TCP thread to receive important
			// in-battle information from the server
			//
			this.battleClient.startTCPThread ( );

			try
			{
				//
				// Step 5 - Receive unit data for other players' units, until
				// START signal received
				//
				this.battleClient.receiveOpponentData ( );

				//
				// Step 6 - Check if we received enemy player's data from the
				// server.-
				//
				if (battleClient.getEnemyPlayerID ( ) == -1)
				{
					//
					// We will fight against AI-driven units ...
					// But first we must be sure that we are attacking!
					//
					if (myExplorePlayer.getId ( ) != flag.getOwner ( ).getId ( ))
					{
						//
						// We are attacking ... so we'll fight against AI!
						//
						ExplorePlayer aiEnemy = PlayerRegistry.getPlayer (flag.getOwner ( ).getId ( ));

						//
						// Set up an AI-driven army based on the server data,
						// because we lost connection to the
						// server.-
						//
						AIPlayer aiPlayer = this.gameEngine.addAiArmy (aiEnemy, flag.getNumHovercraft ( ), 
																					  			  flag.getNumTanks ( ), flag.getNumArtillery ( ));

						//
						// Randomly set the unit positions (0 ... 2)
						//
						int randomAreaX = (int) Math.random ( ) * 3;
						int randomAreaY = (int) Math.random ( ) * 3;

						this.gameEngine.setupRandomAIPlayerUnitPositions (aiPlayer, randomAreaX, randomAreaY);

						//
						// Step 7 - Show the battle field
						//
						return (true);
					}
					else
					{
						//
						// We are defending ... bad luck ... no battle is possible with no data about the enemy.
						//
						Message msg = this.messageHandler.obtainMessage (BattleActivity.SHOW_MESSAGE, "Join to battle did not complete ... :-(");
						msg.sendToTarget ( );
						
						return (false);
					}
				}
				else
				{
					//
					// We will fight against a human player ...
					//
					
					//
					// Step 7 - Fire off threads that deal with multiplayer in-battle updates
					//
					this.battleClient.startUDPThreads ( );
					
					return (true);
				}
			}
			catch (IOException e)
			{
				//
				// The connection was lost while waiting for the battle to begin ...
				// We'll try to start the battle anyway with AI-driven units.-
				//
				if (myExplorePlayer.getId ( ) != flag.getOwner ( ).getId ( ))
				{
					//
					// We are attacking ... so we'll fight against AI!
					//
					ExplorePlayer aiEnemy = PlayerRegistry.getPlayer (flag.getOwner ( ).getId ( ));

					//
					// Set up an AI-driven army based on the server data,
					// because we lost connection to the
					// server.-
					//
					AIPlayer aiPlayer = this.gameEngine.addAiArmy (aiEnemy, flag.getNumHovercraft ( ), 
																				  flag.getNumTanks ( ), flag.getNumArtillery ( ));

					//
					// Randomly set the unit positions (0 ... 2)
					//
					int randomAreaX = (int) Math.random ( ) * 3;
					int randomAreaY = (int) Math.random ( ) * 3;

					this.gameEngine.setupRandomAIPlayerUnitPositions (aiPlayer, randomAreaX, randomAreaY);

					//
					// Step 7 - After START signal has been received from the
					// server, switch the GUI to battle mode
					//
					return (true);
				}
				else
				{
					//
					// We are defending ... bad luck ... no battle is possible.
					// We will have to wait for the result of the battle
					// happening on attacker's phone.
					//
					Message msg = this.messageHandler.obtainMessage (BattleActivity.SHOW_MESSAGE, "Connection to server lost!");
					msg.sendToTarget ( );

					//
					// Debug only!
					//
					if (Debug.LUCAS)
					{
						Log.e (Debug.TAG, "*** ERROR: Connection lost while waiting for the battle to begin.");
					}
					
					return (false);
				}
			}
		}
		else
		{
			//
			// Joining to server failed
			//
			Message msg = this.messageHandler.obtainMessage (BattleActivity.SHOW_MESSAGE, "Connection to server lost!");
			msg.sendToTarget ( );

			//
			// Debug only!
			//
			if (Debug.LUCAS)
			{
				Log.e (Debug.TAG, "*** ERROR: Join to battle FAILED");
			}
			
			return (false);
		}
	}

	
	/**
	 * Displays a special view containing the results of the battle.-
	 * 
	 * @param winner
	 */
	private void showBattleResult (BattlePlayer winner)
	{
		final Intent intent = this.getIntent ( );
		
		BattlePlayer user = this.gameEngine.getUser ( );
		// because there are only 2 players its id
		BattlePlayer enemy = this.gameEngine.getEnemyUser ( );

		final Bundle bundle = intent.getExtras ( );
		bundle.putBoolean ("user_won", winner == user ? true : false);

		bundle.putIntArray ("user_units", user.getUnitsArray ( ));
		bundle.putIntArray ("enemy_units", enemy.getUnitsArray ( ));

		String msg = "";
		String title = "";
		int imgId = 0;

		if (this.gameEngine.getUser ( ) == winner)
		{
			imgId = R.drawable.victory;

			title = "Victory!";
			
			//
			// user won
			//
			if (!intent.getExtras ( ).getBoolean ("practice"))
			{
				Flag flagUnderAttack = BundleUtils.getFlag (intent.getExtras ( ));
				Commander commanderAttacking = BundleUtils.getCommander (intent.getExtras ( ));

				BundleUtils.putFlag (flagUnderAttack, bundle);
				BundleUtils.putCommander (commanderAttacking, bundle);

				//
				// Is the user defending flag ?
				//
				ExplorePlayer player = PlayerRegistry.getPlayer (winner.getName ( ));

				if (flagUnderAttack.getOwner ( ) == player)
				{
					//
					// User won defending battle
					//
					msg = "Congratulations " + player.getRank ( ) + " " + player.getName ( ) + "! The attacking forces have been repelled.";
				}
				else
				{
					//
					// User won attacking battle
					//
					msg = "Congratulations " + player.getRank ( ) + " " + player.getName ( ) + "! The military base is now under your control.";
				}
			}
			else
			{
				msg = "Congratulations soldier!";
			}

		}
		else
		{
			imgId = R.drawable.defeat;

			title = "Defeat!";

			// human defeated
			if (!practice)
			{
				Flag flagUnderAttack = BundleUtils.getFlag (intent.getExtras ( ));
				Commander commanderAttacking = BundleUtils.getCommander (intent.getExtras ( ));

				BundleUtils.putFlag (flagUnderAttack, bundle);
				BundleUtils.putCommander (commanderAttacking, bundle);

				//
				// Is the user defending the flag?
				//
				ExplorePlayer player = PlayerRegistry.getPlayer (this.gameEngine.getUser ( ).getName ( ));

				if (flagUnderAttack.getOwner ( ) == player)
				{
					//
					// User lost defending battle
					//
					msg = player.getRank ( ) + " " + player.getName ( ) + ", you lost your base.";
				}
				else
				{
					//
					// User attacking the flag
					//
					msg = player.getRank ( ) + " " + player.getName ( ) + ", your attempt on the enemy base was unsuccessful.";
				}
			}
			else
			{
				msg = "Keep practicing, soldier!";
			}

		}

		//
		// Don't know how to implement this
		//
		//final String intentMsg = msg;

		// show end battle view
		this.setContentView (R.layout.battle_end);

		// set title
		((TextView) this.findViewById (R.id.txt_battle_end_title)).setText (title);
		// set message
		((TextView) this.findViewById (R.id.txt_battle_end)).setText (msg);
		// set image
		//((ImageView) this.battleModeActivity.findViewById (R.id.img_battle_end)).setBackground (imgId);
		((ImageView) this.findViewById (R.id.img_battle_end)).setBackgroundResource (imgId);

		// set button listener to finish battle mode activity
		((Button) this.findViewById (R.id.btn_battle_end_ok)).setOnClickListener (new OnClickListener ( ) {

			public void onClick (View arg0)
			{
				intent.putExtras (bundle);
				
				BattleActivity.this.setResult (Activity.RESULT_OK, intent);
				BattleActivity.this.finish ( );
			}

		});
	}
	
		
	
	/**
	 * Constructor
	 */
	public BattleActivity ( )
	{
		this.gameEngine = new BattleEngine (this.messageHandler);
		this.gameThread = new BattleThread (this.gameEngine);
	}
	

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);

		//
		// Set a full screen with no title 
		//
		this.requestWindowFeature (Window.FEATURE_NO_TITLE);
		
		//
		// We use the accelerometer, if available 
		//
		this.sensorManager = (SensorManager) this.getSystemService (Context.SENSOR_SERVICE);
		if (this.sensorManager == null)
		{
			this.showMessage ("Accelerometer not available. Use cursor keys to move the screen.");
		}
		else
		{
			//
			// Create the listener that will react to the accelerometer
			//
			this.sensorListener = new AccelerometerListener ( );
		}

		//
		// Tell system to use the layout defined in our XML file
		//
      this.setContentView (R.layout.battle_activity);
      
      //
      // Set pointers to the game and text view
      //
      this.gameView 	   = (GameView) this.findViewById (R.id.game_view);      
      this.gameViewText = (TextView) this.findViewById (R.id.game_view_text);
      
      //
      // Save the pointer to the message handler of this activity (used to start a battle from the GameView)
      //
      this.gameView.setMessageHandler (this.messageHandler);
      
		//
		// Read information received from the calling activity
		//
		Intent intent = this.getIntent ( );

		this.practice 		= intent.getExtras ( ).getBoolean  ("practice");
		this.player1Units = intent.getExtras ( ).getIntArray ("player1_units");
		this.player2Units = intent.getExtras ( ).getIntArray ("player2_units");
	}

	
	
	/***************************************************************************
	 * Methods that handle the activity life cycle.-
	 ***************************************************************************/
	@Override
	protected void onPause ( )
	{
		Log.v ("IU", "Battle activity paused");

		this.finishBattle ( );
		super.onPause ( );
	}

	
	@Override
	protected void onResume ( )
	{
		Log.v ("IU", "Battler activity resumed.");
		
		super.onResume ( );

		if (this.sensorManager != null)
		{
			this.sensorManager.registerListener (this.sensorListener, 
															 SensorManager.SENSOR_ACCELEROMETER,
															 SensorManager.SENSOR_DELAY_GAME);
		}
	}

	
	@Override
	protected void onStop ( )
	{
		Log.v ("IU", "Battle activity stopped");

		if (this.sensorManager != null)
		{
			this.sensorManager.unregisterListener (this.sensorListener);
		}
		
		this.finishBattle ( );
		super.onStop ( );
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy ( )
	{
		Log.v ("IU", "Battle activity destroyed");
		
		this.finishBattle ( );
		super.onDestroy ( );
	}
}
