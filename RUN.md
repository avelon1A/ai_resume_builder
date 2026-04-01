# Run Commands

## Quick Start (Frontend + Backend)

Run both frontend and backend in parallel using a single command:

```bash
bun install && (cd ai-resume-builder/backend && OPENAI_API_KEY=$OPENAI_API_KEY ./gradlew run) & bun dev
```

This starts:
- **Backend** (Ktor) on `http://localhost:8080`
- **Frontend** (Next.js) on `http://localhost:3000`

---

## Individual Commands

### Frontend (Next.js)

Install dependencies (first time only):

```bash
bun install
```

Start dev server at http://localhost:3000:

```bash
bun dev
```

### Backend (Ktor + H2)

Start server at http://localhost:8080:

```bash
cd ai-resume-builder/backend
OPENAI_API_KEY=$OPENAI_API_KEY ./gradlew run
```

### Backend with Docker (PostgreSQL)

```bash
cd ai-resume-builder
OPENAI_API_KEY=$OPENAI_API_KEY docker compose up --build
```

---

## Build & Check

| Command | Purpose |
|---------|---------|
| `bun build` | Production build (frontend) |
| `bun lint` | Lint frontend code |
| `bun typecheck` | Type-check frontend code |
| `./gradlew build` | Build backend |

---

## Android App

### Prerequisites

Set your Android SDK path (first time only):

```bash
cd ai-resume-builder/android-app
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

### Build & Run

Build debug APK:

```bash
cd ai-resume-builder/android-app
./gradlew assembleDebug
```

Install on emulator/device (must have emulator running or device connected):

```bash
cd ai-resume-builder/android-app
./gradlew installDebug
```

Build + Install + Launch in one command:

```bash
cd ai-resume-builder/android-app
./gradlew installDebug && adb shell am start -n com.airesumebuilder/.MainActivity
```

### Run Tests

```bash
cd ai-resume-builder/android-app
./gradlew test
```

---

## Ports

| Service  | Port |
|----------|------|
| Frontend | 3000 |
| Backend  | 8080 |
