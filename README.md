# 🛒 DigitalCommerce API

A robust and scalable E-commerce REST API built with **Spring Boot 3**. 
Focused on traceability, resilience, and industrial standards, featuring full payment integration with **Mercado Pago**, automated testing, and containerization.

---

## 🏗️ Architecture & Business Logic

### ⚙️ MVC & Service Layer Pattern
* **Interface-Implementation Separation:** Controllers interact only with interfaces to promote decoupling.
* **Loose Coupling:** Business logic can be updated without affecting API entry points.
* **Clean Implementation:** Services (like `AddressesServiceImpl`, `CartServiceImpl`) follow predictable and testable contracts.

### 📑 Purchase History & Audit System
* **Transaction Logs:** Each checkout creates an `Order` with an initial `PENDING_PAYMENT` status.
* **Audit Trail:** All outcomes (success, pending, failure) are recorded for financial tracking.
* **Data Immutability:** Once payment is confirmed, order details are "frozen" to prevent future catalog changes from affecting historical records.

---

## 🧪 Automated Testing Suite
* **Unit Testing (JUnit 5 & Mockito):** Validates stock rules, discounts, and pricing logic.
* **Integration Testing:** Ensures the full flow from the API endpoint down to the PostgreSQL database.
* **Gateway Mocking:** External Mercado Pago calls are mocked to ensure consistent and stable test runs.

---

## 🐳 Containerization with Docker
* **Dockerfile:** Optimized multi-stage build to ensure small and secure images.
* **Docker Compose:** Orchestrates the API and PostgreSQL with volume persistence and dedicated networking.

### 🚀 Quick Start
```bash

⚡ Key Features & Services
💳 Checkout & Webhook Flow
Preference Creation: Validates stock availability in real-time, persists the order in the database, and generates the Mercado Pago preference link for the customer.

External Sync: An intelligent Webhook listener handles asynchronous notifications, updating orders to PAID status, logging payment metadata, and triggering automated cart clearance.

Stock Management: Strict concurrency checks prevent overselling during high-traffic checkout sessions.

🔄 Event-Driven Integrity
The system utilizes Spring's ApplicationEventPublisher to maintain internal consistency without tightly coupling services.

Example: When a product is deleted by an admin, an internal event is fired to automatically update or remove that item from all active customer shopping carts.

📁 Advanced File Handling
Security: All product image uploads are renamed using UUIDs to eliminate filename collisions and prevent directory traversal attacks.

Automation: Features dynamic directory creation on the host server to ensure seamless asset management even in fresh environments.

```

## 📖 API Endpoints Summary

| Method | Endpoint | Description | Role |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/signup` | User/Admin registration with CPF validation. | Public |
| **POST** | `/api/auth/signin` | JWT Authentication & Role-based access. | Public |
| **GET** | `/api/public/products` | Catalog with search, pagination, and sorting. | Public |
| **POST** | `/api/admin/products` | Add new product with image upload (UUID). | `ADMIN` |
| **PUT** | `/api/admin/products/{id}` | Update product details and stock. | `ADMIN` |
| **POST** | `/api/carts/products/{id}/{qty}` | Add/Update cart items with stock check. | `USER` |
| **GET** | `/api/carts/users/getCart` | Retrieve the current user's shopping cart. | `USER` |
| **POST** | `/api/orders/create-preference/{id}` | Start checkout & log attempt to DB. | `USER` |
| **GET** | `/api/orders/user` | View personal purchase history (Paid/Failed). | `USER` |
| **POST** | `/api/webhooks/mercadopago` | Async payment listener & auditor. | Internal |

---

## 🔧 Environment Setup

Create a `.env` file or update `application.properties` with the following variables:

```ini
# 🔑 Mercado Pago Integration
config.integrations.mercadopago.access-token=YOUR_MERCADO_PAGO_TOKEN

# 🌐 Webhook Base URL (use Ngrok for local testing)
config.integrations.webhook.base-url=[https://your-public-url.com](https://your-public-url.com)

# 🖼️ Image Upload Path
config.paths.image-upload=/path/to/product/images

```
👨‍💻 Developed by
Arthur – Backend Engineer dedicated to building high-availability, mission-critical systems.
