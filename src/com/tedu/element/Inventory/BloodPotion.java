package com.tedu.element.Inventory;

import java.awt.Color;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.tedu.element.WuKong;

/**
 * 血瓶道具 - 恢复玩家HP
 */
public class BloodPotion extends Item {

	// 默认恢复30点HP
	private static final int DEFAULT_HEAL = 30;

	public BloodPotion() {
		super();
		this.value = DEFAULT_HEAL;
		this.setIcon(new ImageIcon("image/item/health.png"));
	}

	public BloodPotion(int x, int y, int w, int h, ImageIcon icon, int value) {
		super(x, y, w, h, icon, value);
		this.setIcon(new ImageIcon("image/item/health.png"));

	}

	public BloodPotion(int x, int y) {
		super(x, y, 30, 30, null, DEFAULT_HEAL);
		this.setIcon(new ImageIcon("image/item/health.png"));

	}

	@Override
	public void applyEffect(WuKong player) {
		if (player != null) {
			// 恢复玩家HP
			player.recoverHp(value);
			// 拾取后标记为不存在
			this.setLive(false);
		}
	}

	@Override
	protected Color getItemColor() {
		return Color.RED;
	}
}
