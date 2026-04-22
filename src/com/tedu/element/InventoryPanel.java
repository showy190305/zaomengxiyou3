package com.tedu.element;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

public class InventoryPanel extends JPanel {
    private static final int GRID_COLS = 6;
    private static final int GRID_ROWS = 4;
    private static final int SLOT_SIZE = 64;
    private static final int SLOT_GAP = 10;

    private final Inventory inventory;
    private final WuKong player;
    private boolean visible = false;

    // Cached sprite sheets for preview
    private java.awt.image.BufferedImage wukongSprite;
    private java.awt.image.BufferedImage armorSprite;
    private java.awt.image.BufferedImage weaponSprite;
    private java.awt.image.BufferedImage staffSprite;
    private boolean spritesLoaded = false;

    private final Rectangle leftPanelRect = new Rectangle();
    private final Rectangle rightPanelRect = new Rectangle();
    private final Rectangle previewRect = new Rectangle();
    private final Rectangle weaponSlotRect = new Rectangle();
    private final Rectangle armorSlotRect = new Rectangle();
    private final Rectangle[] bagSlotRects = new Rectangle[GRID_COLS * GRID_ROWS];

    public InventoryPanel(WuKong player) {
        this.player = player;
        this.inventory = Inventory.getInstance();
        for (int i = 0; i < bagSlotRects.length; i++) {
            bagSlotRects[i] = new Rectangle();
        }
    }

