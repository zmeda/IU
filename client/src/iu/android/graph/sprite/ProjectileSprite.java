package iu.android.graph.sprite;

import iu.android.Debug;
import iu.android.graph.ImageManager;
import iu.android.unit.Projectile;

import java.util.ArrayList;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class ProjectileSprite extends Sprite
{

	private static ArrayList<ProjectileSprite>	Pool;
	private static final Paint							ProjectilePaint;
	private static final Paint							DebugPaint;
	private static Hashtable<String, Bitmap>		Images;

	static
	{
		ProjectileSprite.Pool = new ArrayList<ProjectileSprite> ( );

		ProjectilePaint = new Paint ( );

		DebugPaint = new Paint ( );
		ProjectileSprite.DebugPaint.setColor (Color.RED);
		ProjectileSprite.DebugPaint.setStrokeWidth (1f);

		ProjectileSprite.Images = new Hashtable<String, Bitmap> ( );
	}

	private Projectile									projectile;
	private final Rect									bounds;


	private ProjectileSprite ( )
	{
		super ( );

		this.projectile = null;
		this.bounds = new Rect ( );
	}


	public static ProjectileSprite newProjectileSprite ( )
	{
		ArrayList<ProjectileSprite> poolVec = ProjectileSprite.Pool;

		if (poolVec.size ( ) == 0)
		{
			return new ProjectileSprite ( );
		}

		int lastIndex = poolVec.size ( ) - 1;
		ProjectileSprite projectileSprite = poolVec.get (lastIndex);
		poolVec.remove (lastIndex);

		return projectileSprite;
	}


	private Bitmap getImage (String projectileName)
	{
		Bitmap b = ProjectileSprite.Images.get (projectileName);
		if (b == null)
		{
			b = ImageManager.getImage (projectileName);
			ProjectileSprite.Images.put (projectileName, b);
		}
		return b;
	}


	public void init (Projectile proj)
	{
		this.projectile = proj;
	}


	// GUI
	@Override
	public void paint (Canvas c)
	{
		Projectile thisProj = this.projectile;
		Rect thisBounds = this.bounds;

		Bitmap b = this.getImage (thisProj.getWeapon ( ).getProjectileName ( ));
		int width = b.getWidth ( );
		int height = b.getHeight ( );

		thisBounds.left = (int) (thisProj.getX ( ) - (width >> 1));
		thisBounds.top = (int) (thisProj.getY ( ) - (height >> 1));
		thisBounds.right = thisBounds.left + width;
		thisBounds.bottom = thisBounds.top + height;

		c.drawBitmap (b, thisBounds.left, thisBounds.top, ProjectileSprite.ProjectilePaint);

		if (Debug.JOSHUA)
		{
			c.drawPoint (thisProj.getX ( ), thisProj.getY ( ), ProjectileSprite.DebugPaint);
		}
	}


	@Override
	public void dispose ( )
	{
		ProjectileSprite.Pool.add (this);
	}
}