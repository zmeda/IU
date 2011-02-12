package iu.android.explore;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class ExploreView extends MapView
{

	MapController	mapController;
	ExploreMode		exploreMode;


	/**
	 * Constructor
	 * 
	 * @param context
	 * @param attrs
	 */
	public ExploreView (Context context, AttributeSet attrs)
	{
		super (context, attrs);

		// Get reference to activity
		this.exploreMode = (ExploreMode) context;

		// MapController is capable of zooming and animating and stuff like that
		this.mapController = this.getController ( );

		// Set initial zoom (19 == about 500x500 meters on the screen)
		this.mapController.setZoom (18);


		// Turn off traffic
		if (this.isTraffic ( ))
		{
			this.setTraffic (true);
		}

		// this.setFocusType (View.WEAK_FOCUS);

		this.setFocusable (false);
		
		this.preLoad ( );
	}


	/*
	 * @Override public boolean onMotionEvent (MotionEvent event) { switch (event.getAction ( )) { case
	 * MotionEvent.ACTION_DOWN: return false; case MotionEvent.ACTION_UP: return false; case
	 * MotionEvent.ACTION_MOVE: return false; case MotionEvent.ACTION_CANCEL: return false; }
	 * 
	 * return false; }
	 */

	public void scrollView (int xDir, int yDir)
	{
		if (!this.exploreMode.locationUpdater.isFollowMode ( ))
		{
			GeoPoint center = this.getMapCenter ( );

			// Move the view 1/5 of the lat/lon span of the screen
			GeoPoint newCenter = new GeoPoint (center.getLatitudeE6 ( ) + yDir * this.getLatitudeSpan ( ) / 5,
					center.getLongitudeE6 ( ) + xDir * this.getLongitudeSpan ( ) / 5);

			this.mapController.animateTo (newCenter);
		}
	}
}
