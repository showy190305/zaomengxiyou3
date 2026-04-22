package com.tedu.element;

import java.awt.Color;

import javax.swing.ImageIcon;

public class ManaPotion extends Item {
    private static final int DEFAULT_MANA = 20;

    public ManaPotion() {
        super();
        this.value = DEFAULT_MANA;
        this.displayName = "MPPot";
        this.setIcon(new ImageIcon("image/item/mana.png"));
    }

    public ManaPotion(int x, int y, int w, int h, ImageIcon icon, int value) {
        super(x, y, w, h, icon, value);
        this.displayName = "MPPot";
        this.setIcon(new ImageIcon("image/item/mana.png"));
    }

    public ManaPotion(int x, int y) {
        super(x, y, 30, 30, null, DEFAULT_MANA);
        this.displayName = "MPPot";
        this.setIcon(new ImageIcon("image/item/mana.png"));
    }

    @Override
    public void applyEffect(WuKong player) {
        if (player != null) {
            player.recoverMp(value);
            this.setLive(false);
        }
    }

    @Override
    protected Color getItemColor() {
        return Color.BLUE;
    }
}
