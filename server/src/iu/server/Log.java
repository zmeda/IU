package iu.server;

import java.util.Formatter;
import java.util.HashMap;

/**
 * A simple class for debugging 'LogCat-style'
 */
public class Log
{
	private static final Log					Default;
	public static final Log						Lucas;
	public static final Log						Boris;

	private static final Formatter			out		= new Formatter (System.out);
	private static final Formatter			err		= new Formatter (System.err);
	private static final String				format	= "%1$-8s %2$-18s %3$s\n";

	private static final String[]				Levels	= {"Fatal", "Error", "Warning", "Info", "Debug", "Verbose"};
	public static final int						Fatal		= 0;
	public static final int						Error		= 1;
	public static final int						Warning	= 2;
	public static final int						Info		= 3;
	public static final int						Debug		= 4;
	public static final int						Verbose	= 5;

	static
	{
		Default = new Log ("Log");
		Log.Default.enableTag ("TestAll", Log.Verbose);
		Log.Default.enableTag ("TestWarn", Log.Warning);
		Log.Default.enableTag ("Server", Log.Info);
		Log.Default.enableTag ("Game", Log.Info);
		Log.Default.enableTag ("Player", Log.Info);

		Lucas = new Log ("");
		Log.Lucas.enableTag ("Server", Log.Info);
		Log.Lucas.enableTag ("Game", Log.Info);
		Log.Lucas.enableTag ("GameLoop", Log.Info);
		Log.Lucas.enableTag ("Player", Log.Info);
		Log.Lucas.enableTag ("Message", Log.Info);
		Log.Lucas.enableTag ("Verbose", Log.Info);

		Boris = new Log ("Boris");
	}

	private final HashMap<String, Integer>	enabledTags;
	private final String							name;


	private Log (String name)
	{
		this.name = name;
		this.enabledTags = new HashMap<String, Integer> ( );
	}


	public void enableTag (String tag, int level)
	{
		this.enabledTags.put (tag, level);
	}


	public void disableTag (String tag)
	{
		this.enabledTags.remove (tag);
	}


	private void write (int level, String tag, String msg)
	{
		Integer tagLevel = this.enabledTags.get (tag);
		if (tagLevel == null || tagLevel.intValue ( ) < level)
		{
			return;
		}

		String lvl = Log.Levels[level];
		Formatter fmt = level > 2 ? Log.out : Log.err;
		fmt.format (Log.format, lvl, "[" + this.name + ":" + tag + "]", msg);
	}


	public void f (String tag, String msg)
	{
		this.write (0, tag, msg);
	}


	public void e (String tag, String msg)
	{
		this.write (1, tag, msg);
	}


	public void w (String tag, String msg)
	{
		this.write (2, tag, msg);
	}


	public void i (String tag, String msg)
	{
		this.write (3, tag, msg);
	}


	public void d (String tag, String msg)
	{
		this.write (4, tag, msg);
	}


	public void v (String tag, String msg)
	{
		this.write (5, tag, msg);
	}


	public static void F (String tag, String msg)
	{
		Log.Default.write (0, tag, msg);
	}


	public static void E (String tag, String msg)
	{
		Log.Default.write (1, tag, msg);
	}


	public static void W (String tag, String msg)
	{
		Log.Default.write (2, tag, msg);
	}


	public static void I (String tag, String msg)
	{
		Log.Default.write (3, tag, msg);
	}


	public static void D (String tag, String msg)
	{
		Log.Default.write (4, tag, msg);
	}


	public static void V (String tag, String msg)
	{
		Log.Default.write (5, tag, msg);
	}


	public static void main (String[] args)
	{
		Log.V ("TestAll", "Verbose mode");
		Log.D ("TestAll", "Debug mode");
		Log.I ("TestAll", "Info mode");
		Log.W ("TestAll", "Warning mode");
		Log.E ("TestAll", "Error mode");
		Log.F ("TestAll", "Fatal mode");

		Log.V ("TestWarn", "Verbose mode");
		Log.D ("TestWarn", "Debug mode");
		Log.I ("TestWarn", "Info mode");
		Log.W ("TestWarn", "Warning mode");
		Log.E ("TestWarn", "Error mode");
		Log.F ("TestWarn", "Fatal mode");

		Log.V ("Disabled", "Verbose mode");
		Log.D ("Disabled", "Debug mode");
		Log.I ("Disabled", "Info mode");
		Log.W ("Disabled", "Warning mode");
		Log.E ("Disabled", "Error mode");
		Log.F ("Disabled", "Fatal mode");
	}
}
