package iu.android.engine.ai;

public abstract class State
{

	public abstract boolean actionDone ( );


	public String GetStateName ( )
	{
		return getClass ( ).getName ( );
	}

}