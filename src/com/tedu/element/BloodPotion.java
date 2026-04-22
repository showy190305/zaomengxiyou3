package com.tedu.element;

import java.awt.Color;

import javax.swing.ImageIcon;

public class BloodPotion extends Item {
    private static final int DEFAULT_HEAL = 30;

    public BloodPotion() {
        super();
        this.value = DEFAULT_HEAL;
        this.displayName = "HPPot";
        this.setIcon(new ImageIcon("image/item/health.png"));
    }

    public BloodPotion(int x, int y, int w, int h, ImageIcon icon, int value) {
        super(x, y, w, h, icon, value);
        this.displayName = "HPPot";
        this.setIcon(new ImageIcon("image/item/health.png"));
    }

    public BloodPotion(int x, int y) {
        super(x, y, 30, 30, null, DEFAULT_HEAL);
        this.displayName = "HPPot";
        this.setIcon(new ImageIcon("image/item/health.png"));
    }

    @Override
    public void applyEffect(WuKong player) {
        if (player != null) {
            player.recoverHp(value);
            this.setLive(false);
        }
    }

    @Override
    protected Color getItemColor() {
        return Color.RED;
    }
}
