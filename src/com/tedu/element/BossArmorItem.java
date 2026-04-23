package com.tedu.element;

import java.awt.Color;

import javax.swing.ImageIcon;

/**
 * Boss2掉落的专属护甲 - 锁子黄金甲
 */
public class BossArmorItem extends Item implements EquipableItem {
    private final int hpBonus;

    public BossArmorItem() {
        this("锁子黄金甲", 80, new javax.swing.ImageIcon("image/icons/armor_icon.png"));
    }

    public BossArmorItem(int x, int y) {
        super(x, y, 30, 30, new javax.swing.ImageIcon("image/icons/armor_icon.png"), 80);
        this.hpBonus = 80;
        this.displayName = "锁子黄金甲";
    }

    public BossArmorItem(String name, int hpBonus, ImageIcon icon) {
        super(0, 0, 30, 30, icon, hpBonus);
        this.hpBonus = hpBonus;
        this.displayName = name;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.ARMOR;
    }

    @Override
    public void applyEffect(WuKong player) {
    }

    @Override
    protected Color getItemColor() {
        return new Color(255, 140, 0); // 橙金色
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.ARMOR;
    }

    @Override
    public void onEquip(WuKong player) {
        if (player != null) {
            player.addMaxHp(hpBonus);
            player.setArmorEquipped(true);
        }
    }

    @Override
    public void onUnequip(WuKong player) {
        if (player != null) {
            player.addMaxHp(-hpBonus);
            player.setArmorEquipped(false);
        }
    }
}
