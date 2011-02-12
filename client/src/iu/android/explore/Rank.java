package iu.android.explore;

import iu.android.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public enum Rank
{

	PVT (500/* 1500 */, R.drawable.rank_1, "Private", 4, 2, 1), CPL (1500/* 5000 */, R.drawable.rank_2,
			"Corporal", 6, 3, 1), SGT (25000, R.drawable.rank_3, "Sergeant", 8, 4, 2), LT (70000,
			R.drawable.rank_4, "Liutenant", 10, 5, 3), CPT (200000, R.drawable.rank_5, "Captain", 10, 6, 4), MAJ (
			800000, R.drawable.rank_6, "Major", 10, 7, 5),
	// TODO proper values
	COL (3000000, R.drawable.rank_7, "Colonel", 10, 8, 5), CMD (10000000, R.drawable.rank_8, "Commander", 10,
			8, 5), LTG (25000000, R.drawable.rank_9, "Liutenant General", 10, 8, 5), GEN (50000000,
			R.drawable.rank_10, "General", 10, 8, 5), MAR (100000000, R.drawable.rank_11, "Marshall", 10, 8, 5), FLM (
			Integer.MAX_VALUE, R.drawable.rank_12, "Field Marshall", 10, 8, 5);

	public final int		experience;
	public final int		insignia_id;
	public final int		maxHover;
	public final int		maxTank;
	public final int		maxArtillery;
	public final String	longName;
	public Drawable		insignia;


	private Rank (int experience, int insignia_id, String longName, int maxHover, int maxTank, int maxArtillery)
	{
		this.experience = experience;
		this.insignia_id = insignia_id;
		this.longName = longName;
		this.maxHover = maxHover;
		this.maxTank = maxTank;
		this.maxArtillery = maxArtillery;
	}


	public static void initDrawables (Context context)
	{
		Resources res = context.getResources ( );

		for (Rank r : Rank.values ( ))
		{
			r.insignia = res.getDrawable (r.insignia_id);
		}
	}


	public static Rank next (Rank rank)
	{
		if (rank == FLM)
			return PVT;
		else
			return Rank.values ( )[rank.ordinal ( ) + 1];
	}
}
