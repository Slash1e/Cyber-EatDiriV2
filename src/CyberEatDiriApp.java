import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

public class CyberEatDiriApp {

    private static final String APP_FONT = "Poppins";

    // ---------- DATA MODELS ----------
    static class MenuItem {
        String icon;          // emoji fallback
        String name;
        String description;
        int price;
        String imagePath;     // asset path

        MenuItem(String icon, String name, String description, int price, String imagePath) {
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.price = price;
            this.imagePath = imagePath;
        }
    }

    static class CreditItem {
        String icon;          // emoji fallback
        String hours;
        String label;
        int price;
        String imagePath;

        CreditItem(String icon, String hours, String label, int price, String imagePath) {
            this.icon = icon;
            this.hours = hours;
            this.label = label;
            this.price = price;
            this.imagePath = imagePath;
        }
    }

    static class CartItem {
        long id;
        String icon;          // emoji fallback
        String name;
        String imagePath;
        int unitPrice;
        int quantity;

        CartItem(long id, String icon, String name, String imagePath, int unitPrice, int quantity) {
            this.id = id;
            this.icon = icon;
            this.name = name;
            this.imagePath = imagePath;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        int getTotal() {
            return unitPrice * quantity;
        }
    }

    static class Order {
        String time;
        String itemsSummary;   // multi-line plain text
        int total;
        String pcNumber;
        String paymentMethod;

        Order(String time, String itemsSummary, int total, String pcNumber, String paymentMethod) {
            this.time = time;
            this.itemsSummary = itemsSummary;
            this.total = total;
            this.pcNumber = pcNumber;
            this.paymentMethod = paymentMethod;
        }
    }

    // ---------- MULTI-LINE CELL RENDERER FOR HISTORY TABLE ----------
    static class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {

        public MultiLineTableCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(new Font(APP_FONT, Font.PLAIN, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText(value == null ? "" : value.toString());

            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            setSize(colWidth, Short.MAX_VALUE);

            int preferredHeight = getPreferredSize().height;
            int currentHeight = table.getRowHeight(row);

            if (preferredHeight > currentHeight) {
                table.setRowHeight(row, preferredHeight);
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            return this;
        }
    }

    // ---------- APP STATE ----------
    private JFrame frame;
    private JButton cartButton;
    private ArrayList<CartItem> cart = new ArrayList<>();
    private ArrayList<Order> orderHistory = new ArrayList<>();
    private DefaultTableModel historyModel;

    private String userPcNumber = "";
    private String userSpecialRequest = "";

    // ---------- MENU DATA with IMAGE PATHS ----------
    private final MenuItem[] foodMenu = new MenuItem[] {
            new MenuItem("🍕", "Gamer's Pizza", "Loaded with pepperoni, cheese, and extra energy.", 180, "/assets/pizza.png"),
            new MenuItem("🍔", "Power Burger",  "Juicy beef patty with all the fixings.",           150, "/assets/burger.png"),
            new MenuItem("🍟", "Crispy Fries",  "Golden and crispy, perfect for snacking.",         80,  "/assets/fries.png"),
            new MenuItem("🥤", "Energy Drink",  "Stay alert and focused while gaming.",             60,  "/assets/energy.png"),
            new MenuItem("🍪", "Cookie Combo",  "Freshly baked cookies for sweet breaks.",          95,  "/assets/cookies.png"),
            new MenuItem("☕", "Coffee Boost",  "Premium coffee to keep you sharp.",                75,  "/assets/coffee.png")
    };

    private final CreditItem[] creditMenu = new CreditItem[] {
            new CreditItem("⚡", "1 Hour",  "Quick Session",  20,  "/assets/credit_1h.png"),
            new CreditItem("🔥", "3 Hours", "Popular Choice", 60,  "/assets/credit_3h.png"),
            new CreditItem("💎", "5 Hours", "Best Value",     100, "/assets/credit_5h.png"),
            new CreditItem("👑", "10 Hours","Ultimate Pack",  200, "/assets/credit_10h.png")
    };

    // ---------- START ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CyberEatDiriApp().start());
    }

