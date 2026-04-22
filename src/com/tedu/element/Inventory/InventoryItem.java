package com.tedu.element.Inventory;

import com.tedu.element.Inventory.BloodPotion;
import com.tedu.element.Inventory.ManaPotion;

/**
 * 背包中的物品包装类
 * 持有 Item 引用 + 数量，支持堆叠（最多99个）
 */
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
	
	/**
	 * 增加堆叠数量
	 * @return 实际增加的数量
	 */
	public int addQuantity(int amount) {
		int canAdd = Math.min(amount, MAX_STACK - quantity);
		quantity += canAdd;
		return canAdd;
	}
	
	/**
	 * 减少数量
	 * @return 减少后是否还剩余
	 */
	public boolean decreaseQuantity() {
		quantity--;
		return quantity > 0;
	}
	
	/**
	 * 是否可以堆叠（相同类型的道具）
	 */
	public boolean canStackWith(Item other) {
		return this.item.getClass() == other.getClass();
	}
	
	public boolean isFull() {
		return quantity >= MAX_STACK;
	}
	
	public String getDisplayName() {
		if (item instanceof BloodPotion) {
			return "BloodPotion";
		} else if (item instanceof ManaPotion) {
			return "ManaPotion";
		}
		return item.getClass().getSimpleName();
	}
}