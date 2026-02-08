# 🛒 DigitalCommerce API

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

A robust and scalable E-commerce REST API built with **Spring Boot 3**. Focused on traceability, resilience, and industrial standards, featuring full payment integration with **Mercado Pago**, automated testing, and containerization.

🔗 **GitHub Repository:** [arthurssace/digitalcommerce-api](https://github.com/arthurssace/digitalcommerce-api)  
🐳 **Docker Hub Image:** [arthurssace/digitalcommerce-api](https://hub.docker.com/r/arthurssace/digitalcommerce-api)

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

## ⚡ Key Features & Services

### 💳 Checkout & Webhook Flow
* **Preference Creation:** Validates stock availability in real-time, persists the order in the database, and generates the **Mercado Pago** preference link.
* **External Sync:** An intelligent **Webhook** listener handles asynchronous notifications, updating orders to `PAID`, logging payment metadata, and triggering automated cart clearance.
* **Stock Management:** Strict concurrency checks prevent overselling during high-traffic checkout sessions.

### 🔄 Event-Driven Integrity
The system utilizes Spring's `ApplicationEventPublisher` to maintain internal consistency without tight coupling.
* **Example:** Deleting a product automatically triggers an internal event to update or remove that item from all active shopping carts.

### 📁 Advanced File Handling
* **Security:** Product image uploads use **UUIDs** to eliminate filename collisions and prevent security vulnerabilities.
* **Automation:** Features dynamic directory creation on the server for seamless asset management.

---

## 🧪 Automated Testing Suite
* **Unit Testing (JUnit 5 & Mockito):** Validates stock rules, discounts, and pricing logic.
* **Integration Testing:** Ensures the full flow from the API endpoint down to the PostgreSQL database.
* **Gateway Mocking:** External Mercado Pago calls are mocked to ensure consistent and stable test runs.

---

## 🐳 Containerization & Quick Start

The application is fully containerized for easy deployment and development.

```bash
# Clone the repository
git clone [https://github.com/arthurssace/digitalcommerce-api.git](https://github.com/arthurssace/digitalcommerce-api.git)

# Spin up the infrastructure (API + PostgreSQL)
docker-compose up -d
```
## ⚡ Key Features & Services

### 💳 Checkout & Webhook Flow
* **Preference Creation:** Valida a disponibilidade de estoque em tempo real, persiste o pedido no banco de dados e gera o link de preferência do **Mercado Pago** para o cliente.
* **External Sync:** Um listener inteligente de **Webhook** lida com notificações assíncronas, atualizando pedidos para `PAID`, registrando metadados de pagamento e acionando a limpeza automática do carrinho.
* **Stock Management:** Verificações rigorosas de concorrência evitam overselling durante sessões de checkout de alto tráfego.

### 🔄 Event-Driven Integrity
O sistema utiliza o `ApplicationEventPublisher` do Spring para manter a consistência interna sem acoplamento rígido.
* **Exemplo:** Quando um produto é deletado por um admin, um evento interno é disparado para atualizar ou remover automaticamente esse item de todos os carrinhos ativos.

### 📁 Advanced File Handling
* **Security:** Uploads de imagens de produtos são renomeados com **UUIDs** para eliminar colisões de nomes e prevenir ataques de traversal de diretórios.
* **Automation:** Criação dinâmica de diretórios no servidor garante gestão de ativos contínua, mesmo em ambientes recém-configurados.


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
