package com.tedu.element;

import java.awt.Color;

import javax.swing.ImageIcon;

public class ArmorItem extends Item implements EquipableItem {
    private final int hpBonus;

    public ArmorItem() {
        this("Armor", 40, null);
    }

    public ArmorItem(String name, int hpBonus, ImageIcon icon) {
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
        return new Color(95, 111, 135);
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
