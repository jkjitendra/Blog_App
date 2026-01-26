# 📝 Blog App

## Overview

The **Blog App** is a full-stack web application that allows users to **create, read, update, and delete** blog posts with **user authentication, role-based access control (RBAC), real-time reactions, comments, and payment processing**. 

This application integrates **third-party authentication (Google, GitHub, etc.), Single Sign-On (SSO), and Vault-based secret management** for enhanced security. Additionally, users can subscribe to premium content through **integrated payment processing**.

## 🚀 Features

### ✨ Core Features
- **User Authentication & Authorization**
  - JWT-based authentication
  - Role-Based Access Control (RBAC)
  - OAuth2 authentication with Google, GitHub, etc.
  - Single Sign-On (SSO) support
  - Password reset & email verification

- **Blog Management**
  - Create, update, and delete blog posts
  - Categorize posts using tags
  - Markdown & rich text editor support
  - Post archiving and soft deletion
  - Media support: Upload images & videos

- **Comments & Reactions**
  - Nested comments & replies
  - Reactions on posts & comments (like, love, etc.)
  - Real-time updates using WebSockets

- **User Profiles**
  - Edit profile, add bio, and profile picture
  - Manage social media links

- **Search & Filtering**
  - Full-text search for posts
  - Filter posts by categories & tags

- **Rate Limiting & Security**
  - Brute force protection using **Bucket4j**
  - Secure API endpoints with **Spring Security**
  - API logging & monitoring

### 💰 Payment & Subscription
- Integrated **payment gateway** (Stripe, Razorpay, etc.)
- **Premium content access** based on subscription
- Different **subscription plans** (monthly, yearly)
- Webhooks for payment event handling
- Payment history and invoices

### 🔑 3rd Party Authentication & SSO
- **Google & GitHub authentication**
- **Single Sign-On (SSO)** integration for corporate users
- Secure OAuth2-based login flow

### ⚙️ Tech Stack

#### Backend (Spring Boot 3.x)
- **Spring Boot** (REST API)
- **Spring Security & OAuth2**
- **Spring Data JPA (MySQL) & MongoDB**
- **Spring Cloud Vault** (for secret management)
- **JWT Authentication**
- **Hibernate & PostgreSQL**
- **Bucket4j** (Rate Limiting)
- **Lombok & ModelMapper**

#### Frontend (React TypeScript + TailwindCSS)
- **React with TypeScript**
- **Tailwind CSS**
- **Redux Toolkit** (state management)
- **React Query** (API calls)
- **OAuth2 Integration for 3rd Party Login**
- **Stripe/Razorpay Integration for Payments**

---

## 🏗️ Architecture

- **Frontend:** React (TypeScript) + Tailwind CSS
- **Backend:** Spring Boot REST API
- **Database:** MySQL & MongoDB
- **Authentication:** JWT, OAuth2 (Google, GitHub), SSO
- **Payments:** Stripe/Razorpay integration
- **Secret Management:** Vault integration

---

## 🔧 Installation & Setup

### Prerequisites
- **Java 17+**
- **Maven**
- **MySQL & MongoDB**
- **Vault (for secret management)**

### Backend Setup
```sh
# Clone the repository
git clone https://github.com/jkjitendra/Blog_App.git

# Navigate to the backend
cd backend/blog

# Build and run
./mvnw clean install
./mvnw spring-boot:run
```

### Configure **Vault** for storing secrets:
   - Ensure **Vault** is running and initialized.
   - Set up the following Vault paths:
     ```
     kv/blogapp/dev
     kv/blogapp/prod
     ```

### Configure **application.properties** (or `application.yml`):
   ```properties
   server.port=9090
   spring.profiles.active=dev
   spring.config.import=vault://
   spring.cloud.vault.uri=${VAULT_ADDR}
   spring.cloud.vault.authentication=TOKEN
   spring.cloud.vault.token=${VAULT_TOKEN}
   spring.cloud.vault.kv.enabled=true
   spring.cloud.vault.kv.backend=kv
   spring.application.name=blogapp/${spring.profiles.active}
   ```
Run the backend:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```


## Database Schema
### ER Diagram
![ER Diagram](https://github.com/user-attachments/assets/d3c5c63b-8e6c-45e4-9cb1-11beacb20715)


## API Endpoints

### Authentication & Authorization (`/api/v1/auth`)
| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|---------------|
| `GET` | `/api/v1/auth/health` | Health check | ❌ Public |
| `POST` | `/api/v1/auth/register` | Register user | ❌ Public |
| `POST` | `/api/v1/auth/login` | Login & get JWT | ❌ Public |
| `POST` | `/api/v1/auth/logout` | Logout user | ✅ Authenticated |
| `POST` | `/api/v1/auth/refresh` | Refresh access token | ✅ Authenticated |
| `POST` | `/api/v1/auth/forgot-password` | Send OTP for password reset | ❌ Public |
| `POST` | `/api/v1/auth/verify-otp` | Verify OTP | ❌ Public |
| `POST` | `/api/v1/auth/reset-password` | Reset password | ❌ Public |
| `POST` | `/api/v1/auth/activate` | Activate user | ✅ Authenticated |
| `POST` | `/api/v1/auth/sso/google` | Google Single Sign-On | ❌ Public |
| `POST` | `/api/v1/auth/sso/github` | GitHub Single Sign-On | ❌ Public |

### OAuth & SSO (`/api/v1/auth/oauth`)
| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|---------------|
| `GET` | `/api/v1/auth/oauth/google` | Google OAuth Login | ❌ Public |
| `GET` | `/api/v1/auth/oauth/github` | GitHub OAuth Login | ❌ Public |
| `GET` | `/api/v1/auth/oauth/sso` | SSO Login | ❌ Public |

### User Management (`/api/v1/users`)
| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|---------------|
| `GET` | `/api/v1/users/me` | Get User Details | ✅ Authenticated |
| `GET` | `/api/v1/users/{email}` | Get user Details by email | ✅ Admin |
| `GET` | `/api/v1/users/all` | Get all users Details | ✅ Admin |
| `PUT` | `/api/v1/users/update-user` | Update user details | ✅ Authenticated |
| `PUT` | `/api/v1/users/update-password` | Change password | ✅ Authenticated |
| `DELETE` | `/api/v1/users/` | Delete user | ✅ Authenticated |
| `POST` | `/api/v1/users/deactivate` | Deactivate user | ✅ Authenticated |

### Post Management (`/api/v1/posts`)
| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/posts/` | Create a new post | ✅ (POST_WRITE) |
| `GET` | `/api/v1/posts/` | Get all posts (paginated) | ❌ Public |
| `GET` | `/api/v1/posts/{postId}` | Get a single post by ID | ❌ Public |
| `PUT` | `/api/v1/posts/{postId}` | Update an existing post | ✅ (POST_WRITE) |
| `PATCH` | `/api/v1/posts/{postId}/visibility` | Toggle post visibility | ✅ (POST_WRITE) |
| `DELETE` | `/api/v1/posts/{postId}` | Delete a post | ✅ (POST_DELETE) |
| `GET` | `/api/v1/posts/user/{username}` | Get posts by a specific user | ❌ Public |
| `GET` | `/api/v1/posts/category/{categoryId}` | Get posts by category | ❌ Public |
| `GET` | `/api/v1/posts/search/{searchKey}` | Search posts by title | ❌ Public |

