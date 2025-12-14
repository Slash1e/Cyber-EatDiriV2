import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

/**
 * Simple Login / Signup UI with SQLite database.
 * After successful login it opens CyberEatDiriApp.
 */
public class CyberEatDiriAuth {

    private static final String APP_FONT = "Poppins";

    // Colors similar to your app
    private static final Color RED_TOP = new Color(0xCC0000);
    private static final Color RED_BOTTOM = new Color(0x7A0000);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BUTTON_RED = new Color(0xC00000);
    private static final Color TEXT_DARK = new Color(0x111111);

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cardContainer;

    // Signup fields
    private JTextField signUpEmailField;
    private JTextField signUpPhoneField;
    private JPasswordField signUpPasswordField;
    private JPasswordField signUpConfirmField;

    // Login fields
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;

    private final DatabaseHelper db = new DatabaseHelper();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CyberEatDiriAuth().start());
    }

    private void start() {
        db.init();  // create DB + table if needed

        frame = new JFrame("CYBER-EATDIRI - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        // Root panel with red gradient background
        JPanel gradientRoot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, RED_TOP,
                        0, getHeight(), RED_BOTTOM
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientRoot.setLayout(new GridBagLayout());

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false); // let gradient show

        cardContainer.add(buildLandingCard(), "landing");
        cardContainer.add(buildSignUpCard(), "signup");
        cardContainer.add(buildLoginCard(), "login");

        gradientRoot.add(cardContainer, new GridBagConstraints());
        frame.setContentPane(gradientRoot);

        showCard("landing");
        frame.setVisible(true);
    }

    private void showCard(String name) {
        cardLayout.show(cardContainer, name);
    }

    // ---------- PANELS ----------

    private JPanel buildLandingCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("CYBER–EATDIRI", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font(APP_FONT, Font.BOLD, 48));
        title.setForeground(Color.WHITE);

        JLabel tagline = new JLabel("GAME • EAT • CONNECT", SwingConstants.CENTER);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setFont(new Font(APP_FONT, Font.PLAIN, 18));
        tagline.setForeground(Color.WHITE);

        panel.add(Box.createVerticalStrut(40));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(tagline);
        panel.add(Box.createVerticalStrut(60));

        JButton loginButton = bigBlackButton("LOG IN");
        loginButton.addActionListener(_ -> showCard("login"));

        JButton signUpButton = bigBlackButton("SIGN UP");
        signUpButton.addActionListener(_ -> showCard("signup"));

        JPanel buttonsRow = new JPanel();
        buttonsRow.setOpaque(false);
        buttonsRow.setLayout(new BoxLayout(buttonsRow, BoxLayout.X_AXIS));
        buttonsRow.add(loginButton);
        buttonsRow.add(Box.createHorizontalStrut(40));
        buttonsRow.add(signUpButton);

        panel.add(buttonsRow);
        panel.add(Box.createVerticalStrut(40));

        return panel;
    }

    private JPanel buildSignUpCard() {
        RoundedPanel card = new RoundedPanel(30);
        card.setBackground(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        card.setPreferredSize(new Dimension(450, 460));
        card.setMaximumSize(new Dimension(450, 460));

        JLabel heading = new JLabel("Sign Up");
        heading.setFont(new Font(APP_FONT, Font.BOLD, 24));
        heading.setForeground(TEXT_DARK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        signUpEmailField = createTextField();
        signUpPhoneField = createTextField();
        signUpPasswordField = createPasswordField();
        signUpConfirmField = createPasswordField();

        card.add(heading);
        card.add(Box.createVerticalStrut(20));
        card.add(labeledField("Email", signUpEmailField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeledField("Phone Number", signUpPhoneField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeledField("Password", signUpPasswordField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeledField("Confirm Password", signUpConfirmField));
        card.add(Box.createVerticalStrut(20));

        JButton signUpBtn = redRoundedButton("Sign Up");
        signUpBtn.addActionListener(_ -> handleSignUp());
        card.add(signUpBtn);
        card.add(Box.createVerticalStrut(15));

        JPanel bottomRow = new JPanel();
        bottomRow.setOpaque(false);
        JLabel already = new JLabel("Already have an account? ");
        already.setFont(new Font(APP_FONT, Font.PLAIN, 12));
        already.setForeground(TEXT_DARK);

        JLabel loginLink = linkLabel("Login");
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCard("login");
            }
        });

        bottomRow.add(already);
        bottomRow.add(loginLink);
        card.add(bottomRow);

        // Wrap to center the card in the gradient background
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, new GridBagConstraints());
        return wrapper;
    }

    private JPanel buildLoginCard() {
        RoundedPanel card = new RoundedPanel(30);
        card.setBackground(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        card.setPreferredSize(new Dimension(450, 360));
        card.setMaximumSize(new Dimension(450, 360));

        JLabel heading = new JLabel("Log In");
        heading.setFont(new Font(APP_FONT, Font.BOLD, 24));
        heading.setForeground(TEXT_DARK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginEmailField = createTextField();
        loginPasswordField = createPasswordField();

        card.add(heading);
        card.add(Box.createVerticalStrut(20));
        card.add(labeledField("Email", loginEmailField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeledField("Password", loginPasswordField));
        card.add(Box.createVerticalStrut(20));

        JButton loginBtn = redRoundedButton("Login");
        loginBtn.addActionListener(_ -> handleLogin());
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(15));

        JPanel bottomRow = new JPanel();
        bottomRow.setOpaque(false);
        JLabel noAccount = new JLabel("Don’t have an account? ");
        noAccount.setFont(new Font(APP_FONT, Font.PLAIN, 12));
        noAccount.setForeground(TEXT_DARK);

        JLabel signUpLink = linkLabel("Sign Up");
        signUpLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCard("signup");
            }
        });

        bottomRow.add(noAccount);
        bottomRow.add(signUpLink);
        card.add(bottomRow);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, new GridBagConstraints());
        return wrapper;
    }

    // ---------- EVENT HANDLERS ----------

    private void handleSignUp() {
        String email = signUpEmailField.getText().trim();
        String phone = signUpPhoneField.getText().trim();
        String password = new String(signUpPasswordField.getPassword());
        String confirm = new String(signUpConfirmField.getPassword());

        if (email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        boolean ok = db.registerUser(email, phone, password);
        if (!ok) {
            showError("Email is already registered.");
            return;
        }

        JOptionPane.showMessageDialog(frame,
                "Account created successfully! You can now log in.",
                "Sign Up",
                JOptionPane.INFORMATION_MESSAGE);

        // Pre-fill login email and go to log in card
        loginEmailField.setText(email);
        loginPasswordField.setText("");
        showCard("login");
    }

    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        boolean valid = db.validateLogin(email, password);
        if (!valid) {
            showError("Invalid email or password.");
            return;
        }

        JOptionPane.showMessageDialog(frame,
                "Login successful! Opening Cyber-EatDiri...",
                "Login",
                JOptionPane.INFORMATION_MESSAGE);

        frame.dispose();

        // 🔹 Open your main CyberEatDiri app window
        // Make sure CyberEatDiriApp has a public start() method.
        new CyberEatDiriApp().start();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- UI HELPERS ----------

    private JButton bigBlackButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(APP_FONT, Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(220, 55));
        btn.setMaximumSize(new Dimension(220, 55));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton redRoundedButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(APP_FONT, Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(BUTTON_RED);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // make it look pill-shaped
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn = new RoundedButton(btn, BUTTON_RED);

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font(APP_FONT, Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(320, 32));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32)); // fill full row
        tf.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        tf.setBackground(new Color(0xEEEEEE));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font(APP_FONT, Font.PLAIN, 14));
        pf.setPreferredSize(new Dimension(320, 32));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32)); // fill full row
        pf.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        pf.setBackground(new Color(0xEEEEEE));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }

    private JPanel labeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(APP_FONT, Font.PLAIN, 13));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);

        return panel;
    }

    private JLabel linkLabel(String text) {
        JLabel link = new JLabel(text);
        link.setFont(new Font(APP_FONT, Font.PLAIN, 12));
        link.setForeground(new Color(0x0066CC));
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return link;
    }

    // ---------- DB HELPER ----------

    /**
     * Simple SQLite helper.
     * Table: users(id, email, phone, password)
     * NOTE: password is stored in plain text for simplicity (not secure for real apps).
     */
    private static class DatabaseHelper {
        private static final String DB_URL = "jdbc:sqlite:cybereatdiri_users.db";

        public void init() {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement st = conn.createStatement()) {
                st.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "email TEXT UNIQUE NOT NULL," +
                                "phone TEXT NOT NULL," +
                                "password TEXT NOT NULL" +
                                ");"
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public boolean registerUser(String email, String phone, String password) {
            String sql = "INSERT INTO users(email, phone, password) VALUES(?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, phone);
                ps.setString(3, password);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                // likely email duplicate
                return false;
            }
        }

        public boolean validateLogin(String email, String password) {
            String sql = "SELECT id FROM users WHERE email = ? AND password = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    // ---------- SMALL UI CLASSES ----------

    // White rounded card panel
    private static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Wraps a JButton to draw a pill-shaped red background
    private static class RoundedButton extends JButton {
        private final JButton inner;
        private final Color bg;

        public RoundedButton(JButton inner, Color bg) {
            this.inner = inner;
            this.bg = bg;
            setLayout(new BorderLayout());
            add(inner, BorderLayout.CENTER);
            setBorderPainted(false);
            setOpaque(false);
            setContentAreaFilled(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return inner.getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = getHeight();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
