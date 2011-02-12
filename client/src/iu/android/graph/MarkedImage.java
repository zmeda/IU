package iu.android.graph;

import android.graphics.Bitmap;
import android.view.View;

// TODO add code for markings
// This is just a skeleton and does not do anything other than an ordinary bitmap
// For all purposes this class behaves as its originalImage
public final class MarkedImage
{

	private Bitmap	originalImage	= null;
	private int[]	originalPixels	= null;


	// private Bitmap markings = null;
	// private int[] markingsPixels = null;
	//	
	// private Bitmap image = null;
	// private int[] pixels = null;
	//	
	// private int width;
	// private int height;
	//	
	// private int red;
	// private int green;
	// private int blue;
	//	
	// private boolean loaded = false;

	public MarkedImage (Bitmap originalImage, Bitmap markings, int color)
	{
		this.originalImage = originalImage;
		// this.markings = markings;
		// this.red = red;
		// this.green = green;
		// this.blue = blue;
	}


	public void load (View v)
	{
		// if ( loaded )
		// return;
		//			
		// // wait while images load
		// do {
		// width = originalImage.width ( );
		// height = originalImage.height ( );
		// Thread.yield();
		// }
		// while( width == -1 );
		//
		// originalPixels = new int[ width*height ];
		// markingsPixels = new int[ width*height ];
		// pixels = new int[ width*height ];
		//
		// loadDoubleImage();
		//
		// MemoryImageSource imageSource = new MemoryImageSource( width, height, pixels, 0, width );
		// image = c.createImage( imageSource );
		//		
		// loaded = true;
	}


	int getWidth ( )
	{
		return this.originalImage.getWidth ( );
	}


	int getHeight ( )
	{
		return this.originalImage.getHeight ( );
	}


	int[] getPixels ( )
	{
		if (this.originalPixels == null)
		{
			this.originalPixels = new int[this.getWidth ( ) * this.getHeight ( )];
			this.originalImage.getPixels (this.originalPixels, 0, 0, 0, 0, getWidth ( ), getHeight ( ));
		}

		return this.originalPixels;
	}


	public Bitmap getImage ( )
	{
		return this.originalImage;
	}

}