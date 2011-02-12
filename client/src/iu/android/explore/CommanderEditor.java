package iu.android.explore;

import iu.android.R;
import iu.android.comm.BundleUtils;
import iu.android.network.NetworkResources;
import iu.android.network.explore.ExploreClient;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CommanderEditor extends Activity implements OnClickListener
{

	public static final int		COMMANDER_EDITOR_REQUEST_CODE	= 222;

	private static final int	FlagMaxHover						= 6;
	private static final int	FlagMaxTank							= 3;
	private static final int	FlagMaxArtillery					= 2;

	Commander						commander;
	Flag								flag;

	private int						flagOrigHover;
	private int						flagOrigTank;
	private int						flagOrigArt;
	private int						cmdOrigHover;
	private int						cmdOrigTank;
	private int						cmdOrigArt;

	TextView							txt_hover_flag;
	TextView							txt_hover_cmd;
	TextView							txt_tank_flag;
	TextView							txt_tank_cmd;
	TextView							txt_artillery_flag;
	TextView							txt_artillery_cmd;

	Button							btn_hover_add;
	Button							btn_hover_remove;
	Button							btn_tank_add;
	Button							btn_tank_remove;
	Button							btn_artillery_add;
	Button							btn_artillery_remove;
	Button							btn_ok;
	Button							btn_cancel;

	private TextView				txt_title;

	private Button					btn_reset;


	protected void onCreate (final Bundle icicle)
	{
		super.onCreate (icicle);

		this.requestWindowFeature (Window.FEATURE_NO_TITLE);

		// this.getWindow ( ).setFlags (WindowManager.LayoutParams.NO_STATUS_BAR_FLAG,
		// WindowManager.LayoutParams.NO_STATUS_BAR_FLAG);
		this.getWindow ( ).setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// this.getWindow().setFlags(WindowManager.LayoutParams.NO_STATUS_BAR_FLAG,
		// WindowManager.LayoutParams.NO_STATUS_BAR_FLAG);

		// Show the map view
		this.setContentView (R.layout.commander_editor);

		this.init ( );

		this.initData ( );
	}


	protected void init ( )
	{

		// Set background image
		// ((ImageView)this.findViewById(R.id.img_commander)).setBackground(R.drawable.attack);
		((ImageView) this.findViewById (R.id.img_commander)).setBackgroundResource (R.drawable.attack);

		//
		// set title
		//
		this.txt_title = (TextView) this.findViewById (R.id.cmdeditor_title);
		this.txt_title.setText (" Military Base");

		((TextView) this.findViewById (R.id.cmdeditor_col1_title)).setText (" Unit");
		((TextView) this.findViewById (R.id.cmdeditor_col2_title)).setText ("Flag (#)");
		((TextView) this.findViewById (R.id.cmdeditor_col3_title)).setText ("Commander (#)");

		((TextView) this.findViewById (R.id.cmdeditor_1_1)).setText (" Hover");
		((TextView) this.findViewById (R.id.cmdeditor_2_1)).setText (" Tank");
		((TextView) this.findViewById (R.id.cmdeditor_3_1)).setText (" Artirery");

		//
		// TextView - for showing actual number of units on flag and commander
		//
		this.txt_hover_flag = (TextView) this.findViewById (R.id.cmdeditor_1_2);
		this.txt_hover_cmd = (TextView) this.findViewById (R.id.cmdeditor_1_3);

		this.txt_tank_flag = (TextView) this.findViewById (R.id.cmdeditor_2_2);
		this.txt_tank_cmd = (TextView) this.findViewById (R.id.cmdeditor_2_3);

		this.txt_artillery_flag = (TextView) this.findViewById (R.id.cmdeditor_3_2);
		this.txt_artillery_cmd = (TextView) this.findViewById (R.id.cmdeditor_3_3);

		//
		// Buttons - for adding and removing unit from flag or commander
		//
		this.btn_hover_add = (Button) this.findViewById (R.id.cmdeditor_1_btn_1);
		this.btn_hover_remove = (Button) this.findViewById (R.id.cmdeditor_1_btn_2);

		this.btn_tank_add = (Button) this.findViewById (R.id.cmdeditor_2_btn_1);
		this.btn_tank_remove = (Button) this.findViewById (R.id.cmdeditor_2_btn_2);

		this.btn_artillery_add = (Button) this.findViewById (R.id.cmdeditor_3_btn_1);
		this.btn_artillery_remove = (Button) this.findViewById (R.id.cmdeditor_3_btn_2);

		this.btn_ok = (Button) this.findViewById (R.id.cmdeditor_btn_ok);
		this.btn_cancel = (Button) this.findViewById (R.id.cmdeditor_btn_cancel);
		this.btn_reset = (Button) this.findViewById (R.id.cmdeditor_btn_reset);

		//
		// register listeners
		//
		this.btn_hover_add.setOnClickListener (this);
		this.btn_hover_remove.setOnClickListener (this);
		this.btn_tank_add.setOnClickListener (this);
		this.btn_tank_remove.setOnClickListener (this);
		this.btn_artillery_add.setOnClickListener (this);
		this.btn_artillery_remove.setOnClickListener (this);
		this.btn_ok.setOnClickListener (this);
		this.btn_cancel.setOnClickListener (this);
		this.btn_reset.setOnClickListener (this);

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
		this.cmdOrigHover = this.commander.getHovercraftCount ( );
		this.cmdOrigTank = this.commander.getTankCount ( );
		this.cmdOrigArt = this.commander.getArtilleryCount ( );

		this.flag = BundleUtils.getFlag (intent.getExtras ( ));
		this.flagOrigHover = this.flag.getNumHovercraft ( );
		this.flagOrigTank = this.flag.getNumTanks ( );
		this.flagOrigArt = this.flag.getNumArtillery ( );

		this.txt_title.setText (" " + this.commander.getPlayer ( ).getName ( ) + " - Military base : "
				+ this.flag.getFlagId ( ));

		this.update ( );
	}


	private void reset ( )
	{

		this.commander.setHovercraftCount (this.cmdOrigHover);
		this.commander.setTankCount (this.cmdOrigTank);
		this.commander.setArtilleryCount (this.cmdOrigArt);

		this.flag.setNumHovercraft (this.flagOrigHover);
		this.flag.setNumTanks (this.flagOrigTank);
		this.flag.setNumArtillery (this.flagOrigArt);

		this.update ( );
	}


	private void update ( )
	{
		this.txt_hover_cmd.setText ("" + this.commander.getHovercraftCount ( ));
		this.txt_tank_cmd.setText ("" + this.commander.getTankCount ( ));
		this.txt_artillery_cmd.setText ("" + this.commander.getArtilleryCount ( ));

		this.txt_hover_flag.setText ("" + this.flag.getNumHovercraft ( ));
		this.txt_tank_flag.setText ("" + this.flag.getNumTanks ( ));
		this.txt_artillery_flag.setText ("" + this.flag.getNumArtillery ( ));
	}


	public void onClick (View v)
	{

		if (v == this.btn_hover_add)
		{
			Log.d ("IU", String.valueOf (this.flag.getNumHovercraft ( )));
			if (this.flag.getNumHovercraft ( ) > 1
					|| (this.flag.getNumHovercraft ( ) == 1 && (this.flag.getNumArtillery ( ) > 0 || this.flag
							.getNumTanks ( ) > 0)))
			{
				Log.d ("hover", "pridem not");
				if (this.commander.getHovercraftCount ( ) < this.commander.player.getRank ( ).maxHover)
				{
					this.flag.setNumHovercraft (this.flag.getNumHovercraft ( ) - 1);
					this.commander.setHovercraftCount (this.commander.getHovercraftCount ( ) + 1);

					this.update ( );
				}
				else
				{
					ExploreClient client = NetworkResources.getClient ( );
					this.notify ("Sorry " + client.getPlayerName ( ) + ".\nAs a " + client.getRank ( )
							+ " you may command only " + this.commander.player.getRank ( ).maxHover
							+ " hovercrafts. ");

				}
			}
		}
		else if (v == this.btn_hover_remove)
		{
			if (this.commander.getHovercraftCount ( ) > 0 && this.flag.getNumHovercraft ( ) < FlagMaxHover)
			{
				this.flag.setNumHovercraft (this.flag.getNumHovercraft ( ) + 1);
				this.commander.setHovercraftCount (this.commander.getHovercraftCount ( ) - 1);

				this.update ( );
			}
		}
		else if (v == this.btn_tank_add)
		{
			if (this.flag.getNumTanks ( ) > 1
					|| (this.flag.getNumTanks ( ) == 1 && (this.flag.getNumArtillery ( ) > 0 || this.flag
							.getNumHovercraft ( ) > 0)))
			{
				if (this.commander.getTankCount ( ) < this.commander.player.getRank ( ).maxTank)
				{
					this.flag.setNumTanks (this.flag.getNumTanks ( ) - 1);
					this.commander.setTankCount (this.commander.getTankCount ( ) + 1);

					this.update ( );
				}
				else
				{
					ExploreClient client = NetworkResources.getClient ( );

					this.notify ("Sorry " + client.getPlayerName ( ) + ".\nAs a " + client.getRank ( )
							+ " you may command only " + this.commander.player.getRank ( ).maxTank + " tanks.");

				}
			}
		}
		else if (v == this.btn_tank_remove)
		{
			if (this.commander.getTankCount ( ) > 0 && this.flag.getNumTanks ( ) < FlagMaxTank)
			{
				this.flag.setNumTanks (this.flag.getNumTanks ( ) + 1);
				this.commander.setTankCount (this.commander.getTankCount ( ) - 1);

				this.update ( );
			}
		}
		if (v == this.btn_artillery_add)
		{
			if (this.flag.getNumArtillery ( ) > 1
					|| (this.flag.getNumArtillery ( ) == 1 && (this.flag.getNumHovercraft ( ) > 0 || this.flag
							.getNumTanks ( ) > 0)))
			{
				if (this.commander.getArtilleryCount ( ) < this.commander.player.getRank ( ).maxArtillery)
				{
					this.flag.setNumArtillery (this.flag.getNumArtillery ( ) - 1);
					this.commander.setArtilleryCount (this.commander.getArtilleryCount ( ) + 1);

					this.update ( );
				}
				else
				{
					ExploreClient client = NetworkResources.getClient ( );
					this.notify ("Sorry " + client.getPlayerName ( ) + ".\nAs a " + client.getRank ( )
							+ " you may command only " + this.commander.player.getRank ( ).maxArtillery
							+ " artillery units. ");

				}
			}
		}
		else if (v == this.btn_artillery_remove)
		{
			if (this.commander.getArtilleryCount ( ) > 0 && this.flag.getNumArtillery ( ) < FlagMaxArtillery)
			{
				this.flag.setNumArtillery (this.flag.getNumArtillery ( ) + 1);
				this.commander.setArtilleryCount (this.commander.getArtilleryCount ( ) - 1);

				this.update ( );
			}
		}
		else if (v == this.btn_ok)
		{
			this.ok ( );
		}
		else if (v == this.btn_cancel)
		{
			this.cancel ( );
		}
		else if (v == this.btn_reset)
		{
			this.reset ( );
		}

	}


	private void notify (String msg)
	{
		AlertDialog.Builder sorryDialog = new AlertDialog.Builder (CommanderEditor.this);
		
		sorryDialog.setIcon (0);
		sorryDialog.setTitle ("Commander Message");
		sorryDialog.setMessage (msg);
		sorryDialog.setPositiveButton ("ok", null);
		sorryDialog.show ( );
	}


	private void cancel ( )
	{
		this.reset ( );
		this.setResult (Activity.RESULT_CANCELED);
		this.finish ( );
	}


	private void ok ( )
	{
		//
		// if change has been made return new OK and number of units at flag and commander
		// otherwise cancel operation
		//
		if (this.changed ( ))
		{
			Intent intent = new Intent ( );
			Bundle bundle = new Bundle ( );

			BundleUtils.putFlag 		 (this.flag, bundle);
			BundleUtils.putCommander (this.commander, bundle);

			intent.putExtras (bundle);

			this.setResult (Activity.RESULT_OK, intent);
			this.finish ( );
		}
		else
		{
			this.cancel ( );
		}
	}


	/**
	 * @return true if change has been made otherwise this returns false
	 */
	private boolean changed ( )
	{
		boolean changed = false;
		changed |= this.commander.getHovercraftCount ( ) != this.cmdOrigHover;
		changed |= this.commander.getTankCount ( ) != this.cmdOrigTank;
		changed |= this.commander.getArtilleryCount ( ) != this.cmdOrigArt;
		return changed;
	}
}
