package iu.android.explore;

import com.google.android.maps.GeoPoint;

import iu.android.R;
import iu.android.engine.PlayerRegistry;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;

public class ExplorePlayer extends Player
{
	public static final int			PASSIVE_COMPUTER_PLAYER	= 0;
	public static final int			ACTIVE_PLAYER				= 0;
	
	public static final int 		HalfInsigniaWidth = 10;
	public static final int 		HalfInsigniaHeight = 15;
	public static final int			HalfTentWidth = 16;
	public static final int			HalfTentHeight = 16;
	
	public static final long		BlinkTime = 3000;
	public static final int			Blinks = 12;
	public static final double		BlinkRate = Blinks * Math.PI;
	
	private static Drawable tent;

	private static final String	DEFAULT_NAME			= "Unknown Player";
	private static final Rank		DEFAULT_RANK			= Rank.PVT;
	private static final Location	DEFAULT_LOCATON		= GeoLocationOverlay.MAX_LOCATION;

	public static final int[]		default_colors			= {0xffff0000, 0xff00ff00, 0xff0000ff, 0xffffff00, 0xffff00ff, 0xff00ffff, 0xffffffff};

	
	public static void initTent (Context c) {
		tent = c.getResources ( ).getDrawable (R.drawable.camp);
	}
	
	
	private int						id;
	private String					name;

	private final byte				playerType;
	private int						color;
	private Rank					rank;
	private long					lastPromotion = 0;
	private boolean					promoted = false;
	
	private boolean 					camp;
	
	private boolean					active						= false;

	// private Location location;

	Location						mapLocation;
	GeoPoint						mapPoint;

	Location						lastLocation;
	GeoPoint						lastPoint;

	GeoPoint						frontPoint;


	public ExplorePlayer (final int id)
	{
		this (id, ExplorePlayer.DEFAULT_NAME, DEFAULT_RANK);
		this.setLocation (ExplorePlayer.DEFAULT_LOCATON);
	}


	public ExplorePlayer (final int id, final String name, final Rank rank)
	{
		super(name);
		this.id = id;
		this.name = name;
		this.rank = rank;

		if (id == 0)
		{
			// AI is green
			this.color = 0xff00ff00;
		}
		else
		{
			// Others are red
			this.color = 0xffff0000;
		}

		this.frontPoint = this.mapPoint;

		// TODO - Passive single-flag computer players also exist
		this.playerType = ExplorePlayer.ACTIVE_PLAYER;

		PlayerRegistry.addPlayer (this);
	}


	public void setLocation (final Location loc)
	{
		this.lastLocation = this.mapLocation;
		this.lastPoint = this.mapPoint;

		this.mapLocation = loc;
		this.mapPoint = new GeoPoint ((int) (loc.getLatitude ( ) * 1E6), (int) (loc.getLongitude ( ) * 1E6));
	}


	public int getId ( )
	{
		return this.id;
	}


	public void setId (final byte id)
	{
		this.id = id;
	}


	public String getName ( )
	{
		return this.name;
	}


	public void setName (final String name)
	{
		this.name = name;
	}


	public int getColor ( )
	{
		return this.color;
	}


	public void setColor (final int color)
	{
		this.color = color;
	}


	@Override
	public String toString ( )
	{
		// TODO Auto-generated method stub
		return "Player[" + this.id + "] = (" + this.name + "," + Integer.toHexString (this.color) + ")";
	}


	/**
	 * @return the playerType
	 */
	public byte getPlayerType ( )
	{
		return this.playerType;
	}


	public boolean isActive ( )
	{
		return this.active;
	}


	public void setActive (boolean active)
	{
		this.active = active;
	}


	public Rank getRank() {
		return rank;
	}


	public void setRank(Rank rank) 
	{
		this.rank = rank;
		this.promoted = true;
		this.lastPromotion = System.currentTimeMillis();
	}
	
	
	////
	// Static stuff for painting
	
	private static final Paint cmdBackgroundPaint;
	////
	// Static stuff for painting
	
	private static final Paint cmdOutlinePaint;
	private static final Rect				cmdBounds;
	private static final Rect				tentBounds;
	
	static {
		cmdBackgroundPaint = new Paint ( );
		cmdBackgroundPaint.setStyle (Style.FILL);
		cmdOutlinePaint = new Paint ( );
		cmdOutlinePaint.setStyle (Style.STROKE);
		cmdOutlinePaint.setColor (Color.BLACK);
		cmdOutlinePaint.setStrokeWidth (1.0f);
		cmdBounds = new Rect ( );
		tentBounds = new Rect ( );
	}
	
	public void paint (Canvas canvas, int x, int y, int color) {
		
		double h = HalfInsigniaHeight;
		double w = HalfInsigniaWidth;
		
		if (this.promoted) {
			float timeLeft = BlinkTime - System.currentTimeMillis() + this.lastPromotion;
			if (timeLeft < 0) {
				this.promoted = false;
			}
			else {
				double d = timeLeft / BlinkTime * BlinkRate;
				
				// float scale = 1 + (Math.abs((timeLeft % BlinkRate) - (BlinkRate/2)) * (BlinkRate));
				
				double scale = 2.0f + (Math.sin(d));
				h = h * scale;
				w = w * scale;
			}
		}
		
		cmdBounds.top = (int) (y - h);
		cmdBounds.bottom = (int) (y + h);
		cmdBounds.left = (int) (x - w);
		cmdBounds.right = (int) (x + w);
		
		if (!this.promoted && !this.camp) {
			// Background
			cmdBackgroundPaint.setColor(color);
			canvas.drawRect (cmdBounds, cmdBackgroundPaint);
		}
		
		// Camp
		if (this.camp) {
			h = HalfTentHeight;
			w = HalfTentWidth;
			int dy = 5;
			int dx = -8;
			
			tentBounds.top = (int) (y - h + dy);
			tentBounds.bottom = (int) (y + h + dy);
			tentBounds.left = (int) (x - w + dx);
			tentBounds.right= (int) (x + w + dx);
			tent.setBounds (tentBounds);
			tent.draw (canvas);
		}
		
		
		// Rank
		this.rank.insignia.setBounds (cmdBounds);
		this.rank.insignia.draw (canvas);
		
		// Outline
		if (!this.promoted && !this.camp) {
			cmdBounds.top--;
			cmdBounds.left--;
			canvas.drawRect (cmdBounds, cmdOutlinePaint);
		}
	}


	public void setUpCamp (boolean camp)
	{
		this.camp = camp;
	}
	
	
	public boolean isCamping ( )
	{
		return (this.camp);
	}
}
