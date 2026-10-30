package com.homeservice.ui.customer;

import com.homeservice.dao.ServiceCategoryDAO;
import com.homeservice.dao.ServiceProviderDAO;
import com.homeservice.model.ServiceCategory;
import com.homeservice.model.ServiceProviderProfile;
import com.homeservice.ui.common.EmptyStatePanel;
import com.homeservice.ui.common.ToastNotification;
import com.homeservice.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BrowseServicesPanel extends JPanel {
    private final ServiceCategoryDAO categoryDAO = new ServiceCategoryDAO();
    private final ServiceProviderDAO providerDAO = new ServiceProviderDAO();
    private final Frame parentFrame;

    private List<ServiceCategory> categoryList = new ArrayList<>();
    private ServiceCategory selectedCategory = null;
    private JPanel categoryTilesContainer;
    private JPanel providersContainer;
    private JLabel lblCategoryDesc;
    private JLabel lblBasePrice;
    private JTextField txtSearchProvider;
    private List<CategoryTile> tiles = new ArrayList<>();

    public BrowseServicesPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.getBgMain());
        setBorder(new EmptyBorder(16, 20, 16, 20));
        initUI();
    }

    private void initUI() {
        // TOP SECTION: Category Tiles Grid + Info Banner
        JPanel topSection = new JPanel(new BorderLayout(10, 12));
        topSection.setOpaque(false);

        // Header Row: Title + Search field
        JPanel headerRow = new JPanel(new BorderLayout(12, 0));
        headerRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Explore Home Services & Verified Professionals");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.getTextDark());

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);

        JLabel lblFilter = new JLabel("Search Expert:");
        lblFilter.setFont(UITheme.FONT_BOLD);
        lblFilter.setForeground(UITheme.getTextMuted());

        txtSearchProvider = new JTextField(14);
        txtSearchProvider.setFont(UITheme.FONT_BODY);
        txtSearchProvider.putClientProperty("JTextField.placeholderText", "Search by name or bio...");
        txtSearchProvider.addActionListener(e -> refreshProviders());

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> refreshProviders());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> reloadCategories());

        searchBar.add(lblFilter);
        searchBar.add(txtSearchProvider);
        searchBar.add(btnSearch);
        searchBar.add(btnRefresh);

        headerRow.add(lblTitle, BorderLayout.WEST);
        headerRow.add(searchBar, BorderLayout.EAST);
        topSection.add(headerRow, BorderLayout.NORTH);

        // Category Tiles Flow/Grid (Suggestion #1)
        categoryTilesContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        categoryTilesContainer.setOpaque(false);

        JScrollPane tilesScroll = new JScrollPane(categoryTilesContainer);
        tilesScroll.setOpaque(false);
        tilesScroll.getViewport().setOpaque(false);
        tilesScroll.setBorder(BorderFactory.createEmptyBorder());
        tilesScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tilesScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        tilesScroll.setPreferredSize(new Dimension(0, 115));
        topSection.add(tilesScroll, BorderLayout.CENTER);

        // Category Info Banner
        JPanel descBox = UITheme.createCardPanel();
        descBox.setLayout(new BorderLayout(12, 6));

        lblCategoryDesc = new JLabel();
        lblCategoryDesc.setFont(UITheme.FONT_BODY);
        lblCategoryDesc.setForeground(UITheme.getTextMuted());

        lblBasePrice = new JLabel();
        lblBasePrice.setFont(UITheme.FONT_BOLD);
        lblBasePrice.setForeground(UITheme.PRIMARY);

        descBox.add(lblCategoryDesc, BorderLayout.WEST);
        descBox.add(lblBasePrice, BorderLayout.EAST);
        topSection.add(descBox, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);

        // CENTER SECTION: Providers List Container
        providersContainer = new JPanel();
        providersContainer.setLayout(new BoxLayout(providersContainer, BoxLayout.Y_AXIS));
        providersContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(providersContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        reloadCategories();
    }

    public void reloadCategories() {
        categoryList = categoryDAO.getAllCategories();
        categoryTilesContainer.removeAll();
        tiles.clear();

        if (categoryList.isEmpty()) {
            return;
        }

        if (selectedCategory == null && !categoryList.isEmpty()) {
            selectedCategory = categoryList.get(0);
        }

        for (ServiceCategory cat : categoryList) {
            CategoryTile tile = new CategoryTile(cat);
            tiles.add(tile);
            categoryTilesContainer.add(tile);
        }

        categoryTilesContainer.revalidate();
        categoryTilesContainer.repaint();
        updateTileSelection();
        refreshProviders();
    }

    private void updateTileSelection() {
        for (CategoryTile t : tiles) {
            t.setSelected(selectedCategory != null && t.category.getId() == selectedCategory.getId());
        }
    }

    private void refreshProviders() {
        providersContainer.removeAll();
        if (selectedCategory == null) {
            providersContainer.revalidate();
            providersContainer.repaint();
            return;
        }

        lblCategoryDesc.setText("Category: " + selectedCategory.getName() + " — " + selectedCategory.getDescription());
        lblBasePrice.setText("Base Diagnostic Rate: ₹" + String.format("%.2f", selectedCategory.getBasePrice()));

        List<ServiceProviderProfile> allProviders = providerDAO.getProvidersByCategory(selectedCategory.getId());
        String query = txtSearchProvider != null ? txtSearchProvider.getText().trim().toLowerCase() : "";

        List<ServiceProviderProfile> filtered = new ArrayList<>();
        for (ServiceProviderProfile p : allProviders) {
            if (query.isEmpty() ||
                (p.getProviderName() != null && p.getProviderName().toLowerCase().contains(query)) ||
                (p.getBio() != null && p.getBio().toLowerCase().contains(query)) ||
                (p.getProviderAddress() != null && p.getProviderAddress().toLowerCase().contains(query))) {
                filtered.add(p);
            }
        }

        if (filtered.isEmpty()) {
            // Suggestion #4: Rich Empty State
            JButton btnReset = UITheme.createPrimaryButton("Show All Providers");
            btnReset.addActionListener(e -> {
                if (txtSearchProvider != null) txtSearchProvider.setText("");
                refreshProviders();
            });
            EmptyStatePanel empty = new EmptyStatePanel(
                "🔍",
                "No Verified Providers Found",
                "There are currently no active service specialists matching your criteria in " + selectedCategory.getName() + ".",
                btnReset
            );
            providersContainer.add(empty);
        } else {
            for (ServiceProviderProfile p : filtered) {
                providersContainer.add(createProviderCard(p, selectedCategory));
                providersContainer.add(Box.createVerticalStrut(12));
            }
        }

        providersContainer.revalidate();
        providersContainer.repaint();
    }

    private JPanel createProviderCard(ServiceProviderProfile p, ServiceCategory cat) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(16, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));

        // Left info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel name = new JLabel(p.getProviderName());
        name.setFont(UITheme.FONT_HEADER);
        name.setForeground(UITheme.getTextDark());
        titleRow.add(name);

        JLabel verified = new JLabel("✓ Verified Pro") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 252, 231));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        verified.setFont(new Font("Segoe UI", Font.BOLD, 10));
        verified.setForeground(new Color(21, 128, 61));
        verified.setBorder(new EmptyBorder(2, 6, 2, 6));
        titleRow.add(verified);

        info.add(titleRow);
        info.add(Box.createVerticalStrut(3));

        JLabel rating = new JLabel("⭐ " + String.format("%.1f", p.getRatingAvg()) + " (" + p.getTotalReviews() + " reviews)  •  " + p.getExperienceYears() + " Years Experience  •  " + (p.getProviderAddress() != null ? p.getProviderAddress() : ""));
        rating.setFont(UITheme.FONT_SMALL);
        rating.setForeground(UITheme.getTextMuted());
        info.add(rating);
        info.add(Box.createVerticalStrut(3));

        JLabel contact = new JLabel("✉ " + (p.getProviderEmail() != null ? p.getProviderEmail() : "") + "  •  📞 " + (p.getProviderPhone() != null ? p.getProviderPhone() : ""));
        contact.setFont(new Font("Segoe UI", Font.BOLD, 12));
        contact.setForeground(UITheme.PRIMARY);
        info.add(contact);
        info.add(Box.createVerticalStrut(4));

        JLabel bio = new JLabel(p.getBio() != null && !p.getBio().isEmpty() ? p.getBio() : "Experienced verified service professional.");
        bio.setFont(UITheme.FONT_BODY);
        bio.setForeground(UITheme.getTextDark());
        info.add(bio);

        // Right Action & Pricing
        JPanel right = new JPanel(new GridLayout(2, 1, 6, 8));
        right.setOpaque(false);

        JLabel price = new JLabel("₹" + String.format("%.0f", p.getHourlyRate()) + " / hr", SwingConstants.RIGHT);
        price.setFont(new Font("Segoe UI", Font.BOLD, 18));
        price.setForeground(UITheme.PRIMARY);

        JButton btnBook = UITheme.createPrimaryButton("Book Service");
        btnBook.addActionListener(e -> {
            BookServiceDialog dlg = new BookServiceDialog(parentFrame, p, cat);
            dlg.setVisible(true);
            if (dlg.isBookingSuccessful()) {
                ToastNotification.showSuccess(parentFrame, "✓ Booking confirmed for " + cat.getName() + " with " + p.getProviderName() + "!");
            }
        });

        right.add(price);
        right.add(btnBook);

        card.add(info, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    public static String getCategoryEmoji(String name) {
        if (name == null) return "🛠️";
        switch (name.trim()) {
            case "Plumbing": return "🔧";
            case "Electrical Work": return "⚡";
            case "Cleaning": return "🧹";
            case "Carpentry": return "🪚";
            case "Painting": return "🎨";
            case "Appliance Repair": return "📺";
            case "AC Servicing": return "❄️";
            case "Handyman": return "🔨";
            default: return "🛠️";
        }
    }

    /**
     * Interactive Category Card Tile Widget
     */
    private class CategoryTile extends JPanel {
        final ServiceCategory category;
        private boolean isSelected = false;
        private boolean isHovered = false;

        CategoryTile(ServiceCategory cat) {
            this.category = cat;
            setPreferredSize(new Dimension(145, 90));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 10, 8, 10));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedCategory = category;
                    updateTileSelection();
                    refreshProviders();
                }
            });

            // Icon
            JLabel lblIcon = new JLabel(getCategoryEmoji(cat.getName()), SwingConstants.CENTER);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Name
            JLabel lblName = new JLabel(cat.getName());
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblName.setForeground(UITheme.getTextDark());
            lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Price badge
            JLabel lblPrice = new JLabel("From ₹" + String.format("%.0f", cat.getBasePrice()));
            lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblPrice.setForeground(UITheme.PRIMARY);
            lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

            add(lblIcon);
            add(Box.createVerticalStrut(3));
            add(lblName);
            add(Box.createVerticalStrut(2));
            add(lblPrice);
        }

        void setSelected(boolean sel) {
            this.isSelected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;
            if (isSelected) {
                bg = new Color(239, 246, 255);
            } else if (isHovered) {
                bg = new Color(241, 245, 249);
            } else {
                bg = UITheme.CARD_BG;
            }

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

            Color border = isSelected ? UITheme.PRIMARY : (isHovered ? UITheme.PRIMARY_DARK : UITheme.getCardBorder());
            g2.setColor(border);
            g2.setStroke(new BasicStroke(isSelected ? 2.0f : 1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
