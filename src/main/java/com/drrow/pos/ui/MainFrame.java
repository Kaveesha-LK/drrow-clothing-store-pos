package com.drrow.pos.ui;

import com.drrow.pos.controller.SessionContext;
import com.drrow.pos.model.User;
import com.drrow.pos.service.AuthService;
import com.drrow.pos.ui.components.ModernButton;
import com.drrow.pos.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final JLabel lblLiveClock = new JLabel();
    private final JLabel lblCurrentSection = new JLabel("Dashboard");
    private final AuthService authService = new AuthService();

    // Panes
    private DashboardPanel dashboardPanel;
    private POSPanel posPanel;
    private BarcodePrintPanel barcodePrintPanel;
    private ProductManagementPanel productPanel;
    private InventoryPanel inventoryPanel;
    private SalesHistoryPanel salesHistoryPanel;
    private CustomerManagementPanel customerPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;
    private UserManagementPanel userPanel;
    private AuditLogPanel auditPanel;

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public MainFrame() {
        super("D’RROW Clothing Store - Enterprise POS & Inventory System");
        initUI();
        startClockTimer();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1360, 840);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG_DARK);
        setLayout(new BorderLayout());

        add(createTopHeaderBar(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        centerPanel.add(createSidebar(), BorderLayout.WEST);
        initPanels();
        centerPanel.add(contentCards, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Select initial view by role
        User cur = SessionContext.getCurrentUser();
        String role = cur != null ? cur.getRoleName() : "ADMIN";
        if ("CASHIER".equalsIgnoreCase(role)) {
            switchView("POS");
        } else if ("STOCK_STAFF".equalsIgnoreCase(role)) {
            switchView("INVENTORY");
        } else {
            switchView("DASHBOARD");
        }
    }

    private JPanel createTopHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.COLOR_HEADER_BG);
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(new EmptyBorder(0, 16, 0, 16));

        // Brand Label
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        leftBrand.setOpaque(false);
        JLabel lblLogo = new JLabel("D’RROW");
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblLogo.setForeground(UITheme.COLOR_GOLD);

        JLabel lblTag = new JLabel("CLOTHING STORE POS");
        lblTag.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTag.setForeground(UITheme.COLOR_TEXT_MUTED);

        JLabel lblSep = new JLabel(" | ");
        lblSep.setForeground(UITheme.COLOR_CARD_BORDER);

        lblCurrentSection.setFont(UITheme.FONT_SUBTITLE);
        lblCurrentSection.setForeground(Color.WHITE);

        leftBrand.add(lblLogo);
        leftBrand.add(lblTag);
        leftBrand.add(lblSep);
        leftBrand.add(lblCurrentSection);
        header.add(leftBrand, BorderLayout.WEST);

        // User Profile & Status Badges
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        rightInfo.setOpaque(false);

        User cur = SessionContext.getCurrentUser();
        String roleName = cur != null ? cur.getRoleName() : "ADMIN";
        String userName = cur != null ? cur.getFullName() : "Administrator";

        JLabel lblRoleBadge = new JLabel("  " + roleName + "  ");
        lblRoleBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblRoleBadge.setOpaque(true);
        lblRoleBadge.setForeground(Color.BLACK);
        if ("ADMIN".equalsIgnoreCase(roleName)) {
            lblRoleBadge.setBackground(UITheme.COLOR_GOLD);
        } else if ("MANAGER".equalsIgnoreCase(roleName)) {
            lblRoleBadge.setBackground(new Color(192, 132, 252));
        } else if ("CASHIER".equalsIgnoreCase(roleName)) {
            lblRoleBadge.setBackground(UITheme.COLOR_SUCCESS);
        } else {
            lblRoleBadge.setBackground(UITheme.COLOR_PRIMARY);
        }

        JLabel lblUser = new JLabel("User: " + userName);
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        lblLiveClock.setFont(UITheme.FONT_MONO);
        lblLiveClock.setForeground(UITheme.COLOR_GOLD);

        ModernButton btnLogout = new ModernButton("Sign Out", ModernButton.ButtonType.DANGER);
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.setMargin(new Insets(4, 10, 4, 10));
        btnLogout.addActionListener(e -> performLogout());

        rightInfo.add(lblRoleBadge);
        rightInfo.add(lblUser);
        rightInfo.add(lblLiveClock);
        rightInfo.add(btnLogout);

        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.COLOR_CARD_BG);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.COLOR_CARD_BORDER));

        User user = SessionContext.getCurrentUser();
        String role = user != null ? user.getRoleName() : "ADMIN";

        // Navigation Items by Role Permissions
        if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role)) {
            addNavButton(sidebar, "DASHBOARD", "📊 Dashboard");
        }
        if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role) || "CASHIER".equalsIgnoreCase(role)) {
            addNavButton(sidebar, "POS", "🛒 POS Terminal");
        }
        if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role) || "STOCK_STAFF".equalsIgnoreCase(role)) {
            addNavButton(sidebar, "BARCODE", "🏷️ Barcode Tags");
            addNavButton(sidebar, "PRODUCTS", "👕 Products & SKU");
            addNavButton(sidebar, "INVENTORY", "📦 Inventory / Stock");
        }
        if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role) || "CASHIER".equalsIgnoreCase(role)) {
            addNavButton(sidebar, "SALES", "📜 Invoices Archive");
            addNavButton(sidebar, "CUSTOMERS", "👥 Customer CRM");
        }
        if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role)) {
            addNavButton(sidebar, "REPORTS", "📈 Reports & Profit");
        }
        if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role)) {
            addNavButton(sidebar, "SETTINGS", "⚙️ Store Settings");
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            addNavButton(sidebar, "USERS", "👤 User Accounts");
            addNavButton(sidebar, "AUDIT", "🛡️ Audit Trails");
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String cardName, String title) {
        JButton btn = new JButton(title);
        btn.setMaximumSize(new Dimension(210, 42));
        btn.setPreferredSize(new Dimension(210, 42));
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(UITheme.COLOR_TEXT_MUTED);
        btn.setBackground(UITheme.COLOR_CARD_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> switchView(cardName));
        navButtons.put(cardName, btn);
        sidebar.add(btn);
    }

    private void initPanels() {
        dashboardPanel = new DashboardPanel();
        posPanel = new POSPanel();
        barcodePrintPanel = new BarcodePrintPanel();
        productPanel = new ProductManagementPanel();
        inventoryPanel = new InventoryPanel();
        salesHistoryPanel = new SalesHistoryPanel();
        customerPanel = new CustomerManagementPanel();
        reportsPanel = new ReportsPanel();
        settingsPanel = new SettingsPanel();
        userPanel = new UserManagementPanel();
        auditPanel = new AuditLogPanel();

        contentCards.add(dashboardPanel, "DASHBOARD");
        contentCards.add(posPanel, "POS");
        contentCards.add(barcodePrintPanel, "BARCODE");
        contentCards.add(productPanel, "PRODUCTS");
        contentCards.add(inventoryPanel, "INVENTORY");
        contentCards.add(salesHistoryPanel, "SALES");
        contentCards.add(customerPanel, "CUSTOMERS");
        contentCards.add(reportsPanel, "REPORTS");
        contentCards.add(settingsPanel, "SETTINGS");
        contentCards.add(userPanel, "USERS");
        contentCards.add(auditPanel, "AUDIT");
    }

    public void switchView(String cardName) {
        cardLayout.show(contentCards, cardName);

        // Highlight active nav button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(cardName)) {
                entry.getValue().setForeground(UITheme.COLOR_GOLD);
                lblCurrentSection.setText(entry.getValue().getText().replaceAll("[^a-zA-Z0-9 &]", "").trim());
            } else {
                entry.getValue().setForeground(UITheme.COLOR_TEXT_MUTED);
            }
        }

        // Trigger refresh on relevant panels
        if ("DASHBOARD".equals(cardName) && dashboardPanel != null) {
            dashboardPanel.refreshData();
        } else if ("SALES".equals(cardName) && salesHistoryPanel != null) {
            salesHistoryPanel.refreshSales();
        } else if ("AUDIT".equals(cardName) && auditPanel != null) {
            auditPanel.refreshLogs();
        }
    }

    private void performLogout() {
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out of D’RROW POS?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        }
    }

    private void startClockTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        Timer timer = new Timer(1000, e -> lblLiveClock.setText(sdf.format(new Date())));
        timer.start();
    }
}
