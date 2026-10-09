# D’RROW Clothing Store POS & Inventory System
## The Beginner-Friendly A–Z User & Deployment Manual

Welcome to the **D’RROW Clothing Store POS System**! This guide is written in plain, friendly language for store owners, cashiers, inventory managers, and anyone deploying a Java desktop application for the very first time.

---

### Table of Contents (A to Z)

- [A — Application Installation & System Setup](#a--application-installation--system-setup)
- [B — Database Setup (MySQL / MariaDB)](#b--database-setup-mysql--mariadb)
- [C — Configuration (Connecting Application to Database)](#c--configuration-connecting-application-to-database)
- [D — Dashboard & Executive Metrics](#d--dashboard--executive-metrics)
- [E — Employees & User Accounts](#e--employees--user-accounts)
- [F — Fast Product Creation & Cataloguing](#f--fast-product-creation--cataloguing)
- [G — Barcode Generation (Code 128)](#g--barcode-generation-code-128)
- [H — Hanging Clothing Tag & Label Printing](#h--hanging-clothing-tag--label-printing)
- [I — Inventory Balances & Real-Time Tracking](#i--inventory-balances--real-time-tracking)
- [J — Jump into POS (Point of Sale Interface)](#j--jump-into-pos-point-of-sale-interface)
- [K — Keyboard & Handheld Barcode Scanner Setup](#k--keyboard--handheld-barcode-scanner-setup)
- [L — Login & Security Authentication](#l--login--security-authentication)
- [M — Making Sales & Processing Transactions](#m--making-sales--processing-transactions)
- [N — New Invoice Generation & Formats](#n--new-invoice-generation--formats)
- [O — Orders & Sales Archive History](#o--orders--sales-archive-history)
- [P — Printing Bills & Thermal Receipts](#p--printing-bills--thermal-receipts)
- [Q — Quantity Modifications & Cart Adjustments](#q--quantity-modifications--cart-adjustments)
- [R — Returns, Exchanges & Restocking](#r--returns-exchanges--restocking)
- [S — Stock Adjustments (Damage, Lost, Count Corrections)](#s--stock-adjustments-damage-lost-count-corrections)
- [T — Tax, Discounts & Financial Calculations](#t--tax-discounts--financial-calculations)
- [U — User Roles & Security Permissions](#u--user-roles--security-permissions)
- [V — Variants (Sizes & Colours Apparel Matrix)](#v--variants-sizes--colours-apparel-matrix)
- [W — Warehouse Inward Stock Receiving](#w--warehouse-inward-stock-receiving)
- [X — XML, CSV & Database Backup](#x--xml-csv--database-backup)
- [Y — Yearly, Monthly & Daily Reports](#y--yearly-monthly--daily-reports)
- [Z — Zero-Stock & Low-Stock Alerts](#z--zero-stock--low-stock-alerts)

---

### A — Application Installation & System Setup

If you have never set up a Java application before, follow these simple steps:

1. **Install Java (JDK 17 or 21 LTS)**:
   - Download the free JDK from [Adoptium Temurin](https://adoptium.net/) (choose version 17 or 21 LTS for Windows, macOS, or Linux).
   - Run the installer. On Windows, ensure you check the box that says: **"Add to PATH"**.
   - Verify in your terminal/command prompt:
     ```bash
     java -version
     ```
2. **Install Apache NetBeans**:
   - Download Apache NetBeans from [netbeans.apache.org](https://netbeans.apache.org/).
   - Choose the installer for your operating system.
   - Run the setup wizard (default options are recommended).
3. **Locate your D’RROW POS Project Folder**:
   - Extract the project ZIP file to a convenient folder, for example `C:\DRROW\drrow-pos` or `/home/user/drrow-pos`.
   - Ensure you see the `pom.xml` file inside that folder.
4. **Open in NetBeans**:
   - Open NetBeans.
   - Click **File** -> **Open Project...** (`Ctrl + Shift + O`).
   - Navigate to your project folder. NetBeans will display a small red/yellow **Maven (Ma)** icon next to `drrow-pos`.
   - Click **Open Project**.

---

### B — Database Setup (MySQL / MariaDB)

1. **Install MySQL or MariaDB**:
   - Download and install **MySQL Community Server** (8.0 or 8.3+) or **MariaDB Server** (10.6+).
   - During installation, set your `root` password (e.g., `root123`). Keep note of this password!
2. **Create the Database and Import SQL**:
   - Open **MySQL Command Line Client** or **MySQL Workbench** or **HeidiSQL**.
   - Log in with your password.
   - Open and run the 3 SQL files in this exact order:
     ```sql
     -- 1. Create tables, primary keys, and foreign keys
     source C:/DRROW/drrow-pos/database/schema.sql;

     -- 2. Insert standard roles, permissions, settings, and first admin account
     source C:/DRROW/drrow-pos/database/seed.sql;

     -- 3. (Optional but recommended) Insert realistic D'RROW clothing products, sizes, colours, barcodes, and inventory
     source C:/DRROW/drrow-pos/database/sample_data.sql;
     ```

---

### C — Configuration (Connecting Application to Database)

Open the configuration file located at:
`src/main/resources/config/db.properties`
*(Or create a copy called `db.properties` directly in your main project folder)*:

```properties
db.type=mysql
db.host=localhost
db.port=3306
db.name=drrow_pos
db.user=root
db.password=root123
```
- Change `db.password` to match the password you set during MySQL installation.
- Save the file. That’s it! The system will now connect automatically.

---

### D — Dashboard & Executive Metrics

When an Administrator or Manager signs in, the **Executive Dashboard** loads automatically:
- **Today's Gross Sales**: Shows total cash and card intake for the current day in real-time.
- **Today's Transactions**: Number of customer invoices finalized today.
- **Estimated Gross Margin**: Real-time gross profit computed from `(Selling Price - Purchase Cost) * Quantity`.
- **Low Stock Alerts**: Number of clothing variants with stock below the reorder threshold (default: 5 units).
- **Total Inventory Units**: Sum of all physical garments currently in stock.
- **Recent Sales Table**: Live stream of the most recent checkout invoices with quick status badges.
- **Reorder Alerts Table**: Direct list of clothing items needing urgent restocking.

---

### E — Employees & User Accounts

*(Admin Role Only)*
1. Click **User Accounts** in the left sidebar.
2. Click **+ Add Employee User**.
3. Enter their username (e.g. `kasun`), full name, email, and telephone.
4. Select their role:
   - **ADMIN**: Complete system control.
   - **MANAGER**: Full access to sales, reports, stock, and catalog.
   - **CASHIER**: Dedicated to fast POS scanning, billing, and reprinting.
   - **STOCK STAFF**: Dedicated to receiving inward stock, adjusting inventory, and printing tag barcodes.
5. Provide a temporary password (minimum 6 characters).
6. The employee will be prompted to choose their own personal secret password on their first login!

---

### F — Fast Product Creation & Cataloguing

1. Navigate to **Products & SKU** from the sidebar.
2. Click **+ New Clothing Product**.
3. Enter:
   - **Item Code / SKU Prefix**: e.g., `DRTS0010`.
   - **Product Name**: e.g., `Organic Cotton Henley Shirt`.
   - **Category**: Select `T-Shirts`, `Shirts`, `Jeans`, `Dresses`, etc.
   - **Brand**: Select `D’RROW Signature`, `D’RROW Essentials`, etc.
   - **Cost / Purchase Price**: What the store paid the manufacturer (e.g., `Rs. 1,800.00`).
   - **Selling Retail Price**: Price on the clothing tag (e.g., `Rs. 4,200.00`).
4. In the **Variant Generator Matrix**, check the sizes (e.g. `S`, `M`, `L`, `XL`) and colours (e.g. `Black`, `Navy Blue`) you have received.
5. Enter the **Initial Stock per Variant** (e.g., `15`).
6. Click **Save & Generate Barcodes**.
7. The system automatically creates every individual variant, generates its unique Code 128 barcode, and puts the 15 units into your live stock!

---

### G — Barcode Generation (Code 128)

- Every single sellable variant receives its own globally unique barcode (e.g. `DRTS000001`, `DRTS000002`).
- The system prevents duplicate barcodes:
  - If a user tries to manually assign an existing barcode, the database unique constraint blocks it with a friendly message: *"Barcode already exists"*.
  - When creating variants, the system checks existing sequences and generates clean, sequential barcodes automatically.
- Format: **Code 128 (Subset B)** — standard high-density alphanumeric barcode supported by 100% of retail handheld scanners.

---

### H — Hanging Clothing Tag & Label Printing

1. Click **Barcode Tags** from the left sidebar.
2. Search for the garment by name, barcode, or SKU in the search bar.
3. Click the variant row in the table.
4. An immediate, high-definition **Live Tag Preview** appears on the right side:
   - **D’RROW CLOTHING** luxury header banner.
   - Product Name (e.g. `Classic Polo Shirt`).
   - SKU line: `SKU: DRTS0001-BLK-M`.
   - Attributes: `Size: M | Colour: Black`.
   - Price tag: `Rs. 4,990.00`.
   - Crisp Code 128 barcode bars and readable numbers.
5. Choose your printing format:
   - **Roll Label (50x30mm)**: Prints 1 label per tag on standard sticky label rolls or clothing tag printers.
   - **A4 Sheet (21 Tags/Sheet)**: Prints 3 columns by 7 rows on a standard A4 sticker sheet.
6. Enter the **Number of Tags** you want to print.
7. Click **Print Tags**.

---

### I — Inventory Balances & Real-Time Tracking

- Every sale **deducts** stock immediately.
- Every return **restores** stock immediately.
- Every supplier delivery **increases** stock immediately.
- The Inventory screen shows:
  - **Current Stock**: Units physically on shelves and in warehouse.
  - **Reorder Level**: Minimum safe threshold.
  - **Status**: `NORMAL`, `LOW STOCK` (orange badge), or `OUT OF STOCK` (red badge).
- The system protects against negative stock by default.

---

### J — Jump into POS (Point of Sale Interface)

The POS terminal is laid out for maximum cashier speed:
- **Left Panel**:
  - Gold barcode scan box with auto-focus.
  - Fast item search field.
  - Category dropdown filter.
  - Interactive product catalog table (double-click any item to add to cart).
- **Right Panel**:
  - Live shopping cart table showing Item, Size, Colour, Quantity, Price, Discount, Line Total.
  - Cart buttons: `+ Qty`, `- Qty`, `Remove Item`, `Clear Cart`, `Apply Discount`, `Hold Sale`, `Resume Sale`.
  - Financial summary: Subtotal, Discount, Tax, Grand Total.
  - Payment inputs: Payment Method, Paid Amount, Change Given.
  - Action buttons: `Complete Sale & Print`, `Print Last Bill`, `New Sale`.

---

### K — Keyboard & Handheld Barcode Scanner Setup

1. **Plugging in the Scanner**:
   - Plug any standard USB handheld barcode scanner into a USB port on the POS computer.
   - No special drivers are needed — the operating system recognizes it as a standard USB Keyboard (HID).
2. **Scanning Items**:
   - The D’RROW POS barcode field is automatically focused.
   - Aim the laser/LED scanner at the clothing tag barcode and pull the trigger.
   - The scanner types the barcode and presses `Enter`.
   - The item immediately pops into the shopping cart with quantity `1`.
   - The barcode field clears automatically and keeps the cursor focused, ready for the next garment!
3. **If a Tag is Damaged or Missing**:
   - The cashier can simply type the barcode digits or the SKU into the search box and double-click the item.

---

### L — Login & Security Authentication

1. Start the application. The **D’RROW Login Terminal** appears.
2. Enter your **Username** and **Password**.
3. Toggle **Show password** if you need to verify your input.
4. Press `Enter` or click **Sign In**.
5. The system verifies your credentials using **BCrypt cryptographic hashing**. Plain text passwords are never stored anywhere in the database.
6. If the user account is flagged with `require_password_change`, the system prompts them to set a new password before granting access.

---

### M — Making Sales & Processing Transactions

1. Scan all garments selected by the customer.
2. Adjust quantities if the customer is buying multiples of the same item.
3. If an in-store promotion applies, click **Apply Discount** and enter the discount amount in Rupees.
4. Select the **Payment Method**:
   - **CASH**: Enter the cash received in **Paid Amount** (e.g., customer hands you `Rs. 5,000.00` for a `Rs. 4,990.00` bill). The system instantly calculates and displays **Change Given: Rs. 10.00** in bright green!
   - **CARD / BANK TRANSFER**: Paid amount automatically matches Grand Total.
5. Click **Complete Sale & Print**.
6. In a split second:
   - The transaction is atomically recorded in the database.
   - Inventory quantities are reduced.
   - Stock movement logs are recorded.
   - The thermal receipt preview appears on screen and can be printed immediately.
   - The cart is cleared, ready for the next customer!

---

### N — New Invoice Generation & Formats

- Every invoice is assigned a unique invoice code (e.g. `DRR-20261008-0001`).
- The pattern can be configured in **Store Settings**:
  - `PREFIX-YYYYMMDD-SEQ` (e.g., `DRR-20261008-0001`)
  - `PREFIX-SEQ` (e.g., `DRR-000001`)
- Duplicate invoice numbers are strictly prohibited by unique database indexes.

---

### O — Orders & Sales Archive History

1. Click **Invoices Archive** in the left sidebar.
2. A list of all historical sales appears with dates, cashier names, customer names, subtotals, and grand totals.
3. Click any invoice to inspect the exact garments purchased, including size, colour, and individual line pricing.
4. Click **Reprint Bill** at any time to re-generate the official receipt.
5. Click **Return / Exchange** to initiate a refund or size replacement.

---

### P — Printing Bills & Thermal Receipts

The D’RROW receipt is designed to professional retail standards:
- **Store Name & Tagline**: *D’RROW Clothing Store - Modern Elegance & Premium Fashion*.
- **Address & Telephone**.
- **Invoice Number, Date, Time, and Cashier Name**.
- **Customer details** (if a loyalty customer was selected).
- **Line Items**: Product name, quantity, unit price, size/colour subtitle, and total.
- **Financial Breakdown**: Subtotal, discount deducted, taxes, and Grand Total.
- **Payment Method, Paid Amount, and Change Given**.
- **Scannable Invoice Barcode**: Can be scanned by the scanner during returns!
- **Store Return Policy & Thank You Message**.
- Can be printed directly to any 80mm ESC/POS thermal printer or standard desktop printer.

---

### Q — Quantity Modifications & Cart Adjustments

- In the POS cart:
  - Select an item and click **+ Qty** to increase by 1.
  - Click **- Qty** to decrease by 1.
  - Click **Remove Item** to delete that line from the cart.
  - Click **Clear Cart** to reset the whole sale.

---

### R — Returns, Exchanges & Restocking

1. Open **Invoices Archive** or click **Return / Exchange**.
2. Select the customer's invoice.
3. In the Return Dialog:
   - The table lists all items on the original bill.
   - Enter the **Return Qty** (the system will not allow returning more than the quantity originally bought).
   - The total refund amount updates automatically.
   - Choose **Restock Action**:
     - `RESTOCKED`: Puts the item back into inventory for resale.
     - `DAMAGED_DISCARD`: Records the refund but marks the damaged item as written off.
   - Choose **Return Action**: `REFUND` (cash return) or `EXCHANGE` (store credit / swap).
   - Enter the **Reason** (e.g., *"Customer wanted size L instead of M"*).
4. Click **Process Return & Restore Stock**.
5. The stock level is restored, a return record is logged, and the original sale remains untouched for audit integrity.

---

### S — Stock Adjustments (Damage, Lost, Count Corrections)

1. Open **Inventory / Stock** and stay on the **Current Inventory** tab.
2. Select the item variant you want to adjust.
3. Click **Stock Adjustment**.
4. Select the Adjustment Type:
   - `ADJUSTMENT_DAMAGE`: Customer tried on and tore zipper, water damage, etc. (negative change).
   - `ADJUSTMENT_LOST`: Discrepancy during cycle count (negative change).
   - `ADJUSTMENT_CORRECTION`: Data entry fix after physical stock audit.
   - `ADJUSTMENT_FOUND`: Garment discovered in fitting room / backroom (positive change).
   - `ADJUSTMENT_MANUAL`: Managerial override.
5. Enter the quantity change (e.g. `-2` or `+5`).
6. Enter a mandatory reason.
7. Click **Save Adjustment**.

---

### T — Tax, Discounts & Financial Calculations

- In **Store Settings**:
  - Set `Tax Percentage` (e.g., `0.00` for no VAT, or `18.00` for 18% VAT).
  - Set `Currency Symbol` (defaults to `Rs.`, or customize to `$`, `€`, `£`, `AED`).
- All calculations use standard half-up 2-decimal arithmetic, ensuring receipts and accounting reports balance down to the exact cent/centime.

---

### U — User Roles & Security Permissions

The system strictly enforces role boundaries:
- **Cashiers** cannot delete products, alter purchase/cost prices, edit other staff accounts, or modify store settings.
- **Stock Staff** cannot delete sales or access financial profit reports.
- **Managers** have full operational access to stock, catalog, POS, returns, and reports.
- **Admins** have unrestricted access including user management, security audit logs, and database backup tools.

---

### V — Variants (Sizes & Colours Apparel Matrix)

Clothing retail requires variants:
- One shirt model (*"Classic Polo"*) has multiple sizes (`S`, `M`, `L`, `XL`) and colours (`Black`, `White`, `Navy Blue`).
- In D’RROW POS, you do not need to create 12 separate products manually!
- Simply create 1 product and select the sizes and colours you carry.
- The system creates the 12 variants in a fraction of a second, with distinct SKUs and unique barcodes for each one.

---

### W — Warehouse Inward Stock Receiving

When a delivery arrives from a garment manufacturer (e.g., Apex Garments):
1. Go to **Inventory / Stock** -> **Supplier Stock Receiving** tab.
2. Click **+ Receive Inward Stock**.
3. Select the **Supplier**.
4. Enter the Purchase Order or Delivery Note reference number.
5. Select the apparel variant and enter the quantity delivered and unit cost price.
6. Click **Confirm Receipt & Add Stock**.
7. The items are added to stock, and an inward stock movement audit log is registered.

---

### X — XML, CSV & Database Backup

- **CSV Export**: Every analytical report (Daily sales, Monthly revenue, Cashier performance, Stock valuation) can be exported to standard CSV with one click for easy opening in Microsoft Excel or Google Sheets.
- **Database Backup**:
  - In **Store Settings** -> **Database Backup & Disaster Recovery**, click **Create Immediate SQL Backup**.
  - Choose where to save your `.sql` file.

---

### Y — Yearly, Monthly & Daily Reports

1. Click **Reports & Profit** in the left sidebar.
2. Select your report:
   - **Daily Sales Report**: Hour-by-hour breakdown of today's sales.
   - **Monthly Sales Report**: Day-by-day revenue, discount, and unit summaries.
   - **Sales by Cashier**: Compare performance and invoice totals across employees.
   - **Product Sales Report**: Best-selling garments ranked by quantity sold and total revenue.
   - **Category Sales Report**: T-Shirts vs. Shirts vs. Jeans revenue comparisons.
   - **Stock Valuation Report**: Total cost value vs. total retail value of inventory.
   - **Profit Estimate Report**: Estimated gross profit margin per invoice.
   - **Returns & Exchanges Report**: Full log of customer returns, reasons, and refunds.
3. Click **Generate Report**, then preview on screen, click **Print Table**, or **Export CSV**.

---

### Z — Zero-Stock & Low-Stock Alerts

- The POS and Dashboard continuously monitor inventory balances.
- On the Dashboard:
  - The **Low Stock Alerts** card shows how many garments are at or below 5 units.
  - The **Urgent Reorder Alerts** table lists the exact garments, sizes, and colours that cashiers are running out of.
- Cashiers receive a warning dialog if they attempt to sell an out-of-stock item (unless negative stock is explicitly permitted by management).
