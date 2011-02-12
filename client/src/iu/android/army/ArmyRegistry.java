package iu.android.army;

/*************************************************************************************************************
 * IU 1.0b, a java realtime strategy game
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 ************************************************************************************************************/

import iu.android.Debug;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * TODO Test it!
 * 
 * @author Lucas
 */
public class ArmyRegistry
{
	public static final int[]						colors	= {0xffff0000, 0xff0000ff, 0xffffff00, 0xffff00ff,
			0xff00ffff, 0xff00ff00, 0xffffffff, 0xff000000};

	private static ArrayList<PluggableArmy>	armies	= new ArrayList<PluggableArmy> ( );


	public static void addArmy (PluggableArmy army)
	{
		ArmyRegistry.armies.add (army);
	}


	public static int getNumArmies ( )
	{
		return ArmyRegistry.armies.size ( );
	}


	public static PluggableArmy getArmy (int i)
	{
		return ArmyRegistry.armies.get (i);
	}


	private static void loadClassesFromDirectory (String nameSoFar, File dir)
	{

		FilenameFilter filter = new FilenameFilter ( ) {
			public boolean accept (File directory, String fileName)
			{
				return fileName.endsWith (".class");
			}
		};

		String[] classFiles = dir.list (filter);

		int filesCount = classFiles.length;
		for (int i = 0; i < filesCount; i++)
		{
			String name = classFiles[i];
			name = name.substring (0, name.length ( ) - ".class".length ( ));
			name = nameSoFar + name; // fully qualified class name

			boolean alreadyloaded = false;

			ArrayList<PluggableArmy> armiesVec = ArmyRegistry.armies;

			int armyCount = armiesVec.size ( );
			for (int j = 0; j < armyCount; j++)
			{
				Object army = armiesVec.get (j);
				if (army.getClass ( ).getName ( ).equals (name))
				{
					alreadyloaded = true;
					break;
				}
			}

			if (!alreadyloaded)
			{
				try
				{
					Class<?> clazz = Class.forName (name);
					if (!clazz.isInterface ( ))
					{

						Object object = clazz.newInstance ( );

						if ((object instanceof PluggableArmy) && !(object instanceof PassiveArmy))
						{
							PluggableArmy army = (PluggableArmy) object;
							ArmyRegistry.addArmy (army);
						}
					}
				}
				catch (Throwable e)
				{
					if (Debug.BORIS)
					{
						Debug.Boris.println (name + " could not be loaded as a PluggableArmy");
					}
				}
			}
		}
		ArmyRegistry.recurseDirectoriesAndLoad (nameSoFar, dir);
	}


	private static void recurseDirectoriesAndLoad (String nameSoFar, File dir)
	{
		FilenameFilter filter = new FilenameFilter ( ) {
			public boolean accept (File dir, String name)
			{
				File file = new File (dir, name);
				return file.isDirectory ( );
			}
		};

		String[] directories = dir.list (filter);

		int dirCount = directories.length;
		for (int i = 0; i < dirCount; i++)
		{
			String name = directories[i];

			if (name.indexOf ('.') == -1)
			{
				//
				// TODO: Not . or .. or something else a bit weird this is probably very system specific?
				//
				ArmyRegistry.loadClassesFromDirectory (nameSoFar + name + '.', new File (dir, name));
			}
		}
	}

	/**
	 * Check <CODE>"user.dir"</CODE> for class files and loads any that implement <CODE>PluggableArmy</CODE>.
	 * TODO: This method is not called from anywhere ... maybe we should delete it in the future.
	 * 
	 * public static void checkForPluggableArmies ( ) { File dir = new File (System.getProperty ("user.dir"));
	 * 
	 * ArmyRegistry.loadClassesFromDirectory ("", dir); }
	 */
}
