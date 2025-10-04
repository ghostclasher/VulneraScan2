# VulneraScan 🛡️

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/SpringBoot-3.1.2-green)](https://spring.io/projects/spring-boot)
[![Semgrep](https://img.shields.io/badge/Semgrep-1.135.0-orange)](https://semgrep.dev/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

VulneraScan is a **Spring Boot-based REST API application** that allows users to upload code files and scan them for security vulnerabilities using **Semgrep**. It supports multiple languages and detects common security risks like SQL injection, XSS, command injection, and weak cryptography.

---

## 🚀 Features

- Upload **single files or ZIP archives** for scanning.
- Detect vulnerabilities in **Java, Python, JavaScript, and C/C++**.
- Uses **in-built Semgrep rules** for basic security coverage.
- Structured scan results stored in a **relational database**.
- Role-based access: only file owner can scan their files.
- REST API returns **JSON results** for integration with other systems.
- Swagger/OpenAPI documentation for testing endpoints.

---

## 🛠️ Technology Stack

- **Backend:** Spring Boot, Spring Security  
- **Database:** H2 (development) / PostgreSQL (production)  
- **Static Analysis:** Semgrep OSS  
- **API Documentation:** Swagger / Springdoc OpenAPI  
- **Build Tool:** Maven  
- **Language:** Java 17+  

---

## 📸 Screenshots

<!-- Add your screenshots here -->

![Upload Page](screenshots/upload.png)  
![Scan Result](screenshots/scan_result.png)

---

## ⚙️ Installation

1. **Clone the repository**

```bash
git clone https://github.com/ghostclasher/VulneraScan.git
cd VulneraScan
