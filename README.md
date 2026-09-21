# HealthFirst PIMS — Pharmacy Inventory Management System

A desktop application for managing a pharmacy: stock, suppliers, users, sales,
billing, and reports.

---

## Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Administrator | `admin` | `admin123` |
| Cashier | `cashier` | `cash123` |

> The sign-in screen has **Admin** and **Cashier** buttons that fill these
> credentials automatically for you.

---

## Starting the Application

**On Windows**

Double-click:

```
402307375_BotlhaleMaruma_pims.exe
```

**On Linux / macOS**

Open a terminal in the application folder and run:

```bash
java -jar pims.jar
```

The sign-in window opens. You do not need to configure anything else.

---

## Signing In

1. Enter your **username** and **password**.
2. Tick **Show password** if you want to see what you typed.
3. Tick **Remember me** to have your username filled in next time.
4. Press **Enter** or click **Sign in**.

If the credentials are wrong, an inline message appears below the form telling
you what to fix. You will be routed automatically to the correct dashboard
based on your role.

---

## For Cashiers — Point of Sale

You will see a two-panel screen: **Available medicines** on the left, and
**Current cart** on the right.

### Finding a medicine

- Type a medicine or company name in the **Search** box.
- Press **Enter** or click **Search** to filter the list.
- Click **Quick stock check** to see price, stock, and expiry of a medicine
  without adding it to the cart.

### Adding items to the cart

- **Double-click** a medicine in the left table, **or**
- Select it and click **Add to cart**.

Adding the same medicine twice increases its quantity. You cannot add more
than the available stock.

### Completing a sale

1. Check the **Total** shown at the bottom right.
2. Click **Checkout**.
3. A receipt window opens showing every item, its quantity, unit price,
   subtotal, and the final total.
4. Click **Save receipt** to save it as a text file, or **Close** to dismiss.
5. Stock levels update automatically. If any item runs out during checkout,
   the entire sale is cancelled and no stock is deducted.

### Clearing the cart

Click **Clear cart** to remove all items before checkout. You will be asked
to confirm.

> Cashiers cannot add, edit, or delete medicines. Only Administrators can.

---

## For Administrators — Management Console

After signing in as `admin`, you reach a window with four tabs.

### Medicines tab

- Fill in the form on the right: **Name**, **Company**, **Type**, **Price**,
  **Quantity**, **Reorder level**, **Expiry date** (format `YYYY-MM-DD`),
  and **Supplier**.
- Click **Add** to create a new medicine.
- Click any row in the table to load it into the form, edit the fields,
  then click **Update** to save changes.
- Select a row and click **Delete** to remove it. You will be asked to confirm.
- **Clear** resets the form. **Refresh** reloads the table from the database.

### Suppliers tab

Same pattern as the medicines tab, for supplier details: **Name**,
**Contact person**, **Phone**, **Email**, and **Address**.

### Users tab

- To create a cashier account: enter a **Username**, **Password**, and
  **Full name**, then click **Add Cashier**.
- To remove one: select the row and click **Delete**.
- Administrator accounts cannot be deleted from this screen.

### Reports tab

Click any of the four buttons to load a report in the table below. Each
report pulls live data every time you click it.

| Report | What it shows |
|--------|---------------|
| **Sales report** | Every sale, with date, cashier, and total |
| **Item-wise report** | Total quantity sold and revenue per medicine |
| **Low stock report** | Medicines at or below their reorder level |
| **Expiry report** | Medicines expiring within the next 30 days |

---

## What Each Role Can Do

| Action | Cashier | Administrator |
|--------|:-------:|:-------------:|
| Process a sale | ✅ | ✅ |
| Print / save a receipt | ✅ | ✅ |
| Check price and availability | ✅ | ✅ |
| Add, edit, or delete medicines | ❌ | ✅ |
| Add, edit, or delete suppliers | ❌ | ✅ |
| Create or delete cashier accounts | ❌ | ✅ |
| View reports | ❌ | ✅ |

---

## Common Messages

| Message | Meaning | What to do |
|---------|---------|------------|
| "Please enter both username and password." | A field is empty | Fill in both fields |
| "Invalid username or password." | Wrong credentials | Check spelling; use the Admin/Cashier demo buttons |
| "Out of stock." | Medicine has zero quantity | Choose a different medicine |
| "Only N in stock." | You tried to add more than available | Reduce the quantity |
| "Cart is empty." | Checkout with no items | Add at least one item first |
| "Checkout failed: ..." | Database rejected the sale | Contact your administrator |

---

## Folder Contents

```
402307375_BotlhaleMaruma_pims.exe   The Windows application
pims.jar                            The cross-platform JAR
lib/                                MariaDB JDBC driver
screenshots/                        Reference screenshots
README.md                           This manual
```

---

## Getting Help

If the application fails to start:

1. Ensure the computer has **Java 17** or newer installed
   ([adoptium.net](https://adoptium.net)).
2. Ensure the **MariaDB** database service is running.
3. Ensure the `pims` database has been created from the included schema
   (your administrator will have done this).
4. Note the exact error message and pass it to your system administrator.

---

*HealthFirst PIMS — PRO732 Project*
*Richfield Graduate Institute of Technology*
