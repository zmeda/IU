package iu.server.explore.game;

import iu.server.battle.BattleListenServer;

public class DummyBattleServer extends BattleListenServer
{
	private Server	server;
	private Battle	battle;


	public DummyBattleServer (Server server, Battle battle, String address, int port)
	{
		super (server, battle, address, port, 1);
		this.server = server;
	}


	@Override
	public void run ( )
	{
		try
		{
			// This is where the proper battle will be run
			Thread.sleep (10000);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace ( );
		}

		// This is where the BServer will inform the EServer about the end of the battle
		Command command = CommandFinishBattle.init (this.battle, this.battle.attacker);
		this.server.getGame ( ).schedule (command);
	}
}
