# Run Commands

## Prerequisites

### 1. Set OpenAI API Key (required for AI features)

```bash
export OPENAI_API_KEY=your_openai_key_here
```

### 2. Android SDK (for Android app only)

```bash
cd ai-resume-builder/android-app
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

---

## Quick Start: Run Everything

```bash
./run.sh
```

This starts all three services:
- **Frontend** at `http://localhost:3000`
- **Backend** at `http://localhost:8080`
- **Android App** on emulator (if running)

---

## Individual Commands

### Frontend (Next.js)

```bash
# Install dependencies (first time)
bun install

# Start dev server
bun dev
```

### Backend (Ktor + H2)

```bash
# Requires OPENAI_API_KEY environment variable
cd ai-resume-builder/backend
OPENAI_API_KEY=your_key_here ./gradlew run
```

### Android App

```bash
# Build and install
cd ai-resume-builder/android-app
./gradlew installDebug

# Launch (with emulator running)
adb shell am start -n com.airesumebuilder/.MainActivity
```

---

## Access Points

| Service | URL |
|--------|-----|
| Frontend | http://localhost:3000 |
| Backend | http://localhost:8080 |
| Admin Dashboard | http://localhost:3000/admin |

---

## Troubleshooting

### Backend won't start with "Rate limit exceeded"
- You're using the free tier (3 requests/day). Upgrade or wait until tomorrow.

### Android app shows connection error
- Make sure backend is running first
- Check the IP address in `android-app/app/build.gradle.kts` matches your computer's IP

### 500 Internal Server Error on resume generation
- Check the backend console for error details
- Verify OPENAI_API_KEY is set correctly