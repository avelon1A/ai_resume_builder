# AI Resume Builder

A production-ready SaaS application for generating professional resumes and cover letters using AI.

## Architecture

```
Android App (Kotlin + Jetpack Compose)
        ↓
  Ktor Backend API
        ↓
    OpenAI API
```

## Backend

### Tech Stack
- **Language**: Kotlin
- **Framework**: Ktor 2.3.12
- **Database**: H2 (dev) / PostgreSQL (prod) with Exposed ORM
- **Auth**: JWT with bcrypt password hashing
- **AI**: OpenAI API integration

### Setup

1. **Clone and configure**:
```bash
cd ai-resume-builder/backend
cp ../.env.example .env
# Edit .env with your OPENAI_API_KEY
```

2. **Generate Gradle wrapper** (if not present):
```bash
gradle wrapper --gradle-version 8.9
```

3. **Run locally** (uses H2 in-memory database):
```bash
./gradlew run
```

4. **Run with Docker** (uses PostgreSQL):
```bash
cd ai-resume-builder
docker compose up --build
```

### API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | No | Register new user |
| POST | `/auth/login` | No | Login and get JWT |
| POST | `/api/generate-resume` | Yes | Generate resume with AI |
| POST | `/api/generate-cover-letter` | Yes | Generate cover letter |
| POST | `/api/analyze-resume` | Yes | Analyze resume quality |
| GET | `/api/resumes` | Yes | List user's resumes |
| GET | `/api/resumes/{id}` | Yes | Get single resume |
| PUT | `/api/resumes/{id}` | Yes | Update resume |
| DELETE | `/api/resumes/{id}` | Yes | Delete resume |

### Rate Limiting
- **Free tier**: 3 AI requests per day
- **Premium**: Unlimited requests

## Android App

### Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp
- **Storage**: DataStore
- **Navigation**: Compose Navigation
- **PDF**: iText 7

### Features
- User authentication (register/login)
- Resume builder with form input
- AI-powered resume generation
- Resume editor with save/export
- Cover letter generator
- Resume template selection (Modern, Minimal, Classic)
- PDF export (ATS-friendly)
- My Resumes management
- Subscription paywall screen

### Setup

1. Open `android-app` in Android Studio
2. Generate Gradle wrapper if prompted (Android Studio usually does this automatically)
3. Sync Gradle
4. Run on emulator or device

The default `BASE_URL` points to `http://10.0.2.2:8080` (Android emulator localhost).

For physical devices, update `BASE_URL` in `app/build.gradle.kts` to your backend's IP address.

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `OPENAI_API_KEY` | Yes | Your OpenAI API key |
| `JWT_SECRET` | No | JWT signing secret |
| `DB_DRIVER` | No | JDBC driver class |
| `DB_URL` | No | JDBC connection URL |
| `DB_USER` | No | Database username |
| `DB_PASSWORD` | No | Database password |

## Database Schema

### Users Table
- `id` (UUID, PK)
- `email` (unique)
- `password_hash`
- `name`
- `subscription_tier` (free/premium)
- `api_requests_today`
- `last_request_date`

### Resumes Table
- `id` (UUID, PK)
- `user_id` (FK → users)
- `title`
- `content` (text)
- `template`
- `created_at`, `updated_at`

## Deployment

The backend is containerized and ready for deployment on any Docker-compatible platform:

```bash
# Build and run
docker compose up --build

# Or build image only
docker build -t resume-builder-backend ./backend
```

For production, use PostgreSQL and set proper environment variables.

## Project Structure

```
ai-resume-builder/
├── .env.example
├── docker-compose.yml
├── backend/
│   ├── Dockerfile
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/airesumebuilder/
│       ├── Application.kt
│       ├── auth/
│       ├── ai/
│       ├── database/
│       ├── models/
│       ├── plugins/
│       ├── routes/
│       └── services/
└── android-app/
    ├── build.gradle.kts
    └── app/src/main/java/com/airesumebuilder/
        ├── data/
        ├── di/
        ├── domain/
        ├── navigation/
        ├── presentation/
        └── ui/
```
