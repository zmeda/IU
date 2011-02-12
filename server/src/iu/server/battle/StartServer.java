package iu.server.battle;

/*************************************************************************************************************
 * IU 1.0b, a real time strategy game
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

import iu.server.battle.BattleListenServer;


public class StartServer
{
	/**
	 * Application entry point
	 */
	public static void main (String[] args)
	{
		String serverAddress = "localhost";
		
		System.out.println ("Welcome to the IU battle server, v" + BattleListenServer.VERSION);
		System.out.print   ("Trying to bind ... ");
		
		//
		// if the number of arguments is more than one, use it as the IP
		//
		if (args.length > 0)
		{
			serverAddress = args[0];
		}
		
		//
		// Start the listening server on the specified IP and wait for new clients to arrive!
		//
		System.out.println (serverAddress);
		
		BattleListenServer battleServer = new BattleListenServer (null, null, serverAddress, 4000, 2);
		
		battleServer.start ( );
	}
}
