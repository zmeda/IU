package iu.android.graph;

import android.graphics.Bitmap;
import android.view.View;

// TODO add code for ambient lighting
// This is just a skeleton and does not do anything other than an ordinary bitmap
// For all purposes this class behaves as its originalImage
public final class LitImage {
	
	private Bitmap originalImage = null;
	
//	private Bitmap bumpMap = null;
//	private int[] bumpMapPixels = null;
//	
//	private Bitmap image = null;
//	private int[] pixels = null;
//	
//	private int width;
//	private int height;
	
	private MarkedImage markedImage = null;
	
	public LitImage( Bitmap originalImage ) {
		this.originalImage = originalImage;
	}
	
	public LitImage( Bitmap originalImage, Bitmap bumpMap ) {
		this.originalImage = originalImage;
//		this.bumpMap = bumpMap;
	}
	
	public LitImage( MarkedImage markedlImage, Bitmap bumpMap ) {
		this.markedImage = markedlImage;
//		this.bumpMap = bumpMap;
	}
		

	
	// TODO
	public void load( View c ){
//		if ( markedImage != null ) {
//			markedImage.load( c );
//			originalImage = markedImage.getImage();
//			//System.out.println( ""+ originalImage );
//		}
//		
//		
//		do {
//			width = originalImage.getWidth( c );
//			height = originalImage.getHeight( c );
//			Thread.yield();
//		}
//		while( width == -1 );
//		//System.out.println( "here" );
//		if ( markedImage == null )
//			originalPixels = new int[ width*height ];
//		else 
//			originalPixels = markedImage.getPixels();
//			
//		bumpMapPixels  = new int[ width*height ];
//		pixels		   = new int[ width*height ];
//
//		if ( bumpMap == null )
//			loadSingleImage();
//		else 
//			loadDoubleImage();
//
//		MemoryImageSource imageSource = new MemoryImageSource( width, height, pixels, 0, width );
//		//imageSource.setAnimated( true );
//		image = c.createImage( imageSource );
	
	}
	
	
// TODO
	public void lightImage( float r, float g, float b, float lx, float ly, float lz ) {
		//System.arraycopy( originalPixels, 0, pixels, 0, originalPixels.length );
		
//		if ( originalPixels == null )
//			return;
//		
//		float ambient = 0.1f;
//		for ( int i = 0; i < height; i++ ) {
//			int iByWidth = i*width;
//			int iMinus1ByWidth = (i-1)*width;
//			int iPlus1ByWidth = (i+1)*width;
//			for ( int j = 0; j < width; j++ ) {
//				int index = iByWidth + j;
//				int iMinus1 = i > 0? bumpMapPixels[ iMinus1ByWidth + j ] : bumpMapPixels[ index ];
//				int iPlus1  = i < height-1? bumpMapPixels[ iPlus1ByWidth + j ] : bumpMapPixels[ index ];
//				int iGradient = iMinus1 - iPlus1;
//
//				int jMinus1 = j > 0? bumpMapPixels[ index-1 ] : bumpMapPixels[ index ];
//				int jPlus1  = j < width-1? bumpMapPixels[ index+1 ] : bumpMapPixels[ index ];
//				int jGradient = jMinus1 - jPlus1;
//
//				int x = iGradient, y = jGradient, z = 64;
//
//				float dist = (float)Math.sqrt( x*x + y*y + z*z );
//
//				float rx = x/dist;
//				float ry = y/dist;
//				float rz = z/dist;
//
//				float light = lx*rx + ly*ry + lz*rz;
//				if ( light < 0.0f )
//					light = 0.0f;
//
//
//				int original_pixel = originalPixels[ index ];
//				int alpha = (original_pixel & 0xFF000000);
//				int red   = (original_pixel & 0x00FF0000) >> 16;
//				int green = (original_pixel & 0x0000FF00) >> 8;
//				int blue  = (original_pixel & 0x000000FF);
//
//				light = ambient + (1.0f - ambient)*light;
//
//				red   = (int)(red*light*r);
//				red   = red > 255? 255 : red;
//				green = (int)(green*light*g);
//				green = green > 255? 255 : green;
//				blue  = (int)(blue*light*b);
//				blue  = blue > 255? 255 : blue;
//
//				pixels[ index ] = alpha | (red << 16) | (green << 8) | blue;
//
//			}
//		}
//		
//		originalPixels = null;
//		bumpMapPixels = null;
//		bumpMap = null;
//		originalImage = null;
//		
//		image.flush();
	}
	
	// TODO shoud return the processed image
	public Bitmap getImage() {
		if (this.markedImage != null) return this.markedImage.getImage ( );
		else return this.originalImage;
	}
	
}