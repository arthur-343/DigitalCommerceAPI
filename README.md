🛒 DigitalCommerce API
A robust and scalable E-commerce REST API built with Spring Boot 3.
Focused on traceability, resilience, and industrial standards, featuring full payment integration with Mercado Pago, automated testing, and containerization.

🏗️ Architecture & Business Logic
⚙️ MVC & Service Layer Pattern
Interface-Implementation Separation: Controllers interact only with interfaces.

Loose Coupling: Business logic can be updated without affecting entry points.

Clean Implementation: Services (AddressesServiceImpl, CartServiceImpl) follow predictable and testable contracts.

📑 Purchase History & Audit System
Transaction Logs: Each checkout creates an Order with PENDING_PAYMENT status.

Audit Trail: All outcomes (success, pending, failure) are recorded.

Data Immutability: Once payment is confirmed, order details are frozen to prevent discrepancies.

🧪 Automated Testing Suite
Unit Testing (JUnit 5 & Mockito): Validates stock rules, discounts, and pricing logic.

Integration Testing: Ensures full flow from API endpoint to PostgreSQL.

Gateway Mocking: External Mercado Pago calls are mocked for consistency.

🐳 Containerization with Docker
Dockerfile: Optimized multi-stage build.

Docker Compose: Orchestrates API + PostgreSQL with volume persistence and networking.

🚀 Quick Start
bash
docker-compose up -d
⚡ Key Features & Services
💳 Checkout & Webhook Flow
Preference Creation: Validates stock, saves order, generates Mercado Pago preference.

External Sync: Webhook updates order to PAID, saves payment details, clears cart.

Stock Management: Real-time checks prevent overselling.

🔄 Event-Driven Integrity
Uses ApplicationEventPublisher for synchronization.

Example: deleting a product triggers updates in active carts.

📁 Advanced File Handling
Image uploads with UUIDs to avoid collisions.

Dynamic directory creation on server.

Method,Endpoint,Description,Role

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


👨‍💻 Developed by
Arthur – Backend Engineer dedicated to building high-availability, mission-critical systems.