### Comment Management
| Method | Endpoint | Description | Authentication |
|--------|---------|-------------|----------------|
| `POST` | `/api/v1/comments/post/{postId}/comments` | Add a comment to a post | ✅ (USER) |
| `GET` | `/api/v1/comments/post/{postId}` | Get all comments for a post | ❌ Public |
| `GET` | `/api/v1/comments/comment/{commentId}` | Get a specific comment by ID | ❌ Public |
| `PUT` | `/api/v1/comments/comment/{commentId}` | Update a comment | ✅ (USER) |
| `DELETE` | `/api/v1/comments/{commentId}` | Delete a comment | ✅ (USER) |
| `DELETE` | `/api/v1/comments/post/{postId}/bulk-delete` | Bulk delete comments | ✅ (ADMIN/MODERATOR) |

### Category Management
| Method | Endpoint | Description | Authentication |
|--------|---------|-------------|----------------|
| `POST` | `/api/v1/categories/` | Create a category | ✅ (CATEGORY_MANAGE) |
| `GET` | `/api/v1/categories/{categoryId}` | Get category by ID | ❌ Public |
| `GET` | `/api/v1/categories/{categoryTitle}` | Get category by title | ❌ Public |
| `GET` | `/api/v1/categories/` | Get all categories | ❌ Public |
| `PUT` | `/api/v1/categories/{categoryId}` | Update a category | ✅ (CATEGORY_MANAGE) |
| `DELETE` | `/api/v1/categories/{categoryId}` | Delete a category | ✅ (CATEGORY_MANAGE) |

### Reaction Management
| Method | Endpoint | Description | Authentication |
|--------|---------|-------------|----------------|
| `POST` | `/api/v1/reactions/post/{postId}` | Add/Update reaction on post | ✅ (USER) |
| `POST` | `/api/v1/reactions/post/{postId}/comment/{commentId}` | Add/Update reaction on comment | ✅ (USER) |
| `GET` | `/api/v1/reactions/post/{postId}/counts` | Get reactions count for a post | ❌ Public |
| `GET` | `/api/v1/reactions/comment/{commentId}/counts` | Get reactions count for a comment | ❌ Public |

### Profile Management
| Method | Endpoint | Description | Authentication |
|--------|---------|-------------|----------------|
| `GET` | `/api/v1/profiles/user` | Get user profile | ✅ |
| `PUT` | `/api/v1/profiles/user` | Update user profile | ✅ |
| `PATCH` | `/api/v1/profiles/user/` | Partially update user profile | ✅ |
| `DELETE` | `/api/v1/profiles/user` | Delete user profile | ✅ |

### Payments (`/api/v1/payments`)
| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/payments/initiate` | Start a new payment transaction | ✅ Authenticated |
| `POST` | `/api/v1/payments/webhook` | Handle webhooks | ✅ Internal |
| `GET` | `/api/v1/payments/history` | Get payment history | ✅ Authenticated |
| `GET` | `/api/v1/payments/status/{paymentId}` | Check payment status | ✅ Authenticated |
| `POST` | `/api/v1/payments/refund/{transactionId}` | Process a refund | ✅ (ADMIN) |

## Security & Role Management
- **Admin** can manage users, posts, and roles.
- **Moderators** can approve or reject posts/comments.
- **Users** can create posts and comments.
- **Subscribers** have access to premium content.

## Logging & Monitoring
- **Logback for logging** (`logs/prod.log`)
- **Spring Boot Actuator** (`/actuator/health` for health checks)
- **Rate Limiting with Bucket4j**

## Testing
### Running Tests
```bash
mvn test
```

### Unit Tests
- **JUnit & Mockito** for unit testing services and controllers.
- **Spring Boot Test** for integration testing.


## Contributors
- **Jitendra Kumar Tiwari** - Full Stack Developer

## License
This project is licensed under the MIT License. You can freely use, modify, and distribute it, but you must include the original license and give credit.

## 🚀 Contribute
We welcome contributions! Please open a Pull Request with proper documentation and tests.

## 📞 Contact & Support
Email: jitendrakumartiwari849@gmail.com
