package com.ironwill;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.time.LocalDate;

public class IronwillPanel extends PluginPanel
{
    @Inject
    private ItemManager itemManager;

    private final JPanel acquiredItemsPanel = new JPanel();

    void init()
    {
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Acquired Items");
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        add(header, BorderLayout.NORTH);

        acquiredItemsPanel.setLayout(new BoxLayout(acquiredItemsPanel, BoxLayout.Y_AXIS));
        acquiredItemsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        acquiredItemsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JScrollPane scrollPane = new JScrollPane(acquiredItemsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addEntry(int itemId, int kc, String nameItem, LocalDate date)
    {
        SwingUtilities.invokeLater(() -> {
            JLabel entry = new JLabel(
                    "<html>" + nameItem + "<br/>" + kc + " KC " + date + "</html>"
            );

            BufferedImage icon = itemManager.getImage(itemId);
            if (icon != null)
            {
                entry.setIcon(new ImageIcon(icon));
            }

            entry.setBorder(new EmptyBorder(5, 0, 5, 0));
            acquiredItemsPanel.add(entry);
            revalidate();
        });
    }
}