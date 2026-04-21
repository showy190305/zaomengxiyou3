package com.tedu.element;

import java.util.ArrayList;
import java.util.List;

/**
 * 背包数据管理类（单例）
 * 管理背包中的物品列表，提供添加、移除、使用道具等功能
 */
public class Inventory {
	
	private static final int MAX_SLOTS = 24; // 6列 x 4行
	
	private static Inventory instance;
	
	private List<InventoryItem> items;
	
	private Inventory() {
		items = new ArrayList<>();
	}
	
	public static synchronized Inventory getInstance() {
		if (instance == null) {
			instance = new Inventory();
		}
		return instance;
	}
	
	/**
	 * 添加物品到背包
	 * @param item 要添加的道具
	 * @return 实际添加的数量（可能因为背包满或堆叠上限而未全部添加）
	 */
	public int addItem(Item item) {
		// 尝试堆叠到已有物品
		for (InventoryItem invItem : items) {
			if (invItem.canStackWith(item) && !invItem.isFull()) {
				int added = invItem.addQuantity(1);
				return added;
			}
		}
		
		// 背包已满
		if (items.size() >= MAX_SLOTS) {
			return 0;
		}
		
		// 新建一个格子
		InventoryItem newItem = new InventoryItem(item, 1);
		items.add(newItem);
		return 1;
	}
	
	/**
	 * 使用指定位置的道具
	 * @param index 背包中的索引
	 * @param player 使用道具的玩家
	 * @return 是否使用成功
	 */
	public boolean useItem(int index, WuKong player) {
		if (index < 0 || index >= items.size() || player == null) {
			return false;
		}
		
		InventoryItem invItem = items.get(index);
		invItem.getItem().applyEffect(player);
		
		// 数量减为0时移除
		if (!invItem.decreaseQuantity()) {
			items.remove(index);
		}
		
		return true;
	}
	
	/**
	 * 获取背包物品列表
	 */
	public List<InventoryItem> getItems() {
		return items;
	}
	
	/**
	 * 获取背包格子数
	 */
	public int getMaxSlots() {
		return MAX_SLOTS;
	}
	
	/**
	 * 清空背包（用于测试或重置）
	 */
	public void clear() {
		items.clear();
	}
	
	/**
	 * 重置单例（用于测试）
	 */
	public static void reset() {
		instance = null;
	}
}
