package com.tedu.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tedu.element.WuKong;
import com.tedu.element.Inventory.BloodPotion;
import com.tedu.element.Inventory.Inventory;
import com.tedu.element.Inventory.InventoryItem;
import com.tedu.element.Inventory.InventoryPanel;
import com.tedu.element.Inventory.ManaPotion;
import com.tedu.element.Inventory.PickupDetector;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * Inventory System Test
 * Tests inventory item adding, stacking, usage, and pickup detection
 *
 * Controls:
 * A/D - Move Wukong
 * B   - Open/Close Inventory
 * 1   - Add a Blood Potion to inventory
 * 2   - Add a Mana Potion to inventory
 * 3   - Batch add 5 Blood Potions to inventory
 * Space - Drain HP/MP to 1, for testing potion effects
 */
public class InventoryTest extends JPanel implements KeyListener {
    private WuKong wukong;
    private PickupDetector pickupDetector;
    private InventoryPanel inventoryPanel;
    private Inventory inventory;

    // Items on the ground
    private BloodPotion bloodPotion1;
    private BloodPotion bloodPotion2;
    private ManaPotion manaPotion1;

    private Thread gameThread;
    private boolean running = true;

    // Track if Wukong sprite loaded successfully
    private boolean spriteLoaded = false;

    public InventoryTest() {
        // Reset inventory singleton to avoid data from previous tests
        Inventory.reset();

        // Initialize inventory
        inventory = Inventory.getInstance();

        // Initialize Wukong
        wukong = new WuKong();
        wukong.createElement("100,400,150,150");
        spriteLoaded = wukong.getTotalFrames() > 0;
        System.out.println("Wukong frames loaded: " + spriteLoaded + " (total: " + wukong.getTotalFrames() + ")");

        // Initialize pickup detector
        pickupDetector = new PickupDetector();

        // Initialize inventory panel
        inventoryPanel = new InventoryPanel(wukong);
        inventoryPanel.setSize(800, 600);

        // Place items on the ground
        bloodPotion1 = new BloodPotion(300, 450);
        bloodPotion2 = new BloodPotion(500, 450);
        manaPotion1 = new ManaPotion(650, 450);

        // Register ground items to ElementManager so PickupDetector can find them
        ElementManager em = ElementManager.getManager();
        em.addElement(bloodPotion1, GameElement.ITEM);
        em.addElement(bloodPotion2, GameElement.ITEM);
        em.addElement(manaPotion1, GameElement.ITEM);

        // Add key listener
        addKeyListener(this);
        setFocusable(true);
        setBackground(Color.BLACK);

        // Add mouse listener to forward clicks to inventory panel
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (inventoryPanel.isVisible()) {
                    inventoryPanel.handleClick(e.getX(), e.getY());
                    repaint();
                }
            }
        });

        // Start game loop thread
        gameThread = new Thread(new GameLoopRunnable(this));
        gameThread.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw title and controls
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g.drawString("=== Inventory System Test ===", 10, 20);
        g.drawString("A/D: Move  |  B: Toggle Inventory  |  1/2/3: Add items  |  Space: Drain HP/MP", 10, 40);
        g.drawString("Walk near items to auto-pickup. Press B, then click a slot to use.", 10, 58);

        // Draw Wukong HP and MP
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.drawString("HP: " + wukong.getHp() + " / " + wukong.getMaxHp(), 10, 82);
        g.drawString("MP: " + wukong.getMp() + " / " + wukong.getMaxMp(), 10, 100);

        // Draw inventory info
        g.setColor(Color.CYAN);
        List<InventoryItem> items = inventory.getItems();
        g.drawString("Inventory: " + items.size() + " / " + inventory.getMaxSlots() + " slots", 10, 124);

        // List inventory items
        int y = 142;
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        for (int i = 0; i < items.size(); i++) {
            InventoryItem invItem = items.get(i);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("[" + i + "] " + invItem.getDisplayName() + " x" + invItem.getQuantity(), 10, y);
            y += 16;
        }

        // Draw Wukong (use showElement for proper sprite rendering)
        if (spriteLoaded) {
            wukong.showElement(g);
        } else {
            // Fallback: draw a yellow circle
            g.setColor(Color.YELLOW);
            g.fillOval(wukong.getX(), wukong.getY(), wukong.getW(), wukong.getH());
            g.setColor(Color.BLACK);
            g.drawString("Wukong", wukong.getX() + 10, wukong.getY() + 30);
        }

        // Draw items on the ground
        if (bloodPotion1.isLive()) {
            bloodPotion1.showElement(g);
            g.setColor(Color.RED);
            g.drawString("BloodPotion1", bloodPotion1.getX(), bloodPotion1.getY() - 10);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("BloodPotion1 picked up", bloodPotion1.getX(), bloodPotion1.getY());
        }

        if (bloodPotion2.isLive()) {
            bloodPotion2.showElement(g);
            g.setColor(Color.RED);
            g.drawString("BloodPotion2", bloodPotion2.getX(), bloodPotion2.getY() - 10);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("BloodPotion2 picked up", bloodPotion2.getX(), bloodPotion2.getY());
        }

        if (manaPotion1.isLive()) {
            manaPotion1.showElement(g);
            g.setColor(Color.BLUE);
            g.drawString("ManaPotion1", manaPotion1.getX(), manaPotion1.getY() - 10);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("ManaPotion1 picked up", manaPotion1.getX(), manaPotion1.getY());
        }

        // Pickup detection
        pickupDetector.detectAndPickup(wukong);

        // Draw inventory overlay
        if (inventoryPanel.isVisible()) {
            inventoryPanel.paintComponent(g);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // B key - toggle inventory
        if (key == 'B' || key == 'b') {
            inventoryPanel.toggleVisibility();
            repaint();
            return;
        }

        // 1 key - add Blood Potion to inventory
        if (key == KeyEvent.VK_1) {
            BloodPotion potion = new BloodPotion(0, 0);
            int added = inventory.addItem(potion);
            System.out.println("Added Blood Potion, count: " + added);
            repaint();
            return;
        }

        // 2 key - add Mana Potion to inventory
        if (key == KeyEvent.VK_2) {
            ManaPotion potion = new ManaPotion(0, 0);
            int added = inventory.addItem(potion);
            System.out.println("Added Mana Potion, count: " + added);
            repaint();
            return;
        }

        // 3 key - batch add 5 Blood Potions
        if (key == KeyEvent.VK_3) {
            int totalAdded = 0;
            for (int i = 0; i < 5; i++) {
                BloodPotion potion = new BloodPotion(0, 0);
                totalAdded += inventory.addItem(potion);
            }
            System.out.println("Batch added 5 Blood Potions, actual added: " + totalAdded);
            repaint();
            return;
        }

        // Space key - drain HP/MP to 1 for testing
        if (key == KeyEvent.VK_SPACE) {
            wukong.setHp(1);
            wukong.setMp(1);
            System.out.println("HP/MP drained to 1");
            repaint();
            return;
        }

        // Other keys handled by Wukong (A/D movement)
        wukong.keyClick(true, key);
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        wukong.keyClick(false, e.getKeyCode());
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    // Game main loop thread
    private static class GameLoopRunnable implements Runnable {
        private InventoryTest panel;
        public GameLoopRunnable(InventoryTest panel) {
            this.panel = panel;
        }
        @Override
        public void run() {
            final int FPS = 60;
            final int frameTime = 1000 / FPS;
            while (panel.running) {
                long start = System.currentTimeMillis();
                panel.repaint();
                long cost = System.currentTimeMillis() - start;
                long sleep = Math.max(2, frameTime - cost);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Inventory System Test");
        InventoryTest testPanel = new InventoryTest();

        frame.add(testPanel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
