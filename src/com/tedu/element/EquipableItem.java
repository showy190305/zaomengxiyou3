package com.tedu.element;

public interface EquipableItem {
    EquipmentSlot getEquipmentSlot();

    void onEquip(WuKong player);

    void onUnequip(WuKong player);
}
