# ISDN Management System

**IslandLink Sales Distribution Network Management System** — a backend REST API for managing sales, inventory, orders, deliveries, payments, and reporting for a distribution network.

## What I Built

A full-featured Spring Boot backend system with the following modules:

- **Authentication & Authorization** — JWT-based login/register with role-based access control
- **Product & Category Management** — CRUD operations with search and filtering
- **Cart & Order Management** — Shopping cart, order placement, and status tracking
- **Inventory Management** — Stock tracking, stock movements, and transfers between RDCs (Regional Distribution Centers)
- **Delivery & Driver Management** — Assign deliveries to drivers, track delivery status and location
- **Payment Integration** — PayHere payment gateway integration (sandbox) with payment verification
- **Invoice Generation** — PDF invoice generation and email delivery via iText7
- **Dashboard & Reporting** — Sales summaries, top products, regional sales, driver performance, inventory turnover
- **Export** — Excel report export using Apache POI

## Technologies Used

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot | 4.0.1 | Application framework |
| Spring Security | 6.5.6 | Authentication & authorization |
| Spring Data JPA / Hibernate | — | ORM & database access |
| MySQL | 8 | Relational database |
| JWT (jjwt) | — | Stateless authentication tokens |
| iText7 | — | PDF invoice generation |
| Apache POI | — | Excel report export |
| Lombok | — | Boilerplate reduction |
| SpringDoc / Swagger UI | — | API documentation |
| Maven | — | Build tool |
| Docker / Docker Compose | — | Containerization |

## How to Run

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8 running locally (or use Docker Compose)

### Option 1 — Docker Compose (Recommended)

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

### Option 2 — Local Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd isdn-management-system
   ```

2. **Create the MySQL database**
   ```sql
   CREATE DATABASE isdn_db;
   ```

3. **Configure credentials**

   Edit `src/main/resources/application.yml` and set your MySQL username/password:
   ```yaml
   spring:
     datasource:
       username: root
       password: your_password
   ```

4. **Build and run**
   ```bash
   ./mvnw spring-boot:run
   ```
   On Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

5. **Access the API**
   - Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`

### Default Credentials

Sample data is loaded via `src/main/resources/data.sql` on startup.

## API Highlights

| Module | Endpoint Prefix |
|---|---|
| Auth | `/api/auth` |
| Products | `/api/products` |
| Cart | `/api/cart` |
| Orders | `/api/orders` |
| Inventory | `/api/inventory` |
| Delivery | `/api/delivery` |
| Drivers | `/api/drivers` |
| Payments | `/api/payments` |
| Dashboard | `/api/dashboard` |

## Academic Supervision
This project was guided by Nimesha Rajakaruna as part of undergraduate coursework.

Git Hub Name - nimesharajakaruna1-beep
