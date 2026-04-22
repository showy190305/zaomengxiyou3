package com.tedu.element;

public class InventoryItem {
    private static final int MAX_STACK = 99;

    private Item item;
    private int quantity;

    public InventoryItem(Item item) {
        this.item = item;
        this.quantity = 1;
    }

    public InventoryItem(Item item, int quantity) {
        this.item = item;
        this.quantity = Math.min(quantity, MAX_STACK);
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public int addQuantity(int amount) {
        int canAdd = Math.min(amount, MAX_STACK - quantity);
        quantity += canAdd;
        return canAdd;
    }

    public boolean decreaseQuantity() {
        quantity--;
        return quantity > 0;
    }

    public boolean canStackWith(Item other) {
        return this.item.getClass() == other.getClass() && !item.isEquipable();
    }

    public boolean isFull() {
        return quantity >= MAX_STACK;
    }

    public String getDisplayName() {
        return item.getDisplayName();
    }
}
