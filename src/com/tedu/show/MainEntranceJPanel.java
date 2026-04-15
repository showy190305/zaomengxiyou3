package com.tedu.show;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * @说明 游戏主入口菜单面板
 * @author L1r0gn
 * @功能说明 显示开始游戏、退出游戏按钮，游戏图标占位框，背景占位
 */
public class MainEntranceJPanel extends JPanel {
	private static final int START_BTN_X = 60;
	private static final int START_BTN_Y = 120;
	private static final int START_BTN_W = 180;
	private static final int START_BTN_H = 50;

	private static final int EXIT_BTN_X = 60;
	private static final int EXIT_BTN_Y = 200;
	private static final int EXIT_BTN_W = 180;
	private static final int EXIT_BTN_H = 50;

	private static final int ICON_X = 620;
	private static final int ICON_Y = 30;
	private static final int ICON_W = 150;
	private static final int ICON_H = 150;

	private boolean startHover = false;
	private boolean exitHover = false;

	private GameMainJPanel gamePanel;

	public MainEntranceJPanel() {
		init();
	}

	public void setGameMainPanel(GameMainJPanel gamePanel) {
		this.gamePanel = gamePanel;
	}

	private void init() {
		setLayout(null);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleClick(e.getX(), e.getY());
			}
		});

		addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				handleHover(e.getX(), e.getY());
			}
		});
	}

	private void handleClick(int x, int y) {
		if (x >= START_BTN_X && x <= START_BTN_X + START_BTN_W && y >= START_BTN_Y && y <= START_BTN_Y + START_BTN_H) {
			if (gamePanel == null) return;

			// 获取窗口对象
			GameJFrame frame = (GameJFrame) getTopLevelAncestor();

			// 移除菜单面板，换上游戏面板
			frame.getContentPane().removeAll();
			for (KeyListener kl : frame.getKeyListeners()) {
				frame.removeKeyListener(kl);
			}

			// 4. 手动“注入”并配置游戏面板
			frame.setLayout(new BorderLayout());
			frame.add(gamePanel, BorderLayout.CENTER);

			// 5. 如果游戏面板需要键盘控制，手动添加监听
			if (gamePanel instanceof KeyListener) {
				frame.addKeyListener((KeyListener) gamePanel);
			}

			// 6. 调用刷新（不调用这个，界面不会变）
			frame.revalidate();
			frame.repaint();

			// 7. 手动启动线程（对应你 JFrame 里的 start 逻辑）
			if (gamePanel instanceof Runnable) {
				Thread renderThread = new Thread((Runnable) gamePanel);
				renderThread.start();
				System.out.println("游戏逻辑线程已启动");
			}

			// 8. 强行让游戏面板获取焦点（不加这句，键盘控制会失效）
			gamePanel.setFocusable(true);
			gamePanel.requestFocusInWindow();

			return;

		}
		if (x >= EXIT_BTN_X && x <= EXIT_BTN_X + EXIT_BTN_W
				&& y >= EXIT_BTN_Y && y <= EXIT_BTN_Y + EXIT_BTN_H) {
			System.exit(0);
		}
	}

	private void handleHover(int x, int y) {
		boolean newStartHover = (x >= START_BTN_X && x <= START_BTN_X + START_BTN_W
				&& y >= START_BTN_Y && y <= START_BTN_Y + START_BTN_H);
		boolean newExitHover = (x >= EXIT_BTN_X && x <= EXIT_BTN_X + EXIT_BTN_W
				&& y >= EXIT_BTN_Y && y <= EXIT_BTN_Y + EXIT_BTN_H);
		if (newStartHover != startHover || newExitHover != exitHover) {
			startHover = newStartHover;
			exitHover = newExitHover;
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawBackground(g2);
		drawTitle(g2);
		drawButton(g2, START_BTN_X, START_BTN_Y, START_BTN_W, START_BTN_H, "开始游戏", startHover);
		drawButton(g2, EXIT_BTN_X, EXIT_BTN_Y, EXIT_BTN_W, EXIT_BTN_H, "退出游戏", exitHover);
		drawIconBox(g2);
	}

	private void drawBackground(Graphics2D g2) {
		int w = getWidth();
		int h = getHeight();
		g2.setColor(new Color(20, 20, 40));
		g2.fillRect(0, 0, w, h);
		g2.setColor(new Color(80, 80, 120));
		g2.setFont(new Font("SansSerif", Font.ITALIC, 16));
		String text = "[ 背景图片占位区域 ]";
		int tw = g2.getFontMetrics().stringWidth(text);
		g2.drawString(text, (w - tw) / 2, h / 2);
	}

	private void drawTitle(Graphics2D g2) {
		g2.setColor(new Color(255, 220, 100));
		g2.setFont(new Font("SansSerif", Font.BOLD, 36));
		String title = "造梦西游";
		int tw = g2.getFontMetrics().stringWidth(title);
		g2.drawString(title, (GameJFrame.GameX - tw) / 2, 70);
	}

	private void drawButton(Graphics2D g2, int x, int y, int w, int h, String text, boolean hover) {
		g2.setColor(hover ? new Color(80, 160, 255) : new Color(50, 100, 180));
		g2.fillRoundRect(x, y, w, h, 10, 10);
		g2.setColor(new Color(100, 180, 255));
		g2.drawRoundRect(x, y, w, h, 10, 10);
		g2.setColor(Color.WHITE);
		g2.setFont(new Font("SansSerif", Font.BOLD, 20));
		int tw = g2.getFontMetrics().stringWidth(text);
		int tx = x + (w - tw) / 2;
		int ty = y + (h + g2.getFontMetrics().getHeight()) / 2 - 5;
		g2.drawString(text, tx, ty);
	}

	private void drawIconBox(Graphics2D g2) {
		g2.setColor(new Color(40, 40, 70));
		g2.fillRoundRect(ICON_X, ICON_Y, ICON_W, ICON_H, 8, 8);
		g2.setColor(new Color(100, 100, 150));
		g2.drawRoundRect(ICON_X, ICON_Y, ICON_W, ICON_H, 8, 8);
		g2.setColor(new Color(120, 120, 160));
		g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
		String text = "[ 游戏图标 ]";
		int tw = g2.getFontMetrics().stringWidth(text);
		g2.drawString(text, ICON_X + (ICON_W - tw) / 2, ICON_Y + ICON_H / 2);
	}
}
