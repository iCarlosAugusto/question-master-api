# Gamified Question API!

A gamified question API inspired by **Duolingo** and **Trivia Track**. The goal is to make learning more dynamic and fun.

## 🚀 Tech Stack

- **Language:** Kotlin with Coroutines
- **Framework:** Spring Boot 3.4.9
- **Database:** PostgreSQL with Flyway migrations
- **Security:** JWT Authentication
- **Documentation:** OpenAPI 3 (Swagger)
- **Build Tool:** Gradle

## 📚 Features

### Core Functionality

- **Question Management:** Create, update, delete questions with multiple choice and true/false types
- **Subject & Topic Management:** Hierarchical organization of content
- **Answer Tracking:** Track user responses with accuracy statistics
- **Authentication & Authorization:** JWT-based security with role-based access control
- **Filtering & Pagination:** Advanced filtering by subject, topic, year, user status

### User Roles

- **ROLE_ADMIN:** Full access (create/update/delete + answer questions)
- **ROLE_USER:** Can answer questions and view own statistics
- **Anonymous:** Read-only access to questions and subjects

## 🛠️ Setup Instructions

### Prerequisites

- Java 21
- Docker & Docker Compose (recommended)
- PostgreSQL 12+ (if not using Docker)
- Gradle 8+ (or use the wrapper)

### Option 1: Docker Setup (Recommended)

#### Quick Start - Development Database Only

```bash
# Windows
scripts\start-dev-db.bat

# Linux/Mac
chmod +x scripts/start-dev-db.sh
./scripts/start-dev-db.sh
```

Then run the application locally:

```bash
./gradlew bootRun
```

#### Full Stack with Docker

```bash
# Windows
scripts\start-full-stack.bat

# Linux/Mac
chmod +x scripts/start-full-stack.sh
./scripts/start-full-stack.sh
```

#### Manual Docker Commands

```bash
# Development database only
docker-compose -f docker-compose.dev.yml up -d

# Full stack (database + API)
docker-compose up --build -d

# Stop services
docker-compose down
```

### Option 2: Local Setup

#### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE questionmaster_dev;
CREATE USER questionmaster WITH PASSWORD 'questionmaster';
GRANT ALL PRIVILEGES ON DATABASE questionmaster_dev TO questionmaster;
```

#### 2. Clone and Configure

```bash
git clone <repository-url>
cd gamified-question-api
```

#### 3. Run the Application

Using Gradle wrapper:

```bash
./gradlew bootRun
```

Or using your local Gradle:

```bash
gradle bootRun
```

The application will start on `http://localhost:8080`

#### 4. Database Migration

Flyway will automatically run migrations on startup. The application includes sample data for testing.

## 📄 API Documentation

Once the application is running, visit:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

## 🔑 Authentication

### Getting Started

1. **Create an Admin User:**

```bash
curl -X POST http://localhost:8080/api/auth/admin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password123"
  }'
```

2. **Create a Regular User:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

3. **Use the JWT Token:**
   Include the token in subsequent requests:

```bash
curl -X GET http://localhost:8080/api/questions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📊 API Endpoints

### Public Endpoints

- `GET /api/questions` - List questions (with filters)
- `GET /api/questions/{id}` - Get specific question
- `GET /api/subjects` - List all subjects
- `GET /api/topics` - List all topics

### User Endpoints (Requires Authentication)

- `POST /api/answers/questions/{id}` - Answer a question
- `GET /api/answers/my-answers` - Get user's answer history
- `GET /api/answers/my-stats` - Get user's statistics

### Admin Endpoints (Requires ADMIN role)

- `POST /api/questions` - Create question
- `PUT /api/questions/{id}` - Update question
- `DELETE /api/questions/{id}` - Delete question
- `POST /api/subjects` - Create subject
- `POST /api/topics` - Create topic

## 🔧 Configuration

### Environment Profiles

- **dev** (default): Development configuration with verbose logging
- **prod**: Production configuration with environment variables
- **test**: Test configuration with H2 in-memory database

### Environment Variables (Production)

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/questionmaster
DATABASE_USERNAME=questionmaster
DATABASE_PASSWORD=your_password
JWT_SECRET=your_very_long_secret_key
JWT_EXPIRATION=86400000
PORT=8080
CORS_ALLOWED_ORIGINS=https://your-domain.com
```

## 📋 Sample Requests

### Create a Question

```bash
curl -X POST http://localhost:8080/api/questions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "statement": "What is the capital of France?",
    "subjectId": 4,
    "year": 2024,
    "questionType": "MULTIPLE_CHOICE",
    "topicIds": [9],
    "alternatives": [
      {"body": "London", "isCorrect": false},
      {"body": "Paris", "isCorrect": true},
      {"body": "Berlin", "isCorrect": false},
      {"body": "Madrid", "isCorrect": false}
    ]
  }'
```

### Answer a Question

```bash
curl -X POST http://localhost:8080/api/answers/questions/550e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "alternativeId": "650e8400-e29b-41d4-a716-446655440002"
  }'
```

### Filter Questions

```bash
# Get questions by subject
curl "http://localhost:8080/api/questions?subjectId=1&page=0&size=10"

# Get questions answered by user
curl "http://localhost:8080/api/questions?answerStatus=CORRECT" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🧪 Testing

Run tests with:

```bash
./gradlew test
```

## 🗄️ Database Schema

The application uses the following main entities:

- **profiles** - User data and roles
- **subjects** - Subject taxonomy
- **topics** - Topic taxonomy (belongs to subjects)
- **questions** - Questions with metadata
- **alternatives** - Answer choices for questions
- **answers** - User response history

See the migration files in `src/main/resources/db/migration/` for detailed schema.

## 🔍 Troubleshooting

### Common Issues

1. **Database Connection Error**

   - Ensure PostgreSQL is running
   - Verify database credentials in `application-dev.properties`

2. **JWT Token Invalid**

   - Check token expiration
   - Ensure proper Bearer token format

3. **Migration Errors**
   - Clear database and restart application
   - Check Flyway migration files for syntax errors

### Logs

Check application logs for detailed error information:

```bash
tail -f logs/application.log
```

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For support and questions, please contact: support@questionmaster.com
