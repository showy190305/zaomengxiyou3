package com.tedu.element;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private static final int MAX_SLOTS = 24;

    private static Inventory instance;

    private List<InventoryItem> items;
    private InventoryItem equippedWeapon;
    private InventoryItem equippedArmor;

    private Inventory() {
        items = new ArrayList<>();
    }

    public static synchronized Inventory getInstance() {
        if (instance == null) {
            instance = new Inventory();
        }
        return instance;
    }

    public int addItem(Item item) {
        for (InventoryItem invItem : items) {
            if (invItem.canStackWith(item) && !invItem.isFull()) {
                return invItem.addQuantity(1);
            }
        }

        if (items.size() >= MAX_SLOTS) {
            return 0;
        }

        items.add(new InventoryItem(item, 1));
        return 1;
    }

    public boolean useItem(int index, WuKong player) {
        if (index < 0 || index >= items.size() || player == null) {
            return false;
        }

        InventoryItem invItem = items.get(index);
        if (invItem.getItem().isEquipable()) {
            return equipItem(index, player);
        }

        invItem.getItem().applyEffect(player);
        if (!invItem.decreaseQuantity()) {
            items.remove(index);
        }
        return true;
    }

    public boolean equipItem(int index, WuKong player) {
        if (index < 0 || index >= items.size() || player == null) {
            return false;
        }

        InventoryItem invItem = items.get(index);
        if (!(invItem.getItem() instanceof EquipableItem)) {
            return false;
        }

        EquipableItem equipable = (EquipableItem) invItem.getItem();
        InventoryItem oldEquipped = null;
        if (equipable.getEquipmentSlot() == EquipmentSlot.WEAPON) {
            oldEquipped = equippedWeapon;
            equippedWeapon = invItem;
        } else if (equipable.getEquipmentSlot() == EquipmentSlot.ARMOR) {
            oldEquipped = equippedArmor;
            equippedArmor = invItem;
        }

        items.remove(index);
        if (oldEquipped != null) {
            ((EquipableItem) oldEquipped.getItem()).onUnequip(player);
            items.add(oldEquipped);
        }
        equipable.onEquip(player);
        return true;
    }

    public boolean unequip(EquipmentSlot slot, WuKong player) {
        if (player == null || items.size() >= MAX_SLOTS) {
            return false;
        }

        InventoryItem equipped = slot == EquipmentSlot.WEAPON ? equippedWeapon : equippedArmor;
        if (equipped == null) {
            return false;
        }

        ((EquipableItem) equipped.getItem()).onUnequip(player);
        items.add(equipped);
        if (slot == EquipmentSlot.WEAPON) {
            equippedWeapon = null;
        } else {
            equippedArmor = null;
        }
        return true;
    }

    public List<InventoryItem> getItems() {
        return items;
    }

    public int getMaxSlots() {
        return MAX_SLOTS;
    }

    public InventoryItem getEquippedWeapon() {
        return equippedWeapon;
    }

    public InventoryItem getEquippedArmor() {
        return equippedArmor;
    }

    public void clear() {
        items.clear();
        equippedWeapon = null;
        equippedArmor = null;
    }

    public static void reset() {
        instance = null;
    }
}
