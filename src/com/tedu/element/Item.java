package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;

/**
 * 道具基类,继承自ElementObj
 * 所有道具(血瓶、蓝瓶等)都应继承此类
 */
public abstract class Item extends ElementObj {
	
	// 道具恢复的数值
	protected int value;
	
	public Item() {
		super();
	}
	
	public Item(int x, int y, int w, int h, ImageIcon icon, int value) {
		super(x, y, w, h, icon);
		this.value = value;
	}
	
	/**
	 * 获取道具恢复的数值
	 */
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	/**
	 * 道具被拾取时触发的效果
	 * 由子类具体实现恢复逻辑
	 */
	public abstract void applyEffect(WuKong player);
	
	/**
	 * 默认使用红色矩形作为占位显示
	 * 后续可替换为实际贴图
	 */
	@Override
	public void showElement(Graphics g) {
		if (this.getIcon() != null) {
			// 如果有贴图则使用贴图
			g.drawImage(this.getIcon().getImage(),
					this.getX(), this.getY(),
					this.getW(), this.getH(), null);
		} else {
			// 没有贴图时使用彩色几何图形作为占位
			g.setColor(getItemColor());
			g.fillOval(this.getX(), this.getY(), this.getW(), this.getH());
			// 绘制边框
			g.setColor(Color.WHITE);
			g.drawOval(this.getX(), this.getY(), this.getW(), this.getH());
		}
	}
	
	/**
	 * 子类返回道具的颜色(用于占位显示)
	 */
	protected abstract Color getItemColor();
	
	@Override
	protected void move() {
		// 道具不需要移动
	}
	
	@Override
	public ElementObj createElement(String str) {
		// 解析格式: "x,y" 或 "x,y,width,height"
		String[] split = str.split(",");
		int x = Integer.parseInt(split[0]);
		int y = Integer.parseInt(split[1]);
		int w = (split.length > 2) ? Integer.parseInt(split[2]) : 30;
		int h = (split.length > 3) ? Integer.parseInt(split[3]) : 30;
		
		this.setX(x);
		this.setY(y);
		this.setW(w);
		this.setH(h);
		
		return this;
	}
}
