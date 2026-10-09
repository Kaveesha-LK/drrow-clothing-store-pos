# D’RROW Clothing Store POS & Inventory Management System

A professional, enterprise-grade desktop Point of Sale (POS) and Inventory Management System designed specifically for clothing and fashion apparel retail stores. Built with **Java 21/17 LTS**, **Java Swing**, **Maven**, and **MySQL 8.x / MariaDB**, with full **Apache NetBeans** compatibility.

---

## 🌟 Key Capabilities & Architectural Highlights

- **Clothing Variant Architecture**: First-class support for apparel matrices (Size: XS–3XL & Free Size; Colours with hex codes). Each sellable variant maintains its own independent SKU, Code 128 barcode, cost price, selling price, and stock balance.
- **Code 128 Barcode Engine**: Automated unique barcode generation (`DRTS000001`), collision prevention, and rendering using standard ZXing with native pure-Java graphics fallbacks.
- **Clothing Tag Label Printing**: Dedicated printing module for thermal roll label tags (50x30mm) and multi-tag A4 sheet layouts (21 labels per page), displaying D'RROW branding, Item SKU, Size, Colour, Retail Price, and high-DPI Code 128 barcodes.
- **USB HID Barcode Scanner Integration**: Built for standard handheld USB barcode scanners operating in keyboard-wedge mode. Automatic focus retention, instantaneous product lookup, cart population, and input clearing.
- **High-Speed Billing & Checkout**: Touch/keyboard friendly POS interface with item search, category filtering, cart operations (+/- Qty, discounts, line totals), cash change computation, card/transfer logging, and bill hold/resume.
- **ACID Database Transactions**: Guaranteed atomicity across invoices, line items, payment splits, inventory deductions, stock movement logs, and audit entries. Never leaves partial transactions.
- **Full Returns & Exchange Lifecycle**: Look up historical invoices, select return items, validate quantities against original sales, restore inventory, and calculate refunds while keeping original sales records immutable.
- **Inventory & Warehouse Operations**: Current, available, and reserved balances, inward supplier stock receiving with purchase orders, and stock adjustments with mandatory reasons (Damage, Lost, Count Correction, Found, Manual).
- **Role-Based Access Control (RBAC)**: Enforced roles (`ADMIN`, `MANAGER`, `CASHIER`, `STOCK_STAFF`) with granular permissions guarding menu items, sensitive cost prices, product deletions, and user accounts.
- **Security & Password Hashing**: Passwords encrypted using industry-standard **BCrypt**. First-run detection forces administrative password changes on initial login.
- **JasperReports & Thermal Printing**: High-fidelity 80mm thermal bill printable and JasperReports `.jrxml` templates with store header, itemized breakdown, tax/discount details, payment methods, and exchange policies.
- **Comprehensive Business Intelligence**: Executive dashboard KPIs (Gross Sales, Gross Margin, Invoice counts, Low Stock alerts) and exportable reports (Daily, Monthly, By Cashier, Product, Category, Stock Valuation, and Profit).

---

## 📂 Project Structure

```
drrow-pos/
├── pom.xml                               # Maven project configuration (NetBeans compatible)
├── README.md                             # Complete setup & developer documentation
├── DRROW_POS_A-Z_GUIDE.md                # Comprehensive beginner's guide (A-Z)
├── database/
│   ├── schema.sql                        # Normalized DDL script (20+ tables, indexes, constraints)
│   ├── seed.sql                          # Roles, permissions, settings, initial accounts
│   └── sample_data.sql                   # Realistic D'RROW clothing products, variants & stock
├── src/
│   ├── main/
│   │   ├── java/com/drrow/pos/
│   │   │   ├── Main.java                 # Application entry point & bootstrap
│   │   │   ├── config/                   # Database & application properties loaders
│   │   │   │   ├── AppConfig.java
│   │   │   │   └── DatabaseConfig.java
│   │   │   ├── model/                    # Domain entity models (23 classes)
│   │   │   ├── dao/                      # JDBC Data Access Objects with PreparedStatement
│   │   │   ├── service/                  # Business logic & atomic transaction services
│   │   │   ├── controller/               # Session context & POS cart controller
│   │   │   ├── util/                     # BCrypt, BarcodeUtil, FormatUtil, Printable engines
│   │   │   ├── reports/                  # JasperReports compile & fill bridge
│   │   │   └── ui/                       # Swing UI: Modern Theme, Components, and Panels
│   │   │       ├── theme/UITheme.java
│   │   │       ├── components/           # ModernButton, ModernTable, KpiCard, SearchField
│   │   │       ├── LoginFrame.java
│   │   │       ├── ChangePasswordDialog.java
│   │   │       ├── MainFrame.java
│   │   │       ├── DashboardPanel.java
│   │   │       ├── POSPanel.java
│   │   │       ├── BarcodePrintPanel.java
│   │   │       ├── ProductManagementPanel.java
│   │   │       ├── InventoryPanel.java
│   │   │       ├── SalesHistoryPanel.java
│   │   │       ├── ReturnDialog.java
│   │   │       ├── CustomerManagementPanel.java
│   │   │       ├── ReportsPanel.java
│   │   │       ├── SettingsPanel.java
│   │   │       ├── UserManagementPanel.java
│   │   │       ├── AuditLogPanel.java
│   │   │       ├── ReceiptPreviewDialog.java
│   │   │       └── BarcodeLabelPreviewDialog.java
│   │   └── resources/
│   │       ├── config/                   # db.properties, app.properties
│   │       ├── reports/                  # JasperReports JRXML templates
│   │       └── database/                 # Bundled SQL scripts for offline setup
│   └── test/
│       └── java/com/drrow/pos/test/
│           └── WorkflowIntegrationTest.java # Complete Requirement #39 verification test
└── docs/
    └── TROUBLESHOOTING.md                # In-depth error resolution guide
```

