# D’RROW Clothing Store POS - Troubleshooting & Diagnostics Guide

This document lists common issues encountered during setup, database connection, barcode scanning, and printing, along with quick solutions.

---

## 1. Database Connection Issues

### Symptom: `CommunicationsException: Communications link failure`
- **Cause**: MySQL / MariaDB server service is not started, or is listening on a different port.
- **Solution**:
  1. On Windows: Open `Services` (`Win + R` -> `services.msc`), find **MySQL80** or **MariaDB**, right-click and select **Start**.
  2. On Linux/macOS:
     ```bash
     sudo systemctl status mysql
     sudo systemctl start mysql
     ```
  3. Verify that `db.port=3306` in `config/db.properties`.

### Symptom: `Access denied for user 'root'@'localhost'`
- **Cause**: Incorrect database password in `config/db.properties`.
- **Solution**:
  1. Check the password set during MySQL installation.
  2. Update `config/db.properties`:
     ```properties
     db.user=root
     db.password=YourActualPassword
     ```
  3. Or test logging into MySQL directly from terminal:
     ```bash
     mysql -u root -p
     ```

### Symptom: `Public Key Retrieval is not allowed`
- **Cause**: MySQL 8.x uses `caching_sha2_password` authentication by default.
- **Solution**:
  Ensure `db.allowPublicKeyRetrieval=true` is set in `config/db.properties`. This parameter is already enabled by default in D’RROW POS configuration.

### Symptom: `Unknown database 'drrow_pos'`
- **Cause**: The schema creation script has not been executed yet.
- **Solution**:
  Log into MySQL and run:
  ```sql
  CREATE DATABASE drrow_pos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```
  Then import `database/schema.sql`, `database/seed.sql`, and `database/sample_data.sql`.

---

## 2. Barcode & USB Scanner Issues

### Symptom: Scanner beeps but nothing happens in POS
- **Cause**: The barcode text input box does not have keyboard focus.
- **Solution**:
  1. Click inside the gold-bordered **SCAN BARCODE** box at the top left of the POS screen.
  2. Scan the barcode.
  3. Verify that the scanner is set to send a **Carriage Return (`Enter`)** suffix after each scan (this is the factory default for 99% of barcode scanners; consult the scanner manual to scan the "Add Enter Suffix" configuration barcode if needed).

### Symptom: Error "Product not found for barcode: DRTSxxxxxx"
- **Cause**: The scanned barcode does not exist in the database or has been deactivated.
- **Solution**:
  1. Go to **Products & Variants** or **Barcode Tags**.
  2. Search for the product to confirm its barcode number.
  3. If missing, create the product variant or generate a new barcode tag.

### Symptom: Duplicate Barcode Error on Product Creation
- **Cause**: Attempting to reuse an existing barcode number.
- **Solution**:
  Leave the barcode field empty or use the automatic barcode generator button to assign the next available unique sequential code (`DRTS0000xx`).

---

## 3. Stock & Checkout Errors

### Symptom: `Insufficient stock for ... Available: 0, Requested: 1`
- **Cause**: The store has run out of physical stock for that specific size/colour variant, and the system policy prevents negative inventory.
- **Solution**:
  1. Check if inward stock has arrived from the supplier: go to **Inventory / Stock** -> **Supplier Stock Receiving** and process the delivery batch.
  2. If the physical item is present on the shelf, perform a **Stock Adjustment** (reason: `ADJUSTMENT_FOUND` or `ADJUSTMENT_CORRECTION`) to reflect physical reality.
  3. If management allows billing negative stock during stock audits, enable `pos.allow_negative_stock=true` in **Store Settings**.

### Symptom: `Please enter a valid payment amount` or `Insufficient payment amount`
- **Cause**: For cash transactions, the amount entered in the "Paid Amount" box is less than the Grand Total.
- **Solution**:
  Enter the actual cash received from the customer (e.g., if the bill is Rs. 2,490.00 and customer gives Rs. 2,500.00, enter `2500`). The system will compute the Rs. 10.00 change automatically.

---

## 4. Printing & Receipt Issues

### Symptom: `No print service found` or Print Dialog does not appear
- **Cause**: No default printer is installed or configured on the operating system.
- **Solution**:
  1. On Windows: Open `Printers & Scanners` and ensure at least one printer (or "Microsoft Print to PDF") is set up and active.
  2. On Linux: Ensure CUPS is running (`sudo systemctl start cups`).
  3. Use the on-screen **Receipt Preview** or **Tag Preview** to view high-resolution visual proofs.

### Symptom: Thermal receipt text is cut off or too wide
- **Cause**: Printer paper width mismatch.
- **Solution**:
  In `config/app.properties`, adjust `app.receipt.width.mm=80` or `app.receipt.width.mm=58` to match your thermal printer roll size.

---

## 5. NetBeans & Maven Build Issues

### Symptom: NetBeans shows red exclamation mark on project
- **Cause**: Maven has not yet downloaded dependencies locally.
- **Solution**:
  1. Right-click project name `drrow-clothing-pos` in NetBeans.
  2. Select **Reload Project**.
  3. Select **Clean and Build**.
  4. Ensure your computer is connected to the internet during the very first build so Maven can fetch libraries declared in `pom.xml`.

### Symptom: `Cannot find Main class com.drrow.pos.Main`
- **Cause**: Project run properties need re-indexing.
- **Solution**:
  1. Right-click project -> **Properties** -> **Run**.
  2. In **Main Class**, click **Browse...** and choose `com.drrow.pos.Main`.
  3. Click **OK** and press `F6` to run.
