package iu.android.gui;

import android.graphics.Color;

public class ColorChooser
{
	private String[][] mColors;
	private int	color_index	= 0;

	public ColorChooser ()
	{		
		this.mColors = new String [8][2];
		
		this.mColors[0][0] = Integer.toString(Color.RED);
		this.mColors[0][1] = "Red";
		this.mColors[1][0] = Integer.toString(Color.BLACK);
		this.mColors[1][1] = "Black";
		this.mColors[2][0] = Integer.toString(Color.BLUE);
		this.mColors[2][1] = "Blue";
		this.mColors[3][0] = Integer.toString(Color.GREEN);
		this.mColors[3][1] = "Green";
		this.mColors[4][0] = Integer.toString(Color.YELLOW);
		this.mColors[4][1] = "Yellow";
		this.mColors[5][0] = Integer.toString(Color.WHITE);
		this.mColors[5][1] = "White";
		this.mColors[6][0] = Integer.toString(Color.GRAY);
		this.mColors[6][1] = "Gray";
		this.mColors[7][0] = Integer.toString(Color.CYAN);
		this.mColors[7][1] = "Cyan";	
		
	}


	private void setColorAt (int x)
	{
		//TODO: implement this if necessary
	}


	public int getColor ( )
	{
		return Integer.parseInt(this.mColors[color_index][0]);
	}
	
	public int getColorAt (int p_Index )
	{
		return Integer.parseInt(this.mColors[p_Index][0]);
	}

	public String[] getColorNames()
	{
		String[] strColorsTmp = new String[this.mColors.length];
		
		for (int i = 0; i < this.mColors.length; i++)
		{
			strColorsTmp[i] = this.mColors[i][1];
				
		}
		
		return strColorsTmp;
	}
	
	public int[] getColors()
	{
		int[] nColorsTmp = new int[this.mColors.length];
		
		for (int i = 0; i < this.mColors.length; i++)
		{
			nColorsTmp[i] = Integer.parseInt(this.mColors[i][0]);
				
		}
		
		return nColorsTmp;
	}
}