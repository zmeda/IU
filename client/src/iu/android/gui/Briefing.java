package iu.android.gui;

import iu.android.R;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

/**
 * This class displays a tutorial based on the activity that calls.-
 * 
 * @author Lucas
 */
public class Briefing extends Activity implements OnClickListener
{
	public static final int													TUTORIAL_REQUEST_CODE		= 1111;

	//
	// Constants that define the tutorial context
	//
	public static final int													ExploreModeBriefing			= 1;
	public static final int													BattleModeBriefing			= 2;
	public static final int													BattlePreparationBriefing	= 3;

	private static final HashMap<Integer, ArrayList<Integer>>	sub_views;

	static
	{
		sub_views = new HashMap<Integer, ArrayList<Integer>> ( );

		ArrayList<Integer> exploreMode = new ArrayList<Integer> ( );
		exploreMode.add (R.id.brief_page1);
		exploreMode.add (R.id.brief_page2);
		exploreMode.add (R.id.brief_page3);
		exploreMode.add (R.id.brief_page4);
		sub_views.put (ExploreModeBriefing, exploreMode);

		ArrayList<Integer> battleMode = new ArrayList<Integer> ( );
		battleMode.add (R.id.brief_page6);
		battleMode.add (R.id.brief_page7);
		battleMode.add (R.id.brief_page8);
		sub_views.put (BattleModeBriefing, battleMode);

		ArrayList<Integer> battlePreparation = new ArrayList<Integer> ( );
		battlePreparation.add (R.id.brief_page5);
		sub_views.put (BattlePreparationBriefing, battlePreparation);
	}

	/** Index of currently displayed View in the ViewFlipper */
	protected int																pageIndex;
	private ArrayList<Integer>												views;

	/** Mode */
	private int																	mode;

	Button																		wButtonMore;
	Button																		wButtonClose;
	ViewFlipper																	wFlipper;


	@Override
	protected void onCreate (Bundle icicle)
	{
		this.requestWindowFeature (Window.FEATURE_NO_TITLE);

		this.setContentView (R.layout.briefing);
		//this.getWindow ( ).setFlags (WindowManager.LayoutParams.NO_STATUS_BAR_FLAG, WindowManager.LayoutParams.NO_STATUS_BAR_FLAG);
		this.getWindow ( ).setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		this.wButtonMore = (Button) this.findViewById (R.id.briefing_more);
		this.wButtonClose = (Button) this.findViewById (R.id.briefing_close);
		this.wFlipper = (ViewFlipper) this.findViewById (R.id.briefing_switcher);
		
		this.pageIndex = 0;
		this.mode = this.getIntent ( ).getExtras ( ).getInt ("tutorial_context");

		this.init ( );
		this.initText ( );

		super.onCreate (icicle);
	}


	/**
	 * Initialize the GUI
	 */
	private void init ( )
	{
		this.wButtonMore.setOnClickListener (this);
		this.wButtonClose.setOnClickListener (this);
	}


	/**
	 * Initialize the text based on the caller
	 */
	private void initText ( )
	{
		this.wButtonMore.setVisibility (View.VISIBLE);
		this.views = sub_views.get (this.mode);
		this.update ( );
	}


	private void update ( )
	{
		int viewId = this.views.get (this.pageIndex);
		this.pageIndex++;
		
		if (this.pageIndex >= this.views.size ( )) 
		{
			this.wButtonMore.setVisibility (View.INVISIBLE);
		}
		
		while (this.findViewById (viewId) != this.wFlipper.getCurrentView ( )) {
			this.wFlipper.showNext ( );
		}
	}


	/**
	 * Click listener
	 */
	public void onClick (final View v)
	{
		if (v == this.wButtonMore)
		{
			this.update ( );
		}
		else if (v == this.wButtonClose)
		{
			//this.setResult (Activity.RESULT_CANCELED, null, null);
			this.setResult (Activity.RESULT_CANCELED);
			this.finish ( );
		}
	}
}
