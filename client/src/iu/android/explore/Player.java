package iu.android.explore;

public class Player
{

	private static byte	playerCount	= 0;

	private String			name;
	private int				color;
	private byte			id;


	public static void resetPlayerCount ( )
	{
		Player.playerCount = 0;
	}


	public Player (String name)
	{
		this.name = name;
		this.id = Player.playerCount++;
	}


	public int getId ( )
	{
		// TODO Auto-generated method stub
		return this.id;
	}


	public String getName ( )
	{
		// TODO Auto-generated method stub
		return this.name;
	}


	public int getColor ( )
	{
		// TODO Auto-generated method stub
		return this.color;
	}


	public void setColor (int color)
	{
		this.color = color;
	}

}
