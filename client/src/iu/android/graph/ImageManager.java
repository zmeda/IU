package iu.android.graph;

import iu.android.RR;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

/**
 * Utility class for loading images, so we can keep all of the image loading code in one place and we can
 * cache images after we have loaded them.
 */
public class ImageManager
{
	static private HashMap<String, Bitmap>			imageCache			= new HashMap<String, Bitmap> ( );
	static private HashMap<String, LitImage>		litImageCache		= new HashMap<String, LitImage> ( );
	static private HashMap<String, MarkedImage>	markedImageCache	= new HashMap<String, MarkedImage> ( );

	static private View									component			= null;


	public static void setComponent (View component)
	{
		ImageManager.component = component;
	}


	/**
	 * Helper Get all pixels from the bitmap as an array
	 */
	public static int[] getAllPixels (Bitmap b)
	{

		int offset = 0;
		int stride = 0;
		int x = 0;
		int y = 0;
		int w = b.getWidth ( );
		int h = b.getHeight ( );
		int[] pixels = new int[w * h];

		b.getPixels (pixels, offset, stride, x, y, w, h);

		return pixels;
	}


	/** How many different images have been requested from the manager. * */
	public static int getNumImages ( )
	{
		return ImageManager.imageCache.size ( );
	}


	public static int getNumLitImages ( )
	{
		return ImageManager.litImageCache.size ( );
	}


	/** How many different images have been fully loaded. * */
	// TODO
	// public int getNumImagesLoaded ( )
	// {
	// int numLoaded = 0;
	//
	// for (int i = 0; i < this.numImages; i++)
	// {
	// if (mediaTracker.checkID (i, true))
	// {
	// numLoaded++;
	// }
	// }
	//
	// return numLoaded;
	// }
	/** Have we finished loading all of the images?. * */
	// TODO
	// public boolean allImagesLoaded ( )
	// {
	// return mediaTracker.checkAll ( );
	// }
	/** Start loading the images. * */
	// TODO
	// public void startLoadingImages ( )
	// {
	// mediaTracker.checkAll (true);
	// }
	/** This will change, probably to something that takes in a progress bar component to update. * */
	// TODO
	// public void loadAllImages (ProgressBar progress)
	// {
	// progress.setMaxValue (getNumImages ( ) + numLitImages);
	// do
	// {
	// int numLoaded = getNumImagesLoaded ( );
	//
	// progress.setCurrentValue (numLoaded);
	// progress.repaint ( );
	// try
	// {
	// Thread.sleep (40);
	// }
	// catch (Exception e)
	// {
	// }
	//
	// } while (!allImagesLoaded ( ));
	//
	// int numLoaded = getNumImagesLoaded ( );
	//
	// progress.setCurrentValue (numLoaded);
	// progress.repaint ( );
	// try
	// {
	// Thread.sleep (40);
	// }
	// catch (Exception e)
	// {
	// }
	//
	// Enumeration myEnum = litImageCache.elements ( );
	//
	// int litLoaded = 0;
	// while (myEnum.hasMoreElements ( ))
	// {
	// LitImage litImage = (LitImage) myEnum.nextElement ( );
	// litImage.load (component);
	// litLoaded++;
	// // game.Debug.john.println( "loading lit image" );
	// progress.setCurrentValue (numLoaded + litLoaded);
	// progress.repaint ( );
	// try
	// {
	// Thread.sleep (40);
	// }
	// catch (Exception e)
	// {
	// }
	// }
	// float x = -0.05f, y = -0.05f, z = 0.5f;
	// float dist = (float) Math.sqrt (x * x + y * y + z * z);
	// // dist /= 1.1;
	// lightImages (x / dist, y / dist, z / dist);
	// }
	/** Get the image with the given name. This will cache images for later retrieval. * */
	public static Bitmap getImage (String imageName)
	{
		Bitmap b = ImageManager.imageCache.get (imageName);

		if (b == null)
		{
			b = BitmapFactory.decodeResource (ImageManager.component.getResources ( ), RR.getDrawableId (imageName));
			ImageManager.imageCache.put (imageName, b);
		}

		return b;
	}


	/**
	 * Gets an image that can be "lit". Uses bump-mapping on the image to make it look more 3D. The image will
	 * be lit by one global, directional light source.
	 * 
	 * @param originalImageId
	 *           resource id of the original image
	 */
	public static LitImage getLitImage (String imageName)
	{
		LitImage litImage = ImageManager.litImageCache.get (imageName);

		if (litImage == null)
		{
			Bitmap image = ImageManager.getImage (imageName);
			litImage = new LitImage (image);
			ImageManager.litImageCache.put (imageName, litImage);
		}

		return litImage;
	}


	public static MarkedImage getMarkedImage (String imageName, String markingsName, int color)
	{
		String name = imageName + markingsName + color;
		MarkedImage markedImage = ImageManager.markedImageCache.get (name);

		if (markedImage == null)
		{
			Bitmap image = ImageManager.getImage (imageName);
			Bitmap markings = ImageManager.getImage (markingsName);

			markedImage = new MarkedImage (image, markings, color);
			ImageManager.markedImageCache.put (name, markedImage);
		}

		return markedImage;
	}


	public static LitImage getLitImage (String imageName, String markingsName, String bumpMapName, int color)
	{
		String name = imageName + markingsName + bumpMapName + color;
		MarkedImage markings = ImageManager.getMarkedImage (imageName, markingsName, color);
		Bitmap bumpMap = ImageManager.getImage (bumpMapName);

		LitImage litImage = ImageManager.litImageCache.get (name);

		if (litImage == null)
		{
			litImage = new LitImage (markings, bumpMap);
			ImageManager.litImageCache.put (name, litImage);
		}

		return litImage;
	}


	/** Light all "lit images", using a light in the direction specified by the vector <CODE>(lx,ly,lz)</CODE>.* */

	public static void lightImages (float lx, float ly, float lz)
	{
		for (LitImage litImage : ImageManager.litImageCache.values ( ))
		{
			litImage.lightImage (1.0f, 1.0f, 1.0f, lx, ly, lz);
		}
	}
}