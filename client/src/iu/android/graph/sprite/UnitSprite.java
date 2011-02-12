package iu.android.graph.sprite;

import android.graphics.Canvas;

import iu.android.unit.Unit;

public abstract class UnitSprite extends Sprite
{
	public abstract void init (Unit unit);


	// GUI - needs review
	public abstract void paintHealth (Canvas c);
}