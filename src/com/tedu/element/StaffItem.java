package com.tedu.element;

import java.awt.Color;

import javax.swing.ImageIcon;

public class StaffItem extends Item implements EquipableItem {
    private final int attackBonus;

    public StaffItem() {
        this("Staff", 18, null);
    }

    public StaffItem(String name, int attackBonus, ImageIcon icon) {
        super(0, 0, 30, 30, icon, attackBonus);
        this.attackBonus = attackBonus;
        this.displayName = name;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.WEAPON;
    }

    @Override
    public void applyEffect(WuKong player) {
    }

    @Override
    protected Color getItemColor() {
        return new Color(123, 72, 29);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.WEAPON;
    }

    @Override
    public void onEquip(WuKong player) {
        if (player != null) {
            player.addAttackPower(attackBonus);
        }
    }

    @Override
    public void onUnequip(WuKong player) {
        if (player != null) {
            player.addAttackPower(-attackBonus);
        }
    }
}
