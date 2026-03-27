# CampusFeedbacks Backend

CampusFeedbacks is a multi-institution feedback management backend system built using Spring Boot.

It is designed for colleges or institutions where admins, instructors, and students interact in a structured system.

This project mainly focuses on backend architecture, security, role management, and real-world system design.

## Problem statement

- Most colleges lack a transparent and structured system to collect and analyze student feedback on instructors and courses.

- Without proper feedback data and analytics, management cannot effectively evaluate teaching quality or make informed improvements.

- CampusFeedbacks provides a secure, institution-based feedback system that enables students to share feedback and helps institutions analyze instructor performance and improve academic quality through data-driven insights.

## Why this project exists

In colleges, students and instructors play a major role in the overall quality of education.

But in most institutions, management does not have a clear and structured way to understand how instructors are actually performing inside classrooms.

Student feedback is usually collected manually or informally, which leads to:

- lack of transparency

- biased or incomplete data

- no proper analytics to take decisions

Because of this, colleges struggle to:

- identify strong and weak teaching patterns

- improve teaching methods

- recognize or reward high-performing instructors

- take corrective actions where needed

This project solves that problem by providing a centralized feedback and analytics system where students can submit feedback for courses and instructors, either openly or anonymously.

The system then converts feedback into structured data and analytics, helping institutions:

- monitor teaching quality

- track instructor performance over time

- make data-driven decisions instead of assumptions

- improve both teaching and learning experience

## What this project does

* Supports multiple institutions.

* Each institution has its own:

  - admins

  - instructors

  - students

  - courses

  - feedbacks

* Users are separated by roles and institution scope.  

* All APIs are secured using JWT authentication.

## Roles


### Admin

- Create and manage institution

- Create users (students, instructors, admins)

- Manage courses

- Assign instructors to courses

- View analytics and feedback stats

- Full control inside their institution

### Instructor

- Linked to a user account

- Assigned to courses

- Can view feedback related to their courses

### Student

- Register under an institution

- Enroll in courses

- Submit feedback

- View own feedback history

## Main features

- Institution-based multi-tenant system

- JWT authentication with refresh tokens

- Role-based access control (ADMIN / INSTRUCTOR / STUDENT)

- Feedback system with analytics

- Course enrollment system

- Institution-scoped data access (no cross-institution access)

- Secure APIs using Spring Security

- Clean layered architecture (Controller, Service, Repository, DTO, Entity)

- Pagination, filtering, and search APIs

- Analytics queries using JPQL

## Tech stack

- Java

- Spring Boot

- Spring Security

- JWT (Access Token + Refresh Token)

- JPA / Hibernate

- MySQL

- Maven

- REST APIs

## Project structure

```text

controller/     -> API layer

service/        -> business logic

repository/     -> database access

entity/         -> JPA entities

dto/            -> request/response models

security/       -> JWT, filters, security config

utils/          -> helper classes

```

## Authentication flow

* User registers under an institution

* User logs in using email + password

* Server returns:

  - access token (JWT)

  - refresh token

* Access token is used for API calls

* Refresh token is used to generate new access token when expired

## Institution logic

Every user belongs to an institution.

All data access is institution scoped.

So:

- Admin of one institution cannot access another institution’s data

- Students cannot see other institution’s courses

- Instructors only see their assigned courses and feedbacks


## Status

Backend system is complete and tested using real database and Postman.

Core flows implemented:

- Auth

- Institution onboarding

- User management

- Instructor mapping

- Course management

- Enrollment

- Feedback system

- Analytics APIs

