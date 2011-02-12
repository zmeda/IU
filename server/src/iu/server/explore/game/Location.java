package iu.server.explore.game;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class Location
{
	float			latitude;
	float			longitude;

	private Tile	tile	= null;
	private long	field	= 0;

	static class GeoFormat extends NumberFormat
	{

		/**
		 * 
		 */
		private static final long	serialVersionUID	= -8380643277242647315L;


		@Override
		public StringBuffer format (double number, StringBuffer sb, FieldPosition pos)
		{
			int degrees = (int) Math.floor (number);
			number -= degrees;
			number *= 60;

			int minutes = (int) Math.floor (number);
			number -= minutes;
			number *= 60;

			int seconds = (int) Math.floor (number);

			sb.append (degrees);
			sb.append ("˚");
			sb.append (minutes);
			sb.append ("'");
			sb.append (seconds);
			sb.append ("\"");

			return sb;
		}


		@Override
		public StringBuffer format (long number, StringBuffer toAppendTo, FieldPosition pos)
		{
			throw new UnsupportedOperationException ("Floating points only");
		}


		@Override
		public Number parse (String source, ParsePosition parsePosition)
		{
			throw new UnsupportedOperationException ("One way only");
		}

	}

	// private static NumberFormat	format	= new GeoFormat ( );
	private static NumberFormat	format	= new DecimalFormat ("#.######");
	

	public Location (final float latitude, final float longitude)
	{
		super ( );
		this.latitude = latitude;
		this.longitude = longitude;

	}


	public Tile tile (final World world)
	{
		if (this.tile == null)
		{
			this.tile = world.tileAt (this);
		}

		return this.tile;
	}


	public long field (final World world)
	{
		if (this.field == 0)
		{
			this.field = world.fieldAt (this);
		}
		return this.field;
	}


	/**
	 * @return the latitude
	 */
	public float getLatitude ( )
	{
		return this.latitude;
	}


	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude (final float latitude)
	{
		this.latitude = latitude;
	}


	/**
	 * @return the longitude
	 */
	public float getLongitude ( )
	{
		return this.longitude;
	}


	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude (final float longitude)
	{
		this.longitude = longitude;
	}


	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );
		sb.append ("Location(");
		sb.append (format.format (this.latitude));
		sb.append (" ");
		sb.append (this.latitude > 0 ? 'N' : 'S');
		sb.append ("  ");
		sb.append (format.format (this.longitude));
		sb.append (" ");
		sb.append (this.longitude > 0 ? 'E' : 'W');
		sb.append (")");
		return sb.toString ( );
	}
}
