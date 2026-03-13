# RailLink - Train Scheduling and Booking System

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

A professional, full-stack Java Spring Boot application designed for efficient web-based train scheduling, route management, and passenger booking.

---

## 📖 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Project Architecture](#-project-architecture)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
- [API Documentation](#-api-documentation)
- [Security Matrix](#-security-matrix)
- [Agile Roadmap](#-agile-roadmap)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### 👤 User Capabilities
- **Role-Based Access**: Specialized interfaces for Passengers, Staff, and Administrators.
- **Dynamic Booking**: Real-time seat availability checks and transactional booking.
- **Profile Management**: Personal booking history and ticket management.
- **E-Tickets**: PDF ticket generation with QR codes (planned) for seamless travel.

### 🛠️ Administrative Tools
- **Route & Station Management**: Comprehensive CRUD operations for rail networks.
- **Train Scheduling**: Precise control over departure/arrival times and capacity.
- **KPI Dashboard**: Administrative insights into system health and booking trends.
- **Announcements**: System-wide communication tools for staff and users.

### ⚡ Technical Excellence
- **Real-time Updates**: SSE-powered (Server-Sent Events) live schedule updates.
- **Secure Authentication**: Robust Spring Security integration.
- **Responsive UI**: Thymeleaf-driven dynamic templates for all devices.

---

## 💻 Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.5.4 |
| **Security** | Spring Security 6 |
| **Data Access** | Spring Data JPA, Hibernate |
| **Database** | MySQL 8.0 |
| **Templating** | Thymeleaf 3 |
| **Build Tool** | Maven 3.6+ |
| **Documentation** | RESTful API Design |

---

## 🏗️ Project Architecture

```text
src/main/java/com/raillink/
├── config/              # Security and System Configurations
├── controller/          # REST & MVC Controllers (Admin, Auth, Booking, etc.)
├── model/               # JPA Entities (User, Train, Booking, etc.)
├── repository/          # Spring Data JPA Repository Interfaces
├── service/             # Business Logic Layer
└── RailLinkApplication  # Application Entry Point
```

---

## 🗄️ Database Schema

The system is built around six core entities:
- **User**: Handles authentication and role management (PASSENGER, ADMIN, STAFF).
- **Train**: Stores physical train attributes and capacity.
- **Station**: Defines geographical nodes in the rail network.
- **Route**: Links stations into logical paths.
- **Schedule**: Maps trains to routes with specific timestamps.
- **Booking**: Manages the relationship between users and scheduled seats.

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or higher
- **MySQL 8.0**
- **Maven 3.6**

### Installation
1. **Clone the Repo**
   ```bash
   git clone <repository-url>
   cd RailLink
   ```

2. **Database Setup**
   - Create a database: `CREATE DATABASE raillink_db;`
   - The application uses `spring.jpa.hibernate.ddl-auto=update` by default.

3. **Build & Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### Configuration
Update `src/main/resources/application.properties` with your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/raillink_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

---

## 📡 API Documentation

### Authentication
- `POST /register`: Register new passenger account.
- `POST /login`: Secure session initiation.

### Booking (ROLE_PASSENGER)
- `GET /api/bookings/search`: Query schedules by origin, destination, and date.
- `POST /api/bookings/`: Create a reservation.
- `GET /api/profile/my-bookings/{id}/ticket`: Download PDF ticket.

### Administration (ROLE_ADMIN)
- `GET /api/admin/trains`: Manage train fleet.
- `POST /api/admin/schedules`: Deploy new train schedules.

---

## 🔒 Security Matrix

| Endpoint | Required Role | Access Level |
| :--- | :--- | :--- |
| `/login`, `/register`, `/css/**` | `PUBLIC` | Anonymous |
| `/api/bookings/**`, `/profile/**` | `PASSENGER` | Authenticated |
| `/api/admin/**` | `ADMIN` | Administrative |
| `/api/staff/**` | `STAFF` | Operational |

---

## 📅 Agile Roadmap

### Completed Sprints
- **Sprint 1**: Project Bootstrap & Auth wiring.
- **Sprint 2**: Core Domain & Admin CRUD operations.
- **Sprint 3**: Search, Booking engine & PDF generation.
- **Sprint 4**: Admin Dashboard & User Profiles.
- **Sprint 5**: SSE Real-time updates & Staff tools.
- **Sprint 6**: Feedback, Announcements & Account Recovery.

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git checkout -b feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License. 
