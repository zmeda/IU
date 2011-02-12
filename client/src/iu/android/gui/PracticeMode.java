package iu.android.gui;

import iu.android.R;
import iu.android.battle.BattleActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PracticeMode extends Activity implements OnClickListener
{
	public static final int	PRACTICE_MODE_REQUEST_CODE	= 999;

	private TextView			txt_hover_p1;
	private TextView			txt_tank_p1;
	private TextView			txt_artillery_p1;
	private TextView			txt_hover_p2;
	private TextView			txt_tank_p2;
	private TextView			txt_artillery_p2;
	private Button				btn_p1_hover_add;
	private Button				btn_p1_hover_remove;
	private Button				btn_p1_tank_add;
	private Button				btn_p1_tank_remove;
	private Button				btn_p1_artillery_add;
	private Button				btn_p1_artillery_remove;
	private Button				btn_p2_hover_add;
	private Button				btn_p2_hover_remove;
	private Button				btn_p2_tank_add;
	private Button				btn_p2_tank_remove;
	private Button				btn_p2_artillery_add;
	private Button				btn_p2_artillery_remove;
	private Button				btn_fight;
	private Button				btn_back;

	private int					hoverP1							= 3;
	private int					tankP1							= 2;
	private int					artilleryP1						= 1;
	private int					hoverP2							= 3;
	private int					tankP2							= 2;
	private int					artilleryP2						= 1;


	@Override
	protected void onCreate (Bundle icicle)
	{
		super.onCreate (icicle);

		this.requestWindowFeature (Window.FEATURE_NO_TITLE);

		// Show the GUI
		this.setContentView (R.layout.practice_mode);

		this.init ( );

		this.update ( );
	}


	private void init ( )
	{
		// Set background image
		// ((ImageView) this.findViewById (R.id.img_practice)).setBackground (R.drawable.attack);
		// ((ImageView) this.findViewById (R.layout.practice_mode)).setBackgroundResource (R.drawable.attack);

		//
		// Set title
		//
		//this.txt_title = (TextView) this.findViewById (R.id.txt_practice_title);

		//
		// TextView - player 1
		//
		this.txt_hover_p1 = (TextView) this.findViewById (R.id.txt_practice_p1_hov);

		this.txt_tank_p1 = (TextView) this.findViewById (R.id.txt_practice_p1_tan);

		this.txt_artillery_p1 = (TextView) this.findViewById (R.id.txt_practice_p1_art);

		//
		// TextView - player 2
		//
		this.txt_hover_p2 = (TextView) this.findViewById (R.id.txt_practice_p2_hov);

		this.txt_tank_p2 = (TextView) this.findViewById (R.id.txt_practice_p2_tan);

		this.txt_artillery_p2 = (TextView) this.findViewById (R.id.txt_practice_p2_art);

		//
		// Buttons - for adding and removing units from player 1
		//
		this.btn_p1_hover_add = (Button) this.findViewById (R.id.btn_practice_p1_hov_add);
		this.btn_p1_hover_remove = (Button) this.findViewById (R.id.btn_practice_p1_hov_remove);

		this.btn_p1_tank_add = (Button) this.findViewById (R.id.btn_practice_p1_tan_add);
		this.btn_p1_tank_remove = (Button) this.findViewById (R.id.btn_practice_p1_tan_remove);

		this.btn_p1_artillery_add = (Button) this.findViewById (R.id.btn_practice_p1_art_add);
		this.btn_p1_artillery_remove = (Button) this.findViewById (R.id.btn_practice_p1_art_remove);

		//
		// Buttons - for adding and removing units from player 2
		//
		this.btn_p2_hover_add = (Button) this.findViewById (R.id.btn_practice_p2_hov_add);
		this.btn_p2_hover_remove = (Button) this.findViewById (R.id.btn_practice_p2_hov_remove);

		this.btn_p2_tank_add = (Button) this.findViewById (R.id.btn_practice_p2_tan_add);
		this.btn_p2_tank_remove = (Button) this.findViewById (R.id.btn_practice_p2_tan_remove);

		this.btn_p2_artillery_add = (Button) this.findViewById (R.id.btn_practice_p2_art_add);
		this.btn_p2_artillery_remove = (Button) this.findViewById (R.id.btn_practice_p2_art_remove);

		this.btn_fight = (Button) this.findViewById (R.id.btn_practice_fight);
		this.btn_back = (Button) this.findViewById (R.id.btn_practice_back);

		//
		// register listeners
		//
		this.btn_p1_hover_add.setOnClickListener (this);
		this.btn_p1_hover_remove.setOnClickListener (this);
		this.btn_p1_tank_add.setOnClickListener (this);
		this.btn_p1_tank_remove.setOnClickListener (this);
		this.btn_p1_artillery_add.setOnClickListener (this);
		this.btn_p1_artillery_remove.setOnClickListener (this);

		this.btn_p2_hover_add.setOnClickListener (this);
		this.btn_p2_hover_remove.setOnClickListener (this);
		this.btn_p2_tank_add.setOnClickListener (this);
		this.btn_p2_tank_remove.setOnClickListener (this);
		this.btn_p2_artillery_add.setOnClickListener (this);
		this.btn_p2_artillery_remove.setOnClickListener (this);

		this.btn_fight.setOnClickListener (this);
		this.btn_back.setOnClickListener (this);
	}


	private void update ( )
	{
		this.txt_hover_p1.setText ("" + this.hoverP1);
		this.txt_tank_p1.setText ("" + this.tankP1);
		this.txt_artillery_p1.setText ("" + this.artilleryP1);

		this.txt_hover_p2.setText ("" + this.hoverP2);
		this.txt_tank_p2.setText ("" + this.tankP2);
		this.txt_artillery_p2.setText ("" + this.artilleryP2);
	}


	public void onClick (View v)
	{
		if (v == this.btn_fight)
		{
			this.fight ( );
		}
		else if (v == this.btn_back)
		{
			this.back ( );
		}
		else if (v == this.btn_p1_hover_add)
		{
			if (this.hoverP1 < 8)
			{
				this.hoverP1++;

				this.update ( );
			}
		}
		else if (v == this.btn_p1_tank_add)
		{
			if (this.tankP1 < 5)
			{
				this.tankP1++;

				this.update ( );
			}
		}
		else if (v == this.btn_p1_artillery_add)
		{
			if (this.artilleryP1 < 3)
			{
				this.artilleryP1++;

				this.update ( );
			}
		}
		else if (v == this.btn_p1_hover_remove)
		{
			if (this.hoverP1 > 1)
			{
				this.hoverP1--;

				this.update ( );
			}
		}
		else if (v == this.btn_p1_tank_remove)
		{
			if (this.tankP1 > 1)
			{
				this.tankP1--;

				this.update ( );
			}
		}
		else if (v == this.btn_p1_artillery_remove)
		{
			if (this.artilleryP1 > 1)
			{
				this.artilleryP1--;

				this.update ( );
			}
		}
		else if (v == this.btn_p2_hover_add)
		{
			if (this.hoverP2 < 8)
			{
				this.hoverP2++;

				this.update ( );
			}
		}
		else if (v == this.btn_p2_tank_add)
		{
			if (this.tankP2 < 5)
			{
				this.tankP2++;

				this.update ( );
			}
		}
		else if (v == this.btn_p2_artillery_add)
		{
			if (this.artilleryP2 < 3)
			{
				this.artilleryP2++;
				this.update ( );
			}
		}
		else if (v == this.btn_p2_hover_remove)
		{
			if (this.hoverP2 > 1)
			{
				this.hoverP2--;

				this.update ( );
			}
		}
		else if (v == this.btn_p2_tank_remove)
		{
			if (this.tankP2 > 1)
			{
				this.tankP2--;

				this.update ( );
			}
		}
		else if (v == this.btn_p2_artillery_remove)
		{
			if (this.artilleryP2 > 1)
			{
				this.artilleryP2--;
				this.update ( );
			}
		}
		else
		{
			Log.d ("IU", "Event unknown!");
		}

	}


	private void back ( )
	{
		this.finish ( );

	}


	private void fight ( )
	{
		Intent intent = new Intent (this, BattleActivity.class);

		intent.putExtra ("practice", true);
		intent.putExtra ("player1_units", new int[] {this.hoverP1, this.tankP1, this.artilleryP1});
		intent.putExtra ("player2_units", new int[] {this.hoverP2, this.tankP2, this.artilleryP2});

		this.startActivity (intent);
	}
}
