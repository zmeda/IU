package iu.android.explore.event;

public class EventBattleStarted 
{
	public int tcpPort;
	private int battleId;

	public EventBattleStarted ( int battleId, int tcpPort)
	{
		this.tcpPort = tcpPort;
		this.battleId = battleId;
	}
}
