# Secure Messaging System

A Java secure messaging demonstration that uses hybrid encryption with RSA, AES, and digital signatures. The original console-based application has been wrapped with a simple Spring Boot web interface so it can run locally, be published on GitHub, and be deployed using Docker/Render.

## Docker Hub badge

[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-secure--messaging--platform-2496ED?logo=docker&logoColor=white)](https://hub.docker.com/r/theresek02/secure-messaging-platform)

## Docker Deployment

Pull the Docker image:

```bash
docker pull theresek02/secure-messaging-platform:latest
```

Run the container:

```bash
docker run -p 8081:8080 theresek02/secure-messaging-platform:latest
```

Open in browser:

```text
http://localhost:8081

```
### Docker Features
- Containerized Spring Boot backend
- Docker image publishing through Docker Hub
- Public container repository
- Render cloud deployment support
- Cross-environment portability

### Docker Hub Repository

![Docker Hub Repository](screenshots/docker_hub_repository.png)

## Features

- User registration with RSA key pair generation
- Password hashing with SHA-256 for demonstration purposes
- Hybrid message encryption
  - AES encrypts the message payload
  - RSA encrypts the AES session key
- Digital signatures using SHA256withRSA
- In-memory user and message repositories
- Browser-based demo interface
- REST API endpoints
- Dockerfile for container deployment
- Render blueprint file

## Project Structure

```text
secure-messaging-system/
├── Dockerfile
├── README.md
├── render.yaml
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/securemessaging/
        │       ├── SecureMessagingApplication.java
        │       ├── core/
        │       │   └── SecureMessagingSystem.java
        │       └── web/
        │           ├── DemoState.java
        │           └── MessagingController.java
        └── resources/
            ├── application.properties
            └── static/
                └── index.html
```

## How It Works

1. The user registers with a username and password.
2. The system generates an RSA public/private key pair for that user.
3. When a sender sends a message:
   - The message is encrypted with an AES session key.
   - The AES session key is encrypted with the receiver's RSA public key.
   - The encrypted payload is digitally signed using the sender's RSA private key.
4. When the receiver opens the inbox:
   - The digital signature is verified with the sender's RSA public key.
   - The AES session key is decrypted with the receiver's RSA private key.
   - The message payload is decrypted and displayed.

## Run Locally

Requirements:

- Java 17+
- Maven 3.9+

```bash
mvn clean package
java -jar target/secure-messaging-system-1.0.0.jar
```

Open:

```text
http://localhost:8080
```

## Run with Docker

```bash
docker build -t secure-messaging-system .
docker run -p 8080:8080 secure-messaging-system
```

Open:

```text
http://localhost:8080
```

## Render Deployment

1. Push this project to GitHub.
2. Go to Render.
3. Create a new Web Service.
4. Connect your GitHub repository.
5. Choose Docker as the environment.
6. Deploy.

The application uses this port setting:

```properties
server.port=${PORT:8080}
```

This allows it to use port 8080 locally and Render's assigned port in production.
## Live Cloud Deployment

Application URL:

```text
https://secure-messaging-platform-v2.onrender.com/
```
## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | Check if the server is running |
| GET | `/api/users` | List registered users |
| POST | `/api/users/register` | Register a user |
| POST | `/api/messages/send` | Send encrypted message |
| POST | `/api/messages/inbox` | Decrypt receiver inbox |
| GET | `/api/messages/encrypted` | View encrypted message records |

## Example API Request

```bash
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{"sender":"Alice","receiver":"Bob","message":"Hello Bob"}'
```

## Academic Note

This project is intended for educational demonstration of hybrid encryption, digital signatures, and secure messaging architecture. For production use, the system would need stronger password storage, authenticated encryption such as AES-GCM, persistent storage, session management, HTTPS enforcement, secure key storage, and additional security controls.

## Author

Therese Kabayanja
