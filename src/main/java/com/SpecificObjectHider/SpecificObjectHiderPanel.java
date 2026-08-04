package com.SpecificObjectHider;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;

public class SpecificObjectHiderPanel extends PluginPanel
{
    private final SpecificObjectHiderPlugin plugin;
    private final JPanel listContainer = new JPanel();
    private boolean allRevealed = false;

    public SpecificObjectHiderPanel(SpecificObjectHiderPlugin plugin)
    {
        // RuneLite's PluginPanel natively handles scrolling automatically!
        super();

        this.plugin = plugin;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("Hidden Objects");
        title.setForeground(Color.WHITE);
        title.setFont(FontManager.getRunescapeFont());
        topPanel.add(title, BorderLayout.WEST);

        // Global Reveal All Icon Toggle Button (24x24 Eye)
        JButton revealAllBtn = new JButton(createEyeIcon(false, 24));
        revealAllBtn.setToolTipText("Toggle reveal all objects temporarily");
        revealAllBtn.setBorderPainted(false);
        revealAllBtn.setContentAreaFilled(false);
        revealAllBtn.setFocusPainted(false);
        revealAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        revealAllBtn.addActionListener(e -> {
            allRevealed = !allRevealed;
            revealAllBtn.setIcon(createEyeIcon(allRevealed, 24));
            plugin.toggleAll(allRevealed);
        });

        topPanel.add(revealAllBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Setup the list container
        listContainer.setLayout(new GridBagLayout());

        // Wrap it in a North-aligned BorderLayout to force items to stack cleanly at the top
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(listContainer, BorderLayout.NORTH);

        // Add directly to the PluginPanel. The native RuneLite Scrollbar will handle overflow.
        add(wrapperPanel, BorderLayout.CENTER);

        updatePanel();
    }

    public void updatePanel()
    {
        SwingUtilities.invokeLater(() ->
        {
            listContainer.removeAll();

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 5, 0);

            java.util.Set<HiddenObject> objects = plugin.getHiddenObjects();

            if (objects.isEmpty())
            {
                JLabel emptyLabel = new JLabel("No objects hidden.");
                emptyLabel.setFont(FontManager.getRunescapeSmallFont());
                emptyLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                listContainer.add(emptyLabel, gbc);
            }
            else
            {
                for (HiddenObject obj : objects)
                {
                    listContainer.add(createObjectPanel(obj), gbc);
                    gbc.gridy++;
                }
            }

            listContainer.revalidate();
            listContainer.repaint();
        });
    }

    private JPanel createObjectPanel(HiddenObject obj)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel textContainer = new JPanel();
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        textContainer.setOpaque(false);

        String objName = obj.getName() != null && !obj.getName().isEmpty() ? obj.getName() : "Unknown Object";

        JLabel nameLabel = new JLabel(String.format("%s (%d)", objName, obj.getId()));
        nameLabel.setFont(FontManager.getRunescapeFont());
        nameLabel.setForeground(obj.isDisabled() ? Color.GRAY : Color.WHITE);

        JLabel locationLabel = new JLabel(String.format("Loc: %d, %d, P: %d", obj.getX(), obj.getY(), obj.getPlane()));
        locationLabel.setFont(FontManager.getRunescapeSmallFont());
        locationLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        textContainer.add(nameLabel);
        textContainer.add(Box.createRigidArea(new Dimension(0, 2)));
        textContainer.add(locationLabel);

        panel.add(textContainer, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionPanel.setOpaque(false);

        JButton eyeBtn = new JButton(createEyeIcon(obj.isDisabled(), 16));
        eyeBtn.setToolTipText(obj.isDisabled() ? "Re-enable Block" : "Temporarily Reveal");
        eyeBtn.setBorderPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setFocusPainted(false);
        eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeBtn.addActionListener(e -> plugin.toggleObject(obj));

        JButton trashBtn = new JButton(createTrashIcon(16));
        trashBtn.setToolTipText("Delete Object Block");
        trashBtn.setBorderPainted(false);
        trashBtn.setContentAreaFilled(false);
        trashBtn.setFocusPainted(false);
        trashBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        trashBtn.addActionListener(e -> {
            JPopupMenu confirmMenu = new JPopupMenu();
            JLabel header = new JLabel("  Delete?  ");
            header.setFont(FontManager.getRunescapeFont());
            confirmMenu.add(header);
            confirmMenu.addSeparator();

            JMenuItem yesItem = new JMenuItem("Yes");
            yesItem.setFont(FontManager.getRunescapeSmallFont());
            yesItem.setForeground(Color.RED);
            yesItem.addActionListener(evt -> plugin.deleteObject(obj));

            JMenuItem noItem = new JMenuItem("No");
            noItem.setFont(FontManager.getRunescapeSmallFont());

            confirmMenu.add(yesItem);
            confirmMenu.add(noItem);
            confirmMenu.show(trashBtn, 0, trashBtn.getHeight());
        });

        actionPanel.add(eyeBtn);
        actionPanel.add(trashBtn);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private ImageIcon createEyeIcon(boolean isSlashed, int size)
    {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float opacity = isSlashed ? 0.45f : 1.0f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Arc2D.Float(1, size * 0.2f, size - 2, size * 0.6f, 0, 180, Arc2D.OPEN));
        g2.draw(new Arc2D.Float(1, size * 0.2f, size - 2, size * 0.6f, 180, 180, Arc2D.OPEN));

        g2.fillOval(size / 2 - 3, size / 2 - 3, 6, 6);

        if (isSlashed)
        {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(2, size - 3, size - 3, 2);
        }

        g2.dispose();
        return new ImageIcon(img);
    }

    private ImageIcon createTrashIcon(int size)
    {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(220, 60, 60));
        g2.setStroke(new BasicStroke(1.5f));

        g2.drawLine(2, 4, size - 3, 4);
        g2.drawRect(size / 2 - 2, 2, 4, 2);
        g2.drawRect(4, 5, size - 9, size - 7);

        g2.drawLine(7, 7, 7, size - 4);
        g2.drawLine(size / 2, 7, size / 2, size - 4);
        g2.drawLine(size - 8, 7, size - 8, size - 4);

        g2.dispose();
        return new ImageIcon(img);
    }
}