    public void toggleVisibility() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void handleClick(int x, int y) {
        if (!visible) {
            return;
        }

        updateLayout();
        if (weaponSlotRect.contains(x, y)) {
            inventory.unequip(EquipmentSlot.WEAPON, player);
            repaint();
            return;
        }
        if (armorSlotRect.contains(x, y)) {
            inventory.unequip(EquipmentSlot.ARMOR, player);
            repaint();
            return;
        }

        List<InventoryItem> items = inventory.getItems();
        for (int i = 0; i < bagSlotRects.length; i++) {
            if (!bagSlotRects[i].contains(x, y) || i >= items.size()) {
                continue;
            }
            Item item = items.get(i).getItem();
            if (item.isEquipable()) {
                inventory.equipItem(i, player);
            } else {
                inventory.useItem(i, player);
            }
            repaint();
            return;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!visible) {
            return;
        }

        updateLayout();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 175));
        g2.fillRect(0, 0, getWidth(), getHeight());

        drawPanelBackground(g2, leftPanelRect);
        drawPanelBackground(g2, rightPanelRect);
        drawHeader(g2);
        drawStats(g2);
        drawPreview(g2);
        drawEquipment(g2);
        drawBag(g2);
        g2.dispose();
    }

    private void updateLayout() {
        int totalWidth = Math.min(980, getWidth() - 80);
        int totalHeight = Math.min(620, getHeight() - 80);
        int startX = (getWidth() - totalWidth) / 2;
        int startY = (getHeight() - totalHeight) / 2;

        int leftWidth = 390;
        leftPanelRect.setBounds(startX, startY, leftWidth, totalHeight);
        rightPanelRect.setBounds(startX + leftWidth + 20, startY, totalWidth - leftWidth - 20, totalHeight);

        previewRect.setBounds(leftPanelRect.x + 28, leftPanelRect.y + 136, 186, 218);
        weaponSlotRect.setBounds(leftPanelRect.x + 252, leftPanelRect.y + 164, 78, 78);
        armorSlotRect.setBounds(leftPanelRect.x + 252, leftPanelRect.y + 264, 78, 78);

        int gridStartX = rightPanelRect.x + 24;
        int gridStartY = rightPanelRect.y + 100;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = row * GRID_COLS + col;
                bagSlotRects[index].setBounds(
                    gridStartX + col * (SLOT_SIZE + SLOT_GAP),
                    gridStartY + row * (SLOT_SIZE + SLOT_GAP),
                    SLOT_SIZE,
                    SLOT_SIZE
                );
            }
        }
    }

    private void drawPanelBackground(Graphics2D g2, Rectangle rect) {
        g2.setColor(new Color(36, 23, 14, 236));
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);
        g2.setColor(new Color(197, 162, 99));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);
    }

    private void drawHeader(Graphics2D g2) {
        g2.setColor(new Color(245, 214, 133));
        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        g2.drawString("Profile", leftPanelRect.x + 18, leftPanelRect.y + 40);

        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.drawString("Bag", rightPanelRect.x + 20, rightPanelRect.y + 40);
    }

    private void drawStats(Graphics2D g2) {
        int x = leftPanelRect.x + 22;
        int topY = leftPanelRect.y + 62;
        int bottomY = leftPanelRect.y + 390;
        int labelW = 86;
        int valueW = 184;
        int rowH = 34;

        drawStatRow(g2, x, topY, labelW, valueW, rowH, "Name", "WuKong");
        drawStatRow(g2, x, topY + rowH + 8, labelW, valueW, rowH, "Level", String.valueOf(player.level));

        drawStatRow(g2, x, bottomY, labelW, valueW, rowH, "HP", player.getHp() + " / " + player.getMaxHp());
        drawStatRow(g2, x, bottomY + rowH + 8, labelW, valueW, rowH, "MP", player.getMp() + " / " + player.getMaxMp());
        drawStatRow(g2, x, bottomY + (rowH + 8) * 2, labelW, valueW, rowH, "ATK", String.valueOf(player.getAttackPower()));
    }

    private void drawStatRow(Graphics2D g2, int x, int y, int labelW, int valueW, int h, String label, String value) {
        g2.setColor(new Color(102, 64, 35));
        g2.fillRoundRect(x, y, labelW, h, 10, 10);
        g2.fillRoundRect(x + labelW + 8, y, valueW, h, 10, 10);
        g2.setColor(new Color(220, 190, 123));
        g2.drawRoundRect(x, y, labelW, h, 10, 10);
        g2.drawRoundRect(x + labelW + 8, y, valueW, h, 10, 10);

        g2.setColor(new Color(244, 225, 173));
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(label, x + 14, y + 22);

        g2.setColor(Color.WHITE);
        g2.drawString(value, x + labelW + 20, y + 22);
    }

    private void drawPreview(Graphics2D g2) {
        g2.setColor(new Color(86, 54, 28));
        g2.fillRoundRect(previewRect.x, previewRect.y, previewRect.width, previewRect.height, 18, 18);
        g2.setColor(new Color(198, 164, 106));
        g2.drawRoundRect(previewRect.x, previewRect.y, previewRect.width, previewRect.height, 18, 18);

        int drawW = 150;
        int drawH = 150;
        int drawX = previewRect.x + (previewRect.width - drawW) / 2;
        int drawY = previewRect.y + 42;

        // 获取玩家当前帧索引
        int playerFrameIndex = player.getCurrentFrameIndex();
        String currentAction = player.getCurrentAction();

        // 根据装备状态选择角色精灵图
        java.awt.image.BufferedImage characterFrame = getCharacterFrame(playerFrameIndex);
        if (characterFrame != null) {
            if (!player.getIsLeft()) {
                Graphics2D g2Flip = (Graphics2D) g2.create();
                g2Flip.translate(drawX + drawW, drawY);
                g2Flip.scale(-1, 1);
                g2Flip.drawImage(characterFrame, 0, 0, drawW, drawH, null);
                g2Flip.dispose();
            } else {
                g2.drawImage(characterFrame, drawX, drawY, drawW, drawH, null);
            }
        } else {
            g2.setColor(new Color(255, 215, 120));
            g2.fillOval(previewRect.x + 48, previewRect.y + 48, 90, 110);
        }

        // 叠加武器帧（仅在没有装备护甲时才叠加，因为armor_yellow已包含武器）
        if (!player.hasArmorEquipped()) {
            java.awt.image.BufferedImage weaponFrame = getWeaponFrame(playerFrameIndex, currentAction);
            if (weaponFrame != null) {
                if (!player.getIsLeft()) {
                    Graphics2D g2Flip = (Graphics2D) g2.create();
                    g2Flip.translate(drawX + drawW, drawY);
                    g2Flip.scale(-1, 1);
                    g2Flip.drawImage(weaponFrame, 0, 0, drawW, drawH, null);
                    g2Flip.dispose();
                } else {
                    g2.drawImage(weaponFrame, drawX, drawY, drawW, drawH, null);
                }
            }
        }

        g2.setColor(new Color(244, 225, 173));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("Preview", previewRect.x + 50, previewRect.y + 26);
    }

    // 根据当前帧索引和装备状态获取角色帧
    private java.awt.image.BufferedImage getCharacterFrame(int frameIndex) {
        loadPreviewSprites();

        java.awt.image.BufferedImage spriteSheet = player.hasArmorEquipped() ? armorSprite : wukongSprite;
        if (spriteSheet == null) return null;

        return extractFrame(spriteSheet, frameIndex);
    }

    // 根据当前帧索引和装备状态获取武器帧
    private java.awt.image.BufferedImage getWeaponFrame(int frameIndex, String action) {
        loadPreviewSprites();

        boolean hasWeaponEquipped = inventory.getEquippedWeapon() != null;
        java.awt.image.BufferedImage spriteSheet = hasWeaponEquipped ? staffSprite : weaponSprite;
        if (spriteSheet == null) return null;

        return extractFrame(spriteSheet, frameIndex);
    }

    private void loadPreviewSprites() {
        if (spritesLoaded) return;
        try {
            wukongSprite = javax.imageio.ImageIO.read(new java.io.File("image/wukong/wukong0.png"));
            armorSprite = javax.imageio.ImageIO.read(new java.io.File("image/wukong/armor_yellow.png"));
            weaponSprite = javax.imageio.ImageIO.read(new java.io.File("image/weapon/weapon0.png"));
            staffSprite = javax.imageio.ImageIO.read(new java.io.File("image/weapon/staff_red.png"));
            spritesLoaded = true;
        } catch (Exception e) {
            // Ignore
        }
    }

    private java.awt.image.BufferedImage extractFrame(java.awt.image.BufferedImage spriteSheet, int frameIndex) {
        int firstX = 20, firstY = 30, frameSpacing = 200, frameWidth = 150, frameHeight = 150;
        int maxCols = (spriteSheet.getWidth() - firstX) / frameSpacing;
        int maxRows = (spriteSheet.getHeight() - firstY) / frameSpacing;

        int row = frameIndex / maxCols;
        int col = frameIndex % maxCols;
        int x = firstX + col * frameSpacing;
        int y = firstY + row * frameSpacing;

        if (x + frameWidth <= spriteSheet.getWidth() && y + frameHeight <= spriteSheet.getHeight()) {
            return spriteSheet.getSubimage(x, y, frameWidth, frameHeight);
        }
        return null;
    }

    private void drawEquipment(Graphics2D g2) {
        drawEquipmentSlot(g2, weaponSlotRect, "Staff", inventory.getEquippedWeapon());
        drawEquipmentSlot(g2, armorSlotRect, "Armor", inventory.getEquippedArmor());
    }

    private void drawEquipmentSlot(Graphics2D g2, Rectangle rect, String label, InventoryItem item) {
        g2.setColor(new Color(114, 75, 43));
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 14, 14);
        g2.setColor(new Color(220, 190, 123));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 14, 14);

        g2.setColor(new Color(244, 225, 173));
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString(label, rect.x + 12, rect.y - 8);

        if (item == null) {
            g2.setColor(new Color(225, 198, 156));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("Empty", rect.x + 16, rect.y + 43);
            return;
        }

        drawItemIcon(g2, rect, item);
    }

    private void drawBag(Graphics2D g2) {
        List<InventoryItem> items = inventory.getItems();
        g2.setColor(new Color(244, 225, 173));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString("Click item to use or equip. Click left slots to unequip.", rightPanelRect.x + 20, rightPanelRect.y + 68);

        for (int i = 0; i < bagSlotRects.length; i++) {
            Rectangle rect = bagSlotRects[i];
            g2.setColor(new Color(111, 73, 43));
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
            g2.setColor(new Color(198, 164, 106));
            g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

            if (i < items.size()) {
                drawItemIcon(g2, rect, items.get(i));
            }
        }

        g2.setColor(new Color(244, 225, 173));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("Slots " + items.size() + " / " + inventory.getMaxSlots(), rightPanelRect.x + 20, rightPanelRect.y + rightPanelRect.height - 24);
    }

    private void drawItemIcon(Graphics2D g2, Rectangle rect, InventoryItem invItem) {
        Item item = invItem.getItem();
        if (item.getIcon() != null) {
            g2.drawImage(item.getIcon().getImage(), rect.x + 6, rect.y + 6, rect.width - 12, rect.height - 12, null);
        } else {
            g2.setColor(item.getItemColor());
            g2.fillOval(rect.x + 12, rect.y + 12, rect.width - 24, rect.height - 24);
            g2.setColor(Color.WHITE);
            g2.drawOval(rect.x + 12, rect.y + 12, rect.width - 24, rect.height - 24);
        }

        if (invItem.getQuantity() > 1) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("x" + invItem.getQuantity(), rect.x + rect.width - 24, rect.y + rect.height - 8);
        }

        g2.setColor(new Color(251, 240, 214));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String name = invItem.getDisplayName();
        String shortName = name.length() > 5 ? name.substring(0, 5) : name;
        g2.drawString(shortName, rect.x + 8, rect.y + 18);
    }
}