---

## 💻 Prerequisites & System Requirements

| Component | Minimum Version | Recommended Version |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | OpenJDK / Oracle JDK 17 LTS | OpenJDK / Oracle JDK 21 LTS |
| **Build Tool** | Apache Maven 3.8+ | Apache Maven 3.9+ (or NetBeans bundled) |
| **IDE** | Apache NetBeans 17+ | Apache NetBeans 21 or 22 |
| **Database Server** | MySQL 8.0+ or MariaDB 10.6+ | MySQL 8.3+ or MariaDB 11.x |
| **Hardware** | 4 GB RAM, 500 MB Disk | 8 GB RAM, 1 GB SSD |
| **Peripherals** | Standard 1D/2D USB Barcode Scanner, 80mm ESC/POS Receipt Printer |

---

## 🚀 Quick Setup & Installation Guide

### Step 1: Database Setup
1. Open your terminal or MySQL Workbench / HeidiSQL:
   ```bash
   mysql -u root -p
   ```
2. Execute the schema script:
   ```sql
   source /path/to/drrow-pos/database/schema.sql;
   ```
3. Execute the seed script:
   ```sql
   source /path/to/drrow-pos/database/seed.sql;
   ```
4. (Optional) Load realistic D'RROW sample products and inventory:
   ```sql
   source /path/to/drrow-pos/database/sample_data.sql;
   ```

### Step 2: Configure Database Credentials
Edit `src/main/resources/config/db.properties` (or place a `db.properties` file in the root execution directory):
```properties
db.type=mysql
db.host=localhost
db.port=3306
db.name=drrow_pos
db.user=root
db.password=YourRootPasswordHere
db.useSSL=false
db.serverTimezone=UTC
db.allowPublicKeyRetrieval=true
```

### Step 3: Open and Run in Apache NetBeans
1. Launch **Apache NetBeans**.
2. Select **File** -> **Open Project...** (`Ctrl + Shift + O`).
3. Browse to the folder containing `pom.xml` (`drrow-pos`). NetBeans will automatically recognize it with the Maven "Ma" badge.
4. Click **Open Project**.
5. Wait a few seconds for NetBeans to scan dependencies.
6. Right-click the project root in the **Projects** explorer and select **Clean and Build**.
7. Click the green **Run Project** button (or press `F6`).

### Step 4: Running from Command Line
You can also compile and package using pure Maven:
```bash
mvn clean package
java -jar target/drrow-clothing-pos-1.0.0.jar
```

---

## 🔐 Default Development Credentials

> **Security Notice**: These credentials are provided for development and testing. On first login with `admin`, the application enforces an immediate password change dialog.

| Username | Default Password | Role | Permissions Scope |
| :--- | :--- | :--- | :--- |
| `admin` | `Admin@123` | **ADMIN** | Full administrative privileges across all modules |
| `manager` | `Manager@123` | **MANAGER** | Catalog, inventory, sales, returns, reports, settings |
| `cashier` | `Cashier@123` | **CASHIER** | POS terminal, scanning, sales checkout, reprinting bills |
| `stock` | `Stock@123` | **STOCK STAFF** | Inward receiving, inventory adjustments, barcode labels |

---

## 🏷️ Hardware Integration

### 1. USB HID Barcode Scanner
- Standard USB barcode scanners function as keyboard input devices (Human Interface Device).
- When a tag is scanned, it inputs the barcode alphanumeric characters followed by a carriage return (`Enter` key).
- In the D’RROW POS terminal:
  - The barcode input field is highlighted in gold and automatically grabs focus on launch and after every transaction.
  - Simply pull the scanner trigger pointing at any clothing tag.
  - The item is instantly added to the cart, the quantity is set to 1 (or incremented if already in the cart), the input box clears itself, and the cursor immediately returns for the next scan.

### 2. Receipt and Tag Printers
- **80mm Thermal Receipt Printers**: Standard POS printers (Epson, Star Micronics, Xprinter, Rongta). In **Store Settings**, configure receipt widths. Printing uses Java Print Service (`java.awt.print.Printable`).
- **Barcode Tag Printers**: Supports thermal roll label printers (standard 50x30mm apparel tags) as well as standard office laser/inkjet printers using A4 21-label sheets.

---

## 🧪 Automated End-to-End Workflow Verification

The project includes an automated test harness (`com.drrow.pos.test.WorkflowIntegrationTest`) that executes Requirement #39:
1. Authenticates as Admin.
2. Creates the Category **T-Shirts**.
3. Creates the Product **Classic T-Shirt** (`DRTS1001`, Rs. 2,500).
4. Generates Variant **Black / M** with unique barcode (`DRTS000002`) and 20 units initial stock.
5. Renders and verifies high-DPI clothing tag preview with D'RROW branding.
6. Simulates USB barcode scanner scan of `DRTS000002`.
7. Populates cart with quantity 1.
8. Completes Cash Sale (Customer pays Rs. 3,000, Grand Total Rs. 2,500, Change Rs. 500).
9. Verifies stock level decrements from **20 to 19**.
10. Retrieves invoice from archive and generates reprint bill preview.
11. Processes a return of 1 item with reason "Customer requested size exchange" and restores stock.
12. Verifies stock level restores from **19 back to 20**.
13. Generates Daily Sales and Returns reports, confirming all financial summaries balance.

To run this test:
```bash
mvn test
```
Or execute `WorkflowIntegrationTest.main()`.