    private void start() {
        frame = new JFrame("CYBER-EATDIRI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1350, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(buildTopHero(), BorderLayout.NORTH);
        frame.add(buildTabs(), BorderLayout.CENTER);

        frame.setVisible(true);
        updateCartButton();
    }

    // ---------- UI BUILDERS ----------
    private JPanel buildTopHero() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(15, 20, 15, 20));
        top.setBackground(new Color(0x8B0000));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("CYBER-EATDIRI");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(APP_FONT, Font.BOLD, 32));

        JLabel tagline = new JLabel("Game • Eat • Connect");
        tagline.setForeground(Color.WHITE);
        tagline.setFont(new Font(APP_FONT, Font.PLAIN, 14));

        titlePanel.add(title);
        titlePanel.add(tagline);

        // 🔹 Cart button WITHOUT emoji, WITH icon
        ImageIcon cartIcon = loadIcon("/assets/cart.png", 18);

        cartButton = new JButton("Cart [0]");
        if (cartIcon != null) {
            cartButton.setIcon(cartIcon);
            cartButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        }
        cartButton.setFocusPainted(false);
        cartButton.setFont(new Font(APP_FONT, Font.BOLD, 14));
        cartButton.addActionListener(e -> openCartDialog());

        top.add(titlePanel, BorderLayout.WEST);
        top.add(cartButton, BorderLayout.EAST);

        return top;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(APP_FONT, Font.PLAIN, 13));

        // 🔹 Tab icons – we reuse existing images where possible
        ImageIcon foodIcon    = loadIcon("/assets/pizza.png",      16); // Food tab
        ImageIcon creditsIcon = loadIcon("/assets/credit_1h.png",  16); // Game Credits tab
        ImageIcon historyIcon = loadIcon("/assets/history.png",    16); // Order History tab

        // No emojis in titles now, just text + icons
        tabs.addTab("Food Menu",      foodIcon,    buildFoodPanel());
        tabs.addTab("Game Credits",   creditsIcon, buildCreditsPanel());
        tabs.addTab("Order History",  historyIcon, buildHistoryPanel());

