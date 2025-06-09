# Loan Origination System (LOS)

A comprehensive loan origination system backend built with Spring Boot that manages the full lifecycle of loan applications from submission to decision.

## Overview

This Loan Origination System provides a complete solution for financial institutions to process loan applications efficiently. It includes:

- Automated loan processing with configurable decision rules
- Manual review workflow for complex cases
- Agent management and assignment
- Real-time dashboard for monitoring loan statuses
- Customer tracking and statistics
- RESTful APIs for integration with other systems

## Features

### Loan Application Processing

- **Multi-Stage Processing Pipeline**: Applications move through various stages from submission to decision
- **Automated Decisioning**: Rules-based engine for automatic approval/rejection based on loan amount and type
- **Manual Agent Review**: Complex cases are routed to agents for manual review
- **Status Tracking**: Track loans through their entire lifecycle

### Agent Workflow

- **Agent Dashboard**: Dedicated interface for agents to review and process loans
- **Agent Assignment**: Automatic round-robin assignment of loans to available agents
- **Manager Oversight**: Hierarchical structure with managers overseeing agent activities

### Dashboard & Analytics

- **Real-time Status Dashboard**: Visual representation of all loan applications and their statuses
- **Loan Statistics**: Track approval rates, processing times, and other key metrics
- **Top Customer Insights**: Identify top customers by loan amount and approval rates

### Technical Architecture

- **Asynchronous Processing**: Non-blocking loan processing for improved throughput
- **Paginated APIs**: Efficient data retrieval with pagination support
- **Transactional Operations**: ACID-compliant database operations
- **Notification System**: Decoupled notification logic for various channels

## Technology Stack

- **Backend**: Spring Boot, Java 17
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Frontend Templates**: Thymeleaf
- **Styling**: Bootstrap 5
- **Build Tool**: Maven

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── loanapp/
│   │           └── los/
│   │               ├── LoanOriginationSystemApplication.java
│   │               ├── config/
│   │               ├── controller/
│   │               ├── dto/
│   │               ├── model/
│   │               ├── repository/
│   │               ├── service/     
│   └── resources/
│       ├── application.properties
│       ├── static/
│       └── templates/
│           ├── admin.html
│           ├── agent-dashboard.html
│           ├── dashboard.html
│           ├── loan-application.html
│           ├── logs.html
│           └── notifications.html
└── test/
    └── java/
        └── com/
            └── loanapp/
                └── los/
                    ├── LoanOriginationSystemApplicationTests.java
                    └── service/
                        └── [Service tests]
```

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 13 or higher
- Maven 3.8+

### Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE los_db;
CREATE USER yourusername WITH PASSWORD 'yourpassword';
GRANT ALL PRIVILEGES ON DATABASE los_db TO yourusername;
```

2. Update database connection in application.properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/los_db
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
```

### Running the Application

```bash
# Clone the repository
git clone https://github.com/yourusername/los-backend.git
cd los-backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

## API Documentation

### Loan APIs

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/loans` | GET | Get loans with filtering and pagination |
| `/api/v1/loans` | POST | Submit a new loan application |
| `/api/v1/agents/decision/{loanId}` | POST | Process an agent's decision |
| `/api/top-customers` | GET | Get top customers by loan amount |
| `/api/top-customers/approved` | GET | Get top customers by approved loans |

### Sample Request and Response

#### Submit Loan Application
```http
POST /api/v1/loans
Content-Type: application/json

{
  "customerName": "John Doe",
  "customerPhone": "1234567890",
  "customerEmail": "john@example.com",
  "customerIdentityNumber": "ID12345",
  "loanAmount": 10000,
  "loanType": "PERSONAL",
  "purpose": "Vacation",
  "termMonths": 36
}
```

Response:
```json
{
  "loanId": 1,
  "customerName": "John Doe",
  "loanAmount": 10000,
  "loanType": "PERSONAL",
  "status": "APPLIED",
  "applicationDate": "2025-06-09T10:15:30"
}
```

## Testing

The application includes comprehensive unit tests for core services:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=LoanProcessingServiceTest
```

## Solution Design

### Data Model

- **Loan**: Core entity with status, amount, type, and timestamps  
- **Customer**: Borrower information  
- **Agent**: Staff member who reviews loans  
- **Enums**: `ApplicationStatus`, `LoanType` for consistent state management  

### Workflow

1. **Loan application is submitted** (status: `APPLIED`)  
2. **System processes application** (status: `PROCESSING`)  
3. **System makes automated decision**:
   - Small loans are auto-approved (status: `APPROVED_BY_SYSTEM`)  
   - Large loans are auto-rejected (status: `REJECTED_BY_SYSTEM`)  
   - Medium loans need review (status: `PENDING_REVIEW`)  
4. **Loans pending review are assigned to agents** (status: `UNDER_REVIEW`)  
5. **Agents make final decision** (status: `APPROVED_BY_AGENT` or `REJECTED_BY_AGENT`)  

### Architectural Decisions

- **Service Layer**: Contains core business logic, separated from controllers  
- **Repository Pattern**: Data access abstraction for testability  
- **DTO Pattern**: Clean separation between API and domain models  
- **Async Processing**: Background processing for loan evaluation  
