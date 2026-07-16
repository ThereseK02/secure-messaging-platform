# Secure Messaging Platform

> Documentation update in progress
> The application is actively evolving. Some feature descriptions, screenshots,
> endpoints, and architecture details in this README may not yet reflect the
> latest deployed version.

## Project Documentation

- [Current Application State](CURRENT_STATE.md)
- [Changelog](CHANGELOG.md)


## Project Overview



The Secure Messaging Platform is a full-stack cloud-deployed communication application that enables secure user authentication, private messaging, and group messaging through a modern web interface.



I designed and developed the platform using Spring Boot for the backend and React for the frontend. The system implements JWT-based authentication, secure password storage, protected REST APIs, containerized deployment using Docker, and cloud hosting on AWS EC2.



The platform demonstrates full-stack software engineering, cloud deployment, DevOps automation, database management, authentication, authorization, and secure communication workflows.



---



## Table of Contents

- [Project Overview](#project-overview)
- [Table of Contents](#table-of-contents)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Database Design](#database-design)
- [Security Features](#security-features)
- [Encryption Features](#encryption-features)
- [Welcome Page](#welcome-page)
- [Authentication Workflow](#authentication-workflow)
- [Messaging Features](#messaging-features)
- [Group Messaging](#group-messaging)
    - [Group Creation and Private Membership](#group-creation-and-private-membership)
    - [Invitations for Registered and Unregistered Users](#invitations-for-registered-and-unregistered-users)
    - [Group Roles and Administration](#group-roles-and-administration)
    - [Encrypted Group Attachments](#encrypted-group-attachments)
    - [Group Message Search](#group-message-search)
    - [Group Message Actions](#group-message-actions)
    - [Seen Status and Message Metadata](#seen-status-and-message-metadata)
    - [Real-Time Group Updates](#real-time-group-updates)
    - [Group Messaging Screenshots](#group-messaging-screenshots)
- [Deployment Architecture](#deployment-architecture)
- [Monitoring and Logging](#monitoring-and-logging)
- [CI/CD Pipeline](#cicd-pipeline)
- [Diagrams](#diagrams)
- [Key Contributions](#key-contributions)
- [Future Improvements](#future-improvements)
    - [Group Conversation Awareness](#group-conversation-awareness)
    - [Secure Decisions and Acknowledgments](#secure-decisions-and-acknowledgments)
    - [Password and Authentication Management](#password-and-authentication-management)
    - [Messaging and Notification Improvements](#messaging-and-notification-improvements)
    - [User Experience Improvements](#user-experience-improvements)
    - [Scalability Improvements](#scalability-improvements)
    - [Security Enhancements](#security-enhancements)
- [Learning Outcomes](#learning-outcomes)
- [Final Conclusion](#final-conclusion)
- [Author](#author)

---


## Key Features


- User registration and login

- JSON Web Token (JWT)-based authentication and authorization

- Secure password storage with BCrypt

- Private messaging between registered users

- Direct message attachment upload and download

- Inbox unread message tracking

- Group creation, joining, membership display, and group chat

- Real-time group chat with WebSocket/STOMP support and REST polling fallback

- PostgreSQL database persistence

- Spring Security integration

- RESTful API architecture

- Docker containerization

- AWS EC2 production deployment

- Nginx reverse proxy configuration

- HTTPS/SSL support

- GitHub Actions CI/CD automation

- Monitoring and backend logging

---



## System Architecture



The Secure Messaging Platform follows a modern three-tier architecture consisting of a React frontend, a Spring Boot backend, and a PostgreSQL database.

The frontend provides the user interface for authentication, private messaging, and group communication. Incoming user requests are routed through Nginx and forwarded to the appropriate application components. Spring Security and JWT authentication protect secured endpoints, while Hibernate/JPA manages database persistence and data access operations.

The Secure Messaging Platform is deployed on an AWS EC2 production server using Docker Compose. Incoming HTTPS requests are handled by Nginx, which serves the React frontend and routes API requests to the Spring Boot backend. The backend exposes REST APIs, enforces authentication and authorization through Spring Security and JWT tokens, and persists application data in a PostgreSQL database.

This architecture provides a clear separation of concerns between the presentation layer (React), application layer (Spring Boot), and data layer (PostgreSQL), resulting in a scalable, maintainable, and production-ready full-stack application.

---

## Technology Stack



### Backend



- Java 17

- Spring Boot

- Spring Security

- Hibernate / JPA

- JWT Authentication

- Maven



### Frontend



- React

- Vite

- React Router
  
- Axios

- JavaScript

- HTML5

- CSS3



### Database



- PostgreSQL



### DevOps & Cloud



- Docker

- Docker Compose

- AWS EC2

- Nginx

- GitHub Actions

- HTTPS / SSL



---



## Project Structure



```text
secure-messaging-platform/
├─ secure-messaging-backend/
├─ secure-messaging-frontend/
├─ screenshots/
├─ diagrams/
├─ docker-compose.yml
└─ README.md
```

---



## Database Design

### Database Design Diagram

![Database Design Diagram](diagrams/database_diagram.png)



Illustrates the relational database structure used by the platform, including users, messages, groups, group memberships, and group messages.



---



## Security Features



The Secure Messaging Platform implements multiple layers of security to protect user accounts, application resources, and communication workflows.


### JSON Web Token (JWT) Authentication



- Authenticates users after successful login

- Issues a JWT token for protected requests

- Requires valid tokens for secured API access

- Prevents unauthorized users from accessing private messaging features



### Spring Security



- Protects backend REST endpoints

- Separates public routes from protected routes

- Allows public access to registration and login

- Requires authentication for messaging, dashboard, and group communication features



### Password Protection



- Hashes passwords using BCrypt

- Avoids storing plain-text passwords

- Improves credential security in the database



### Protected REST APIs



- Validates authentication before processing sensitive requests

- Rejects unauthorized API calls

- Protects user messages, inbox data, and group messaging endpoints



### Secure Deployment



- Uses Nginx as a reverse proxy

- Supports HTTPS/SSL in production

- Runs services inside Docker containers

- Deploys the application on AWS EC2



---

## Encryption Features

The Secure Messaging Platform includes encryption-focused features that demonstrate secure communication workflows and protected message storage.

### Encrypted Message Transmission

Messages can be encrypted before being transmitted between users, helping protect message content during communication.

### Encrypted Storage Concepts

Encrypted message handling was explored during development to demonstrate how sensitive communication data can be protected within application workflows.

### Encryption Workflow

1. User composes a message.
2. Message content is encrypted.
3. Encrypted data is transmitted or stored.
4. Authorized users retrieve the message.
5. Message content is decrypted for viewing.

### Encryption Screenshots

#### Encrypted Send Message

![Encrypted Send Message](screenshots/security/07_encrypted_send_message.png)

Demonstrates message encryption before transmission, illustrating how message content can be protected prior to delivery.

#### Decrypted Inbox View

![Decrypted Inbox](screenshots/security/08_decrypt_inbox.png)

Shows successful message decryption within the inbox interface, allowing authorized users to view protected message content.

#### Encrypted Repository View

![Encrypted Repository View](screenshots/security/09_encrypted_repository_view.png)

Illustrates encrypted message persistence and storage concepts explored during development to support secure communication workflows.

---
## Welcome Page

The Secure Messaging Platform begins with a dedicated Welcome Page that serves as the public entry point to the application. The landing experience introduces the platform, highlights key features, and provides convenient access to registration and login workflows.

### Welcome Screen

![Welcome Page](screenshots/authentication/04_welcome_page.png)

The Welcome Page presents the Secure Messaging Platform branding, project overview, feature highlights, and navigation options that guide users into the secure messaging environment. This enhancement improves usability, strengthens application branding, and creates a more professional user experience before authentication.

---


## Authentication Workflow



The authentication workflow controls how users register, log in, and access protected features inside the Secure Messaging Platform.



### Registration



- A new user creates an account through the registration page.

- The backend receives the registration request through a REST API endpoint.

- The password is hashed using BCrypt before being stored.

- User information is persisted in the PostgreSQL database.



### Login



- A registered user logs in with valid credentials.

- The backend verifies the submitted password against the stored BCrypt hash.

- If authentication succeeds, the backend generates a JWT token.

- The frontend stores the token and uses it for future protected API requests.



### Protected Access



- The user can access secured pages such as the dashboard, private messaging, inbox, and group messaging.

- Each protected request includes the JWT token.

- Spring Security validates the token before allowing access to backend resources.


### Authentication Screenshots

#### Register Page

![Register Page](screenshots/authentication/01_register_page.png)

Displays the user registration interface used to create new platform accounts.

#### Login Page

![Login Page](screenshots/authentication/02_login_page.png)

Shows the secure login interface where users authenticate using their registered credentials.

#### Authenticated Dashboard

![Authenticated Dashboard](screenshots/authentication/03_dashboard_jwt_authenticated.png)

Illustrates successful authentication, confirming that the user's secure session is active and protected application features are accessible.

#### Dashboard Overview

![Dashboard Overview](screenshots/legacy-development/14_dashboard_overview.png)

Displays an earlier authenticated dashboard implementation used during development, highlighting navigation options and the evolution of the user interface prior to the final production design.

---


## Messaging Features

The Secure Messaging Platform enables authenticated users to exchange private messages through a secure and user-friendly interface. Private messaging is protected by JWT-authenticated API requests and integrated with the backend message persistence layer.

### Secure Message Delivery

Users can compose and send private messages to other registered users through the messaging interface. Messages are transmitted through protected REST API endpoints secured with JWT authentication.

### Attachment Upload and Download

The platform supports direct message attachments. Users can send a message with an attached file, and the receiver can view the attachment metadata from the inbox and download the file from the frontend interface.

### Inbox Management

Incoming messages are displayed in the user's inbox, allowing users to review received communications in a centralized location. The inbox displays sender information, message content, timestamps, unread status, and attachment download controls when a file is included.

### Unread Message Tracking

The inbox includes unread message tracking to help users identify new messages that require attention. Message cards display unread indicators, and the inbox summary shows unread and attention counts.

### Messaging Screenshots

#### Send Message with Attachment

![Send Message with Attachment](screenshots/messaging/07_send_message_with_attachment_success.png)

Shows a secure direct message being sent with an attachment and a success confirmation.

#### Inbox Attachment Download

![Inbox Attachment Download](screenshots/messaging/08_inbox_attachment_download_success.png)

Shows a received direct message with attachment metadata, unread status, and a successful attachment download confirmation.


---

## Group Messaging

The Secure Messaging Platform includes private group conversations for authenticated users. The group workflow supports invitation-based membership, role-based administration, encrypted attachments, searchable message history, message actions, seen counts, and real-time conversation updates.

### Group Creation and Private Membership

Authenticated users can create private groups by entering a group name. The user who creates the group becomes its `Owner`.

Groups are invitation-only. The earlier direct-join workflow using a group ID is disabled and no longer creates memberships.

A user becomes a group member only after accepting a valid invitation.

### Invitations for Registered and Unregistered Users

Group owners and administrators can invite participants through two separate workflows:

- Registered users are invited directly by username.
- Unregistered users receive an email registration invitation.

Registered users can review pending group invitations and either accept or decline them.

After an invitation is accepted, the group appears in the user's **My Groups** section.

The invitation workflow prevents invalid cases such as:

- Inviting the current user
- Inviting a user who is already a member
- Creating duplicate pending invitations
- Inviting a nonexistent username through the registered-user workflow

### Group Roles and Administration

Group participation uses three roles:

- `Owner`
- `Admin`
- `Member`

The group owner has the highest level of administrative control.

The owner can:

- Promote eligible members to administrators
- Demote administrators
- Remove eligible members
- Invite registered and unregistered users
- Manage the group

Administrators can invite users and remove eligible regular members. The group owner cannot be removed.

The interface displays role labels so that participants can distinguish the owner, administrators, and regular members.

### Group Membership Display

The selected group displays its current members using compact member badges.

Each member entry includes the username and the assigned role. Administrative controls appear only when the current user's role permits the corresponding action.

### Group Conversation Experience

The Group Chat interface uses a two-page workflow:

1. The group-management page displays group creation, pending invitations, and **My Groups**.
2. Selecting a group opens its conversation page.
3. Users can return to the group-management page with **Back to Groups**.
4. Eligible users can leave the selected group.
5. The group owner can manage the group according to the available owner controls.

Messages are displayed in a chat-style layout with sender labels, timestamps, date separators, and separate alignment for the current user's messages.

The message composer supports:

- Sending with the **Send** button
- Sending with `Enter`
- Adding a new line with `Shift+Enter`
- Emoji insertion
- Optional file attachment selection

### Encrypted Group Attachments

Group members can send encrypted file attachments inside group conversations.

The attachment workflow supports:

- Uploading an attachment with a group message
- Sending an attachment-only message
- Sending a text caption with an attachment
- Displaying attachment name and file size
- Downloading an attachment from the conversation
- Restricting attachment access to group members

The backend encrypts the attachment and stores a separately encrypted attachment key for each eligible group member. A user must have a valid encrypted key before the attachment can be decrypted and downloaded.

Messages containing attachments cannot currently be edited or deleted.

### Group Message Search

Users can search the selected group conversation.

Search matching includes:

- Sender username
- Message text
- Formatted timestamp
- Message date
- Attachment filename

Clearing the search restores the complete conversation history.

### Group Message Actions

Group messages use a compact actions interface instead of displaying permanent action buttons inside every message bubble.

A small `⋯` hint appears when a message is hovered, focused, or opened. Selecting the message displays the available actions.

The message bubble also supports keyboard activation with `Enter` or `Space`.

Available actions include:

- Pin or unpin a message
- Edit the current user's own text-only message
- Delete the current user's own text-only message

Authorization is enforced by both the frontend and backend.

### Pinned and Edited Messages

Pinned messages display the username of the member who pinned them.

Edited messages display an `edited` indicator beside their seen-status metadata.

The edit workflow uses an inline text area with Save and Cancel controls. Attachment messages remain protected from editing.

### Seen Status and Message Metadata

Group messages display a seen count using the current group membership total.

Example:

`Seen by 1 of 7`

Edited messages display both the seen count and edited status:

`Seen by 1 of 7 · edited`

The message-actions hint and seen-status text share a responsive metadata row. This prevents overlap and keeps normal, edited, pinned, short, and long messages aligned consistently.

Group unread-count badges have not yet been implemented. They remain part of a future conversation-awareness iteration.

### Real-Time Group Updates

Group conversations use Spring WebSocket/STOMP on the backend and a SockJS/STOMP client in React.

When group activity occurs, the backend publishes a notification to the selected group topic. The frontend then reloads the latest messages, attachments, and member data.

Real-time notifications support events such as:

- New group messages
- Edited messages
- Deleted messages
- Pinned or unpinned messages
- Membership changes
- Group changes

Periodic REST polling remains available as a fallback when the WebSocket connection is unavailable.

### Responsive Layout

The Group Chat interface is designed for laptop and monitor screens.

The layout includes:

- Internal scrolling for the conversation history
- A visible message composer
- Compact group and member controls
- Responsive message metadata
- Message bubbles that adapt to short and long content
- Separate alignment for sent and received messages

### Database Management

Group data is persisted with Hibernate/JPA and PostgreSQL.

The group messaging workflow uses stored records for:

- Groups
- Group memberships
- Registered-user invitations
- Email invitations
- Group messages
- Group message read records
- Attachments
- Group attachment encryption keys

Deleting a group also removes its related messages, read records, invitations, memberships, attachments, and group attachment keys.

### Group Messaging Screenshots

The screenshots below include updated views of the invitation and active-conversation workflows together with selected earlier development milestones. Milestone images are labeled accordingly and may contain controls or visual indicators that were later replaced or refined.

#### Group Management and Invitation Workflow

![Group Management and Invitation Workflow](screenshots/group-messaging/18_group_management_page.png)

Shows the invitation-based group-management workflow, including group creation, pending invitations, invitation details, Accept and Decline actions, and the **My Groups** section. Direct group joining by group ID is no longer part of the interface.

#### Active Group Conversation

![Active Group Conversation](screenshots/group-messaging/19_active_group_conversation.png)

Shows the active group conversation with registered-user and email invitation controls, role-based member administration, message search, message actions, seen and edited metadata, attachment selection, and the group message composer.

#### Two-User Group Chat and Autoscroll Milestone

![Two-User Group Chat and Autoscroll Milestone](screenshots/group-messaging/20_group_chat_two_user_autoscroll_test.png)

Documents a two-user conversation test used to verify message exchange, responsive layout, and autoscroll behavior.

#### Real-Time Group Chat Milestone

![Real-Time Group Chat Milestone](screenshots/group-messaging/21_real_time_group_chat_connected.png)

Documents the WebSocket connectivity milestone that introduced real-time group update notifications. The earlier green connection indicator shown in the screenshot is no longer treated as the final interface design.

---


## Deployment Architecture


The Secure Messaging Platform was designed using a containerized deployment architecture that supports local development, cloud hosting, automated deployments, and production scalability.



### Containerized Infrastructure



The application is packaged and deployed using Docker containers. Containerization provides a consistent runtime environment across development, testing, and production systems while simplifying deployment and maintenance.



### Docker Compose Orchestration



Docker Compose is used to manage and coordinate the application's services, including the Spring Boot backend and supporting infrastructure. This approach simplifies multi-service deployment and environment management.



### Cloud Deployment



The platform evolved through multiple deployment stages, beginning with local Docker deployments and cloud-hosted environments before reaching a production-ready AWS EC2 deployment.



### AWS EC2 Hosting



The production environment is hosted on AWS EC2, providing a reliable cloud infrastructure capable of supporting secure communication services. EC2 enables full control over deployment configuration, networking, and application management.



### Nginx Reverse Proxy



Nginx serves as a reverse proxy between users and the application services. Incoming HTTPS requests are routed through Nginx, which forwards frontend traffic to the React service, API traffic to the Spring Boot backend, and WebSocket traffic through the `/ws/` route for real-time group chat support.



### HTTPS and Domain Configuration



The platform supports HTTPS-secured communication, ensuring that data exchanged between users and the application is encrypted during transmission. HTTPS configuration improves security and aligns with modern web deployment standards.



### Deployment Workflow



1\. Develop and test features locally.

2\. Build Docker images.

3\. Push application updates to GitHub.

4\. Trigger the CI/CD pipeline.

5\. Deploy updated containers to AWS EC2.

6\. Route traffic through Nginx.

7\. Serve the application over HTTPS.



### Deployment Screenshots

#### Docker Local Deployment

![Docker Local Deployment](screenshots/deployment/10_docker_local_deployment.png)

Illustrates successful local execution of the application using Docker containers during development and testing.

#### Render Cloud Deployment

![Render Cloud Deployment](screenshots/deployment/11_render_cloud_deployment.png)

Shows an earlier cloud-hosted deployment stage used during platform development before migration to AWS EC2.

#### Server Status Response

![Server Status Response](screenshots/deployment/12_server_status_response.png)

Confirms successful backend operation through a health or status endpoint response.

#### Docker Hub Repository

![Docker Hub Repository](screenshots/deployment/13_docker_hub_repository.png)

Displays the Docker image repository used to store and manage application container images.

#### Docker Containers Running

![Docker Containers Running](screenshots/deployment/17_docker_containers_running.png)

Demonstrates successful execution of the platform services within running Docker containers.

#### Production HTTPS Domain

![Production HTTPS Domain](screenshots/deployment/18_production_https_domain.png)

Shows the production environment being served through HTTPS, demonstrating secure web communication.

#### AWS EC2 Deployment

![AWS EC2 Deployment](screenshots/deployment/19_aws_ec2_deployment.png)

Illustrates the application deployed and managed within the AWS EC2 cloud infrastructure.

---


## Monitoring and Logging



Monitoring and logging play an important role in maintaining application reliability, diagnosing issues, and verifying system behavior during development and production deployments.



### Backend Logging



The Spring Boot backend generates detailed runtime logs that provide visibility into application activity, authentication events, database operations, and API requests. These logs assist in identifying issues and validating system functionality.



### Hibernate SQL Monitoring



Hibernate SQL logging was enabled during development and testing to monitor database interactions. This capability allows developers to inspect generated SQL queries, verify database transactions, and troubleshoot persistence-related issues.



### Docker Log Management



Application logs can be accessed through Docker, providing centralized visibility into container activity. Docker logging simplifies monitoring and debugging of deployed services.



### Production Troubleshooting



Backend logs were extensively used throughout deployment and testing phases to:



- Verify successful API requests

- Monitor database connectivity

- Validate group messaging operations

- Troubleshoot authentication issues

- Confirm container health and application startup



### Observability Benefits



The logging infrastructure provides several operational benefits:



- Faster issue identification

- Improved debugging capabilities

- Enhanced deployment verification

- Better visibility into database operations

- Simplified maintenance and monitoring



### Monitoring Workflow



1\. Deploy application services.

2\. Monitor container status.

3\. Review backend runtime logs.

4\. Analyze Hibernate SQL queries.

5\. Identify and resolve issues.

6\. Validate application functionality.



### Monitoring and Logging Screenshot


#### Backend Monitoring and Logs

![Backend Monitoring and Logs](screenshots/deployment/21_backend_logs.png)

The screenshot demonstrates backend runtime monitoring through Hibernate SQL logs, showing successful database queries related to group messaging functionality and confirming communication between the Spring Boot application and the PostgreSQL database.

---

## CI/CD Pipeline



The Secure Messaging Platform incorporates a Continuous Integration and Continuous Deployment (CI/CD) workflow to automate the build, testing, and deployment process. This approach improves development efficiency, reduces manual deployment tasks, and ensures consistent application delivery.



### GitHub Actions Automation



GitHub Actions is used to automate deployment workflows whenever code changes are pushed to the repository. The pipeline provides a reliable mechanism for building and deploying application updates.



### Continuous Integration



The CI process automatically validates project updates by:



- Pulling the latest source code from GitHub

- Building the application components

- Verifying project configuration

- Preparing deployment artifacts



This helps identify issues early in the development lifecycle and improves code quality.



### Continuous Deployment



After successful validation, the deployment workflow can automatically update the target environment, reducing the need for manual deployment procedures.



### DevOps Benefits



Implementing CI/CD provides several advantages:



- Faster software delivery

- Reduced deployment errors

- Consistent deployment procedures

- Improved development productivity

- Better collaboration through version control automation



### Deployment Workflow



1\. Developer pushes code changes to GitHub.

2\. GitHub Actions automatically triggers the workflow.

3\. Application components are built and validated.

4\. Deployment artifacts are generated.

5\. Updated services are deployed to the target environment.

6\. The application becomes available to users.



### CI/CD Technologies



- GitHub Actions

- GitHub Repository Management

- Docker

- Docker Compose

- AWS EC2

- Nginx



### CI/CD Pipeline Screenshot

#### GitHub Actions CI/CD Pipeline

![GitHub Actions CI/CD Pipeline](screenshots/deployment/20_github_actions_pipeline.png)


The GitHub Actions workflow demonstrates the automated deployment pipeline used to build, validate, and deploy application updates, supporting a modern DevOps-oriented software development process.

---


## Diagrams



### System Design



#### System Architecture Diagram



![System Architecture Diagram](diagrams/system_architecture.png)


Illustrates the interaction between the React frontend, Spring Boot backend, PostgreSQL database, and supporting infrastructure services.


---


### Deployment Architecture



#### Production Deployment Architecture



![Deployment Architecture](diagrams/deployment_architecture.png)



Demonstrates the production deployment environment hosted on AWS EC2 using Docker Compose, including Nginx, React Frontend, Spring Boot Backend, and PostgreSQL Database services.



---



### UML Design



#### Design Class Diagram



![Class Diagram](diagrams/design_class_diagram.png)



Demonstrates the original object-oriented design of the Secure Messaging System and showcases UML modeling and software design principles.



---



## Key Contributions



Throughout this project, I was responsible for the complete software development lifecycle, including system design, backend development, frontend development, cloud deployment, security implementation, and DevOps (Development and Operations) automation.



### Backend Development



- Designed and implemented RESTful APIs (Representational State Transfer Application Programming Interfaces) using Spring Boot.

- Integrated Spring Security and JWT (JSON Web Token) authentication.

- Developed private messaging and group messaging functionality.

- Configured Hibernate and JPA (Java Persistence API) for database persistence.

- Designed database entities, repositories, services, and controller layers.

- Implemented secure communication workflows between frontend and backend services.



### Frontend Development



- Built a responsive user interface using React and Vite.

- Developed authentication, messaging, inbox, and group chat pages.

- Integrated frontend components with backend REST APIs.

- Improved user experience through intuitive navigation and communication workflows.

- Implemented secure token-based authentication handling within the frontend application.



### Security Implementation



- Implemented JWT (JSON Web Token) authentication and authorization.

- Secured passwords using BCrypt hashing, a password-hashing algorithm designed to protect credentials from brute-force attacks.

- Protected REST endpoints using Spring Security.

- Configured HTTPS (Hypertext Transfer Protocol Secure) for encrypted communication.

- Implemented secure backend validation and access-control mechanisms.



### Cloud and DevOps



- Containerized the application using Docker.

- Managed multi-container deployments using Docker Compose.

- Deployed the platform to AWS EC2 (Amazon Web Services Elastic Compute Cloud).

- Configured Nginx reverse proxy services for traffic routing and application access.

- Implemented CI/CD (Continuous Integration and Continuous Deployment) automation using GitHub Actions.

- Managed production deployment, monitoring, logging, and troubleshooting workflows.


---


## Future Improvements



Several enhancements can be implemented to further improve the platform's functionality, scalability, security, and user experience.


### Planned Features

#### Group Conversation Awareness

- Unread group-message counts for each member
- Unread badges beside groups in the **My Groups** list
- A visible boundary between previously viewed and newly received messages
- Typing indicators for active group participants
- Online and offline presence indicators
- Presence timeout handling for closed tabs, disconnected clients, and inactive sessions

#### Secure Decisions and Acknowledgments

- Convert an eligible group message into a structured decision record
- Require acknowledgment from selected group members or all group members
- Display pending and completed acknowledgment counts
- Record the username and timestamp for each acknowledgment
- Preserve the original decision text and subsequent status changes
- Allow decisions to be marked as active, completed, superseded, or withdrawn
- Maintain an auditable decision history
- Restrict decision-management actions through group roles and permissions
- Export or review decision and acknowledgment records for administrative purposes

#### Password and Authentication Management

- Configurable minimum password length requirements
- Password strength validation using length and character-composition rules
- Rejection of weak, common, or compromised passwords
- Secure password change and password reset workflows
- Verification of the current password before sensitive account changes
- Login rate limiting and temporary account protection after repeated failed attempts
- Multi-Factor Authentication (MFA)
- Backup recovery codes
- Active-session and trusted-device management
- Refresh-token rotation and secure session revocation
- Security notifications for important account activity

#### Messaging and Notification Improvements

- Message delivery confirmation beyond the existing read and seen-status features
- Push notifications for new direct and group messages
- Optional notification preferences and sound controls
- Richer real-time collaboration events

#### User Experience Improvements

- User profile management
- Richer file and image previews
- File type validation
- Larger attachment workflows
- Improved attachment management

### Scalability Improvements



- Redis (Remote Dictionary Server) caching integration to improve performance and reduce database load.

- Load balancing for multiple application instances.

- Kubernetes (K8s) container orchestration for automated deployment, scaling, and management of containerized applications.

- Cloud-native monitoring solutions.

- Migration toward a microservices architecture.



### Security Enhancements


- Role-Based Access Control (RBAC) for granular permission management.

- Advanced audit logging.

- Enhanced security monitoring and session management.

- Key rotation and forward secrecy implementation.

---



## Learning Outcomes



This project provided valuable experience across multiple areas of software engineering, cybersecurity, cloud computing, and DevOps practices.



### Software Engineering



- Applied object-oriented design principles.

- Developed scalable backend architectures.

- Built modern frontend applications using React.

- Implemented RESTful API (Representational State Transfer Application Programming Interface) design practices.

- Improved software maintainability through modular application design.



### Security



- Learned JWT (JSON Web Token) authentication workflows.

- Implemented Spring Security authorization mechanisms.

- Applied secure password management using BCrypt hashing.

- Configured HTTPS (Hypertext Transfer Protocol Secure) deployments.

- Improved understanding of authentication and authorization best practices.



### Cloud Computing and DevOps



- Gained hands-on experience with Docker containerization.

- Managed multi-container applications using Docker Compose.

- Deployed applications to AWS EC2 (Amazon Web Services Elastic Compute Cloud).

- Configured Nginx reverse proxy services.

- Built CI/CD (Continuous Integration and Continuous Deployment) pipelines using GitHub Actions.

- Troubleshot production deployment issues and cloud infrastructure configurations.



### Database Management



- After designing relational database structures, I implemented persistence using Hibernate and JPA (Java Persistence API).

- Applied ORM (Object-Relational Mapping) concepts to connect Java objects with relational database tables.

- Gained more practical experience in managing data retrieval, storage, and database interactions.



### Full-Stack Development


- Integrated frontend and backend systems.

- Managed application deployment from development to production.

- Troubleshot deployment, networking, and infrastructure issues.

- Improved application maintainability, scalability, and security.

- Gained practical experience building and deploying a production-oriented full-stack application.



---



## Final Conclusion



The Secure Messaging Platform demonstrates the successful development and deployment of a modern full-stack communication application. The project combines secure authentication, private messaging, group messaging, database persistence, cloud deployment, containerization, and DevOps automation within a single production-oriented system.



Through this project, I applied software engineering best practices across backend development, frontend development, cybersecurity, database management, cloud infrastructure, and CI/CD (Continuous Integration and Continuous Deployment) workflows. The result is a scalable and maintainable platform that showcases practical experience with modern enterprise development technologies, including Spring Boot, React, Docker, AWS EC2 (Amazon Web Services Elastic Compute Cloud), Nginx, GitHub Actions, Hibernate, and JWT (JSON Web Token) authentication.



This project represents an important milestone in my journey as a Software Engineer and demonstrates my ability to design, build, secure, deploy, and maintain full-stack applications within modern cloud environments.



---

## Author

**Therese Kabayanja**

Software Engineer | Machine Learning Engineer | Data Scientist

- GitHub: [ThereseK02](https://github.com/ThereseK02)
- LinkedIn: [Therese Kabayanja](https://www.linkedin.com/in/therese-kabayanja-14a43739b)
- Production Website: [brain-secure-messaging.com](https://brain-secure-messaging.com)


