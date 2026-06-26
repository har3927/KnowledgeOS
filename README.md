# KnowledgeOS

A personal AI-powered learning platform — your learning operating system.

## Tech Stack

- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS, ShadCN UI, TanStack Query, React Router, React Flow, Zustand, Recharts
- **Backend:** Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL, Flyway, OpenAPI
- **AI:** Local LLM via Ollama (abstracted via `AiProvider`, default model `qwen2.5:7b`)

## Quick Start

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Ollama (local AI)

Install [Ollama](https://ollama.com) and pull the recommended model:

```bash
ollama pull qwen2.5:7b
```

Keep Ollama running while using AI features (tutor, quizzes, summaries).

### 3. Backend

```bash
cd backend
mvn spring-boot:run
```

API docs: http://localhost:8080/swagger-ui.html

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

App: http://localhost:5173

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OLLAMA_BASE_URL` | Ollama API URL | `http://localhost:11434` |
| `OLLAMA_MODEL` | Model name | `qwen2.5:7b` |
| `OLLAMA_ENABLED` | Enable/disable local AI | `true` |
| `OLLAMA_TIMEOUT_SECONDS` | Request timeout | `120` |

## Default User

A default user is seeded for local development:
- Email: `demo@knowledgeos.dev`
- Name: `Demo User`

## Project Structure

```
KnowledgeOS/
├── backend/          # Spring Boot API
├── frontend/         # React SPA
└── docker-compose.yml
```
