package com.tedu.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

/**
 * 背包UI面板
 * 在游戏窗口内绘制覆盖层，显示背包中的物品，支持点击使用
 */
public class InventoryPanel extends JPanel {
	
	private static final int PANEL_WIDTH = 480;
	private static final int PANEL_HEIGHT = 360;
	private static final int GRID_COLS = 6;
	private static final int GRID_ROWS = 4;
	private static final int SLOT_SIZE = 64;
	private static final int SLOT_GAP = 8;
	private static final int PADDING = 20;
	
	private Inventory inventory;
	private WuKong player;
	private boolean visible = false;
	
	public InventoryPanel(WuKong player) {
		this.player = player;
		this.inventory = Inventory.getInstance();
		
		// 计算面板实际大小
		int contentWidth = GRID_COLS * SLOT_SIZE + (GRID_COLS - 1) * SLOT_GAP;
		int contentHeight = GRID_ROWS * SLOT_SIZE + (GRID_ROWS - 1) * SLOT_GAP;
		this.setPreferredSize(new java.awt.Dimension(
			contentWidth + PADDING * 2,
			contentHeight + PADDING * 2 + 30 // 30 for title area
		));
	}
	
	/**
	 * 切换背包显示/隐藏
	 */
	public void toggleVisibility() {
		visible = !visible;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * 处理鼠标点击，判断是否点击了某个格子
	 * 由调用方面板转发鼠标事件到此方法
	 */
	public void handleClick(int x, int y) {
		if (!visible) return;
		
		int contentWidth = GRID_COLS * SLOT_SIZE + (GRID_COLS - 1) * SLOT_GAP;
		int contentHeight = GRID_ROWS * SLOT_SIZE + (GRID_ROWS - 1) * SLOT_GAP;
		int offsetX = (getWidth() - contentWidth) / 2;
		int offsetY = (getHeight() - contentHeight) / 2 + 30;
		
		// 计算点击的是哪个格子
		for (int row = 0; row < GRID_ROWS; row++) {
			for (int col = 0; col < GRID_COLS; col++) {
				int slotX = offsetX + col * (SLOT_SIZE + SLOT_GAP);
				int slotY = offsetY + row * (SLOT_SIZE + SLOT_GAP);
				
				if (x >= slotX && x < slotX + SLOT_SIZE &&
					y >= slotY && y < slotY + SLOT_SIZE) {
					
					int index = row * GRID_COLS + col;
					List<InventoryItem> items = inventory.getItems();
					if (index < items.size()) {
						// 使用道具
						inventory.useItem(index, player);
						repaint();
						return;
					}
				}
			}
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (!visible) return;
		
		// 绘制半透明黑色背景
		g.setColor(new Color(0, 0, 0, 180));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		// 绘制背包面板背景
		int contentWidth = GRID_COLS * SLOT_SIZE + (GRID_COLS - 1) * SLOT_GAP;
		int contentHeight = GRID_ROWS * SLOT_SIZE + (GRID_ROWS - 1) * SLOT_GAP;
		int offsetX = (getWidth() - contentWidth) / 2;
		int offsetY = (getHeight() - contentHeight) / 2 + 30;
		
		g.setColor(new Color(40, 40, 60));
		g.fillRect(offsetX - 5, offsetY - 5, contentWidth + 10, contentHeight + 10);
		g.setColor(new Color(100, 100, 140));
		g.drawRect(offsetX - 5, offsetY - 5, contentWidth + 10, contentHeight + 10);
		
		// 绘制标题
		g.setColor(Color.WHITE);
		g.setFont(new Font("SansSerif", Font.BOLD, 16));
		g.drawString("Inventory (Click to Use)", offsetX, offsetY - 12);
		
		// 绘制格子
		List<InventoryItem> items = inventory.getItems();
		for (int row = 0; row < GRID_ROWS; row++) {
			for (int col = 0; col < GRID_COLS; col++) {
				int index = row * GRID_COLS + col;
				int slotX = offsetX + col * (SLOT_SIZE + SLOT_GAP);
				int slotY = offsetY + row * (SLOT_SIZE + SLOT_GAP);
				
				// 格子背景
				g.setColor(new Color(60, 60, 80));
				g.fillRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
				g.setColor(new Color(120, 120, 160));
				g.drawRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
				
				// 绘制物品
				if (index < items.size()) {
					InventoryItem invItem = items.get(index);
					Item item = invItem.getItem();
					
					// 绘制道具图标（缩放适配格子大小）
					if (item.getIcon() != null) {
						g.drawImage(item.getIcon().getImage(),
							slotX + 4, slotY + 4,
							SLOT_SIZE - 8, SLOT_SIZE - 8, null);
					} else {
						// 没有图标时用占位色
						g.setColor(item.getItemColor());
						g.fillOval(slotX + 8, slotY + 8, SLOT_SIZE - 16, SLOT_SIZE - 16);
					}
					
					// 绘制数量
					if (invItem.getQuantity() > 1) {
						g.setColor(Color.WHITE);
						g.setFont(new Font("SansSerif", Font.BOLD, 12));
						String qtyText = "x" + invItem.getQuantity();
						g.drawString(qtyText, slotX + SLOT_SIZE - 20, slotY + SLOT_SIZE - 6);
					}
					
					// 绘制道具名称
					g.setColor(new Color(200, 200, 255));
					g.setFont(new Font("SansSerif", Font.PLAIN, 10));
					String name = invItem.getDisplayName();
					int nameWidth = g.getFontMetrics().stringWidth(name);
					g.drawString(name, slotX + (SLOT_SIZE - nameWidth) / 2, slotY + SLOT_SIZE - 2);
				}
			}
		}
	}
}
