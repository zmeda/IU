package iu.android.unit;

import iu.android.battle.BattleThread;

public interface Weapon
{
	public String getProjectileName();

	public int getRange();

	public int getMinimumRange();

	public boolean isDisabled();

	public boolean isReady();

	public float timeUntilReloaded();

	public void fireOnPosition(int x, int y);

	public void register(BattleThread gameThread);

	public void integrate(float dt);
}