CREATE DATABASE IF NOT EXISTS pims;
USE pims;

DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS medicines;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin','Cashier') NOT NULL,
    full_name VARCHAR(100)
);

CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT
);

CREATE TABLE medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(100),
    medicine_type VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    expiry_date DATE,
    supplier_id INT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL
);

CREATE TABLE sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE sale_items (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);

-- Sample data
INSERT INTO users (username, password, role, full_name) VALUES
('admin', 'admin123', 'Admin', 'System Administrator'),
('cashier', 'cash123', 'Cashier', 'John Cashier');

INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('PharmaDist', 'Alice Smith', '011-123-4567', 'alice@pharmadist.com', '123 Main Rd, Johannesburg'),
('MedSupply', 'Bob Jones', '021-987-6543', 'bob@medsupply.com', '45 Long St, Cape Town');

INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Paracetamol 500mg', 'GenericPharma', 'Tablet', 25.50, 120, 20, DATE_ADD(CURDATE(), INTERVAL 8 MONTH), 1),
('Amoxicillin 250mg', 'BioMed', 'Capsule', 85.00, 60, 15, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 2),
('Cough Syrup 100ml', 'HealWell', 'Syrup', 45.75, 40, 10, DATE_ADD(CURDATE(), INTERVAL 15 DAY), 1),
('Insulin Injection', 'LifeCare', 'Injection', 250.00, 25, 5, DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 2),
('Hydrocortisone Cream', 'DermaCare', 'Cream', 65.30, 8, 10, DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 1);