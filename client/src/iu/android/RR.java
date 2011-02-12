/**
 * 
 */
package iu.android;

import iu.android.R;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Uses Java Reflection API to get resources using their names
 * 
 * @author luka
 */
public class RR
{
	/**
	 * Get the drawable resource by name
	 * 
	 * @param context
	 * @param name
	 * @return a Drawable
	 */
	public static Drawable getDrawable(Context context, String name)
	{
		return context.getResources().getDrawable(RR.getDrawableId(name));
	}

	/**
	 * Get the ID of the drawable resource
	 * 
	 * @param name
	 * @return ID of the named resource or 0 on error
	 */
	public static int getDrawableId(String name)
	{
		Class<R.drawable> c = R.drawable.class;
		Field f;
		int i = 0;
		try
		{
			f = c.getField(name);
			i = f.getInt(f);
		}
		catch (Exception e)
		{
			Log.e("RR", e.toString());
		}
		return i;
	}

	/**
	 * Get the string resource by name
	 * 
	 * @param context
	 * @param name
	 * @return a String
	 */
	public static String getString(Context context, String name)
	{
		return context.getResources().getString(RR.getStringId(name));
	}

	/**
	 * Get the ID of the string resource
	 * 
	 * @param name
	 * @return ID of the named resource or 0 on error
	 */
	public static int getStringId(String name)
	{
		Class<R.string> c = R.string.class;
		Field f;
		int i = 0;
		try
		{
			f = c.getField(name);
			i = f.getInt(f);
		}
		catch (Exception e)
		{
			Log.e("RR", e.toString());
		}
		return i;
	}
}
