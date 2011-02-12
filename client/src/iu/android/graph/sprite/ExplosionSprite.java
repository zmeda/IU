/**
 * 
 */
package iu.android.graph.sprite;

import iu.android.Debug;
import iu.android.engine.Explosion;
import iu.android.graph.ImageManager;
import iu.android.graph.LitImage;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Visual representation of an explosion
 * 
 * @author luka
 */
public class ExplosionSprite extends Sprite {
	private static final Paint Paint;
	private static final Paint DebugPaint;

	private static ArrayList<ExplosionSprite> Pool;

	private static final int Frames = 8;
	private static LitImage[] Images = new LitImage[Frames];
	private static Bitmap[] Bitmaps = new Bitmap[Frames];
	private static final int HalfHeight;
	private static final int HalfWidth;
	private static final float NormalizeExplosionRadius = 1.0f / 75.0f;

	static {
		ExplosionSprite.Pool = new ArrayList<ExplosionSprite>();

		Paint = new Paint();
		Paint.setAlpha(192);
		// TODO [] - initialize Paint to proper values

		DebugPaint = new Paint();
		ExplosionSprite.DebugPaint.setColor(Color.BLUE);
		ExplosionSprite.DebugPaint.setStrokeWidth(3f);

		for (int i = 0; i < Frames; i++) {
			ExplosionSprite.Images[i] = ImageManager.getLitImage("explosion_"
					+ i);
			ExplosionSprite.Bitmaps[i] = ExplosionSprite.Images[i].getImage();
		}

		HalfHeight = ExplosionSprite.Bitmaps[0].getHeight ( ) / 2;
		HalfWidth = ExplosionSprite.Bitmaps[0].getWidth ( ) / 2;
	}

	private Explosion explosion;
	private float scale;

	/**
	 * Private constructor to make sure instances are acquired from the Pool
	 */
	private ExplosionSprite() {
		super();

		this.explosion = null;
		this.scale = 1.0f;
	}

	/**
	 * Get an instance from the Pool or create a new one
	 * 
	 * @return
	 */
	public static ExplosionSprite newExplosionSprite() {

		ArrayList<ExplosionSprite> spritePool = ExplosionSprite.Pool;

		if (spritePool.size() == 0) {
			return new ExplosionSprite();
		}

		int lastIndex = spritePool.size() - 1;
		ExplosionSprite explosionSprite = spritePool.remove(lastIndex);
		spritePool.remove(lastIndex);

		return explosionSprite;
	}

	/**
	 * Initialize with given parameters
	 * 
	 * @param explosion
	 */
	public void init(Explosion expl) {
		this.explosion = expl;

		Explosion thisExp = this.explosion;

		float timeLeft = thisExp.getTimeLeft();
		float duration = thisExp.getDuration();

		float relTime = timeLeft / duration;

		// XXX what does 0.33 mean
		// XXX what does 3.0 mean
		this.scale = (float) ((3.0 - relTime) * thisExp.getRadius() * ExplosionSprite.NormalizeExplosionRadius);
	}

	// itak nima veze - zaradi View.invalidate()
	// public Rect getDirtyRectangle ( )
	// {
	// if (this.explosion == null)
	// throw new RuntimeException ("trying to use diposed sprite");
	//
	// return this.bounds;
	// }
	//

	@Override
	public void paint(Canvas c) {
		Explosion thisExp = this.explosion;

		if (thisExp.getTimeLeft() < 0.0) {
			return;
		}

		this.init(thisExp);

		int x = (int) (thisExp.getX() - ExplosionSprite.HalfWidth * this.scale);
		int y = (int) (thisExp.getY() - ExplosionSprite.HalfHeight * this.scale);

		int i = Frames - ((int) (Frames * (thisExp.getTimeLeft() / thisExp.getDuration())));

		if (i < 0)
			i = 0;
		else if (i > Frames - 1)
			i = Frames - 1;

		// GUI needs review
		c.save();
		c.scale(this.scale, this.scale, x, y);
		c.drawBitmap(ExplosionSprite.Bitmaps[i], x, y, ExplosionSprite.Paint);
		c.restore();

		if (Debug.JOSHUA) {
			c.drawPoint(thisExp.getX(), thisExp.getY(),
					ExplosionSprite.DebugPaint);
		}

	}

	@Override
	public void dispose() {
		this.explosion = null;
		ExplosionSprite.Pool.add(this);
	}

}
