package iu.android.explore;

import java.io.IOException;

import iu.android.R;
import iu.android.battle.BattleActivity;
import iu.android.comm.BundleUtils;
import iu.android.explore.event.EventBattleStarted;
import iu.android.gui.Briefing;
import iu.android.network.NetworkResources;
import iu.android.network.explore.ExploreClient;
import iu.android.network.explore.ReceiverThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BattlePreparation extends Activity implements OnClickListener
{

	public static final int		BATTLE_PREP_REQUEST_CODE	= 333;

	protected static final int UPDATE_REQUEST = 1;

	protected static final int START_TIMER = 2;
	protected static final int UPDATE_TIMER = 3;
	protected static final int STOP_TIMER = 4;

	protected static final int COMMUNICATION_FAILED = 5;

	protected static final int START_BATTLE_MODE = 6;
	
	
	Commander					commander;
	Flag							flag;

	View							battleprepView;

	TextView txt_hover_flag;
	TextView txt_hover_cmd;
	TextView txt_tank_flag;
	TextView txt_tank_cmd;
	TextView txt_artillery_flag;
	TextView txt_artillery_cmd;

	private Button btn_attack;
	private Button btn_cancel;

	private TextView			txt_title;

	private TextView txt_notify;
	private boolean tutorialMode;


	@Override
	protected void onCreate (final Bundle icicle)
	{
		super.onCreate (icicle);

		this.requestWindowFeature (Window.FEATURE_NO_TITLE);

		//this.getWindow ( ).setFlags (WindowManager.LayoutParams.NO_STATUS_BAR_FLAG, WindowManager.LayoutParams.NO_STATUS_BAR_FLAG);
		this.getWindow ( ).setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		

		// Show the map view
		this.setContentView (R.layout.battle_preparation);

		
		this.tutorialMode = this.getIntent ( ).getExtras ( ).getBoolean ("tutorial_mode");
		//
		// Check if tutorial mode is on
		//
		if (this.tutorialMode)
		{
			Intent i = new Intent (this, Briefing.class);
			
			i.putExtra 				 ("tutorial_context", Briefing.BattlePreparationBriefing);
			//this.startSubActivity (i, Briefing.TUTORIAL_REQUEST_CODE);
			this.startActivity (i);
		}
		
		this.init ( );

		this.initData ( );
		
		this.initWaitingThread();

	}


	protected void init ( )
	{
		// Set background image
		((ImageView)this.findViewById(R.id.img_attack)).setBackgroundResource (R.drawable.attack);

		//
		// set title
		//
		this.txt_title = (TextView) this.findViewById (R.id.battleprep_title);
		
		((TextView) this.findViewById (R.id.battleprep_col1_title)).setText (" Unit");
		((TextView) this.findViewById (R.id.battleprep_col2_title)).setText ("Base");
		((TextView) this.findViewById (R.id.battleprep_col3_title)).setText ("Commander");

		((TextView) this.findViewById (R.id.battleprep_1_1)).setText (" Hover");
		((TextView) this.findViewById (R.id.battleprep_2_1)).setText (" Tank");
		((TextView) this.findViewById (R.id.battleprep_3_1)).setText (" Artillery");

		//
		// TextView - for showing actual number of units on flag and commander
		//
		this.txt_hover_flag = (TextView) this.findViewById (R.id.battleprep_1_2);
		this.txt_hover_cmd = (TextView) this.findViewById (R.id.battleprep_1_3);

		this.txt_tank_flag = (TextView) this.findViewById (R.id.battleprep_2_2);
		this.txt_tank_cmd = (TextView) this.findViewById (R.id.battleprep_2_3);

		this.txt_artillery_flag = (TextView) this.findViewById (R.id.battleprep_3_2);
		this.txt_artillery_cmd = (TextView) this.findViewById (R.id.battleprep_3_3);

		this.txt_notify = (TextView) this.findViewById (R.id.txt_notify);
		
		this.btn_attack = (Button) this.findViewById (R.id.battleprep_btn_attack);
		this.btn_cancel = (Button) this.findViewById (R.id.battleprep_btn_cancel);

		//
		// register listeners
		//
		this.btn_attack.setOnClickListener (this);
		this.btn_cancel.setOnClickListener (this);
	}


	/**
	 * Read data from intent
	 */
	private void initData ( )
	{
		Intent intent = this.getIntent ( );

		//
		// with help of intent utils new instances of Commander and Flag are created from Intent
		//

		this.commander = BundleUtils.getCommander (intent.getExtras ( ));
		this.flag = BundleUtils.getFlag (intent.getExtras ( ));

		Log.d ("IU", "Commander name = "+this.commander.getPlayer().getName());
		this.txt_title.setText (" "+this.flag.getOwner().getName()+ " - Military base: " + this.flag.getFlagId ( ));


		this.update ( );
	}

	private void initWaitingThread ()
	{
		flag.unitsUpdated = false;
		
		Thread t = new Thread (new Runnable() {
			public void run() 
			{
				Flag flag = BattlePreparation.this.flag;
				
				flag.unitsUpdated = false;
				
				ExploreClient client = NetworkResources.getClient();
				
				try 
				{
					client.getSender().getFlagUnits(flag.getFlagId());
				} catch (IOException e) {
					// signal error to user
				}
				
				//
				// wait for server to respond
				//
				while (!flag.unitsUpdated)
				{
					try 
					{
						Thread.sleep(200);
					} 
					catch (InterruptedException e) 
					{
						//
					}
				}
				
				Message m = new Message();
				m.what = BattlePreparation.UPDATE_REQUEST;
				BattlePreparation.this.handler.sendMessage(m);
				
			}
		});
		
		t.start();
	}
	

	private void update ( )
	{
		 
		this.txt_hover_cmd.setText ("" + this.commander.getHovercraftCount ( ));
		this.txt_tank_cmd.setText ("" + this.commander.getTankCount ( ));
		this.txt_artillery_cmd.setText ("" + this.commander.getArtilleryCount ( ));

		if (this.flag.unitsUpdated)
		{
			this.txt_hover_flag.setText ("" + this.flag.getNumHovercraft ( ));
			this.txt_tank_flag.setText ("" + this.flag.getNumTanks ( ));
			this.txt_artillery_flag.setText ("" + this.flag.getNumArtillery ( ));
			
			this.txt_notify.setText("");
		}
		else
		{
			this.txt_hover_flag.setText	 	("???");
			this.txt_tank_flag.setText  	("???");
			this.txt_artillery_flag.setText ("???");
			
			this.txt_notify.setText("Waiting for data ...");
		}

	}


	public void onClick (final View v)
	{

		if (v == this.btn_attack)
		{
			this.attackRequest ( );
		}
		else if (v == this.btn_cancel)
		{
			this.cancel ( );
		}
	}


	private void cancel ( )
	{
		this.setResult (Activity.RESULT_CANCELED);
		this.finish ( );
	}


	private void attackRequest ( )
	{
		//
		// If any squad have 0 units don't allow battle
		//
		if (this.commander.getArtilleryCount()==0 || this.commander.getTankCount() == 0 || this.commander.getArtilleryCount() == 0)
		{
			AlertDialog.Builder errorDialog = new AlertDialog.Builder (BattlePreparation.this);

			errorDialog.setIcon (0);
			errorDialog.setTitle ("Attack is not possible");
			errorDialog.setPositiveButton ("ok", null);
			errorDialog.setMessage ("Go back and join at least one unit of each type.");
			errorDialog.show ( );
			
			return;
		}
		
		if (this.tutorialMode)
		{
			Intent i = new Intent (this, Briefing.class);
			
			i.putExtra 				 ("tutorial_context", Briefing.BattleModeBriefing);
			//this.startSubActivity (i, Briefing.TUTORIAL_REQUEST_CODE);
			this.startActivity (i);
		}
		
		this.btn_attack.setVisibility(View.INVISIBLE);
		this.btn_cancel.setVisibility(View.INVISIBLE);
		
		
		// 1. send attack command
		Thread t = new Thread (new Runnable() {
			public void run() 
			{
				Flag flag = BattlePreparation.this.flag;
				
				EventBattleStarted ebs = null;
				
				ExploreClient client = NetworkResources.getClient();
				
				try 
				{
					client.getSender().attackFlag(flag.getFlagId(), 
							BattlePreparation.this.commander.getHovercraftCount ( ), 
							BattlePreparation.this.commander.getTankCount(), 
							BattlePreparation.this.commander.getArtilleryCount());
					
					ReceiverThread receiver = client.getReceiverThread();
					
					Thread timer = new Thread (new Runnable() {
						
						public void run() 
						{
							// update notify 
							Message m = new Message();
							m.what = BattlePreparation.START_TIMER;
							BattlePreparation.this.handler.sendMessage(m);
							
							long start = System.currentTimeMillis();
							
							while ((System.currentTimeMillis() < start + 35000))
							{
								m = new Message();
								m.what = BattlePreparation.UPDATE_TIMER;
								BattlePreparation.this.handler.sendMessage(m);
								
								try 
								{
									Thread.sleep(1000);
								} 
								catch (InterruptedException e) 
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							m = new Message();
							m.what = BattlePreparation.STOP_TIMER;
							BattlePreparation.this.handler.sendMessage(m);
						}
					});
					
					timer.start();
					
					ebs = receiver.waitForEventBattleStarted ();
					
					timer.stop();
					
				} catch (IOException e) 
				{
					// signal error to user
				}
				

				if (ebs == null)
				{
					BattlePreparation.this.handler.sendEmptyMessage(BattlePreparation.COMMUNICATION_FAILED);
					
				}
				else
				{
					Message m = new Message();
					m.obj = ebs;
					m.what = BattlePreparation.START_BATTLE_MODE;
					
					BattlePreparation.this.handler.sendMessage(m);
				}
			}
		});
		
		t.start ( );		
	}

	
	protected void communicationFailed ( ) 
	{
		this.txt_notify.setText("Communication failed ... try again");
	}
	
	
	protected void startBattleMode( EventBattleStarted ebs) 
	{
		this.txt_notify.setText("Communication established");
		
		Intent intent = new Intent (this, BattleActivity.class);
		
		Bundle bundle = new Bundle( );
		BundleUtils.putFlag (this.flag, bundle);
		BundleUtils.putCommander (this.commander, bundle);
		BundleUtils.putTCPPort (ebs.tcpPort, bundle);

		intent.putExtras (bundle);
		
		this.setResult (Activity.RESULT_OK, intent);
		this.finish ( );
	}
	
	
	
	
	Handler			handler	= new Handler ( ) {
										// @Override
										public void handleMessage (Message msg)
										{
											switch (msg.what)
											{
												case BattlePreparation.UPDATE_REQUEST:

													BattlePreparation.this.update ( );
													break;

												case BattlePreparation.COMMUNICATION_FAILED:

													BattlePreparation.this.communicationFailed ( );
													break;

												case BattlePreparation.START_BATTLE_MODE:

													BattlePreparation.this.startBattleMode ((EventBattleStarted) msg.obj);
													break;

												case BattlePreparation.START_TIMER:

													BattlePreparation.this.startTimer ( );
													break;

												case BattlePreparation.UPDATE_TIMER:
													BattlePreparation.this.updateTimer ( );
													break;

												case BattlePreparation.STOP_TIMER:
													BattlePreparation.this.stopTimer ( );
													break;

											}
											super.handleMessage (msg);
										}
									};

   //
   // Methods for updating time when waiting for start battle event
   //
	
   private long startTime;


	protected void startTimer() 
	{
		this.startTime = System.currentTimeMillis();
		
	}

	protected void stopTimer() 
	{
		this.txt_notify.setText("Connected");
	}


	protected void updateTimer() 
	{
		this.txt_notify.setText("Waiting for enemy ...\n "+(35-(int)(System.currentTimeMillis()-this.startTime)/1000)+"s");
	} 

}
