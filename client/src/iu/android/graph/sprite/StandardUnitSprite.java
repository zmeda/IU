package iu.android.graph.sprite;

import iu.android.Debug;
import iu.android.engine.BattlePlayer;
import iu.android.graph.ImageManager;
import iu.android.graph.LitImage;
import iu.android.unit.Squad;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class StandardUnitSprite /* implements */extends UnitSprite
{

	/** Ending of the unit resource name */
	public static final String	ImageTag					= "_unit";
	/** Ending of the units markings resource name */
	public static final String	MarkingsTag				= "_mark";
	/** Ending of the units bump map resource name */
	public static final String	BumpMapTag				= "_map";
	/** Ending of the units bump map resource name */
	public static final String	DamageTag				= "_dmg";

	private static final Paint	HealthRectOutline;
	private static final Paint	HealthRectFill;
	private static final Paint	PlainTextPaint;
	private static final Paint	BoldTextPaint;
	private static final Paint	UnitPaint;
	private static final Paint	DamagePaint;
	private static final Paint	Debug_LocationPaint;

	/** How far from the unit to draw the health bar */
	private static final float	VerticalHealthOffset	= -6;

	/** When units damage is below this value, the visible damage (e.g. flames and smoke) is drawn */
	private static final int	DamageThreshold		= 50;

	static
	{
		HealthRectOutline = new Paint ( );
		StandardUnitSprite.HealthRectOutline.setColor (Color.BLACK);
		StandardUnitSprite.HealthRectOutline.setStyle (Style.STROKE);
		// HealthRectOutline.setAntiAlias (true);

		HealthRectFill = new Paint ( );
		StandardUnitSprite.HealthRectFill.setStyle (Style.FILL);

		// [YARTS] private Font plain = new Font ("monospaced", Font.PLAIN, 12);
		PlainTextPaint = new Paint ( );
		StandardUnitSprite.PlainTextPaint.setTextSize (12);
		StandardUnitSprite.PlainTextPaint.setTypeface (Typeface.create (Typeface.MONOSPACE, Typeface.NORMAL));
		StandardUnitSprite.PlainTextPaint.setTextAlign (Align.LEFT);

		// [YARTS] private Font bold = new Font ("monospaced", Font.BOLD, 16);
		BoldTextPaint = new Paint ( );
		StandardUnitSprite.BoldTextPaint.setTextSize (16);
		StandardUnitSprite.BoldTextPaint.setTypeface (Typeface.create (Typeface.MONOSPACE, Typeface.BOLD));
		StandardUnitSprite.BoldTextPaint.setTextAlign (Align.LEFT);
		StandardUnitSprite.BoldTextPaint.setStyle (Style.FILL_AND_STROKE);
		StandardUnitSprite.BoldTextPaint.setFakeBoldText (true);
		StandardUnitSprite.BoldTextPaint.setAntiAlias (true);

		UnitPaint = new Paint ( );
		StandardUnitSprite.UnitPaint.setAntiAlias (true);

		DamagePaint = new Paint ( );
		StandardUnitSprite.DamagePaint.setAntiAlias (true);
		StandardUnitSprite.DamagePaint.setAlpha (192);

		Debug_LocationPaint = new Paint ( );
		StandardUnitSprite.Debug_LocationPaint.setColor (Color.RED);
		StandardUnitSprite.Debug_LocationPaint.setStrokeWidth (2.0f);
		StandardUnitSprite.Debug_LocationPaint.setAntiAlias (true);
	}

	private LitImage				image						= null;
	private LitImage				markings					= null;
	private LitImage				damage					= null;
	private int						half_width				= -1;
	private int						half_height				= -1;
	private Unit					unit						= null;
	private String					name						= null;
	private int						playerColor				= 0;
	private String					squad						= "";


	public StandardUnitSprite (String name)
	{
		this.name = name;
		// half_width = width/2;
		// half_height = height/2;
	}


	// public int getHalfWidth ( )
	// {
	// return this.half_width;
	// }
	//
	//
	// public int getHalfHeight ( )
	// {
	// return this.half_height;
	// }

	@Override
	public void init (Unit u)
	{
		this.unit = u;
		BattlePlayer player = this.unit.getPlayer ( );
		this.playerColor = player.getColor ( );

		String imageName = this.name + StandardUnitSprite.ImageTag;
		String markingsName = this.name + StandardUnitSprite.MarkingsTag;
		String bumMapName = this.name + StandardUnitSprite.BumpMapTag;
		String damageName = this.name + StandardUnitSprite.DamageTag;

		// Markings are specially created for red and blue
		if (this.playerColor == Color.BLUE)
		{
			markingsName += "_blue";
		}
		else if (this.playerColor == Color.RED)
		{
			markingsName += "_red";
		}

		// Log.d("Render", "MarkedImage = " + markingsName);

		// [YARTS] insignia = Managers.getImageManager().getColorisedImage( "star.gif", red, green, blue );
		this.image = ImageManager.getLitImage (imageName, markingsName, bumMapName, this.playerColor);

		// [YARTS] damaged_image_strip = Managers.getImageManager().getLitImage( name + "Strip.gif", name +
		// "Markings.gif", name + "Map.gif", red, green, blue );

		this.damage = ImageManager.getLitImage (damageName);
		this.markings = ImageManager.getLitImage (markingsName);

		Squad s = this.unit.getSquad ( );
		if (s != null)
		{
			this.squad = Integer.toString (s.getId ( ) + 1);
		}
	}


	@Override
	public void dispose ( )
	{

	}


	// GUI
	@Override
	public void paintHealth (Canvas c)
	{
		StandardUnitSprite.HealthRectFill.setColor (this.playerColor);
		StandardUnitSprite.BoldTextPaint.setColor (this.playerColor);
		StandardUnitSprite.PlainTextPaint.setColor (this.playerColor);

		float left, right, top, bottom;

		int health = this.unit.getHealth ( );
		Vector2d pos = this.unit.getPosition ( );
		// [] 100 = MAX UNITS HEALTH ??
		int width = ((health * 2 * this.half_width) / 100);

		left = (pos.x - this.half_width);
		top = (pos.y - this.half_height) + StandardUnitSprite.VerticalHealthOffset;
		right = left + width;
		bottom = top + 4;
		c.drawRect (left, top, right, bottom, StandardUnitSprite.HealthRectFill);

		width = (2 * this.half_width);
		left = (float) (pos.x - width / 2.0);
		top = (pos.y - this.half_height) + StandardUnitSprite.VerticalHealthOffset;
		right = left + width - 1;
		bottom = top + 4;
		// [YARTS] g.drawRect ((int) (pos.x - width / 2.0), (int) (pos.y - this.half_height), width - 1, 4);
		c.drawRect (left, top, right, bottom, StandardUnitSprite.HealthRectOutline);

		// [YARTS] g.drawString (this.squad, (int) ((int) pos.x - (this.half_width) - 1), (int) ((int) pos.y +
		// (this.half_width >> 1) + 1));
		float x = (int) pos.x - (this.half_width) - 1;
		float y = (int) pos.y + (this.half_height >> 1) + StandardUnitSprite.BoldTextPaint.getTextSize ( );
		c.drawText (this.squad, x, y, StandardUnitSprite.BoldTextPaint);

	}


	@Override
	public void paint (Canvas c)
	{
		Unit thisUnit = this.unit;

		this.paint ((int) thisUnit.getPosition ( ).x, (int) thisUnit.getPosition ( ).y, thisUnit.getDirection ( ), c);

		// Draw a point on the units location in debug mode
		if (Debug.JOSHUA)
		{
			c.drawPoint ((int) thisUnit.getPosition ( ).x, (int) thisUnit.getPosition ( ).y, StandardUnitSprite.Debug_LocationPaint);
		}
	}


	public void paint (int x, int y, float dir, Canvas c)
	{

		// XXX Radians to degrees
		float deg = (float) Math.toDegrees (-dir);

		int hw = this.half_width;
		int hh = this.half_height;

		LitImage thisImage = this.image;

		Unit thisUnit = this.unit;

		if (hw == -1)
		{
			hw = thisImage.getImage ( ).getWidth ( ) >> 1;
			hh = thisImage.getImage ( ).getHeight ( ) >> 1;
		}

		// [] Matts debug code
		// Rectangle rect = null;
		//
		// if (Debug.MATT)
		// {
		// rect = g.getClipBounds ( );
		// }

		c.save ( );
		c.rotate (deg, x, y); // rotate around the unit's center point
		c.drawBitmap (thisImage.getImage ( ), x - hw, y - hh, StandardUnitSprite.UnitPaint);
		c.drawBitmap (this.markings.getImage ( ), x - hw, y - hh, StandardUnitSprite.DamagePaint);

		// Draws visible damage of the unit (not the health bar)
		if (thisUnit.getHealth ( ) < StandardUnitSprite.DamageThreshold && thisUnit.getUnitType ( ) != Unit.PASSIVE_UNIT)
		{
			c.drawBitmap (this.damage.getImage ( ), x - hw, y - hh, StandardUnitSprite.DamagePaint);
		}

		this.half_width = hw;
		this.half_height = hh;

		c.restore ( );

		// g.drawImage( images[ i ].getImage(), x - half_width, y - half_height, component ); // so centered at
		// x,y
		// g.drawImage( images[ i ], prev_x - half_width, prev_y - half_height, component );
		// g.drawImage( insignia, x -4, y -4, component );
		// if ( Math.random() < 0.1 ) {
	}

}