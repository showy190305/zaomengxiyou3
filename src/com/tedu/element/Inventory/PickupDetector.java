package com.tedu.element.Inventory;

import java.util.List;

import com.tedu.element.ElementObj;
import com.tedu.element.WuKong;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * 拾取检测器
 * 每帧检测玩家与道具的碰撞，自动拾取道具到背包
 */
public class PickupDetector {
	
	private static final int PICKUP_DISTANCE = 40; // 拾取距离阈值
	
	private ElementManager elementManager;
	
	public PickupDetector() {
		this.elementManager = ElementManager.getManager();
	}
	
	/**
	 * 检测并拾取玩家附近的道具
	 */
	public void detectAndPickup(WuKong player) {
		if (player == null) return;
		
		Inventory inventory = Inventory.getInstance();
		List<ElementObj> items = elementManager.getElementsByKey(GameElement.ITEM);
		
		for (ElementObj obj : items) {
			if (!obj.isLive()) continue;
			
			if (!(obj instanceof Item)) continue;
			
			Item item = (Item) obj;
			
			// 检测碰撞
			if (player.pk(item) || isWithinDistance(player, item, PICKUP_DISTANCE)) {
				// 尝试添加到背包
				int added = inventory.addItem(item);
				if (added > 0) {
					// 拾取成功，从地图上移除
					item.setLive(false);
					System.out.println("拾取道具: " + item.getClass().getSimpleName());
				}
			}
		}
	}
	
	/**
	 * 检测两个对象是否在指定距离内
	 */
	private boolean isWithinDistance(ElementObj a, ElementObj b, int distance) {
		int dx = (a.getX() + a.getW() / 2) - (b.getX() + b.getW() / 2);
		int dy = (a.getY() + a.getH() / 2) - (b.getY() + b.getH() / 2);
		return (dx * dx + dy * dy) <= (distance * distance);
	}
}