        return tabs;
    }

    private JPanel buildFoodPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(0x1a0a0a));

        JLabel header = new JLabel("Food & Drinks", SwingConstants.CENTER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font(APP_FONT, Font.BOLD, 26));
        main.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 15, 15));
        grid.setBorder(new EmptyBorder(20, 0, 0, 0));
        grid.setBackground(new Color(0x1a0a0a));

        for (MenuItem item : foodMenu) {
            grid.add(buildFoodCard(item));
        }

        main.add(new JScrollPane(grid), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildCreditsPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(0x1a0a0a));

        JLabel header = new JLabel("Game Credits", SwingConstants.CENTER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font(APP_FONT, Font.BOLD, 26));
        main.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
        grid.setBorder(new EmptyBorder(20, 0, 0, 0));
        grid.setBackground(new Color(0x1a0a0a));

        for (CreditItem c : creditMenu) {
            grid.add(buildCreditCard(c));
        }

        main.add(new JScrollPane(grid), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildHistoryPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(0x1a0a0a));

        JLabel header = new JLabel("Order History", SwingConstants.CENTER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font(APP_FONT, Font.BOLD, 26));
        main.add(header, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(
                new Object[]{"Date/Time", "Items", "Total (P)", "PC Number", "Payment"},
                0
        ) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(historyModel);
        table.setFont(new Font(APP_FONT, Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font(APP_FONT, Font.BOLD, 12));
        table.setRowHeight(40);

        MultiLineTableCellRenderer multiRenderer = new MultiLineTableCellRenderer();
        table.getColumnModel().getColumn(1).setCellRenderer(multiRenderer); // "Items" column

        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(table);

        main.add(scrollPane, BorderLayout.CENTER);

        JLabel info = new JLabel("Completed food orders will appear here.", SwingConstants.CENTER);
        info.setForeground(Color.LIGHT_GRAY);
        info.setFont(new Font(APP_FONT, Font.PLAIN, 12));
        main.add(info, BorderLayout.SOUTH);

        return main;
    }

    private JPanel buildFoodCard(MenuItem item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4A0000), 2),
                new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(new Color(0x2d1414));

        JLabel iconLabel = new JLabel();
        ImageIcon iconImg = loadIcon(item.imagePath, 48);
        if (iconImg != null) {
            iconLabel.setIcon(iconImg);
        } else {
            iconLabel.setText(item.icon); // fallback emoji
            iconLabel.setFont(new Font(APP_FONT, Font.PLAIN, 32));
            iconLabel.setForeground(Color.WHITE);
        }
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(item.name);
        name.setForeground(Color.WHITE);
        name.setFont(new Font(APP_FONT, Font.BOLD, 16));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><body style='width:220px;color:#cccccc;font-family:Poppins;font-size:11px;'>"
                + item.description + "</body></html>");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel price = new JLabel("P" + item.price);
        price.setForeground(new Color(0xDC143C));
        price.setFont(new Font(APP_FONT, Font.BOLD, 20));
        price.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton add = new JButton("Add to Cart");
        add.setAlignmentX(Component.LEFT_ALIGNMENT);
        add.setFocusPainted(false);
        add.setFont(new Font(APP_FONT, Font.PLAIN, 13));
        add.addActionListener(e -> openFoodQuantityDialog(item));

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(name);
        card.add(Box.createVerticalStrut(6));
        card.add(desc);
        card.add(Box.createVerticalStrut(10));
        card.add(price);
        card.add(Box.createVerticalStrut(10));
        card.add(add);

        return card;
    }

    private JPanel buildCreditCard(CreditItem c) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x8B0000), 2),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(new Color(0x2d1414));

        JLabel iconLabel = new JLabel();
        ImageIcon iconImg = loadIcon(c.imagePath, 48);
        if (iconImg != null) {
            iconLabel.setIcon(iconImg);
        } else {
            iconLabel.setText(c.icon);
            iconLabel.setFont(new Font(APP_FONT, Font.PLAIN, 36));
            iconLabel.setForeground(Color.WHITE);
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hours = new JLabel(c.hours);
        hours.setForeground(Color.WHITE);
        hours.setFont(new Font(APP_FONT, Font.BOLD, 22));
        hours.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(c.label);
        label.setForeground(new Color(0xcccccc));
        label.setFont(new Font(APP_FONT, Font.PLAIN, 12));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel price = new JLabel("P" + c.price);
        price.setForeground(Color.WHITE);
        price.setFont(new Font(APP_FONT, Font.BOLD, 24));
        price.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton buy = new JButton("Buy Now");
        buy.setFocusPainted(false);
        buy.setAlignmentX(Component.CENTER_ALIGNMENT);
        buy.setFont(new Font(APP_FONT, Font.PLAIN, 13));
        buy.addActionListener(e -> buyCreditFlow(c));

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(hours);
        card.add(Box.createVerticalStrut(5));
        card.add(label);
        card.add(Box.createVerticalStrut(10));
        card.add(price);
        card.add(Box.createVerticalStrut(15));
        card.add(buy);

        return card;
    }

    // ---------- FOOD FLOW (Add to Cart popup) ----------
    private void openFoodQuantityDialog(MenuItem item) {
        // Quantity spinner
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        qtySpinner.setFont(new Font(APP_FONT, Font.PLAIN, 13));

        // Labels – black on white
        JLabel nameLabel = new JLabel(item.name);
        nameLabel.setFont(new Font(APP_FONT, Font.BOLD, 14));
        nameLabel.setForeground(Color.BLACK);

        JLabel priceLabel = new JLabel("Price: P" + item.price);
        priceLabel.setFont(new Font(APP_FONT, Font.PLAIN, 13));
        priceLabel.setForeground(Color.BLACK);

        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(new Font(APP_FONT, Font.PLAIN, 13));
        qtyLabel.setForeground(Color.BLACK);

        JLabel totalLabel = new JLabel("Total: P" + item.price);
        totalLabel.setFont(new Font(APP_FONT, Font.PLAIN, 13));
        totalLabel.setForeground(Color.BLACK);

        qtySpinner.addChangeListener(e -> {
            int q = (int) qtySpinner.getValue();
            totalLabel.setText("Total: P" + (item.price * q));
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBackground(Color.WHITE); // important so black text is visible

        panel.add(nameLabel);
        panel.add(priceLabel);
        panel.add(qtyLabel);
        panel.add(qtySpinner);
        panel.add(totalLabel);

        int result = JOptionPane.showConfirmDialog(
                frame,
                panel,
                "Add to Cart",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            int q = (int) qtySpinner.getValue();
            cart.add(new CartItem(
                    System.currentTimeMillis(),
                    item.icon,
                    item.name,
                    item.imagePath,
                    item.price,
                    q
            ));
            updateCartButton();
            showToast(q + "x " + item.name + " added to cart!");
        }
    }

    // ---------- CREDIT FLOW ----------
    private void buyCreditFlow(CreditItem c) {
        String[] methods = {"GCash", "PayPal", "Cash on Hand"};
        String method = (String) JOptionPane.showInputDialog(
                frame,
                "Choose payment method for " + c.hours + " (P" + c.price + "):",
                "Select Payment Method",
                JOptionPane.PLAIN_MESSAGE,
                null,
                methods,
                methods[0]
        );

        if (method == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Confirm purchase?\n\n" +
                        c.hours + " - P" + c.price + "\n" +
                        "Payment: " + method,
                "Confirm Purchase",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (confirm == JOptionPane.OK_OPTION) {
            showSuccessDialog(c.hours + " purchased via " + method + " for P" + c.price + "!");
        }
    }

    // ---------- CART DIALOG (no logo column) ----------
    private void openCartDialog() {
        JDialog dialog = new JDialog(frame, "Shopping Cart", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Name", "Qty", "Unit", "Total"},
                0
        ) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font(APP_FONT, Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font(APP_FONT, Font.BOLD, 12));
        table.setRowHeight(24);

        refreshCartTable(model);

        JScrollPane scroll = new JScrollPane(table);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inputs = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField pcField = new JTextField(userPcNumber);
        pcField.setFont(new Font(APP_FONT, Font.PLAIN, 13));
        JTextArea requestArea = new JTextArea(userSpecialRequest, 3, 20);
        requestArea.setLineWrap(true);
        requestArea.setWrapStyleWord(true);
        requestArea.setFont(new Font(APP_FONT, Font.PLAIN, 13));

        inputs.add(simpleLabel("PC Number:", Font.PLAIN, 13));
        inputs.add(pcField);
        inputs.add(simpleLabel("Special Request:", Font.PLAIN, 13));
        inputs.add(new JScrollPane(requestArea));

        JLabel totalLabel = new JLabel("Total: P" + getCartTotal());
        totalLabel.setFont(new Font(APP_FONT, Font.BOLD, 16));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeBtn = simpleButton("Remove Selected");
        JButton clearBtn = simpleButton("Clear Cart");
        JButton checkoutBtn = simpleButton("Checkout");

        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Select an item to remove.");
                return;
            }
            cart.remove(row);
            refreshCartTable(model);
            updateCartButton();
            totalLabel.setText("Total: P" + getCartTotal());
        });

        clearBtn.addActionListener(e -> {
            cart.clear();
            refreshCartTable(model);
            updateCartButton();
            totalLabel.setText("Total: P" + getCartTotal());
        });

        checkoutBtn.addActionListener(e -> {
            userPcNumber = pcField.getText().trim();
            userSpecialRequest = requestArea.getText().trim();

            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Your cart is empty.");
                return;
            }

            openCheckoutConfirm(dialog);
            refreshCartTable(model);
            updateCartButton();
            totalLabel.setText("Total: P" + getCartTotal());
        });

        buttons.add(removeBtn);
        buttons.add(clearBtn);
        buttons.add(checkoutBtn);

        bottom.add(inputs, BorderLayout.CENTER);

        JPanel rightBottom = new JPanel();
        rightBottom.setLayout(new BoxLayout(rightBottom, BoxLayout.Y_AXIS));
        rightBottom.add(totalLabel);
        rightBottom.add(Box.createVerticalStrut(10));
        rightBottom.add(buttons);

        bottom.add(rightBottom, BorderLayout.SOUTH);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void openCheckoutConfirm(JDialog parentDialog) {
        StringBuilder itemsText = new StringBuilder();
        int itemCount = 0;

        for (CartItem c : cart) {
            itemsText.append(c.name)
                    .append(" x").append(c.quantity)
                    .append(" = P").append(c.getTotal())
                    .append("\n");
            itemCount += c.quantity;
        }

        String pc = userPcNumber.isEmpty() ? "Not specified" : userPcNumber;
        String req = userSpecialRequest.isEmpty() ? "(none)" : userSpecialRequest;

        String[] methods = {"GCash", "PayPal", "Cash on Hand"};
        String paymentMethod = (String) JOptionPane.showInputDialog(
                parentDialog,
                "Choose payment method for this order:",
                "Payment Method",
                JOptionPane.PLAIN_MESSAGE,
                null,
                methods,
                methods[0]
        );
        if (paymentMethod == null) {
            return;
        }

        String message =
                "PC Number: " + pc + "\n" +
                        "Special Request: " + req + "\n" +
                        "Payment: " + paymentMethod + "\n\n" +
                        "Items:\n" + itemsText +
                        "\nTOTAL: P" + getCartTotal() + "\n\n" +
                        "Confirm purchase?";

        int confirm = JOptionPane.showConfirmDialog(
                parentDialog,
                message,
                "Confirm Purchase",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (confirm == JOptionPane.OK_OPTION) {
            int totalAmount = getCartTotal();
            showSuccessDialog("Purchase confirmed! " + itemCount + " items for P" + totalAmount + ".");

            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            Order order = new Order(time, itemsText.toString().trim(), totalAmount, pc, paymentMethod);
            orderHistory.add(order);
            addOrderToHistoryTable(order);

            cart.clear();
        }
    }

    private void addOrderToHistoryTable(Order order) {
        if (historyModel != null) {
            historyModel.addRow(new Object[]{
                    order.time,
                    order.itemsSummary,
                    order.total,
                    order.pcNumber,
                    order.paymentMethod
            });
        }
    }

    private void refreshCartTable(DefaultTableModel model) {
        model.setRowCount(0);

        if (cart.isEmpty()) {
            model.addRow(new Object[]{"Your cart is empty", "", "", ""});
            return;
        }

        for (CartItem c : cart) {
            model.addRow(new Object[]{
                    c.name,
                    c.quantity,
                    "P" + c.unitPrice,
                    "P" + c.getTotal()
            });
        }
    }

    // ---------- HELPERS ----------
    private void updateCartButton() {
        int totalItems = 0;
        for (CartItem c : cart) totalItems += c.quantity;
        cartButton.setText("Cart [" + totalItems + "]");
    }

    private int getCartTotal() {
        int total = 0;
        for (CartItem c : cart) total += c.getTotal();
        return total;
    }

    private void showToast(String msg) {
        UIManager.put("OptionPane.messageFont", new Font(APP_FONT, Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new Font(APP_FONT, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(frame, msg);
    }

    private void showSuccessDialog(String msg) {
        ImageIcon okIcon = loadIcon("/assets/check.png", 32);

        if (okIcon != null) {
            JOptionPane.showMessageDialog(
                    frame,
                    msg,
                    "Success",
                    JOptionPane.PLAIN_MESSAGE,
                    okIcon
            );
        } else {
            // Fallback if the icon isn't found
            JOptionPane.showMessageDialog(
                    frame,
                    msg,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private JLabel simpleLabel(String text, int style, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(APP_FONT, style, size));
        label.setForeground(Color.BLACK);  // so it’s readable on default light panels
        return label;
    }

    private JButton simpleButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(APP_FONT, Font.PLAIN, 13));
        button.setFocusPainted(false);
        return button;
    }

    // Load and scale an icon from file path
    private ImageIcon loadIcon(String path, int size) {
        if (path == null || path.isEmpty()) return null;

        // Look up the image as a classpath resource
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.out.println("Image resource NOT found: " + path);
            return null;
        }

        ImageIcon raw = new ImageIcon(url);
        if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) {
            System.out.println("Failed to load image (bad size): " + path);
            return null;
        }

        Image scaled = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
