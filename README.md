# Home Service Booking and Management System

A Java Swing desktop application developed for managing household services connecting customers, trade service providers, and administrators.

### Project Attribution
- **Abhinav Sajeev** (25CS009)
- **Alan Jojo** (25CS032)
- **Allan Peter** (25CS038)
- **Sebin Raju** (25CS187)

---

## 🛠️ Technology Stack
- **Programming Language:** Java 25 (Java SE)
- **GUI Framework:** Java Swing with FlatLaf modern UI theme
- **Database:** Supabase Cloud PostgreSQL (with hybrid support for MySQL 8.0)
- **Database Connectivity:** JDBC (`postgresql-42.7.3.jar` & `mysql-connector-j-8.3.0.jar`)
- **Design Pattern:** Model-View-Controller (MVC) & Data Access Object (DAO) Pattern

---

## 🌟 Key Features Completed

### 1. Customer Role
- **Account Registration & Authentication:** Register new customer profile with contact information and address.
- **Browse & Filter Services:** Browse 8 standard categories (Plumbing, Electrical, Cleaning, Carpentry, Painting, Appliance Repair, AC Servicing, Household Maintenance).
- **Service Provider Discovery:** View ratings, reviews count, years of experience, hourly pricing, and bios.
- **Service Scheduling & Booking:** Choose service date, time slot, provide service location and detailed problem description.
- **Booking Tracking:** Live status badge tracking (`PENDING`, `ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `REJECTED`).
- **Cancellation:** Cancel pending bookings with 1 click.
- **Ratings & Reviews:** Submit 1–5 star ratings and written comments for completed services.
- **Customer Profile:** View/edit personal details and password.

### 2. Service Provider Role
- **Profile & Availability Management:** Set hourly rates, update professional bio, update skills/category, and toggle active availability for customer bookings.
- **Incoming Requests:** Review pending requests with client location, problem description, and fee. Accept or reject requests.
- **Active Jobs & Workflow:** Start accepted jobs (`IN_PROGRESS`) and mark services as `COMPLETED`.
- **Ratings & Feedback:** View customer reviews and overall calculated average star rating.
- **Earnings & Metrics:** View live counts of pending requests, active jobs, completed services, and total earnings.

### 3. Administrator Role
- **System Statistics & Analytics:** Live analytics dashboard tracking total bookings, customers, providers, categories, gross revenue, and status breakdown.
- **User Management:** Audit all customers and service providers, activate/deactivate accounts, or permanently delete users.
- **Service Categories & Pricing:** Full CRUD on service categories (add new services, edit base pricing and description, delete).
- **Master Bookings Management:** Search and filter all system bookings across all users with administrative override and cancellation.
- **Review Moderation:** View all customer reviews and moderate/delete inappropriate reviews with automatic recalculation of provider ratings.

---

## 🔑 Quick Demo Credentials

| Role / Trade | Name | Sample Email | Password |
|---|---|---|---|
| **Customer** | Abhinav Customer | `customer@example.com` | `password123` |
| **Administrator** | System Admin | `admin@homeservice.com` | `admin123` |
| **Provider (Plumbing)** | Rohit Verma | `rohit.plumber@service.com` | `provider123` |
| **Provider (Electrical)** | Arun Kumar | `arun.electrician@service.com` | `provider123` |
| **Provider (Cleaning)** | Priya Sharma | `priya.cleaner@service.com` | `provider123` |
| **Provider (Carpentry)** | Suresh Nair | `suresh.carpenter@service.com` | `provider123` |
| **Provider (Painting)** | Deepak Mishra | `deepak.painter@service.com` | `provider123` |
| **Provider (Appliance)** | Manoj Patil | `manoj.repair@service.com` | `provider123` |
| **Provider (AC Servicing)** | Vikram Singh | `vikram.ac@service.com` | `provider123` |
| **Provider (Handyman)** | Rajesh Gupta | `rajesh.handyman@service.com` | `provider123` |

*(Note: The login dialog and top taskbar feature a "Quick Demo Login" dropdown to populate any customer, admin, or provider with one click!)*

---

## 🚀 How to Run & Build

### 1. Build Standalone Windows EXE (To Share with Other Devices)
Double-click `build_exe.bat` in the project root.
* Automatically creates a self-contained Windows application in `dist\HomeService\HomeService.exe`.
* Bundles an embedded private Java runtime so **other devices DO NOT need Java installed**!
* Generates **`dist\HomeService-Windows.zip`** ready to send via Google Drive, WhatsApp, or USB.
* When the other devices extract the ZIP, they simply double-click `HomeService.exe` to run.

### 2. Quick Local Development Launch
Double-click `run.bat` in the project root.

### 3. Automated Test Suite
Double-click `test.bat` to run all 8 backend automated tests.

---

## 🌐 Multi-Device Cloud Database Sync

To run this app on multiple devices simultaneously sharing live data:
1. Create a free MySQL database on **[Aiven.io](https://aiven.io)** (free forever, no credit card required) or Clever Cloud.
2. Open `db.properties` and paste your host, port, db name, user, password, and set `db.ssl=true`.
3. Run `build_exe.bat` — your `dist\HomeService-Windows.zip` is now pre-configured to connect to the cloud from all devices!

---

## 🗄️ Database Architecture
The application automatically creates the `home_service_db` database and seeds realistic demo data on first launch using `db.properties` (`localhost:3306`, user: `root`, password: `root`).

Tables:
- `users`: User authentication, roles, contact details, status.
- `service_categories`: Service definitions and base pricing.
- `provider_profiles`: Trade details, hourly rates, experience, bio, and rating averages.
- `bookings`: Service schedules, time slots, problem descriptions, fee, and status transitions.
- `reviews`: Customer ratings and feedback linked to completed bookings.
