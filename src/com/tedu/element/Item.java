package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;

/**
 * 道具基类，统一支持消耗品和装备品。
 */
public abstract class Item extends ElementObj {
    protected int value;
    protected String displayName;

    public Item() {
        super();
        this.displayName = getClass().getSimpleName();
    }

    public Item(int x, int y, int w, int h, ImageIcon icon, int value) {
        super(x, y, w, h, icon);
        this.value = value;
        this.displayName = getClass().getSimpleName();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemType getItemType() {
        return ItemType.CONSUMABLE;
    }

    public boolean isEquipable() {
        return this instanceof EquipableItem;
    }

    public abstract void applyEffect(WuKong player);

    @Override
    public void showElement(Graphics g) {
        int screenX = this.getX() + MapObj.bgOffsetX;
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(), screenX, this.getY(), this.getW(), this.getH(), null);
        } else {
            g.setColor(getItemColor());
            g.fillOval(screenX, this.getY(), this.getW(), this.getH());
            g.setColor(Color.WHITE);
            g.drawOval(screenX, this.getY(), this.getW(), this.getH());
        }
    }

    protected abstract Color getItemColor();

    @Override
    protected void move() {
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        int w = (split.length > 2) ? Integer.parseInt(split[2]) : 30;
        int h = (split.length > 3) ? Integer.parseInt(split[3]) : 30;

        this.setX(x);
        this.setY(y);
        this.setW(w);
        this.setH(h);

        return this;
    }
}
