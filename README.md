#  Eagle Bank REST API

This repository is for Eagle Bank REST API. It implements a Spring Boot 3 (Java 17) service with:

- **JWT Bearer Authentication**
- **CRUD for Users** (`/v1/users`)
- **CRUD for Bank Accounts** (`/v1/accounts`)
- **Deposit/Withdrawal Transactions** (`/v1/accounts/{acct}/transactions`)
- **OpenAPI 3.1–first** design with Swagger UI

---

##  Features

| Category       | Details                                                   |
| -------------- | --------------------------------------------------------- |
| **Security**   | JWT auth, password hashing, Spring Security filters       |
| **API Spec**   | OpenAPI 3.1 contract (`openapi.yaml`), Swagger UI         |
| **Persistence**| Spring Data JPA, PostgreSQL                              |
| **Validation** | javax.validation annotations + custom error responses     |
| **Testing**    | JUnit 5 unit tests with Mockito for service layer         |

---

##  Getting Started

> **Prerequisites**  
> • Java 17 • Maven 3.9+ • PostgreSQL 

1. **Build the project**

   ```bash
   ./mvnw clean package

##  Configure the Database

To set up the PostgreSQL database for this project, follow these steps:

1. **Create a PostgreSQL database named `eagleBank`**
2. **Create a user and password that match the configuration in `src/main/resources/application.yml`**

> **Default credentials:**
> - **Username:** `YOUR_USERNAME`
> - **Password:** `YOUR_PASSWORD`

###  Setup Instructions

Run the following command in your terminal to open the PostgreSQL prompt:

```bash
psql -U postgres
```
## 2. Create the Database and User

Once you're inside the `psql` prompt, execute the following SQL commands:

```bash
CREATE DATABASE eagleBank;

CREATE USER YOUR_USERNAME WITH ENCRYPTED PASSWORD 'YOUR_PASSWORD';

GRANT ALL PRIVILEGES ON DATABASE eagleBank TO YOUR_USERNAME;

```

##  Authentication

All endpoints **except** `POST /v1/users` require a Bearer JWT obtained via:

### `POST /auth/login`

**Request Headers:**

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

**Response (200 OK)** 

```json
{
  "token": "eyJh…signed.jwt…"
}
```

##  User Endpoints

| Method | Path                  | Description                        |
|--------|-----------------------|------------------------------------|
| POST   | /v1/users             | Register a new user (public)       |
| GET    | /v1/users/{userId}    | Fetch your profile                 |
| PATCH  | /v1/users/{userId}    | Update your profile                |
| DELETE | /v1/users/{userId}    | Delete your profile (if no accounts) |


##  Example: Create User

### Request

**POST** `/v1/users`  
**Content-Type:** `application/json`

```json
{
  "name": "Test User",
  "email": "user@example.com",
  "phoneNumber": "+447123456789",
  "password": "secret123",
  "address": {
    "line1": "123 Main St",
    "town": "London",
    "county": "Greater London",
    "postcode": "W1A 1AA"
  }
}
```

### Response (201 Created)

```json
{
  "id": "usr-abc123",
  "name": "Test User",
  "email": "user@example.com",
  "phoneNumber": "+447123456789",
  "address": {
    "line1": "123 Main St",
    "line2": "",
    "line3": "",
    "town": "London",
    "county": "Greater London",
    "postcode": "W1A 1AA"
  },
  "createdTimestamp": "2025-07-07T15:21:30Z",
  "updatedTimestamp": "2025-07-07T15:21:30Z"
}
```
##  Account Endpoints

| Method | Path                          | Description           |
|--------|-------------------------------|-----------------------|
| POST   | /v1/accounts                  | Create a bank account |
| GET    | /v1/accounts                  | List your accounts    |
| GET    | /v1/accounts/{accountNumber} | Fetch one account     |
| PATCH  | /v1/accounts/{accountNumber} | Update account        |
| DELETE | /v1/accounts/{accountNumber} | Close an account      |

## Example: Create Account

### Request

**POST** `/v1/accounts`  
**Headers:**
- `Authorization: Bearer eyJh…signed.jwt…`
- `Content-Type: application/json`

```json
{
  "name": "Personal Account",
  "accountType": "personal"
}
```

### Response (201 Created)

```json
{
  "accountNumber": "01234567",
  "sortCode": "10-10-10",
  "name": "Personal Account",
  "accountType": "personal",
  "balance": 0.00,
  "currency": "GBP",
  "createdTimestamp": "2025-07-07T15:22:00Z",
  "updatedTimestamp": "2025-07-07T15:22:00Z"
}
```

## Transaction Endpoints

| Method | Path                                                           | Description           |
|--------|----------------------------------------------------------------|-----------------------|
| POST   | /v1/accounts/{acct}/transactions                               | Deposit or withdraw   |
| GET    | /v1/accounts/{acct}/transactions                               | List transactions     |
| GET    | /v1/accounts/{acct}/transactions/{transactionId}              | Fetch one transaction |


##  Example: Deposit

### Request

**POST** `/v1/accounts/01234567/transactions`  
**Headers:**
- `Authorization: Bearer eyJh…signed.jwt…`
- `Content-Type: application/json`

```json
{
  "amount": 250.00,
  "currency": "GBP",
  "type": "deposit",
  "reference": "Initial funding"
}

```

### Response (201 Created)

```json
{
  "id": "tan-6f8c2a",
  "amount": 250.00,
  "currency": "GBP",
  "type": "deposit",
  "reference": "Initial funding",
  "userId": "usr-abc123",
  "createdTimestamp": "2025-07-07T15:23:11Z"
}
```

## Example: Withdrawal (Insufficient Funds)

### Request

**POST** `/v1/accounts/01234567/transactions`  
**Headers:**
- `Authorization: Bearer eyJh…signed.jwt…`
- `Content-Type: application/json`

```json
{
  "amount": 1000.00,
  "currency": "GBP",
  "type": "withdrawal"
}
```

### Response (422 Unprocessable Entity)

```json
{
  "message": "Insufficient funds"
}
```
##  Running Tests

```bash
./mvnw test

