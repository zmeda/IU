/**
 * @author xp
 * 
 */
package iu.database.util;

public class ReadDB
{

	// public static final String dbRoot = "~/bin/hsql/iu";
	// public static final String dbRoot = "hsqldb/iu";

	/**
	 * @param args
	 */
	public static void main (final String[] args)
	{

		DBUtility iuDbUtil = new DBUtility ( );

		iuDbUtil.openDbConnection ( );

		iuDbUtil.printDB ( );

		iuDbUtil.closeDbConnection ( );
	}

}
