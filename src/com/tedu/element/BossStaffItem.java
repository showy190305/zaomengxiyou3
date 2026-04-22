package com.tedu.element;

import java.awt.Color;

import javax.swing.ImageIcon;

/**
 * Boss1掉落的专属武器 - 金箍棒
 */
public class BossStaffItem extends Item implements EquipableItem {
    private final int attackBonus;

    public BossStaffItem() {
        this("金箍棒", 30, new javax.swing.ImageIcon("image/icons/staff_icon.png"));
    }

    public BossStaffItem(int x, int y) {
        super(x, y, 30, 30, new javax.swing.ImageIcon("image/icons/staff_icon.png"), 30);
        this.attackBonus = 30;
        this.displayName = "金箍棒";
    }

    public BossStaffItem(String name, int attackBonus, ImageIcon icon) {
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
        return new Color(255, 215, 0); // 金色
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.WEAPON;
    }

    @Override
    public void onEquip(WuKong player) {
        if (player != null) {
            player.addAttackPower(attackBonus);
            setWeaponEquipped(true);
        }
    }

    @Override
    public void onUnequip(WuKong player) {
        if (player != null) {
            player.addAttackPower(-attackBonus);
            setWeaponEquipped(false);
        }
    }

    private void setWeaponEquipped(boolean equipped) {
        try {
            java.util.List<ElementObj> plays = com.tedu.manager.ElementManager.getManager()
                .getElementsByKey(com.tedu.manager.GameElement.PLAY);
            for (ElementObj obj : plays) {
                if (obj instanceof com.tedu.element.Weapon) {
                    ((com.tedu.element.Weapon) obj).setStaffEquipped(equipped);
                    System.out.println("武器状态已更新: " + (equipped ? "装备金箍棒" : "卸下金箍棒"));
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("设置武器状态失败: " + e.getMessage());
        }
    }
}
