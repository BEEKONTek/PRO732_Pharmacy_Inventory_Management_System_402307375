Pharmacy Inventory Management System (PIMS) - HealthFirst
=========================================================
A Java Swing + JDBC desktop application for pharmacy inventory,
point-of-sale, and business reporting.

Structure
---------
  src/          Java source files
  lib/          MariaDB JDBC driver
  database.sql  Schema + sample data
  screenshots/  UI screenshots for submission

Setup
-----
1. Import the database:
     mysql -u root -p < database.sql
2. Set environment variables before running:
     export PIMS_DB_URL=jdbc:mariadb://localhost:3306/pims
     export PIMS_DB_USER=pims_app
     export PIMS_DB_PASSWORD='your_password'
3. Build:
     javac -d build -cp "lib/*" src/*.java
4. Run:
     java -cp "build:lib/*" LoginFrame

Default logins:
  Admin   : admin / admin123
  Cashier : cashier / cash123
