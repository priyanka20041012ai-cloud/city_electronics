# city_electronics
# 🏪 City Electronics Management System

A **Java Swing desktop application** for managing an electronics store — featuring separate Admin and Customer portals, real-time inventory management, billing with tax calculation, and PDF receipt generation.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Pages & Modules](#pages--modules)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Default Credentials](#default-credentials)
- [Data Files](#data-files)
- [Screenshots / UI Flow](#screenshots--ui-flow)
- [Known Notes](#known-notes)

---

## Overview

**City Electronics Management System** is a Java-based desktop GUI application built with **NetBeans** and **Java Swing**. It simulates a real-world electronics shop management system with two roles:

- **Admin** — manages product inventory (add, update, remove products with images)
- **Customer** — browses products, selects items, calculates bills, and prints PDF receipts

Data is stored in plain text files (`products.txt`, `users.txt`, `password.txt`), making the system lightweight and database-free.

---

## Features

### 👤 Customer Side
- Register a new account (Sign In page)
- Login with username and password
- Browse all available products in a card-style grid view
- View product image, name, price, and live stock count
- Add products to cart using checkboxes and quantity spinners
- Out-of-stock products are automatically disabled with a red badge
- **CALCULATE** — generates an itemized bill with 5% tax
- **PRINT BILL** — saves the bill as a PDF file (no external library needed)
- **RESET** — clears the cart and bill
- **LOGOUT** — returns to the Welcome page

### 🔐 Admin Side
- Secure login with username (`admin`) and password
- Password reset via **OTP sent to email** (Gmail SMTP)
- Show/hide password toggle
- View all products in a table with image previews
- **ADD** — add a new product (ID, name, quantity, image path, price)
- **UPDATE** — edit selected product details
- **REMOVE** — delete selected product from inventory
- Image file picker (JPG, PNG, GIF supported)
- Real-time date and clock display
- **LOGOUT** — returns to the Welcome page

### 📦 Inventory
- Admin and Customer pages share the **same `products.txt`** file
- When a customer purchases, stock is automatically reduced and saved
- Admin sees updated stock when they reload

---

## System Architecture

```
Welcome Page
├── Admin Login  →  Admin Dashboard (product management)
└── Customer Login
        ├── Customer Sign Up  (new account)
        └── Customer Dashboard (browse & buy)
```

**Data Flow:**
```
Admin adds product → products.txt
Customer buys product → stock decreases → products.txt updated
Customer registers → users.txt
Admin resets password → password.txt updated
```

---

## Pages & Modules

| File | Role | Description |
|------|------|-------------|
| `Welcome_page.java` | Landing | Entry screen with Admin Login and Customer Login buttons |
| `Login_page.java` | Admin Auth | Admin login with password, show/hide toggle, OTP-based password reset |
| `Admin_page.java` | Admin Dashboard | Full product CRUD with image support and real-time clock |
| `C_Login.java` | Customer Auth | Customer login; loads user accounts from `users.txt` |
| `C_Signin.java` | Customer Registration | New customer account creation with confirm-password validation |
| `Customer_page.java` | Customer Dashboard | Product cards, cart, billing, PDF export |
| `UserStore.java` | Utility | Manages customer user accounts (load/save from `users.txt`) |

---

## Technologies Used

| Technology | Purpose |
|------------|---------|
| Java SE (JDK 8+) | Core programming language |
| Java Swing | GUI framework (JFrame, JPanel, JTable, JSpinner, etc.) |
| NetBeans IDE | Project scaffolding and GUI form builder |
| Java AWT | Graphics, color, layout management |
| JavaMail API | OTP email sending via Gmail SMTP |
| Java I/O (BufferedReader/Writer) | File-based data storage |
| Nimbus Look & Feel | Modern UI appearance |

---

## Project Structure

```
city_electronics/
├── src/
│   └── city_electronics/
│       ├── Welcome_page.java       # Landing page
│       ├── Login_page.java         # Admin login + OTP reset
│       ├── Admin_page.java         # Admin product management
│       ├── C_Login.java            # Customer login
│       ├── C_Signin.java           # Customer registration
│       ├── Customer_page.java      # Customer shopping portal
│       ├── UserStore.java          # User account management
│       └── images.png              # App logo/banner image
├── build/
│   └── classes/                    # Compiled .class files
├── nbproject/                      # NetBeans project config
├── products.txt                    # Product inventory (shared)
├── users.txt                       # Customer accounts
├── password.txt                    # Admin password (persisted)
├── build.xml                       # Ant build script
└── manifest.mf                     # JAR manifest
```

---

## How to Run

### Prerequisites
- Java JDK 8 or higher installed
- NetBeans IDE (recommended) **or** any Java-capable IDE
- JavaMail library (`javax.mail.jar`) — required only for OTP email feature

### Option 1: Run in NetBeans
1. Open NetBeans IDE
2. Go to **File → Open Project** and select the `city_electronics` folder
3. Right-click the project → **Clean and Build**
4. Right-click → **Run Project**
5. The `Welcome_page` will launch as the entry point

### Option 2: Run via Command Line
```bash
# Compile
javac -cp . src/city_electronics/*.java -d build/classes

# Run
java -cp build/classes city_electronics.Welcome_page
```

### Option 3: Run the JAR (if built)
```bash
java -jar city_electronics.jar
```

> ⚠️ Make sure `products.txt`, `users.txt`, and `password.txt` are in the **same directory as the JAR** when running in production.

---

## Default Credentials

### Admin Login
| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `1234` *(default, stored in `password.txt`)* |

> The admin password can be changed via the **Forget Password** link, which sends an OTP to the registered email.

### Customer Login
- Customers must first **Sign Up** via the Customer Sign In page
- Credentials are saved to `users.txt`

---

## Data Files

### `products.txt`
Stores all product records. Format:
```
ProductID,ProductName,Quantity,ImagePath,Price
```
**Example:**
```
P001,Samsung TV,10,C:/images/tv.jpg,75000
P002,Sony Headphones,5,C:/images/headphone.png,3500
```

### `users.txt`
Stores registered customer accounts. Format:
```
username,password
```
**Example:**
```
john,john123
sara,sara456
```

### `password.txt`
Stores the current admin password (plain text). Created automatically when the admin resets their password.
```
1234
```

---

## Screenshots / UI Flow

```
[Welcome Page]
      |
  ----+----
  |       |
[Admin   [Customer
 Login]   Login]
  |           |
[Admin    [Customer
Dashboard] Register / Login]
  |                |
 CRUD           [Customer
Products]       Dashboard]
                    |
              [Bill & PDF]
```

**Customer Page UI Sections:**
- **Left Panel** — Product cards in a 3-column grid (image, name, price, stock, qty spinner, add-to-cart checkbox)
- **Center Panel** — Live bill/receipt display (monospaced format)
- **Right Panel** — Order summary (tax, subtotal, total payable)
- **Bottom Bar** — CALCULATE | PRINT BILL | RESET | EXIT buttons

---

## Known Notes

- **Email OTP** requires a valid Gmail account and App Password configured in `Login_page.java`. Update the `fromEmail` and `password` fields before use.
- **Image paths** in `products.txt` are absolute paths. When moving the project to another machine, image paths need to be updated.
- **Plain-text passwords** — this is an educational/demo project. In a production system, passwords should be hashed.
- **Tax rate** is fixed at **5%** (defined as `TAX_RATE = 0.05` in `Customer_page.java`).
- The PDF bill is generated using a **pure Java PDF writer** — no external library (like iText) is required.

---

## Author

Developed as a **Java Swing desktop project** for City Electronics store management.

> Built with ❤️ using Java Swing & NetBeans IDE
