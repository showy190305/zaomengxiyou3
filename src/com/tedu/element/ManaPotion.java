package com.tedu.element;

import java.awt.Color;

import javax.swing.ImageIcon;

/**
 * 蓝瓶道具 - 恢复玩家MP
 */
public class ManaPotion extends Item {
	
	// 默认恢复20点MP
	private static final int DEFAULT_MANA = 20;
	
	public ManaPotion() {
		super();
		this.value = DEFAULT_MANA;
		this.setIcon(new ImageIcon("image/item/mana.png"));
	}
	
	public ManaPotion(int x, int y, int w, int h, ImageIcon icon, int value) {
		super(x, y, w, h, icon, value);
		this.setIcon(new ImageIcon("image/item/mana.png"));
	}
	
	public ManaPotion(int x, int y) {
		super(x, y, 30, 30, null, DEFAULT_MANA);
		this.setIcon(new ImageIcon("image/item/mana.png"));
	}
	
	@Override
	public void applyEffect(WuKong player) {
		if (player != null) {
			// 恢复玩家MP
			player.recoverMp(value);
			// 拾取后标记为不存在
			this.setLive(false);
		}
	}
	
	@Override
	protected Color getItemColor() {
		return Color.BLUE;
	}
}
