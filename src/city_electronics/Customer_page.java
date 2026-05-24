/*
 * Customer_page.java
 * City Electronics Management System
 * - Clean White & Grey Design
 * - Reads stock from products.txt (Admin page)
 * - Decreases stock when purchased → writes back to products.txt
 * - Shows "OUT OF STOCK" badge when stock = 0
 */
package city_electronics;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Customer_page extends javax.swing.JFrame {

    private static final double TAX_RATE = 0.05;

    // ── White / Grey Color Palette ────────────────────────────────────────────
    private static final Color C_WHITE       = Color.WHITE;
    private static final Color C_LIGHT_GREY  = new Color(245, 245, 245);
    private static final Color C_BORDER      = new Color(210, 210, 210);
    private static final Color C_HEADER_BG   = new Color(50,  50,  50);   // dark header
    private static final Color C_HEADER_FG   = Color.WHITE;
    private static final Color C_ACCENT      = new Color(60,  60,  60);   // dark text accent
    private static final Color C_TEXT        = new Color(30,  30,  30);
    private static final Color C_MUTED       = new Color(120, 120, 120);
    private static final Color C_BTN_DARK    = new Color(50,  50,  50);
    private static final Color C_BTN_RED     = new Color(200,  40,  40);
    private static final Color C_BTN_GREEN   = new Color(40,  160,  80);
    private static final Color C_BTN_ORANGE  = new Color(210, 120,   0);
    private static final Color C_GREEN_TEXT  = new Color(0,  140,  60);
    private static final Color C_RED_TEXT    = new Color(200,  30,  30);
    private static final Color C_OOS_BG      = new Color(255, 230, 230);  // out-of-stock bg

    // ── Product model ─────────────────────────────────────────────────────────
    static class Product {
        String id, name, imagePath;
        double price;
        int    stock;
        JSpinner  spinner;
        JCheckBox checkbox;
        JLabel    stockLabel;   // live stock label on card
        JPanel    card;         // card panel reference

        Product(String id, String name, int stock, double price, String imagePath) {
            this.id = id; this.name = name; this.stock = stock;
            this.price = price; this.imagePath = imagePath;
        }
    }

    private final java.util.List<Product> products = new ArrayList<Product>();

    // ── UI refs ───────────────────────────────────────────────────────────────
    private JLabel     lblTime, lblDate;
    private JTextArea  receiptArea;
    private JTextField txtTax, txtSubtotal, txtTotal;
    private JButton    btnTotal, btnReset, btnExit, btnReceipt, btnLogout;
    private JPanel     cardGrid;
    private javax.swing.Timer clockTimer;
    private String lastBillText = "";

    // ─────────────────────────────────────────────────────────────────────────
    public Customer_page() {
        setTitle("City Electronics - Customer Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 820);
        setLocationRelativeTo(null);
        setResizable(false);
        loadProducts();
        buildUI();
        startClock();
    }

    // ── Use the SAME path as Admin_page so both pages share one file ────────────
    private java.io.File getProductsFile() {
        return Admin_page.getProductsFile();
    }

    // ── Load products from products.txt (admin format: ID,Name,Qty,Image,Price)
    private void loadProducts() {
        products.clear();
        java.io.File productFile = getProductsFile();

        System.out.println("[Customer_page] Loading products from: "
            + productFile.getAbsolutePath()
            + "  exists=" + productFile.exists());

        if (!productFile.exists()) {
            JOptionPane.showMessageDialog(null,
                "products.txt not found!\nExpected at:\n" + productFile.getAbsolutePath()
                + "\n\nPlease add products from the Admin page first.",
                "No Products File", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(productFile));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",", 5);
                if (p.length < 5) {
                    System.out.println("[Customer_page] Skipping malformed line: " + line);
                    continue;
                }
                try {
                    products.add(new Product(
                        p[0].trim(), p[1].trim(),
                        Integer.parseInt(p[2].trim()),
                        Double.parseDouble(p[4].trim()),
                        p[3].trim()
                    ));
                    System.out.println("[Customer_page] Loaded: " + p[1].trim()
                        + " | stock=" + p[2].trim()
                        + " | price=" + p[4].trim()
                        + " | img=" + p[3].trim());
                } catch (NumberFormatException nfe) {
                    System.out.println("[Customer_page] Bad number in line: " + line);
                }
            }
            br.close();
            System.out.println("[Customer_page] Total products loaded: " + products.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error reading products.txt:\n" + e.getMessage(),
                "Read Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ── Save updated stock back to products.txt ───────────────────────────────
    private void saveStockToFile() {
        try {
            java.io.File f = getProductsFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (Product pr : products) {
                String priceStr = (pr.price == Math.floor(pr.price))
                    ? String.valueOf((long) pr.price)
                    : String.valueOf(pr.price);
                bw.write(pr.id + "," + pr.name + "," + pr.stock + "," + pr.imagePath + "," + priceStr);
                bw.newLine();
            }
            bw.close();
            System.out.println("[Customer_page] Stock saved to: " + f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Main UI ───────────────────────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(C_LIGHT_GREY);
        setContentPane(root);
        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);
    }

    // ── HEADER ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(null);
        h.setBackground(C_HEADER_BG);
        h.setPreferredSize(new Dimension(1440, 62));

        JLabel logo = new JLabel("  CITY ELECTRONICS  |  Customer Portal");
        logo.setFont(new Font("Arial", Font.BOLD, 22));
        logo.setForeground(C_HEADER_FG);
        logo.setBounds(18, 14, 520, 34);
        h.add(logo);

        lblTime = new JLabel("00:00:00");
        lblTime.setFont(new Font("Arial", Font.BOLD, 16));
        lblTime.setForeground(new Color(200, 200, 200));
        lblTime.setBounds(660, 18, 200, 26);
        h.add(lblTime);

        lblDate = new JLabel();
        lblDate.setFont(new Font("Arial", Font.BOLD, 15));
        lblDate.setForeground(new Color(200, 200, 200));
        lblDate.setBounds(880, 18, 300, 26);
        h.add(lblDate);

        JLabel welcome = new JLabel("Welcome, Customer!");
        welcome.setFont(new Font("Arial", Font.ITALIC | Font.PLAIN, 13));
        welcome.setForeground(new Color(180, 180, 180));
        welcome.setBounds(1210, 18, 210, 26);
        h.add(welcome);

        return h;
    }

    // ── CENTER ────────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel c = new JPanel(new BorderLayout(8, 0));
        c.setBackground(C_LIGHT_GREY);
        c.setBorder(new EmptyBorder(10, 10, 6, 10));

        cardGrid = new JPanel(new GridLayout(0, 3, 8, 8));
        cardGrid.setBackground(C_LIGHT_GREY);
        buildProductCards();
        JScrollPane gs = new JScrollPane(cardGrid);
        gs.setPreferredSize(new Dimension(690, 0));
        gs.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        gs.getViewport().setBackground(C_LIGHT_GREY);
        c.add(gs, BorderLayout.WEST);

        c.add(buildBillPanel(),     BorderLayout.CENTER);
        c.add(buildSummaryPanel(),  BorderLayout.EAST);

        return c;
    }

    // ── Bill display panel ────────────────────────────────────────────────────
    private JPanel buildBillPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(C_WHITE);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        bar.setBackground(C_ACCENT);
        JLabel t = new JLabel("  Bill Details");
        t.setFont(new Font("Arial", Font.BOLD, 15));
        t.setForeground(C_WHITE);
        bar.add(t);
        p.add(bar, BorderLayout.NORTH);

        receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        receiptArea.setEditable(false);
        receiptArea.setBackground(C_WHITE);
        receiptArea.setForeground(C_TEXT);
        receiptArea.setMargin(new Insets(10, 14, 10, 14));
        receiptArea.setText(getEmptyBillText());

        JScrollPane sc = new JScrollPane(receiptArea);
        sc.setBorder(null);
        p.add(sc, BorderLayout.CENTER);

        return p;
    }

    private String getEmptyBillText() {
        String d1 = sep('=', 48);
        String d2 = sep('-', 48);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  ").append(d1).append("\n");
        sb.append("        CITY ELECTRONICS\n");
        sb.append("      TECHGEN VENDORS PVT.LTD\n");
        sb.append("         CUSTOMER BILL\n");
        sb.append("  ").append(d1).append("\n");
        sb.append("  Date     : ").append(new SimpleDateFormat("dd-MM-yyyy").format(new Date())).append("\n");
        sb.append("  ").append(d2).append("\n");
        sb.append(String.format("  %-22s  %4s  %11s\n", "Product", "Qty", "Amount(Rs.)"));
        sb.append("  ").append(d2).append("\n\n");
        sb.append("  [ Select products from left panel ]\n");
        sb.append("  [ Tick 'ADD TO CART' & set qty   ]\n");
        sb.append("  [ Click CALCULATE to see totals  ]\n\n");
        sb.append("  ").append(d2).append("\n");
        sb.append(String.format("  %-28s  %9s\n", "Subtotal (Rs.)", "0.00"));
        sb.append(String.format("  %-28s  %9s\n", "Tax 5%  (Rs.)",  "0.00"));
        sb.append("  ").append(d2).append("\n");
        sb.append(String.format("  %-28s  %9s\n", "TOTAL   (Rs.)",  "0.00"));
        sb.append("  ").append(d1).append("\n");
        sb.append("  Thank you for shopping at City Electronics!\n");
        sb.append("  ").append(d1).append("\n");
        return sb.toString();
    }

    // ── Order Summary (right panel) ───────────────────────────────────────────
    private JPanel buildSummaryPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(C_WHITE);
        p.setPreferredSize(new Dimension(272, 0));
        p.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));

        JPanel strip = new JPanel(null);
        strip.setBackground(C_ACCENT);
        strip.setBounds(0, 0, 272, 44);
        JLabel hdr = new JLabel("   Order Summary");
        hdr.setFont(new Font("Arial", Font.BOLD, 15));
        hdr.setForeground(C_WHITE);
        hdr.setBounds(0, 0, 272, 44);
        strip.add(hdr);
        p.add(strip);

        int y = 60;

        JLabel lTax = makeSumLabel("Tax (5%)", 16, y); p.add(lTax); y += 26;
        txtTax = makeSumField("0.00");
        txtTax.setBounds(16, y, 240, 34); p.add(txtTax); y += 48;

        JLabel lSub = makeSumLabel("Subtotal", 16, y); p.add(lSub); y += 26;
        txtSubtotal = makeSumField("0.00");
        txtSubtotal.setBounds(16, y, 240, 34); p.add(txtSubtotal); y += 48;

        JLabel lTot = makeSumLabel("TOTAL PAYABLE", 16, y);
        lTot.setFont(new Font("Arial", Font.BOLD, 14));
        lTot.setForeground(C_ACCENT);
        p.add(lTot); y += 26;

        txtTotal = makeSumField("0.00");
        txtTotal.setFont(new Font("Arial", Font.BOLD, 20));
        txtTotal.setForeground(C_GREEN_TEXT);
        txtTotal.setBackground(new Color(240, 255, 245));
        txtTotal.setBounds(16, y, 240, 42); p.add(txtTotal); y += 58;

        JSeparator sep = new JSeparator();
        sep.setBounds(16, y, 240, 2);
        sep.setForeground(C_BORDER);
        p.add(sep); y += 14;

        btnLogout = makeBtn("LOG OUT", C_BTN_RED, C_WHITE);
        btnLogout.setBounds(16, y, 240, 40);
        p.add(btnLogout);

        return p;
    }

    private JLabel makeSumLabel(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(C_MUTED);
        l.setBounds(x, y, 240, 22);
        return l;
    }

    private JTextField makeSumField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("Arial", Font.BOLD, 16));
        f.setForeground(C_TEXT);
        f.setBackground(C_LIGHT_GREY);
        f.setEditable(false);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            new EmptyBorder(2, 10, 2, 8)));
        return f;
    }

    // ── Bottom bar ────────────────────────────────────────────────────────────
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(null);
        bar.setBackground(C_HEADER_BG);
        bar.setPreferredSize(new Dimension(1440, 58));

        btnTotal   = makeBtn("CALCULATE",  C_WHITE,      C_ACCENT);
        btnReceipt = makeBtn("PRINT BILL", C_BTN_GREEN,  C_WHITE);
        btnReset   = makeBtn("RESET",      C_BTN_ORANGE, C_WHITE);
        btnExit    = makeBtn("EXIT",       C_BTN_RED,    C_WHITE);
        
        // Make sure text is always visible
        btnTotal.setForeground(C_ACCENT);
        btnTotal.setBackground(C_WHITE);
        
        btnReceipt.setForeground(C_ACCENT);
        btnReceipt.setBackground(C_WHITE);
        
        btnReset.setForeground(C_ACCENT);
        btnReset.setBackground(C_WHITE);
        
         btnExit .setForeground(C_ACCENT);
        btnExit .setBackground(C_WHITE);

        int x = 16;
        for (JButton b : new JButton[]{btnTotal, btnReceipt, btnReset, btnExit}) {
            b.setBounds(x, 9, 152, 40);
            bar.add(b);
            x += 164;
        }

        JLabel note = new JLabel("City Electronics Management System  |  Customer Portal");
        note.setFont(new Font("Arial", Font.PLAIN, 12));
        note.setForeground(new Color(160, 160, 160));
        note.setBounds(780, 18, 600, 22);
        bar.add(note);

        btnTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { doBillCalculation(); }
        });
        btnReceipt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { printBillToPDF(); }
        });
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { resetAll(); }
        });
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { handleExit(); }
        });
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { handleLogout(); }
        });

        return bar;
    }

    // ── Product cards ─────────────────────────────────────────────────────────
    private void buildProductCards() {
        cardGrid.removeAll();
        for (int i = 0; i < products.size(); i++) {
            cardGrid.add(buildCard(products.get(i)));
        }
        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private JPanel buildCard(final Product prod) {
        prod.card = new JPanel(null);
        prod.card.setBackground(C_WHITE);
        prod.card.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        prod.card.setPreferredSize(new Dimension(210, 330));

        // Top strip — grey
        JPanel strip = new JPanel(null);
        strip.setBackground(C_WHITE);
        strip.setBounds(0, 0, 210, 5);
        prod.card.add(strip);

        // Product image
        JLabel imgLbl = new JLabel();
        imgLbl.setBounds(10, 12, 190, 140);
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setVerticalAlignment(SwingConstants.CENTER);
        imgLbl.setBackground(C_LIGHT_GREY);
        imgLbl.setOpaque(true);
        imgLbl.setBorder(BorderFactory.createLineBorder(C_BORDER));
        loadImage(imgLbl, prod.imagePath);
        prod.card.add(imgLbl);

        // Product name
        JLabel lblName = new JLabel(prod.name, SwingConstants.CENTER);
        lblName.setFont(new Font("Arial", Font.BOLD, 14));
        lblName.setForeground(C_TEXT);
        lblName.setBounds(4, 157, 202, 24);
        prod.card.add(lblName);

        // Price badge
        JLabel lblPrice = new JLabel("Rs. " + String.format("%,.0f", prod.price), SwingConstants.CENTER);
        lblPrice.setFont(new Font("Arial", Font.BOLD, 13));
        lblPrice.setForeground(C_WHITE);
        lblPrice.setBackground(C_WHITE);
        lblPrice.setOpaque(true);
        lblPrice.setBounds(10, 185, 190, 24);
        prod.card.add(lblPrice);

        // Code label
        JLabel lblCode = new JLabel("Code: " + prod.id, SwingConstants.CENTER);
        lblCode.setFont(new Font("Arial", Font.PLAIN, 11));
        lblCode.setForeground(C_MUTED);
        lblCode.setBounds(4, 213, 202, 18);
        prod.card.add(lblCode);

        // ── Stock label (live) ────────────────────────────────────────────────
        prod.stockLabel = new JLabel();
        prod.stockLabel.setFont(new Font("Arial", Font.BOLD, 12));
        prod.stockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        prod.stockLabel.setBounds(4, 233, 202, 20);
        updateStockLabel(prod);
        prod.card.add(prod.stockLabel);

        // Qty spinner
        JLabel lblQty = new JLabel("Qty:");
        lblQty.setFont(new Font("Arial", Font.BOLD, 12));
        lblQty.setForeground(C_TEXT);
        lblQty.setBounds(14, 258, 36, 26);
        prod.card.add(lblQty);

        SpinnerNumberModel sm = new SpinnerNumberModel(0, 0, Math.max(prod.stock, 0), 1);
        prod.spinner = new JSpinner(sm);
        prod.spinner.setFont(new Font("Arial", Font.PLAIN, 13));
        prod.spinner.setBounds(52, 258, 72, 26);
        prod.card.add(prod.spinner);

        // ADD TO CART checkbox
        prod.checkbox = new JCheckBox("ADD TO CART");
        prod.checkbox.setFont(new Font("Arial", Font.BOLD, 11));
        prod.checkbox.setBackground(C_WHITE);
        prod.checkbox.setForeground(C_ACCENT);
        prod.checkbox.setBounds(8, 290, 150, 26);
        prod.card.add(prod.checkbox);

        // Disable card if out of stock
        if (prod.stock <= 0) {
            disableCard(prod);
        }

        return prod.card;
    }

    // ── Update stock label text & color ───────────────────────────────────────
    private void updateStockLabel(Product prod) {
        if (prod.stock <= 0) {
            prod.stockLabel.setText("OUT OF STOCK");
            prod.stockLabel.setForeground(C_RED_TEXT);
            prod.stockLabel.setOpaque(true);
            prod.stockLabel.setBackground(C_OOS_BG);
        } else if (prod.stock <= 3) {
            prod.stockLabel.setText("Stock: " + prod.stock + "  (Low!)");
            prod.stockLabel.setForeground(new Color(180, 80, 0));
            prod.stockLabel.setOpaque(false);
        } else {
            prod.stockLabel.setText("In Stock: " + prod.stock);
            prod.stockLabel.setForeground(C_GREEN_TEXT);
            prod.stockLabel.setOpaque(false);
        }
    }

    // ── Disable card visually when out of stock ───────────────────────────────
    private void disableCard(Product prod) {
        prod.spinner.setEnabled(false);
        prod.checkbox.setEnabled(false);
        prod.checkbox.setSelected(false);
        prod.card.setBackground(new Color(250, 245, 245));
    }

    // ── Enable card when stock is restored ───────────────────────────────────
    private void enableCard(Product prod) {
        prod.spinner.setEnabled(true);
        prod.checkbox.setEnabled(true);
        prod.card.setBackground(C_WHITE);
    }

    // ── Load image ────────────────────────────────────────────────────────────
    private void loadImage(JLabel lbl, String path) {
        if (path == null || path.trim().isEmpty()) { lbl.setText("No Image"); return; }
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getIconWidth() <= 0) { lbl.setText("No Image"); return; }
            Image img = icon.getImage().getScaledInstance(188, 138, Image.SCALE_SMOOTH);
            lbl.setIcon(new ImageIcon(img));
        } catch (Exception ex) { lbl.setText("No Image"); }
    }

    // ── Separator helper ──────────────────────────────────────────────────────
    private String sep(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    // ── Actual bill calculation (called by CALCULATE button) ──────────────────
    private void doBillCalculation() {
        // Step 1: collect purchase map
        java.util.Map<Product, Integer> cart = new LinkedHashMap<Product, Integer>();
        for (Product pr : products) {
            if (pr.checkbox.isSelected()) {
                int q = (int) pr.spinner.getValue();
                if (q > 0) cart.put(pr, q);
            }
        }

        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please tick 'ADD TO CART' and set qty > 0 for at least one product.",
                "Cart Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Step 2: validate stock
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product pr = entry.getKey();
            int q = entry.getValue();
            if (q > pr.stock) {
                JOptionPane.showMessageDialog(this,
                    "Not enough stock for '" + pr.name + "'!\nAvailable: " + pr.stock,
                    "Stock Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Step 3: calculate totals
        double subtotal = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            subtotal += entry.getKey().price * entry.getValue();
        }
        double tax   = subtotal * TAX_RATE;
        double total = subtotal + tax;

        txtSubtotal.setText(String.format("Rs. %,.2f", subtotal));
        txtTax.setText(String.format("Rs. %,.2f",      tax));
        txtTotal.setText(String.format("Rs. %,.2f",    total));

        // Step 4: deduct stock
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product pr = entry.getKey();
            int q = entry.getValue();
            pr.stock -= q;

            // Update spinner max
            SpinnerNumberModel sm = (SpinnerNumberModel) pr.spinner.getModel();
            sm.setMaximum(pr.stock);
            pr.spinner.setValue(0);

            // Update stock label on card
            updateStockLabel(pr);
            if (pr.stock <= 0) disableCard(pr);

            // Uncheck
            pr.checkbox.setSelected(false);
        }

        // Step 5: save updated stock → products.txt (admin sees updated stock)
        saveStockToFile();

        // Step 6: build bill text
        String d1 = sep('=', 48);
        String d2 = sep('-', 48);
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a");
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("  ").append(d1).append("\n");
        sb.append("        CITY ELECTRONICS\n");
        sb.append("      TECHGEN VENDORS PVT.LTD\n");
        sb.append("         CUSTOMER BILL\n");
        sb.append("  ").append(d1).append("\n");
        sb.append("  Date     : ").append(fmt.format(new Date())).append("\n");
        sb.append("  ").append(d2).append("\n");
        sb.append(String.format("  %-22s  %4s  %11s\n", "Product", "Qty", "Amount(Rs.)"));
        sb.append("  ").append(d2).append("\n");

        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product pr = entry.getKey();
            int q = entry.getValue();
            sb.append(String.format("  %-22s  %4d  %11.2f\n", pr.name, q, pr.price * q));
        }

        sb.append("  ").append(d2).append("\n");
        sb.append(String.format("  %-28s  %9d\n",  "Total Items Purchased:", cart.size()));
        sb.append("  ").append(d2).append("\n");
        sb.append(String.format("  %-28s  %9.2f\n", "Subtotal (Rs.)", subtotal));
        sb.append(String.format("  %-28s  %9.2f\n", "Tax 5%  (Rs.)",  tax));
        sb.append("  ").append(d2).append("\n");
        sb.append(String.format("  %-28s  %9.2f\n", "TOTAL   (Rs.)",  total));
        sb.append("  ").append(d1).append("\n");
        sb.append("  Thank you for shopping at City Electronics!\n");
        sb.append("  ").append(d1).append("\n");

        lastBillText = sb.toString();
        receiptArea.setText(lastBillText);
        receiptArea.setCaretPosition(0);
    }

    // ── Print bill as PDF ─────────────────────────────────────────────────────
    private void printBillToPDF() {
        if (lastBillText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please calculate bill first!", "No Bill",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Bill as PDF");
        fc.setSelectedFile(new java.io.File("CityElectronics_Bill_"
            + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf"))
            file = new java.io.File(file.getAbsolutePath() + ".pdf");

        try {
            writePdfBill(file);
            JOptionPane.showMessageDialog(this,
                "Bill saved as PDF:\n" + file.getAbsolutePath(),
                "PDF Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving PDF: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Write PDF (no external library) ──────────────────────────────────────
    private void writePdfBill(java.io.File file) throws IOException {
        String d1 = sep('=', 50);
        String d2 = sep('-', 50);
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a");

        java.util.List<String> lines = new ArrayList<String>();
        lines.add(""); lines.add("  " + d1);
        lines.add("          CITY ELECTRONICS");
        lines.add("        TECHGEN VENDORS PVT.LTD");
        lines.add("           CUSTOMER BILL RECEIPT");
        lines.add("  " + d1);
        lines.add("  Date     : " + fmt.format(new Date()));
        lines.add("  " + d2);
        lines.add(String.format("  %-24s  %4s  %11s", "Product", "Qty", "Amount(Rs.)"));
        lines.add("  " + d2);

        double subtotal = 0; int itemCount = 0;
        for (Product pr : products) {
            if (pr.checkbox.isSelected()) {
                int q = (int) pr.spinner.getValue();
                if (q > 0) {
                    double amt = pr.price * q;
                    subtotal += amt; itemCount++;
                    lines.add(String.format("  %-24s  %4d  %11.2f", pr.name, q, amt));
                }
            }
        }

        double tax = subtotal * TAX_RATE, total = subtotal + tax;
        lines.add("  " + d2);
        lines.add(String.format("  %-30s  %9d",   "Total Items:", itemCount));
        lines.add("  " + d2);
        lines.add(String.format("  %-30s  %9.2f", "Subtotal (Rs.)", subtotal));
        lines.add(String.format("  %-30s  %9.2f", "Tax 5%  (Rs.)",  tax));
        lines.add("  " + d2);
        lines.add(String.format("  %-30s  %9.2f", "TOTAL   (Rs.)",  total));
        lines.add("  " + d1);
        lines.add("    Thank you for shopping at City Electronics!");
        lines.add("  " + d1); lines.add("");

        StringBuilder cs = new StringBuilder();
        cs.append("BT\n/F1 10 Tf\n50 750 Td\n14 TL\n");
        for (String ln : lines) {
            String s = ln.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("\r", "");
            cs.append("(" + s + ") Tj T*\n");
        }
        cs.append("ET\n");

        byte[] csBytes = cs.toString().getBytes("ISO-8859-1");
        FileOutputStream fos = new FileOutputStream(file);
        int[] offsets = new int[6];
        String header = "%PDF-1.4\n";
        fos.write(header.getBytes("ISO-8859-1"));
        int pos = header.length();

        String[] objs = new String[6];
        objs[1] = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        objs[2] = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";
        objs[3] = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 5 0 R /Resources << /Font << /F1 4 0 R >> >> >>\nendobj\n";
        objs[4] = "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n";

        for (int i = 1; i <= 4; i++) {
            offsets[i] = pos;
            byte[] b = objs[i].getBytes("ISO-8859-1");
            fos.write(b); pos += b.length;
        }

        offsets[5] = pos;
        String obj5h = "5 0 obj\n<< /Length " + csBytes.length + " >>\nstream\n";
        fos.write(obj5h.getBytes("ISO-8859-1")); pos += obj5h.length();
        fos.write(csBytes); pos += csBytes.length;
        String obj5f = "\nendstream\nendobj\n";
        fos.write(obj5f.getBytes("ISO-8859-1")); pos += obj5f.length();

        String xref = "xref\n0 6\n0000000000 65535 f \n"
            + String.format("%010d 00000 n \n", offsets[1])
            + String.format("%010d 00000 n \n", offsets[2])
            + String.format("%010d 00000 n \n", offsets[3])
            + String.format("%010d 00000 n \n", offsets[4])
            + String.format("%010d 00000 n \n", offsets[5]);
        fos.write(xref.getBytes("ISO-8859-1"));

        String trailer = "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + pos + "\n%%EOF\n";
        fos.write(trailer.getBytes("ISO-8859-1"));
        fos.close();
    }

    // ── Reset ─────────────────────────────────────────────────────────────────
    private void resetAll() {
        for (Product pr : products) {
            pr.spinner.setValue(0);
            pr.checkbox.setSelected(false);
        }
        lastBillText = "";
        receiptArea.setText(getEmptyBillText());
        txtTax.setText("0.00");
        txtSubtotal.setText("0.00");
        txtTotal.setText("0.00");
    }

    // ── Exit / Logout ─────────────────────────────────────────────────────────
    private void handleExit() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
            "Exit", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (clockTimer != null) clockTimer.stop();
            System.exit(0);
        }
    }

    private void handleLogout() {
        int c = JOptionPane.showConfirmDialog(this, "Logout and return to login?",
            "Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (clockTimer != null) clockTimer.stop();
             new Welcome_page().setVisible(true);
            dispose();
        }
    }

    // ── Clock ─────────────────────────────────────────────────────────────────
    private void startClock() {
        lblDate.setText(new SimpleDateFormat("EEEE, dd-MM-yyyy").format(new Date()));
        clockTimer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                lblTime.setText(new SimpleDateFormat("hh:mm:ss a").format(new Date()));
            }
        });
        clockTimer.setInitialDelay(0);
        clockTimer.start();
    }

    // ── Button factory ────────────────────────────────────────────────────────
    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Wiring: buildBottomBar calls doBillCalculation ────────────────────────
    // (Already wired above via btnTotal.addActionListener → doBillCalculation)

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() { new Customer_page().setVisible(true); }
        });
    }
}