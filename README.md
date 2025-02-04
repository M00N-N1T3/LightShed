# Lightshed

Lightshed is a replica of the **Eskom Loadshedding app**, built using **Java, Thymeleaf, Javalin, and ActiveMQ**. The application is designed as a microservices architecture, where five independent services work together to provide real-time loadshedding schedules and notifications.

It is a mock Loadshedding website, ran using the 2008 PlaceNameCSV which contains all the areas in South Africa that was recorded at that time. 

## Disclaimer

My main focus in this application was the backend logic. I do intend to continue working on it and improve its overall perfomance and UI interface once I learn enough CSS

## Features
- **Microservices Architecture** – The system is built using five independent microservices.
- **RESTful APIs** – Javalin is used to create and manage API endpoints.
- **Server-Side Rendering (SSR)** – Thymeleaf generates dynamic HTML templates, following MVC principles.
- **Message Queueing** – ActiveMQ relays changes in loadshedding stages and notifies when a service is down.
- **Real-Time Notifications** – Users get updates on loadshedding status.

## Technologies Used
- **Java** – Core programming language.
- **Javalin** – Lightweight web framework for REST API development.
- **Thymeleaf** – Server-side rendering for dynamic HTML generation.
- **ActiveMQ** – Message broker for handling real-time updates.
- **Maven** – Project dependency management.

## Installation & Setup

### Prerequisites
Ensure you have the following installed:
- Java (JDK 11 or later)
- Maven
- ActiveMQ



