package iu.android.network.explore;

public class FlagBattle {

	public int idAttacker; 
	public int idFlag; 
	
	// get from server
	public int battleId = -1;
	
	// get from server
	public short attHover = 0;
	public short attTank = 0;
	public short attArtillery = 0;
	
	// configured by user
	public short defHover = 0;
	public short defTank = 0;
	public short defArtillery = 0;
	
	// recived by server
	public int communicationPort = 0;
	
	public FlagBattle(int idAttacker, int idFlag) 
	{
		this.idAttacker = idAttacker;
		this.idFlag = idFlag;
	}
}
