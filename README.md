# ConTACTO Backend

Backend de la plataforma ConTACTO, un servicio diseñado para conectar a personas mayores del colectivo LGTBI con voluntarios, fomentando el acompañamiento y reduciendo la soledad no deseada.

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-28-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-EC2%2FS3-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)

## 🚀 Tecnologías Clave

*   **Framework:** Spring Boot 3.2
*   **Seguridad:** Spring Security con autenticación JWT
*   **Base de Datos:** PostgreSQL
*   **Comunicación en Tiempo Real:** WebSockets con STOMP
*   **Cloud & Despliegue:** Docker, GitHub Actions (CI/CD), AWS EC2 & S3

## ✨ Funcionalidades Principales

*   **Registro y Autenticación:** Gestión de usuarios con roles (MAYOR, VOLUNTARIO, ADMIN).
*   **Perfiles de Usuario:** Gestión de información personal e intereses.
*   **Sistema de Matching:** Algoritmo que sugiere voluntarios compatibles según intereses comunes.
*   **Chat en Tiempo Real:** Comunicación bidireccional mediante WebSockets.
*   **Bienestar Emocional:** Registro diario del estado de ánimo y generación automática de alertas.
*   **Panel de Administración:** Endpoints para la gestión completa de usuarios, matches y alertas.

## 🛠️ Primeros Pasos (Local)

### Prerrequisitos

*   JDK 17
*   Maven
*   Docker y Docker Compose (opcional, para desarrollo local)

### Instalación y Ejecución

**Con Docker (recomendado)**
1.  Clona el repositorio:
    ```bash
    git clone https://github.com/salvarez91/contacto-backend.git
    cd contacto-backend
