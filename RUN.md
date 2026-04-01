# Run Commands

## Quick Start (Frontend + Backend)

Run both frontend and backend in parallel using a single command:

```bash
bun install && (cd ai-resume-builder/backend && ./gradlew run) & bun dev
```

This starts:
- **Backend** (Ktor) on `http://localhost:8080`
- **Frontend** (Next.js) on `http://localhost:3000`

---

## Individual Commands

### Frontend (Next.js)

```bash
bun install   # Install dependencies (first time only)
bun dev       # Start dev server at http://localhost:3000
```

### Backend (Ktor + H2)

```bash
cd ai-resume-builder/backend
./gradlew run   # Start server at http://localhost:8080
```

### Backend with Docker (PostgreSQL)

```bash
cd ai-resume-builder
OPENAI_API_KEY=<your-key> docker compose up --build
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

## Ports

| Service  | Port |
|----------|------|
| Frontend | 3000 |
| Backend  | 8080 |
