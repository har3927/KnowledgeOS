# KnowledgeOS

A minimalist, highly interactive technology-only learning platform — your personalized learning operating system. It features interactive learning slides, gamified active recall quiz quests, spaced repetition flashcard review, and Feynman Sandbox writing critiques with AI-generated feedback.

---

## Tech Stack

* **Frontend**: React 19, TypeScript, Vite, Tailwind CSS, ShadCN UI, TanStack Query, React Router, Zustand, Recharts
* **Backend**: Java 21, Spring Boot 3.5, Spring Data JPA, H2 Database (PostgreSQL compatibility mode), Flyway Migrations, OpenAPI
* **AI**: OpenAI-compatible API Integration (defaults to high-performance NVIDIA API)
* **Storage**: Persistent local file-based H2 storage (`db/knowledgeos`)

---

## Environment Variables

### Backend Configuration

| Environment Variable | Description | Default / Fallback |
| :--- | :--- | :--- |
| `OPENAI_API_KEY` | Your AI provider API secret key | *(Required for AI features)* |
| `OPENAI_BASE_URL` | Base URL of the OpenAI-compatible endpoint | `https://integrate.api.nvidia.com/v1` |
| `OPENAI_MODEL` | AI model name | `z-ai/glm-5.1` |
| `OPENAI_ENABLED` | Enable or disable AI-driven tutor features | `true` |
| `OPENAI_TIMEOUT_SECONDS` | Network request timeout in seconds | `60` |
| `JDBC_DATABASE_URL` | JDBC database connection string | `jdbc:h2:file:./db/knowledgeos;MODE=PostgreSQL...` |
| `ALLOWED_ORIGINS` | Permitted CORS origins (comma-separated patterns) | `http://localhost:5173,http://localhost:3000` |

### Frontend Configuration

| Environment Variable | Description | Default / Fallback |
| :--- | :--- | :--- |
| `VITE_API_URL` | Backend server URL endpoint | `/api` *(Local dev proxies to `http://localhost:8080`)* |

---

## Quick Start

### 1. Setup Environment
Set your OpenAI/NVIDIA API Key:
```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY="your-nvapi-key"

# Linux / macOS
export OPENAI_API_KEY="your-nvapi-key"
```

### 2. Start the Backend
Runs Flyway migrations and starts the Spring Boot server on port `8080` using a local persistent file database.
```bash
cd backend
mvn spring-boot:run
```
* **API Documentation & Swagger UI**: http://localhost:8080/swagger-ui.html

### 3. Start the Frontend
Installs dependencies and runs the Vite development server.
```bash
cd frontend
npm install
npm run dev
```
* **Application URL**: http://localhost:5173

---

## Project Structure

```
KnowledgeOS/
├── backend/          # Spring Boot API & H2 Local Database File Store
├── frontend/         # React SPA (Dashboard, Topics, and Revisions pages)
└── README.md
```

## Production Deployment

* **Backend**: Host on any container/Java platform (like Render or Railway) using the included `Dockerfile` and mount a **Persistent Volume Disk** to preserve H2 data at `/var/data` (set `JDBC_DATABASE_URL` accordingly).
* **Frontend**: Deploy to **Vercel** as a static project. The included `vercel.json` maps routing proxy rules and client route fallbacks automatically.
